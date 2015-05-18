package org.builditbreakit.seada.logappend.io;

import java.io.File;
import java.io.IOException;

import org.builditbreakit.seada.common.data.EquivalenceUtil;
import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.data.VisitorType;
import org.builditbreakit.seada.common.io.LogFileReader;
import org.junit.Test;

public class LogFileWriterTest {
	@Test
	public void testEmptyGalleryStateSerialization() throws IOException,
			ClassNotFoundException {
		testSerialization(new GalleryState());
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

		testSerialization(state);
	}

	private void testSerialization(GalleryState galleryState) throws IOException {
		//printEntropySource();
		final double millisToNanos = 1000000.0;
		
		String password = "secret";
		
		long start = System.nanoTime();
		File logFile = File.createTempFile("bibifi-test", "tmp");
		logFile.deleteOnExit();
		
		LogFileWriter writer = new LogFileWriter(logFile);
		writer.write(galleryState, password);
		
		LogFileReader reader = new LogFileReader(logFile);
		GalleryState recoveredGalleryState = reader.read(password);
		double runtime = (System.nanoTime() - start) / millisToNanos;
		long logFileSize = logFile.length();
		
		EquivalenceUtil.assertEquivalent(galleryState, recoveredGalleryState);
		
		System.out.println("Runtime: " + runtime + " ms");
		System.out.println("Log file size: " + logFileSize + " bytes");
		System.out.println();
	}

	@SuppressWarnings("unused")
	private static void printEntropySource() {
		System.out.println(System.getProperty("java.security.egd"));
	}
}
