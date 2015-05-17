package org.builditbreakit.seada.logappend.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.util.zip.DeflaterOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.io.Crypto;

public final class LogFileWriter {
	private File file;

	public LogFileWriter(File file) {
		this.file = file;
	}

	public void write(GalleryState galleryState, String password)
			throws IOException {
		// Configure Crypto
		Key key = Crypto.generateBaseKey(password);

		byte[] iv = Crypto.generateIV();
		Cipher encryptor = Crypto.getEncryptingCipher(key, iv);

		// Create a temporary working file
		File tempFile = File.createTempFile("bibifi-seada-", ".tmp");
		tempFile.deleteOnExit();

		// Chain output streams. Final result is:
		// Serialization -> Compression -> Encryption -> Buffer -> Disk
		try (OutputStream plaintextOut = new BufferedOutputStream(
				new FileOutputStream(tempFile))) {
			// Write the initialization vector
			plaintextOut.write(iv);

			try (ObjectOutputStream objectOut = buildOutputStreams(
					plaintextOut, encryptor)) {
				// Write the data
				objectOut.writeObject(galleryState);
			}
		}

		// Atomically overwrite existing file, if there is one, with new data
		Files.move(tempFile.toPath(), file.toPath(),
				StandardCopyOption.ATOMIC_MOVE);
	}

	private static ObjectOutputStream buildOutputStreams(
			OutputStream plaintextOut, Cipher encryptor) throws IOException {
		return new ObjectOutputStream(new DeflaterOutputStream(
				new CipherOutputStream(plaintextOut, encryptor)));
	}
}
