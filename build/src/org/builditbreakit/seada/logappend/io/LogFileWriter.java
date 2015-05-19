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
import java.util.zip.DeflaterOutputStream;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.io.CipherOutputStream;
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
		byte[] key = Crypto.generateKey(password);

		byte[] iv = Crypto.generateIV();
		BufferedBlockCipher encryptor = Crypto.getEncryptingCipher(key, iv);
		Mac mac = Crypto.getMac(key);

		// Create a temporary working file
		File tempFile = File.createTempFile("bibifi-seada-", ".tmp");
		tempFile.deleteOnExit();

		try (FileOutputStream fos = new FileOutputStream(tempFile);
				FileChannel fileChannel = fos.getChannel();
				OutputStream macOut = new MacBuildingOutputStream(
						new BufferedOutputStream(fos), mac)) {
			// Make room for the mac in the header
			fileChannel.position(mac.getMacSize());

			// Write the initialization vector
			macOut.write(iv);

			// Chain output streams. Final result is:
			// Serialization -> Compression -> Encryption |-> Buffer -> Disk
			//                                            |-> MAC
			try (ObjectOutputStream objectOut = buildOutputStreams(macOut,
					encryptor)) {
				// Write the data
				objectOut.writeObject(galleryState);
			}
		}

		// Go back and add the mac
		byte[] cipherMac = new byte[mac.getMacSize()];
		mac.doFinal(cipherMac, 0);
		try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
			raf.write(cipherMac);
		}

		// Atomically overwrite existing file, if there is one, with new data
		Files.move(tempFile.toPath(), file.toPath(),
				StandardCopyOption.ATOMIC_MOVE);
	}

	private static ObjectOutputStream buildOutputStreams(
			OutputStream plaintextOut, BufferedBlockCipher encryptor)
			throws IOException {
		return new ObjectOutputStream(new DeflaterOutputStream(
				new CipherOutputStream(plaintextOut, encryptor)));
	}
}
