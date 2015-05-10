package org.builditbreakit.seada.logappend.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Key;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.io.Crypto;

public final class LogFileWriter {
	private File file;

	public LogFileWriter(File file) {
		this.file = file;
	}

	public void write(GalleryState galleryState, String password) throws IOException {
		Key key = Crypto.genKey(password);
		
		byte[] plaintext = toBytes(galleryState);
		
		byte[] ciphertext = Crypto.encrypt(key, plaintext);
		byte[] mac = Crypto.mac(key, ciphertext);
		
		File tempFile = File.createTempFile("bibifi-seada", "tmp");
		tempFile.deleteOnExit();
		try(BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile))) {
			out.write(mac);
			out.write(ciphertext);
			out.flush();
		}
		
		// TODO verify VM supports this option
		Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE);
	}

	private static byte[] toBytes(GalleryState galleryState) throws IOException {
		try (ByteArrayOutputStream b = new ByteArrayOutputStream();
				ObjectOutput a = new ObjectOutputStream(b)) {
			a.writeObject(galleryState);
			a.flush();
			return b.toByteArray();
		}
	}
}
