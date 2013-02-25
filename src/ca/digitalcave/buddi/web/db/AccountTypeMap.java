package ca.digitalcave.buddi.web.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.model.AccountType;


public interface AccountTypeMap {
	public List<AccountType> selectAccountTypes(@Param("email") String email);
}
