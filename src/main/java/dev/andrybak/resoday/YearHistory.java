package dev.andrybak.resoday;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toList;

public class YearHistory {
	private static final DateTimeFormatter CALENDAR_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final Set<LocalDate> dates;
	private final List<YearHistoryListener> listeners = new ArrayList<>();
	/**
	 * Whether or not this {@code YearHistory} has any changes since last {@linkplain #saveTo saving}.
	 *
	 * @implSpec All methods which modify {@link #dates} must set this flag to true.
	 */
	private boolean hasChanges = true;

	YearHistory() {
		this(emptySet());
	}

	YearHistory(Collection<LocalDate> dates) {
		this.dates = new HashSet<>(dates);
	}

	static YearHistory read(Path statePath) {
		if (!Files.exists(statePath)) {
			System.out.println("No saved state.");
			return new YearHistory();
		}
		try (Stream<String> lines = Files.lines(statePath)) {
			List<LocalDate> dates = lines
				.map(line -> {
					try {
						TemporalAccessor t = CALENDAR_DAY_FORMATTER.parse(line);
						return LocalDate.of(
							t.get(ChronoField.YEAR),
							t.get(ChronoField.MONTH_OF_YEAR),
							t.get(ChronoField.DAY_OF_MONTH)
						);
					} catch (DateTimeException | ArithmeticException e) {
						System.err.println("Could not read value: " + sanitize(line));
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(toList());
			return new YearHistory(dates);
		} catch (IOException e) {
			System.err.println("Could not read " + statePath);
			return new YearHistory();
		}
	}

	private static String sanitize(String s) {
		if (s.length() < 20)
			return s;
		return s.substring(0, 20);
	}

	void turnOn(LocalDate d) {
		hasChanges = true;
		dates.add(d);
		listeners.forEach(l -> l.onTurnOn(d));
	}

	void turnOff(LocalDate d) {
		hasChanges = true;
		dates.remove(d);
		listeners.forEach(l -> l.onTurnOff(d));
	}

	boolean isTurnedOn(LocalDate d) {
		return dates.contains(d);
	}

	Collection<LocalDate> serialize() {
		return unmodifiableCollection(dates);
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
		if (!hasChanges)
			return;
		try {
			System.out.println("\tSaving to '" + statePath + "'...");
			Path tmpFile = Files.createTempFile(statePath.getParent(), "resoday", ".habit.tmp");
			Files.write(tmpFile,
				serialize().stream()
					.map(CALENDAR_DAY_FORMATTER::format)
					.collect(toList())
			);
			Files.move(tmpFile, statePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			System.out.printf("\tSaved %d dates.%n", size());
		} catch (IOException e) {
			System.err.println("Could not save current state in '" + statePath + "'.");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		hasChanges = false;
	}

	void addListener(YearHistoryListener listener) {
		listeners.add(listener);
	}
}
