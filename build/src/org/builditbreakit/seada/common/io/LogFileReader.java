package org.builditbreakit.seada.common.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.Mac;
import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

public class LogFileReader {
	private File file;
	private int macSize;

	public LogFileReader(File file) {
		this.file = file;
	}

	public GalleryState read(String password) throws IOException {
		byte[] key = Crypto.generateKey(password);
		assertAuthenticLogFile(key);
		return decryptGalleryState(key);
	}

	private void assertAuthenticLogFile(byte[] key) throws IOException,
			FileNotFoundException {
		// Configure crypto
		Mac mac = Crypto.getMac(key);

		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			// Read in the file's claimed mac
			macSize = mac.getMacSize();
			byte[] expectedMac = new byte[macSize];
			if (in.read(expectedMac) != expectedMac.length) {
				throw new IntegrityViolationException("Unexpected EOF");
			}

			// Mac the remaining bytes in the file
			int readBytes = 0;
			byte[] buf = new byte[512];
			while ((readBytes = in.read(buf)) != -1) {
				mac.update(buf, 0, readBytes);
			}
			byte[] actualMac = new byte[macSize];
			mac.doFinal(actualMac, 0);

			// Check if the mac is authentic
			if (!Arrays.equals(expectedMac, actualMac)) {
				throw new IntegrityViolationException("Unable to authenticate");
			}
		}
	}

	private GalleryState decryptGalleryState(byte[] key) throws IOException,
			FileNotFoundException {
		// Open the file
		try (InputStream ciphertextIn = new BufferedInputStream(
				new FileInputStream(file))) {
			// Skip the mac
			ciphertextIn.skip(macSize);

			// Read the initialization vector
			byte[] iv = new byte[Crypto.IV_SIZE];
			if(ciphertextIn.read(iv) != iv.length) {
				throw new IntegrityViolationException("Unexpected EOF");
			}

			// Configure crypto
			BufferedBlockCipher decryptor = Crypto.getDecryptingCipher(key, iv);

			// Chain input streams. Final result is:
			// Disk -> Buffer -> Decryption -> Decompression -> Deserialization
			try (ObjectInputStream objectIn = new ObjectInputStream(
					new InflaterInputStream(
							new org.bouncycastle.crypto.io.CipherInputStream(
									ciphertextIn, decryptor)))) {
				// Read the data and return the gallery state
				return (GalleryState) objectIn.readObject();
			} catch (FileNotFoundException e) {
				throw e;
			} catch (ClassNotFoundException | IOException e) {
				throw new IntegrityViolationException(e);
			}
		}
	}
}
