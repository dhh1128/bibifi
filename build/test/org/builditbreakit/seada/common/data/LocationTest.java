package org.builditbreakit.seada.common.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class LocationTest {
	public static final int ROOM_LOWER_BOUND = 0;
	public static final int ROOM_UPPER_BOUND = 1073741823;

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
}
