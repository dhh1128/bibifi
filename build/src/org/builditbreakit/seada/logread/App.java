package org.builditbreakit.seada.logread;

import java.io.File;
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
			if (!file.exists()) {
				switch (cmd.getStyle()) {
				case DUMP_CURRENT_STATE:
					System.out.println();
					System.out.println();
					break;
				case TOTAL_TIME:
					System.out.println(0);
					break;
				default:
					break;
				}
				System.exit(0);
			}

			LogFileReader reader = new LogFileReader(file);
			GalleryState state = reader.read(cmd.getToken());

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
					System.out.println(0);
					System.exit(0);
				}
				formatter = new VisitorTimeFormatter(timeVisitor);
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

			System.out.print(formatter.format());
			System.out.flush();

			System.exit(0);
		} catch (IntegrityViolationException | SecurityException e) {
			System.out.println("integrity violation");
			System.exit(255);
		} catch (Throwable e) {
			System.out.println("invalid");
			System.exit(255);
		}
	}

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
