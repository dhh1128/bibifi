package org.builditbreakit.seada.common.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;

public final class Visitor {

	private transient final String name;
	private transient final VisitorType visitorType;
	private transient final List<LocationRecord> history;

	public Visitor(String name, VisitorType visitorType) {
		ValidationUtil.assertValidVisitorName(name);
		ValidationUtil.assertValidVisitorType(visitorType);
		
		this.name = name;
		this.visitorType = visitorType;
		this.history = new ArrayList<>();
	}
	
	private Visitor(String name, VisitorType visitorType, List<LocationRecord> history) {
		ValidationUtil.assertValidVisitorName(name);
		ValidationUtil.assertValidVisitorType(visitorType);
		ValidationUtil.assertNotNull(history, "History");

		this.name = name;
		this.visitorType = visitorType;
		this.history = new ArrayList<>(history);
	}

	public Location getCurrentLocation() {
		if (history.isEmpty()) {
			return Location.OFF_PREMISES;
		}
		return history.get(history.size() - 1).getLocation();
	}

	public String getName() {
		return name;
	}
	
	public VisitorType getVisitorType() {
		return visitorType;
	}
	
	public List<LocationRecord> getHistory() {
		return Collections.unmodifiableList(history);
	}

	@Override
	public int hashCode() {
		/* Eclipse Generated */
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result	+ ((visitorType == null) ? 0 : visitorType.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		/* Eclipse Generated */
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Visitor other = (Visitor) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (visitorType != other.visitorType) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Visitor [name=");
		builder.append(name);
		builder.append(", type=");
		builder.append(visitorType);
		builder.append(", lastLocationRecord=");
		if (history.isEmpty()) {
			builder.append(Location.OFF_PREMISES);
		} else {
			builder.append(history.get(history.size() - 1));
		}
		builder.append("]");
		return builder.toString();
	}

	void moveTo(int timestamp, Location newLocation) {
		ValidationUtil.assertValidUINT32(timestamp, "Timestamp");
		ValidationUtil.assertNotNull(newLocation, "Location");

		assertValidStateTransition(newLocation);
		assertValidTime(timestamp);
		history.add(new LocationRecord(timestamp, newLocation));
	}

	private void assertValidTime(int timestamp) {
		if (!history.isEmpty()) {
			int lastTimestamp = history.get(history.size() - 1)
					.getArrivalTime();
			if (lastTimestamp >= timestamp) {
				throw new IllegalStateException("New timestamp " + timestamp
						+ " is not after the visitor's last timestamp: "
						+ lastTimestamp);
			}
		}
	}

	private void assertValidStateTransition(Location newLocation) {
		Location currentLocation = getCurrentLocation();
		if ((currentLocation.isInRoom() || currentLocation.isOffPremises())
				&& !newLocation.isInGallery()) {
			throw new IllegalStateException(getStateTransitionErrorMsg(
					currentLocation, newLocation));
		}
		if (currentLocation.isInGallery() && newLocation.isInGallery()) {
			throw new IllegalStateException(getStateTransitionErrorMsg(
					currentLocation, newLocation));
		}
	}

	private static String getStateTransitionErrorMsg(Location currentLocation,
			Location newLocation) {
		StringBuilder builder = new StringBuilder(80);
		builder.append("Cannot transition from state ").append(currentLocation)
				.append(" to state ").append(newLocation);
		return builder.toString();
	}

	private static final Serializer<Visitor> employeeSerializer = new VisitorSerializer(VisitorType.EMPLOYEE);
	private static final Serializer<Visitor> guestSerializer = new VisitorSerializer(VisitorType.GUEST);

	public static Serializer<Visitor> getSerializer(VisitorType serializerType) {
		if(serializerType == VisitorType.EMPLOYEE) {
			return employeeSerializer;
		}
		return guestSerializer;
	}

	private static class VisitorSerializer extends Serializer<Visitor> {
		private static CollectionSerializer historySerializer = new CollectionSerializer(
				LocationRecord.class, LocationRecord.getSerializer(), false);
		private final VisitorType serializerType;

		public VisitorSerializer(VisitorType serializerType) {
			super(true);
			this.serializerType = serializerType;
		}

		@Override
		public Visitor read(Kryo kryo, Input in, Class<Visitor> clazz) {
			String name = kryo.readObject(in, String.class);
			@SuppressWarnings("unchecked")
			List<LocationRecord> history = kryo.readObject(in, ArrayList.class,
					historySerializer);
			return new Visitor(name, serializerType, history);
		}

		@Override
		public void write(Kryo kryo, Output out, Visitor object) {
			if (object == null) {
				throw new IntegrityViolationException();
			}
			kryo.writeObject(out, object.name);
			kryo.writeObject(out, object.history, historySerializer);
		}
	}
}
