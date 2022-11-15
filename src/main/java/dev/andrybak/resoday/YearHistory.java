package dev.andrybak.resoday;

import com.google.gson.JsonParseException;
import dev.andrybak.resoday.gui.settings.DataDirSupplier;
import dev.andrybak.resoday.storage.HabitFiles;
import dev.andrybak.resoday.storage.SerializableYearHistory;
import dev.andrybak.resoday.storage.SerializableYearHistoryV1;

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
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

/**
 * Main business logic class. Keeps track of which dates are turned on and which are turned off on the calendar.
 */
public final class YearHistory {
	private static final DateTimeFormatter CALENDAR_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final Set<LocalDate> dates;
	private final List<YearHistoryListener> listeners = new ArrayList<>();
	private final String id;
	/**
	 * Gives the parent directory for where to store the habit file.
	 */
	private final DataDirSupplier dataDirSupplier;
	/**
	 * User-visible name of this habit history.
	 */
	private String name;
	/**
	 * Where this history is stored in serialized form.
	 */
	private Path relativeStatePath;
	/**
	 * Whether this history is visible in the GUI.
	 */
	private Visibility visibility;
	/**
	 * Whether this {@code YearHistory} has any changes since last {@linkplain #save saving}.
	 *
	 * @implSpec All methods which modify {@link #dates} must set this flag to true.
	 */
	private boolean hasChanges = true;

	public YearHistory(DataDirSupplier dataDirSupplier, Path relativeStatePath, String name, String id) {
		this(dataDirSupplier, relativeStatePath, emptySet(), name, id, Visibility.VISIBLE);
	}

	private YearHistory(DataDirSupplier dataDirSupplier, Path relativeStatePath, Collection<LocalDate> dates,
		String name, String id, Visibility visibility)
	{
		this.dataDirSupplier = dataDirSupplier;
		this.relativeStatePath = relativeStatePath;
		this.dates = new HashSet<>(dates);
		this.name = name;
		this.id = id;
		this.visibility = visibility;
	}

	private YearHistory(DataDirSupplier dataDirSupplier, Path relativeStatePath,
		SerializableYearHistory serializableYearHistory)
	{
		this(dataDirSupplier, relativeStatePath, serializableYearHistory.getDates(),
			serializableYearHistory.getName(), serializableYearHistory.getId(),
			serializableYearHistory.getVisibility());
	}

	/**
	 * Read a habit file of any text-based version. Currently:
	 * <ul>
	 *     <li>version 0: plain text {@link #readV0(DataDirSupplier, Path)}</li>
	 *     <li>version 1: JSON see {@link SerializableYearHistoryV1}</li>
	 *     <li>version 2: JSON see {@link SerializableYearHistory}</li>
	 * </ul>
	 */
	public static Optional<YearHistory> read(DataDirSupplier dataDirSupplier, Path statePath) {
		Path relativeStatePath = dataDirSupplier.getDataDir().relativize(statePath);
		if (!Files.exists(statePath)) {
			System.out.println("No saved state.");
			return Optional.of(new YearHistory(dataDirSupplier, relativeStatePath, HabitFiles.pathToName(statePath),
				HabitFiles.v0v1PathToId(statePath)));
		}
		if (!Files.isRegularFile(statePath) || !Files.isReadable(statePath)) {
			System.err.println("Can't read '" + statePath + "' as file.");
			return Optional.empty();
		}
		YearHistory tmp;
		final boolean isV0V1HiddenFile = HabitFiles.isV0V1HiddenFile(statePath);
		try (BufferedReader r = Files.newBufferedReader(statePath)) {
			String name = HabitFiles.pathToName(statePath);
			SerializableYearHistory serializableYearHistory = SerializableYearHistory.fromJson(r, name, statePath);
			tmp = new YearHistory(dataDirSupplier, relativeStatePath, serializableYearHistory);
		} catch (IOException e) {
			System.err.println("Could not read '" + statePath.toAbsolutePath() + "': " + e);
			return Optional.empty();
		} catch (JsonParseException e) {
			// fallback to version 0 of storage
			return readV0(dataDirSupplier, statePath);
		}
		if (isV0V1HiddenFile) {
			tmp.reHideV0V1File();
		}
		return Optional.of(tmp);
	}

