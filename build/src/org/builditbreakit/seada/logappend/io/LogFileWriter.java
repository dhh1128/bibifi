package org.builditbreakit.seada.logappend.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.util.zip.DeflaterOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.Mac;

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
		Mac mac = Crypto.getMac(key);

		// Create a temporary working file
		File tempFile = File.createTempFile("bibifi-seada-", ".tmp");
		tempFile.deleteOnExit();

		// Chain output streams. Final result is:
		// Serialization -> Compression -> Encryption|-> Buffer -> Disk
		//                                           |-> Mac
		try (FileOutputStream fos = new FileOutputStream(tempFile);
				FileChannel fileChannel = fos.getChannel();
				OutputStream plaintextOut = new BufferedOutputStream(fos)) {
			// Make room for the mac in the header
			fileChannel.position(Crypto.MAC_SIZE);

			// Write the initialization vector
			plaintextOut.write(iv);

			// IV is not encrypted, so we add it to the mac manually
			mac.update(iv);

			try (ObjectOutputStream objectOut = buildOutputStreams(
					plaintextOut, encryptor, mac)) {
				// Write the data
				objectOut.writeObject(galleryState);
			}
		}
		
		// Go back and add the mac
		try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
			raf.write(mac.doFinal());
		}

		// Atomically overwrite existing file, if there is one, with new data
		Files.move(tempFile.toPath(), file.toPath(),
				StandardCopyOption.ATOMIC_MOVE);
	}

	private static ObjectOutputStream buildOutputStreams(
			OutputStream plaintextOut, Cipher encryptor, Mac mac)
			throws IOException {
		return new ObjectOutputStream(new DeflaterOutputStream(
				new CipherOutputStream(new MacBuildingOutputStream(
						plaintextOut, mac), encryptor)));
	}
}
