package org.builditbreakit.seada.logappend;

import org.builditbreakit.seada.common.TransitionEvent;
import org.builditbreakit.seada.common.data.ValidationUtil;
import org.builditbreakit.seada.common.data.VisitorType;

public class AppendCommand {
	
	private int timestamp = -1;
	private String token;
	private String logfile;
	private String visitorName;	
	private int room = -1;	
	private TransitionEvent event;
	
	public AppendCommand(String[] args) {
		parse(args);
	}

	public int getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(int value) {
		ValidationUtil.assertValidTimestamp(value);
		timestamp = value;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String value) {
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
		ValidationUtil.assertValidVisitorType(value);
		if (visitorType != null && value != visitorType) {
			throw new IllegalArgumentException("Can't specify both -E and -G");
		}
		visitorType = value;
	}
	
	public String getVisitorName() {
		return visitorName;
	}
	
	public void setVisitorName(String value) {
		ValidationUtil.assertValidVisitorName(value);
		visitorName = value;
	}
	
	public int getRoom() {
		return room;
	}
	
	public void setRoom(int value) {
		ValidationUtil.assertValidRoomNumber(value);
		room = value;
	}
	
	public TransitionEvent getEvent() {
		return event;
	}
	
	public void setEvent(TransitionEvent value) {
		ValidationUtil.assertValidEvent(value);
		if (event != null && value != event) {
			throw new IllegalArgumentException("Can't specify both -A and -L");
		}
		event = value;
	}
	
	private static boolean isValuelessSwitch(String arg) {
		switch (arg.charAt(1)) {
		case 'T':
		case 'K':
		case 'E':
		case 'G':
		case 'R':
			return false;
		default:
			return true;
		}
	}
	
	public void parse(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			String arg = args[i];
			if (ValidationUtil.isSwitch(arg)) {
				if (i == args.length - 1 && !isValuelessSwitch(arg)) {
					throw new IllegalArgumentException(arg + " must be followed by a value");
				}
				switch (arg.charAt(1)) {
				case 'T':
					setTimestamp(Integer.parseInt(args[++i]));
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
					setRoom(Integer.parseInt(args[++i]));
					break;
				default:
					throw new IllegalArgumentException(arg + " not recognized");
				}
			} else {
				setLogfile(arg);
			}
		}
		if (logfile == null || event == null || timestamp == -1
				|| visitorType == null || visitorName == null || token == null) {
			throw new IllegalArgumentException("incomplete command");
		}
	}
}
