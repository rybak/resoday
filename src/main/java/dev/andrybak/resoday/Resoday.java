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
		if (args.length < 1) {
			ProjectDirectories projectDirs = ProjectDirectories.from("dev", "andrybak", StringConstants.APP_NAME);
			dataDir = Paths.get(projectDirs.dataDir).toAbsolutePath();
		} else {
			dataDir = Paths.get(args[0]).toAbsolutePath();
		}
		try {
			Files.createDirectories(dataDir);
		} catch (IOException e) {
			System.err.printf("Could not create directory '%s'%n", dataDir.toAbsolutePath());
			dataDir = Paths.get(".").toAbsolutePath();
		}
		System.out.printf("Using '%s'%n", dataDir.toAbsolutePath());
		if (!Files.isDirectory(dataDir)) {
			System.err.println("'" + dataDir.toAbsolutePath() + "' is not a directory. Aborting.");
			System.exit(1);
		}
		new MainGui(dataDir).go();
	}
}
