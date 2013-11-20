package ca.digitalcave.buddi.live.util;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.restlet.engine.util.Base64;

import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.moss.crypto.MossHash;

public class CryptoUtil {
	
	private static final String DEFAULT_RNG_ALGORITHM = "SHA1PRNG";
	private static final String DEFAULT_KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final int DEFAULT_SALT_LENGTH = 16;
	private static final int DEFAULT_ITERATIONS = 1;
	private static final int KEY_LENGTH = 256;

	public static String decryptWrapper(String value, User user) throws CryptoException {
		if (StringUtils.isNotBlank(value) && user.isEncrypted()){
			return decrypt(value, user.getDecryptedEncryptionKey());
		}
		return value;
	}
	
	public static String encrypt(String value, String password) throws CryptoException {
		if (value == null) return null;
		final int saltLength = DEFAULT_SALT_LENGTH;
		final int iterations = DEFAULT_ITERATIONS;
		
		try {
			final byte[] salt = new byte[saltLength];
			SecureRandom.getInstance(DEFAULT_RNG_ALGORITHM).nextBytes(salt);

			final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DEFAULT_KEY_ALGORITHM);
			final PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
			final Key tmp = keyFactory.generateSecret(keySpec);
			final Key key = new SecretKeySpec(tmp.getEncoded(), "AES");

			final Cipher c = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, key);
			final AlgorithmParameters p = c.getParameters();

			final byte[] iv = p.getParameterSpec(IvParameterSpec.class).getIV();
			final byte[] out = c.doFinal(value.getBytes("UTF-8"));

			final StringBuilder sb = new StringBuilder();
			sb.append(iterations);
			sb.append(":");
			sb.append(encode(salt));
			sb.append(":");
			sb.append(encode(iv));
			sb.append(":");
			sb.append(encode(out));
			
			return sb.toString();
		}
		catch (Exception e){
			throw new CryptoException(e);
		}
	}

	//Checks whether the given value looks as if it could be encrypted.  We check for separators, encoding, etc. 
	public static boolean isEncryptedValue(String value){
		if (value == null) return false;
		String[] split = value.split(":");
		if (split.length != 4) {
			return false;
		}
		
		try {
			Integer.parseInt(split[0]);
			decode(split[1]);
			if (decode(split[2]).length <= 4) return false;		//Nothing is really special about 4 here... it is just a small value which is smaller than any valid value I have seen.  We could probably calculate what the actual value is, but this is fine for now.
			if (decode(split[3]).length <= 4) return false;
		}
		catch (Throwable e){
			return false;
		}
		
		return true;
	}
	
	public static String decrypt(String value, String password) throws CryptoException {
		if (value == null) return null;
		String[] split = value.split(":");
		if (split.length != 4) {
			throw new CryptoException("Invalid cyphertext");
		}
		
		// recover the salt
		final int iterations = Integer.parseInt(split[0]);

		// recover the salt
		final byte[] salt = decode(split[1]);

		// recover the iv
		final byte[] iv = decode(split[2]);

		// recover the cyphertext
		final byte[] in = decode(split[3]);

		try {
			final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DEFAULT_KEY_ALGORITHM);
			final PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
			final Key tmp = keyFactory.generateSecret(keySpec);
			final Key key = new SecretKeySpec(tmp.getEncoded(), "AES");

			final Cipher c = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
			return new String(c.doFinal(in), "UTF-8");
		}
		catch (Exception e){
			throw new CryptoException(e);
		}
	}

	public static String getSha256Hash(String message) {
		return getSha256Hash(DEFAULT_ITERATIONS, getSecureRandom(), message);
	}

	public static String getSha256Hash(int iterations, byte[] salt, String message) {
		return getHash("SHA-256", iterations, salt, message);
	}

	/**
	 * Returns a string in the format "algorithm:iterations:salt:hash".
	 */
	public static String getHash(String algorithm, int iterations, byte[] salt, String message) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			final byte[] messageBytes = message.getBytes();
			
			digest.update(messageBytes);
			digest.update(salt);

			for (int i = 0; i < iterations; i++) {
				digest.update(digest.digest());
				digest.update(messageBytes);
				digest.update(salt);
			}

			final StringBuilder sb = new StringBuilder();
			sb.append(algorithm);
			sb.append(":");
			sb.append(Integer.toString(iterations, 16));
			sb.append(":");
			sb.append(encode(salt));
			sb.append(":");
			sb.append(encode(digest.digest()));
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean verify(String hash, String message) {
		final int a = hash.indexOf(':');
		final int b = hash.indexOf(':', a + 1);
		final int c = hash.indexOf(':', b + 1);
		final String algorithm = hash.substring(0, a);
		final int iterations = Integer.parseInt(hash.substring(a + 1, b), 16);
		final byte[] salt = decode(hash.substring(b + 1, c));
		final String calc = getHash(algorithm, iterations, salt, message);
		return hash.equalsIgnoreCase(calc);
	}

	public static String encode(byte[] bytes) {
		return Base64.encode(bytes, false);
	}

	public static byte[] decode(String encoded) {
		return Base64.decode(encoded);
	}

	public static byte[] getSecureRandom() {
		return getSecureRandom(DEFAULT_SALT_LENGTH);
	}

	public static byte[] getSecureRandom(int bytes) {
		final byte[] salt = new byte[bytes];
		try {
			final SecureRandom r = SecureRandom.getInstance(DEFAULT_RNG_ALGORITHM);
			r.nextBytes(salt);
			return salt;
		}
		catch (NoSuchAlgorithmException e){
			throw new RuntimeException(e);
		}
	}
	
	public static class CryptoException extends Exception {
		private static final long serialVersionUID = 1L;
		public CryptoException() {}
		public CryptoException(String message){
			super(message);
		}
		public CryptoException(Throwable throwable){
			super(throwable);
		}
		public CryptoException(String message, Throwable throwable){
			super(message, throwable);
		}
	}
}
