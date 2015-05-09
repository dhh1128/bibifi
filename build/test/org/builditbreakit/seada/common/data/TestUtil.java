package org.builditbreakit.seada.common.data;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TestUtil {
	/**
	 * Checks to make sure the readObject method cannot be used to inject
	 * malicious data, when used in conjunction with the Serialization Proxy
	 * pattern.
	 * 
	 * @param obj
	 *            an object to check
	 */
	public static void assertReadObjectFails(Serializable obj) {
		Class<?> clazz = obj.getClass();
		try {
			Method readObject = clazz.getDeclaredMethod("readObject",
					ObjectInputStream.class);
			readObject.setAccessible(true);
			readObject.invoke(obj, (ObjectInputStream) null);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof InvalidObjectException) {
				// Do nothing. This is the expected exception
			} else {
				throw new AssertionError(e);
			}
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
