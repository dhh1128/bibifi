package org.builditbreakit.seada.common.data;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class VisitorTest {
	/* ctor validation tests */

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyNameCtor() {
		new Visitor("", VisitorType.EMPLOYEE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullNameCtor() {
		new Visitor(null, VisitorType.EMPLOYEE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullVisitorTypeCtor() {
		new Visitor("Bob", null);
	}

	/* moveTo tests */

	@Test(expected = IllegalStateException.class)
	public void testOffPremisesToOffPremises() {
		runMoveTest(Location.OFF_PREMISES, Location.OFF_PREMISES);
	}

	@Test
	public void testOffPremisesToInGallery() {
		runMoveTest(Location.OFF_PREMISES, Location.IN_GALLERY);
	}

	@Test(expected = IllegalStateException.class)
	public void testOffPremisesToInRoom() {
		runMoveTest(Location.OFF_PREMISES, Location.locationOfRoom(101));
	}

	@Test
	public void testInGalleryToOffPremises() {
		runMoveTest(Location.IN_GALLERY, Location.OFF_PREMISES);
	}

	@Test(expected = IllegalStateException.class)
	public void testInGalleryToInGallery() {
		runMoveTest(Location.IN_GALLERY, Location.IN_GALLERY);
	}

	@Test
	public void testInGalleryToInRoom() {
		runMoveTest(Location.IN_GALLERY, Location.locationOfRoom(101));
	}

	@Test(expected = IllegalStateException.class)
	public void testInRoomToOffPremises() {
		runMoveTest(Location.locationOfRoom(101), Location.OFF_PREMISES);
	}

	@Test
	public void testInRoomToInGallery() {
		runMoveTest(Location.locationOfRoom(101), Location.IN_GALLERY);
	}

	@Test(expected = IllegalStateException.class)
	public void testInRoomToSameRoom() {
		runMoveTest(Location.locationOfRoom(101), Location.locationOfRoom(101));
	}

	@Test(expected = IllegalStateException.class)
	public void testInRoomToNewRoom() {
		runMoveTest(Location.locationOfRoom(101), Location.locationOfRoom(10));
	}
	
	@Test(expected = IllegalStateException.class)
	public void testOutOfOrderMove() {
		Visitor visitor = new Visitor("Bob", VisitorType.EMPLOYEE);
		visitor.moveTo(4, Location.IN_GALLERY);
		visitor.moveTo(1, Location.OFF_PREMISES);
	}

	/* getHistory Tests */

	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableHistory() {
		Visitor visitor = new Visitor("Bob", VisitorType.EMPLOYEE);
		visitor.getHistory().add(new LocationRecord(5, Location.IN_GALLERY));
	}

	/* getCurrentHistory Tests */

	@Test
	public void testGetCurrentLocationNoHistory() {
		Visitor visitor = new Visitor("Bob", VisitorType.EMPLOYEE);
		assertEquals(Location.OFF_PREMISES, visitor.getCurrentLocation());
	}

	@Test
	public void testGetCurrentLocationWithHistory() {
		Visitor visitor = new Visitor("Bob", VisitorType.EMPLOYEE);
		visitor.moveTo(5, Location.IN_GALLERY);
		assertEquals(Location.IN_GALLERY, visitor.getCurrentLocation());
	}

	/* Equals and Hashcode Tests */

	@Test
	public void testEqualsContract() {
		/*
		 * OK to suppress transient because verifier is not aware of
		 * Serialization Proxy pattern
		 */
		EqualsVerifier.forClass(Visitor.class).usingGetClass()
				.suppress(Warning.TRANSIENT_FIELDS).verify();
	}

	/* Serialization Tests */

	@Test
	public void testVisitorSerialization() throws IOException,
			ClassNotFoundException {
		testVisitorSerialization(new Visitor("Bob", VisitorType.EMPLOYEE));
	}

	@Test
	public void testReadObjectFails() {
		Visitor visitor = new Visitor("Bob", VisitorType.EMPLOYEE);
		TestUtil.assertReadObjectFails(visitor);
	}

	/* Serialization White-box Tests */

	@Test
	public void testDefensiveCopyPrivateCtor() throws Exception {
		Constructor<Visitor> ctor = getPrivateConstructor();

		List<LocationRecord> originalList = new LinkedList<>();
		Visitor visitor = ctor.newInstance("Bob", VisitorType.EMPLOYEE,
				originalList);

		originalList.add(new LocationRecord(5, Location.IN_GALLERY));

		assertTrue(visitor.getHistory().isEmpty());
	}

	@Test
	public void testNullNamePrivateCtor() throws Exception {
		assertPrivateCtorValidationCheck(null, VisitorType.EMPLOYEE,
				new LinkedList<>());
	}

	@Test
	public void testEmptyNamePrivateCtor() throws Exception {
		assertPrivateCtorValidationCheck("", VisitorType.EMPLOYEE,
				new LinkedList<>());
	}

	@Test
	public void testBadNamePrivateCtor() throws Exception {
		assertPrivateCtorValidationCheck("&", VisitorType.EMPLOYEE,
				new LinkedList<>());
	}

	@Test
	public void testNullVisitorTypePrivateCtor() throws Exception {
		assertPrivateCtorValidationCheck("Bob", null, new LinkedList<>());
	}

	@Test
	public void testNullHistoryPrivateCtor() throws Exception {
		assertPrivateCtorValidationCheck("Bob", VisitorType.EMPLOYEE, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMaliciousSerialization() throws Exception {
		testVisitorSerialization(createMaliciousVisitor());
	}

	/* Private helpers */

	private static void runMoveTest(Location from, Location to) {
		Visitor visitor = new Visitor("Bob", VisitorType.EMPLOYEE);
		if (from != Location.OFF_PREMISES) {
			if(from != Location.IN_GALLERY) {
				visitor.moveTo(4, Location.IN_GALLERY);
			}
			visitor.moveTo(5, from);
		}
		visitor.moveTo(6, to);
		assertEquals(to, visitor.getCurrentLocation());
	}

	private static void testVisitorSerialization(Visitor visitor)
			throws ClassNotFoundException, IOException {
		TestUtil.testSerialization(visitor,
				(result) -> EquivalenceUtil.assertEquivalent(visitor, result));
	}

	private static Visitor createMaliciousVisitor() throws Exception {
		Visitor maliciousObj = new Visitor("Bob", VisitorType.EMPLOYEE);

		Field stateField = Visitor.class.getDeclaredField("name");
		stateField.setAccessible(true);

		// Set the field to something illegal
		stateField.set(maliciousObj, null);

		return maliciousObj;
	}

	private static void assertPrivateCtorValidationCheck(String name,
			VisitorType visitorType, List<LocationRecord> history)
			throws Exception {
		Constructor<Visitor> ctor = getPrivateConstructor();
		try {
			ctor.newInstance(name, visitorType, history);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof IllegalArgumentException) {
				// Do nothing. Expected Exception
			}
		}
	}

	private static Constructor<Visitor> getPrivateConstructor()
			throws Exception {
		Constructor<Visitor> ctor = Visitor.class.getDeclaredConstructor(
				String.class, VisitorType.class, List.class);
		ctor.setAccessible(true);
		return ctor;
	}
}
