package org.builditbreakit.seada.common.data;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public final class LocationRecord implements Serializable, Comparable<LocationRecord> {
	private static final long serialVersionUID = -1826625561705118367L;
	
	private transient final Location location;
	private transient final long arrivalTime;
	
	LocationRecord(long arrivalTime, Location location) {
		ValidationUtil.assertValidTimestamp(arrivalTime);
		ValidationUtil.assertValidLocation(location);

		this.location = location;
		this.arrivalTime = arrivalTime;
	}

	public Location getLocation() {
		return location;
	}
	
	public long getArrivalTime() {
		return arrivalTime;
	}

	@Override
	public int compareTo(LocationRecord other) {
		return Long.compare(this.arrivalTime, other.arrivalTime);
	}
	
	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = 1009514308536095709L;
		
		private final Location location;
		private final long arrivalTime;
		
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
