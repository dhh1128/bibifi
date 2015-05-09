package org.builditbreakit.seada.common.exceptions;

public class IntegrityViolationException extends RuntimeException {
	private static final long serialVersionUID = -5345811759956316023L;

	public IntegrityViolationException() {
		super();
	}

	protected IntegrityViolationException(String message, Throwable cause,
			boolean enableSupression, boolean writableStackTrace) {
		super(message, cause, enableSupression, writableStackTrace);
	}

	public IntegrityViolationException(String message, Throwable cause) {
		super(message, cause);
	}

	public IntegrityViolationException(String message) {
		super(message);
	}

	public IntegrityViolationException(Throwable cause) {
		super(cause);
	}
}
