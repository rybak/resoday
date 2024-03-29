package dev.andrybak.resoday.storage;

import dev.andrybak.resoday.gui.settings.DataDirSupplier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Saved as part of the data, near the {@code .habit} files.
 */
public class SortOrder {
	private static final Path ORDER_FILE = Paths.get("resoday-order.txt");

	private final List<String> order;

	SortOrder(List<String> order) {
		this.order = new ArrayList<>(order);
	}

	public static void save(DataDirSupplier dataDirSupplier, List<String> ids) {
		new SortOrder(ids).save(dataDirSupplier);
	}

	public static Optional<SortOrder> read(Path rootDir) {
		Path p = rootDir.resolve(ORDER_FILE);
		if (Files.isDirectory(p)) {
			throw new IllegalStateException("Path '" + p + "' is a directory");
		}
		if (!Files.isReadable(p)) {
			return Optional.empty();
		}
		try {
			return Optional.of(new SortOrder(Files.readAllLines(p)));
		} catch (IOException e) {
			System.err.println("Could not read '" + p + "'. Got error: " + e);
			e.printStackTrace();
			return Optional.empty();
		}
	}

	public void save(DataDirSupplier dataDirSupplier) {
		Path p = dataDirSupplier.getDataDir().resolve(ORDER_FILE);
		try {
			Files.write(p, order);
		} catch (IOException e) {
			System.err.println("Could not write '" + p + "'. Got error: " + e);
			e.printStackTrace();
		}
	}

	public <T> Stream<T> order(Map<String, T> elements) {
		Set<String> inputIds = new HashSet<>(elements.keySet());
		Set<String> actualOrder = new LinkedHashSet<>(); // LinkedHashSet because we need preserved order
		for (String id : order) {
			if (inputIds.contains(id)) {
				actualOrder.add(id);
			}
		}
		actualOrder.addAll(inputIds); // saved order file can be missing some stuff, add anything that's missing
		return actualOrder.stream()
			.map(elements::get);
	}
}
