package org.builditbreakit.seada.logappend;

import java.io.IOException;

public class App {
	
	private static void applyCommand(String[] args, GalleryUpdateManager gum) throws IOException {
		// This line with throw if cmdline syntax is bad.
		AppendCommand cmd = new AppendCommand(args);
		
		// This line will throw if token is bad or logfile is corrupt/uncreatable.
		GalleryUpdate item = gum.getGalleryFor(cmd);
		
		// This switch statement should throw if a guest name is used as an employee name,
		// a timestamp isn't >= previous value, etc.
		switch (cmd.getEvent()) {
		case ARRIVAL:
			if (cmd.getRoom() == -1) {
				item.state.arriveAtBuilding(cmd.getTimestamp(), cmd.getVisitorName(), cmd.getVisitorType());
			} else {
				item.state.arriveAtRoom(cmd.getTimestamp(), cmd.getVisitorName(), cmd.getVisitorType(), cmd.getRoom());
			}
			break;
		case DEPARTURE:
			if (cmd.getRoom() == -1) {
				item.state.departBuilding(cmd.getTimestamp(), cmd.getVisitorName(), cmd.getVisitorType());
			} else {
				item.state.departRoom(cmd.getTimestamp(), cmd.getVisitorName(), cmd.getVisitorType(), cmd.getRoom());
			}
			break;
		default:
			throw new IllegalArgumentException("bad event type (neither arrival nor departure)");
		}
		
		item.modified = true;
	}
	
	/**
	 * @return Number of commands that modified model.
	 */
	private static void processBatch(GalleryUpdateManager gum, String batchFilePath) throws IOException {
		
		BatchProcessor b = new BatchProcessor(batchFilePath);
		while (b.hasNextLine()) {
			try {
				applyCommand(b.nextLine(), gum);				
			} catch (Throwable e) {
				System.out.println("invalid");
			}
		}
	}
	
	public static void main(String[] args) {
		int exitCodeForErrors = 255;
		try {
			
			GalleryUpdateManager gum = new GalleryUpdateManager();
			
			if (args.length == 2 && args[0].equals("-B")) {
				exitCodeForErrors = 0;
				processBatch(gum, args[1]);
				
			} else {
				applyCommand(args, gum);
			}
			
			gum.save();
			
		} catch (Throwable e) {
			System.out.println("invalid");
			System.exit(exitCodeForErrors);
		}
	}
}
