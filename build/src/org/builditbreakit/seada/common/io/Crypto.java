package org.builditbreakit.seada.common.io;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class Crypto {
	public static final int IV_SIZE = 8;
	
	private static final int KEY_SIZE = 16;
	private static final String KEY_HASH_NAME = "SHA-1";
	private static final String CIPHER_KEY_TYPE = "Blowfish";
	
	private static final String NATIVE_PRNG_NAME = "NativePRNGNonBlocking";
	private static final String BACKUP_PRNG_NAME = "SHA1PRNG";
	
	private static final String CIPHER_NAME = "Blowfish/CBC/PKCS5Padding";
	
	private static SecureRandom rand;
	
	private Crypto() {
		super();
	}
	
	public static Key generateBaseKey(String password) {
		try {
			MessageDigest hash = MessageDigest.getInstance(KEY_HASH_NAME);
			byte[] hashedPassword = hash.digest(password.getBytes());
			return new SecretKeySpec(hashedPassword, 0, KEY_SIZE,
					CIPHER_KEY_TYPE);
		} catch (NoSuchAlgorithmException e) {
			// Should not happen with AES/CBC/PKCS5Padding or SHA-1
			throw new RuntimeException(e);
		}
	}
	
	public static Cipher getEncryptingCipher(Key key, byte[] iv) {
		return getCipher(key, iv, Cipher.ENCRYPT_MODE);
	}

	public static Cipher getDecryptingCipher(Key key, byte[] iv) {
		return getCipher(key, iv, Cipher.DECRYPT_MODE);
	}

	public static byte[] generateIV() {
		byte[] result = new byte[IV_SIZE];
		getRandom().nextBytes(result);
		return result;
	}
	
	private static Cipher getCipher(Key key, byte[] iv, int mode) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_NAME);
			IvParameterSpec ivParam = new IvParameterSpec(iv);
			cipher.init(mode, key, ivParam);
			return cipher;
		} catch (NoSuchPaddingException | NoSuchAlgorithmException
				| InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException(e);
		}
	}

	/** Not Thread Safe. Single-threaded apps only */
	private static SecureRandom getRandom() {
		if (rand == null) {
			try {
				if (System.getProperty("os.name").startsWith("Windows")) {
					rand = SecureRandom.getInstance(BACKUP_PRNG_NAME);
				} else {
					rand = SecureRandom.getInstance(NATIVE_PRNG_NAME);
				}
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}
		return rand;
	}
}
