package dev.andrybak.resoday.gui;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public record WindowPosition(int width, int height, int x, int y) {
	private static final Path WINDOW_POSITION_FILE = Paths.get("window-position.txt");
	private static final Pattern PLAIN_POSITIVE_INTEGER_PATTERN = Pattern.compile("^\\d{1,10}$");

	public static WindowPosition from(Window w) {
		return new WindowPosition(w.getWidth(), w.getHeight(), w.getX(), w.getY());
	}

	public static Optional<WindowPosition> read(Path configDir) {
		Path source = configDir.resolve(WINDOW_POSITION_FILE);
		if (!Files.isReadable(source)) {
			return Optional.empty();
		}
		try {
			List<String> params = Files.readAllLines(source);
			if (params.size() < 4) {
				return Optional.empty();
			}
			OptionalInt maybeWidth = readInt(params.get(0));
			OptionalInt maybeHeight = readInt(params.get(1));
			OptionalInt maybeX = readInt(params.get(2));
			OptionalInt maybeY = readInt(params.get(3));
			if (maybeWidth.isEmpty() || maybeHeight.isEmpty() || maybeX.isEmpty() || maybeY.isEmpty()) {
				return Optional.empty();
			}
			return Optional.of(
				new WindowPosition(
					maybeWidth.getAsInt(),
					maybeHeight.getAsInt(),
					maybeX.getAsInt(),
					maybeY.getAsInt()
				)
			);
		} catch (IOException e) {
			System.err.println("Could not read from '" + source + "'");
			e.printStackTrace();
			return Optional.empty();
		}
	}

	private static OptionalInt readInt(String s) {
		if (!PLAIN_POSITIVE_INTEGER_PATTERN.matcher(s).matches()) {
			return OptionalInt.empty();
		}
		long maybe = Long.parseLong(s);
		if (maybe > Integer.MAX_VALUE) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(Math.toIntExact(maybe));
	}

	public void applyTo(Window window) {
		Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		if (x > maxBounds.getWidth() || y > maxBounds.getHeight() ||
			width > maxBounds.getWidth() || height > maxBounds.getHeight())
		{
			return;
		}
		window.setSize(new Dimension(width, height));
		window.setLocation(x, y);
	}

	public void save(Path configDir) {
		List<String> params = IntStream.of(width, height, x, y)
			.mapToObj(String::valueOf)
			.collect(toList());
		Path destination = configDir.resolve(WINDOW_POSITION_FILE);
		try {
			Files.write(destination, params);
		} catch (IOException e) {
			System.err.println("Could not write to '" + destination + "'");
			e.printStackTrace();
		}
	}
}
