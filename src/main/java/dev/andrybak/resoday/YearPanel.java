package dev.andrybak.resoday;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

class YearPanel extends JPanel {
	private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM");
	private static final DateTimeFormatter DAY_BUTTON_FORMATTER = DateTimeFormatter.ofPattern("dd");

	private final Map<LocalDate, JToggleButton> buttons = new HashMap<>();
	private final YearHistory history;

	YearPanel(YearHistory history, Year year) {
		super(new BorderLayout());
		this.history = history;

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		for (Month m : Month.values()) {
			gbc.gridx = m.getValue() - 1;
			gbc.gridy = 0;
			buttonPanel.add(new JLabel(MONTH_LABEL_FORMATTER.format(m)), gbc);
		}
		LocalDate currentYearStart = LocalDate.ofYearDay(year.getValue(), 1);
		LocalDate nextYearStart = LocalDate.ofYearDay(year.plusYears(1).getValue(), 1);
		LocalDate today = LocalDate.now();
		for (LocalDate i = currentYearStart; i.isBefore(nextYearStart); i = i.plusDays(1)) {
			final LocalDate d = i; // for final inside lambdas
			gbc.gridx = d.getMonthValue() - 1;
			gbc.gridy = d.getDayOfMonth();
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
			buttonPanel.add(b, gbc);
			if (d.equals(today)) {
				makeFontBold(b);
			} else {
				makeFontNonBold(b);
			}
		}
		this.add(buttonPanel, BorderLayout.CENTER);
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

	void updateButtonToggle(LocalDate d) {
		JToggleButton maybeButton = buttons.get(d);
		if (maybeButton == null) { // date might be from a different year
			return;
		}
		if (maybeButton.isSelected() == history.isTurnedOn(d)) {
			return;
		}
		maybeButton.setSelected(history.isTurnedOn(d));
	}

	/**
	 * Update decorations (bells and whistles) of this panel, which may depend on current time.
	 */
	void updateDecorations() {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		makeFontBold(buttons.get(today));
		makeFontNonBold(buttons.get(yesterday));
	}
}
