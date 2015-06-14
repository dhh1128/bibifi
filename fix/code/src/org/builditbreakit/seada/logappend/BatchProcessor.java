package org.builditbreakit.seada.logappend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BatchProcessor {
	
	BufferedReader reader;
	
	public BatchProcessor(String path) throws IOException {
		reader = new BufferedReader(new FileReader(path));
	}
	
	public String[] nextLine() throws IOException {
		String line = reader.readLine();
		if(line == null) {
			return null;
		}
		return splitLine(line);
	}
	
	private static boolean isSplitChar(char c) {
		return (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f' || c == '\13');
	}

	/**
	 * This logic is super simple and would not be adequate for a robust
	 * cmdline parser that has to handle quoted args, escape sequences,
	 * and so forth. However, it is adequate for the bibifi spec. I tested
	 * this regex split against a scanner; it is faster.
	 */
	public static String[] splitLine(String line) {
		ArrayList<String> tokens = new ArrayList<>(16);
		int length = line.length();
		int offset = 0;
		// Consume leading whitespace.
		while (isSplitChar(line.charAt(offset)) && offset < length) {
			++offset;
		}
		for (int i = offset; i < length; i++) {
			char c = line.charAt(i);
			// Check whitespace characters in regex set: [\s]
			if (isSplitChar(c) && (offset != i)) {
				tokens.add(line.substring(offset, i));
				offset = i + 1;
			}
		}
		if (offset != length) {
			tokens.add(line.substring(offset, length));
		}

		return tokens.toArray(new String[tokens.size()]);
	}
}
