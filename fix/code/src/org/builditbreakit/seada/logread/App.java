package org.builditbreakit.seada.logread;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.data.Visitor;
import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;
import org.builditbreakit.seada.common.io.LogFileReader;
import org.builditbreakit.seada.logread.ReadCommand;
import org.builditbreakit.seada.logread.ReadCommand.VisitorSpec;
import org.builditbreakit.seada.logread.format.ConcurrentVisitorsFormatter;
import org.builditbreakit.seada.logread.format.Formatter;
import org.builditbreakit.seada.logread.format.StateFormatter;
import org.builditbreakit.seada.logread.format.VisitorRoomFormatter;
import org.builditbreakit.seada.logread.format.VisitorTimeFormatter;

public class App {
	
	public static void main(String[] args) {
		
		try {
			ReadCommand cmd = new ReadCommand();
			cmd.parse(args);

			File file = new File(cmd.getLogfile());
			if (cmd.getLogfile().isEmpty() || !file.exists()) {
				switch (cmd.getStyle()) {
				case DUMP_CURRENT_STATE:
					harness.println();
					harness.println();
					break;
				case TOTAL_TIME:
					harness.println(0);
					break;
				default:
					break;
				}
				harness.exit(0);
			}

			GalleryState state = harness.loadFile(file, cmd.getToken());

			Formatter formatter;
			switch (cmd.getStyle()) {
			case DUMP_CURRENT_STATE:
				formatter = new StateFormatter(state);
				break;
			case DUMP_ENTERED_ROOMS:
				Visitor roomVisitor = extractSingleVisitor(cmd, state);
				if (roomVisitor == null) {
					System.exit(0);
				}
				formatter = new VisitorRoomFormatter(roomVisitor);
				break;
			case TOTAL_TIME:
				Visitor timeVisitor = extractSingleVisitor(cmd, state);
				if (timeVisitor == null) {
					harness.println(0);
					harness.exit(0);
				}
				formatter = new VisitorTimeFormatter(state, timeVisitor);
				break;
			case ROOMS_OCCUPIED_TOGETHER:
				ConcurrentVisitorsFormatter occupedRoomsFrmt = new ConcurrentVisitorsFormatter(
						state);
				List<VisitorSpec> visitors = cmd.getVisitors();
				for (VisitorSpec visitor : visitors) {
					occupedRoomsFrmt.addVisitor(visitor.name, visitor.type);
				}
				formatter = occupedRoomsFrmt;
				break;
			default:
				throw new IllegalArgumentException("Bad style");
			}

			harness.print(formatter.format());
			harness.exit(0);
			
		} catch (IntegrityViolationException | SecurityException e) {
			harness.println("integrity violation");
			harness.exit(255);
			
		} catch (Harness.ExitException e) {
			// do nothing. This code path only happens in unit tests.
			
		} catch (Throwable e) {
			harness.println("invalid");
			harness.exit(255);
		}
	}
	
	/**
	 * I encapsulated these OS calls into a class so I can override them
	 * in a test.
	 */
	static class Harness {
		@SuppressWarnings("serial")
		static class ExitException extends RuntimeException {}
		
		GalleryState loadFile(File f, String password) throws IOException {
			LogFileReader reader = new LogFileReader(f);
			return reader.read(password);
		}
	
		void exit(int code) {
			System.exit(code);
		}
		
		void println() {
			System.out.println();
		}
		
		void println(String msg) {
			System.out.println(msg);
		}
		
		void println(int n) {
			System.out.println(n);
		}
		
		void print(String txt) {
			System.out.print(txt);
		}
	}
	
	static Harness harness = new Harness();
	
	private static Visitor extractSingleVisitor(ReadCommand cmd,
			GalleryState state) {
		List<VisitorSpec> visitors = cmd.getVisitors();
		if (visitors.size() != 1) {
			throw new IllegalArgumentException("Too many visitors");
		}
		VisitorSpec spec = visitors.iterator().next();
		if(!state.containsVisitor(spec.name, spec.type)) {
			return null;
		}
		return state.getVisitor(spec.name, spec.type);
	}
}
