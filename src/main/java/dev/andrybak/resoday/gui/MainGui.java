package dev.andrybak.resoday.gui;

import dev.andrybak.resoday.StringConstants;
import dev.andrybak.resoday.YearHistory;
import dev.andrybak.resoday.storage.HabitFiles;

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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public final class MainGui {
	private static final Duration AUTO_SAVE_PERIOD = Duration.ofMinutes(10);

	private final JFrame window = new JFrame(StringConstants.APP_NAME_GUI);
	private final JPanel content;
	private final List<HistoryPanel> historyPanels = new ArrayList<>();
	private final Timer autoSaveTimer;

	public MainGui(Path dir) {
		content = new JPanel(new BorderLayout());

		JTabbedPane tabs = new JTabbedPane();
		try (Stream<Path> paths = Files.walk(dir)) {
			paths
				.filter(Files::isRegularFile)
				.filter(Files::isReadable)
				.filter(HabitFiles.IS_HABIT_FILE)
				.map(YearHistory::read)
				.flatMap(Optional::stream)
				.forEach(yearHistory -> {
					HistoryPanel historyPanel = new HistoryPanel(yearHistory);
					historyPanels.add(historyPanel);
					tabs.addTab(yearHistory.getName(), historyPanel);
				});
		} catch (IOException e) {
			System.err.println("Could not find files in '" + dir.toAbsolutePath() + "': " + e);
			System.err.println("Aborting.");
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

		setUpMenuBar(dir, tabs);
	}

	private void markTodayInCurrentTab(JTabbedPane tabs) {
		getCurrentHistoryPanel(tabs).ifPresent(HistoryPanel::markToday);
	}

	private void setUpMenuBar(Path dir, JTabbedPane tabs) {
		JMenuBar menuBar = new JMenuBar();

		JMenu mainMenu = new JMenu("Main");
		JMenuItem addHabitMenuItem = new JMenuItem("Add habit");
		addHabitMenuItem.addActionListener(ignored -> openAddHabitDialog(dir, tabs));
		mainMenu.add(addHabitMenuItem);
		menuBar.add(mainMenu);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.setMnemonic('A');
		aboutMenuItem.addActionListener(ignored -> AboutDialog.show(window));
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
				desktop.setAboutHandler(ignored -> AboutDialog.show(window));
			}
		}
		helpMenu.add(aboutMenuItem);
		menuBar.add(helpMenu);

		window.setJMenuBar(menuBar);
	}

	private void openAddHabitDialog(Path dir, JTabbedPane tabs) {
		Set<String> names = historyPanels.stream()
			.map(HistoryPanel::getHistoryName)
			.collect(toSet());
		AddHabitDialog.show(window, names::contains, habitName -> {
			String filename = HabitFiles.createNewFilename(habitName);
			Path newHabitPath = dir.resolve(filename);
			YearHistory newHistory = new YearHistory(newHabitPath, habitName);
			HistoryPanel newPanel = new HistoryPanel(newHistory);
			historyPanels.add(newPanel);
			tabs.addTab(habitName, newPanel);
			System.out.println("Added new habit '" + habitName + "' at path '" + newHabitPath.toAbsolutePath() + "'.");
		});
	}

	private void updateWindowTitle(JTabbedPane tabs) {
		Optional<HistoryPanel> maybeHistoryPanel = getCurrentHistoryPanel(tabs);
		final String title;
		if (maybeHistoryPanel.isPresent()) {
			HistoryPanel historyPanel = maybeHistoryPanel.get();
			title = historyPanel.getHistoryName() + " – " + StringConstants.APP_NAME_GUI;
		} else {
			title = StringConstants.APP_NAME_GUI;
		}
		window.setTitle(title);
	}

	private Optional<HistoryPanel> getCurrentHistoryPanel(JTabbedPane tabs) {
		final int currentTabIndex = tabs.getSelectedIndex();
		if (currentTabIndex >= 0) {
			return Optional.of(historyPanels.get(currentTabIndex));
		} else {
			return Optional.empty();
		}
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

	public void go() {
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
