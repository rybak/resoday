package dev.andrybak.resoday;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;

class YearPanel extends JPanel {
	private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM");
	private static final DateTimeFormatter DAY_BUTTON_FORMATTER = DateTimeFormatter.ofPattern("dd");

	YearPanel(YearHistory history, Year year) {
		super(new BorderLayout());

		JPanel yearLabelWrapper = new JPanel(new BorderLayout());
		{
			JLabel yearLabel = new JLabel(year.toString(), SwingConstants.CENTER);
			yearLabel.setFont(yearLabel.getFont().deriveFont(yearLabel.getFont().getSize() * 2.0f));
			yearLabelWrapper.add(yearLabel, BorderLayout.CENTER);
		}
		this.add(yearLabelWrapper, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		for (Month m : Month.values()) {
			gbc.gridx = m.getValue() - 1;
			gbc.gridy = 0;
			buttonPanel.add(new JLabel(MONTH_LABEL_FORMATTER.format(m)), gbc);
		}
		LocalDate currentYearStart = LocalDate.ofYearDay(year.getValue(), 1);
		LocalDate nextYearStart = LocalDate.ofYearDay(year.plusYears(1).getValue(), 1);
		for (LocalDate i = currentYearStart; i.isBefore(nextYearStart); i = i.plusDays(1)) {
			final LocalDate d = i; // for final inside lambdas
			gbc.gridx = d.getMonthValue() - 1;
			gbc.gridy = d.getDayOfMonth();
			JToggleButton b = new JToggleButton(DAY_BUTTON_FORMATTER.format(d));
			b.setSelected(history.isTurnedOn(d));
			b.addActionListener(ignored -> {
				if (b.isSelected()) {
					System.out.println("Turned on " + d);
					history.turnOn(d);
				} else {
					System.out.println("Turned off " + d);
					history.turnOff(d);
				}
			});
			buttonPanel.add(b, gbc);
		}
		this.add(buttonPanel, BorderLayout.CENTER);
	}
}
