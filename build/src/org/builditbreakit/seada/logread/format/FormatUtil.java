package org.builditbreakit.seada.logread.format;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

public class FormatUtil {
	public static final String NEWLINE = "\n";
	public static final String COMMA = ",";

	private FormatUtil() {
		super();
	}
	
	public static <T> String join(Collection<T> collection,
			String delimiter) {
		return join(new StringBuilder(), collection, delimiter)
				.toString();
	}

	public static <T> String join(Collection<T> collection,
			String delimiter, Predicate<? super T> predicate,
			Function<? super T, Object> formatter) {
		return join(new StringBuilder(), collection, delimiter,
				predicate, formatter).toString();
	}

	public static <T> StringBuilder join(StringBuilder strBuilder,
			Collection<T> collection, String delimiter) {
		return join(strBuilder, collection, delimiter, (item) -> true,
				Function.identity());
	}

	public static <T> StringBuilder join(StringBuilder strBuilder,
			Collection<T> collection, String delimiter,
			Predicate<? super T> predicate,
			Function<? super T, Object> formatter) {
		boolean first = true;
		for (T item : collection) {
			if (predicate.test(item)) {
				if (first) {
					first = false;
				} else {
					strBuilder.append(FormatUtil.COMMA);
				}
				strBuilder.append(formatter.apply(item));
			}
		}
		return strBuilder;
	}
}
