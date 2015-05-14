package org.builditbreakit.seada.common.data;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;
import org.junit.Test;

public class GalleryStateTest {

	/* getVisitors Tests */

	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableVisitors() {
		GalleryState galleryState = new GalleryState();
		galleryState.getVisitors().clear();
	}

	/* getVisitor Tests */

	@Test(expected = IllegalStateException.class)
	public void testGetVisitorMissingVisitor() {
		GalleryState galleryState = new GalleryState();
		galleryState.getVisitor("Bob", VisitorType.EMPLOYEE);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetVisitorWrongVisitorType() {
		GalleryState galleryState = new GalleryState();

		String visitorName = "Bob";
		galleryState.arriveAtBuilding(5, visitorName, VisitorType.GUEST);
		galleryState.getVisitor(visitorName, VisitorType.EMPLOYEE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetVisitorNullName() {
		GalleryState galleryState = new GalleryState();

		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(5, visitorName, visitorType);
		galleryState.getVisitor(null, visitorType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetVisitorEmptyName() {
		GalleryState galleryState = new GalleryState();

		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(5, visitorName, visitorType);
		galleryState.getVisitor("", visitorType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetVisitorInvalidName() {
		GalleryState galleryState = new GalleryState();

		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(5, visitorName, visitorType);
		galleryState.getVisitor("&", visitorType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetVisitorNullVisitorType() {
		GalleryState galleryState = new GalleryState();

		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(5, visitorName, visitorType);
		galleryState.getVisitor(visitorName, null);
	}

	/* arriveAtBuilding Tests */

	@Test
	public void testArriveAtBuilding() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime, visitorName, visitorType);
		Visitor visitor = galleryState.getVisitor(visitorName, visitorType);

		Location expectedLocation = Location.IN_GALLERY;

		assertEquals("Location", expectedLocation, visitor.getCurrentLocation());
		assertEquals("Name", visitorName, visitor.getName());
		assertEquals("Visitor Type", visitorType, visitor.getVisitorType());

		List<LocationRecord> expectedHistory = Arrays
				.asList(new LocationRecord(arrivalTime, expectedLocation));
		EquivalenceUtil.assertEquivalent("History", expectedHistory,
				visitor.getHistory());
	}

	@Test(expected = IllegalStateException.class)
	public void testArriveAtBuildingWrongType() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";

		galleryState.arriveAtBuilding(arrivalTime++, visitorName,
				VisitorType.GUEST);
		galleryState.departBuilding(arrivalTime++, visitorName,
				VisitorType.GUEST);
		galleryState.arriveAtBuilding(arrivalTime++, visitorName,
				VisitorType.EMPLOYEE);
	}

	@Test(expected = IllegalStateException.class)
	public void testArriveAtBuildingWrongOrderDifferentPerson() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		galleryState.arriveAtBuilding(arrivalTime, "Bob", VisitorType.GUEST);
		galleryState.arriveAtBuilding(arrivalTime, "Tom", VisitorType.EMPLOYEE);
	}

	@Test(expected = IllegalStateException.class)
	public void testArriveAtBuildingFromGallery() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		galleryState.arriveAtBuilding(arrivalTime++, "Bob", VisitorType.GUEST);
		galleryState.arriveAtBuilding(arrivalTime++, "Bob", VisitorType.GUEST);
	}

	@Test(expected = IllegalStateException.class)
	public void testArriveAtBuildingFromRoom() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		galleryState.arriveAtBuilding(arrivalTime++, "Bob", VisitorType.GUEST);
		galleryState.arriveAtRoom(arrivalTime++, "Bob", VisitorType.GUEST, 101);
		galleryState.arriveAtBuilding(arrivalTime++, "Bob", VisitorType.GUEST);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArriveAtBuildingNullName() {
		GalleryState galleryState = new GalleryState();
		galleryState.arriveAtBuilding(5, null, VisitorType.GUEST);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArriveAtBuildingEmptyName() {
		GalleryState galleryState = new GalleryState();
		galleryState.arriveAtBuilding(5, "", VisitorType.GUEST);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArriveAtBuildingInvalidName() {
		GalleryState galleryState = new GalleryState();
		galleryState.arriveAtBuilding(5, "&", VisitorType.GUEST);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArriveAtBuildingInvalidTime() {
		GalleryState galleryState = new GalleryState();
		galleryState.arriveAtBuilding(-1, "Bob", VisitorType.GUEST);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArriveAtBuildingNullVisitorType() {
		GalleryState galleryState = new GalleryState();
		galleryState.arriveAtBuilding(-1, "Bob", null);
	}

	/* arriveAtBuilding Tests */

	@Test
	public void testArriveAtRoom() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		int roomNumber = 101;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime + 1, visitorName, visitorType,
				roomNumber);
		Visitor visitor = galleryState.getVisitor(visitorName, visitorType);

		Location expectedLocation = Location.locationOfRoom(roomNumber);

		assertEquals("Location", expectedLocation, visitor.getCurrentLocation());
		assertEquals("Name", visitorName, visitor.getName());
		assertEquals("Visitor Type", visitorType, visitor.getVisitorType());

		List<LocationRecord> expectedHistory = Arrays.asList(
				new LocationRecord(arrivalTime, Location.IN_GALLERY),
				new LocationRecord(arrivalTime + 1, expectedLocation));
		EquivalenceUtil.assertEquivalent("History", expectedHistory,
				visitor.getHistory());
	}

	@Test(expected = IllegalStateException.class)
	public void testArriveAtRoomWrongType() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";

		galleryState.arriveAtBuilding(arrivalTime++, visitorName,
				VisitorType.GUEST);
		galleryState.arriveAtRoom(arrivalTime++, visitorName,
				VisitorType.EMPLOYEE, 101);
	}

	@Test(expected = IllegalStateException.class)
	public void testArriveAtRoomSamePersonTimeCheck() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime, visitorName, visitorType, 101);
	}

	@Test(expected = IllegalStateException.class)
	public void testArriveAtRoomDifferentPersonTimeCheck() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime, "Bob", visitorType);
		galleryState.arriveAtBuilding(arrivalTime + 1, "Tom",
				VisitorType.EMPLOYEE);
		galleryState.arriveAtRoom(arrivalTime + 1, "Bob", visitorType, 101);
	}

	@Test(expected = IllegalStateException.class)
	public void testArriveAtRoomDifferentPersonTimeUpdate() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime, "Bob", visitorType);
		galleryState.arriveAtRoom(arrivalTime + 1, "Bob", visitorType, 101);
		galleryState.arriveAtBuilding(arrivalTime + 1, "Tom",
				VisitorType.EMPLOYEE);
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingVisitorArriveAtRoom() {
		GalleryState galleryState = new GalleryState();
		galleryState.arriveAtRoom(5, "Bob", VisitorType.GUEST, 101);
	}

