package dev.andrybak.resoday;

import dev.andrybak.resoday.gui.MainGui;
import dev.andrybak.resoday.settings.storage.CustomDataDirectory;
import dev.dirs.ProjectDirectories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class Resoday {

	public static void main(String... args) {
		Path dataDir;
		Path configDir;
		if (args.length < 1) {
			ProjectDirectories projectDirs = ProjectDirectories.from(StringConstants.TOP_LEVEL, StringConstants.ORGANIZATION,
				StringConstants.APP_NAME);
			configDir = Paths.get(projectDirs.configDir).toAbsolutePath();
			dataDir = getDataDir(configDir, Paths.get(projectDirs.dataDir).toAbsolutePath());
		} else {
			Path p = Paths.get(args[0]).toAbsolutePath();
			configDir = p;
			dataDir = getDataDir(configDir, p);
		}
		try {
			Files.createDirectories(dataDir);
		} catch (IOException e) {
			System.err.printf("Could not create directory '%s'%n", dataDir.toAbsolutePath());
			dataDir = Paths.get(".").toAbsolutePath();
		}
		try {
			Files.createDirectories(configDir);
		} catch (IOException e) {
			System.err.printf("Could not create directory '%s'%n", configDir.toAbsolutePath());
			configDir = Paths.get(".").toAbsolutePath();
		}
		System.out.printf("Using '%s'%n", dataDir.toAbsolutePath());
		System.out.printf("Config from '%s'%n", configDir.toAbsolutePath());
		if (!Files.isDirectory(dataDir) || !Files.isDirectory(configDir)) {
			System.err.println("'" + dataDir.toAbsolutePath() + "' must be a directory. Aborting.");
			System.err.println("'" + configDir.toAbsolutePath() + "' must be a directory. Aborting.");
			System.exit(1);
		}
		new MainGui(dataDir, configDir).go(configDir);
	}

	private static Path getDataDir(Path configDir, Path defaultDataDir) {
		final Path dataDir;
		final Optional<Path> maybeCustomDataDir = CustomDataDirectory.from(configDir);
		if (maybeCustomDataDir.isPresent()) {
			dataDir = maybeCustomDataDir.get();
			System.out.println("Using custom data directory: " + dataDir);
		} else {
			dataDir = defaultDataDir;
		}
		return dataDir;
	}
}
