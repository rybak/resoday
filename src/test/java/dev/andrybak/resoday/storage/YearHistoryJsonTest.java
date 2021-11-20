package dev.andrybak.resoday.storage;

import dev.andrybak.resoday.YearHistory;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YearHistoryJsonTest {

	private static final String V1_TEST_ID = "13e42334-22fa-7e4c-32fd-7d6126554ecd-V1TestFile";

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
		), "Testing123", "UniqueId", YearHistory.Visibility.VISIBLE);

		testJsonRoundTrip(SerializableYearHistory::toJson,
			s -> SerializableYearHistory.fromJson(s, "UniqueId.habit", "fallbackIdShouldNotBeUsed"), history);
	}

	@Test
	void testThatV1CanBeDeserialized() {
		String v1Filename = V1_TEST_ID + HabitFiles.HABIT_FILE_EXT;
		InputStream v1File = getClass().getResourceAsStream(v1Filename);
		assert v1File != null;
		SerializableYearHistory actual = SerializableYearHistory.fromJson(new InputStreamReader(v1File),
			V1_TEST_ID, Path.of(v1Filename));
		SerializableYearHistory expected = new SerializableYearHistory(
			Arrays.asList( // order should be preserved
				LocalDate.of(2021, 11, 20),
				LocalDate.of(2020, 2, 29),
				LocalDate.of(2020, 1, 20)
			),
			"TestNameFoobar",
			V1_TEST_ID,
			YearHistory.Visibility.VISIBLE
		);
		assertEquals(expected, actual);
	}

	@Test
	void testThatEmptyVersionZeroFileCanBeDeserialized() {
		String fileContent = "";
		SerializableYearHistory actual = SerializableYearHistory.fromJson(fileContent, "Foobar", "Foobar");
		SerializableYearHistory expected = new SerializableYearHistory(Collections.emptyList(), "Foobar", "Foobar", YearHistory.Visibility.VISIBLE);
		assertEquals(expected, actual);
	}
}