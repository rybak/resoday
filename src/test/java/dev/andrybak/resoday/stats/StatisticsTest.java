package dev.andrybak.resoday.stats;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsTest {
	private static void testRoughRate(List<LocalDate> dates, RoughRate expected) {
		RoughRate actual = Statistics.roughRateOfCollection(dates);
		assertEquals(expected, actual);
	}

	@Test
	void testThatSimpleRoughEachDayIsCalculated() {
		testRoughRate(
			List.of(
				LocalDate.of(2024, 8, 12),
				LocalDate.of(2024, 8, 13),
				LocalDate.of(2024, 8, 14),
				LocalDate.of(2024, 8, 15),
				LocalDate.of(2024, 8, 16),
				LocalDate.of(2024, 8, 17),
				LocalDate.of(2024, 8, 18)
			),
			new RoughRate(6, 6)
		);
	}

	@Test
	void testThatSimpleRoughEveryTwoDaysIsCalculated() {
		testRoughRate(
			List.of(
				LocalDate.of(2024, 8, 12),
				LocalDate.of(2024, 8, 14),
				LocalDate.of(2024, 8, 16),
				LocalDate.of(2024, 8, 18)
			),
			/*
			 * Last date is explicitly "excluded" from the calculation.
			 */
			new RoughRate(3, 6)
		);
	}
}