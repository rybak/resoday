package dev.andrybak.resoday;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {
	private static final Duration AUTO_SAVE_PERIOD = Duration.ofMinutes(10);

	private final JFrame window = new JFrame(StringConstants.APP_NAME);
	private final JPanel content;
	private final List<HistoryPanel> historyPanels = new ArrayList<>();
	private final Timer autoSaveTimer;

	Main(Path dir) {
		setUpMenuBar();

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

		initKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK), () ->
			historyPanels.get(tabs.getSelectedIndex()).markToday()
		);

		autoSaveTimer = new Timer(Math.toIntExact(AUTO_SAVE_PERIOD.toMillis()), ignored -> autoSave());
		autoSaveTimer.addActionListener(ignored -> historyPanels.forEach(HistoryPanel::updateDecorations));
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

	private void setUpMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.setMnemonic('A');
		aboutMenuItem.addActionListener(ignored -> AboutDialog.show(window));
		helpMenu.add(aboutMenuItem);
		menuBar.add(helpMenu);
		window.setJMenuBar(menuBar);
	}

	private void updateWindowTitle(JTabbedPane tabs) {
		HistoryPanel historyPanel = historyPanels.get(tabs.getSelectedIndex());
		window.setTitle(historyPanel.getPath().getFileName() + " - " + StringConstants.APP_NAME);
	}

	private void initKeyStroke(KeyStroke nextKeyStroke, Runnable runnable) {
		SwingUtilities.invokeLater(() -> {
			Object cmd = new Object();
			content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(nextKeyStroke, cmd);
			content.getActionMap().put(cmd, new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					runnable.run();
				}
			});
		});
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
				autoSaveTimer.stop();
				save();
			}
		});
		autoSaveTimer.start();
	}

	private void autoSave() {
		System.out.println("Auto-saving...");
		save();
		System.out.println("Auto-saving complete.");
	}

	private void save() {
		historyPanels.forEach(HistoryPanel::save);
	}
}
