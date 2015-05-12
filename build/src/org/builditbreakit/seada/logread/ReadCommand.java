package org.builditbreakit.seada.logread;

import java.util.ArrayList;
import java.util.List;

import org.builditbreakit.seada.common.data.ValidationUtil;
import org.builditbreakit.seada.common.data.VisitorType;

public class ReadCommand {
	
	public static enum Style {
		/** Corresponds to -S */
		DUMP_CURRENT_STATE,
		/** Corresponds to -R */
		DUMP_ENTERED_ROOMS,
		/** Corresponds to -T */
		TOTAL_TIME,
		/** Corresponds to -I */
		ROOMS_OCCUPIED_TOGETHER,
	}
	
	public static class VisitorSpec {
		VisitorType type;
		String name;
	}
	
	private String token;
	private String logfile;
	private List<VisitorSpec> visitors;	
	private Style style;	

	public String getToken() {
		return token;
	}
	
	public void setToken(String value) {
		//ValidationUtil.assertAssignedOnlyOnce(token == null);
		ValidationUtil.assertValidToken(value);
		token = value;
	}
	
	public Style getStyle() {
		return style;
	}
	
	public void setStyle(Style value) {
		ValidationUtil.assertAssignedOnlyOnce(style == null);
		ValidationUtil.assertNotNull(value, "style");
		style = value;
	}

	public String getLogfile() {
		return logfile;
	}
	
	public void setLogfile(String value) {
		//ValidationUtil.assertAssignedOnlyOnce(logfile == null);
		ValidationUtil.assertValidLogfile(value);
		logfile = value;
	}
	
	public List<VisitorSpec> getVisitors() {
		return visitors;
	}
	
	public void addVisitor(VisitorType type, String name) {
		// Because of ordering vagaries, we do not attempt to prove, here,
		// that the number of visitors matches the semantics implied by
		// the presence of the style flag. We'll do that at the end of
		// the parse method, instead.
		ValidationUtil.assertValidVisitorType(type);
		ValidationUtil.assertValidVisitorName(name);
		if (visitors == null) {
			visitors = new ArrayList<VisitorSpec>();
		}
		VisitorSpec spec = new VisitorSpec();
		spec.name = name;
		spec.type = type;
		visitors.add(spec);
	}
	
	public void parse(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			String arg = args[i];
			if (ValidationUtil.isSwitch(arg)) {
				if (i == args.length - 1) {
					throw new IllegalArgumentException(arg + " must be followed by a value");
				}
				switch (arg.charAt(1)) {
				case 'S':
					setStyle(Style.DUMP_CURRENT_STATE);
					break;
				case 'R':
					setStyle(Style.DUMP_ENTERED_ROOMS);
					break;
				case 'T':
					setStyle(Style.TOTAL_TIME);
					break;
				case 'I':
					setStyle(Style.ROOMS_OCCUPIED_TOGETHER);
					break;
				case 'K':
					setToken(args[++i]);
					break;
				case 'E':
					addVisitor(VisitorType.EMPLOYEE, args[++i]);
					break;
				case 'G':
					addVisitor(VisitorType.GUEST, args[++i]);
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
		switch (style) {
		case DUMP_CURRENT_STATE:
			if (visitors != null) {
				throw new IllegalArgumentException("-S can't be combined with -E or -G");
			}
			break;
		case DUMP_ENTERED_ROOMS:
		case TOTAL_TIME:
			if (visitors == null) {
				throw new IllegalArgumentException("Need -E or -G plus name of visitor");
			}
			if (visitors.size() > 1) {
				throw new IllegalArgumentException("Only one visitor can be supplied with -E or -G");
			}
			break;
		case ROOMS_OCCUPIED_TOGETHER:
			if (visitors == null) {
				throw new IllegalArgumentException("Need at least one -E or -G plus name of visitor");
			}
			break;
		default:
			throw new IllegalArgumentException("Bad style");
		}
		if (logfile == null || token == null) {
			throw new IllegalArgumentException("incomplete command");
		}
	}
}
