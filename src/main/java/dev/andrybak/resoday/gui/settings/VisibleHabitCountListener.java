package dev.andrybak.resoday.gui.settings;

public interface VisibleHabitCountListener {
	/**
	 * This method is called whenever amount of visible habits (open tabs) changes.
	 */
	void consumeHabitCount(int habitCount);
}
