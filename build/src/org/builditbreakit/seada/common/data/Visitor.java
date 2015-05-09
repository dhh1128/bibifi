package org.builditbreakit.seada.common.data;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Visitor implements Serializable {
	private static final long serialVersionUID = -8422758600278331225L;
	
	private transient final String name;
	private transient final VisitorType visitorType;
	private transient final List<LocationRecord> history;

	public Visitor(String name, VisitorType visitorType) {
		// TODO Validate parameters
		
		this.name = name;
		this.visitorType = visitorType;
		this.history = new LinkedList<>();
	}
	
	private Visitor(String name, VisitorType visitorType,
			List<LocationRecord> history) {
		// TODO Validate parameters
		
		this.name = name;
		this.visitorType = visitorType;
		this.history = new LinkedList<>(history);
	}

	public Location getCurrentLocation() {
		if(history.isEmpty()) {
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

	void moveTo(long timestamp, Location newLocation) {
		ValidationUtil.assertValidUINT32(timestamp, "Timestamp");
		ValidationUtil.assertNotNull(newLocation, "Location");
		
		assertValidStateTransition(newLocation);
		history.add(new LocationRecord(timestamp, newLocation));
	}

	@Override
	public int hashCode() {
		/* Eclipse Generated */
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		return true;
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
	
	
	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = -6377133384230120340L;
		
		private final String name;
		private final VisitorType visitorType;
		private final List<LocationRecord> history;
		
		SerializationProxy(Visitor visitor) {
			this.name = visitor.name;
			this.visitorType = visitor.visitorType;
			this.history = visitor.history;
		}
		
		private Object readResolve() {
			return new Visitor(name, visitorType, history);
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