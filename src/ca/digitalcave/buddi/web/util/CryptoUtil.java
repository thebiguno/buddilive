package ca.digitalcave.buddi.web.util;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class CryptoUtil {

	private static final String ALGORITHM = "PBEWithMD5AndTripleDES";
	private static final char[] PASSWORD = "xMa&6-EG!VdLu#]Ne5ye9\"3De[nrmSl".toCharArray();
	private static final int ITERATION_COUNT = 2048;
	private static final int SALT_LENGTH = 4;

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
	
	public static String getSha1Hash(String salt, String secret) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA1");
			digest.update(salt.getBytes());
			final byte[] hash = digest.digest(secret.getBytes());
			final StringBuilder sb = new StringBuilder();
			sb.append("SHA1:");
			sb.append(salt);
			sb.append(bytesToString(hash));
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	private static String bytesToString(byte[] bytes) {
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
				c = 'a' +( c - '0' - 10);
			buf.append((char) c);
		}
		return buf.toString();
	}
	public static String getRandomSalt() {
		final byte[] salt = new byte[SALT_LENGTH];
		final Random r = new Random();
		r.nextBytes(salt);
		return bytesToString(salt);
	}
	public static String extractSalt(String hash) {
		return hash.substring(5, 5 + (SALT_LENGTH * 2));	//Here we assume an ASCII representation of an N byte hash, i.e. the one generated above.
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 3; i++){
			String salt = getRandomSalt();
			System.out.println("Salt: " + salt);
			String hash = getSha1Hash(salt, "admin@digitalcave.ca");
			System.out.println("Hash: " + hash);
			System.out.println("ExtS: " + extractSalt(hash));
		}
	}
}
