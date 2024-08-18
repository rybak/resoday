package dev.andrybak.resoday.stats;

public record TimesPerPeriod(PeriodType periodType) implements Rate {
	public TimesPerPeriod {
		if (periodType == PeriodType.DAY) {
			throw new IllegalArgumentException("TimesPerPeriod cannot take DAY as parameter in Resoday");
		}
	}
}
