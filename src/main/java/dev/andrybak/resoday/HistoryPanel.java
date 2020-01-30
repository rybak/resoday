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
	/**
	 * Shows to the user, which year is currently presented by {@link #shownYearPanel}.
	 */
	private JLabel shownYearLabel;
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

		JButton futureButton = new JButton(">");
		futureButton.addActionListener(ignored -> {
			shownYear = shownYear.plusYears(1);
			recreateShownYearPanel();
		});

		shownYearLabel = new JLabel("", SwingConstants.CENTER);
		shownYearLabel.setFont(shownYearLabel.getFont().deriveFont(shownYearLabel.getFont().getSize() * 2.0f));

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		topPanel.add(pastButton);
		topPanel.add(shownYearLabel);
		topPanel.add(futureButton);
		this.add(topPanel, BorderLayout.NORTH);

		shownYear = history.years()
			.boxed()
			.min(Comparator.comparingInt(y -> Math.abs(currentYear.getValue() - y)))
			.map(Year::of)
			.orElse(currentYear);
		createShownYearPanel();
	}

	private void createShownYearPanel() {
		shownYearLabel.setText(shownYear.toString());
		shownYearPanel = new YearPanel(history, shownYear);
		this.add(shownYearPanel, BorderLayout.CENTER);
	}

	private void recreateShownYearPanel() {
		this.remove(shownYearPanel);
		createShownYearPanel();
		this.revalidate();
		this.repaint();
	}

	void save() {
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
