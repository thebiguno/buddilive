package ca.digitalcave.buddi.web.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.db.util.DatabaseException;
import ca.digitalcave.buddi.web.model.Account;
import ca.digitalcave.buddi.web.model.AccountType;
import ca.digitalcave.buddi.web.model.Category;
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.User;


public interface Sources {
	public List<Account> selectAccounts(@Param("user") User user, @Param("accountType") String accountType, @Param("showBalance") boolean showBalance);
	public List<Account> selectAccounts(@Param("user") User user, @Param("showBalance") boolean showBalance);
	
	public List<AccountType> selectAccountTypes(@Param("user") User user, @Param("showBalance") boolean showBalance);
	
	public List<Category> selectCategories(@Param("user") User user, @Param("income") Boolean income);
	
	public Source selectSource(@Param("user") User user, @Param("id") int id);
	public List<Source> selectSources(@Param("user") User user);
	
	public Integer insertAccount(@Param("user") User user, @Param("account") Account account) throws DatabaseException;
	
	public Integer updateAccount(@Param("user") User user, @Param("account") Account account) throws DatabaseException;
	public Integer updateSourceDeleted(@Param("user") User user, @Param("source") Source source) throws DatabaseException;
}
