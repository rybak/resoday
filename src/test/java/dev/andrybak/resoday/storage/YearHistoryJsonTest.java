package dev.andrybak.resoday.storage;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YearHistoryJsonTest {

	private static <T> void testJsonRoundTrip(Function<T, String> toJson, Function<String, T> fromJson, T before) {
		String s = toJson.apply(before);
		T after = fromJson.apply(s);
		assertEquals(before, after, () -> "Object of " + before.getClass() + " should be the equal after Gson round trip");
	}

	@Test
	void testThatCurrentVersionCanBeDeserialized() {
		SerializableYearHistory history = new SerializableYearHistory(List.of(
			LocalDate.of(2021, 5, 1),
			LocalDate.of(1961, 1, 1),
			LocalDate.of(2121, 12, 31)
		));

		testJsonRoundTrip(SerializableYearHistory::toJson, SerializableYearHistory::fromJson, history);
	}
}