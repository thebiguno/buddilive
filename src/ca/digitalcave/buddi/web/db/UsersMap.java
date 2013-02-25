package ca.digitalcave.buddi.web.db;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.model.User;


public interface UsersMap {
	public User selectUser(@Param("email") String email);
}
