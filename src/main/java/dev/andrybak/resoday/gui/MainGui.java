package dev.andrybak.resoday.gui;

import dev.andrybak.resoday.SortOrder;
import dev.andrybak.resoday.StringConstants;
import dev.andrybak.resoday.YearHistory;
import dev.andrybak.resoday.gui.edithabits.ChooseHabitNameDialog;
import dev.andrybak.resoday.gui.edithabits.HideHabitDialog;
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
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public final class MainGui {
	private static final Duration AUTO_SAVE_PERIOD = Duration.ofMinutes(10);
	private static final String APP_ICON_FILENAME = "resoday-icon.png";

	private final JFrame window = new JFrame(StringConstants.APP_NAME_GUI);
	private final JPanel content;
	private final Histories histories = new Histories();
	private final Timer autoSaveTimer;

	public MainGui(Path dir) {
		content = new JPanel(new BorderLayout());

		JTabbedPane tabs = new JTabbedPane();
		try (Stream<Path> paths = Files.walk(dir)) {
			Map<String/* id */, YearHistory> yearHistories = paths
				.filter(Files::isRegularFile)
				.filter(Files::isReadable)
				.filter(HabitFiles.IS_HABIT_FILE)
				.map(YearHistory::read)
				.flatMap(Optional::stream)
				.collect(Collectors.toMap(YearHistory::getId, Function.identity()));
			Optional<SortOrder> maybeOrder = SortOrder.read(dir);
			final Stream<YearHistory> sortedYearHistories;
			if (maybeOrder.isPresent()) {
				sortedYearHistories = maybeOrder.get().order(yearHistories);
			} else {
				sortedYearHistories = yearHistories.values().stream();
			}
			sortedYearHistories
				.forEach(yearHistory -> {
					if (yearHistory.getVisibility() == YearHistory.Visibility.VISIBLE) {
						HistoryPanel historyPanel = new HistoryPanel(yearHistory);
						histories.add(yearHistory, historyPanel);
						tabs.addTab(yearHistory.getName(), historyPanel);
					} else {
						histories.add(yearHistory, null);
					}
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
		autoSaveTimer.addActionListener(ignored -> histories.forEachPanel(HistoryPanel::updateDecorations));

		setUpMenuBar(dir, tabs);
	}

	private static Image getResodayImage() {
		URL resodayIconUrl = Objects.requireNonNull(MainGui.class.getResource(APP_ICON_FILENAME));
		return Toolkit.getDefaultToolkit().getImage(resodayIconUrl);
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
		JMenuItem hideHabitMenuItem = new JMenuItem("Hide habit");
		hideHabitMenuItem.addActionListener(ignored -> openHideHabitDialog(tabs));
		mainMenu.add(hideHabitMenuItem);
		JMenuItem editHabitsMenuItem = new JMenuItem("Edit habits");
		editHabitsMenuItem.addActionListener(ignored -> openEditHabitsDialog(dir, tabs));
		mainMenu.add(editHabitsMenuItem);
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
		Set<String> names = histories.histories()
			.map(YearHistory::getName)
			.collect(toSet());
		ChooseHabitNameDialog.show(window, names::contains, habitName -> {
			String newId = HabitFiles.createNewId();
			String filename = HabitFiles.createNewFilename(newId, habitName);
			Path newHabitPath = dir.resolve(filename);
			YearHistory newHistory = new YearHistory(newHabitPath, habitName, newId);
			HistoryPanel newPanel = new HistoryPanel(newHistory);
			histories.add(newHistory, newPanel);
			tabs.addTab(habitName, newPanel);
			System.out.println("Added new habit '" + habitName + "' at path '" + newHabitPath.toAbsolutePath() + "'.");
		});
	}

	private void openHideHabitDialog(JTabbedPane tabs) {
		Optional<HistoryPanel> maybeHistoryPanel = getCurrentHistoryPanel(tabs);
		if (maybeHistoryPanel.isEmpty()) {
			return;
		}

		HistoryPanel historyPanel = maybeHistoryPanel.get();
		switch (HideHabitDialog.show(window, historyPanel.getHistoryName())) {
		case YES:
			hideHabit(tabs, historyPanel);
			break;
		case NO:
			break;
		}
	}

	private void hideHabit(JTabbedPane tabs, HistoryPanel historyPanel) {
		historyPanel.hideHistory();
		int i = tabs.indexOfComponent(historyPanel);
		assert i >= 0;
		histories.hide(historyPanel.getHistoryId());
		tabs.removeTabAt(i);
	}

	private void openEditHabitsDialog(Path dir, JTabbedPane tabs) {
		String oldSelectedId = getCurrentHistoryPanel(tabs)
			.map(HistoryPanel::getHistoryId)
			.orElse(null);
		boolean edited = histories.edit(this.window, dir);
		if (!edited) {
			return;
		}
		for (int i = 0, n = tabs.getTabCount(); i < n; i++) {
			tabs.removeTabAt(0); // removing all tabs
		}
		List<HistoryPanel> orderedPanels = histories.getOrderedPanels();
		for (HistoryPanel hp : orderedPanels) {
			tabs.addTab(hp.getHistoryName(), hp);
		}
		if (oldSelectedId != null) {
			Optional<HistoryPanel> maybeNewSelected = orderedPanels.stream()
				.filter(hp -> hp.getHistoryId().equals(oldSelectedId))
				.findFirst();
			if (maybeNewSelected.isPresent()) {
				tabs.setSelectedComponent(maybeNewSelected.get());
			} else {
				tabs.setSelectedIndex(0);
			}
		}
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
			return Optional.of((HistoryPanel)tabs.getSelectedComponent());
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
		window.setIconImage(getResodayImage());
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
		histories.forEachHistory(YearHistory::save);
	}
}
