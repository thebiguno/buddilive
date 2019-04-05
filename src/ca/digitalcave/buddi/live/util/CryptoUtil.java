package ca.digitalcave.buddi.live.util;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.moss.crypto.Crypto;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class CryptoUtil {
	
	public static BigDecimal decryptWrapperBigDecimal(String value, User user, boolean nullIsZero) throws CryptoException {
		value = decryptWrapper(value, user);

		if (StringUtils.isBlank(value)){
			if (nullIsZero) return BigDecimal.ZERO;
			else return null;
		}
		else {
			return new BigDecimal(value);
		}
	}
	
	public static String decryptWrapper(String value, User user) throws CryptoException {
		if (StringUtils.isNotBlank(value) && user.isEncrypted()){
			try {
				return Crypto.decrypt(user.getDecryptedSecretKey(), value);
			}
			catch (CryptoException e){
				//Especially when upgrading from previous encryption versions, there are some times
				// where we are expecting an encrypted value but it is actually not encrypted.  This
				// should not happen in normal operation.
				//An alternative to this would be to check each value before decrypting to see if it
				// is in the proper 'encryption-looking' format.  However, since this should be the
				// exception rather than the rule, and since we would have to check every single value,
				// it is easier and more efficient to just catch the exception from incorrect decrypting.
				return value;
			}
		}
		return value;
	}
}