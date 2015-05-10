package org.builditbreakit.seada.common.data;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For testing if two data model object contain the exact samce data. Use just
 * like you would use JUnit assertions.
 */
public class EquivalenceUtil {
	public static void assertEquivalent(GalleryState expected,
			GalleryState actual) {
		assertEquivalent(null, expected, actual);
	}

	public static void assertEquivalent(Visitor expected, Visitor actual) {
		assertEquivalent(null, expected, actual);
	}

	public static void assertEquivalent(Location expected, Location actual) {
		assertEquivalent(null, expected, actual);
	}

	public static void assertEquivalent(LocationRecord expected,
			LocationRecord actual) {
		assertEquivalent(null, expected, actual);
	}
	
	public static void assertEquivalent(List<LocationRecord> expected,
			List<LocationRecord> actual) {
		assertEquivalent(null, expected, actual);
	}

	public static void assertEquivalent(String message, GalleryState expected,
			GalleryState actual) {
		if (checkBothNotNull(message, expected, actual)) {
			Map<String, Visitor> expectedVisitors = new HashMap<>();
			expected.getVisitors().forEach(
					(visitor) -> expectedVisitors.put(visitor.getName(),
							visitor));

			Map<String, Visitor> actualVisitors = new HashMap<>();
			actual.getVisitors().forEach(
					(visitor) -> actualVisitors.put(visitor.getName(), visitor));

			assertEquals(message, expectedVisitors.keySet(),
					actualVisitors.keySet());

			expectedVisitors.values().forEach(
					(expectedVisitor) -> {
						String name = expectedVisitor.getName();
						Visitor actualVisitor = actualVisitors.get(name);
						assertEquivalent(
								buildDebugMessage(message, "Visitor: " + name),
								expectedVisitor, actualVisitor);
					});
		}
	}

	public static void assertEquivalent(String message, Visitor expected,
			Visitor actual) {
		if (checkBothNotNull(message, expected, actual)) {
			assertEquals(buildDebugMessage(message, "Name"),
					expected.getName(), actual.getName());
			assertEquals(buildDebugMessage(message, "Visitor Type"),
					expected.getVisitorType(), actual.getVisitorType());
			assertEquivalent(buildDebugMessage(message, "History"),
					expected.getHistory(), actual.getHistory());
			assertEquivalent(buildDebugMessage(message, "Location"),
					expected.getCurrentLocation(), actual.getCurrentLocation());
		}
	}

	public static void assertEquivalent(String message, Location expected,
			Location actual) {
		assertEquals(message, expected, actual);
		assertSame(message, expected, actual);
	}

	public static void assertEquivalent(String message,
			LocationRecord expected, LocationRecord actual) {
		if (checkBothNotNull(message, expected, actual)) {
			assertEquals(buildDebugMessage(message, "Arrival Time"),
					expected.getArrivalTime(), actual.getArrivalTime());
			assertEquivalent(buildDebugMessage(message, "Location"),
					expected.getLocation(), actual.getLocation());
		}
	}

	public static void assertEquivalent(String message,
			List<LocationRecord> expected, List<LocationRecord> actual) {
		if (checkBothNotNull(message, expected, actual)) {
			assertEquals(buildDebugMessage(message, "List Size"),
					expected.size(), actual.size());
	
			for (int i = 0; i < expected.size(); i++) {
				assertEquivalent(buildDebugMessage(message, "At Index " + i),
						expected.get(i), actual.get(i));
			}
		}
	}

	private static String buildDebugMessage(String currentMessage,
			String additionalMessage) {
		if ((currentMessage != null) && !currentMessage.equals("")) {
			return currentMessage + " | " + additionalMessage;
		} else {
			return additionalMessage;
		}
	}

	/**
	 * @return false if both are null; true if both are not null
	 * @throws AssertionError
	 *             if one is null but not the other
	 */
	private static boolean checkBothNotNull(String message, Object expected,
			Object actual) {
		if ((expected == null) || (actual == null)) {
			if (expected == actual) {
				return false;
			}
			assertEquals(message, expected, actual);
		}
		return true;
	}
}
