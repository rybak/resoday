package dev.andrybak.resoday.storage;

import java.nio.file.Path;
import java.util.function.Predicate;

public class HabitFiles {
	public static final String HABIT_FILE_EXT = ".habit";
	public static final Predicate<Path> IS_HABIT_FILE = p -> p.getFileName().toString().endsWith(HABIT_FILE_EXT);

	private HabitFiles() {
	}

	/**
	 * In version 0 storage, filenames were used for habit names.
	 */
	public static String pathToName(Path p) {
		String fn = p.getFileName().toString();
		return fn.substring(0, fn.length() - HABIT_FILE_EXT.length());
	}
}
