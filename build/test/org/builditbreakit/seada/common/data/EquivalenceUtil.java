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
			// Compare employees
			Map<String, Visitor> expectedEmployees = new HashMap<>();
			expected.getEmployees().forEach(
					(visitor) -> expectedEmployees.put(visitor.getName(),
							visitor));
			
			Map<String, Visitor> actualEmployees = new HashMap<>();
			actual.getEmployees().forEach(
					(visitor) -> actualEmployees.put(visitor.getName(),
							visitor));
			
			assertEquals(buildDebugMessage(message, "Employees"),
					expectedEmployees.keySet(), actualEmployees.keySet());
			
			expectedEmployees.values().forEach(
					(expectedVisitor) -> {
						String name = expectedVisitor.getName();
						Visitor actualVisitor = actualEmployees.get(name);
						assertEquivalent(
								buildDebugMessage(message, "Visitor: " + name),
								expectedVisitor, actualVisitor);
					});
							
			// Compare guests
			Map<String, Visitor> expectedGuests = new HashMap<>();
			expected.getGuests().forEach(
					(visitor) -> expectedGuests.put(visitor.getName(),
							visitor));
			
			Map<String, Visitor> actualGuests = new HashMap<>();
			actual.getGuests().forEach(
					(visitor) -> actualGuests.put(visitor.getName(), visitor));

			assertEquals(buildDebugMessage(message, "Guests"),
					expectedGuests.keySet(), actualGuests.keySet());

			expectedGuests.values().forEach(
					(expectedVisitor) -> {
						String name = expectedVisitor.getName();
						Visitor actualVisitor = actualGuests.get(name);
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
