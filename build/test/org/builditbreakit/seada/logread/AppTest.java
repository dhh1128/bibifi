package org.builditbreakit.seada.logread;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import junit.framework.TestCase;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.data.VisitorType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AppTest extends TestCase {
	
	private static App.Harness oldTTY;
	private MockHarness harness;
	private static final File existingLogFile;
	
	static {
		File f = null;
		try {
			f = Files.createTempFile("seadafakelogfile", ".tmp").toFile();
			f.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
		existingLogFile = f;
	}
	
	@Before
	public void setUp() {
		oldTTY = App.harness;
		harness = new MockHarness();
		App.harness = harness;
	}
	
	@After
	public void tearDown() {
		App.harness = oldTTY;
	}
	
	private void runCommand(String line) {
		App.main(line.split("\\s+"));
	}
	
	@Test
	public void test_DUMP_CURRENT_STATE_with_nonexistent_file() {
		runCommand("-K secret -S logThatDoesntExist");
		assertEquals(0, harness.exitCode);
		assertEquals("\n\n", harness.stdout.toString());
	}
	
	@Test
	public void test_TOTAL_TIME_with_nonexistent_file() {
		runCommand("-K secret -T -E Alice logThatDoesntExist");
		assertEquals(0, harness.exitCode);
		assertEquals("0", harness.stdout.toString().trim());
	}
	
	@Test
	public void test_DUMP_ENTERED_ROOMS_with_nonexistent_file() {
		runCommand("-K secret -R -E Alice logThatDoesntExist");
		assertEquals(0, harness.exitCode);
		assertEquals("", harness.stdout.toString().trim());
	}
	
	@Test
	public void test_ROOMS_OCCUPIED_TOGETHER_with_nonexistent_file() {
		runCommand("-K secret -I -E Alice -G Bob logThatDoesntExist");
		assertEquals(0, harness.exitCode);
		assertEquals("", harness.stdout.toString().trim());
	}
	
	@Test
	public void testIntegrityViolation() {
		harness.pretendFileIsCorrupt = true;
		runCommand("-K secret -I -E Alice -G Bob " + osIndependentPath(existingLogFile));
		assertEquals(255, harness.exitCode);
		assertEquals("integrity violation", harness.stdout.toString().trim());
	}

	@Test
	public void test_DUMP_CURRENT_STATE_15() {
		harness.fillGallery(15);
		runCommand("-K secret -S " + osIndependentPath(existingLogFile));
		assertEquals(0, harness.exitCode);
		assertEquals("Alice\nBob,Fred,Mary\n\n204:Mary\n400:Alice", harness.stdout.toString());
	}

	@Test
	public void test_DUMP_CURRENT_STATE_30() {
		harness.fillGallery(30);
		runCommand("-K secret -S " + osIndependentPath(existingLogFile));
		assertEquals(0, harness.exitCode);
		assertEquals("Alice\nFred,Mary\n\n204:Mary\n301:Alice", harness.stdout.toString());
	}

	@Test
	public void test_TOTAL_TIME_Bob_at_20() {
		harness.fillGallery(20);
		runCommand("-K secret -T -G Bob " + osIndependentPath(existingLogFile));
		assertEquals(0, harness.exitCode);
		assertEquals("15", harness.stdout.toString().trim());
	}
	
	@Test
	public void test_ENTERED_ROOMS_Bob_at_25() {
		harness.fillGallery(25);
		runCommand("-K secret -R -G Bob " + osIndependentPath(existingLogFile));
		assertEquals(0, harness.exitCode);
		assertEquals("101,102,103,301", harness.stdout.toString().trim());
	}
	
	@Test
	public void test_ROOMS_OCCUPIED_TOGETHER_Bob_and_Alice() {
		harness.fillGallery(30);
		runCommand("-K secret -I -G Bob -E Alice " + osIndependentPath(existingLogFile));
		assertEquals(0, harness.exitCode);
		assertEquals("301", harness.stdout.toString().trim());
	}
	
	@Test
	public void test_ROOMS_OCCUPIED_TOGETHER_Bob_and_Mary() {
		harness.fillGallery(30);
		runCommand("-K secret -I -G Bob -G Mary " + osIndependentPath(existingLogFile));
		assertEquals(0, harness.exitCode);
		assertEquals("", harness.stdout.toString().trim());
	}
	
	private String osIndependentPath(File file) {
		String osPath = file.getPath();
		if(osPath.contains("\\")) {
			osPath = osPath.replaceAll("\\\\", "/");
			osPath = osPath.replaceAll("[A-Z]:", "");
		}
		return osPath;
	}
	
	static class MockHarness extends App.Harness {
		
		StringBuilder stdout = new StringBuilder();
		int exitCode = -1;
		GalleryState state;
		boolean pretendFileIsCorrupt = false;
		
		void fillGallery(int upToSecond) {
			GalleryState s = new GalleryState();
			
			VisitorType G = VisitorType.GUEST;
			VisitorType E = VisitorType.EMPLOYEE;
			
			if (upToSecond >= 1) s.arriveAtBuilding(1, "Bob", G);
			if (upToSecond >= 2) s.arriveAtBuilding(2, "Alice", E);
			if (upToSecond >= 3) s.arriveAtRoom(3, "Bob", G, 101);
			if (upToSecond >= 4) s.arriveAtRoom(4, "Alice", E, 400);
			if (upToSecond >= 5) s.arriveAtBuilding(5, "Mary", G);
			if (upToSecond >= 6) s.departRoom(6, "Bob", G, 101);
			if (upToSecond >= 7) s.arriveAtRoom(7, "Bob", G, 102);
			if (upToSecond >= 8) s.departRoom(8, "Bob", G, 102);
			if (upToSecond >= 9) s.arriveAtBuilding(9, "Fred", G);
			if (upToSecond >= 10) s.arriveAtRoom(10, "Mary", G,  204);
			if (upToSecond >= 16) s.arriveAtRoom(16, "Bob", G, 103);
			if (upToSecond >= 18) s.departRoom(18, "Alice", E, 400);
			if (upToSecond >= 19) s.arriveAtRoom(19, "Alice", E, 301);
			if (upToSecond >= 23) s.departRoom(23, "Bob", G, 103);
			if (upToSecond >= 24) s.arriveAtRoom(24, "Bob", G, 301);
			if (upToSecond >= 25) s.departRoom(25, "Bob", G, 301);
			if (upToSecond >= 26) s.departBuilding(26, "Bob", G);
			state = s;
		}
		
		@Override
		GalleryState loadFile(File f, String password) throws IOException {
			if (pretendFileIsCorrupt) {
				throw new SecurityException();
			}
			return state;
		}

		@Override
		void exit(int code) {
			exitCode = code;
			if (code == 0) {
				// Force the flow of App.main() to exit immediately, even
				// though we didn't call System.exit().
				throw new App.Harness.ExitException();
			}
		}
		
		@Override
		void println() {
			stdout.append('\n');
		}
		
		@Override
		void println(String msg) {
			stdout.append(msg);
			stdout.append('\n');
		}
		
		@Override
		void println(int n) {
			stdout.append(n);
			stdout.append('\n');
		}
		
		@Override
		void print(String txt) {
			stdout.append(txt);
		}
		
	}
}
