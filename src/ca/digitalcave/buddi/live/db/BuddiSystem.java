package ca.digitalcave.buddi.live.db;

import org.apache.ibatis.annotations.Param;


public interface BuddiSystem {
	public String selectCookieEncryptionKey();
	public int updateCookieEncryptionKey(@Param("encryptionKey") String encryptionKey);
}
