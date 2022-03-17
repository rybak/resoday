package dev.andrybak.resoday.gui.calendar;

import dev.andrybak.resoday.YearHistory;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

/**
 * Months go left to right, days go top to bottom in long 28–31-days-long columns.
 * Corresponds to <a href="http://www.simonegiertz.com/every-day-calendar">Every Day Calendar</a> by
 * Simone Giertz shown in <a href="https://www.youtube.com/watch?v=Pm9CQn07OjU&t=4m26s">Veritasium video titled
 * <i>Why Most Resolutions Fail &amp; How To Succeed</i></a>.
 */
public class ColumnsCalendarPanel implements CalendarPanel {
	private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM");
	private static final DateTimeFormatter DAY_BUTTON_FORMATTER = DateTimeFormatter.ofPattern("dd");
	private final JPanel buttonPanel;
	private final Map<LocalDate, JToggleButton> buttons = new HashMap<>();

	public ColumnsCalendarPanel(YearHistory history, Year year) {
		buttonPanel = new JPanel(new GridBagLayout());
		for (Month m : Month.values()) {
			buttonPanel.add(new JLabel(MONTH_LABEL_FORMATTER.format(m)), createMonthLabelConstraints(m));
		}
		LocalDate currentYearStart = LocalDate.ofYearDay(year.getValue(), 1);
		LocalDate nextYearStart = LocalDate.ofYearDay(year.plusYears(1).getValue(), 1);
		for (LocalDate i = currentYearStart; i.isBefore(nextYearStart); i = i.plusDays(1)) {
			final LocalDate d = i; // for final inside lambdas
			JToggleButton b = new JToggleButton(DAY_BUTTON_FORMATTER.format(d));
			buttons.put(d, b);
			b.setSelected(history.isTurnedOn(d));
			b.addActionListener(ignored -> {
				if (b.isSelected()) {
					history.turnOn(d);
				} else {
					history.turnOff(d);
				}
			});
			makeFontNonBold(b); // no highlight by default
			buttonPanel.add(b, createDateButtonConstraints(d));
		}
	}

	private static void makeFontBold(JComponent c) {
		updateFontStyle(c, style -> style | Font.BOLD);
	}

	private static void makeFontNonBold(JComponent c) {
		updateFontStyle(c, style -> style & ~Font.BOLD);
	}

	private static void updateFontStyle(JComponent c, IntUnaryOperator styleUpdate) {
		if (c == null) {
			return;
		}
		//noinspection MagicConstant
		c.setFont(c.getFont().deriveFont(styleUpdate.applyAsInt(c.getFont().getStyle())));
	}

	private static GridBagConstraints createMonthLabelConstraints(Month m) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = m.getValue() - 1;
		gbc.gridy = 0;
		return gbc;
	}

	private static GridBagConstraints createDateButtonConstraints(LocalDate d) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = d.getMonthValue() - 1;
		gbc.gridy = d.getDayOfMonth();
		return gbc;
	}

	@Override
	public JComponent getView() {
		return buttonPanel;
	}

	@Override
	public void turnOnButton(LocalDate d) {
		maybeButtonSetSelected(d, true);
	}

	@Override
	public void turnOffButton(LocalDate d) {
		maybeButtonSetSelected(d, false);
	}

	@Override
	public void addHighlight(LocalDate d) {
		maybeButton(d, ColumnsCalendarPanel::makeFontBold);
	}

	@Override
	public void removeHighlight(LocalDate d) {
		maybeButton(d, ColumnsCalendarPanel::makeFontNonBold);
	}

	private void maybeButtonSetSelected(LocalDate d, boolean selected) {
		maybeButton(d, b -> b.setSelected(selected));
	}

	private void maybeButton(LocalDate d, Consumer<JToggleButton> consumer) {
		JToggleButton maybeButton = buttons.get(d);
		if (maybeButton == null) {
			return;
		}
		consumer.accept(maybeButton);
	}
}
