package org.builditbreakit.seada.logappend.io;

import java.io.File;
import java.io.IOException;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.data.VisitorType;
import org.builditbreakit.seada.common.io.LogFileReader;

public class IOBenchmark {

	public static void main(String[] args) throws InterruptedException,
			IOException {
		GalleryState state = new GalleryState();
		long time = 1;
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

		Thread.sleep(10000);

		while (true) {
			testSerialization(state);
			Thread.sleep(1000);
		}
	}

	private static void testSerialization(GalleryState galleryState)
			throws IOException {
		final double millisToNanos = 1000000.0;

		final String password = "secret";

		long start = System.nanoTime();
		File logFile = File.createTempFile("bibifi-test", "tmp");
		logFile.deleteOnExit();

		LogFileWriter writer = new LogFileWriter(logFile);
		writer.write(galleryState, password);

		LogFileReader reader = new LogFileReader(logFile);
		reader.read(password);
		double runtime = (System.nanoTime() - start) / millisToNanos;
		long logFileSize = logFile.length();

		System.out.println("Runtime: " + runtime + " ms");
		System.out.println("Log file size: " + logFileSize + " bytes");
		System.out.println();
	}
}