	@Test(expected = IllegalStateException.class)
	public void testArriveAtRoomFromOffPremises() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.departBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType, 101);
	}

	@Test(expected = IllegalStateException.class)
	public void testArriveAtRoomFromSameRoom() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		int roomNumber = 101;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType,
				roomNumber);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType,
				roomNumber);
	}

	@Test(expected = IllegalStateException.class)
	public void testArriveAtRoomFromDifferentRoom() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		int roomNumber = 101;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType,
				roomNumber);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType,
				roomNumber + 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArriveAtRoomNullName() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		int roomNumber = 101;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, null, visitorType, roomNumber);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArriveAtRoomEmptyName() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		int roomNumber = 101;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, "", visitorType, roomNumber);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArriveAtRoomInvalidName() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		int roomNumber = 101;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, "&", visitorType, roomNumber);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArriveAtRoomInvalidTime() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		int roomNumber = 101;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(Integer.MAX_VALUE, visitorName, visitorType,
				roomNumber);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArriveAtRoomNullVisitorType() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		int roomNumber = 101;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, null, roomNumber);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArriveAtRoomInvalidRoomNumber() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType, -1);
	}

	/* departBuilding Tests */

	@Test
	public void testDepartBuilding() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime, visitorName, visitorType);
		galleryState.departBuilding(arrivalTime + 1, visitorName, visitorType);
		Visitor visitor = galleryState.getVisitor(visitorName, visitorType);

		Location expectedLocation = Location.OFF_PREMISES;

		assertEquals("Location", expectedLocation, visitor.getCurrentLocation());
		assertEquals("Name", visitorName, visitor.getName());
		assertEquals("Visitor Type", visitorType, visitor.getVisitorType());

		List<LocationRecord> expectedHistory = Arrays.asList(
				new LocationRecord(arrivalTime, Location.IN_GALLERY),
				new LocationRecord(arrivalTime + 1, expectedLocation));
		EquivalenceUtil.assertEquivalent("History", expectedHistory,
				visitor.getHistory());
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartBuildingFromOffPremises() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
	
		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.departBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.departBuilding(arrivalTime++, visitorName, visitorType);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartBuildingFromRoom() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
	
		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType, 101);
		galleryState.departBuilding(arrivalTime++, visitorName, visitorType);
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingVisitorDepartBuilding() {
		GalleryState galleryState = new GalleryState();
		galleryState.departBuilding(5, "Bob", VisitorType.GUEST);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartBuildingWrongType() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		String visitorName = "Bob";
	
		galleryState.arriveAtBuilding(arrivalTime++, visitorName,
				VisitorType.GUEST);
		galleryState.departBuilding(arrivalTime++, visitorName,
				VisitorType.EMPLOYEE);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartBuildingWrongOrderSamePerson() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
	
		galleryState.arriveAtBuilding(arrivalTime, visitorName, visitorType);
		galleryState.departBuilding(arrivalTime - 1, visitorName, visitorType);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartBuildingSamePersonTimeCheck() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
	
		galleryState.arriveAtBuilding(arrivalTime, visitorName, visitorType);
		galleryState.departBuilding(arrivalTime, visitorName, visitorType);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartBuildingDifferentPersonTimeCheck() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		VisitorType visitorType = VisitorType.GUEST;
	
		galleryState.arriveAtBuilding(arrivalTime, "Bob", visitorType);
		galleryState.arriveAtBuilding(arrivalTime + 1, "Tom",
				VisitorType.EMPLOYEE);
		galleryState.departBuilding(arrivalTime + 1, "Bob", visitorType);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartBuildingDifferentPersonTimeUpdate() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		VisitorType visitorType = VisitorType.GUEST;
	
		galleryState.arriveAtBuilding(arrivalTime, "Bob", visitorType);
		galleryState.departBuilding(arrivalTime + 1, "Bob", visitorType);
		galleryState.arriveAtBuilding(arrivalTime + 1, "Tom",
				VisitorType.EMPLOYEE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDepartBuildingNullName() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
	
		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.departBuilding(arrivalTime++, null, visitorType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDepartBuildingEmptyName() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
	
		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.departBuilding(arrivalTime++, "", visitorType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDepartBuildingInvalidName() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
	
		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.departBuilding(arrivalTime++, "&", visitorType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDepartBuildingInvalidTime() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
	
		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.departBuilding(Integer.MAX_VALUE, visitorName, visitorType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDepartBuildingNullVisitorType() {
		GalleryState galleryState = new GalleryState();
	
		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
	
		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.departBuilding(arrivalTime++, visitorName, null);
	}
	
	/* departRoom Tests */

	@Test
	public void testDepartRoom() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		int roomNumber = 101;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime + 1, visitorName, visitorType,
				roomNumber);
		galleryState.departRoom(arrivalTime + 2, visitorName, visitorType,
				roomNumber);
		Visitor visitor = galleryState.getVisitor(visitorName, visitorType);

		Location expectedLocation = Location.IN_GALLERY;

		assertEquals("Location", expectedLocation, visitor.getCurrentLocation());
		assertEquals("Name", visitorName, visitor.getName());
		assertEquals("Visitor Type", visitorType, visitor.getVisitorType());

		List<LocationRecord> expectedHistory = Arrays.asList(
				new LocationRecord(arrivalTime, Location.IN_GALLERY),
				new LocationRecord(arrivalTime + 1, Location
						.locationOfRoom(roomNumber)), new LocationRecord(
						arrivalTime + 2, expectedLocation));
		EquivalenceUtil.assertEquivalent("History", expectedHistory,
				visitor.getHistory());
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartRoomFromOffPremises() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.departBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.departRoom(arrivalTime++, visitorName, visitorType, 101);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartRoomFromGallery() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.departRoom(arrivalTime++, visitorName, visitorType, 101);
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingVisitorDepartRoom() {
		GalleryState galleryState = new GalleryState();
		galleryState.departRoom(5, "Bob", VisitorType.GUEST, 101);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartRoomWrongType() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		int roomNumber = 101;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName,
				VisitorType.GUEST);
		galleryState.arriveAtRoom(arrivalTime++, visitorName,
				VisitorType.GUEST, roomNumber);
		galleryState.departRoom(arrivalTime++, visitorName,
				VisitorType.EMPLOYEE, roomNumber);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartRoomWrongRoom() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		int roomNumber = 101;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName,
				VisitorType.GUEST);
		galleryState.arriveAtRoom(arrivalTime++, visitorName,
				VisitorType.GUEST, roomNumber);
		galleryState.departRoom(arrivalTime++, visitorName,
				VisitorType.GUEST, roomNumber + 1);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartRoomWrongOrderSamePerson() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
		int roomNumber = 101;

		galleryState.arriveAtBuilding(arrivalTime, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime + 1, visitorName, visitorType,
				roomNumber);
		galleryState.departRoom(arrivalTime + 1, visitorName, visitorType,
				roomNumber);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartRoomSamePersonTimeCheck() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
		int roomNumber = 101;

		galleryState.arriveAtBuilding(arrivalTime, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime + 1, visitorName, visitorType,
				roomNumber);
		galleryState.departRoom(arrivalTime + 1, visitorName, visitorType,
				roomNumber);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartRoomDifferentPersonInBuidlingTimeCheck() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		VisitorType visitorType = VisitorType.GUEST;
		int roomNumber = 101;

		galleryState.arriveAtBuilding(arrivalTime, "Bob", visitorType);
		galleryState.arriveAtRoom(arrivalTime + 1, "Bob", visitorType,
				roomNumber);
		galleryState.arriveAtBuilding(arrivalTime + 2, "Tom",
				VisitorType.EMPLOYEE);
		galleryState
				.departRoom(arrivalTime + 2, "Bob", visitorType, roomNumber);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartRoomDifferentPersonInRoomTimeCheck() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime, "Bob", visitorType);
		galleryState.arriveAtRoom(arrivalTime + 1, "Bob", visitorType, 101);
		galleryState.arriveAtBuilding(arrivalTime + 2, "Tom",
				VisitorType.EMPLOYEE);
		galleryState.arriveAtRoom(arrivalTime + 3, "Tom", VisitorType.EMPLOYEE,
				102);
		galleryState.departRoom(arrivalTime + 3, "Bob", visitorType, 101);
	}

	@Test(expected = IllegalStateException.class)
	public void testDepartRoomDifferentPersonInRoomTimeUpdate() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		VisitorType visitorType = VisitorType.GUEST;
		int roomNumber = 101;

		galleryState.arriveAtBuilding(arrivalTime, "Bob", visitorType);
		galleryState.arriveAtRoom(arrivalTime + 1, "Bob", visitorType,
				roomNumber);
		galleryState.arriveAtBuilding(arrivalTime + 2, "Tom",
				VisitorType.EMPLOYEE);
		galleryState
				.departRoom(arrivalTime + 3, "Bob", visitorType, roomNumber);
		galleryState.arriveAtRoom(arrivalTime + 3, "Tom", VisitorType.EMPLOYEE,
				102);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDepartRoomNullName() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
		int roomNumber = 101;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType,
				roomNumber);
		galleryState.departRoom(arrivalTime++, null, visitorType, roomNumber);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDepartRoomEmptyName() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
		int roomNumber = 101;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType,
				roomNumber);
		galleryState.departRoom(arrivalTime++, "", visitorType, roomNumber);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDepartRoomInvalidName() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
		int roomNumber = 101;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType,
				roomNumber);
		galleryState.departRoom(arrivalTime++, "&", visitorType, roomNumber);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDepartRoomInvalidTime() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
		int roomNumber = 101;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType,
				roomNumber);
		galleryState.departRoom(Integer.MAX_VALUE, visitorName, visitorType,
				roomNumber);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDepartRoomNullVisitorType() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;
		int roomNumber = 101;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType,
				roomNumber);
		galleryState.departRoom(arrivalTime++, visitorName, null, roomNumber);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDepartRoomInvalidRoomNumber() {
		GalleryState galleryState = new GalleryState();

		int arrivalTime = 5;
		String visitorName = "Bob";
		VisitorType visitorType = VisitorType.GUEST;

		galleryState.arriveAtBuilding(arrivalTime++, visitorName, visitorType);
		galleryState.arriveAtRoom(arrivalTime++, visitorName, visitorType, 101);
		galleryState.departRoom(arrivalTime++, visitorName, visitorType, -1);
	}

	/* Equals and Hashcode Tests */

	@Test
	public void testEqualsContract() {
		if (!TestUtil.usesDefaultEquals(GalleryState.class)) {
			EqualsVerifier.forClass(GalleryState.class).verify();
		}
	}

	/* Serialization Tests */

	@Test
	public void testEmptyGalleryStateSerialization() throws IOException,
			ClassNotFoundException {
		testGalleryStateSerialization(new GalleryState());
	}

	@Test
	public void testGalleryStateSerialization() throws IOException,
			ClassNotFoundException {
		GalleryState state = new GalleryState();
		int time = 1;
		state.arriveAtBuilding(time++, "Bob", VisitorType.EMPLOYEE);
		state.arriveAtRoom(time++, "Bob", VisitorType.EMPLOYEE, 101);
		state.arriveAtBuilding(time++, "Jill", VisitorType.EMPLOYEE);
		state.arriveAtBuilding(time++, "Alice", VisitorType.EMPLOYEE);
		state.arriveAtRoom(time++, "Jill", VisitorType.EMPLOYEE, 101);
		state.departRoom(time++, "Bob", VisitorType.EMPLOYEE, 101);
		state.arriveAtBuilding(time++, "John", VisitorType.GUEST);
		state.departBuilding(time++, "John", VisitorType.GUEST);
		state.arriveAtBuilding(time++, "John", VisitorType.GUEST);
		state.departBuilding(time++, "John", VisitorType.GUEST);

		testGalleryStateSerialization(state);
	}

	@Test(expected = IllegalStateException.class)
	public void testGalleryStateSerializationWrongTime() throws IOException,
			ClassNotFoundException {
		GalleryState state = new GalleryState();
		int time = 1;
		state.arriveAtBuilding(time++, "Bob", VisitorType.EMPLOYEE);
		state.arriveAtRoom(time++, "Bob", VisitorType.EMPLOYEE, 101);
		state.arriveAtBuilding(time++, "Jill", VisitorType.EMPLOYEE);
		state.arriveAtBuilding(time++, "Alice", VisitorType.EMPLOYEE);
		state.arriveAtRoom(time++, "Jill", VisitorType.EMPLOYEE, 101);
		state.departRoom(time++, "Bob", VisitorType.EMPLOYEE, 101);
		state.arriveAtBuilding(time++, "John", VisitorType.GUEST);
		state.departBuilding(time++, "John", VisitorType.GUEST);
		state.arriveAtBuilding(time++, "John", VisitorType.GUEST);
		state.departBuilding(time++, "John", VisitorType.GUEST);

		TestUtil.testSerialization(state, (result) -> result.arriveAtBuilding(
				1, "Mary", VisitorType.GUEST));
	}

	@Test
	public void testReadObjectFails() {
		GalleryState galleryState = new GalleryState();
		TestUtil.assertReadObjectFails(galleryState);
	}

	/* Serialization White-box Tests */
	@Test(expected = IntegrityViolationException.class)
	public void testMaliciousSerialization() throws Exception {
		testGalleryStateSerialization(createMaliciousGalleryState());
	}

	/* Private helpers */

	private static void testGalleryStateSerialization(GalleryState gallery)
			throws ClassNotFoundException, IOException {
		TestUtil.testSerialization(gallery,
				(result) -> EquivalenceUtil.assertEquivalent(gallery, result));
	}

	private static GalleryState createMaliciousGalleryState() throws Exception {
		GalleryState maliciousObj = new GalleryState();

		Field visitorsField = GalleryState.class.getDeclaredField("visitorMap");
		visitorsField.setAccessible(true);

		@SuppressWarnings("unchecked")
		Map<String, Visitor> visitorMap = (Map<String, Visitor>) visitorsField
				.get(maliciousObj);

		// Duplicate visitor (trick the map by lying about visitor's name)
		visitorMap.put("John", new Visitor("John", VisitorType.EMPLOYEE));
		visitorMap.put("Mike", new Visitor("John", VisitorType.EMPLOYEE));

		return maliciousObj;
	}
}
