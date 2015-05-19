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
import org.bouncycastle.crypto.io.CipherInputStream;
import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

public class LogFileReader {
	private File file;

	public LogFileReader(File file) {
		this.file = file;
	}

	public GalleryState read(String password) throws IOException {
		byte[] key = Crypto.generateKey(password);
		return decryptGalleryState(key);
	}

	private GalleryState decryptGalleryState(byte[] key) throws IOException,
			FileNotFoundException {
		// Configure MAC
		Mac mac = Crypto.getMac(key);
		final int macSize = mac.getMacSize();
		
		// Open the file
		try (InputStream ciphertextIn = new BufferedInputStream(
				new FileInputStream(file));
				InputStream macStream = new MacBuildingInputStream(
						ciphertextIn, mac)) {
			// Read the MAC
			byte[] expectedMac = new byte[macSize];
			if (ciphertextIn.read(expectedMac) != expectedMac.length) {
				throw new IntegrityViolationException("Unexpected EOF");
			}

			// Read the initialization vector, and MAC it
			byte[] iv = new byte[Crypto.IV_SIZE];
			if(macStream.read(iv) != iv.length) {
				throw new IntegrityViolationException("Unexpected EOF");
			}

			// Configure decryption
			BufferedBlockCipher decryptor = Crypto.getDecryptingCipher(key, iv);

			// Chain input streams. Final result is:
			// Disk -> Buffer |-> Decryption -> Decompression -> Deserialization
			//                |-> MAC  
			GalleryState result;
			try (ObjectInputStream objectIn = buildStreams(macStream,
					decryptor)) {
				// Read the data and return the gallery state
				result = (GalleryState) objectIn.readObject();
			} catch (FileNotFoundException e) {
				throw e;
			} catch (ClassNotFoundException | IOException e) {
				throw new IntegrityViolationException(e);
			}
			
			byte[] actualMac = new byte[macSize];
			mac.doFinal(actualMac, 0);
			
			if(!Arrays.equals(expectedMac, actualMac)) {
				throw new IntegrityViolationException("Unable to authenticate");
			}
			
			return result;
		}
	}

	private ObjectInputStream buildStreams(InputStream in,
			BufferedBlockCipher decryptor) throws IOException {
		return new ObjectInputStream(new InflaterInputStream(
				new CipherInputStream(in, decryptor)));
	}
}
