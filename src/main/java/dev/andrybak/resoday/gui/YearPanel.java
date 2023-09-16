package dev.andrybak.resoday.gui;

import dev.andrybak.resoday.YearHistory;
import dev.andrybak.resoday.gui.calendar.CalendarPanel;
import dev.andrybak.resoday.gui.settings.CalendarLayoutSettingProvider;
import dev.andrybak.resoday.gui.util.ScrollPanes;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.Year;

final class YearPanel extends JPanel {
	private final CalendarPanel calendarPanel;

	YearPanel(YearHistory history, Year year, CalendarLayoutSettingProvider calendarLayoutSettingProvider) {
		super(new BorderLayout());
		calendarPanel = calendarLayoutSettingProvider.getCalendarLayoutSetting().createButtonLayout(history, year);
		calendarPanel.addHighlight(LocalDate.now());
		JScrollPane scrollableCalendarView = ScrollPanes.regular(calendarPanel.getView());
		scrollableCalendarView.setBorder(BorderFactory.createEmptyBorder());
		this.add(scrollableCalendarView, BorderLayout.CENTER);
	}

	void turnOnButton(LocalDate d) {
		calendarPanel.turnOnButton(d);
	}

	void turnOffButton(LocalDate d) {
		calendarPanel.turnOffButton(d);
	}

	/**
	 * Update decorations (bells and whistles) of this panel, which may depend on current time.
	 */
	void updateDecorations() {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		calendarPanel.addHighlight(today);
		calendarPanel.removeHighlight(yesterday);
	}
}
