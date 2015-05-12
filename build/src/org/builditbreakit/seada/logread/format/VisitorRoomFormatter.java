package org.builditbreakit.seada.logread.format;

import java.util.List;

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
		return FormatUtil.join(history, FormatUtil.COMMA, (item) -> item
				.getLocation().isInRoom(), (item) -> item.getLocation()
				.getRoomNumber());
	}
}
