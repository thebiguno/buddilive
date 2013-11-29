package ca.digitalcave.buddi.live.util;

import org.apache.commons.lang.StringUtils;

import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.moss.crypto.Crypto;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class CryptoUtil {
	
	public static String decryptWrapper(String value, User user) throws CryptoException {
		if (StringUtils.isNotBlank(value) && user.isEncrypted()){
			try {
				return Crypto.decrypt(user.getDecryptedSecretKey(), value);
			}
			catch (CryptoException e){
				//I saw some weirdness where an encrypted value would not decrypt properly; most likely from
				// an old bug.  Regardless, we now return the unencrypted value if there was a problem
				// returning the decrypted value.  This value may be an encrypted string, or it may
				// just be the plaintext value; regardless, by returning it instead of just failing, we
				// give the user the ability to correct the data and recover (somewhat) gracefully.
				return value;
			}
		}
		return value;
	}
}