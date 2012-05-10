package br.com.oncast.ontrack.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import br.com.oncast.ontrack.shared.utils.WorkingDay;
import br.com.oncast.ontrack.shared.utils.WorkingDayFactory;

public class WorkingDayTest {

	@Test
	public void todayIsBeforeFiveDaysFromNow() throws Exception {
		final WorkingDay today = WorkingDayFactory.create();

		assertTrue(today.isBefore(today.copy().add(5)));
	}

	@Test
	public void todayIsNotBeforeFiveDaysAgo() throws Exception {
		final WorkingDay today = WorkingDayFactory.create();

		assertFalse(today.isBefore(today.copy().add(-5)));
	}

	@Test
	public void todayIsBeforeTomorrow() throws Exception {
		final WorkingDay today = WorkingDayFactory.create();

		assertTrue(today.isBefore(today.copy().add(1)));
	}

	@Test
	public void todayIsNotBeforeToday() throws Exception {
		final WorkingDay today = WorkingDayFactory.create();

		assertFalse(today.isBefore(today.copy()));
	}

	@Test
	public void shouldConsiderDifferentMonthsInDatesComparation() throws Exception {
		final WorkingDay dayAtEndOfMonth = WorkingDayFactory.create(2011, Calendar.OCTOBER, 30);

		assertTrue(dayAtEndOfMonth.isBefore(dayAtEndOfMonth.copy().add(5)));
	}

	@Test
	public void theNumberOfDaysFromMondayToFridayIsFive() throws Exception {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		final WorkingDay monday = WorkingDayFactory.create(calendar.getTime());

		calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		assertEquals(5, monday.countTo(WorkingDayFactory.create(calendar.getTime())));
	}

	@Test
	public void theCountingOfTwoDatesShouldNotConsiderWeekends() throws Exception {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		final WorkingDay friday = WorkingDayFactory.create(calendar.getTime());

		calendar.add(Calendar.DAY_OF_MONTH, 3);

		assertEquals(2, friday.countTo(WorkingDayFactory.create(calendar.getTime())));
	}

	@Test
	public void twoWorkingDaysAfterFridayIsTuesday() throws Exception {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);

		final Date finalDate = WorkingDayFactory.create(calendar.getTime()).add(2).getJavaDate();
		calendar.setTime(finalDate);

		assertEquals(Calendar.TUESDAY, calendar.get(Calendar.DAY_OF_WEEK));
	}

	@Test
	public void oneWorkingDaysAfterTuesdayIsWednesday() throws Exception {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);

		final Date finalDate = WorkingDayFactory.create(calendar.getTime()).add(1).getJavaDate();
		calendar.setTime(finalDate);

		assertEquals(Calendar.WEDNESDAY, calendar.get(Calendar.DAY_OF_WEEK));
	}

	@Test
	public void shouldReturnNextMondayWhenNewWorkingDayIsCreatedOnSundayOrSaturday() throws Exception {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(2001, Calendar.MAY, 1);

		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		final int sunday = calendar.get(Calendar.DAY_OF_MONTH);

		calendar.setTime(WorkingDayFactory.create(calendar.getTime()).getJavaDate());

		assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK));
		assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH) - sunday);

		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		final int saturday = calendar.get(Calendar.DAY_OF_MONTH);

		calendar.setTime(WorkingDayFactory.create(calendar.getTime()).getJavaDate());

		assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK));
		System.out.println(calendar.get(Calendar.DAY_OF_MONTH) + " - " + saturday);
		assertEquals(2, calendar.get(Calendar.DAY_OF_MONTH) - saturday);
	}

	@Test
	public void compareToShouldReturnTheDifferenceBetweenDays() throws Exception {
		final WorkingDay day1 = WorkingDayFactory.create(2011, Calendar.OCTOBER, 17);
		final WorkingDay day2 = WorkingDayFactory.create(2011, Calendar.OCTOBER, 21);
		assertEquals(-1, day1.compareTo(day2));
	}
}
