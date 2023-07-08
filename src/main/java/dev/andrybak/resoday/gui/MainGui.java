package dev.andrybak.resoday.gui;

import dev.andrybak.resoday.StringConstants;
import dev.andrybak.resoday.YearHistory;
import dev.andrybak.resoday.gui.edithabits.ChooseHabitNameDialog;
import dev.andrybak.resoday.gui.edithabits.DeleteHabitDialog;
import dev.andrybak.resoday.gui.edithabits.HideHabitDialog;
import dev.andrybak.resoday.gui.help.AboutDialog;
import dev.andrybak.resoday.gui.help.DebugDialog;
import dev.andrybak.resoday.gui.help.HelpDialog;
import dev.andrybak.resoday.gui.settings.CalendarLayoutSettingProvider;
import dev.andrybak.resoday.gui.settings.DataDirSupplier;
import dev.andrybak.resoday.gui.settings.GuiSettingsSaver;
import dev.andrybak.resoday.gui.settings.SettingsMenu;
import dev.andrybak.resoday.gui.stats.StatisticsDialog;
import dev.andrybak.resoday.settings.gui.CalendarLayoutSetting;
import dev.andrybak.resoday.settings.gui.GuiSettings;
import dev.andrybak.resoday.settings.storage.CustomDataDirectory;
import dev.andrybak.resoday.storage.HabitFiles;
import dev.andrybak.resoday.storage.SortOrder;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

public final class MainGui implements CalendarLayoutSettingProvider {
	private static final Duration AUTO_SAVE_PERIOD = Duration.ofMinutes(10);

	private final JFrame window = new JFrame(StringConstants.APP_NAME_GUI);
	private final JPanel content;
	private final Histories histories = new Histories();
	private final Timer autoSaveTimer;
	private final GuiSettingsSaver guiSettingsSaver = new GuiSettingsSaver();
	private GuiSettings guiSettings;
	private Path dataDir;

