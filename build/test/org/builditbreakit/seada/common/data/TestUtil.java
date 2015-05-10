package org.builditbreakit.seada.common.data;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

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

	/**
	 * Tests if a class overrides the {@link Object#equals(Object)} and
	 * {@link Object#hashCode()} methods.
	 * 
	 * @param clazz
	 *            the class to test
	 * @return {@code true} if the class uses the default methods; {@code false}
	 *         if the class overrides at least one of these methods
	 */
	public static boolean usesDefaultEquals(Class<?> clazz) {
		try {
			Method equalsMethod = clazz.getMethod("equals", Object.class);
			if (!equalsMethod.getDeclaringClass().equals(Object.class)) {
				return false;
			}

			Method hashCodeMethod = clazz.getMethod("hashCode");
			return hashCodeMethod.getDeclaringClass().equals(Object.class);
		} catch (NoSuchMethodException e) {
			// Should not happen
			throw new RuntimeException("Unexpected exception", e);
		}
	}

	/**
	 * Tests the serialization and deserialization of an object.
	 * 
	 * @param object
	 *            the object to serialize
	 * @param verifier
	 *            a consumer that accepts the deserialized object and tests it
	 * @throws IOException
	 *             if serialization or deserialization fails
	 * @throws ClassNotFoundException
	 *             if the deserialized object's class cannot be found
	 */
	public static <T> void testSerialization(T object, Consumer<T> verifier)
			throws IOException, ClassNotFoundException {
		byte[] serializedObject;

		try (ByteArrayOutputStream os = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(os)) {
			out.writeObject(object);
			serializedObject = os.toByteArray();
		}

		try (ObjectInputStream in = new ObjectInputStream(
				new ByteArrayInputStream(serializedObject))) {
			@SuppressWarnings("unchecked")
			T result = (T) in.readObject();
			verifier.accept(result);
		}
	}
	
	static String buildDebugMessage(String currentMessage, String additionalMessage) {
		if ((currentMessage != null) && !currentMessage.equals("")) {
			return currentMessage + " | " + additionalMessage;
		} else {
			return additionalMessage;
		}
	}
	
	
	static void assertOneOfNotNull(Object expected, Object actual) {
		assertOneOfNotNull(null, expected, actual);
	}
	
	static void assertOneOfNotNull(String message, Object expected, Object actual) {
		if(expected == actual) {
			return;
		}
		if((expected == null) || (actual == null)) {
			assertEquals(message, expected, actual);
		}
	}

	static <T> void assertEquivalentHelper(String message, T expected,
			T actual, Consumer<Void> equivalenceChecker) {
		if ((expected == null) || (actual == null)) {
			assertEquals(expected, actual);
		}

		if ((message != null) && !message.equals("")) {
			message = message + " | at ";
		}

		equivalenceChecker.accept(null);
	}
}
