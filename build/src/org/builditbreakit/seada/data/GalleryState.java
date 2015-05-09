package org.builditbreakit.seada.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GalleryState implements Serializable {
	private static final long serialVersionUID = 1229809171003207567L;
	
	private final Set<Visitor> visitors;
	private long lastTimestamp = 0;

	private transient final Map<String, Visitor> visitorMap;

	public GalleryState() {
		this.visitors = new HashSet<>();
		this.visitorMap = new HashMap<>();
	}

	public Set<Visitor> getVisitors() {
		return Collections.unmodifiableSet(visitors);
	}

	public Visitor getVisitor(VisitorType visitorType, String name) {
		ValidationUtil.assertValidVisitorType(visitorType);
		ValidationUtil.assertValidVisitorName(name);
		
		Visitor visitor = visitorMap.get(name);
		if (visitor == null) {
			// TODO Message
			throw new IllegalStateException();
		}

		assertMatchingVisitorType(visitor, visitorType);
		return visitor;
	}

	public void arriveAtBuilding(VisitorType visitorType, long timestamp,
			String name) {
		ValidationUtil.assertValidVisitorType(visitorType);
		ValidationUtil.assertValidVisitorName(name);
		assertValidTimestamp(timestamp);

		Visitor visitor = visitorMap.get(name);
		if (visitor == null) {
			visitor = new Visitor(name, visitorType);
			visitorMap.put(name, visitor);
			visitors.add(visitor);
		} else {
			assertMatchingVisitorType(visitor, visitorType);
		}
 
		visitor.moveTo(timestamp, Location.GALLERY);
	}

	public void arriveAtRoom(VisitorType visitorType, long timestamp,
			String name, long roomNumber) {
		ValidationUtil.assertValidVisitorType(visitorType);
		ValidationUtil.assertValidVisitorName(name);
		ValidationUtil.assertValidRoomNumber(roomNumber);
		assertValidTimestamp(timestamp);

		Visitor visitor = getVisitor(visitorType, name);
		if (visitor == null) {
			// TODO Message
			throw new IllegalStateException();
		}

		assertMatchingVisitorType(visitor, visitorType);
		visitor.moveTo(timestamp, Location.locationOfRoom(roomNumber));
	}

	public void depart(VisitorType visitorType, long timestamp, String name) {
		ValidationUtil.assertValidVisitorType(visitorType);
		ValidationUtil.assertValidVisitorName(name);
		assertValidTimestamp(timestamp);
		
		Visitor visitor = getVisitor(visitorType, name);
		Location currentLocation = visitor.getCurrentLocation();
		if (currentLocation.isInRoom()) {
			visitor.moveTo(timestamp, Location.GALLERY);
		} else if (currentLocation.isInGallery()) {
			visitor.moveTo(timestamp, Location.OFF_PREMISES);
		} else {
			// TODO Message
			throw new IllegalStateException();
		}
	}

	private void assertValidTimestamp(long timestamp) {
		ValidationUtil.assertValidUINT32(timestamp, "Timestamp");
		if (timestamp <= lastTimestamp) {
			throw new IllegalStateException("Timestamp " + timestamp
					+ " is prior to the current timestamp: " + lastTimestamp);
		}
	}
	
	private void assertMatchingVisitorType(Visitor visitor,
			VisitorType visitorType) {
		if (visitor.getVisitorType() != visitorType) {
			// TODO Message
			throw new IllegalStateException();
		}
	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		// default deserialization
		ois.defaultReadObject();

		// Populate transient values
		if (!visitors.isEmpty()) {
			visitors.forEach((visitor) -> visitorMap.put(visitor.getName(),
					visitor));
		}
	}

	
}
