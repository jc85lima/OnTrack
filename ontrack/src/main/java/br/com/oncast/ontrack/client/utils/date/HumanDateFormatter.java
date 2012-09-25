package br.com.oncast.ontrack.client.utils.date;

import static br.com.oncast.ontrack.client.utils.date.DateUnit.DAY;
import static br.com.oncast.ontrack.client.utils.date.DateUnit.HOUR;
import static br.com.oncast.ontrack.client.utils.date.DateUnit.MINUTE;
import static br.com.oncast.ontrack.client.utils.date.DateUnit.MONTH;
import static br.com.oncast.ontrack.client.utils.date.DateUnit.WEEK;
import static br.com.oncast.ontrack.client.utils.date.DateUnit.YEAR;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

public enum HumanDateFormatter {
	JUST_NOW(1 * MINUTE, "yyyyMMddHHmm") {
		@Override
		protected String formatDifferenceTime(final long difference) {
			return messages.lessThanAMinuteAgo();
		}

		@Override
		protected String formatRelativeTime(final Date date) {
			return messages.justNow();
		}
	},
	MINUTES(1 * HOUR, "yyyyMMddHH") {
		@Override
		protected String formatDifferenceTime(final long difference) {
			return mountDifferenceText(difference, MINUTE, messages.minute(), messages.minutes());
		}

		@Override
		protected String formatRelativeTime(final Date date) {
			return format("h:mm a", date);
		}
	},
	HOURS(1 * DAY, "yyyyMMdd") {
		@Override
		protected String formatDifferenceTime(final long difference) {
			return mountDifferenceText(difference, HOUR, messages.hour(), messages.hours());
		}

		@Override
		protected String formatRelativeTime(final Date date) {
			return format("h:mm a", date);
		}
	},
	DAYS(1 * WEEK, "yyyyMM") {
		@Override
		protected String formatDifferenceTime(final long difference) {
			return mountDifferenceText(difference, DAY, messages.day(), messages.days());
		}

		@Override
		protected String formatRelativeTime(final Date date) {
			return format("E, d", date);
		}
	},
	WEEKS(1 * MONTH, "yyyyMM") {
		@Override
		protected String formatDifferenceTime(final long difference) {
			return mountDifferenceText(difference, WEEK, messages.week(), messages.weeks());
		}

		@Override
		protected String formatRelativeTime(final Date date) {
			return format("E, d", date);
		}
	},
	MONTHS(1 * YEAR, "yyyy") {
		@Override
		protected String formatDifferenceTime(final long difference) {
			return mountDifferenceText(difference, MONTH, messages.month(), messages.months());
		}

		@Override
		protected String formatRelativeTime(final Date date) {
			return format("MMM, d", date);
		}
	},
	YEARS(Long.MAX_VALUE, "G") {
		@Override
		protected String formatDifferenceTime(final long difference) {
			return mountDifferenceText(difference, YEAR, messages.year(), messages.years());
		}

		@Override
		protected String formatRelativeTime(final Date date) {
			return format("MMM, yyyy", date);
		}
	};

	private static final HumanDateFormatterMessages messages = GWT.create(HumanDateFormatterMessages.class);

	private final long maxTimeDifference;
	private DateTimeFormat format;

	private HumanDateFormatter(final long maxTimeDifference, final String pattern) {
		this.maxTimeDifference = maxTimeDifference;
		this.format = DateTimeFormat.getFormat(pattern);
	}

	public static String getDifferenceDate(final Date date) {
		final long difference = new Date().getTime() - date.getTime();
		for (final HumanDateFormatter formatter : values()) {
			if (formatter.maxTimeDifference > difference) { return formatter.formatDifferenceTime(difference); }
		}
		return getAbsoluteText(date);
	}

	public static String getRelativeDate(final Date date) {
		final Date currentDate = new Date();
		for (final HumanDateFormatter formatter : values()) {
			if (formatter.accepts(currentDate, date)) { return formatter.formatRelativeTime(date); }
		}
		return getAbsoluteText(date);
	}

	public static String getAbsoluteText(final Date date) {
		return format("EEE, dd/MM/yyyy '" + messages.at() + "' hh:mm:ss", date);
	}

	protected abstract String formatDifferenceTime(final long difference);

	protected abstract String formatRelativeTime(final Date date);

	protected static String format(final String pattern, final Date date) {
		return DateTimeFormat.getFormat(pattern).format(date);
	}

	protected String mountDifferenceText(final long difference, final long delimiter, final String singular, final String plural) {
		final int time = (int) (difference / delimiter);
		return time + " " + (time <= 1 ? singular : plural) + " " + messages.ago();
	}

	private boolean accepts(final Date currentDate, final Date date) {
		return format.format(currentDate).equals(format.format(date));
	}

	public static String getShortAbsuluteDate(final Date date) {
		return format("dd/MM/yy", date);
	}

}