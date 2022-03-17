package dev.andrybak.resoday.gui.calendar;

import javax.swing.JComponent;
import java.time.LocalDate;

/**
 * A {@link JComponent} which has buttons or other components for each calendar year, which are toggleable between
 * being "on" and "off".
 */
public interface CalendarPanel {
	/**
	 * @return {@link JComponent} which contains the buttons (or their equivalent)
	 */
	JComponent getView();

	/**
	 * Switch the button (or equivalent), that corresponds to given {@link LocalDate}, to "on" status.
	 */
	void turnOnButton(LocalDate d);

	/**
	 * Switch the button (or equivalent), that corresponds to given {@link LocalDate}, to "off" status.
	 */
	void turnOffButton(LocalDate d);

	/**
	 * Highlight the given {@link LocalDate} visually. All buttons (or equivalent) should be without a highlight by
	 * default. Optional.
	 */
	void addHighlight(LocalDate d);

	/**
	 * Remove the highlight which corresponds to given {@link LocalDate}.
	 * Optional.
	 *
	 * @param d should be a date, which was previously passed to {@link #addHighlight(LocalDate)}
	 */
	void removeHighlight(LocalDate d);
}
