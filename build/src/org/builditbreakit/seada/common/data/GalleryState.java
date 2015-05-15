package org.builditbreakit.seada.common.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;

public class GalleryState {
	private transient int lastTimestamp = 0;
	private transient final Map<String, Visitor> visitorMap;

	public GalleryState() {
		this.visitorMap = new HashMap<>();
	}

	private GalleryState(Collection<? extends Visitor> visitors) {
		ValidationUtil.assertNotNull(visitors, "Imported data");

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
					int lastVisitTime = history.get(history.size() - 1)
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

	public Visitor getVisitor(String name, VisitorType visitorType) {
		ValidationUtil.assertValidVisitorType(visitorType);
		ValidationUtil.assertValidVisitorName(name);

		Visitor visitor = visitorMap.get(name);
		if (visitor == null) {
			throw new IllegalStateException("Visitor does not exist");
		}

		assertMatchingVisitorType(visitor, visitorType);
		return visitor;
	}
	
	public int getLastTimestamp() {
		return lastTimestamp;
	}
	
	public boolean containsVisitor(String name, VisitorType visitorType) {
		Visitor visitor = visitorMap.get(name);
		if (visitor == null) {
			return false;
		}
		return visitor.getVisitorType() == visitorType;
	}

	public void arriveAtBuilding(int timestamp, String name,
			VisitorType visitorType) {
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
		if (visitor == null) {
			throw new IllegalStateException("Visitor does not exist");
		}
		
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

	private void assertMatchingVisitorType(Visitor visitor,
			VisitorType visitorType) {
		if (visitor.getVisitorType() != visitorType) {
			throw new IllegalStateException("Incorrect visitor type");
		}
	}
	
	private static final Serializer<GalleryState> serializer = new GalleryStateSerializer();
	public static Serializer<GalleryState> getSerializer() {
		return serializer;
	}

	private static class GalleryStateSerializer extends
			Serializer<GalleryState> {
		private static CollectionSerializer visitorsSerializer = new CollectionSerializer(
				Visitor.class, Visitor.getSerializer(), false);

		@Override
		public GalleryState read(Kryo kryo, Input in, Class<GalleryState> clazz) {
			@SuppressWarnings("unchecked")
			Collection<Visitor> visitors = kryo.readObject(in,
					ArrayList.class, visitorsSerializer);
			return new GalleryState(visitors);
		}

		@Override
		public void write(Kryo kryo, Output out, GalleryState object) {
			Collection<Visitor> visitors = object.visitorMap.values();
			kryo.writeObject(out, visitors, visitorsSerializer);
		}
	}
}
