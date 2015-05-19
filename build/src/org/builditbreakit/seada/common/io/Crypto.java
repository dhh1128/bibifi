package org.builditbreakit.seada.common.io;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public final class Crypto {
	private static final int KEY_SIZE = 16;
	
	public static final String NATIVE_PRNG_NAME = "NativePRNGNonBlocking";
	public static final String BACKUP_PRNG_NAME = "SHA1PRNG";
	public static final String MAC_NAME = "HmacSHA1";
	public static final int IV_SIZE = 8;
	
	private static SecureRandom rand;
	private static BufferedBlockCipher cipher;
	private static Digest digest;
	private static Mac mac;
	
	private Crypto() {
		super();
	}
	
	public static byte[] generateKey(String password) {
		try {
			Digest digest = getDigest();
			if (digest.getDigestSize() < KEY_SIZE) {
				throw new InvalidKeyException("Cannot create key of size "
						+ KEY_SIZE + " with " + digest.getAlgorithmName());
			}
			byte[] hash = new byte[digest.getDigestSize()];
			byte[] passwordBytes = password.getBytes();
			digest.update(passwordBytes, 0, passwordBytes.length);
			digest.doFinal(hash, 0);
			return Arrays.copyOf(hash, KEY_SIZE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] generateIV() {
		byte[] result = new byte[IV_SIZE];
		getRandom().nextBytes(result);
		return result;
	}

	public static Mac getMac(byte[] key) {
		Mac result = getMac();
		result.init(getMacParams(key));
		return result;
	}

	public static BufferedBlockCipher getEncryptingCipher(byte[] key, byte[] iv) {
		return initCipher(getCipher(), true, key, iv);
	}

	public static BufferedBlockCipher getDecryptingCipher(byte[] key, byte[] iv) {
		return initCipher(getCipher(), false, key, iv);
	}

	private static BufferedBlockCipher initCipher(BufferedBlockCipher cipher,
			boolean forEncryption, byte[] key, byte[] iv) {
		cipher.init(forEncryption, getCipherParams(key, iv));
		return cipher;
	}
	
	private static Digest getDigest() {
		if(digest == null) {
			digest = new SHA1Digest();
		}
		return digest;
	}

	private static Mac getMac() {
		if(mac == null) {
			Digest digest = new SHA1Digest();
			mac = new HMac(digest);
		}
		return mac;
	}
	
	private static CipherParameters getCipherParams(byte[] key, byte[] iv) {
		KeyParameter keyParam = new KeyParameter(key);
		return new ParametersWithIV(keyParam, iv);
	}
	
	private static CipherParameters getMacParams(byte[] key) {
		return new KeyParameter(key);
	}
	
	private static BufferedBlockCipher getCipher() {
		if (cipher == null) {
			BlockCipher engine = new BlowfishEngine();
			CBCBlockCipher mode = new CBCBlockCipher(engine);
			BlockCipherPadding padding = new PKCS7Padding();
			cipher = new PaddedBufferedBlockCipher(mode, padding);
		}
		return cipher;
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
