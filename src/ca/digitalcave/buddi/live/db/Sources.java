package ca.digitalcave.buddi.live.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.AccountType;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.CategoryPeriod;
import ca.digitalcave.buddi.live.model.Source;
import ca.digitalcave.buddi.live.model.User;


public interface Sources {
	public List<Account> selectAccounts(@Param("user") User user, @Param("accountType") String accountType, @Param("showBalance") boolean showBalance);
	public List<Account> selectAccounts(@Param("user") User user, @Param("showBalance") boolean showBalance);
	
	public List<AccountType> selectAccountTypes(@Param("user") User user, @Param("showBalance") boolean showBalance);
	
	public Category selectCategory(@Param("user") User user, @Param("id") Integer id);
	
	public List<Category> selectCategories(@Param("user") User user);
	public List<Category> selectCategories(@Param("user") User user, @Param("income") Boolean income);
	public List<Category> selectCategories(@Param("user") User user, @Param("periodType") String periodType);
	public List<Category> selectCategories(@Param("user") User user, @Param("categoryPeriod") CategoryPeriod categoryPeriod);
	
	public List<String> selectCategoryPeriods(@Param("user") User user);
	
	public Source selectSource(@Param("user") User user, @Param("id") int id);
	public List<Source> selectSources(@Param("user") User user);
	
	public Integer insertAccount(@Param("user") User user, @Param("account") Account account);
	public Integer insertCategory(@Param("user") User user, @Param("category") Category category);
	
	public Integer updateAccount(@Param("user") User user, @Param("account") Account account);
	public Integer updateCategory(@Param("user") User user, @Param("category") Category category);
	public Integer updateSourceDeleted(@Param("user") User user, @Param("source") Source source);
}