package org.builditbreakit.seada.logappend;

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
	public final String path;
	public final String password;
	public boolean modified;
	
	public GalleryUpdate(GalleryState _state, String _path, String _password) {
		state = _state;
		path = _path;
		password = _password;
		modified = false;
	}
	
	@Override
	public boolean equals(Object o) {
		// The whole point of the GalleryUpdateManager class is to guarantee that
		// we only create one GalleryUpdate object for each path that is physically
		// unique. As such, the path for a GalleryUpdate always uniquely identifies
		// it, so delegating equals() to our .path member is adequate. We could do
		// more (testing all the other members as well); that would make our class
		// fit for other uses besides bibifi. However, it would also make us slower,
		// and this code doesn't need to be genericized to enhance its long-term
		// usefulness--so we're sticking with "good enough" for now.
		return path.equals(((GalleryUpdate)o).path);
	}
	
	@Override
	public int hashCode() {
		// Delegating to path.hashCode() is adequate for our purposes by the same
		// logic documented in the comment in .equals().
		return path.hashCode();
	}
}
