package org.builditbreakit.seada.common.data;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

public class GalleryState implements Serializable {
	private static final long serialVersionUID = 6928424752159836102L;
	
	private transient long lastTimestamp = 0;
	private transient final Map<String, Visitor> visitorMap;

	public GalleryState() {
		this.visitorMap = new HashMap<>();
	}

	private GalleryState(Collection<? extends Visitor> visitors) {
		final float load_factor = 0.75f;
		final int default_capacity = 16;
		final int extra_space = 10;
		final int initial_capacity = Math.max(
				(int) (visitors.size() / load_factor) + extra_space,
				default_capacity);

		this.visitorMap = new HashMap<>(initial_capacity, load_factor);

		if (!visitors.isEmpty()) {
			visitors.forEach((visitor) -> {
				if (visitorMap.put(visitor.getName(), visitor) != null) {
					throw new IntegrityViolationException("Duplicate visitor: "
							+ visitor.getName());
				}
				List<LocationRecord> history = visitor.getHistory();
				if (!history.isEmpty()) {
					long lastVisitTime = history.get(history.size() - 1)
							.getArrivalTime();
					if (lastVisitTime > lastTimestamp) {
						lastTimestamp = lastVisitTime;
					}
				}
			});
		}
	}

	public Collection<Visitor> getVisitors() {
		return Collections.unmodifiableCollection(visitorMap.values());
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
		} else {
			assertMatchingVisitorType(visitor, visitorType);
		}
 
		visitor.moveTo(timestamp, Location.IN_GALLERY);
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
			visitor.moveTo(timestamp, Location.IN_GALLERY);
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
	
	
	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = 8768461134732556971L;
		
		private final Collection<Visitor> visitors;
		
		SerializationProxy(GalleryState galleryState) {
			this.visitors = galleryState.getVisitors();
		}
		
		private Object readResolve() {
			return new GalleryState(visitors);
		}
	}

	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	private void readObject(ObjectInputStream ois)
			throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}
}
