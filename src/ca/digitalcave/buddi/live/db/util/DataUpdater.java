package ca.digitalcave.buddi.live.db.util;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;

public class DataUpdater {

	public static void updateBalances(User user, SqlSession sqlSession) throws DatabaseException {
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
	
	public static void turnOnEncryption(User user, String password, SqlSession sqlSession) throws DatabaseException, CryptoException {
		//To turn on encryption, we must do the following:
		// a) Generate a new random key for the user
		// b) Encrypt this key using their password
		// c) ASCII-armour the key, and store it in their user table
		// d) Iterate through all sources, transactions, and split in the system, and encrypt the appropriate fields.  Encryptable fields are:
		//   i) Source name
		//   ii) Transaction description
		//   iii) Transaction number
		//	 iiii) Split memo
		
		if (user.isEncrypted()) throw new DatabaseException("This account is already encrypted");
		
		final String encryptionKey = CryptoUtil.encode(CryptoUtil.getSecureRandom(128));
		user.setDecryptedEncryptionKey(encryptionKey);
		user.setEncryptionKey(CryptoUtil.encrypt(encryptionKey, password));
		
		sqlSession.getMapper(Users.class).updateUserEncryptionKey(user);
		
		for (Account a : sqlSession.getMapper(Sources.class).selectAccounts(user)) {
			a.setName(CryptoUtil.encryptWrapper(a.getName(), user));
			sqlSession.getMapper(Sources.class).updateAccount(user, a);
		}
		for (Category c : sqlSession.getMapper(Sources.class).selectCategories(user)) {
			c.setName(CryptoUtil.encryptWrapper(c.getName(), user));
			sqlSession.getMapper(Sources.class).updateCategory(user, c);
		}
		for (Transaction t : sqlSession.getMapper(Transactions.class).selectTransactions(user)){
			t.setDescription(CryptoUtil.encryptWrapper(t.getDescription(), user));
			t.setNumber(CryptoUtil.encryptWrapper(t.getDescription(), user));
			sqlSession.getMapper(Transactions.class).updateTransaction(user, t);
		}
		for (Split s : sqlSession.getMapper(Transactions.class).selectSplits(user)){
			s.setMemo(CryptoUtil.encryptWrapper(s.getMemo(), user));
			sqlSession.getMapper(Transactions.class).updateSplit(user, s);
		}
	}
	
	public static void turnOffEncryption(User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		//To turn off encryption, we must do the following:
		// a) Iterate through all sources and transactions in the system, and decrypt the appropriate fields.
		//   i) Source name
		//   ii) Transaction description
		//   iii) Transaction number
		// b) Set the encryption key to null, and store it in the user table
		
		if (!user.isEncrypted()) throw new DatabaseException("This account is not encrypted");

		for (Account a : sqlSession.getMapper(Sources.class).selectAccounts(user)) {
			a.setName(CryptoUtil.decryptWrapper(a.getName(), user));
			sqlSession.getMapper(Sources.class).updateAccount(user, a);
		}
		for (Category c : sqlSession.getMapper(Sources.class).selectCategories(user)) {
			c.setName(CryptoUtil.decryptWrapper(c.getName(), user));
			sqlSession.getMapper(Sources.class).updateCategory(user, c);
		}
		for (Transaction t : sqlSession.getMapper(Transactions.class).selectTransactions(user)){
			t.setDescription(CryptoUtil.decryptWrapper(t.getDescription(), user));
			t.setNumber(CryptoUtil.decryptWrapper(t.getNumber(), user));
			sqlSession.getMapper(Transactions.class).updateTransaction(user, t);
		}
		for (Split s : sqlSession.getMapper(Transactions.class).selectSplits(user)){
			if (s.getMemo() != null){
				s.setMemo(CryptoUtil.decryptWrapper(s.getMemo(), user));
				sqlSession.getMapper(Transactions.class).updateSplit(user, s);
			}
		}

		user.setEncryptionKey(null);
		user.setDecryptedEncryptionKey(null);
		sqlSession.getMapper(Users.class).updateUserEncryptionKey(user);
	}
}
