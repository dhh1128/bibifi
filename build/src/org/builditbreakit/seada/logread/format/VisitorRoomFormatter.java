package org.builditbreakit.seada.logread.format;

import java.util.List;

import org.builditbreakit.seada.common.data.Location;
import org.builditbreakit.seada.common.data.LocationRecord;
import org.builditbreakit.seada.common.data.ValidationUtil;
import org.builditbreakit.seada.common.data.Visitor;

public class VisitorRoomFormatter implements Formatter {
	private final List<LocationRecord> history;

	public VisitorRoomFormatter(Visitor visitor) {
		ValidationUtil.assertNotNull(visitor, "Visitor");
		this.history = visitor.getHistory();
	}

	@Override
	public String format() {
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		for (LocationRecord record : history) {
			Location location = record.getLocation();
			if (location.isInRoom()) {
				if (first) {
					first = false;
				} else {
					builder.append(FormatUtil.COMMA);
				}
				builder.append(location.getRoomNumber());
			}
		}

		return builder.toString();
	}
}
