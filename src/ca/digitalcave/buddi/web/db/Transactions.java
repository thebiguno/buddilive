package ca.digitalcave.buddi.web.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.Split;
import ca.digitalcave.buddi.web.model.Transaction;
import ca.digitalcave.buddi.web.model.User;


public interface Transactions {
	public Transaction selectTransaction(@Param("user") User user, @Param("id") Long id);
	public Transaction selectTransaction(@Param("user") User user, @Param("id") String uuid);
	
	public List<Transaction> selectTransactions(@Param("user") User user);
	public List<Transaction> selectTransactions(@Param("user") User user, @Param("source") Source source);
	
	public Integer insertTransaction(@Param("user") User user, @Param("transaction") Transaction transaction);
	public Integer insertSplit(@Param("user") User user, @Param("split") Split split);
	
	public Integer deleteSplits(@Param("user") User user, @Param("transaction") Transaction transaction);
}
