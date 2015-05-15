package org.builditbreakit.seada.common.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a physical location relative to the art gallery. Valid locations
 * are:
 * <ul>
 * <li>{@code OFF_PREMISES} - Location outside the building</li>
 * <li>{@code IN_GALLERY} - Location inside the building, specifically the
 * gallery</li>
 * <li>{@code IN_ROOM(N)} - Location inside the building, specifically Room
 * Number N</li>
 * </ul>
 * 
 * {@link #OFF_PREMISES} and {@link #IN_GALLERY} locations are obtained by
 * accessing the corresponding constant field. {@code IN_ROOM(N)} instances are
 * obtained by calling {@link #locationOfRoom(int)}. All instances are
 * immutable.
 */
public final class Location {
	static final int OFF_PREMISES_STATE = ValidationUtil.UINT32_MIN - 2;

	static final int IN_GALLERY_STATE = ValidationUtil.UINT32_MIN - 1;

	/**
	 * A location outside the building
	 */
	public static final Location OFF_PREMISES = new Location(OFF_PREMISES_STATE);

	/**
	 * The Gallery location inside the building
	 */
	public static final Location IN_GALLERY = new Location(IN_GALLERY_STATE);

	/*
	 * Since there could be lots of rooms we limit the number of objects. For
	 * example, room #5 will always be represented by the same object.
	 */
	private static final Map<Integer, Location> cache = new HashMap<>();

	
	private final int state;

	/**
	 * Private. Object creation is managed by {@link #locationOfRoom(int)}
	 */
	private Location(int location) {
		this.state = location;
	}

	/**
	 * Check if this location is {@code OFF_PREMISES}
	 * 
	 * @return {@code true} iff this location represents the
	 *         {@link #OFF_PREMISES} location.
	 */
	public boolean isOffPremises() {
		return state == OFF_PREMISES.state;
	}

	/**
	 * Check if this location is {@code IN_GALLERY}
	 * 
	 * @return {@code true} iff this location represents the {@link #IN_GALLERY}
	 *         location.
	 */
	public boolean isInGallery() {
		return state == IN_GALLERY.state;
	}

	/**
	 * Check if this location is one of the {@code IN_ROOM} locations.
	 * 
	 * @return {@code true} iff this location is one of the {@code IN_ROOM}
	 *         locations
	 */
	public boolean isInRoom() {
		return state >= ValidationUtil.UINT32_MIN;
	}

	/**
	 * Get the room number if this location is one of the {@code IN_ROOM}
	 * locations.
	 * 
	 * @return the room number N if this location is one of {@code IN_ROOM(N)};
	 *         Otherwise returns {@code null}
	 * @see #isInRoom()
	 */
	public Integer getRoomNumber() {
		if (!isInRoom()) {
			return null;
		}
		return state;
	}
	
	@Override
	public String toString() {
		if (isOffPremises()) {
			return "OFF_PREMISES";
		}
		if (isInGallery()) {
			return "IN_GALLERY";
		}
		return  "IN_ROOM(" + state + ")";
	}

	/**
	 * @return {@code true} if this object represents the same location as the
	 *         {@code obj} argument; {@code false} otherwise.
	 */
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
	public int hashCode() {
		return state;
	}

	/**
	 * Returns the location representing the given room number. Room number must
	 * be a valid unsigned 32-bit integer.
	 * 
	 * @param roomNumber
	 *            the room number
	 * @return the location {@code IN_ROOM(N)}, where N is the
	 *         {@code roomNumber} parameter
	 */
	public synchronized static Location locationOfRoom(int roomNumber) {
		ValidationUtil.assertValidRoomNumber(roomNumber);

		Location result = cache.get(roomNumber);
		if (result == null) {
			result = new Location(roomNumber);
			cache.put(roomNumber, result);
		}
		return result;
	}
}