	public MainGui(Path dataDir, Path configDir) {
		this.dataDir = dataDir;
		content = new JPanel(new BorderLayout());
		guiSettings = GuiSettings.read(configDir);

		JTabbedPane tabs = new JTabbedPane();
		try (Stream<Path> paths = Files.walk(dataDir)) {
			Map<String/* id */, YearHistory> yearHistories = paths
				.filter(Files::isRegularFile)
				.filter(Files::isReadable)
				.filter(HabitFiles.IS_HABIT_FILE)
				.map(statePath -> YearHistory.read(getDataDirSupplier(), statePath))
				.flatMap(Optional::stream)
				.collect(Collectors.toMap(YearHistory::getId, Function.identity()));
			Optional<SortOrder> maybeOrder = SortOrder.read(dataDir);
			final Stream<YearHistory> sortedYearHistories;
			if (maybeOrder.isPresent()) {
				sortedYearHistories = maybeOrder.get().order(yearHistories);
			} else {
				sortedYearHistories = yearHistories.values().stream();
			}
			sortedYearHistories
				.forEach(yearHistory -> {
					if (yearHistory.getVisibility() == YearHistory.Visibility.VISIBLE) {
						HistoryPanel historyPanel = new HistoryPanel(yearHistory, this);
						histories.add(yearHistory, historyPanel);
						tabs.addTab(yearHistory.getName(), historyPanel);
					} else {
						histories.add(yearHistory, null);
					}
				});
		} catch (IOException e) {
			System.err.println("Could not find files in '" + dataDir.toAbsolutePath() + "': " + e);
			System.err.println("Aborting.");
			System.exit(1);
		}

		tabs.addChangeListener(ignored -> updateWindowTitle(tabs));
		updateWindowTitle(tabs);

		content.add(tabs, BorderLayout.CENTER);

		initKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK), () ->
			markTodayInCurrentTab(tabs)
		);

		autoSaveTimer = new Timer(Math.toIntExact(AUTO_SAVE_PERIOD.toMillis()), ignored -> autoSave(configDir));
		autoSaveTimer.addActionListener(ignored -> histories.forEachPanel(HistoryPanel::updateDecorations));

		setUpMenuBar(tabs, configDir);
	}

	private DataDirSupplier getDataDirSupplier() {
		return () -> dataDir;
	}

	private void markTodayInCurrentTab(JTabbedPane tabs) {
		getCurrentHistoryPanel(tabs).ifPresent(HistoryPanel::markToday);
	}

	private void setUpMenuBar(JTabbedPane tabs, Path configDir) {
		JMenuBar menuBar = new JMenuBar();

		JMenu mainMenu = new JMenu("Main");
		mainMenu.setMnemonic('M');
		{
			JMenuItem addHabitMenuItem = new JMenuItem("Add habit");
			addHabitMenuItem.setMnemonic('A');
			addHabitMenuItem.addActionListener(ignored -> openAddHabitDialog(tabs));
			mainMenu.add(addHabitMenuItem);
		}
		{
			JMenuItem reorderHabitsMenuItem = new JMenuItem("Reorder habits");
			reorderHabitsMenuItem.setMnemonic('O');
			reorderHabitsMenuItem.addActionListener(ignored -> openReorderHabitsDialog(tabs));
			mainMenu.add(reorderHabitsMenuItem);
		}
		mainMenu.add(new JSeparator());
		{
			JMenuItem renameHabitMenuItem = new JMenuItem("Rename habit");
			renameHabitMenuItem.setMnemonic('R');
			renameHabitMenuItem.addActionListener(ignored -> openRenameHabitDialog(tabs));
			mainMenu.add(renameHabitMenuItem);
		}
		{
			JMenuItem hideHabitMenuItem = new JMenuItem("Hide habit");
			hideHabitMenuItem.setMnemonic('H');
			hideHabitMenuItem.addActionListener(ignored -> openHideHabitDialog(tabs));
			mainMenu.add(hideHabitMenuItem);
		}
		{
			JMenuItem deleteHabitMenuItem = new JMenuItem("Delete habit");
			deleteHabitMenuItem.setMnemonic('D');
			deleteHabitMenuItem.addActionListener(ignored -> openDeleteHabitDialog(tabs));
			mainMenu.add(deleteHabitMenuItem);
		}
		mainMenu.add(new JSeparator());
		{
			JMenuItem statsMenuItem = new JMenuItem("Statistics");
			statsMenuItem.setMnemonic('S');
			statsMenuItem.addActionListener(ignored -> StatisticsDialog.show(window, histories));
			mainMenu.add(statsMenuItem);
		}
		menuBar.add(mainMenu);

		JMenu settingsMenu = SettingsMenu.create(
			guiSettings, newSettings -> {
				guiSettings = newSettings;
				histories.forEachPanel(p -> p.newSettings(this));
			},
			getDataDirSupplier(), newDataDir -> {
				System.out.println("Trying to move data to directory '" + newDataDir + "'");
				Path oldDataDir = dataDir;
				dataDir = newDataDir;
				// Re-save everything in package `dev.andrybak.resoday.storage` into new data dir.
				// Hopefully in the future no new kinds of files will be saved in the data dir :-)
				SortOrder.read(oldDataDir).ifPresent(order -> order.save(getDataDirSupplier()));
				histories.forEachHistory(YearHistory::forceSave);
				CustomDataDirectory.save(configDir, dataDir);
			}
		);
		menuBar.add(settingsMenu);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		{
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
		}
		{
			JMenuItem helpMenuItem = new JMenuItem("Help");
			helpMenuItem.setMnemonic('H');
			helpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
			helpMenuItem.addActionListener(ignored -> HelpDialog.show(window));
			helpMenu.add(helpMenuItem);
		}
		{
			JMenuItem debugMenuItem = new JMenuItem("Debug");
			debugMenuItem.setMnemonic('D');
			debugMenuItem.addActionListener(ignored -> DebugDialog.show(window));
			helpMenu.add(debugMenuItem);
		}
		menuBar.add(helpMenu);

		window.setJMenuBar(menuBar);
	}

	private void openAddHabitDialog(JTabbedPane tabs) {
		Set<String> names = histories.histories()
			.map(YearHistory::getName)
			.collect(toSet());
		ChooseHabitNameDialog.show(window, "Add habit", "+", null, names::contains, habitName -> {
			String newId = HabitFiles.createNewId();
			String filename = HabitFiles.createNewFilename(newId, habitName);
			YearHistory newHistory = new YearHistory(getDataDirSupplier(), Path.of(filename), habitName, newId);
			HistoryPanel newPanel = new HistoryPanel(newHistory, this);
			histories.add(newHistory, newPanel);
			tabs.addTab(habitName, newPanel);
			Path newHabitPath = dataDir.resolve(filename);
			System.out.println("Added new habit '" + habitName + "' at path '" + newHabitPath.toAbsolutePath() + "'.");
		});
	}

	private void openRenameHabitDialog(JTabbedPane tabs) {
		Optional<HistoryPanel> maybeHistoryPanel = getCurrentHistoryPanel(tabs);
		if (maybeHistoryPanel.isEmpty()) {
			return;
		}

		String id = maybeHistoryPanel.orElseThrow().getHistoryId();
		String oldName = maybeHistoryPanel.orElseThrow().getHistoryName();
		// collect names which we won't allow to be used
		Set<String> names = histories.histories()
			.map(YearHistory::getName)
			.collect(toCollection(HashSet::new));
		names.remove(oldName); // existing name is OK
		ChooseHabitNameDialog.show(window, "Rename habit '" + oldName + "'", "Rename", oldName,
			names::contains,
			newHabitName -> {
				if (oldName.equals(newHabitName)) {
					System.out.println("Habit '" + oldName + "' wasn't renamed.");
					return;
				}
				histories.rename(id, newHabitName);
				int selectedIndex = tabs.getSelectedIndex();
				tabs.setTitleAt(selectedIndex, newHabitName);
				updateWindowTitle(tabs); // title depends on current tab's name
				System.out.println("Renamed habit '" + oldName + "' to '" + newHabitName + "'.");
			}
		);
	}

	private void openHideHabitDialog(JTabbedPane tabs) {
		Optional<HistoryPanel> maybeHistoryPanel = getCurrentHistoryPanel(tabs);
		if (maybeHistoryPanel.isEmpty()) {
			return;
		}

		HistoryPanel historyPanel = maybeHistoryPanel.orElseThrow();
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

	private void openDeleteHabitDialog(JTabbedPane tabs) {
		Optional<HistoryPanel> maybeHistoryPanel = getCurrentHistoryPanel(tabs);
		if (maybeHistoryPanel.isEmpty()) {
			return;
		}

		HistoryPanel historyPanel = maybeHistoryPanel.orElseThrow();
		switch (DeleteHabitDialog.show(window, historyPanel.getHistoryName())) {
		case YES:
			deleteHabit(tabs, historyPanel);
			break;
		case NO:
			break;
		}
	}

	private void deleteHabit(JTabbedPane tabs, HistoryPanel historyPanel) {
		int i = tabs.indexOfComponent(historyPanel);
		assert i >= 0;
		histories.delete(historyPanel.getHistoryId());
		tabs.removeTabAt(i);
	}

	private void openReorderHabitsDialog(JTabbedPane tabs) {
		String oldSelectedId = getCurrentHistoryPanel(tabs)
			.map(HistoryPanel::getHistoryId)
			.orElse(null);
		histories.reorder(this.window, getDataDirSupplier(), this, () -> {
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
					tabs.setSelectedComponent(maybeNewSelected.orElseThrow());
				} else {
					tabs.setSelectedIndex(0);
				}
			}
		});
	}

	private void updateWindowTitle(JTabbedPane tabs) {
		Optional<HistoryPanel> maybeHistoryPanel = getCurrentHistoryPanel(tabs);
		final String title;
		if (maybeHistoryPanel.isPresent()) {
			HistoryPanel historyPanel = maybeHistoryPanel.orElseThrow();
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

	public void go(Path configDir) {
		// all GUI manipulations should be done in AWT event dispatching thread
		SwingUtilities.invokeLater(() -> goInEdt(configDir));
	}

	private void goInEdt(Path configDir) {
		window.setMinimumSize(new Dimension(640, 480));
		window.setContentPane(content);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.pack();
		/*
		 * Can't use Logo.getMultiResolutionImage() here because of a JDK bug on Linux.
		 */
		window.setIconImages(Logo.getImages());
		window.setVisible(true);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				autoSaveTimer.stop();
				save(configDir);
				WindowPosition.from(window).save(configDir);
			}
		});
		Optional<WindowPosition> maybePos = WindowPosition.read(configDir);
		maybePos.ifPresent(pos -> pos.applyTo(window));
		autoSaveTimer.start();
	}

	private void autoSave(Path configDir) {
		System.out.println("Auto-saving...");
		save(configDir);
		System.out.println("Auto-saving complete.");
	}

	private void save(Path configDir) {
		histories.forEachHistory(YearHistory::save);
		guiSettingsSaver.save(configDir, guiSettings);
	}

	@Override
	public CalendarLayoutSetting getCalendarLayoutSetting() {
		return Objects.requireNonNullElse(guiSettings, GuiSettings.DEFAULT).getButtonLayoutSetting();
	}
}
