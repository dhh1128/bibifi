package org.builditbreakit.seada.common.io;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class Crypto {
	private static final int ITERATIONS = 5;
	private static final int KEY_SIZE = 128;
	private static final byte[] SALT = { 0x43, (byte) 0x95, (byte) 0xB0, 0x3A,
			0x47, 0x01, (byte) 0x8C, (byte) 0xC2, (byte) 0xDF, 0x27,
			(byte) 0xF4, (byte) 0xE5, 0x6E, 0x40, 0x1D, 0x05, 0x76, 0x3F, 0x10,
			0x1D };

	public static final String PBKDF_NAME = "PBKDF2WithHmacSHA1";
	public static final String CIPHER_KEY_TYPE = "AES";
	public static final String CIPHER_NAME = "AES/CBC/PKCS5Padding";
	public static final String NATIVE_PRNG_NAME = "NativePRNGNonBlocking";
	public static final String BACKUP_PRNG_NAME = "SHA1PRNG";
	public static final String MAC_NAME = "HmacSHA1";
	public static final String MAC_KEY_TYPE = "HmacSHA1";
	public static final int MAC_SIZE = 20;
	public static final int SALT_SIZE = 20;
	public static final int IV_SIZE = 16;
	
	private static volatile SecureRandom rand;
	
	private Crypto() {
		super();
	}
	
	public static Key generateBaseKey(String password) {
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF_NAME);
			PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(),
					SALT, ITERATIONS, KEY_SIZE);
			return keyFactory.generateSecret(pbeKeySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Mac getMac(Key key) {
		try {
			Mac mac = Mac.getInstance(MAC_NAME);
			mac.init(generateMacKey(key));
			return mac;
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
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

	public static byte[] getBlankMacArray() {
		return new byte[MAC_SIZE];
	}
	
	private static Cipher getCipher(Key key, byte[] iv, int mode) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_NAME);
			IvParameterSpec ivParam = new IvParameterSpec(iv);
			cipher.init(mode, generateCipherKey(key), ivParam);
			return cipher;
		} catch (NoSuchPaddingException | NoSuchAlgorithmException
				| InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Key generateCipherKey(Key baseKey) {
		return new SecretKeySpec(baseKey.getEncoded(), CIPHER_KEY_TYPE);
	}

	private static Key generateMacKey(Key baseKey) {
		return new SecretKeySpec(baseKey.getEncoded(), MAC_KEY_TYPE);
	}

	private static SecureRandom getRandom() {
		SecureRandom result = rand;
	    if (result == null) {
	        synchronized(Crypto.class) {
	            result = rand;
	            if (result == null) {
	            	try {
						if (System.getProperty("os.name").startsWith("Windows")) {
							rand = result = SecureRandom
									.getInstance(BACKUP_PRNG_NAME);
						} else {
							rand = result = SecureRandom
									.getInstance(NATIVE_PRNG_NAME);
						}
					} catch (NoSuchAlgorithmException e) {
						throw new RuntimeException(e);
					}
	            }
	        }
	    }
	    return result;
	}
}
