package br.com.oncast.ontrack.utils.assertions;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import br.com.oncast.ontrack.shared.model.effort.Effort;
import br.com.oncast.ontrack.shared.model.scope.Scope;

public class AssertTestUtils {

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
		assertEquals(message, expected.getBottomUpValue(), actual.getBottomUpValue(), 0.09);
		assertEquals(message, expected.getTopDownValue(), actual.getTopDownValue(), 0.09);
		assertEquals(message, expected.hasDeclared(), actual.hasDeclared());
		assertEquals(message, expected.getDeclared(), actual.getDeclared());
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
		assertEquals(expected.size(), actual.size());
		assertTrue(actual.containsAll(expected));
	}
}
