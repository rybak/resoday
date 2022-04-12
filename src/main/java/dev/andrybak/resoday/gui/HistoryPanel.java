package dev.andrybak.resoday.gui;

import dev.andrybak.resoday.YearHistory;
import dev.andrybak.resoday.YearHistoryListener;
import dev.andrybak.resoday.gui.settings.CalendarLayoutSettingProvider;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shows to the user {@link YearHistory habit histories}, with access to one year at a time.
 */
final class HistoryPanel extends JPanel {
	private static final Year currentYear = Year.now();

	private final YearHistory history;
	/**
	 * Shows to the user, which year is currently presented by {@link #shownYearPanel}.
	 */
	private final JLabel shownYearLabel;
	private Year shownYear;
	private YearPanel shownYearPanel;
	private final List<Runnable> listenerRemovals = new ArrayList<>();

	HistoryPanel(YearHistory history, CalendarLayoutSettingProvider calendarLayoutSettingProvider) {
		super(new BorderLayout());
		this.history = history;
		listenerRemovals.add(history.addListener(new AudioPlayer()));
		listenerRemovals.add(history.addListener(new ButtonStateUpkeep()));

		JButton pastButton = new JButton("<");
		pastButton.addActionListener(ignored -> {
			shownYear = shownYear.minusYears(1);
			recreateShownYearPanel(calendarLayoutSettingProvider);
		});

		JButton futureButton = new JButton(">");
		futureButton.addActionListener(ignored -> {
			shownYear = shownYear.plusYears(1);
			recreateShownYearPanel(calendarLayoutSettingProvider);
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
		createShownYearPanel(calendarLayoutSettingProvider);
	}

	private void createShownYearPanel(CalendarLayoutSettingProvider calendarLayoutSettingProvider) {
		shownYearLabel.setText(shownYear.toString());
		shownYearPanel = new YearPanel(history, shownYear, calendarLayoutSettingProvider);
		this.add(shownYearPanel, BorderLayout.CENTER);
	}

	private void recreateShownYearPanel(CalendarLayoutSettingProvider calendarLayoutSettingProvider) {
		this.remove(shownYearPanel);
		createShownYearPanel(calendarLayoutSettingProvider);
		this.revalidate();
		this.repaint();
	}

	void hideHistory() {
		history.setVisibility(YearHistory.Visibility.HIDDEN);
	}

	void markToday() {
		LocalDate today = LocalDate.now();
		if (history.isTurnedOn(today)) {
			history.turnOff(today);
		} else {
			history.turnOn(today);
		}
	}

	/**
	 * Update UI decorations (bells and whistles) of this panel, which may depend on current time.
	 */
	void updateDecorations() {
		shownYearPanel.updateDecorations();
	}

	String getHistoryName() {
		return history.getName();
	}

	String getHistoryId() {
		return history.getId();
	}

	/**
	 * Must be called when this panel is no longer in use.
	 */
	void close() {
		listenerRemovals.forEach(Runnable::run);
	}

	public void newSettings(CalendarLayoutSettingProvider calendarLayoutSettingProvider) {
		recreateShownYearPanel(calendarLayoutSettingProvider);
	}

	private class ButtonStateUpkeep implements YearHistoryListener {
		@Override
		public void onTurnOn(LocalDate d) {
			shownYearPanel.turnOnButton(d);
		}

		@Override
		public void onTurnOff(LocalDate d) {
			shownYearPanel.turnOffButton(d);
		}
	}
}
