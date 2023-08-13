package dev.andrybak.resoday.gui.settings;

import dev.andrybak.resoday.settings.gui.HabitCalendarLayout;

import java.util.Optional;

public interface HabitCalendarLayoutsOwner {
	/**
	 * May return an empty {@link Optional} if there are no tabs open.
	 */
	Optional<HabitCalendarLayout> getCurrentTabLayout();

	void addVisibleHabitCountListener(VisibleHabitCountListener listener);

	void addVisibleHabitChangedListener(VisibleHabitChangedListener listener);

	void acceptNewCurrentHabitLayout(HabitCalendarLayout habitCalendarLayout);

	/**
	 * Must be called exactly once.
	 */
	void addDefaultLayoutChangeListener(DefaultLayoutChangeListener listener);
}
