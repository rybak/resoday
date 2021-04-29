package dev.andrybak.resoday;

import dev.dirs.ProjectDirectories;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Resoday {
	private static final Duration AUTO_SAVE_PERIOD = Duration.ofMinutes(10);
	private static final String HABIT_FILE_EXT = ".habit";

	private final JFrame window = new JFrame(StringConstants.APP_NAME_GUI);
	private final JPanel content;
	private final List<HistoryPanel> historyPanels = new ArrayList<>();
	private final Timer autoSaveTimer;

	Resoday(Path dir) {
		setUpMenuBar();
		content = new JPanel(new BorderLayout());

		JTabbedPane tabs = new JTabbedPane();
		try (Stream<Path> paths = Files.walk(dir)) {
			paths
				.filter(Files::isRegularFile)
				.filter(Files::isReadable)
				.filter(p -> p.getFileName().toString().endsWith(HABIT_FILE_EXT))
				.forEach(p -> {
					HistoryPanel historyPanel = new HistoryPanel(p);
					historyPanels.add(historyPanel);
					String fn = p.getFileName().toString();
					String tabName = fn.substring(0, fn.length() - HABIT_FILE_EXT.length());
					tabs.addTab(tabName, historyPanel);
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
			markTodayInCurrentTab(tabs)
		);

		autoSaveTimer = new Timer(Math.toIntExact(AUTO_SAVE_PERIOD.toMillis()), ignored -> autoSave());
		autoSaveTimer.addActionListener(ignored -> historyPanels.forEach(HistoryPanel::updateDecorations));
	}

	private void markTodayInCurrentTab(JTabbedPane tabs) {
		historyPanels.get(tabs.getSelectedIndex()).markToday();
	}

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
		new Resoday(dir).go();
	}

	private void setUpMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.setMnemonic('A');
		aboutMenuItem.addActionListener(ignored -> AboutDialog.show(window));
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.APP_ABOUT))
				desktop.setAboutHandler(ignored -> AboutDialog.show(window));
		}
		helpMenu.add(aboutMenuItem);
		menuBar.add(helpMenu);
		window.setJMenuBar(menuBar);
	}

	private void updateWindowTitle(JTabbedPane tabs) {
		HistoryPanel historyPanel = historyPanels.get(tabs.getSelectedIndex());
		window.setTitle(historyPanel.getPath().getFileName() + " – " + StringConstants.APP_NAME_GUI);
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
