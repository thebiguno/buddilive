package ca.digitalcave.buddi.live.db;

import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.moss.restlet.model.AuthUser;


public interface Users {
	public User selectUser(@Param("identifier") String identifier);
	public User selectUserByActivationKey(@Param("activationKey") String activationKey);
	public int selectEncryptionVersion(@Param("user") User user);
	
	public int insertUser(@Param("user") User user);
	public int insertActivationKey(@Param("user") User user, @Param("activationKey") String activationKey);
	public int insertTotpBackupCode(@Param("user") User user, @Param("backupCode") String backupCode);
	
	public int updateUserLoginTime(@Param("user") User user);
	public int updateUser(@Param("user") User user);
	public int updateUserEncryptionKey(@Param("user") User user);
	public int updateUserSecret(@Param("user") AuthUser user, @Param("hashedSecret") String hashedSecret);
	public int updateUserEncryptionVersion(@Param("user") User user, @Param("encryptionVersion") int encryptionVersion);
	public int updateUserPremium(@Param("user") User user, @Param("premium") String premium);
	public int updateUserTotpSecret(@Param("user") User user, @Param("totpSecret") String totpSecret);
	public int updateUserTotpBackupCodeUsed(@Param("user") User user, @Param("backupCode") String backupCode);
	
	public int deleteUser(@Param("user") User user);
	public int deleteActivationKey();
	public int deleteActivationKey(@Param("user") User user);
	public int deleteInactiveUsers();
	public int deleteUnusedBackupCodes(@Param("user") User user);
}
