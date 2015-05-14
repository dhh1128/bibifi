package org.builditbreakit.seada.logappend.io;

import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Mac;

import org.builditbreakit.seada.common.data.ValidationUtil;

class MacBuildingOutputStream extends OutputStream {
	private final Mac mac;
	private final OutputStream os;

	public MacBuildingOutputStream(OutputStream os, Mac mac) {
		ValidationUtil.assertNotNull(os, "Output Stream");
		ValidationUtil.assertNotNull(mac, "MAC");
		this.os = os;
		this.mac = mac;
	}

	@Override
	public void write(int b) throws IOException {
		mac.update((byte) b);
		os.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		mac.update(b);
		os.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		mac.update(b, off, len);
		os.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public void close() throws IOException {
		os.close();
	}
}