package dev.andrybak.fuladve;

import java.time.LocalDate;
import java.util.*;

import static java.util.Collections.*;

public class YearHistory {
	private final Set<LocalDate> dates;

	YearHistory() {
		this(emptySet());
	}

	YearHistory(Collection<LocalDate> dates) {
		this.dates = new HashSet<>(dates);
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
}
