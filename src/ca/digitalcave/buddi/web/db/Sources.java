package ca.digitalcave.buddi.web.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.db.util.DatabaseException;
import ca.digitalcave.buddi.web.model.Account;
import ca.digitalcave.buddi.web.model.AccountType;
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.User;


public interface Sources {
	public List<Account> selectAccountsByAccountType(@Param("user") User user, @Param("accountType") String accountType);
	public List<AccountType> selectAccountsWithBalancesByType(@Param("user") User user);
	
	public Source selectSource(@Param("user") User user, @Param("id") int id);
	public Source selectSource(@Param("user") User user, @Param("uuid") String uuid);
	public List<Source> selectSources(@Param("user") User user);
	
	public Integer insertAccount(@Param("user") User user, @Param("account") Account account) throws DatabaseException;
	
	public Integer updateAccount(@Param("user") User user, @Param("account") Account account) throws DatabaseException;
	public Integer updateSourceDeleted(@Param("user") User user, @Param("source") Source source) throws DatabaseException;
}
