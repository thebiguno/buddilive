package ca.digitalcave.buddi.web.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.db.util.DatabaseException;
import ca.digitalcave.buddi.web.model.AccountType;
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.User;


public interface Sources {
	public List<Source> selectAccounts(@Param("user") User user);
	public List<Source> selectAccountsByAccountType(@Param("user") User user, @Param("accountType") String accountType);
	public List<AccountType> selectAccountsWithBalancesByType(@Param("user") User user);
	
	public Source selectSource(@Param("user") User user, @Param("id") int id);
	public Source selectSource(@Param("user") User user, @Param("uuid") String uuid);
	public List<Source> selectSources(@Param("user") User user);
	
	public Integer insertSource(@Param("user") User user, @Param("source") Source source) throws DatabaseException;
	
	public Integer updateDeleted(@Param("user") User user, @Param("source") Source source) throws DatabaseException;
	
	public Integer updateSource(@Param("user") User user, @Param("source") Source source) throws DatabaseException;
}
