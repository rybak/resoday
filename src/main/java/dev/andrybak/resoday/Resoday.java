package dev.andrybak.resoday;

import dev.andrybak.resoday.gui.MainGui;
import dev.andrybak.resoday.settings.storage.CustomDataDirectory;
import dev.dirs.ProjectDirectories;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class Resoday {

	public static void main(String... args) {
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			showCrashError(thread, throwable);
			System.exit(1);
		});
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

	private static void showCrashError(Thread thread, Throwable t) {
		JPanel message = new JPanel(new BorderLayout());
		message.add(new JLabel("Uncaught exception: " + t.getMessage()), BorderLayout.NORTH);
		{
			JTextArea exceptionText = new JTextArea();
			StringWriter sw = new StringWriter();
			PrintWriter printWriter = new PrintWriter(sw);
			printWriter.println("Uncaught exception in thread " + thread.getName());
			printWriter.println(t.getMessage());
			t.printStackTrace(printWriter);
			exceptionText.setText(sw.toString());
			message.add(exceptionText, BorderLayout.CENTER);
		}
		message.add(new JLabel(StringConstants.APP_NAME_GUI + " is going to close"), BorderLayout.SOUTH);
		JOptionPane.showMessageDialog(
			JOptionPane.getRootFrame(),
			message,
			"Crash",
			JOptionPane.ERROR_MESSAGE
		);
	}
}
