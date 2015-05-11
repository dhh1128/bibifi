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

		testSerialization(state);
	}

	private void testSerialization(GalleryState galleryState) throws IOException {
		String password = "secret";
		
		File logFile = File.createTempFile("bibifi-test", "tmp");
		logFile.deleteOnExit();
		
		LogFileWriter writer = new LogFileWriter(logFile);
		writer.write(galleryState, password);
		
		LogFileReader reader = new LogFileReader(logFile);
		GalleryState recoveredGalleryState = reader.read(password);
		
		EquivalenceUtil.assertEquivalent(galleryState, recoveredGalleryState);
	}

}
