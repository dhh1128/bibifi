package org.builditbreakit.seada.common.data;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

public class GalleryState implements Serializable {
	private static final long serialVersionUID = 6928424752159836102L;

	private transient int lastTimestamp = 0;
	private transient final Map<String, Visitor> guestMap;
	private transient final Map<String, Visitor> employeeMap;

	public GalleryState() {
		this.guestMap = new HashMap<>();
		this.employeeMap = new HashMap<>();
	}

	private GalleryState(Collection<? extends Visitor> guests,
			Collection<? extends Visitor> employees) {
		this.employeeMap = buildMap(employees);
		this.guestMap = buildMap(guests);
	}
	
	public Collection<Visitor> getGuests() {
		return Collections.unmodifiableCollection(guestMap.values());
	}

	public Collection<Visitor> getEmployees() {
		return Collections.unmodifiableCollection(employeeMap.values());
	}
	
	private Map<String, Visitor> getMap(VisitorType visitorType) {
		if (visitorType == VisitorType.EMPLOYEE) {
			return employeeMap;
		}
		if (visitorType == VisitorType.GUEST) {
			return guestMap;
		}
		throw new IntegrityViolationException("Unknown visitor type: "
				+ visitorType);
	}

	public Visitor getVisitor(String name, VisitorType visitorType) {
		ValidationUtil.assertValidVisitorType(visitorType);
		ValidationUtil.assertValidVisitorName(name);
		
		Visitor visitor = getMap(visitorType).get(name);
		if (visitorType == VisitorType.EMPLOYEE) {
			visitor = employeeMap.get(name);
		} else if (visitorType == VisitorType.GUEST) {
			visitor = guestMap.get(name);
		} else {
			throw new IntegrityViolationException("Unknown visitor type: "
					+ visitorType);
		}

		if (visitor == null) {
			throw new IllegalStateException("Visitor does not exist");
		}

		return visitor;
	}
	
	public int getLastTimestamp() {
		return lastTimestamp;
	}
	
	public boolean containsVisitor(String name, VisitorType visitorType) {
		return getMap(visitorType).get(name) != null;
	}

	public void arriveAtBuilding(int timestamp, String name,
			VisitorType visitorType) {
		ValidationUtil.assertValidVisitorType(visitorType);
		ValidationUtil.assertValidVisitorName(name);
		assertValidTimestamp(timestamp);

		Map<String, Visitor> map = getMap(visitorType);
		Visitor visitor = map.get(name);
		if (visitor == null) {
			visitor = new Visitor(name);
			map.put(name, visitor);
		}

		if(!visitor.getCurrentLocation().isOffPremises()) {
			throw new IllegalStateException("Visitor is already in the gallery");
		}
		visitor.moveTo(timestamp, Location.IN_GALLERY);
		lastTimestamp = timestamp;
	}

	public void arriveAtRoom(int timestamp, String name,
			VisitorType visitorType, int roomNumber) {
		ValidationUtil.assertValidVisitorType(visitorType);
		ValidationUtil.assertValidVisitorName(name);
		ValidationUtil.assertValidRoomNumber(roomNumber);
		assertValidTimestamp(timestamp);

		Visitor visitor = getVisitor(name, visitorType);
		visitor.moveTo(timestamp, Location.locationOfRoom(roomNumber));
		lastTimestamp = timestamp;
	}

	public void departRoom(int timestamp, String name, VisitorType visitorType, int roomNumber) {
		ValidationUtil.assertValidVisitorType(visitorType);
		ValidationUtil.assertValidVisitorName(name);
		ValidationUtil.assertValidRoomNumber(roomNumber);
		assertValidTimestamp(timestamp);

		Visitor visitor = getVisitor(name, visitorType);
		Location currentLocation = visitor.getCurrentLocation();
		if (!currentLocation.equals(Location.locationOfRoom(roomNumber))) {
			throw new IllegalStateException("Visitor is not in that room");
		}
		visitor.moveTo(timestamp, Location.IN_GALLERY);
		lastTimestamp = timestamp;
	}

	public void departBuilding(int timestamp, String name, VisitorType visitorType) {
		ValidationUtil.assertValidVisitorType(visitorType);
		ValidationUtil.assertValidVisitorName(name);
		assertValidTimestamp(timestamp);

		Visitor visitor = getVisitor(name, visitorType);
		Location currentLocation = visitor.getCurrentLocation();
		if (!currentLocation.isInGallery()) {
			throw new IllegalStateException("Visitor is not in the lobby");
		}
		visitor.moveTo(timestamp, Location.OFF_PREMISES);
		lastTimestamp = timestamp;
	}

	private void assertValidTimestamp(int timestamp) {
		ValidationUtil.assertValidUINT32(timestamp, "Timestamp");
		if (timestamp <= lastTimestamp) {
			throw new IllegalStateException("Timestamp " + timestamp
					+ " does not follow the current timestamp: " + lastTimestamp);
		}
	}

	private Map<String, Visitor> buildMap(Collection<? extends Visitor> visitors) {
		ValidationUtil.assertNotNull(visitors, "Imported data");
	
		final float load_factor = 0.75f;
		final int default_capacity = 16;
		final int extra_space = 10;
		final int initial_capacity = Math.max(
				(int) (visitors.size() / load_factor) + extra_space,
				default_capacity);
		Map<String, Visitor> visitorMap = new HashMap<>(initial_capacity,
				load_factor);
	
		if (!visitors.isEmpty()) {
			visitors.forEach((visitor) -> {
				if (visitorMap.put(visitor.getName(), visitor) != null) {
					throw new IntegrityViolationException("Duplicate visitor: "
							+ visitor.getName());
				}
				List<LocationRecord> history = visitor.getHistory();
				if (!history.isEmpty()) {
					int lastVisitTime = history.get(history.size() - 1)
							.getArrivalTime();
					if (lastVisitTime > lastTimestamp) {
						lastTimestamp = lastVisitTime;
					}
				}
			});
		}
	
		return visitorMap;
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = 8768461134732556971L;

		private final Collection<Visitor> guests;
		private final Collection<Visitor> employees;

		SerializationProxy(GalleryState galleryState) {
			// Need to make a copy. Map values are not serializable
			this.employees = new ArrayList<>(galleryState.employeeMap.values());
			this.guests = new ArrayList<>(galleryState.guestMap.values());
		}

		private Object readResolve() {
			return new GalleryState(guests, employees);
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
