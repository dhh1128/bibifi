package org.builditbreakit.seada.logappend;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.io.LogFileReader;
import org.builditbreakit.seada.logappend.io.LogFileWriter;

public class GalleryUpdateManager {
	
	private Map<String, GalleryUpdate> states = new HashMap<String, GalleryUpdate>();
	
	/**
	 * Find the existing gallery state for a particular path -- or create a new one, if
	 * the path doesn't exist yet. This allows the same state to be reused without being
	 * read/written between each use.
	 */
	public GalleryUpdate getGalleryFor(AppendCommand cmd) throws IOException, SecurityException {
		File f = new File(cmd.getLogfile());
		String password = cmd.getToken();
		// Convert to canonical version of path so different paths to the same logfile
		// are resolved to the same thing.
		String key = f.getCanonicalPath();
		GalleryUpdate item = states.get(key);
		if (item == null) {
			GalleryState state;
			if (f.exists()) {
				LogFileReader reader = new LogFileReader(new File(key));
				state = reader.read(password);
			} else {
				state = new GalleryState();
			}
			item = new GalleryUpdate(state, f, password);
			states.put(key, item);
		} else if (!item.password.equals(password)) {
			throw new IllegalArgumentException("Can't change password now.");
		}
		return item;
	}
	
	public boolean save() {
		boolean ok = true;
		for (GalleryUpdate item : states.values()) {
			if (item.modified) {
				try {
					LogFileWriter writer = new LogFileWriter(item.path);
					writer.write(item.state, item.password);
				} catch (IOException e) {
					System.out.println("invalid");
					ok = false;
				}
			}
		}
		return ok;
	}
	
}
