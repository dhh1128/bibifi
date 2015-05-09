package org.builditbreakit.seada.common.data;

final class ValidationUtil {
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

	public static void assertValidString(String value, String param) {
		assertNotNull(value, param);
		if (value.isEmpty()) {
			throw new IllegalArgumentException(param + " must not be empty");
		}

		for (char c : value.toCharArray()) {
			if (((c < 'a') || (c > 'z')) && ((c < 'A') && (c > 'Z'))) {
				throw new IllegalArgumentException(param
						+ " contains invalid characters: " + value);
			}
		}
	}

	public static void assertValidVisitorType(VisitorType visitorType) {
		assertNotNull(visitorType, "Visitor Type");
	}
	
	public static void assertValidRoomNumber(long roomNumber) {
		assertValidUINT32(roomNumber, "Room number");
	}

	public static void assertValidVisitorName(String visitorName) {
		assertValidString(visitorName, "Visitor name");
	}

	public static void assertValidTimestamp(long timestamp) {
		assertValidUINT32(timestamp, "Timestamp");
	}

	public static void assertValidLocation(Location location) {
		assertNotNull(location, "Location");
	}
}
