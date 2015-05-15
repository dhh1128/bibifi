package org.builditbreakit.seada.common.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.Mac;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

import com.esotericsoftware.kryo.io.Input;

public class LogFileReader {
	private File file;

	public LogFileReader(File file) {
		this.file = file;
	}

	public GalleryState read(String password) throws IOException {
		Key key = Crypto.generateBaseKey(password);
		assertAuthenticLogFile(key);
		return decryptGalleryState(key);
	}

	private void assertAuthenticLogFile(Key key) throws IOException,
			FileNotFoundException {
		// Configure crypto
		Mac mac = Crypto.getMac(key);

		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			// Read in the file's claimed mac
			byte[] expectedMac = new byte[Crypto.MAC_SIZE];
			if (in.read(expectedMac) != expectedMac.length) {
				throw new IntegrityViolationException("Unexpected EOF");
			}

			// Mac the remaining bytes in the file
			int readBytes = 0;
			byte[] buf = new byte[512];
			while ((readBytes = in.read(buf)) != -1) {
				mac.update(buf, 0, readBytes);
			}
			byte[] actualMac = mac.doFinal();

			// Check if the mac is authentic
			if (!Arrays.equals(expectedMac, actualMac)) {
				throw new IntegrityViolationException("Unable to authenticate");
			}
		}
	}

	private GalleryState decryptGalleryState(Key key) throws IOException,
			FileNotFoundException {
		// Open the file
		try (InputStream ciphertextIn = new BufferedInputStream(
				new FileInputStream(file))) {
			// Skip the mac
			ciphertextIn.skip(Crypto.MAC_SIZE);

			// Read the initialization vector
			byte[] iv = new byte[Crypto.IV_SIZE];
			ciphertextIn.read(iv);

			// Configure crypto
			Cipher decryptor = Crypto.getDecryptingCipher(key, iv);

			// Chain input streams. Final result is:
			// Disk -> Buffer -> Decryption -> Decompression -> Deserialization
			try (Input objectIn = new Input(new InflaterInputStream(
					new CipherInputStream(ciphertextIn, decryptor)))) {
				// Read the data and return the gallery state
				return KryoInstance.getInstance().readObject(objectIn,
						GalleryState.class);
			}
		}
	}
}
