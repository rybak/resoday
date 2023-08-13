package dev.andrybak.resoday.storage;

import dev.andrybak.resoday.YearHistory;
import dev.andrybak.resoday.settings.gui.HabitCalendarLayout;
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
	private static final String V2_HORIZONTAL_BASE_NAME = "v2-Horizontal-example";
	private static final String V2_NO_LAYOUT_BASE_NAME = "v2-no-layout-Example";

	private static <T> void testJsonRoundTrip(Function<T, String> toJson, Function<String, T> fromJson, T before) {
		String s = toJson.apply(before);
		T after = fromJson.apply(s);
		assertEquals(before, after, () -> "Object of " + before.getClass() + " should be the equal after Gson round trip");
	}

	private static SerializableYearHistory testJsonDeserialization(String filename, String fallbackName,
		SerializableYearHistory expected)
	{
		InputStream inputStream = YearHistoryJsonTest.class.getResourceAsStream(filename);
		assert inputStream != null;
		SerializableYearHistory actual = SerializableYearHistory.fromJson(new InputStreamReader(inputStream),
			fallbackName, Path.of(filename));
		assertEquals(expected, actual);
		return actual;
	}

	@Test
	void testThatCurrentVersionCanBeDeserialized() {
		SerializableYearHistory history = new SerializableYearHistory(List.of(
			LocalDate.of(2021, 5, 1),
			LocalDate.of(1961, 1, 1),
			LocalDate.of(2121, 12, 31)
		), "Testing123", "UniqueId", YearHistory.Visibility.VISIBLE, HabitCalendarLayout.COLUMNS);

		testJsonRoundTrip(SerializableYearHistory::toJson,
			s -> SerializableYearHistory.fromJson(s, "UniqueId.habit", "fallbackIdShouldNotBeUsed"), history);
	}

	@Test
	void testThatV1CanBeDeserialized() {
		String v1Filename = V1_TEST_ID + HabitFiles.HABIT_FILE_EXT;
		SerializableYearHistory expected = new SerializableYearHistory(
			Arrays.asList( // order should be preserved
				LocalDate.of(2021, 11, 20),
				LocalDate.of(2020, 2, 29),
				LocalDate.of(2020, 1, 20)
			),
			"TestNameFoobar",
			V1_TEST_ID,
			YearHistory.Visibility.VISIBLE,
			HabitCalendarLayout.DEFAULT
		);
		testJsonDeserialization(v1Filename, V1_TEST_ID, expected);
	}

	@Test
	void testThatEmptyVersionZeroFileCanBeDeserialized() {
		String fileContent = "";
		SerializableYearHistory actual = SerializableYearHistory.fromJson(fileContent, "Foobar", "Foobar");
		SerializableYearHistory expected = new SerializableYearHistory(Collections.emptyList(), "Foobar", "Foobar",
			YearHistory.Visibility.VISIBLE, HabitCalendarLayout.DEFAULT);
		assertEquals(expected, actual);
	}

	@Test
	void testThatV2WithoutLayoutCanBeDeserialized() {
		String v2Filename = V2_NO_LAYOUT_BASE_NAME + HabitFiles.HABIT_FILE_EXT;
		SerializableYearHistory expected = new SerializableYearHistory(
			List.of(
				LocalDate.of(2023, 8, 12)
			),
			"No layout example",
			"7131211f-f397-434e-917e-f24e7be62ef4",
			YearHistory.Visibility.VISIBLE,
			null // main assertion
		);
		var actual = testJsonDeserialization(v2Filename, "garbage", expected);
		assertEquals(HabitCalendarLayout.DEFAULT, actual.getHabitCalendarLayout());
	}

	@Test
	void testThatV2WithLayoutCanBeDeserialized() {
		String v2Filename = V2_HORIZONTAL_BASE_NAME + HabitFiles.HABIT_FILE_EXT;
		SerializableYearHistory expected = new SerializableYearHistory(
			Collections.emptyList(),
			"Horizontal example",
			"36df74b4-0976-4689-a412-bf8e18795205",
			YearHistory.Visibility.VISIBLE,
			HabitCalendarLayout.CLASSIC_HORIZONTAL // main assertion
		);
		testJsonDeserialization(v2Filename, "garbage", expected);
	}
}