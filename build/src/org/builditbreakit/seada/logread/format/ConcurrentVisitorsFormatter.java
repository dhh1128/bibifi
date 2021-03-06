package org.builditbreakit.seada.logread.format;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.builditbreakit.seada.common.TransitionEvent;
import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.data.Location;
import org.builditbreakit.seada.common.data.LocationRecord;
import org.builditbreakit.seada.common.data.ValidationUtil;
import org.builditbreakit.seada.common.data.Visitor;
import org.builditbreakit.seada.common.data.VisitorType;
import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;
import org.builditbreakit.seada.common.exceptions.MissingVisitorException;

public class ConcurrentVisitorsFormatter implements Formatter {
	private boolean printNothing = false;
	private final GalleryState state;
	private final Set<Visitor> visitors = new HashSet<>();

	public ConcurrentVisitorsFormatter(GalleryState state) {
		ValidationUtil.assertNotNull(state, "Gallery State");
		this.state = state;
	}

	public void addVisitor(String name, VisitorType type) {
		try {
			visitors.add(state.getVisitor(name, type));
		} catch (MissingVisitorException e) {
			printNothing = true;
		}
	}

	@Override
	public String format() {
		if(printNothing) {
			return "";
		}
		
		Map<Integer, SortedSet<VisitorRecord>> roomIndex = buildIndex(visitors);

		SortedSet<Integer> results = new TreeSet<>();
		for (Entry<Integer, SortedSet<VisitorRecord>> entry : roomIndex.entrySet()) {
			if (allVisitorsInRoom(entry.getValue())) {
				results.add(entry.getKey());
			}
		}

		return FormatUtil.join(results, FormatUtil.COMMA);
	}

	private boolean allVisitorsInRoom(SortedSet<VisitorRecord> roomVisists) {
		Set<Visitor> currentlyInRoom = new HashSet<>();
		for (VisitorRecord record : roomVisists) {
			TransitionEvent event = record.getEvent();
			if (event == TransitionEvent.ARRIVAL) {
				currentlyInRoom.add(record.getVisitor());
				if (currentlyInRoom.containsAll(visitors)) {
					return true;
				}
			} else if (event == TransitionEvent.DEPARTURE) {
				currentlyInRoom.remove(record.getVisitor());
			} else {
				throw new IntegrityViolationException("Unepxected event type: "
						+ event);
			}
		}
		return false;
	}

	private static Map<Integer, SortedSet<VisitorRecord>> buildIndex(
			Set<Visitor> visitors) {
		Map<Integer, SortedSet<VisitorRecord>> roomIndex = new HashMap<>();
		for (Visitor visitor : visitors) {
			List<LocationRecord> history = visitor.getHistory();

			Location lastLocation = Location.OFF_PREMISES;
			for (LocationRecord record : history) {
				Location currentLocation = record.getLocation();

				if (currentLocation.isInRoom()) {
					addVisitRecord(roomIndex, currentLocation.getRoomNumber(),
							record.getArrivalTime(), visitor,
							TransitionEvent.ARRIVAL);
				} else if (lastLocation.isInRoom()) {
					addVisitRecord(roomIndex, lastLocation.getRoomNumber(),
							record.getArrivalTime(), visitor,
							TransitionEvent.DEPARTURE);
				}
				lastLocation = currentLocation;
			}
		}
		return roomIndex;
	}

	private static void addVisitRecord(
			Map<Integer, SortedSet<VisitorRecord>> roomIndex, int roomNumber,
			int time, Visitor visitor, TransitionEvent event) {
		SortedSet<VisitorRecord> roomVisitors = roomIndex.get(roomNumber);
		if (roomVisitors == null) {
			roomVisitors = new TreeSet<>();
			roomIndex.put(roomNumber, roomVisitors);
		}
		roomVisitors.add(new VisitorRecord(time, visitor, event));
	}
}
