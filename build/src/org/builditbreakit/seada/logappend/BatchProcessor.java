package org.builditbreakit.seada.logappend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
	
	/**
	 * This logic is super simple and would not be adequate for a robust
	 * cmdline parser that has to handle quoted args, escape sequences,
	 * and so forth. However, it is adequate for the bibifi spec. I tested
	 * this regex split against a scanner; it is faster.
	 */
	//private static final Pattern whitespace = Pattern.compile("\\s+");
	public static String[] splitLine(String line) {
		ArrayList<String> tokens = new ArrayList<>(16);
		StringTokenizer tokenizer = new StringTokenizer(line, " \t");
		while(tokenizer.hasMoreTokens()) {
			tokens.add(tokenizer.nextToken());
		}
		return tokens.toArray(new String[tokens.size()]);
	}
	
	

}
