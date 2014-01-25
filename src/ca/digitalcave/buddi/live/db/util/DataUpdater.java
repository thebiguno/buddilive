package ca.digitalcave.buddi.live.db.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.restlet.Application;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Entries;
import ca.digitalcave.buddi.live.db.ScheduledTransactions;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.model.ScheduledTransaction;
import ca.digitalcave.buddi.live.model.ScheduledTransaction.ScheduleFrequency;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.common.DateUtil;
import ca.digitalcave.moss.crypto.Crypto;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class DataUpdater {

	public static void updateBalances(User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		final BuddiApplication application = (BuddiApplication) Application.getCurrent();
		
		//Look through all splits in all accounts.  Start with the earliest split in each account.  If we find a
		// split which does not have the correct balance, we update it; if the balance is already good, leave it alone.
		final List<Split> splits = sqlSession.getMapper(Transactions.class).selectSplits(user);
		final Map<Integer, List<Split>> splitsByAccount = new HashMap<Integer, List<Split>>();
		for (Split split : splits) {
			//Put the splits into a map by account ID.  A split can be associated with two accounts, so we don't do 'else if' below.
			if ("C".equals(split.getFromType()) || "D".equals(split.getFromType())){
				final int accountId = split.getFromSource();
				if (splitsByAccount.get(accountId) == null) splitsByAccount.put(accountId, new ArrayList<Split>());
				splitsByAccount.get(accountId).add(split);
			}
			if ("C".equals(split.getToType()) || "D".equals(split.getToType())){
				final int accountId = split.getToSource();
				if (splitsByAccount.get(accountId) == null) splitsByAccount.put(accountId, new ArrayList<Split>());
				splitsByAccount.get(accountId).add(split);
			}
		}
		
		final List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccounts(user);
		for (Account account : accounts){
			if (splitsByAccount.get(account.getId()) == null) splitsByAccount.put(account.getId(), new ArrayList<Split>());
			BigDecimal previousBalance = CryptoUtil.decryptWrapperBigDecimal(account.getStartBalance(), user, true);
			BigDecimal newBalance = previousBalance;
			int count = 0;
			for (Split split : splitsByAccount.get(account.getId())) {
				if (split.getFromSource() == account.getId()){
					newBalance = previousBalance.subtract(CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true));
					BigDecimal splitFromBalance = CryptoUtil.decryptWrapperBigDecimal(split.getFromBalance(), user, false);
					if (splitFromBalance == null || splitFromBalance.compareTo(newBalance) != 0) {
						final String encodedNewBalance = user.isEncrypted() ? application.getCrypto().encrypt(user.getDecryptedSecretKey(), newBalance.toPlainString()) : newBalance.toPlainString();
						count = sqlSession.getMapper(Transactions.class).updateSplitBalance(user, split.getId(), encodedNewBalance, true);
						if (count != 1) throw new DatabaseException("Expected 1 split row updated; returned " + count);
					}
				}
				else if (split.getToSource() == account.getId()){
					newBalance = previousBalance.add(CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true));
					BigDecimal splitToBalance = CryptoUtil.decryptWrapperBigDecimal(split.getToBalance(), user, false);
					if (splitToBalance == null || splitToBalance.compareTo(newBalance) != 0) {
						final String encodedNewBalance = user.isEncrypted() ? application.getCrypto().encrypt(user.getDecryptedSecretKey(), newBalance.toPlainString()) : newBalance.toPlainString();
						count = sqlSession.getMapper(Transactions.class).updateSplitBalance(user, split.getId(), encodedNewBalance, false);
						if (count != 1) throw new DatabaseException("Expected 1 split row updated; returned " + count);
					}
				}
				else {
					throw new DatabaseException("This should never happen");
				}
				
				previousBalance = newBalance;
			}

			//Set the account balance to the latest balance if it has changed.
			if (newBalance.compareTo(CryptoUtil.decryptWrapperBigDecimal(account.getBalance(), user, true)) != 0){
				final String encodedNewBalance = user.isEncrypted() ? application.getCrypto().encrypt(user.getDecryptedSecretKey(), newBalance.toPlainString()) : newBalance.toPlainString();
				count = sqlSession.getMapper(Sources.class).updateAccountBalance(user, account.getId(), encodedNewBalance);
				if (count != 1) throw new DatabaseException("Expected 1 account row updated; returned " + count);
			}
		}
	}
	
	/**
	 * Runs through the list of scheduled transactions, and adds any which
	 * show be executed to the appropriate transactions list.
 	 * Checks for the frequency type and based on it finds if a transaction is scheduled for a date
 	 * that has gone past.
 	 * 
 	 * This method includes an argument to specify what the current date is.  This can
 	 * be useful if you want to add transactions after the current date.
 	 * 
 	 * Returns one of:
 	 * a) null if nothing has been added
 	 * b) string containing all messages for scheduled transactions which had been added (or an empty string if transactions were added, but there were no messages)
	 */
	public static String updateScheduledTransactions(User user, SqlSession sqlSession, Date userDate) throws CryptoException, DatabaseException {
		//Update any scheduled transactions
		final Date today = DateUtil.getEndOfDay(userDate);
		
		//If there was at least one update, we will force a recalculation of all balances.
		boolean thereWasAnUpate = false;
		
		final StringBuilder sb = new StringBuilder();
		
		//We specify a GregorianCalendar because we make some assumptions
		// about numbering of months, etc that may break if we 
		// use the default calendar for the locale.  It's not the
		// prettiest code, but it works.  Perhaps we can change
		// it to be cleaner later on...
		final GregorianCalendar tempCal = new GregorianCalendar();

		final List<ScheduledTransaction> scheduledTransactions = sqlSession.getMapper(ScheduledTransactions.class).selectOustandingScheduledTransactions(user);
		
		for (ScheduledTransaction scheduledTransaction : scheduledTransactions) {
			Date tempDate = scheduledTransaction.getLastCreatedDate();
			//#1779286 Bug BiWeekly Scheduled Transactions -Check if this transaction has never been created. 
			boolean isNewTransaction=false;
			//The lastDayCreated need to date as such without rolling forward by a day and the start fo the day 
			//so calculations of difference of days are on the same keel as tempDate.
			Date lastDayCreated = null;
			//Temp date is where we will start looping from.
			if (tempDate == null){
				//If it is null, we need to init it to a sane value.
				tempDate = scheduledTransaction.getStartDate();
				isNewTransaction=true;
				//The below is just to avoid NPE's; ideally changing the order 
				// of the checking below will solve the problem, but better safe than sorry.
				//The reason we set this date to an impossibly early date is to ensure
				// that we include a scheduled transaction on the first day that matches,
				// even if that day is the first day of any scheduled transactions.
				lastDayCreated=DateUtil.getDate(1900);
			}
			else {
				lastDayCreated = DateUtil.getStartOfDay(tempDate);
				//We start one day after the last day, to avoid repeats.  
				// See bug #1641937 for more details.
				tempDate = DateUtil.addDays(tempDate, 1);

			}



			tempDate = DateUtil.getStartOfDay(tempDate);

			//The transaction is scheduled for a date before today and before the EndDate 
			while (tempDate.before(today) 
					&& (scheduledTransaction.getEndDate() == null 
							|| scheduledTransaction.getEndDate().after(tempDate)
							|| (DateUtil.getDaysBetween(scheduledTransaction.getEndDate(), tempDate, false) == 0))) {

				//We use a Calendar instead of a Date object for comparisons
				// because the Calendar interface is much nicer.
				tempCal.setTime(tempDate);

				boolean todayIsTheDay = false;

				//We check each type of schedule, and if it matches,
				// we set todayIsTheDay to true.  We could do it 
				// all in one huge if statement, but that is very
				// hard to read and maintain.

				//If we are using the Monthly by Date frequency, 
				// we only check if the given day is equal to the
				// scheduled day.
				if (scheduledTransaction.getFrequencyType().equals(ScheduleFrequency.SCHEDULE_FREQUENCY_MONTHLY_BY_DATE.toString())
						&& (scheduledTransaction.getScheduleDay() == tempCal.get(Calendar.DAY_OF_MONTH) 
								|| (scheduledTransaction.getScheduleDay() == 32 //Position 32 is 'Last Day of Month'.  ScheduleFrequencyDayOfMonth.SCHEDULE_DATE_LAST_DAY.ordinal() + 1
										&& tempCal.get(Calendar.DAY_OF_MONTH) == tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)))){

					todayIsTheDay = true;
				}
				//If we are using the Monthly by Day of Week,
				// we check if the given day (Sunday, Monday, etc) is equal to the
				// scheduleDay, and if the given day is within the first week.
				// FYI, we store Sunday == 0, even though Calendar.SUNDAY == 1.  Thus,
				// we add 1 to our stored day before comparing it.
				else if (scheduledTransaction.getFrequencyType().equals(ScheduleFrequency.SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK.toString())
						&& scheduledTransaction.getScheduleDay() + 1 == tempCal.get(Calendar.DAY_OF_WEEK)
						&& tempCal.get(Calendar.DAY_OF_MONTH) <= 7){
					todayIsTheDay = true;
				}
				//If we are using Weekly frequency, we only need to compare
				// the number of the day.
				// FYI, we store Sunday == 0, even though Calendar.SUNDAY == 1.  Thus,
				// we add 1 to our stored day before comparing it.
				else if (scheduledTransaction.getFrequencyType().equals(ScheduleFrequency.SCHEDULE_FREQUENCY_WEEKLY.toString())
						&& scheduledTransaction.getScheduleDay() + 1 == tempCal.get(Calendar.DAY_OF_WEEK)){
					todayIsTheDay = true;
				}
				//If we are using BiWeekly frequency, we need to compare
				// the number of the day as well as ensure that there is one
				// week between each scheduled transaction.
				// FYI, we store Sunday == 0, even though Calendar.SUNDAY == 1.  Thus,
				// we add 1 to our stored day before comparing it.
				else if (scheduledTransaction.getFrequencyType().equals(ScheduleFrequency.SCHEDULE_FREQUENCY_BIWEEKLY.toString())
						&& scheduledTransaction.getScheduleDay() + 1 == tempCal.get(Calendar.DAY_OF_WEEK)
						//As tempdate has been moved forward by one day we need to check if it is >= 13 instead of >13
						&& ((DateUtil.getDaysBetween(lastDayCreated, tempDate, false) >= 13)
								|| isNewTransaction)){
					todayIsTheDay = true;
					lastDayCreated = (Date) tempDate.clone();
					if(isNewTransaction){
						isNewTransaction=false;
					}
				}
				//Every X days, where X is the value in s.getScheduleDay().  Check if we
				// have passed the correct number of days since the last transaction.
				else if (scheduledTransaction.getFrequencyType().equals(ScheduleFrequency.SCHEDULE_FREQUENCY_EVERY_X_DAYS.toString())
						&& DateUtil.getDaysBetween(lastDayCreated, tempDate, false) >= scheduledTransaction.getScheduleDay() ){
					todayIsTheDay = true;
					lastDayCreated = (Date) tempDate.clone();
				}
				//Every day - it's obvious enough even for a monkey!
				else if (scheduledTransaction.getFrequencyType().equals(ScheduleFrequency.SCHEDULE_FREQUENCY_EVERY_DAY.toString())){
					todayIsTheDay = true;
				}
				//Every weekday - all days but Saturday and Sunday.
				else if (scheduledTransaction.getFrequencyType().equals(ScheduleFrequency.SCHEDULE_FREQUENCY_EVERY_WEEKDAY.toString())
						&& (tempCal.get(Calendar.DAY_OF_WEEK) < Calendar.SATURDAY)
						&& (tempCal.get(Calendar.DAY_OF_WEEK) > Calendar.SUNDAY)){
					todayIsTheDay = true;
				}
				//To make this one clearer, we do it in two passes.
				// First, we check the frequency type and the day.
				// If these match, we do our bit bashing to determine
				// if the week is correct.
				else if (scheduledTransaction.getFrequencyType().equals(ScheduleFrequency.SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH.toString())
						&& scheduledTransaction.getScheduleDay() + 1 == tempCal.get(Calendar.DAY_OF_WEEK)){
					int week = scheduledTransaction.getScheduleWeek();
					//The week mask should return 1 for the first week (day 1 - 7), 
					// 2 for the second week (day 8 - 14), 4 for the third week (day 15 - 21),
					// and 8 for the fourth week (day 22 - 28).  We then AND it with 
					// the scheduleWeek to determine if this week matches the criteria
					// or not.
					int weekNumber = tempCal.get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1;
					int weekMask = (int) Math.pow(2, weekNumber);
					if ((week & weekMask) != 0){
						todayIsTheDay = true;
					}
				}
				//To make this one clearer, we do it in two passes.
				// First, we check the frequency type and the day.
				// If these match, we do our bit bashing to determine
				// if the month is correct.
				else if (scheduledTransaction.getFrequencyType().equals(ScheduleFrequency.SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR.toString())
						&& scheduledTransaction.getScheduleDay() == tempCal.get(Calendar.DAY_OF_MONTH)){
					int months = scheduledTransaction.getScheduleMonth();
					//The month mask should be 2 ^ MONTH NUMBER,
					// where January == 0.
					// i.e. 1 for January, 4 for March, 2048 for December.
					int monthMask = (int) Math.pow(2, tempCal.get(Calendar.MONTH));
					if ((months & monthMask) != 0){
						todayIsTheDay = true;
					}
				}

				//Check that there has not already been a scheduled transaction with identical
				// paramters for this day.  This is in response to a potential bug where
				// the last scheduled day is missing (happened once in development 
				// version, but may not be a repeating problem).
				//This has the potential to skip scheduled transactions, if there
				// are multiple scheduled transactions which go to and from the 
				// same accounts / categories on the same day.  If this proves to
				// be a problem, we may make the checks more specific.
				if (todayIsTheDay){
					for (Transaction t : sqlSession.getMapper(Transactions.class).selectTransactions(user, tempDate, tempDate)) {
						if (DateUtil.isSameDay(t.getDate(), tempDate)
								&& t.getScheduledTransactionId() == scheduledTransaction.getId()){
							todayIsTheDay = false;
							scheduledTransaction.setLastCreatedDate(tempDate);
						}
					}
				}
				
				
				//If one of the above rules matches, we will copy the
				// scheduled transaction into the transactions list
				// at the given day.
				if (todayIsTheDay){

					scheduledTransaction.setLastCreatedDate(DateUtil.getEndOfDay(tempDate));

					final String decryptedMessage = CryptoUtil.decryptWrapper(scheduledTransaction.getMessage(), user);
					if (scheduledTransaction.getMessage() != null && StringUtils.isNotBlank(decryptedMessage)){
						sb.append(FormatUtil.formatDate(tempDate, user)).append(": ").append(decryptedMessage).append("<br/>");
					}

					if (tempDate != null && scheduledTransaction.getDescription() != null) {
						final Transaction t = new Transaction();
						t.setDate(tempDate);
						t.setDescription(scheduledTransaction.getDescription());
						t.setNumber(scheduledTransaction.getNumber());
						t.setSplits(new ArrayList<Split>());
						for (Split split : scheduledTransaction.getSplits()) {
							split.setId(null);	//Reset ID and parent ID since we will be re-saving it back to another table
							split.setTransactionId(null);
							t.getSplits().add(split);
						}
						t.setScheduledTransactionId(scheduledTransaction.getId());

						ConstraintsChecker.checkInsertTransaction(t, user, sqlSession);
						int count = sqlSession.getMapper(Transactions.class).insertTransaction(user, t);
						if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
						for (Split split : t.getSplits()) {
							split.setTransactionId(t.getId());
							
							count = sqlSession.getMapper(Transactions.class).insertSplit(user, split);
							if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
						}

						ConstraintsChecker.checkUpdateScheduledTransaction(scheduledTransaction, user, sqlSession);
						sqlSession.getMapper(ScheduledTransactions.class).updateScheduledTransaction(user, scheduledTransaction);
						
						thereWasAnUpate = true;
					}
				}
				else {
				}

				tempDate = DateUtil.addDays(tempDate, 1);
			}
		}
		
		if (thereWasAnUpate){
			DataUpdater.updateBalances(user, sqlSession);
		}
		
		final String messages = sb.toString();
		return (thereWasAnUpate ? messages : null);
	}
	
	public static void turnOnEncryption(User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		//To turn on encryption, we must do the following:
		// a) Generate a new random key for the user
		// b) Encrypt this key using their password
		// c) ASCII-armour the key, and store it in their user table
		// d) Iterate through all sources, transactions, and split in the system, and encrypt the appropriate fields.  Encryptable fields are:
		//   i) Source name
		//   ii) Account type
		//   iii) Transaction description
		//   iv) Transaction number
		//	 v) Split memo,
		//   vi) Scheduled transaction name, description, number, split memo
		
		final BuddiApplication application = (BuddiApplication) Application.getCurrent();
		if (user.isEncrypted()) throw new DatabaseException("This account is already encrypted");
		
		final String password = user.getPlaintextSecret();
		final SecretKey key = application.getCrypto().generateSecretKey();
		user.setEncryptionKey(application.getCrypto().encrypt(password, Crypto.encodeSecretKey(key)));
		
		int count = sqlSession.getMapper(Users.class).updateUserEncryptionKey(user);
		if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
		
		for (Account a : sqlSession.getMapper(Sources.class).selectAccounts(user)) {
			a.setName(application.getCrypto().encrypt(key, a.getName()));
			a.setAccountType(application.getCrypto().encrypt(key, a.getAccountType()));
			sqlSession.getMapper(Sources.class).updateAccount(user, a);
		}
		for (Category c : sqlSession.getMapper(Sources.class).selectCategories(user)) {
			c.setName(application.getCrypto().encrypt(key, c.getName()));
			sqlSession.getMapper(Sources.class).updateCategory(user, c);
		}
		for (Transaction t : sqlSession.getMapper(Transactions.class).selectTransactions(user)){
			t.setDescription(application.getCrypto().encrypt(key, t.getDescription()));
			t.setNumber(application.getCrypto().encrypt(key, t.getNumber()));
			sqlSession.getMapper(Transactions.class).updateTransaction(user, t);
		}
		for (Split s : sqlSession.getMapper(Transactions.class).selectSplits(user)){
			s.setMemo(application.getCrypto().encrypt(key, s.getMemo()));
			sqlSession.getMapper(Transactions.class).updateSplit(user, s);
		}
		for (ScheduledTransaction st : sqlSession.getMapper(ScheduledTransactions.class).selectScheduledTransactions(user)){
			st.setScheduleName(application.getCrypto().encrypt(key, st.getScheduleName()));
			st.setDescription(application.getCrypto().encrypt(key, st.getDescription()));
			st.setNumber(application.getCrypto().encrypt(key, st.getNumber()));
			st.setMessage(application.getCrypto().encrypt(key, st.getMessage()));
			sqlSession.getMapper(ScheduledTransactions.class).updateScheduledTransaction(user, st);
			for (Split s : st.getSplits()) {
				s.setMemo(application.getCrypto().encrypt(key, s.getMemo()));
				sqlSession.getMapper(ScheduledTransactions.class).updateScheduledSplit(user, s);
			}
		}
	}
	
	public static void turnOffEncryption(User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		//To turn off encryption, we must do the following:
		// a) Iterate through all sources and transactions in the system, and decrypt the appropriate fields (see above for encrypted fields).
		// b) Set the encryption key to null, and store it in the user table
		
		if (!user.isEncrypted()) throw new DatabaseException("This account is not encrypted");
		
		final SecretKey key = user.getDecryptedSecretKey();
		user.setEncryptionKey(null);
		int count = sqlSession.getMapper(Users.class).updateUserEncryptionKey(user);
		if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));

		for (Account a : sqlSession.getMapper(Sources.class).selectAccounts(user)) {
			a.setName(Crypto.decrypt(key, a.getName()));
			a.setAccountType(Crypto.decrypt(key, a.getAccountType()));
			sqlSession.getMapper(Sources.class).updateAccount(user, a);
		}
		for (Category c : sqlSession.getMapper(Sources.class).selectCategories(user)) {
			c.setName(Crypto.decrypt(key, c.getName()));
			sqlSession.getMapper(Sources.class).updateCategory(user, c);
		}
		for (Transaction t : sqlSession.getMapper(Transactions.class).selectTransactions(user)){
			t.setDescription(Crypto.decrypt(key, t.getDescription()));
			t.setNumber(Crypto.decrypt(key, t.getNumber()));
			sqlSession.getMapper(Transactions.class).updateTransaction(user, t);
		}
		for (Split s : sqlSession.getMapper(Transactions.class).selectSplits(user)){
			if (s.getMemo() != null){
				s.setMemo(Crypto.decrypt(key, s.getMemo()));
				sqlSession.getMapper(Transactions.class).updateSplit(user, s);
			}
		}
		for (ScheduledTransaction st : sqlSession.getMapper(ScheduledTransactions.class).selectScheduledTransactions(user)){
			try { st.setScheduleName(Crypto.decrypt(key, st.getScheduleName())); } catch (CryptoException e){}
			st.setDescription(Crypto.decrypt(key, st.getDescription()));
			st.setNumber(Crypto.decrypt(key, st.getNumber()));
			try { st.setMessage(Crypto.decrypt(key, st.getMessage())); } catch (CryptoException e){}
			sqlSession.getMapper(ScheduledTransactions.class).updateScheduledTransaction(user, st);
			for (Split s : st.getSplits()) {
				s.setMemo(Crypto.decrypt(key, s.getMemo()));
				sqlSession.getMapper(ScheduledTransactions.class).updateScheduledSplit(user, s);
			}
		}
	}
	
	public static void upgradeEncryptionFrom0(User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		if (!user.isEncrypted()) {
			//If the account is not encrypted, there is nothing to be done; just update the encryption version number.
			sqlSession.getMapper(Users.class).updateUserEncryptionVersion(user, 2);
			return;
		}
		
		final BuddiApplication application = (BuddiApplication) Application.getCurrent();
		final String password = user.getPlaintextSecret();
		final String oldEncryptionKey = user.getDecryptedEncryptionKey();
		final SecretKey key = application.getCrypto().generateSecretKey();
		final Crypto crypto = application.getCrypto();

		for (Account a : sqlSession.getMapper(Sources.class).selectAccounts(user)) {
			a.setName(crypto.encrypt(key, Crypto.decrypt(oldEncryptionKey, a.getName())));
			a.setAccountType(crypto.encrypt(key, Crypto.decrypt(oldEncryptionKey, a.getAccountType())));
			sqlSession.getMapper(Sources.class).updateAccount(user, a);
		}
		for (Category c : sqlSession.getMapper(Sources.class).selectCategories(user)) {
			c.setName(crypto.encrypt(key, Crypto.decrypt(oldEncryptionKey, c.getName())));
			sqlSession.getMapper(Sources.class).updateCategory(user, c);
		}
		for (Transaction t : sqlSession.getMapper(Transactions.class).selectTransactions(user)){
			t.setDescription(crypto.encrypt(key, Crypto.decrypt(oldEncryptionKey, t.getDescription())));
			t.setNumber(crypto.encrypt(key, Crypto.decrypt(oldEncryptionKey, t.getNumber())));
			sqlSession.getMapper(Transactions.class).updateTransaction(user, t);
		}
		for (Split s : sqlSession.getMapper(Transactions.class).selectSplits(user)){
			if (s.getMemo() != null){
				s.setMemo(crypto.encrypt(key, Crypto.decrypt(oldEncryptionKey, s.getMemo())));
				sqlSession.getMapper(Transactions.class).updateSplit(user, s);
			}
		}
		for (ScheduledTransaction st : sqlSession.getMapper(ScheduledTransactions.class).selectScheduledTransactions(user)){
			//Some scheduled transaction fields can be corrupted in DB; ignore decryption errors
			try { st.setScheduleName(Crypto.decrypt(oldEncryptionKey, st.getScheduleName())); } catch (CryptoException e){}
			st.setScheduleName(crypto.encrypt(key, st.getScheduleName()));
			st.setDescription(crypto.encrypt(key, Crypto.decrypt(oldEncryptionKey, st.getDescription())));
			st.setNumber(crypto.encrypt(key, Crypto.decrypt(oldEncryptionKey, st.getNumber())));
			try { st.setMessage(Crypto.decrypt(oldEncryptionKey, st.getMessage())); } catch (CryptoException e){}
			crypto.encrypt(key, st.getMessage());
			sqlSession.getMapper(ScheduledTransactions.class).updateScheduledTransaction(user, st);
			for (Split s : st.getSplits()) {
				s.setMemo(crypto.encrypt(key, Crypto.decrypt(oldEncryptionKey, s.getMemo())));
				sqlSession.getMapper(ScheduledTransactions.class).updateScheduledSplit(user, s);
			}
		}
		
		user.setEncryptionKey(application.getCrypto().encrypt(password, Crypto.encodeSecretKey(key)));
		sqlSession.getMapper(Users.class).updateUserEncryptionKey(user);
		sqlSession.getMapper(Users.class).updateUserEncryptionVersion(user, 1);
		
		upgradeEncryptionFrom1(user, sqlSession);
	}
	
	public static void upgradeEncryptionFrom1(User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		if (!user.isEncrypted()) {
			//If the account is not encrypted, there is nothing to be done; just update the encryption version number.
			sqlSession.getMapper(Users.class).updateUserEncryptionVersion(user, 2);
			return;
		}

		final BuddiApplication application = (BuddiApplication) Application.getCurrent();
		final Crypto crypto = application.getCrypto();
		final SecretKey key = user.getDecryptedSecretKey();
		
		for (Entry entry : sqlSession.getMapper(Entries.class).selectEntries(user)){
			BigDecimal amount = new BigDecimal(entry.getAmount());
			entry.setAmount(crypto.encrypt(key, amount.toPlainString()));
			sqlSession.getMapper(Entries.class).updateEntry(user, entry);
		}
		for (Split split : sqlSession.getMapper(Transactions.class).selectSplits(user)){
			BigDecimal amount = new BigDecimal(split.getAmount());
			split.setAmount(crypto.encrypt(key, amount.toPlainString()));
			sqlSession.getMapper(Transactions.class).updateSplit(user, split);
		}
		for (ScheduledTransaction st : sqlSession.getMapper(ScheduledTransactions.class).selectScheduledTransactions(user)){
			for (Split split : st.getSplits()) {
				BigDecimal amount = new BigDecimal(split.getAmount());
				split.setAmount(crypto.encrypt(key, amount.toPlainString()));
				sqlSession.getMapper(ScheduledTransactions.class).updateScheduledSplit(user, split);
			}
		}
		
		sqlSession.getMapper(Users.class).updateUserEncryptionVersion(user, 2);
		
		updateBalances(user, sqlSession);
	}
}
