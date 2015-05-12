package org.builditbreakit.seada.logread.format;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.data.Location;
import org.builditbreakit.seada.common.data.ValidationUtil;
import org.builditbreakit.seada.common.data.Visitor;
import org.builditbreakit.seada.common.data.VisitorType;
import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

public class StateFormatter implements Formatter {
	private final GalleryState galleryState;

	private final Set<String> employees = new TreeSet<>();
	private final Set<String> guests = new TreeSet<>();
	private final Map<Long, Set<String>> rooms = new TreeMap<>();

	public StateFormatter(GalleryState galleryState) {
		ValidationUtil.assertNotNull(galleryState, "Gallery State");
		this.galleryState = galleryState;
	}

	@Override
	public String format() {
		buildIndex();
		StringBuilder strBuilder = new StringBuilder();
		join(strBuilder, employees).append(FormatUtil.NEWLINE);
		join(strBuilder, guests).append(FormatUtil.NEWLINE);
		strBuilder.append(FormatUtil.NEWLINE);
		appendRooms(strBuilder);
		
		// Trim last newline to match oracle output
		strBuilder.setLength(strBuilder.length() - FormatUtil.NEWLINE.length());
		return strBuilder.toString();
	}

	private StringBuilder appendRooms(StringBuilder strBuilder) {
		for (Entry<Long, Set<String>> roomEntry : rooms.entrySet()) {
			strBuilder.append(Long.toString(roomEntry.getKey())).append(":");
			join(strBuilder, roomEntry.getValue()).append(FormatUtil.NEWLINE);
		}
		return strBuilder;
	}

	private static StringBuilder join(StringBuilder strBuilder, Set<String> set) {
		boolean first = true;
		for (String value : set) {
			if (first) {
				first = false;
			} else {
				strBuilder.append(FormatUtil.COMMA);
			}
			strBuilder.append(value);
		}
		return strBuilder;
	}

	private void buildIndex() {
		for (Visitor visitor : galleryState.getVisitors()) {
			Location visitorLocation = visitor.getCurrentLocation();
			if (!visitorLocation.isOffPremises()) {
				String visitorName = visitor.getName();
				VisitorType visitorType = visitor.getVisitorType();
				switch (visitorType) {
				case EMPLOYEE:
					employees.add(visitorName);
					break;
				case GUEST:
					guests.add(visitorName);
					break;
				default:
					throw new IntegrityViolationException(
							"Unknwon visitor type: " + visitorType);
				}

				if (visitorLocation.isInRoom()) {
					long roomNumber = visitorLocation.getRoomNumber();
					Set<String> room = rooms.get(roomNumber);
					if (room == null) {
						room = new TreeSet<String>();
						rooms.put(roomNumber, room);
					}
					room.add(visitorName);
				}
			}
		}
	}
}
