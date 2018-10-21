package com.gentics.mesh;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Various utility functions regarding streams
 */
final public class StreamUtil {
	/**
	 * Creates a stream out an iterable.
	 * @param iterable
	 * @param <T>
	 * @return
	 */
	public static <T> Stream<T> fromIterable(Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false);
	}

	/**
	 * Creates a stream out of an iterator.
	 * @param iterator
	 * @param <T>
	 * @return
	 */
	public static <T> Stream<T> fromIterator(Iterator<T> iterator) {
		return fromIterable(() -> iterator);
	}
}
