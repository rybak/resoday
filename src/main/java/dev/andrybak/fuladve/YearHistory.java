package dev.andrybak.fuladve;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toList;

public class YearHistory {
	private static final DateTimeFormatter CALENDAR_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final Set<LocalDate> dates;

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
		try {
			List<String> lines = Files.readAllLines(statePath);
			System.out.println("\tRead " + lines.size() + " lines...");
			if (lines.isEmpty())
				return new YearHistory();
			List<LocalDate> dates = new ArrayList<>();
			for (String line : lines) {
				try {
					TemporalAccessor t = CALENDAR_DAY_FORMATTER.parse(line);
					LocalDate candidate = LocalDate.of(
						t.get(ChronoField.YEAR),
						t.get(ChronoField.MONTH_OF_YEAR),
						t.get(ChronoField.DAY_OF_MONTH)
					);
					dates.add(candidate);
				} catch (DateTimeException | ArithmeticException e) {
					System.err.println("Could not read value: " + sanitize(line));
				}
			}
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
		dates.add(d);
	}

	void turnOff(LocalDate d) {
		dates.remove(d);
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
		try {
			Files.write(statePath,
				serialize().stream()
					.map(CALENDAR_DAY_FORMATTER::format)
					.collect(toList())
			);
			System.out.println("Saved state.");
			System.out.println(size() + " dates.");
		} catch (IOException e) {
			System.err.println("Could not save current state in '" + statePath + "'.");
		}
	}
}
