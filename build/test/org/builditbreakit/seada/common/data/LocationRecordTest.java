package org.builditbreakit.seada.common.data;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class LocationRecordTest {
	public static final int TIME_LOWER_BOUND = 0;
	public static final int TIME_UPPER_BOUND = 1073741823;

	/* Constructor Tests */

	@Test
	public void testLowerBoundCtor() {
		new LocationRecord(TIME_LOWER_BOUND, Location.IN_GALLERY);
	}

	@Test
	public void testUpperBoundCtor() {
		new LocationRecord(TIME_UPPER_BOUND, Location.IN_GALLERY);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFailLowerBoundCtor() {
		new LocationRecord(TIME_LOWER_BOUND - 1, Location.IN_GALLERY);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFailUpperBoundCtor() {
		new LocationRecord(TIME_UPPER_BOUND + 1, Location.IN_GALLERY);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFailNullLocationCtor() {
		new LocationRecord(5, null);
	}

	/* Equals and Hashcode Tests */

	@Test
	public void testEqualsContract() {
		if (!TestUtil.usesDefaultEquals(LocationRecord.class)) {
			EqualsVerifier.forClass(LocationRecord.class).verify();
		}
	}

	/* Serialization Tests */

	@Test
	public void testLocationRecordSerialization() throws IOException,
			ClassNotFoundException {
		testLocationRecordSerialization(new LocationRecord(5,
				Location.locationOfRoom(101)));
	}

	@Test
	public void testOffPremisesSerialization() throws IOException,
			ClassNotFoundException {
		testLocationSerialization(Location.OFF_PREMISES);
	}

	@Test
	public void testInGallerySerialization() throws IOException,
			ClassNotFoundException {
		testLocationSerialization(Location.IN_GALLERY);
	}

	@Test
	public void testInRoomSerialization() throws IOException,
			ClassNotFoundException {
		testLocationSerialization(Location.locationOfRoom(101));
	}

	/**
	 * Checks that deserialized data is actually validated. Java's default
	 * serialization will fail this test. This is a white-box test.
	 */
	@Test(expected = Exception.class)
	public void testMaliciousRecordSerialization() throws Exception {
		testLocationRecordSerialization(createMaliciousRecord());
	}

	/**
	 * Checks that deserialized data is actually validated. Java's default
	 * serialization will fail this test. This is a white-box test.
	 */
	@Test(expected = Exception.class)
	public void testMaliciousLocationSerialization() throws Exception {
		testLocationSerialization(createMaliciousLocation());
	}

	/* Private helpers */

	private static void testLocationRecordSerialization(LocationRecord record)
			throws ClassNotFoundException, IOException {
		TestUtil.testSerialization(record,
				(result) -> EquivalenceUtil.assertEquivalent(record, result));
	}
	
	private static void testLocationSerialization(Location location)
			throws ClassNotFoundException, IOException {
		testLocationRecordSerialization(new LocationRecord(5, location));
	}

	private static LocationRecord createMaliciousRecord() throws Exception {
		LocationRecord maliciousObj = new LocationRecord(5, Location.IN_GALLERY);
		
		Field stateField = LocationRecord.class.getDeclaredField("arrivalTime");
		stateField.setAccessible(true);
		
		// Set the field to something illegal
		stateField.setInt(maliciousObj, TIME_LOWER_BOUND - 10);
		
		return maliciousObj;
	}
	
	private static Location createMaliciousLocation() throws Exception {
		Class<Location> clazz = Location.class;
		Constructor<Location> ctor = clazz.getDeclaredConstructor(Integer.TYPE);
		ctor.setAccessible(true);
		Location maliciousObj = ctor.newInstance(LocationTest.ROOM_UPPER_BOUND + 1);
		
		Field stateField = clazz.getDeclaredField("state");
		stateField.setAccessible(true);
		
		// Set the field to something illegal
		stateField.setInt(maliciousObj, LocationTest.ROOM_LOWER_BOUND - 3);
		
		return maliciousObj;
	}
}
