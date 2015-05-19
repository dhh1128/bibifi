package org.builditbreakit.seada.logread.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.data.Location;
import org.builditbreakit.seada.common.data.ValidationUtil;
import org.builditbreakit.seada.common.data.Visitor;

public class StateFormatter implements Formatter {
	private final GalleryState galleryState;

	private final Set<String> employees = new TreeSet<>();
	private final Set<String> guests = new TreeSet<>();
	private final Map<Integer, List<String>> rooms = new TreeMap<>();

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
		for (Entry<Integer, List<String>> roomEntry : rooms.entrySet()) {
			strBuilder.append(Integer.toString(roomEntry.getKey())).append(":");
			join(strBuilder, roomEntry.getValue()).append(FormatUtil.NEWLINE);
		}
		return strBuilder;
	}

	private void buildIndex() {
		buildIndex(employees, galleryState.getEmployees());
		buildIndex(guests, galleryState.getGuests());
		for(List<String> list : rooms.values()) {
			list.sort(null);
		}
	}
	
	private void buildIndex(Set<String> index, Collection<Visitor> visitors) {
		for (Visitor visitor : visitors) {
			Location visitorLocation = visitor.getCurrentLocation();
			if (!visitorLocation.isOffPremises()) {
				String name = visitor.getName();
				index.add(name);
				
				if (visitorLocation.isInRoom()) {
					int roomNumber = visitorLocation.getRoomNumber();
					List<String> room = rooms.get(roomNumber);
					if (room == null) {
						room = new ArrayList<String>();
						rooms.put(roomNumber, room);
					}
					room.add(name);
				}
			}
		}
	}

	private static StringBuilder join(StringBuilder strBuilder, Collection<String> set) {
		return FormatUtil.join(strBuilder, set, FormatUtil.COMMA);
	}
}
