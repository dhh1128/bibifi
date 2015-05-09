package org.builditbreakit.seada.logread;

public class App {
	public static void main(String[] args) {
		// Super dumb code, just to provide a slightly interesting stub. Rip and replace.
		for (String arg: args) {
			if (arg.equals("-T")) {
				return;
			}
		}
		System.out.println("invalid");
		System.exit(255);
	}
}
