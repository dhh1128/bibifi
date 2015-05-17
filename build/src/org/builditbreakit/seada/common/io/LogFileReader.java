package org.builditbreakit.seada.common.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.Key;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

public class LogFileReader {
	private File file;

	public LogFileReader(File file) {
		this.file = file;
	}

	public GalleryState read(String password) throws IOException {
		Key key = Crypto.generateBaseKey(password);
		return decryptGalleryState(key);
	}

	private GalleryState decryptGalleryState(Key key) throws IOException,
			FileNotFoundException {
		// Open the file
		try (InputStream ciphertextIn = new BufferedInputStream(
				new FileInputStream(file))) {
			// Read the initialization vector
			byte[] iv = new byte[Crypto.IV_SIZE];
			if(ciphertextIn.read(iv) != iv.length) {
				throw new IntegrityViolationException("Unexpected EOF");
			}

			// Configure crypto
			Cipher decryptor = Crypto.getDecryptingCipher(key, iv);

			// Chain input streams. Final result is:
			// Disk -> Buffer -> Decryption -> Decompression -> Deserialization
			try (ObjectInputStream objectIn = new ObjectInputStream(
					new InflaterInputStream(new CipherInputStream(ciphertextIn,
							decryptor)))) {
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
