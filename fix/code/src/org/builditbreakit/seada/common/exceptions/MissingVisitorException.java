package org.builditbreakit.seada.common.exceptions;

public class MissingVisitorException extends RuntimeException {
	private static final long serialVersionUID = 6693659648592893328L;

	public MissingVisitorException() {
		super();
	}

	public MissingVisitorException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MissingVisitorException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingVisitorException(String message) {
		super(message);
	}

	public MissingVisitorException(Throwable cause) {
		super(cause);
	}
}
