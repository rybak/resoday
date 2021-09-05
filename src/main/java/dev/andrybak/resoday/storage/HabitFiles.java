package dev.andrybak.resoday.storage;

import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Predicate;

public class HabitFiles {
	public static final String HABIT_FILE_EXT = ".habit";
	public static final Predicate<Path> IS_HABIT_FILE = p -> p.getFileName().toString().endsWith(HABIT_FILE_EXT);

	private HabitFiles() {
		throw new UnsupportedOperationException();
	}

	/**
	 * In version 0 storage, filenames were used for habit names.
	 */
	public static String pathToName(Path p) {
		String fn = p.getFileName().toString();
		return fn.substring(0, fn.length() - HABIT_FILE_EXT.length());
	}

	public static String createNewFilename(String habitName) {
		habitName = habitName.trim();
		if (habitName.isEmpty()) {
			habitName = "unnamed";
		}
		UUID uuid = UUID.randomUUID();
		return uuid + "-" + habitName.replaceAll("\\W+", "-") + HABIT_FILE_EXT;
	}
}
