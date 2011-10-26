package br.com.oncast.ontrack.client.ui.components.releasepanel.widgets;

import static junit.framework.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import br.com.oncast.ontrack.mocks.models.ScopeTestUtils;
import br.com.oncast.ontrack.shared.model.progress.Progress;
import br.com.oncast.ontrack.shared.model.progress.Progress.ProgressState;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.release.ReleaseEstimator;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.utils.WorkingDay;
import br.com.oncast.ontrack.shared.utils.WorkingDayFactory;

import com.ibm.icu.util.Calendar;

public class ReleaseChartDataProviderTest {

	private ReleaseEstimator estimatorMock;
	private Release releaseMock;

	private WorkingDay estimatedEndDay;
	private WorkingDay estimatedStartDay;
	private Float releaseEffortSum;
	private static List<Scope> releaseScopes;

	@Before
	public void setup() {
		releaseMock = Mockito.mock(Release.class);
		releaseEffortSum = 0f;
		releaseScopes = new ArrayList<Scope>();
		setupReleaseMock();

		estimatorMock = Mockito.mock(ReleaseEstimator.class);
		estimatedStartDay = WorkingDayFactory.create();
		estimatedEndDay = WorkingDayFactory.create();
		setupEstimatorMock();
	}

	@After
	public void verifyMocks() {
		Mockito.verify(estimatorMock, Mockito.atLeastOnce()).getEstimatedEndDayFor(releaseMock);
		Mockito.verify(estimatorMock, Mockito.atLeastOnce()).getEstimatedStartDayFor(releaseMock);
		Mockito.verify(releaseMock, Mockito.atLeastOnce()).getAllScopesIncludingChildrenReleases();
	}

	@Test
	public void releaseDaysShouldHaveOnlyTheEstimatedDayWhenEstimatedStartDayAndEstimatedEndDayAreEqual() throws Exception {
		for (int i = 0; i < 10; i++) {
			estimatedStartDay.add(i);
			estimatedEndDay.add(i);
			assertEquals(estimatedStartDay, estimatedEndDay);
			assertReleaseDays(estimatedEndDay);
		}
	}

	@Test
	public void releaseDaysShouldStartOnReleaseStartDay() throws Exception {
		assertEquals(estimatedStartDay.getDayAndMonthString(), getProvider().getReleaseDays().get(0));
	}

	@Test
	public void releaseDaysShouldEndOnEstimatedEndDay() throws Exception {
		final List<String> releaseDays = getProvider().getReleaseDays();
		final int lastIndex = releaseDays.size() - 1;
		assertEquals(estimatedEndDay.getDayAndMonthString(), releaseDays.get(lastIndex));
	}

	@Test
	public void releaseDaysShouldContainAllDaysFromStartDayToEndDayInOrder() throws Exception {
		estimatedStartDay = WorkingDayFactory.create(2011, Calendar.JANUARY, 3);
		estimatedEndDay = WorkingDayFactory.create(2011, Calendar.JANUARY, 5);
		assertReleaseDays("03/01", "04/01", "05/01");
	}

	@Test
	public void getEffortSumShouldReturnEffortSumOfTheRelease() throws Exception {
		for (int i = 0; i < 20; i++) {
			releaseEffortSum = (float) i;
			assertEquals(releaseEffortSum, getProvider().getEffortSum());
		}
		Mockito.verify(releaseMock, Mockito.times(20)).getEffortSum();
	}

	@Test
	public void accomplishedEffortByDateShouldHaveOnlyZeroWhenReleaseEffortSumIsZero() throws Exception {
		assertAccomplishedEffortsByDate(0);
		estimatedStartDay = WorkingDayFactory.create(2011, Calendar.JANUARY, 3);
		setReleaseDuration(3);
		assertAccomplishedEffortsByDate(0, 0, 0);
	}

	@Test
	public void shouldNotHaveAccomplishedEffortAfterToday() throws Exception {
		setReleaseDuration(20);
		Accomplish.effortPoints(5).today();
		Accomplish.effortPoints(13).on(WorkingDayFactory.create().add(1));

		assertAccomplishedEffortsByDate(5);
	}

	private void setReleaseDuration(final int nDays) {
		estimatedEndDay = estimatedStartDay.copy().add(nDays - 1);
	}

	private void assertAccomplishedEffortsByDate(final float... efforts) {
		final List<Float> list = getProvider().getAccomplishedEffortsByDate();
		assertEquals(efforts.length, list.size());
		for (int i = 0; i < efforts.length; i++) {
			assertEquals(efforts[i], list.get(i));
		}
	}

	private void assertReleaseDays(final WorkingDay... days) {
		final List<String> list = getProvider().getReleaseDays();
		assertEquals(days.length, list.size());
		for (int i = 0; i < days.length; i++) {
			assertEquals(days[i].getDayAndMonthString(), list.get(i));
		}

	}

	private void assertReleaseDays(final String... days) {
		final List<String> list = getProvider().getReleaseDays();
		assertEquals(days.length, list.size());
		for (int i = 0; i < days.length; i++) {
			assertEquals(days[i], list.get(i));
		}

	}

	private void setupEstimatorMock() {
		Mockito.when(estimatorMock.getEstimatedStartDayFor(releaseMock)).thenAnswer(new Answer<WorkingDay>() {

			@Override
			public WorkingDay answer(final InvocationOnMock invocation) throws Throwable {
				return estimatedStartDay.copy();
			}
		});
		Mockito.when(estimatorMock.getEstimatedEndDayFor(releaseMock)).thenAnswer(new Answer<WorkingDay>() {

			@Override
			public WorkingDay answer(final InvocationOnMock invocation) throws Throwable {
				return estimatedEndDay.copy();
			}
		});
	}

	private void setupReleaseMock() {
		Mockito.when(releaseMock.getEffortSum()).thenAnswer(new Answer<Float>() {

			@Override
			public Float answer(final InvocationOnMock invocation) throws Throwable {
				return releaseEffortSum;
			}
		});
		Mockito.when(releaseMock.getAllScopesIncludingChildrenReleases()).thenAnswer(new Answer<List<Scope>>() {

			@Override
			public List<Scope> answer(final InvocationOnMock invocation) throws Throwable {
				return releaseScopes;
			}
		});
	}

	private ReleaseChartDataProvider getProvider() {
		return new ReleaseChartDataProvider(releaseMock, estimatorMock);
	}

	private static class Accomplish {

		private final Scope scope;

		public Accomplish(final int effort) {
			scope = createScope(ProgressState.DONE, effort);
			releaseScopes.add(scope);
		}

		public void today() {}

		public static Accomplish effortPoints(final int effort) {
			return new Accomplish(effort);
		}

		public void on(final WorkingDay workingDay) throws Exception {
			setEndDay(scope, workingDay);
		}

		private static Scope createScope(final ProgressState progress, final int effort) {
			final Scope scope = new Scope("Scope " + effort);
			ScopeTestUtils.setProgress(scope, progress);
			ScopeTestUtils.setDelcaredEffort(scope, effort);
			return scope;
		}

		private static void setEndDay(final Scope scope, final WorkingDay endDate) throws NoSuchFieldException, IllegalAccessException {
			final Field endDateField = Progress.class.getDeclaredField("endDate");
			endDateField.setAccessible(true);
			endDateField.set(scope.getProgress(), endDate);
		}

	}
}