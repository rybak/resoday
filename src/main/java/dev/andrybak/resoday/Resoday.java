package dev.andrybak.resoday;

import dev.andrybak.resoday.gui.MainGui;
import dev.dirs.ProjectDirectories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Resoday {

	public static void main(String... args) {
		Path dataDir;
		Path configDir;
		if (args.length < 1) {
			ProjectDirectories projectDirs = ProjectDirectories.from(StringConstants.TOP_LEVEL, StringConstants.ORGANIZATION,
				StringConstants.APP_NAME);
			dataDir = Paths.get(projectDirs.dataDir).toAbsolutePath();
			configDir = Paths.get(projectDirs.configDir).toAbsolutePath();
		} else {
			Path p = Paths.get(args[0]).toAbsolutePath();
			dataDir = p;
			configDir = p;
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
}
