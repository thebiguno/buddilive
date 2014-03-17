package ca.digitalcave.buddi.live.db;

import org.apache.ibatis.annotations.Param;


public interface BuddiSystem {
	public String selectCookieEncryptionKey();
	public int insertCookieEncryptionKey(@Param("encryptionKey") String encryptionKey);
	public int updateCookieEncryptionKey(@Param("encryptionKey") String encryptionKey);
	public int deleteCookieEncryptionKey();
}
