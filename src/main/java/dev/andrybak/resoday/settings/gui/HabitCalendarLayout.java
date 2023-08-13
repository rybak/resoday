package dev.andrybak.resoday.settings.gui;

import java.util.Optional;

/**
 * Layout of the calendar for an individual habit.
 * Stored as part of the data, rather than config.
 */
public enum HabitCalendarLayout {
	/**
	 * Setting that falls back to the default layout configured in the {@link GuiSettings}.
	 * See {@link GuiSettings#getButtonLayoutSetting()}.
	 */
	DEFAULT {
		@Override
		public Optional<CalendarLayoutSetting> toSetting() {
			return Optional.empty();
		}
	},
	COLUMNS {
		@Override
		public Optional<CalendarLayoutSetting> toSetting() {
			return Optional.of(CalendarLayoutSetting.COLUMNS_SIMONE_GIERTZ);
		}
	},
	CLASSIC_VERTICAL {
		@Override
		public Optional<CalendarLayoutSetting> toSetting() {
			return Optional.of(CalendarLayoutSetting.CLASSIC_VERTICAL);
		}
	},
	CLASSIC_HORIZONTAL {
		@Override
		public Optional<CalendarLayoutSetting> toSetting() {
			return Optional.of(CalendarLayoutSetting.CLASSIC_HORIZONTAL);
		}
	},
	;

	public abstract Optional<CalendarLayoutSetting> toSetting();

	public String getGuiName() {
		return toSetting().map(CalendarLayoutSetting::getGuiName).orElse("Default");
	}
}
