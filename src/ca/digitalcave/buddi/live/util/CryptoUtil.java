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

public class CryptoUtil {

	private static final String SALT_ALGORITHM = "SHA1PRNG";
	private static final String STRONG_KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
	private static final String STRONG_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

	public static byte[] encrypt(byte[] bytes, char[] password) throws Exception {
		// generate some random salt
		final byte[] salt = new byte[8];
		SecureRandom.getInstance(SALT_ALGORITHM).nextBytes(salt);

		final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(STRONG_KEY_ALGORITHM);
		final PBEKeySpec keySpec = new PBEKeySpec(password, salt, 65525, 256);
		final Key tmp = keyFactory.generateSecret(keySpec);
		final Key key = new SecretKeySpec(tmp.getEncoded(), "AES");

		final Cipher c = Cipher.getInstance(STRONG_CIPHER_ALGORITHM);
		c.init(Cipher.ENCRYPT_MODE, key);
		final AlgorithmParameters p = c.getParameters();

		final byte[] iv = p.getParameterSpec(IvParameterSpec.class).getIV();
		final byte[] out = c.doFinal(bytes);

		final byte[] result = new byte[out.length + iv.length + salt.length];
		System.arraycopy(salt, 0, result, 0, salt.length);
		System.arraycopy(iv, 0, result, salt.length, iv.length);
		System.arraycopy(out, 0, result, salt.length + iv.length, out.length);

		return result;
	}

	public static byte[] decrypt(byte[] bytes, char[] password) throws Exception {
		// recoven the salt
		final byte[] salt = new byte[8];
		System.arraycopy(bytes, 0, salt, 0, salt.length);

		// recover the iv
		final byte[] iv = new byte[16];
		System.arraycopy(bytes, salt.length, iv, 0, iv.length);

		// recover the cyphertext
		final byte[] in = new byte[bytes.length - iv.length - salt.length];
		System.arraycopy(bytes, salt.length + iv.length, in, 0, in.length);

		final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(STRONG_KEY_ALGORITHM);
		final PBEKeySpec keySpec = new PBEKeySpec(password, salt, 65525, 256);
		final Key tmp = keyFactory.generateSecret(keySpec);
		final Key key = new SecretKeySpec(tmp.getEncoded(), "AES");

		final Cipher c = Cipher.getInstance(STRONG_CIPHER_ALGORITHM);
		c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		return c.doFinal(in);
	}

	/**
	 * The resulting string is 40 characters for the hash + 5 for the algorithm + salt + iteration
	 */
	public static String getSha1Hash(int iterations, byte[] salt, String message) {
		return getHash("SHA-1", 0, salt, message);
	}

	/**
	 * The resulting string is 64 characters for the hash + 7 for the algorithm + salt + iteration
	 */
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
		final int base = 16;
		final StringBuilder buf = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			int bi = 0xff & bytes[i];
			int c = '0' + (bi/base) % base;
			if (c > '9') 
				c = 'a' + (c - '0' - 10);
			buf.append((char) c);
			c = '0' + bi % base;
			if (c > '9')
				c = 'a' + (c - '0' - 10);
			buf.append((char) c);
		}
		return buf.toString();
	}

	public static byte[] decode(String encoded) {
		final char[] hex = encoded.toCharArray();
		int length = hex.length / 2;
		byte[] raw = new byte[length];
		for (int i = 0; i < length; i++) {
			int high = Character.digit(hex[i * 2], 16);
			int low = Character.digit(hex[i * 2 + 1], 16);
			int value = (high << 4) | low;
			if (value > 127)
				value -= 256;
			raw[i] = (byte) value;
		}
		return raw;
	}

	public static byte[] getRandomSalt() {
		return getRandomSalt(2);
	}

	public static byte[] getRandomSalt(int bytes) {
		final byte[] salt = new byte[bytes];
		try {
			final SecureRandom r = SecureRandom.getInstance(SALT_ALGORITHM);
			r.nextBytes(salt);
			return salt;
		}
		catch (NoSuchAlgorithmException e){
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			final byte[] salt = getRandomSalt(2);
			final String test = "password";
	
			long start = System.currentTimeMillis();
			System.out.println(getHash("MD5", 0, salt, test));
			System.out.println(System.currentTimeMillis() - start);
	
			start = System.currentTimeMillis();
			System.out.println(getHash("SHA-1", 0, salt, test));
			System.out.println(System.currentTimeMillis() - start);
	
			start = System.currentTimeMillis();
			System.out.println(getHash("SHA-1", 255, salt, test));
			System.out.println(System.currentTimeMillis() - start);
	
			start = System.currentTimeMillis();
			System.out.println(getHash("SHA-1", 32767, salt, test));
			System.out.println(System.currentTimeMillis() - start);
	
			start = System.currentTimeMillis();
			System.out.println(getHash("SHA-1", 65535, salt, test));
			System.out.println(System.currentTimeMillis() - start);
	
			start = System.currentTimeMillis();
			System.out.println(getHash("SHA-1", 16777215, salt, test));
			System.out.println(System.currentTimeMillis() - start);
		}
	}
}
