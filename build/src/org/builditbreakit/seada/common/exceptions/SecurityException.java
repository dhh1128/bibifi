package org.builditbreakit.seada.common.exceptions;

public class SecurityException extends RuntimeException {
	private static final long serialVersionUID = -8878141493405962517L;

	public SecurityException() {
		super();
	}

	public SecurityException(String message) {
		super(message);
	}

	public SecurityException(Throwable cause) {
		super(cause);
	}

	public SecurityException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecurityException(String message, Throwable cause,
			boolean enableSupression, boolean writeableStackTrace) {
		super(message, cause, enableSupression, writeableStackTrace);
	}
}
