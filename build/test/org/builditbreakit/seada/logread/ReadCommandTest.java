package org.builditbreakit.seada.logread;

import junit.framework.TestCase;
import org.builditbreakit.seada.logappend.BatchProcessor;

public class ReadCommandTest extends TestCase {

	private static String[] validCommands = {
		"-K secret -S logfile",
		"-K secret -R -E fred logfile",
		"-K secret -T -E fred logfile",
		"-K secret -I -E fred -G barney logfile",
	};

	private static String[] invalidCommands = {
		"-K secret -S", // no logfile
		"-K secret -X -E fred logfile", // bad style
		"-K secret -T -E fr0ed logfile", // bad guestname
		"-K secret -I logfile", // no visitors
	};
	
	public void testValid() {
		for (String line: validCommands) {
			try {
				ReadCommand cmd = new ReadCommand();
				cmd.parse(BatchProcessor.splitLine(line));
			} catch (Throwable e) {
				fail("Cmdline \"" + line + "\" should have been valid, but wasn't: " + e.toString());
			}
		}
	}

	public void testInvalid() {
		for (String line: invalidCommands) {
			boolean ok = true;
			try {
				ReadCommand cmd = new ReadCommand();
				cmd.parse(BatchProcessor.splitLine(line));
				ok = false;
			} catch (Throwable e) {
			}
			if (!ok) {
				fail("Cmdline \"" + line + "\" should not have been valid, but was.");
			}
		}
	}
}
