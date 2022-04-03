package dev.andrybak.resoday.gui.calendar;

import dev.andrybak.resoday.YearHistory;

import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;

/**
 * Months go left to right, days go top to bottom in long 28–31-days-long columns.
 * Corresponds to <a href="http://www.simonegiertz.com/every-day-calendar">Every Day Calendar</a> by
 * Simone Giertz shown in <a href="https://www.youtube.com/watch?v=Pm9CQn07OjU&t=4m26s">Veritasium video titled
 * <i>Why Most Resolutions Fail &amp; How To Succeed</i></a>.
 */
public final class ColumnsCalendarPanel extends AbstractToggleButtonCalendarPanel implements CalendarPanel {
	private static final DateTimeFormatter MONTH_ABBREVIATION_FORMATTER = DateTimeFormatter.ofPattern("MMM");

	public ColumnsCalendarPanel(YearHistory history, Year year) {
		super(new JPanel(new GridBagLayout()), history, year);
	}

	@Override
	protected DateTimeFormatter getMonthLabelFormatter() {
		return MONTH_ABBREVIATION_FORMATTER;
	}

	@Override
	protected GridBagConstraints createMonthLabelConstraints(Month m) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = m.getValue() - 1;
		gbc.gridy = 0;
		return gbc;
	}

	@Override
	protected GridBagConstraints createDateButtonConstraints(LocalDate d) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = d.getMonthValue() - 1;
		gbc.gridy = d.getDayOfMonth();
		return gbc;
	}
}
