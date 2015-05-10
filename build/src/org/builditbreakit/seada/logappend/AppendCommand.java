package org.builditbreakit.seada.logappend;

import org.builditbreakit.seada.common.TransitionEvent;
import org.builditbreakit.seada.common.data.ValidationUtil;
import org.builditbreakit.seada.common.data.VisitorType;

public class AppendCommand {
	
	private long timestamp = -1;
	private String token;
	private String logfile;
	private String visitorName;	
	private long room = -1;	
	private TransitionEvent event;	

	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long value) {
		ValidationUtil.assertAssignedOnlyOnce(timestamp == -1);
		ValidationUtil.assertValidTimestamp(value);
		timestamp = value;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String value) {
		ValidationUtil.assertAssignedOnlyOnce(token == null);
		ValidationUtil.assertValidToken(value);
		token = value;
	}

	public String getLogfile() {
		return logfile;
	}
	
	public void setLogfile(String value) {
		ValidationUtil.assertAssignedOnlyOnce(logfile == null);
		ValidationUtil.assertValidLogfile(value);
		logfile = value;
	}
	
	private VisitorType visitorType;
	
	public VisitorType getVisitorType() {
		return visitorType;
	}
	
	public void setVisitorType(VisitorType value) {
		ValidationUtil.assertAssignedOnlyOnce(visitorType == null);
		ValidationUtil.assertValidVisitorType(value);
		visitorType = value;
	}
	
	public String getVisitorName() {
		return visitorName;
	}
	
	public void setVisitorName(String value) {
		ValidationUtil.assertAssignedOnlyOnce(visitorName == null);
		ValidationUtil.assertValidVisitorName(value);
		visitorName = value;
	}
	
	public long getRoom() {
		return room;
	}
	
	public void setRoom(long value) {
		ValidationUtil.assertAssignedOnlyOnce(room == -1);
		ValidationUtil.assertValidRoomNumber(value);
		room = value;
	}
	
	public TransitionEvent getEvent() {
		return event;
	}
	
	public void setEvent(TransitionEvent value) {
		ValidationUtil.assertAssignedOnlyOnce(event == null);
		ValidationUtil.assertValidEvent(value);
		event = value;
	}
	
	public void parse(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			String arg = args[i];
			if (ValidationUtil.isSwitch(arg)) {
				if (i == args.length - 1) {
					throw new IllegalArgumentException(arg + " must be followed by a value");
				}
				switch (arg.charAt(1)) {
				case 'T':
					setTimestamp(Long.parseLong(args[++i]));
					break;
				case 'K':
					setToken(args[++i]);
					break;
				case 'E':
					setVisitorType(VisitorType.EMPLOYEE);
					setVisitorName(args[++i]);
					break;
				case 'G':
					setVisitorType(VisitorType.GUEST);
					setVisitorName(args[++i]);
					break;
				case 'A':
					setEvent(TransitionEvent.ARRIVAL);
					break;
				case 'L':
					setEvent(TransitionEvent.DEPARTURE);
					break;
				case 'R':
					setRoom(Long.parseLong(args[++i]));
					break;
				default:
					throw new IllegalArgumentException(arg + " not recognized");
				}
			} else if (i + 1 == args.length) {
				setLogfile(arg);
			} else {
				throw new IllegalArgumentException(arg + " not recognized");
			}
		}
		if (logfile == null || event == null || timestamp == -1
				|| visitorType == null || visitorName == null || token == null) {
			throw new IllegalArgumentException("incomplete command");
		}
	}
}
