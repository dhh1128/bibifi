package org.builditbreakit.seada.logread.format;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class FormatUtil {
	public static final String NEWLINE = "\n";
	public static final String COMMA = ",";

	private static BiConsumer<StringBuilder, Object> appendingBiConsumer = new BiConsumer<StringBuilder, Object>() {
		@Override
		public void accept(StringBuilder strBuilder, Object item) {
			strBuilder.append(item);
		}
	};
	private static Predicate<Object> truePredicate = new Predicate<Object>() {
		@Override
		public boolean test(Object t) {
			return true;
		}
	};

	private FormatUtil() {
		super();
	}

	public static <T> String join(Collection<T> collection, String delimiter) {
		return join(collection, delimiter, truePredicate, appendingBiConsumer)
				.toString();
	}

	public static <T> String join(Collection<T> collection, String delimiter,
			BiConsumer<StringBuilder, ? super T> formatter) {
		StringBuilder strBuilder = new StringBuilder();
		return join(strBuilder, collection, delimiter, truePredicate, formatter)
				.toString();
	}

	public static <T> StringBuilder join(StringBuilder strBuilder,
			Collection<T> collection, String delimiter) {
		return join(strBuilder, collection, delimiter, truePredicate,
				appendingBiConsumer);
	}

	public static <T> String join(Collection<T> collection, String delimiter,
			Predicate<? super T> predicate) {
		return join(collection, delimiter, predicate, appendingBiConsumer);
	}

	public static <T> String join(Collection<T> collection, String delimiter,
			Predicate<? super T> predicate,
			BiConsumer<StringBuilder, ? super T> formatter) {
		StringBuilder strBuilder = new StringBuilder();
		return join(strBuilder, collection, delimiter, predicate, formatter)
				.toString();
	}

	public static <T> StringBuilder join(StringBuilder strBuilder,
			Collection<T> collection, String delimiter,
			Predicate<? super T> predicate) {
		return join(strBuilder, collection, delimiter, predicate,
				appendingBiConsumer);
	}

	public static <T> StringBuilder join(StringBuilder strBuilder,
			Collection<T> collection, String delimiter,
			Predicate<? super T> predicate,
			BiConsumer<StringBuilder, ? super T> formatter) {

		boolean first = true;
		for (T item : collection) {
			if (predicate.test(item)) {
				if (first) {
					first = false;
				} else {
					strBuilder.append(delimiter);
				}
				formatter.accept(strBuilder, item);
			}
		}

		return strBuilder;
	}
}
