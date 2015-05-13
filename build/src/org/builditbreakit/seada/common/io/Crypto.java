package org.builditbreakit.seada.common.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class Crypto {
	public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	public static final String HASH_ALGORITHM = "SHA-1";
	public static final String MAC_ALGORITHM = "HmacSHA1";
	public static final String KEY_ALGORITHM = "AES";
	public static final int KEY_BYTES = 16;
	public static final int IV_BYTES = 16;
	public static final int MAC_BYTES = 20;
	public static final String STRING_ENCODING = "ASCII";
	
	private Crypto() {
		// Utility class
	}

	public static Key genKey(String password) {
		try {
			MessageDigest hash = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] hashedPassword = hash.digest(password
					.getBytes(STRING_ENCODING));
			return new SecretKeySpec(Arrays.copyOf(hashedPassword, KEY_BYTES),
					KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			// Should not happen with AES/CBC/PKCS5Padding or SHA-1
			throw new SecurityException(e);
		}
	}

	public static byte[] encrypt(Key key, byte[] bytes) {
		try {
			SecureRandom rand = SecureRandom.getInstanceStrong();
			IvParameterSpec iv = new IvParameterSpec(
					rand.generateSeed(IV_BYTES));

			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);

			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			byteStream.write(iv.getIV());
			byteStream.write(cipher.doFinal(bytes));
			
			return byteStream.toByteArray();
		} catch (IllegalBlockSizeException | BadPaddingException
				| InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException
				| IOException e) {
			throw new SecurityException(e);
		}
	}

	public static byte[] decrypt(Key key, byte[] ciphertext) {
		try {
			IvParameterSpec iv = new IvParameterSpec(ciphertext, 0, IV_BYTES);

			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key, iv);

			return cipher.doFinal(ciphertext, IV_BYTES, ciphertext.length
					- IV_BYTES);
		} catch (IllegalBlockSizeException | BadPaddingException
				| InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			throw new SecurityException(e);
		}
	}

	public static byte[] mac(Key key, byte[] message) {
		try {
			Mac mac = Mac.getInstance(MAC_ALGORITHM);
			mac.init(key);
			return mac.doFinal(message);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new SecurityException(e);
		}
	}

	public static boolean authenticate(Key key, byte[] mac, byte[] ciphertext) {
		byte[] currentMac = mac(key, ciphertext);
		return Arrays.equals(mac, currentMac);
	}
}
