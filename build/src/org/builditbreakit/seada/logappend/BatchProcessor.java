package org.builditbreakit.seada.logappend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BatchProcessor {
	
	Scanner scanner;
	
	public BatchProcessor(String path) throws IOException {
		scanner = new Scanner(new File(path), "ASCII");
	}
	
	public boolean hasNextLine() {
		return scanner.hasNextLine();
	}
	
	public String[] nextLine() {
		String line = scanner.nextLine();
		return splitLine(line);
	}
	
	/**
	 * This logic is super simple and would not be adequate for a robust
	 * cmdline parser that has to handle quoted args, escape sequences,
	 * and so forth. However, it is adequate for the bibifi spec.
	 */
	public static String[] splitLine(String line) {
		List<String> items = new ArrayList<String>();
		if (line != null && !line.isEmpty()) {
			Scanner scanner = new Scanner(line);
			try {
				while (scanner.hasNext()) {
					String arg = scanner.next();
					items.add(arg);
				}
			} finally {
				scanner.close();
			}
		}
		return items.toArray(new String[items.size()]);
	}
	
	

}
