package dev.andrybak.fuladve;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {
	private static final String APP_NAME = "Every Day Calendar";

	private final JFrame window = new JFrame(APP_NAME);
	private final JPanel content;
	private final List<HistoryPanel> historyPanels = new ArrayList<>();

	Main(Path dir) {
		content = new JPanel(new BorderLayout());

		JTabbedPane tabs = new JTabbedPane();
		try (Stream<Path> paths = Files.walk(dir)) {
			paths
				.filter(Files::isRegularFile)
				.filter(Files::isReadable)
				.filter(p -> p.getFileName().toString().endsWith(".habit"))
				.forEach(p -> {
					HistoryPanel historyPanel = new HistoryPanel(p);
					historyPanels.add(historyPanel);
					tabs.addTab(p.getFileName().toString(), historyPanel);
				});
		} catch (IOException e) {
			System.err.println("Could not find files in '" + dir.toAbsolutePath() + "'. Aborting.");
			e.printStackTrace();
			System.exit(1);
		}

		tabs.addChangeListener(ignored -> updateWindowTitle(tabs));
		updateWindowTitle(tabs);

		content.add(tabs, BorderLayout.CENTER);
	}

	public static void main(String... args) {
		final Path dir;
		if (args.length < 1) {
			dir = Paths.get(".");
			System.out.println("Defaulting to current directory...");
		} else {
			dir = Paths.get(args[0]);
		}
		if (!Files.isDirectory(dir)) {
			System.err.println("'" + dir.toAbsolutePath() + "' is not a directory. Aborting.");
			System.exit(1);
		}
		new Main(dir).go();
	}

	private void updateWindowTitle(JTabbedPane tabs) {
		HistoryPanel historyPanel = historyPanels.get(tabs.getSelectedIndex());
		window.setTitle(historyPanel.getPath().getFileName() + " - " + APP_NAME);
	}

	private void go() {
		window.setMinimumSize(new Dimension(640, 480));
		window.setContentPane(content);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				historyPanels.forEach(HistoryPanel::save);
			}
		});
	}
}
