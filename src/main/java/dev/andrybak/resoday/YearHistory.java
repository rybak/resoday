package dev.andrybak.resoday;

import com.google.gson.JsonParseException;
import dev.andrybak.resoday.storage.HabitFiles;
import dev.andrybak.resoday.storage.SerializableYearHistory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

public class YearHistory {
	private static final DateTimeFormatter CALENDAR_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final Set<LocalDate> dates;
	private final List<YearHistoryListener> listeners = new ArrayList<>();
	private final String name;
	/**
	 * Whether or not this {@code YearHistory} has any changes since last {@linkplain #saveTo saving}.
	 *
	 * @implSpec All methods which modify {@link #dates} must set this flag to true.
	 */
	private boolean hasChanges = true;

	YearHistory(String name) {
		this(emptySet(), name);
	}

	YearHistory(Collection<LocalDate> dates, String name) {
		this.dates = new HashSet<>(dates);
		this.name = name;
	}

	YearHistory(SerializableYearHistory serializableYearHistory) {
		this(serializableYearHistory.getDates(), serializableYearHistory.getName());
	}

	static Optional<YearHistory> read(Path statePath) {
		if (!Files.exists(statePath)) {
			System.out.println("No saved state.");
			return Optional.of(new YearHistory(HabitFiles.pathToName(statePath)));
		}
		if (!Files.isRegularFile(statePath) || !Files.isReadable(statePath)) {
			System.err.println("Can't read '" + statePath + "' as file.");
			return Optional.empty();
		}
		try (BufferedReader r = Files.newBufferedReader(statePath)) {
			String name = HabitFiles.pathToName(statePath);
			SerializableYearHistory serializableYearHistory = SerializableYearHistory.fromJson(r, name);
			return Optional.of(new YearHistory(serializableYearHistory));
		} catch (IOException e) {
			System.err.println("Could not read '" + statePath.toAbsolutePath() + "': " + e);
			return Optional.empty();
		} catch (JsonParseException e) {
			// fallback to version 0 of storage
			return readV0(statePath);
		}
	}

	/**
	 * For backward compatibility
	 */
	private static Optional<YearHistory> readV0(Path statePath) {
		try (Stream<String> lines = Files.lines(statePath)) {
			List<LocalDate> dates = lines
				.map(line -> {
					try {
						return LocalDate.parse(line, CALENDAR_DAY_FORMATTER);
					} catch (DateTimeException | ArithmeticException e) {
						System.err.println("Could not read value: " + sanitize(line));
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(toList());
			return Optional.of(new YearHistory(dates, HabitFiles.pathToName(statePath)));
		} catch (IOException | UncheckedIOException e) {
			System.err.println("Could not read '" + statePath.toAbsolutePath() + "': " + e);
			return Optional.empty();
		}
	}

	private static String sanitize(String s) {
		if (s.length() < 20) {
			return s;
		}
		return s.substring(0, 20);
	}

	void turnOn(LocalDate d) {
		System.out.println("Turned on " + d);
		hasChanges = true;
		dates.add(d);
		listeners.forEach(l -> l.onTurnOn(d));
	}

	void turnOff(LocalDate d) {
		System.out.println("Turned off " + d);
		hasChanges = true;
		dates.remove(d);
		listeners.forEach(l -> l.onTurnOff(d));
	}

	boolean isTurnedOn(LocalDate d) {
		return dates.contains(d);
	}

	int size() {
		return dates.size();
	}

	IntStream years() {
		return dates.stream()
			.mapToInt(LocalDate::getYear)
			.distinct();
	}

	void saveTo(Path statePath) {
		if (!hasChanges) {
			return;
		}
		try {
			System.out.println("\tSaving to '" + statePath.toAbsolutePath() + "'...");
			Path tmpFile = Files.createTempFile(statePath.getParent(), "resoday", ".habit.tmp");
			SerializableYearHistory toSave = new SerializableYearHistory(dates, name);
			try (BufferedWriter w = Files.newBufferedWriter(tmpFile)) {
				toSave.writeToJson(w);
			}
			Files.move(tmpFile, statePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			System.out.printf("\tSaved %d dates.%n", size());
		} catch (IOException e) {
			System.err.println("Could not save current state in '" + statePath.toAbsolutePath() + "': " + e);
			e.printStackTrace();
		}
		hasChanges = false;
	}

	void addListener(YearHistoryListener listener) {
		listeners.add(listener);
	}

	String getName() {
		return name;
	}
}
