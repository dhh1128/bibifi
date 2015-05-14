package org.builditbreakit.seada.logread.format;

import java.util.List;

import org.builditbreakit.seada.common.data.Location;
import org.builditbreakit.seada.common.data.LocationRecord;
import org.builditbreakit.seada.common.data.ValidationUtil;
import org.builditbreakit.seada.common.data.Visitor;
import org.builditbreakit.seada.common.exceptions.IntegrityViolationException;

public class VisitorTimeFormatter implements Formatter {
	private final List<LocationRecord> history;

	public VisitorTimeFormatter(Visitor visitor) {
		ValidationUtil.assertNotNull(visitor, "Visitor");
		this.history = visitor.getHistory();
	}

	@Override
	public String format() {
		int totalTime = 0;
		
		Location lastLocation = Location.OFF_PREMISES;
		int lastTime = 0;
		
		for (LocationRecord record : history) {
			Location currentLocation = record.getLocation();
			if (currentLocation.equals(lastLocation)) {
				throw new IntegrityViolationException();
			}
			if (!(currentLocation.isInGallery() && lastLocation.isOffPremises())) {
				totalTime += record.getArrivalTime() - lastTime;
			}
			lastTime = record.getArrivalTime();
			lastLocation = record.getLocation();
		}
		
		// Add extra newline to match oracle output
		return String.valueOf(totalTime) + FormatUtil.NEWLINE;
	}
}
