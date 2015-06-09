package org.builditbreakit.seada.common.io;

import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.crypto.Mac;

public class MacBuildingInputStream extends InputStream {
	private final InputStream in;
	private final Mac mac;

	public MacBuildingInputStream(InputStream in, Mac mac) {
		this.in = in;
		this.mac = mac;
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		in.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	@Override
	public int read() throws IOException {
		int byteValue = in.read();
		if (byteValue != -1) {
			mac.update((byte) byteValue);
		}
		return byteValue;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int bytesRead = in.read(b, off, len);
		if (bytesRead != -1) {
			mac.update(b, off, bytesRead);
		}
		return bytesRead;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int bytesRead = in.read(b);
		if (bytesRead != -1) {
			mac.update(b, 0, bytesRead);
		}
		return bytesRead;
	}

	@Override
	public synchronized void reset() throws IOException {
		in.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}
}
