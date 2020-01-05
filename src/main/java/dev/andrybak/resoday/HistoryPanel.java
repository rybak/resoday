package dev.andrybak.resoday;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;

class HistoryPanel extends JPanel {
	private static final Year currentYear = Year.now();

	private final YearHistory history;
	private final Path statePath;
	private Year shownYear;
	private YearPanel shownYearPanel;

	HistoryPanel(Path statePath) {
		super(new BorderLayout());
		this.statePath = statePath;
		history = YearHistory.read(this.statePath);
		history.addListener(new AudioPlayer());
		history.addListener(new ButtonStateUpkeep());
		System.out.println("Read " + history.size() + " dates.");

		JButton pastButton = new JButton("<");
		pastButton.addActionListener(ignored -> {
			shownYear = shownYear.minusYears(1);
			recreateShownYearPanel();
		});
		setArrowButtonWidth(pastButton);
		this.add(pastButton, BorderLayout.WEST);

		JButton futureButton = new JButton(">");
		futureButton.addActionListener(ignored -> {
			shownYear = shownYear.plusYears(1);
			recreateShownYearPanel();
		});
		setArrowButtonWidth(futureButton);
		this.add(futureButton, BorderLayout.EAST);

		shownYear = history.years()
			.boxed()
			.min(Comparator.comparingInt(y -> Math.abs(currentYear.getValue() - y)))
			.map(Year::of)
			.orElse(currentYear);
		createShownYearPanel();
	}

	private void createShownYearPanel() {
		shownYearPanel = new YearPanel(history, shownYear);
		this.add(shownYearPanel, BorderLayout.CENTER);
	}

	private void recreateShownYearPanel() {
		this.remove(shownYearPanel);
		createShownYearPanel();
		this.revalidate();
		this.repaint();
	}

	private void setArrowButtonWidth(JButton b) {
		Dimension size = new Dimension(
			(int) new JLabel("XXXXXXXX").getPreferredSize().getWidth(),
			Short.MAX_VALUE
		);
		b.setPreferredSize(size);
		b.setMinimumSize(size);
	}

	void save() {
		System.out.println("Saving '" + statePath + "'...");
		history.saveTo(statePath);
	}

	Path getPath() {
		return statePath;
	}

	void markToday() {
		LocalDate today = LocalDate.now();
		if (history.isTurnedOn(today)) {
			history.turnOff(today);
		} else {
			history.turnOn(today);
		}
	}

	private class ButtonStateUpkeep implements YearHistoryListener {
		@Override
		public void onTurnOn(LocalDate d) {
			updateButton(d);
		}

		@Override
		public void onTurnOff(LocalDate d) {
			updateButton(d);
		}

		private void updateButton(LocalDate d) {
			shownYearPanel.updateButtonToggle(d);
		}
	}
}
