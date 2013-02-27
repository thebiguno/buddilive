package ca.digitalcave.buddi.web.db;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.web.model.User;


public interface Users {
	/**
	 * Selects the user with the specified identifier
	 * @param identifier
	 * @return
	 */
	public User selectUser(@Param("identifier") String identifier);
	
	/**
	 * Inserts the user
	 * @param user
	 * @return
	 */
	public Integer insertUser(@Param("user") User user);
}
