package dev.andrybak.resoday.gui.calendar;

import dev.andrybak.resoday.YearHistory;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * "Classic" vertical layout of calendars. Days go horizontally left to right, weeks go top to bottom.
 * <pre>
 *     January February March
 *     April   May      June
 *     July    August   September
 *     October November December
 * </pre>
 */
public final class VerticalCalendarPanel extends AbstractToggleButtonCalendarPanel implements CalendarPanel {
	private static final DateTimeFormatter MONTH_NAME_FORMATTER = DateTimeFormatter.ofPattern("MMMM");
	private static final int MONTH_GRID_WIDTH = 8; // 7 for each day of a week + 1 for gaps
	private static final int MONTH_GRID_HEIGHT = 9; // 6==max number of rows + 1 month label + 1 week label + 1 gap
	private static final int GRID_GAP_PIXELS = 20;

	public VerticalCalendarPanel(YearHistory history, Year year) {
		super(new JPanel(new GridBagLayout()), history, year);
		// Vertical gaps between columns
		Stream.of(Month.FEBRUARY, Month.MARCH).forEach(m -> {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = getLeft(m) - 1; // to the left of columns of February and March
			gbc.gridy = getTop(m);
			buttonPanel.add(Box.createHorizontalStrut(GRID_GAP_PIXELS), gbc);
		});
		// Horizontal gaps between rows
		Stream.of(Month.APRIL, Month.JULY, Month.OCTOBER).forEach(m -> {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = getLeft(m);
			gbc.gridy = getTop(m) - 1; // above rows of April, July, and October
			buttonPanel.add(Box.createVerticalStrut(GRID_GAP_PIXELS), gbc);
		});
		for (Month m : Month.values()) {
			for (DayOfWeek dow : DayOfWeek.values()) {
				GridBagConstraints gbc = createDayOfWeekLabelConstraints(m, dow);
				String name = dow.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault());
				buttonPanel.add(new JLabel(name), gbc);
			}
		}
	}

	private static int getLeft(Month m) {
		return ((m.getValue() - 1) % 3) * MONTH_GRID_WIDTH;
	}

	private static int getTop(Month m) {
		return ((m.getValue() - 1) / 3) * MONTH_GRID_HEIGHT;
	}

	@Override
	protected DateTimeFormatter getMonthLabelFormatter() {
		return MONTH_NAME_FORMATTER;
	}

	@Override
	protected GridBagConstraints createMonthLabelConstraints(Month m) {
		GridBagConstraints gbc = new GridBagConstraints();
		final int left = getLeft(m);
		final int top = getTop(m);
		gbc.gridx = left;
		gbc.gridy = top;
		gbc.gridwidth = 7; // Month label will be centered along the week columns
		return gbc;
	}

	private GridBagConstraints createDayOfWeekLabelConstraints(Month m, DayOfWeek dow) {
		GridBagConstraints gbc = new GridBagConstraints();
		final int left = getLeft(m) + dow.getValue() - 1; // minus 1 because getValue() returns 1 for Monday
		final int top = getTop(m) + 1; // +1 == below month label
		gbc.gridx = left;
		gbc.gridy = top;
		return gbc;
	}

	@Override
	protected GridBagConstraints createDateButtonConstraints(LocalDate d) {
		final int columnInMonth = d.getDayOfWeek().getValue() - 1;
		LocalDate firstRowMonday = d.withDayOfMonth(1);
		while (firstRowMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
			firstRowMonday = firstRowMonday.minusDays(1);
		}
		final int daysSinceFirstMonday = (int)firstRowMonday.until(d, ChronoUnit.DAYS);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = getLeft(d.getMonth()) + columnInMonth;
		// +2 == one for month label, one for week labels
		gbc.gridy = getTop(d.getMonth()) + daysSinceFirstMonday / 7 + 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		return gbc;
	}
}
