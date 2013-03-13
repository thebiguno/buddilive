package ca.digitalcave.buddi.live.db.util;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;

public class MassUpdater {

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
}
