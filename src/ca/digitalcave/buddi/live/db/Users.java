package ca.digitalcave.buddi.live.db;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.User;


public interface Users {
	public User selectUser(@Param("identifier") String identifier);
	public int selectEncryptionVersion(@Param("user") User user);
	
	public int insertUser(@Param("user") User user, @Param("activationKey") String activationKey);
	
	public int updateUserLoginTime(@Param("user") User user);
	public int updateUser(@Param("user") User user);
	public int updateUserEncryptionKey(@Param("user") User user);
	public int updateUserActivationKey(@Param("hashedIdentifier") String hashedIdentifier, @Param("activationKey") String activationKey);
	public int updateUserSecret(@Param("activationKey") String activationKey, @Param("hashedSecret") String hashedSecret);
	public int updateUserEncryptionVersion(@Param("user") User user, @Param("encryptionVersion") int encryptionVersion);
	
	public Integer deleteUser(@Param("user") User user);
}
