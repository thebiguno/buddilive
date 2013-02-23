package ca.digitalcave.buddi.web.util;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.restlet.engine.util.Base64;

public class CryptoUtil {

	private static final String ALGORITHM = "PBEWithMD5AndTripleDES";
	private static final char[] PASSWORD = "xMa&6-EG!VdLu#]Ne5ye9\"3De[nrmSl".toCharArray();
	private static final int ITERATION_COUNT = 2048;

	public static byte[] encrypt(byte[] bytes) throws Exception {
		final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
		final PBEKeySpec keySpec = new PBEKeySpec(PASSWORD);
		final Key key = keyFactory.generateSecret(keySpec);
		
		// generate some random salt
		final byte[] salt = new byte[8];
		SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
		final Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, ITERATION_COUNT));
		
		final byte[] out = c.doFinal(bytes);
		final byte[] result = new byte[out.length + salt.length];
		System.arraycopy(out, 0, result, salt.length, out.length);
		System.arraycopy(salt, 0, result, 0, salt.length);
		
		return result;
	}
	
	public static byte[] decrypt(byte[] bytes) throws Exception {
		final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
		final PBEKeySpec keySpec = new PBEKeySpec(PASSWORD);
		final Key key = keyFactory.generateSecret(keySpec);
		
		// recover the salt
		final byte[] salt = new byte[8];
		System.arraycopy(bytes, 0, salt, 0, salt.length);
		
		final byte[] in = new byte[bytes.length - salt.length];
		System.arraycopy(bytes, salt.length, in, 0, in.length);
		
		final Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(salt, ITERATION_COUNT));
		return c.doFinal(in);
	}
}
