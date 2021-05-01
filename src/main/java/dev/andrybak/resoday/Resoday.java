package dev.andrybak.resoday;

import dev.dirs.ProjectDirectories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Resoday {
	public static void main(String... args) {
		Path dir;
		if (args.length < 1) {
			ProjectDirectories projectDirs = ProjectDirectories.from("dev", "andrybak", StringConstants.APP_NAME);
			dir = Paths.get(projectDirs.dataDir).toAbsolutePath();
		} else {
			dir = Paths.get(args[0]);
		}
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			System.err.printf("Could not create directory '%s'%n", dir);
			dir = Paths.get(".").toAbsolutePath();
		}
		System.out.printf("Using '%s'%n", dir);
		if (!Files.isDirectory(dir)) {
			System.err.println("'" + dir.toAbsolutePath() + "' is not a directory. Aborting.");
			System.exit(1);
		}
		new MainGui(dir).go();
	}
}
