package ca.digitalcave.buddi.web.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.model.Transaction;
import ca.digitalcave.buddi.web.model.User;


public interface Transactions {
	public Transaction selectTransaction(@Param("user") User user, @Param("id") Long id);
	public Transaction selectTransaction(@Param("user") User user, @Param("id") String uuid);
	public List<Transaction> selectTransactions(@Param("user") User user);
	public Integer insertTransaction(@Param("user") User user, @Param("transaction") Transaction transaction);
}
