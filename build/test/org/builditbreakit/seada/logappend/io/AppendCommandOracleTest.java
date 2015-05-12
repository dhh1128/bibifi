package org.builditbreakit.seada.logappend.io;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.builditbreakit.seada.common.TransitionEvent;
import org.builditbreakit.seada.common.data.VisitorType;
import org.builditbreakit.seada.logappend.AppendCommand;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

@Ignore
public class AppendCommandOracleTest {
	private static long NO_ROOM = -1;
	private AppendCommand cmd;

	@Before
	public void before() {
		cmd = new AppendCommand();
	}

	@Test
	public void testEmployeeArriveBuilding() {
		testCommand("-T 1 -K secret -A -E Fred log1");
		assertCommand(1, "secret", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Fred", "log1");
	}

	@Test
	public void testArgsAfterLog() {
		testCommand("-K secret -A log1 -E Fred -T 1");
		assertCommand(1, "secret", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Fred", "log1");
	}
	
	@Test
	public void testDuplicateRoom() {
		testCommand("-T 1 -K secret -R 10 -A -E Fred -R 5 log1");
		assertCommand(1, "secret", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Fred", 5, "log1");
	}

	@Test
	public void testDuplicateTimes() {
		testCommand("-T 1 -T 2 -K secret -A -E Fred log1");
		assertCommand(2, "secret", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Fred", "log1");
	}

	@Test
	public void testTripleTimesInOrder() {
		testCommand("-T 1 -T 2 -K secret -A -E Fred -T 3 log1");
		assertCommand(3, "secret", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Fred", "log1");
	}

	@Test
	public void testTripleTimesOutOfOrder() {
		testCommand("-T 3 -T 1 -K secret -A -E Fred -T 2 log1");
		assertCommand(2, "secret", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Fred", "log1");
	}

	@Test
	public void testTripleDuplicateToken() {
		testCommand("-T 1 -K thing1 -K thing2 -A -E Fred log1");
		assertCommand(1, "thing2", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Fred", "log1");
	}

	@Test(expected = Exception.class)
	public void testMultipleEventsArrivalAndDeparture() {
		testCommand("-T 1 -K secret -A -L -A -E Fred log1");
		assertCommand(1, "secret", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Fred", "log1");
	}

	@Test
	public void testMultipleEventsArrival() {
		testCommand("-T 1 -K secret -A -A -E Fred log1");
		assertCommand(1, "secret", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Fred", "log1");
	}

	@Test
	public void testMultipleEventsDeparture() {
		testCommand("-T 1 -K secret -A -A -E Fred log1");
		assertCommand(1, "secret", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Fred", "log1");
	}

	@Test
	public void testArrivalLast() {
		testCommand("-T 1 -K secret log1 -E Fred -A");
		assertCommand(1, "secret", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Fred", "log1");
	}
	
	@Test(expected = Exception.class)
	public void testIncompleteCommand() {
		testCommand("-T 1 -K secret log1 -E Fred");
	}
	
	@Test(expected = Exception.class)
	public void testBadArg() {
		testCommand("-T 1 -K secret log1 -Z -A -E Fred");
	}
	
	@Test(expected = Exception.class)
	public void testMultipeLogs() {
		testCommand("-T 1 -K secret log1 -A  log2 -E Fred");
	}
	
	@Test(expected = Exception.class)
	public void testMultipeLogsAtEnd() {
		testCommand("-T 1 -K secret -A -E Fred log1 log2");
	}

	@Test
	public void testDepartureLast() {
		testCommand("-T 1 -K secret log1 -E Fred -L");
		assertCommand(1, "secret", TransitionEvent.DEPARTURE,
				VisitorType.EMPLOYEE, "Fred", "log1");
	}

	public void testTwoEmployees() {
		testCommand("-T 1 -K secret -A -E Fred -E Jill log1");
		assertCommand(1, "secret", TransitionEvent.ARRIVAL,
				VisitorType.EMPLOYEE, "Jill", "log1");
	}

	@Test(expected = Exception.class)
	public void testOneEmployeeAndOneGuest() {
		testCommand("-T 1 -K secret -A -E Fred -G Jill log1");
	}

	@Test(expected = Exception.class)
	public void testOneGuestAndOneEmployee() {
		testCommand("-T 1 -K secret -A -G Fred -E Jill log1");
	}

	@Test
	public void testTwoGuests() {
		testCommand("-T 1 -K secret -A -G Fred -G Jill log1");
		assertCommand(1, "secret", TransitionEvent.ARRIVAL, VisitorType.GUEST,
				"Jill", "log1");
	}

	@Test(expected = Exception.class)
	public void testDuplicateKeySwitch() {
		testCommand("-T 1 -K -K thing2 -A -E Fred log1");
	}

	@Test(expected = Exception.class)
	public void testBadTime() {
		testCommand("-T a -K secret -A -E Fred log1");
	}

	@Test(expected = Exception.class)
	public void testDuplicateTimesWithOneInvalid1() {
		testCommand("-T a -T 1 -K secret -A -E Fred log1");
	}

	@Test(expected = Exception.class)
	public void testDuplicateTimesWithOneInvalid2() {
		testCommand("-T 1 -T a -K secret -A -E Fred log1");
	}

	@Test
	public void fuzzyArgumentOrderTest() {
		final Random rand = new Random();
		boolean useRoom = rand.nextBoolean();
		long room = (useRoom) ? 101 : NO_ROOM;
		
		List<String> args = new LinkedList<>(Arrays.asList("-T 1", "-K secret",
				"-A", "-E Fred", "log1"));
		if (useRoom) {
			args.add("-R " + room);
		}
		Collections.shuffle(args);

		StringBuilder builder = new StringBuilder();
		args.forEach((arg) -> builder.append(arg).append(" "));
		String commandString = builder.toString();

		try {
			testCommand(commandString);
			assertCommand(commandString, 1, "secret", TransitionEvent.ARRIVAL,
					VisitorType.EMPLOYEE, "Fred", room, "log1");
		} catch (AssertionError | RuntimeException e) {
			System.out.println("Fail on " + commandString);
			throw e;
		}
	}

	private void assertCommand(String message, long expectedTime,
			String expectedToken, TransitionEvent expectedEvent,
			VisitorType expectedVisitorType, String expectedName,
			String expectedLogFile) {
		assertCommand(message, expectedTime, expectedToken, expectedEvent,
				expectedVisitorType, expectedName, NO_ROOM, expectedLogFile);
	}

	private void assertCommand(String message, long expectedTime,
			String expectedToken, TransitionEvent expectedEvent,
			VisitorType expectedVisitorType, String expectedName,
			long expectedRoom, String expectedLogFile) {
		if (message != null && !message.isEmpty()) {
			message += " | ";
		} else {
			message = "";
		}

		assertEquals(message + "Time", expectedTime, cmd.getTimestamp());
		assertEquals(message + "Token", expectedToken, cmd.getToken());
		assertEquals(message + "Event", expectedEvent, cmd.getEvent());
		assertEquals(message + "Visitor Type", expectedVisitorType,
				cmd.getVisitorType());
		assertEquals(message + "Visitor Name", expectedName,
				cmd.getVisitorName());
		assertEquals(message + "Room", expectedRoom, cmd.getRoom());
		assertEquals(message + "Log File", expectedLogFile, cmd.getLogfile());
	}

	private void assertCommand(long expectedTime, String expectedToken,
			TransitionEvent expectedEvent, VisitorType expectedVisitorType,
			String expectedName, String expectedLogFile) {
		assertCommand(null, expectedTime, expectedToken, expectedEvent,
				expectedVisitorType, expectedName, expectedLogFile);
	}
	
	private void assertCommand(long expectedTime, String expectedToken,
			TransitionEvent expectedEvent, VisitorType expectedVisitorType,
			String expectedName, long room, String expectedLogFile) {
		assertCommand(null, expectedTime, expectedToken, expectedEvent,
				expectedVisitorType, expectedName, room, expectedLogFile);
	}

	private void testCommand(String command) {
		cmd.parse(tokenize(command));
	}

	private static String[] tokenize(String str) {
		return str.split("\\s+");
	}
}
