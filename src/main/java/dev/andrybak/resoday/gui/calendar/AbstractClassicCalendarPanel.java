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
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * "Classic" layouts of calendars. Days go horizontally left to right arranged per week in groups of seven, weeks go top
 * to bottom in four to six rows, depending on the month and on the day of the week of the first day of the month.
 */
abstract class AbstractClassicCalendarPanel extends AbstractToggleButtonCalendarPanel implements CalendarPanel {
	private static final DateTimeFormatter MONTH_NAME_FORMATTER = DateTimeFormatter.ofPattern("MMMM");
	private static final int MONTH_GRID_WIDTH = 8; // 7 for each day of a week + 1 for gaps
	private static final int MONTH_GRID_HEIGHT = 9; // 6==max number of rows + 1 month label + 1 week label + 1 gap
	private static final int GRID_GAP_PIXELS = 20;

	public AbstractClassicCalendarPanel(YearHistory history, Year year) {
		super(new JPanel(new GridBagLayout()), history, year);
		if (getNumberOfColumns() < 1 || getNumberOfColumns() > 12) {
			/* The way methods `getLeft` and `getTop` are used for gaps calculation, this class cannot be used for
			 * 1×12 and 12×1 layouts. */
			throw new IllegalArgumentException("numberOfColumns should be in [1-12] range. Got: " +
				getNumberOfColumns());
		}
		if (getNumberOfColumns() > 1) {
			// Vertical gaps between columns
			getAfterVerticalGapMonths().forEach(m -> {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = getLeft(m) - 1; // gap is to the left of columns of the given months
				gbc.gridy = getTop(m);
				buttonPanel.add(Box.createHorizontalStrut(GRID_GAP_PIXELS), gbc);
			});
		}
		if (getNumberOfColumns() != 12) {
			// Horizontal gaps between rows
			getBelowHorizontalGapMonths().forEach(m -> {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = getLeft(m);
				gbc.gridy = getTop(m) - 1; // gap is to above the rows of given months
				buttonPanel.add(Box.createVerticalStrut(GRID_GAP_PIXELS), gbc);
			});
		}
		for (Month m : Month.values()) {
			for (DayOfWeek dow : DayOfWeek.values()) {
				GridBagConstraints gbc = createDayOfWeekLabelConstraints(m, dow);
				String name = dow.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault());
				buttonPanel.add(new JLabel(name), gbc);
			}
		}
	}

	/**
	 * @implNote number of columns has to be a method and not a parameter in constructor, because constructor of
	 * {@link AbstractToggleButtonCalendarPanel} calls to {@link #getLeft(Month)} and {@link #getTop(Month)}.
	 */
	protected abstract int getNumberOfColumns();

	/**
	 * @return months of the first row, except January
	 */
	private Stream<Month> getAfterVerticalGapMonths() {
		return IntStream.range(2, getNumberOfColumns() + 1)
			.mapToObj(Month::of);
	}

	/**
	 * @return months of the first column, except January
	 */
	private Stream<Month> getBelowHorizontalGapMonths() {
		return IntStream.iterate(
			Month.of(getNumberOfColumns() + 1).getValue(),
			i -> i <= 12,
			i -> i + getNumberOfColumns()
		).mapToObj(Month::of);
	}

	/**
	 * @return x coordinate of top left corner of given month in {@link GridBagConstraints} terms
	 */
	private int getLeft(Month m) {
		return ((m.getValue() - 1) % getNumberOfColumns()) * MONTH_GRID_WIDTH;
	}

	/**
	 * @return y coordinate of top left corner of given month in {@link GridBagConstraints} terms
	 */
	private int getTop(Month m) {
		return ((m.getValue() - 1) / getNumberOfColumns()) * MONTH_GRID_HEIGHT;
	}

	@Override
	protected final DateTimeFormatter getMonthLabelFormatter() {
		return MONTH_NAME_FORMATTER;
	}

	@Override
	protected final GridBagConstraints createMonthLabelConstraints(Month m) {
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
	protected final GridBagConstraints createDateButtonConstraints(LocalDate d) {
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
