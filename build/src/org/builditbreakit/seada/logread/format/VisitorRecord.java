package org.builditbreakit.seada.logread.format;

import org.builditbreakit.seada.common.TransitionEvent;
import org.builditbreakit.seada.common.data.Visitor;

final class VisitorRecord implements Comparable<VisitorRecord> {
	private final long time;
	private final Visitor visitor;
	private final TransitionEvent event;

	public VisitorRecord(long time, Visitor visitor, TransitionEvent event) {
		this.time = time;
		this.visitor = visitor;
		this.event = event;
	}

	public long getTime() {
		return time;
	}

	public Visitor getVisitor() {
		return visitor;
	}

	public TransitionEvent getEvent() {
		return event;
	}

	@Override
	public int compareTo(VisitorRecord other) {
		return Long.compare(this.time, other.time);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VisitorRecord [time=");
		builder.append(time);
		builder.append(", visitor=");
		builder.append(visitor.getName());
		builder.append(", event=");
		builder.append(event);
		builder.append("]");
		return builder.toString();
	}
}