package org.builditbreakit.seada.logappend;

import java.io.File;

import org.builditbreakit.seada.common.data.GalleryState;


/**
 * The path where a particular gallery state is persisted, and the password
 * used to encrypt it, are not part of its internal data. However, we need
 * a way to connect gallery state and these external pieces of info so we
 * can track which log files we modify during a logappend batch. This class
 * provides the association.
 */
public class GalleryUpdate {
	
	public final GalleryState state;
	public final File path;
	public final String password;
	public boolean modified;
	
	public GalleryUpdate(GalleryState _state, File _path, String _password) {
		state = _state;
		path = _path;
		password = _password;
		modified = false;
	}

}
