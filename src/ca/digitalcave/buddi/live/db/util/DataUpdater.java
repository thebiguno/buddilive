package ca.digitalcave.buddi.live.db.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import ca.digitalcave.buddi.live.db.ScheduledTransactions;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.ScheduledTransaction;
import ca.digitalcave.buddi.live.model.ScheduledTransaction.ScheduleFrequency;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;
import ca.digitalcave.moss.common.DateUtil;

public class DataUpdater {

	public static void updateBalances(User user, SqlSession sqlSession, boolean forceAll) throws DatabaseException {
		//If the forceAll flag is passed, we first clear all balances to ensure a fresh start.  This is a good idea when adding new accounts, etc.
		if (forceAll){
			sqlSession.getMapper(Transactions.class).updateSplitsClearBalances(user);
		}
		
		//Update all balances, starting from the earliest split which has a null balance, and moving forward updating each one in turn.
		//We first loop through all accounts for the user
		final List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccounts(user);
		for (Account account : accounts){
			//We find the oldest split which has null balances for the given account
			final Split earliestSplitWithoutBalances = sqlSession.getMapper(Transactions.class).selectEarliestSplitWithoutBalances(user, account);
			
			//We then find the last split from before this one, for the same account.  If the split is null, then there are no splits without balances in this account.
			if (earliestSplitWithoutBalances != null){
				final Transaction transaction = sqlSession.getMapper(Transactions.class).selectTransaction(user, earliestSplitWithoutBalances.getTransactionId());
				final List<Split> splits = sqlSession.getMapper(Transactions.class).selectSplits(user, account, transaction.getDate());
				for (int i = 0; i < splits.size(); i++) {
					final Split split = splits.get(i);
					
					//We need special handling for this case... if the first split has a null balance, then we need to set the balance to the account's starting balance.
					if (i == 0){
						if (split.getFromSource() == account.getId()){
							if (split.getFromBalance() == null){
								split.setFromBalance((account.getStartBalance() == null ? BigDecimal.ZERO : account.getStartBalance()).subtract(split.getAmount()));
							}
						}
						else {
							if (split.getToBalance() == null){
								split.setToBalance((account.getStartBalance() == null ? BigDecimal.ZERO : account.getStartBalance()).add(split.getAmount()));
							}
						}
					}
					else {
						final Split previous = splits.get(i - 1);
						final BigDecimal previousBalance = (previous.getFromSource() == account.getId() ? previous.getFromBalance() : previous.getToBalance());
						if (split.getFromSource() == account.getId()){
							split.setFromBalance(previousBalance.subtract(split.getAmount()));
						}
						else {
							split.setToBalance(previousBalance.add(split.getAmount()));
						}
					}
					
					final int count = sqlSession.getMapper(Transactions.class).updateSplit(user, split);
					if (count != 1) throw new DatabaseException("Expected 1 split row updated; returned " + count);
				}
			}

			final Split split = sqlSession.getMapper(Transactions.class).selectLatestSplit(user, account);
			if (split != null){
				account.setBalance(split.getFromSource() == account.getId() ? split.getFromBalance() : split.getToBalance());
			}
			else {
				//There are no transactions for this account; default to start balance
				account.setBalance(account.getStartBalance());
			}
			final int count = sqlSession.getMapper(Sources.class).updateAccount(user, account);
			if (count != 1) throw new DatabaseException("Expected 1 account row updated; returned " + count);

		}
		
		sqlSession.commit();
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
 	 * Returns a string containing all messages for scheduled transactions which had been added.
	 */
	public static String updateScheduledTransactions(User user, SqlSession sqlSession) throws CryptoException, DatabaseException {
		//Update any scheduled transactions
		final Date today = DateUtil.getEndOfDay(new Date());
		
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

					if (scheduledTransaction.getMessage() != null && scheduledTransaction.getMessage().trim().length() > 0){
						sb.append(FormatUtil.formatDate(tempDate, user)).append(": ").append(scheduledTransaction.getMessage()).append("<br/>");
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
					}
				}
				else {
				}

				tempDate = DateUtil.addDays(tempDate, 1);
			}
		}
		
		final String messages = sb.toString();
		return (messages.trim().length() == 0 ? null : messages);
	}
	
	public static void turnOnEncryption(User user, String password, SqlSession sqlSession) throws DatabaseException, CryptoException {
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
		
		if (user.isEncrypted()) throw new DatabaseException("This account is already encrypted");
		
		final String encryptionKey = CryptoUtil.encode(CryptoUtil.getSecureRandom(128));
		user.setDecryptedEncryptionKey(encryptionKey);
		user.setEncryptionKey(CryptoUtil.encrypt(encryptionKey, password));
		
		sqlSession.getMapper(Users.class).updateUserEncryptionKey(user);
		
		for (Account a : sqlSession.getMapper(Sources.class).selectAccounts(user)) {
			a.setName(CryptoUtil.encrypt(a.getName(), encryptionKey));
			a.setAccountType(CryptoUtil.encrypt(a.getAccountType(), encryptionKey));
			sqlSession.getMapper(Sources.class).updateAccount(user, a);
		}
		for (Category c : sqlSession.getMapper(Sources.class).selectCategories(user)) {
			c.setName(CryptoUtil.encrypt(c.getName(), encryptionKey));
			sqlSession.getMapper(Sources.class).updateCategory(user, c);
		}
		for (Transaction t : sqlSession.getMapper(Transactions.class).selectTransactions(user)){
			t.setDescription(CryptoUtil.encrypt(t.getDescription(), encryptionKey));
			t.setNumber(CryptoUtil.encrypt(t.getNumber(), encryptionKey));
			sqlSession.getMapper(Transactions.class).updateTransaction(user, t);
		}
		for (Split s : sqlSession.getMapper(Transactions.class).selectSplits(user)){
			s.setMemo(CryptoUtil.encrypt(s.getMemo(), encryptionKey));
			sqlSession.getMapper(Transactions.class).updateSplit(user, s);
		}
		for (ScheduledTransaction st : sqlSession.getMapper(ScheduledTransactions.class).selectScheduledTransactions(user)){
			st.setScheduleName(CryptoUtil.encrypt(st.getScheduleName(), encryptionKey));
			st.setDescription(CryptoUtil.encrypt(st.getDescription(), encryptionKey));
			st.setNumber(CryptoUtil.encrypt(st.getNumber(), encryptionKey));
			sqlSession.getMapper(ScheduledTransactions.class).updateScheduledTransaction(user, st);
			for (Split s : st.getSplits()) {
				s.setMemo(CryptoUtil.encrypt(s.getMemo(), encryptionKey));
				sqlSession.getMapper(ScheduledTransactions.class).updateScheduledSplit(user, s);
			}
		}
	}
	
	public static void turnOffEncryption(User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		//To turn off encryption, we must do the following:
		// a) Iterate through all sources and transactions in the system, and decrypt the appropriate fields (see above for encrypted fields).
		// b) Set the encryption key to null, and store it in the user table
		
		if (!user.isEncrypted()) throw new DatabaseException("This account is not encrypted");
		
		final String encryptionKey = user.getDecryptedEncryptionKey();
		user.setEncryptionKey(null);
		sqlSession.getMapper(Users.class).updateUserEncryptionKey(user);

		for (Account a : sqlSession.getMapper(Sources.class).selectAccounts(user)) {
			a.setName(CryptoUtil.decrypt(a.getName(), encryptionKey));
			a.setAccountType(CryptoUtil.decrypt(a.getAccountType(), encryptionKey));
			sqlSession.getMapper(Sources.class).updateAccount(user, a);
		}
		for (Category c : sqlSession.getMapper(Sources.class).selectCategories(user)) {
			c.setName(CryptoUtil.decrypt(c.getName(), encryptionKey));
			sqlSession.getMapper(Sources.class).updateCategory(user, c);
		}
		for (Transaction t : sqlSession.getMapper(Transactions.class).selectTransactions(user)){
			t.setDescription(CryptoUtil.decrypt(t.getDescription(), encryptionKey));
			t.setNumber(CryptoUtil.decrypt(t.getNumber(), encryptionKey));
			sqlSession.getMapper(Transactions.class).updateTransaction(user, t);
		}
		for (Split s : sqlSession.getMapper(Transactions.class).selectSplits(user)){
			if (s.getMemo() != null){
				s.setMemo(CryptoUtil.decrypt(s.getMemo(), encryptionKey));
				sqlSession.getMapper(Transactions.class).updateSplit(user, s);
			}
		}
	}
}
