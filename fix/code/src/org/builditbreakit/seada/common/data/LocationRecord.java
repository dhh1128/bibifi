package org.builditbreakit.seada.common.data;

import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class LocationRecord {
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

	private static final Serializer<LocationRecord> serializer = new LocationRecordSerializer();
	public static Serializer<LocationRecord> getSerializer() {
		return serializer;
	}

	private static class LocationRecordSerializer extends
			Serializer<LocationRecord> {

		public LocationRecordSerializer() {
			super(true, true);
		}

		@Override
		public LocationRecord read(Kryo kryo, Input in, Class<LocationRecord> clazz) {
			int locationState = kryo.readObject(in, Integer.TYPE);
			int arrivalTime = kryo.readObject(in, Integer.TYPE);
			
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

		@Override
		public void write(Kryo kryo, Output out, LocationRecord object) {
			if(object == null) {
				throw new IntegrityViolationException();
			}
			int locationState;
			Location location = object.location;
			if(location.isOffPremises()) {
				locationState = Location.OFF_PREMISES_STATE;
			} else if(location.isInGallery()){
				locationState = Location.IN_GALLERY_STATE;
			} else {
				locationState = location.getRoomNumber();
			}
			
			kryo.writeObject(out, locationState);
			kryo.writeObject(out, object.arrivalTime);
		}
	}
}
