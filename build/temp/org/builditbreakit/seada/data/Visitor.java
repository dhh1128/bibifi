package org.builditbreakit.seada.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Visitor implements Serializable {	
	private static final long serialVersionUID = -2865757182731074012L;
	
	private final String name;
	private final VisitorType visitorType;
	private final List<ArrivalRecord> history;
	 
	private transient Location currentLocation = Location.OFF_PREMISES;

	public Visitor(String name, VisitorType visitorType) {
		this.name = name;
		this.visitorType = visitorType;
		this.history = new LinkedList<>();
	}
	
	public Location getCurrentLocation() {
		return currentLocation;
	}
	
	public String getName() {
		return name;
	}
	
	public VisitorType getVisitorType() {
		return visitorType;
	}
	
	public List<ArrivalRecord> getHistory() {
		return Collections.unmodifiableList(history);
	}

	void moveTo(long timestamp, Location newLocation) {
		ValidationUtil.assertValidUINT32(timestamp, "Timestamp");
		ValidationUtil.assertNotNull(newLocation, "Location");
		
		assertValidStateTransition(newLocation);
		history.add(new ArrivalRecord(timestamp, newLocation));
		currentLocation = newLocation;
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
	
	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		// default deserialization
		ois.defaultReadObject();
	
		// Populate transient values
		if (!history.isEmpty()) {
			currentLocation = history.get(history.size() - 1).getLocation();
		}
	}

	private static String getStateTransitionErrorMsg(Location currentLocation,
			Location newLocation) {
		StringBuilder builder = new StringBuilder(80);
		builder.append("Cannot transition from state ").append(currentLocation)
				.append(" to state ").append(newLocation);
		return builder.toString();
	}
}