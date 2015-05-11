package org.builditbreakit.seada.logappend;

import junit.framework.TestCase;

public class AppendCommandTest extends TestCase {

	private static String[] validCommands = {
		"-T 1 -K abc -G Fred -A -R 25 foo",
	};

	private static String[] invalidCommands = {
		"-K abc -G Fred -A -R 25 foo", // missing -T
		"-T 1 -K abc -G Fred -A -R 25 -T 5 foo", // duplicate -T
	};
	
	public void testValid() {
		for (String line: validCommands) {
			try {
				AppendCommand cmd = new AppendCommand();
				cmd.parse(BatchProcessor.splitLine(line));
			} catch (Throwable e) {
				fail("Cmdline \"" + line + "\" should have been valid, but wasn't: " + e.toString());
			}
		}
	}

	public void testInvalid() {
		for (String line: invalidCommands) {
			try {
				AppendCommand cmd = new AppendCommand();
				cmd.parse(BatchProcessor.splitLine(line));
				fail("Cmdline \"" + line + "\" should not have been valid, but was.");
			} catch (Throwable e) {
			}
		}
	}
}
