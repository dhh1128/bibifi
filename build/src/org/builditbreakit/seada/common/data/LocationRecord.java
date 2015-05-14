package org.builditbreakit.seada.common.data;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public final class LocationRecord implements Serializable {
	private static final long serialVersionUID = 5097760039407445632L;
	
	private transient final Location location;
	private transient final int arrivalTime;
	
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
		private static final long serialVersionUID = 1009514308536095709L;
		
		private final Location location;
		private final int arrivalTime;
		
		SerializationProxy(LocationRecord record) {
			this.location = record.location;
			this.arrivalTime = record.arrivalTime;
		}
		
		private Object readResolve() {
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
