package org.builditbreakit.seada.logappend;

public class App {
	public static void main(String[] args) {
		// Super dumb code, just to provide a slightly interesting stub. Rip and replace.
		for (String arg: args) {
			if (arg.equals("-B")) {
				return;
			}
		}
		System.out.println("invalid");
		System.exit(255);
	}
}
