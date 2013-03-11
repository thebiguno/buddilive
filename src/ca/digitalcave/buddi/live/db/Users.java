package ca.digitalcave.buddi.live.db;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.User;


public interface Users {
	public User selectUser(@Param("identifier") String identifier);
	
	public Integer insertUser(@Param("user") User user);
	
	public Integer updateUser(@Param("user") User user);
	
	public Integer deleteUser(@Param("user") User user);
}
