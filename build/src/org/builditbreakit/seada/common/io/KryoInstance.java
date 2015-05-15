package org.builditbreakit.seada.common.io;

import org.builditbreakit.seada.common.data.GalleryState;
import org.builditbreakit.seada.common.data.LocationRecord;
import org.builditbreakit.seada.common.data.Visitor;

import com.esotericsoftware.kryo.Kryo;

public final class KryoInstance {
	private static boolean initialized = false;
	private static final Kryo KRYO = new Kryo();

	private KryoInstance() {
		super();
	}

	/** Not thread safe */
	public static Kryo getInstance() {
		init();
		return KRYO;
	}

	private static void init() {
		if (!initialized) {
			KRYO.register(GalleryState.class, GalleryState.getSerializer());
			KRYO.register(Visitor.class, Visitor.getSerializer());
			KRYO.register(LocationRecord.class, LocationRecord.getSerializer());

			initialized = true;
		}
	}
}
