package dev.andrybak.resoday.storage;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOrderTest {
	@Test
	void testThatWholeOrderWorks() {
		Map<String, String> m = Map.of(
			"third", "1234567890",
			"second", "Foobar",
			"first", "Hello"
		);
		SortOrder sortOrder = new SortOrder(List.of(
			"first",
			"second",
			"third"
		));
		List<String> expected = List.of("Hello", "Foobar", "1234567890");
		List<String> actual = sortOrder.order(m).toList();
		assertEquals(expected, actual);
	}

	@Test
	void testThatOrderWithMissingElementsWorks() {
		Map<String, String> m = Map.of(
			"fourth", "The Fourth String",
			"third", "1234567890",
			"second", "Foobar",
			"first", "Hello"
		);
		SortOrder sortOrder = new SortOrder(List.of(
			"first",
			"second"
		));
		List<String> expectedHalf = List.of("Hello", "Foobar");
		List<String> actual = sortOrder.order(m).toList();
		assertEquals(expectedHalf, actual.subList(0, 2));
		assertEquals(new HashSet<>(m.values()), new HashSet<>(actual), "The rest of values should also be in actual");
	}

	@Test
	void testThatOrderWithExtraElementsWorks() {
		Map<String, String> m = Map.of(
			"third", "1234567890",
			"second", "Foobar",
			"first", "Hello"
		);
		SortOrder sortOrder = new SortOrder(List.of(
			"first",
			"second",
			"third",
			"ninth (does not exist)",
			"tenth (does not exist)"
		));
		List<String> expected = List.of("Hello", "Foobar", "1234567890");
		List<String> actual = sortOrder.order(m).toList();
		assertEquals(expected, actual);
	}

	@Test
	void testThatOrderWithAllWrongElementsWorks() {
		Map<String, String> m = Map.of(
			"third", "1234567890",
			"second", "Foobar",
			"first", "Hello"
		);
		SortOrder sortOrder = new SortOrder(List.of(
			"eighth (does not exist)",
			"ninth (does not exist)",
			"tenth (does not exist)"
		));
		Set<String> expected = Set.of("Hello", "Foobar", "1234567890");
		Set<String> actual = sortOrder.order(m).collect(toSet());
		assertEquals(expected, actual);
	}
}