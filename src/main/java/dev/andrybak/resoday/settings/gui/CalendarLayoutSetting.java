package dev.andrybak.resoday.settings.gui;

import dev.andrybak.resoday.YearHistory;
import dev.andrybak.resoday.gui.calendar.CalendarPanel;
import dev.andrybak.resoday.gui.calendar.ColumnsCalendarPanel;
import dev.andrybak.resoday.gui.calendar.HorizontalCalendarPanel;
import dev.andrybak.resoday.gui.calendar.VerticalCalendarPanel;

import java.time.Year;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A layout of the calendar of a tab. Stored in config as a default layout for all habits
 * that have {@link HabitCalendarLayout#DEFAULT} set as their habit.
 */
public enum CalendarLayoutSetting {
	COLUMNS_SIMONE_GIERTZ("Columns", ColumnsCalendarPanel::new),
	CLASSIC_VERTICAL("Vertical", VerticalCalendarPanel::new),
	CLASSIC_HORIZONTAL("Horizontal", HorizontalCalendarPanel::new),
	;

	private final String guiName;
	private final BiFunction<YearHistory, Year, CalendarPanel> calendarPanelSupplier;

	CalendarLayoutSetting(String guiName, BiFunction<YearHistory, Year, CalendarPanel> calendarPanelCreator) {
		this.guiName = guiName;
		this.calendarPanelSupplier = Objects.requireNonNull(calendarPanelCreator);
	}

	public String getGuiName() {
		return guiName;
	}

	public final CalendarPanel createButtonLayout(YearHistory history, Year year) {
		return calendarPanelSupplier.apply(history, year);
	}
}
