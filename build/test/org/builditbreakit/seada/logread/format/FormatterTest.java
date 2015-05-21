package org.builditbreakit.seada.logread.format;

import static org.junit.Assert.*;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.data.VisitorType;
import org.junit.BeforeClass;
import org.junit.Test;

public class FormatterTest {
	private static GalleryState state;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		state = new GalleryState();
		int time = 1;
		state.arriveAtBuilding(time++, "Jake", VisitorType.GUEST);
		state.arriveAtRoom(time++, "Jake", VisitorType.GUEST, 102);
		state.arriveAtBuilding(time++, "Bob", VisitorType.EMPLOYEE);
		state.arriveAtRoom(time++, "Bob", VisitorType.EMPLOYEE, 102);
		state.departRoom(time++, "Bob", VisitorType.EMPLOYEE, 102);
		state.arriveAtRoom(time++, "Bob", VisitorType.EMPLOYEE, 101);
		state.arriveAtBuilding(time++, "Jill", VisitorType.EMPLOYEE);
		state.arriveAtBuilding(time++, "Alice", VisitorType.EMPLOYEE);
		state.arriveAtRoom(time++, "Jill", VisitorType.EMPLOYEE, 102);
		state.departRoom(time++, "Jill", VisitorType.EMPLOYEE, 102);
		state.arriveAtRoom(time++, "Jill", VisitorType.EMPLOYEE, 101);
		state.departRoom(time++, "Bob", VisitorType.EMPLOYEE, 101);
		state.departBuilding(time++, "Bob", VisitorType.EMPLOYEE);
		state.arriveAtBuilding(time++, "John", VisitorType.GUEST);
		state.departBuilding(time++, "John", VisitorType.GUEST);
		state.arriveAtBuilding(time++, "John", VisitorType.GUEST);
		state.arriveAtBuilding(time++, "Bob", VisitorType.EMPLOYEE);
		state.arriveAtRoom(time++, "Bob", VisitorType.EMPLOYEE, 102);
	}

	@Test
	public void testStateOutput() {
		String expected = "Alice,Bob,Jill\nJake,John\n101:Jill\n102:Bob,Jake";
		
		Formatter formatter = new StateFormatter(state);
		String actual = formatter.format();
		
		assertEquals(expected, actual);
	}

	@Test
	public void testRoomOutput() {
		String expected = "102,101,102";
		
		Formatter formatter = new VisitorRoomFormatter(state.getVisitor("Bob",
				VisitorType.EMPLOYEE));
		String actual = formatter.format();

		assertEquals(expected, actual);
	}

	@Test
	public void testTimeOutput() {
		String expected = "11\n";
		
		Formatter formatter = new VisitorTimeFormatter(state, state.getVisitor(
				"Bob", VisitorType.EMPLOYEE));
		String actual = formatter.format();

		assertEquals(expected, actual);
	}

	@Test
	public void testTimeOutputWithUnrelatedEvents() {
		String expected = "3\n";
		
		Formatter formatter = new VisitorTimeFormatter(state, state.getVisitor(
				"John", VisitorType.GUEST));
		String actual = formatter.format();

		assertEquals(expected, actual);
	}

	@Test
	public void testConcurrentRoomOutputOneVisitor() {
		String expected = "101,102";
		
		ConcurrentVisitorsFormatter formatter = new ConcurrentVisitorsFormatter(state);
		formatter.addVisitor("Bob", VisitorType.EMPLOYEE);
		String actual = formatter.format();

		assertEquals(expected, actual);
	}

	@Test
	public void testConcurrentRoomOutputTwoVisitorsMatching() {
		String expected = "102";
		
		ConcurrentVisitorsFormatter formatter = new ConcurrentVisitorsFormatter(state);
		formatter.addVisitor("Bob", VisitorType.EMPLOYEE);
		formatter.addVisitor("Jake", VisitorType.GUEST);
		String actual = formatter.format();

		assertEquals(expected, actual);
	}

	@Test
	public void testConcurrentRoomOutputThreeVisitorsNotMatching() {
		String expected = "";
		
		ConcurrentVisitorsFormatter formatter = new ConcurrentVisitorsFormatter(state);
		formatter.addVisitor("Bob", VisitorType.EMPLOYEE);
		formatter.addVisitor("Jake", VisitorType.GUEST);
		formatter.addVisitor("Jill", VisitorType.EMPLOYEE);
		String actual = formatter.format();

		assertEquals(expected, actual);
	}

	@Test
	public void testConcurrentRoomOutputTwoVisitorsNotMatching() {
		String expected = "101";
		
		ConcurrentVisitorsFormatter formatter = new ConcurrentVisitorsFormatter(state);
		formatter.addVisitor("Bob", VisitorType.EMPLOYEE);
		formatter.addVisitor("Jill", VisitorType.EMPLOYEE);
		String actual = formatter.format();

		assertEquals(expected, actual);
	}
}
