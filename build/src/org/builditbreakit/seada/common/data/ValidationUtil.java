package org.builditbreakit.seada.common.data;

import org.builditbreakit.seada.common.TransitionEvent;

public final class ValidationUtil {
	private ValidationUtil() {
		super();
	}
	
	public static final long UINT32_MIN = 0L;
	public static final long UINT32_MAX = 4294967295L;

	public static void assertNotNull(Object value, String param) {
		if (value == null) {
			throw new IllegalArgumentException(param + " must not be null");
		}
	}

	public static void assertValidUINT32(long value, String param) {
		if ((value < UINT32_MIN) || (value > UINT32_MAX)) {
			throw new IllegalArgumentException(param
					+ " is not a valid integer: " + value);
		}
	}

	public static void assertValidVisitorName(String value) {
		assertNotNull(value, "Visitor name");
		if (value.isEmpty()) {
			throw new IllegalArgumentException("Visitor name must not be empty");
		}

		for (int i = 0, len = value.length(); i < len; ++i) {
			char c = value.charAt(i);
			if (!isAlpha(c)) {
				throw new IllegalArgumentException("Visitor name contains invalid characters: " + value);
			}
		}
	}

	public static void assertValidVisitorType(VisitorType visitorType) {
		assertNotNull(visitorType, "Visitor Type");
	}
	
	public static void assertValidRoomNumber(long roomNumber) {
		assertValidUINT32(roomNumber, "Room number");
	}

	public static void assertValidTimestamp(long timestamp) {
		assertValidUINT32(timestamp, "Timestamp");
	}

	public static void assertValidLocation(Location location) {
		assertNotNull(location, "Location");
	}
	
	public static void assertValidEvent(TransitionEvent event) {
		assertNotNull(event, "Event");
	}
	
	/**
	 * Similar to {@link Character#isAlphabetic(char)}, but only accepts ASCII.
	 */
	public static boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}
	
	/**
	 * Similar to {@link Character#isDigit(char)}, but only accepts ASCII.
	 */
	public static boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}
	
	/**
	 * Return true if char matches alphanumeric def from bibifi spec.
	 */
	public static boolean isAlphanumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}
	
	/**
	 * Similar to {@link Character#isSpace(char)}, but only accepts ASCII.
	 */
	public static boolean isSpace(char c) {
		return c == ' ' || c == '\t' || c == '\r' || c == '\n';
	}

	/**
	 * @return true if a string is something like -T or -B -- a switch as
	 * defined in the spec.
	 */
	public static boolean isSwitch(String s) {
		if (s == null || s.length() != 2 || s.charAt(0) != '-') {
			return false;
		}
		char c = s.charAt(1);
		return c >= 'A' && c <= 'Z';
	}
	
	public static void assertValidToken(String s) {
		boolean bad = false;
		if (s == null || s.isEmpty()) {
			bad = true;
		} else {
			for (int i = 0, len = s.length(); i < len; ++i) {
				if (!isAlphanumeric(s.charAt(i))) {
					bad = true;
					break;
				}
			}
		}
		if (bad) {
			throw new IllegalArgumentException("Bad token");
		}
	}
	
	public static void assertValidLogfile(String value) {
		boolean bad = false;
		if (value == null || value.isEmpty()) {
			bad = true;
		} else {
			/*
			for (int i = 0, len = value.length(); i < len; ++i) {
				char c = value.charAt(i);
				if (c != '_' && c != '.' && c != '/' && !isAlphanumeric(c)) {
					bad = true;
					break;
				}
			}*/
		}
		if (bad) {
			throw new IllegalArgumentException(value + " is not a valid logfile");
		}
	}
	
	/**
	 * Many args can only appear a single time on the cmdline. This method
	 * asserts that an arg has not been seen before, to catch that validation
	 * problem.
	 */
	public static void assertAssignedOnlyOnce(boolean expr) {
		if (!expr) {
			throw new IllegalArgumentException("Can't assign more than once");
		}
	}
}
