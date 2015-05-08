package org.builditbreakit.seada.data;

import java.io.Serializable;

public final class ArrivalRecord implements Serializable, Comparable<ArrivalRecord> {
	private static final long serialVersionUID = -3875621941891444819L;
	
	private final Location location;
	private final long arrivalTime;
	
	ArrivalRecord(long arrivalTime, Location location) {
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
	public int compareTo(ArrivalRecord other) {
		return Long.compare(this.arrivalTime, other.arrivalTime);
	}
}
