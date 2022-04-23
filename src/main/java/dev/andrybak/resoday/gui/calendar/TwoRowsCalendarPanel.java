package dev.andrybak.resoday.gui.calendar;

import dev.andrybak.resoday.YearHistory;

import java.time.Year;

/**
 * <pre>
 *     January February March     April   May      June
 *     July    August   September October November December
 * </pre>
 */
public final class TwoRowsCalendarPanel extends AbstractClassicCalendarPanel {
	public TwoRowsCalendarPanel(YearHistory history, Year year) {
		super(history, year);
	}

	@Override
	protected int getNumberOfColumns() {
		return 6;
	}
}
