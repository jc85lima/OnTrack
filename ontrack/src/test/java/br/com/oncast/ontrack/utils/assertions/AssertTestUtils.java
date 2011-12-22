package br.com.oncast.ontrack.utils.assertions;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import br.com.oncast.ontrack.shared.model.effort.Effort;
import br.com.oncast.ontrack.shared.model.scope.Scope;

public class AssertTestUtils {

	private static final double EFFORT_TOLERANCE = 0.09;

	public static void assertDeepEquals(final Scope expected, final Scope actual) {
		assertEquality(expected, actual);
		for (int i = 0; i < actual.getChildren().size(); i++) {
			assertDeepEquals(expected.getChild(i), actual.getChild(i));
		}
	}

	private static void assertEquality(final Scope expected, final Scope actual) {
		assertTrue("Checking equality of scope '" + actual.getDescription() + "'.", actual.getDescription().equals(expected.getDescription()));
		assertEquality("Checking equality of effort of scope '" + actual.getDescription() + "'.", expected.getEffort(), actual.getEffort());
	}

	public static void assertEquality(final String message, final Effort expected, final Effort actual) {
		assertEquals(message, expected.getBottomUpValue(), actual.getBottomUpValue(), EFFORT_TOLERANCE);
		assertEquals(message, expected.getTopDownValue(), actual.getTopDownValue(), EFFORT_TOLERANCE);
		assertEquals(message, expected.hasDeclared(), actual.hasDeclared());
		assertEquals(message, expected.getDeclared(), actual.getDeclared(), EFFORT_TOLERANCE);
	}

	public static void assertNotEquals(final Object expected, final Object actual) {
		assertFalse(expected.equals(actual));
	}

	public static <T> void assertNotContains(final T unexpected, final Collection<T> actual) {
		assertFalse(actual.contains(unexpected));
	}

	public static <T> void assertContainsNone(final Collection<T> unexpected, final Collection<T> actual) {
		for (final T t : unexpected) {
			assertFalse(actual.contains(t));
		}
	}

	public static <T> void assertCollectionEquality(final Collection<T> expected, final Collection<T> actual) {
		assertEquals("The size of the given collections are different.", expected.size(), actual.size());
		final Iterator<T> expectedIterator = expected.iterator();
		final Iterator<T> actualIterator = actual.iterator();
		while (expectedIterator.hasNext()) {
			assertEquals(expectedIterator.next(), actualIterator.next());
		}
	}
}
