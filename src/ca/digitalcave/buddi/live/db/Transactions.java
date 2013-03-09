package ca.digitalcave.buddi.live.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.Source;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;


public interface Transactions {
	public Transaction selectTransaction(@Param("user") User user, @Param("id") Long id);
//	public Transaction selectTransaction(@Param("user") User user, @Param("uuid") String uuid);
	
	public List<Transaction> selectTransactions(@Param("user") User user);
	public List<Transaction> selectTransactions(@Param("user") User user, @Param("source") Source source);
	
	public List<Transaction> selectDescriptions(@Param("user") User user);
	
	public Integer insertTransaction(@Param("user") User user, @Param("transaction") Transaction transaction);
	public Integer insertSplit(@Param("user") User user, @Param("split") Split split);
	
	public Integer updateTransaction(@Param("user") User user, @Param("transaction") Transaction transaction);
	
	public Integer deleteTransaction(@Param("user") User user, @Param("transaction") Transaction transaction);
	public Integer deleteSplits(@Param("user") User user, @Param("transaction") Transaction transaction);
}
