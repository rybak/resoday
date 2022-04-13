package dev.andrybak.resoday.gui.calendar;

import dev.andrybak.resoday.YearHistory;

import java.time.Year;

/**
 * "Classic" horizontal layout of calendars. Days go horizontally left to right, weeks go top to bottom.
 * <pre>
 *     January   February March    April
 *     May       June     July     August
 *     September October  November December
 * </pre>
 */
public final class HorizontalCalendarPanel extends AbstractClassicCalendarPanel {
	private static final int NUMBER_OF_COLUMNS = 4;

	public HorizontalCalendarPanel(YearHistory history, Year year) {
		super(history, year);
	}

	@Override
	protected int getNumberOfColumns() {
		return NUMBER_OF_COLUMNS;
	}
}
