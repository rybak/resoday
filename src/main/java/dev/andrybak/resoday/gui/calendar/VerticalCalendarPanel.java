package dev.andrybak.resoday.gui.calendar;

import dev.andrybak.resoday.YearHistory;

import java.time.Year;

/**
 * "Classic" vertical layout of calendars. Days go horizontally left to right, weeks go top to bottom.
 * <pre>
 *     January February March
 *     April   May      June
 *     July    August   September
 *     October November December
 * </pre>
 */
public final class VerticalCalendarPanel extends AbstractClassicCalendarPanel implements CalendarPanel {
	public static final int NUMBER_OF_COLUMNS = 3;

	public VerticalCalendarPanel(YearHistory history, Year year) {
		super(history, year);
	}

	protected int getNumberOfColumns() {
		return NUMBER_OF_COLUMNS;
	}
}
