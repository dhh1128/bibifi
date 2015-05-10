package org.builditbreakit.seada.common.data;

import java.io.IOException;
import java.lang.reflect.Field;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class LocationRecordTest {
	private static final long TIME_LOWER_BOUND = 0L;
	private static final long TIME_UPPER_BOUND = 4294967295L;

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
	
	/**
	 * Checks that deserialized data is actually validated. Java's default
	 * serialization will fail this test. This is a white-box test.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testMaliciousSerialization() throws Exception {
		testLocationRecordSerialization(createMaliciousRecord());
	}

	@Test
	public void testReadObjectFails() {
		LocationRecord record = new LocationRecord(5, Location.OFF_PREMISES);
		TestUtil.assertReadObjectFails(record);
	}

	/* Private helpers */

	private static void testLocationRecordSerialization(LocationRecord record)
			throws ClassNotFoundException, IOException {
		TestUtil.testSerialization(record,
				(result) -> EquivalenceUtil.assertEquivalent(record, result));
	}
	
	private static LocationRecord createMaliciousRecord() throws Exception {
		LocationRecord maliciousObj = new LocationRecord(5, Location.IN_GALLERY);
		
		Field stateField = LocationRecord.class.getDeclaredField("arrivalTime");
		stateField.setAccessible(true);
		
		// Set the field to something illegal
		stateField.setLong(maliciousObj, TIME_LOWER_BOUND - 10);
		
		return maliciousObj;
	}
}
