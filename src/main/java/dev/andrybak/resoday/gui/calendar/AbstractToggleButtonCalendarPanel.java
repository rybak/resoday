package dev.andrybak.resoday.gui.calendar;

import dev.andrybak.resoday.YearHistory;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.Font;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

abstract class AbstractToggleButtonCalendarPanel implements CalendarPanel {
	private static final DateTimeFormatter DAY_BUTTON_FORMATTER = DateTimeFormatter.ofPattern("dd");
	protected final JPanel buttonPanel;
	private final Map<LocalDate, JToggleButton> buttons = new HashMap<>();

	protected AbstractToggleButtonCalendarPanel(JPanel buttonPanel, YearHistory history, Year year) {
		this.buttonPanel = buttonPanel;
		for (Month m : Month.values()) {
			buttonPanel.add(new JLabel(getMonthLabelFormatter().format(m)), createMonthLabelConstraints(m));
		}
		LocalDate currentYearStart = LocalDate.ofYearDay(year.getValue(), 1);
		LocalDate nextYearStart = LocalDate.ofYearDay(year.plusYears(1).getValue(), 1);
		for (LocalDate i = currentYearStart; i.isBefore(nextYearStart); i = i.plusDays(1)) {
			final LocalDate d = i; // for final inside lambdas
			JToggleButton b = new JToggleButton(DAY_BUTTON_FORMATTER.format(d));
			buttons.put(Objects.requireNonNull(d), Objects.requireNonNull(b));
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

	@Override
	public final JComponent getView() {
		return buttonPanel;
	}

	@Override
	public final void turnOnButton(LocalDate d) {
		maybeButtonSetSelected(d, true);
	}

	@Override
	public final void turnOffButton(LocalDate d) {
		maybeButtonSetSelected(d, false);
	}

	@Override
	public final void addHighlight(LocalDate d) {
		maybeButton(d, AbstractToggleButtonCalendarPanel::makeFontBold);
	}

	@Override
	public final void removeHighlight(LocalDate d) {
		maybeButton(d, AbstractToggleButtonCalendarPanel::makeFontNonBold);
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

	protected abstract DateTimeFormatter getMonthLabelFormatter();

	protected abstract Object createMonthLabelConstraints(Month m);

	protected abstract Object createDateButtonConstraints(LocalDate d);
}