	/**
	 * For backward compatibility
	 */
	private static Optional<YearHistory> readV0(DataDirSupplier dataDirSupplier, Path statePath) {
		YearHistory tmp;
		final boolean isV0V1HiddenFile = HabitFiles.isV0V1HiddenFile(statePath);
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
			tmp = new YearHistory(dataDirSupplier,
				dataDirSupplier.getDataDir().relativize(statePath),
				dates,
				HabitFiles.pathToName(statePath),
				HabitFiles.v0v1PathToId(statePath),
				isV0V1HiddenFile ? Visibility.HIDDEN : Visibility.VISIBLE
			);
		} catch (IOException | UncheckedIOException e) {
			System.err.println("Could not read '" + statePath.toAbsolutePath() + "': " + e);
			return Optional.empty();
		}
		if (isV0V1HiddenFile) {
			tmp.reHideV0V1File();
		}
		return Optional.of(tmp);
	}

	private static String sanitize(String s) {
		if (s.length() < 20) {
			return s;
		}
		return s.substring(0, 20);
	}

	private Path getStatePath() {
		return dataDirSupplier.getDataDir().resolve(relativeStatePath);
	}

	public void turnOn(LocalDate d) {
		System.out.println("Turned on " + d);
		hasChanges = true;
		dates.add(d);
		listeners.forEach(l -> l.onTurnOn(d));
	}

	public void turnOff(LocalDate d) {
		System.out.println("Turned off " + d);
		hasChanges = true;
		dates.remove(d);
		listeners.forEach(l -> l.onTurnOff(d));
	}

	public boolean isTurnedOn(LocalDate d) {
		return dates.contains(d);
	}

	public IntStream years() {
		return dates.stream()
			.mapToInt(LocalDate::getYear)
			.distinct();
	}

	public void forceSave() {
		hasChanges = true;
		save();
	}

	public void save() {
		if (!hasChanges) {
			return;
		}
		Path statePath = getStatePath();
		try {
			System.out.println("\tSaving to '" + statePath.toAbsolutePath() + "'...");
			Path tmpFile = Files.createTempFile(statePath.getParent(), "resoday", ".habit.tmp");
			SerializableYearHistory toSave = new SerializableYearHistory(dates, name, id, visibility);
			try (BufferedWriter w = Files.newBufferedWriter(tmpFile)) {
				toSave.writeToJson(w);
			}
			Files.move(tmpFile, statePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			System.out.printf("\tSaved %d dates for habit '%s'.%n", dates.size(), getName());
		} catch (IOException e) {
			System.err.println("Could not save current state in '" + statePath.toAbsolutePath() + "': " + e);
			e.printStackTrace();
		}
		hasChanges = false;
	}

	public void reHideV0V1File() {
		final Path originalHiddenPath = getStatePath();
		System.out.println("Re-hiding '" + originalHiddenPath.toAbsolutePath() + "'...");
		Path newPath = HabitFiles.fromV0Hidden(originalHiddenPath);
		if (newPath.equals(originalHiddenPath)) {
			System.err.println("Warning: trying to re-hide '" + originalHiddenPath.toAbsolutePath() + "'");
			return;
		}
		try {
			Files.move(originalHiddenPath, newPath);
			relativeStatePath = dataDirSupplier.getDataDir().relativize(newPath);
			setVisibility(Visibility.HIDDEN); // it was .hidden before, so override whatever was deserialized
			System.out.println("Moved '" + originalHiddenPath.toAbsolutePath() +
				"' to '" + newPath.toAbsolutePath() + "'");
		} catch (IOException e) {
			System.err.println("Could not move '" + originalHiddenPath.toAbsolutePath() +
				"' to '" + newPath.toAbsolutePath() + "': " + e);
		}
	}

	public void delete() {
		try {
			Files.delete(getStatePath());
		} catch (IOException e) {
			System.err.println("Could not delete '" + getStatePath().toAbsolutePath() + "': " + e);
			e.printStackTrace();
		}
	}

	public Runnable addListener(YearHistoryListener listener) {
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	public String getName() {
		return name;
	}

	public void setName(String newHabitName) {
		if (name.equals(newHabitName)) {
			return;
		}
		name = Objects.requireNonNull(newHabitName);
		hasChanges = true;
	}

	public String getId() {
		return id;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		if (this.visibility != Objects.requireNonNull(visibility)) {
			hasChanges = true;
		}
		this.visibility = visibility;
	}

	public NavigableSet<LocalDate> toNavigableSet() {
		return new TreeSet<>(dates);
	}

	public enum Visibility {
		VISIBLE,
		HIDDEN
	}
}
