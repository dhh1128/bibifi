package org.builditbreakit.seada.common.data;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public final class LocationRecord implements Serializable {
	private static final long serialVersionUID = -9012533294897310918L;
	
	private final Location location;
	private final int arrivalTime;
	
	LocationRecord(int arrivalTime, Location location) {
		ValidationUtil.assertValidTimestamp(arrivalTime);
		ValidationUtil.assertValidLocation(location);

		this.location = location;
		this.arrivalTime = arrivalTime;
	}

	public Location getLocation() {
		return location;
	}
	
	public int getArrivalTime() {
		return arrivalTime;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LocationRecord [arrivalTime=");
		builder.append(arrivalTime);
		builder.append(", location=");
		builder.append(location);
		builder.append("]");
		return builder.toString();
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = -4050269614086351057L;
		
		private final int locationState;
		private final int arrivalTime;
		
		SerializationProxy(LocationRecord record) {
			Location location = record.location;
			if(location.isOffPremises()) {
				this.locationState = Location.OFF_PREMISES_STATE;
			} else if(location.isInGallery()){
				this.locationState = Location.IN_GALLERY_STATE;
			} else {
				this.locationState = location.getRoomNumber();
			}
			this.arrivalTime = record.arrivalTime;
		}
		
		private Object readResolve() {
			Location location;
			if (locationState == Location.OFF_PREMISES_STATE) {
				location = Location.OFF_PREMISES;
			} else if (locationState == Location.IN_GALLERY_STATE) {
				location = Location.IN_GALLERY;
			} else {
				location = Location.locationOfRoom(locationState);
			}
			return new LocationRecord(arrivalTime, location);
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
