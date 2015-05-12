package org.builditbreakit.seada.logappend;

import junit.framework.TestCase;

public class AppendCommandTest extends TestCase {

	private static String[] validCommands = {
		"-T 1 -K abc -G Fred -A -R 25 foo",
		"-T 2 -K abc -E fred -A foo", // no room
		"-T 1 -K abc -G Fred -A -R 25 -T 5 foo", // duplicate -T
	};

	private static String[] invalidCommands = {
		"-K abc -G Fred -A -R 25 foo", // missing -T
	};
	
	public void testValid() {
		for (String line: validCommands) {
			try {
				new AppendCommand(BatchProcessor.splitLine(line));
			} catch (Throwable e) {
				fail("Cmdline \"" + line + "\" should have been valid, but wasn't: " + e.toString());
			}
		}
	}

	public void testInvalid() {
		for (String line: invalidCommands) {
			boolean ok = true;
			try {
				new AppendCommand(BatchProcessor.splitLine(line));
				ok = false;
			} catch (Throwable e) {
			}
			if (!ok) {
				fail("Cmdline \"" + line + "\" should not have been valid, but was.");
			}
		}
	}
}
