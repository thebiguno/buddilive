package ca.digitalcave.buddi.live.db;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.User;


public interface Users {
	public User selectUser(@Param("identifier") String identifier);
	
	public Integer insertUser(@Param("user") User user, @Param("activationKey") String activationKey);
	
	public Integer updateUserLoginTime(@Param("user") User user);
	public Integer updateUser(@Param("user") User user);
	public Integer updateUserEncryptionKey(@Param("user") User user);
	public Integer updateUserActivationKey(@Param("hashedIdentifier") String hashedIdentifier, @Param("activationKey") String activationKey);
	public Integer updateUserSecret(@Param("activationKey") String activationKey, @Param("hashedSecret") String hashedSecret);
	
	public Integer deleteUser(@Param("user") User user);
}
