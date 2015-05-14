package org.builditbreakit.seada.common.data;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class LocationTest {
	private static final int ROOM_LOWER_BOUND = 0;
	private static final int ROOM_UPPER_BOUND = 1073741823;

	/* isOffPremises Tests */

	@Test
	public void testOffPremisesIsOffPremises() {
		assertTrue(Location.OFF_PREMISES.isOffPremises());
	}

	@Test
	public void testGalleryIsNotOffPremises() {
		assertFalse(Location.IN_GALLERY.isOffPremises());
	}

	@Test
	public void testRoomIsNotOffPremises() {
		assertFalse(Location.locationOfRoom(101).isOffPremises());
	}

	/* isInGallery Tests */

	@Test
	public void testOffPremisesIsNotInGallery() {
		assertFalse(Location.OFF_PREMISES.isInGallery());
	}

	@Test
	public void testGalleryIsInGallery() {
		assertTrue(Location.IN_GALLERY.isInGallery());
	}

	@Test
	public void testRoomIsNotInGallery() {
		assertFalse(Location.locationOfRoom(101).isInGallery());
	}

	/* isInRoom Tests */

	@Test
	public void testOffPremisesIsNotInRoom() {
		assertFalse(Location.OFF_PREMISES.isInRoom());
	}

	@Test
	public void testGalleryIsNotInRoom() {
		assertFalse(Location.IN_GALLERY.isInRoom());
	}

	@Test
	public void testRoomIsInRoom() {
		assertTrue(Location.locationOfRoom(101).isInRoom());
	}

	/* getRoomNumber Tests */

	@Test
	public void testOffPremisesNullRoomNumber() {
		assertNull(Location.OFF_PREMISES.getRoomNumber());
	}

	@Test
	public void testGalleryNullRoomNumber() {
		assertNull(Location.IN_GALLERY.getRoomNumber());
	}

	@Test
	public void testRoomHasRoomNumber() {
		assertEquals(Integer.valueOf(101), Location.locationOfRoom(101)
				.getRoomNumber());
	}

	/* Constructor Tests */

	@Test
	public void testLocationOfRoomCaching() {
		assertSame(Location.locationOfRoom(101), Location.locationOfRoom(101));
	}

	@Test
	public void testLocationOfRoomLowerBound() {
		assertEquals(Integer.valueOf(ROOM_LOWER_BOUND),
				Location.locationOfRoom(ROOM_LOWER_BOUND).getRoomNumber());
	}

	@Test
	public void testLocationOfRoomUpperBound() {
		assertEquals(Integer.valueOf(ROOM_UPPER_BOUND),
				Location.locationOfRoom(ROOM_UPPER_BOUND).getRoomNumber());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLocationOfRoomLowerConstraint() {
		Location.locationOfRoom(ROOM_LOWER_BOUND - 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLocationOfRoomUpperConstraint() {
		Location.locationOfRoom(ROOM_UPPER_BOUND + 1);
	}

	/* Equals and Hashcode Tests */

	@Test
	public void testEqualsContract() {
		/*
		 * OK to suppress transient because verifier is not aware of
		 * Serialization Proxy pattern
		 */
		EqualsVerifier.forClass(Location.class).usingGetClass()
				.suppress(Warning.TRANSIENT_FIELDS).verify();
	}

	/* Serialization Tests */

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
	@Test(expected = IllegalArgumentException.class)
	public void testMaliciousSerialization() throws Exception {
		testLocationSerialization(createMaliciousLocation());
	}

	@Test
	public void testReadObjectFails() {
		Location location = Location.locationOfRoom(101);
		TestUtil.assertReadObjectFails(location);
	}
	
	/* Private helpers */

	private static void testLocationSerialization(Location location)
			throws ClassNotFoundException, IOException {
		TestUtil.testSerialization(location,
				(result) -> EquivalenceUtil.assertEquivalent(location, result));
	}
	
	private static Location createMaliciousLocation() throws Exception {
		Class<Location> clazz = Location.class;
		Constructor<Location> ctor = clazz.getDeclaredConstructor(Integer.TYPE);
		ctor.setAccessible(true);
		Location maliciousObj = ctor.newInstance(ROOM_LOWER_BOUND + 1);
		
		Field stateField = clazz.getDeclaredField("state");
		stateField.setAccessible(true);
		
		// Set the field to something illegal
		stateField.setInt(maliciousObj, ROOM_LOWER_BOUND - 10);
		
		return maliciousObj;
	}
}
