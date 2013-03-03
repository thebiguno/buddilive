package ca.digitalcave.buddi.web.db;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.model.User;


public interface Users {
	public User selectUser(@Param("identifier") String identifier);
	
	public Integer insertUser(@Param("user") User user);
}
