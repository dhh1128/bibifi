package org.builditbreakit.seada.logappend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class App {
	
	private static void applyCommand(String[] args, GalleryUpdateManager gum) throws IOException, SecurityException {
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
			String[] args = b.nextLine();
			try {
				applyCommand(args, gum);
			} catch (SecurityException e) {
				logError(e, args);
				System.out.println("integrity violation");
			} catch (Throwable e) {
				logError(e, args);
				System.out.println("invalid");
			}
		}
	}
	
	private static final boolean DEBUG_ERRORS = true; // disable before final build
	
	private static void logError(Throwable e, String[] args) {
		if (DEBUG_ERRORS) {
			try {
				File tempFile = File.createTempFile("seada-logappend", ".tmp");
				FileOutputStream fout = new FileOutputStream(tempFile);
				PrintWriter out = new PrintWriter(fout);
				out.print("Error with");
				for (String arg: args) {
					out.print(' ');
					out.print(arg);
				}
				out.println("\n");
				e.printStackTrace(out);
				out.println("\nclasspath = " + java.lang.System.getProperty("java.class.path"));
				out.close();
				fout.close();
			} catch (IOException e1) {
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
			System.exit(0);
			
		} catch (SecurityException e) {
			logError(e, args);
			System.out.println("integrity violation");
			System.exit(exitCodeForErrors);
		} catch (Throwable e) {
			logError(e, args);
			System.out.println("invalid");
			System.exit(exitCodeForErrors);
		}
	}
}
