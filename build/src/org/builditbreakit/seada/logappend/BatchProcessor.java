package org.builditbreakit.seada.logappend;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

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
	 * and so forth. However, it is adequate for the bibifi spec. I tested
	 * this regex split against a scanner; it is faster.
	 */
	private static final Pattern whitespace = Pattern.compile("\\s+");
	public static String[] splitLine(String line) {
		return whitespace.split(line);
	}
	
	

}
