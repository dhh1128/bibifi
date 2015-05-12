package org.builditbreakit.seada.logread;

import org.builditbreakit.seada.logread.ReadCommand;

public class App {
	public static void main(String[] args) {
		try {
			ReadCommand cmd = new ReadCommand();
			// TODO: Read persisted model. We do it here instead of earlier,
			// because there's no point in doing that work if cmd is bad.
			cmd.parse(args);
			// TODO: Query the model with this cmd.
			
			System.exit(0);
		} catch (SecurityException e) {
			System.out.println("integrity violation");
			System.exit(255);
		} catch (Throwable e) {
			System.out.println("invalid");
			System.exit(255);
		}
	}
}
