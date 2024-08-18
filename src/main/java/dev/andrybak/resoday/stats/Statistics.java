package dev.andrybak.resoday.stats;

import dev.andrybak.resoday.YearHistory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

public class Statistics {
	private final Rate rate;

	private Statistics(Rate rate) {
		this.rate = rate;
	}

	public static RoughRate rough(YearHistory yearHistory) {
		return roughRateOfNavigableSet(yearHistory.toNavigableSet());
	}

	private static RoughRate roughRateOfNavigableSet(NavigableSet<LocalDate> dates) {
		LocalDate first = dates.first();
		LocalDate last = dates.last();
		long daysCount = first.until(last, ChronoUnit.DAYS); // exclusive, so -1 is done for us
		int n = dates.size();
		return new RoughRate(n - 1, daysCount);
	}

	static RoughRate roughRateOfCollection(Collection<LocalDate> dates) {
		return roughRateOfNavigableSet(new TreeSet<>(dates));
	}

	public Rate getRate() {
		return rate;
	}
}
