package ca.digitalcave.buddi.live.util;

import org.apache.commons.lang.StringUtils;

import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.moss.crypto.Base64;
import ca.digitalcave.moss.crypto.Crypto;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class CryptoUtil {
	
	public static String decryptWrapper(String value, User user) throws CryptoException {
		if (StringUtils.isNotBlank(value) && user.isEncrypted()){
			try {
				return Crypto.decrypt(user.getDecryptedEncryptionKey(), value);
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
	
	public static boolean isEncryptedValue(String value){
		if (StringUtils.isBlank(value)) return false;
		final String[] split = value.split(":");
		//Depending on the encryption scheme used, the length will be one of 3, 4, or 5.
		if (split.length != 3 && split.length != 4 && split.length != 5) {
			return false;
		}
		
		try {
			//Nothing is really special about 4 here... it is just a small value which is smaller than any valid value I have seen.  We could probably calculate what the actual value is, but this is fine for now.
			//Try decoding the last segment; this is always going to be the message.
			if (Base64.decode(split[split.length - 1]).length <= 4) return false;	
		}
		catch (Throwable e){
			return false;
		}
		
		return true;
	}
}
