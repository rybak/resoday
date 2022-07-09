package dev.andrybak.resoday.settings.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class CustomDataDirectory {
	private static final Path CUSTOM_DATA_DIR_FILE = Paths.get("custom-data-directory.txt");

	private CustomDataDirectory() {
		throw new AssertionError();
	}

	public static Optional<Path> from(Path configDir) {
		Path maybeFile = configDir.resolve(CUSTOM_DATA_DIR_FILE);
		if (Files.isRegularFile(maybeFile) && Files.isReadable(maybeFile)) {
			try {
				List<String> strings = Files.readAllLines(maybeFile);
				if (strings.isEmpty()) {
					return Optional.empty();
				}
				Path customDataDir = Path.of(strings.get(0));
				return Optional.of(customDataDir);
			} catch (IOException e) {
				throw new UncheckedIOException("Could not read " + maybeFile, e);
			}
		}
		return Optional.empty();
	}

	public static void save(Path configDir, Path customDataDir) {
		Path f = configDir.resolve(CUSTOM_DATA_DIR_FILE);
		try {
			Files.writeString(f, customDataDir.toAbsolutePath().toString());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
