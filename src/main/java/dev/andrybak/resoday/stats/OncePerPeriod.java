package dev.andrybak.resoday.stats;

public record OncePerPeriod(long periodLength, PeriodType periodType) implements Rate {
}
