package dev.andrybak.resoday;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOrderTest {
	@Test
	void testThatWholeOrderWorks() {
		Map<String, String> m = new HashMap<>();
		m.put("third", "1234567890");
		m.put("second", "Foobar");
		m.put("first", "Hello");
		SortOrder sortOrder = new SortOrder(Arrays.asList(
			"first",
			"second",
			"third"
		));
		List<String> expected = Arrays.asList("Hello", "Foobar", "1234567890");
		List<String> actual = sortOrder.order(m).collect(Collectors.toList());
		assertEquals(expected, actual);
	}

	@Test
	void testThatOrderWithMissingElementsWorks() {
		Map<String, String> m = new HashMap<>();
		m.put("fourth", "The Fourth String");
		m.put("third", "1234567890");
		m.put("second", "Foobar");
		m.put("first", "Hello");
		SortOrder sortOrder = new SortOrder(Arrays.asList(
			"first",
			"second"
		));
		List<String> expectedHalf = Arrays.asList("Hello", "Foobar");
		List<String> actual = sortOrder.order(m).collect(Collectors.toList());
		assertEquals(expectedHalf, actual.subList(0, 2));
		assertEquals(new HashSet<>(m.values()), new HashSet<>(actual), "The rest of values should also be in actual");
	}

	@Test
	void testThatOrderWithExtraElementsWorks() {
		Map<String, String> m = new HashMap<>();
		m.put("third", "1234567890");
		m.put("second", "Foobar");
		m.put("first", "Hello");
		SortOrder sortOrder = new SortOrder(Arrays.asList(
			"first",
			"second",
			"third",
			"ninth (does not exist)",
			"tenth (does not exist)"
		));
		List<String> expected = Arrays.asList("Hello", "Foobar", "1234567890");
		List<String> actual = sortOrder.order(m).collect(Collectors.toList());
		assertEquals(expected, actual);
	}

	@Test
	void testThatOrderWithAllWrongElementsWorks() {
		Map<String, String> m = new HashMap<>();
		m.put("third", "1234567890");
		m.put("second", "Foobar");
		m.put("first", "Hello");
		SortOrder sortOrder = new SortOrder(Arrays.asList(
			"eighth (does not exist)",
			"ninth (does not exist)",
			"tenth (does not exist)"
		));
		Set<String> expected = new HashSet<>(Arrays.asList("Hello", "Foobar", "1234567890"));
		Set<String> actual = sortOrder.order(m).collect(Collectors.toSet());
		assertEquals(expected, actual);
	}
}