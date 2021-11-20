package dev.andrybak.resoday.storage;

import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Utilities for working with {@code .habit} files which store user data.
 */
public class HabitFiles {
	public static final String HABIT_FILE_EXT = ".habit";
	public static final String V0V1_HIDDEN_HABIT_FILE_EXT = ".habit.hidden";
	public static final Predicate<Path> IS_HABIT_FILE = p -> {
		String filename = p.getFileName().toString();
		return filename.endsWith(HABIT_FILE_EXT) || filename.endsWith(V0V1_HIDDEN_HABIT_FILE_EXT);
	};

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

	public static String createNewId() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	public static String createNewFilename(String id, String habitName) {
		habitName = habitName.trim();
		if (habitName.isEmpty()) {
			habitName = "unnamed";
		}
		return id + "-" + habitName.replaceAll("\\W+", "-") + HABIT_FILE_EXT;
	}

	public static boolean isV0V1HiddenFile(Path p) {
		return p.getFileName().toString().endsWith(V0V1_HIDDEN_HABIT_FILE_EXT);
	}

	public static Path fromV0Hidden(Path p) {
		String filename = p.getFileName().toString();
		if (!filename.endsWith(V0V1_HIDDEN_HABIT_FILE_EXT)) {
			return p;
		}
		filename = filename.substring(0, filename.length() - V0V1_HIDDEN_HABIT_FILE_EXT.length()) + HABIT_FILE_EXT;
		return p.resolveSibling(filename);
	}

	public static String v0v1PathToId(Path p) {
		String filename = p.getFileName().toString();
		if (!filename.contains(HABIT_FILE_EXT)) {
			throw new IllegalArgumentException("Unsupported filename: " + filename);
		}
		if (isV0V1HiddenFile(p)) {
			return filename.substring(0, filename.length() - V0V1_HIDDEN_HABIT_FILE_EXT.length());
		} else {
			return filename.substring(0, filename.length() - HABIT_FILE_EXT.length());
		}
	}
}
