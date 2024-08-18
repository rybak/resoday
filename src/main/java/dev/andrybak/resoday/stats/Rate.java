package dev.andrybak.resoday.stats;

public sealed interface Rate permits OncePerPeriod, TimesPerPeriod, RoughRate {
}
