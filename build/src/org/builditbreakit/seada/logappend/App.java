package org.builditbreakit.seada.logappend;

import java.io.IOException;

public class App {
	
	/**
	 * @return Number of commands that modified model.
	 */
	private static int processBatch(String batchFilePath) throws IOException {
		int validCmdCount = 0;
		BatchProcessor b = new BatchProcessor(batchFilePath);
		while (b.hasNextLine()) {
			try {
				AppendCommand cmd = new AppendCommand();
				cmd.parse(b.nextLine());
				// TODO: Update the model with this cmd
				++validCmdCount;
			} catch (Throwable e) {
				System.out.println("invalid");
			}
		}
		return validCmdCount;
	}
	
	public static void main(String[] args) {
		int exitCodeForErrors = 255;
		boolean persistModel = false;
		try {
			if (args.length == 2 && args[0].equals("-B")) {
				exitCodeForErrors = 0;
				if (processBatch(args[1]) > 0) {
					persistModel = true;
				}
			} else {
				AppendCommand cmd = new AppendCommand();
				// TODO: Read persisted model. We do it here instead of earlier,
				// because there's no point in doing that work if cmd is bad.
				cmd.parse(args);
				// TODO: Update the model with this cmd.
				persistModel = true;
			}
			if (persistModel) {
				// TODO: Persist the model.
			}
		} catch (Throwable e) {
			System.out.println("invalid");
			System.exit(exitCodeForErrors);
		}
	}
}
