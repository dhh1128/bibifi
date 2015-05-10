package org.builditbreakit.seada.common.exceptions;

public class UnexpectedException extends RuntimeException {
	private static final long serialVersionUID = 4393873823419174927L;

	public UnexpectedException() {
		super();
	}

	public UnexpectedException(String message) {
		super(message);
	}

	public UnexpectedException(Throwable cause) {
		super(cause);
	}

	public UnexpectedException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnexpectedException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
