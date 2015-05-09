package org.builditbreakit.seada.common.data;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocationTest {
	private static final long ROOM_LOWER_BOUND = 0L;
	private static final long ROOM_UPPER_BOUND = 4294967295L;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	
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
		assertEquals(Long.valueOf(101), Location.locationOfRoom(101)
				.getRoomNumber());
	}

	
	
	@Test
	public void testLocationOfRoomCaching() {
		assertSame(Location.locationOfRoom(101), Location.locationOfRoom(101));
	}

	@Test
	public void testLocationOfRoomLowerBound() {
		assertEquals(Long.valueOf(ROOM_LOWER_BOUND),
				Location.locationOfRoom(ROOM_LOWER_BOUND).getRoomNumber());
	}

	@Test
	public void testLocationOfRoomUpperBound() {
		assertEquals(Long.valueOf(ROOM_UPPER_BOUND),
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
	
	
	
	@Test
	public void testEqualsContract() {
		/*
		 * OK to suppress transient because verifier is not aware of
		 * Serialization Proxy pattern
		 */
		EqualsVerifier.forClass(Location.class).usingGetClass()
				.suppress(Warning.TRANSIENT_FIELDS).verify();
	}
	
	
	
	@Test
	public void testOffPremisesSerialization() throws IOException, ClassNotFoundException {
		testLocationSerialization(Location.OFF_PREMISES);
	}
	
	@Test
	public void testInGallerySerialization() throws IOException, ClassNotFoundException {
		testLocationSerialization(Location.IN_GALLERY);
	}
	
	@Test
	public void testInRoomSerialization() throws IOException, ClassNotFoundException {
		testLocationSerialization(Location.locationOfRoom(101));
	}
	
	@Test
	public void testReadObjectFails() {
		Location location = Location.locationOfRoom(101);
		TestUtil.assertReadObjectFails(location);
	}

	
	
	private static void testLocationSerialization(Location location)
			throws IOException, FileNotFoundException, ClassNotFoundException {
		File tempFile = File.createTempFile("bibifi-test", "tmp");
		tempFile.deleteOnExit();

		try (ObjectOutputStream out = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(tempFile)))) {
			out.writeObject(location);
		}

		try (ObjectInputStream in = new ObjectInputStream(
				new BufferedInputStream(new FileInputStream(tempFile)))) {
			Location result = (Location) in.readObject();
			assertSame(location, result);
		}
	}
}
