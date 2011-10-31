package br.com.oncast.ontrack.shared.model.release;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ReleaseDescriptionParserTest {

	private static final String SEPARATOR = "/";
	private static final int TEST_LENGTH = 5;
	private static final char[] WHITE_CHARACTERS = { ' ', '\n', '\r', '\t' };
	private static final int VARIATIONS_LENGTH = 3 * WHITE_CHARACTERS.length + 1;

	@Test
	public void emptyStringShouldBeTheHeadOfABlankDescription() {
		for (final String description : getWhiteSpaceConcatenationVariationsOf("")) {
			assertEquals("", getHeadOfDescription(description));
		}
	}

	@Test
	public void emptyStringShouldBeTheHeadOfADescriptionContainningOnlyTheSeparatorAndWhiteSpaces() throws Exception {
		for (final String description : getWhiteSpaceConcatenationVariationsOf(SEPARATOR)) {
			assertEquals("", getHeadOfDescription(description));
		}
	}

	@Test
	public void theTrimOfTheReleaseShouldBeTheHeadOfADescriptionWithSingleRelease() {
		for (final String description : getWhiteSpaceConcatenationVariationsOf("R1")) {
			assertEquals("R1", getHeadOfDescription(description));
		}
	}

	@Test
	public void theHeadOfOtherSingleReleaseDescriptionIsTheTrimOfHimself() {
		for (final String description : getWhiteSpaceConcatenationVariationsOf("R2")) {
			assertEquals("R2", getHeadOfDescription(description));
		}
	}

	@Test
	public void theTrimOfTheSecoundReleaseShouldBeTheHeadWhenTheFirstReleaseIsInvalid() throws Exception {
		for (final String invalidFirstRelease : getWhiteSpaceConcatenationVariationsOf("")) {
			for (final String description : getWhiteSpaceConcatenationVariationsOf(invalidFirstRelease + SEPARATOR + "R2")) {
				assertEquals("R2", getHeadOfDescription(description));
			}
		}
	}

	@Test
	public void theFirstAndSecoundReleaseSeparatedByTheSeparatorShouldBeTheHeadWhenTheDescriptionHasTwoLevelsAndNextIsCalled() throws Exception {
		final ReleaseDescriptionParser parser = new ReleaseDescriptionParser("R1" + SEPARATOR + "R2");
		assertEquals("R1", parser.getHeadRelease());
		assertTrue(parser.next());
		assertEquals("R2", parser.getHeadRelease());
		assertEquals("R1" + SEPARATOR + "R2", parser.getFullDescriptionOfHeadRelease());
		for (int i = 0; i < TEST_LENGTH; i++) {
			assertFalse(parser.next());
			assertEquals("", parser.getHeadRelease());
		}
	}

	@Test
	public void headShoudIgnoreInvalidReleases() throws Exception {
		final ReleaseDescriptionParser parser = new ReleaseDescriptionParser("R1" + SEPARATOR + " " + SEPARATOR + "R2");
		assertEquals("R1", parser.getHeadRelease());
		assertTrue(parser.next());
		assertEquals("R2", parser.getHeadRelease());
		assertEquals("R1" + SEPARATOR + "R2", parser.getFullDescriptionOfHeadRelease());
		for (int i = 0; i < TEST_LENGTH; i++) {
			assertFalse(parser.next());
			assertEquals("", parser.getHeadRelease());
		}
	}

	@Test
	public void headShoudIgnoreTwoSeparatorWithNoTextBetween() throws Exception {
		final ReleaseDescriptionParser parser = new ReleaseDescriptionParser("R1" + SEPARATOR + SEPARATOR + "R2");
		assertEquals("R1", parser.getHeadRelease());
		assertTrue(parser.next());
		assertEquals("R2", parser.getHeadRelease());
		assertEquals("R1" + SEPARATOR + "R2", parser.getFullDescriptionOfHeadRelease());
		for (int i = 0; i < TEST_LENGTH; i++) {
			assertFalse(parser.next());
			assertEquals("", parser.getHeadRelease());
		}
	}

	@Test
	public void TheTrimOfTheFirstReleaseShouldBeTheHeadOfADescriptionWithMoreThanOneLevelOfReleases() {
		for (final String description : getWhiteSpaceConcatenationVariationsOf("R1" + SEPARATOR + "R2")) {
			assertEquals("R1", getHeadOfDescription(description));
		}
	}

	@Test
	public void theTailOfABlankDescriptionShouldBeAEmptyString() throws Exception {
		for (final String description : getWhiteSpaceConcatenationVariationsOf("")) {
			assertEquals("", getTailOfDescription(description));
		}
	}

	@Test
	public void theTailOfADescriptionContainningOnlyTheSeparatorAndWhiteSpacesShouldBeAEmptyString() throws Exception {
		for (final String description : getWhiteSpaceConcatenationVariationsOf(SEPARATOR)) {
			assertEquals("", getTailOfDescription(description));
		}
	}

	@Test
	public void tailOfADescriptionWithSingleReleaseShouldBeAnEmptyString() throws Exception {
		for (final String description : getWhiteSpaceConcatenationVariationsOf("R1")) {
			assertEquals("", getTailOfDescription(description));
		}
	}

	@Test
	public void theTailOfADescriptionWithTwoLevelsOfReleasesShouldBeTheTrimOfTheSecondRelease() throws Exception {
		for (final String secondRelease : getWhiteSpaceConcatenationVariationsOf("R2")) {
			assertEquals("R2", getTailOfDescription("R1" + SEPARATOR + secondRelease));
		}
	}

	@Test
	public void theTailOfADescriptionWithMoreThanOneLevelOfReleasesShouldBeATrimOfTheStringWithAllReleasesAfterTheFirstRelease() throws Exception {
		for (final String description : getWhiteSpaceConcatenationVariationsOf("R1/R2/R3")) {
			assertEquals("R2/R3", getTailOfDescription(description));
		}
	}

	@Test
	public void theNextTailOfADescriptionWithMoreThanOneLevelOfReleasesShouldBeATrimOfTheStringWithAllReleasesAfterTheSecoundRelease() throws Exception {
		for (final String description : getWhiteSpaceConcatenationVariationsOf("R1/R2/R3")) {
			final ReleaseDescriptionParser parser = new ReleaseDescriptionParser(description);
			assertEquals("R2/R3", parser.getTailReleases());
			parser.next();
			assertEquals("R3", parser.getTailReleases());
		}
	}

	@Test
	public void testingTheHeadAndTheTailOfAllPossibilityOfWhiteSpacesInADescriptionWithAtLeastOneLevelOfReleases() throws Exception {
		// init
		final int[] indexes = new int[TEST_LENGTH];
		final List<List<String>> releasesList = new ArrayList<List<String>>(TEST_LENGTH);
		for (int i = 0; i < TEST_LENGTH; i++) {
			indexes[i] = 0;
			releasesList.add(getWhiteSpaceConcatenationVariationsOf("R" + i));
		}

		while (indexes[TEST_LENGTH - 1] < VARIATIONS_LENGTH) {
			// mount description and the expected tail
			String description = releasesList.get(0).get(indexes[0]);
			String tail = "";
			for (int i = 1; i < TEST_LENGTH; i++) {
				description += SEPARATOR + releasesList.get(i).get(indexes[i]);
				tail += SEPARATOR + releasesList.get(i).get(indexes[i]);
			}
			tail = tail.substring(SEPARATOR.length()).trim();

			// test expectations
			final ReleaseDescriptionParser parser = new ReleaseDescriptionParser(description);
			assertEquals("R0", parser.getHeadRelease());
			assertEquals(tail, parser.getTailReleases());

			// increment indexes to get all combinations
			indexes[0]++;
			int i = 0;
			while (i < TEST_LENGTH - 1 && indexes[i] == VARIATIONS_LENGTH) {
				indexes[i++] = 0;
				indexes[i]++;
			}
		}
	}

	private String getTailOfDescription(final String description) {
		return new ReleaseDescriptionParser(description).getTailReleases();
	}

	private String getHeadOfDescription(final String description) {
		return new ReleaseDescriptionParser(description).getHeadRelease();
	}

	private List<String> getWhiteSpaceConcatenationVariationsOf(final String string) {
		final ArrayList<String> list = new ArrayList<String>();
		list.add(string);
		for (final char character : WHITE_CHARACTERS) {
			list.add(character + string);
			list.add(character + string + character);
			list.add(string + character);
		}
		return list;
	}
}