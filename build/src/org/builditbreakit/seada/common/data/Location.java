package org.builditbreakit.seada.common.data;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Location implements Serializable {
	private static final long serialVersionUID = -2330173990359232226L;
	
	public static final Location OFF_PREMISES = new Location(
			ValidationUtil.UINT32_MIN - 2);
	public static final Location IN_GALLERY = new Location(
			ValidationUtil.UINT32_MIN - 1);

	private static final Map<Long, Location> cache = Collections
			.synchronizedMap(new HashMap<>());

	private transient long state;

	private Location(long location) {
		this.state = location;
	}

	public boolean isOffPremises() {
		return state == OFF_PREMISES.state;
	}

	public boolean isInBuilding() {
		return !isOffPremises();
	}

	public boolean isInGallery() {
		return state == IN_GALLERY.state;
	}

	public boolean isInRoom() {
		return state >= ValidationUtil.UINT32_MIN;
	}

	public Long getRoomNumber() {
		if (!isInRoom()) {
			return null;
		}
		return state;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (state ^ (state >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Location other = (Location) obj;
		if (state != other.state) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (isOffPremises()) {
			return "OFF_PREMISES";
		}
		if (isInGallery()) {
			return "IN_GALLERY";
		}
		return Long.toString(state);
	}

	public synchronized static Location locationOfRoom(long roomNumber) {
		ValidationUtil.assertValidRoomNumber(roomNumber);

		Location result = cache.get(roomNumber);
		if (result == null) {
			synchronized (Location.class) {
				if (result == null) {
					result = new Location(roomNumber);
					cache.put(roomNumber, result);
				}
			}
		}
		return result;
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = -6040714525289607781L;
		
		private final long state;

		SerializationProxy(Location location) {
			this.state = location.state;
		}

		private Object readResolve() {
			if (state == OFF_PREMISES.state) {
				return OFF_PREMISES;
			}
			if (state == IN_GALLERY.state) {
				return IN_GALLERY;
			}
			return locationOfRoom(state);
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
