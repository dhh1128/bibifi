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
		GalleryUpdate item = null;
		File f = new File(cmd.getLogfile());
		String password = cmd.getToken();
		// Convert to canonical version of path so different paths to the same logfile
		// are resolved to the same thing.
		String key = f.getCanonicalPath();
		if (!states.containsKey(key)) {
			LogFileReader reader = new LogFileReader(new File(key));
			GalleryState state = reader.read(password);
			item = new GalleryUpdate(state, key, password);
			states.put(key, item);
		} else {
			item = states.get(key);
			if (!item.password.equals(password)) {
				throw new IllegalArgumentException("Can't change password now.");
			}
		}
		return item;
	}
	
	public void save() {
		for (GalleryUpdate item: states.values()) {
			if (item.modified) {
				try {
					File f = new File(item.path);
					LogFileWriter writer = new LogFileWriter(f);
					writer.write(item.state, item.password);
				} catch (IOException e) {
					System.out.println("invalid");
				}
			}
		}
	}
	
}
