package org.builditbreakit.seada.data;

import java.io.Serializable;

public final class Location implements Serializable {
	private static final long serialVersionUID = -3374934159786470141L;
	
	public static final Location OFF_PREMISES = new Location(ValidationUtil.UINT32_MIN - 2);
	public static final Location GALLERY = new Location(ValidationUtil.UINT32_MIN - 1);

	private long location;

	private Location(long location) {
		this.location = location;
	}

	public boolean isOffPremises() {
		return location == OFF_PREMISES.location;
	}

	public boolean isInBuilding() {
		return !isOffPremises();
	}

	public boolean isInGallery() {
		return location == GALLERY.location;
	}

	public boolean isInRoom() {
		return location >= ValidationUtil.UINT32_MIN;
	}

	public Long getRoomNumber() {
		if (!isInRoom()) {
			return null;
		}
		return location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (location ^ (location >>> 32));
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
		if (location != other.location) {
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
			return "GALLERY";
		}
		return Long.toString(location);
	}

	public static Location locationOfRoom(long roomNumber) {
		ValidationUtil.assertValidRoomNumber(roomNumber);
		// TODO Cache instances if object creation causes performance bottleneck
		return new Location(roomNumber);
	}
}
