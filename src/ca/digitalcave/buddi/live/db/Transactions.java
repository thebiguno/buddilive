package ca.digitalcave.buddi.live.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;

import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Source;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;


public interface Transactions {
	public int selectTransactionCount(@Param("user") User user, @Param("uuid") String uuid);
	
	public List<Transaction> selectTransactions(@Param("user") User user);
	public List<Transaction> selectTransactions(@Param("user") User user, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
	public List<Transaction> selectTransactions(@Param("user") User user, @Param("type") String type, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
	public List<Transaction> selectTransactions(@Param("user") User user, @Param("source") Source source, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
	public void selectTransactions(@Param("user") User user, @Param("source") Source source, ResultHandler handler);
	
	public List<Transaction> selectDescriptions(@Param("user") User user);
	
	public List<Split> selectSplits(@Param("user") User user);
	public List<Split> selectSplits(@Param("user") User user, @Param("account") Account account);
	public List<Split> selectSplits(@Param("user") User user, @Param("account") Account account, @Param("date") Date date);
	
	public Integer insertTransaction(@Param("user") User user, @Param("transaction") Transaction transaction);
	public Integer insertSplit(@Param("user") User user, @Param("split") Split split);
	
	public Integer updateTransaction(@Param("user") User user, @Param("transaction") Transaction transaction);
	public Integer updateSplit(@Param("user") User user, @Param("split") Split split);
	
	public Integer deleteAllTransactions(@Param("user") User user);
	public Integer deleteTransaction(@Param("user") User user, @Param("transaction") Transaction transaction);
	public Integer deleteSplits(@Param("user") User user, @Param("transaction") Transaction transaction);
}
