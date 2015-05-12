package org.builditbreakit.seada.common.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.util.Arrays;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

public final class LogFileReader {
	private File file;

	public LogFileReader(File file) {
		this.file = file;
	}

	public GalleryState read(String password) throws IOException {
		Path path = file.toPath();

		byte[] fileBytes = Files.readAllBytes(path);
		byte[] mac = Arrays.copyOf(fileBytes, Crypto.MAC_BYTES);
		byte[] ciphertext = Arrays.copyOfRange(fileBytes, Crypto.MAC_BYTES,
				fileBytes.length);

		Key key = Crypto.genKey(password);
		if (!Crypto.authenticate(key, mac, ciphertext)) {
			throw new SecurityException();
		}
		return fromBytes(Crypto.decrypt(key, ciphertext));
	}

	private static GalleryState fromBytes(byte[] objectBytes)
			throws IOException {
		try (ByteArrayInputStream b = new ByteArrayInputStream(objectBytes);
				ObjectInput a = new ObjectInputStream(b)) {
			return (GalleryState) a.readObject();
		} catch (ClassNotFoundException e) {
			throw new IntegrityViolationException(e);
		}
	}
}
