package dev.andrybak.resoday.gui.settings;

import dev.andrybak.resoday.settings.gui.CalendarLayoutSetting;
import dev.andrybak.resoday.settings.gui.GuiSettings;
import dev.andrybak.resoday.settings.gui.HabitCalendarLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public final class SettingsMenu {
	private SettingsMenu() {
		throw new UnsupportedOperationException();
	}

	public static JMenu create(GuiSettings current, Consumer<GuiSettings> newSettingsConsumer,
		DataDirSupplier dataDirSupplier, Consumer<Path> newDataDirConsumer,
		HabitCalendarLayoutsOwner layoutsOwner)
	{
		JMenu menu = new JMenu("Settings");
		menu.setMnemonic('S');
		{
			JMenu habitLayoutItem = new JMenu("Habit calendar layout");
			habitLayoutItem.setToolTipText("Change layout of an individual habit (tab)");
			habitLayoutItem.setMnemonic('L');
			Optional<HabitCalendarLayout> maybeCurrentLayout = layoutsOwner.getCurrentTabLayout();
			habitLayoutItem.setEnabled(maybeCurrentLayout.isPresent());
			layoutsOwner.addVisibleHabitCountListener(newVisibleHabitCount ->
				habitLayoutItem.setEnabled(newVisibleHabitCount > 0));
			HabitCalendarLayout currentHabitLayout = maybeCurrentLayout.orElse(HabitCalendarLayout.DEFAULT);
			ButtonGroup habitLayoutButtonGroup = new ButtonGroup();
			for (HabitCalendarLayout hcl : HabitCalendarLayout.values()) {
				JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(hcl.getGuiName(), hcl == currentHabitLayout);
				layoutsOwner.addVisibleHabitChangedListener(() ->
					layoutsOwner.getCurrentTabLayout().ifPresent(layout -> menuItem.setSelected(hcl == layout)));
				habitLayoutButtonGroup.add(menuItem);
				menuItem.addActionListener(ignored -> layoutsOwner.acceptNewCurrentHabitLayout(hcl));
				habitLayoutItem.add(menuItem);
			}
			menu.add(habitLayoutItem);
		}
		{
			JMenu calendarLayoutMenu = new JMenu("Default calendar layout");
			calendarLayoutMenu.setToolTipText("Calendar layout applied to all habits by default");
			calendarLayoutMenu.setMnemonic('C');
			CalendarLayoutSetting currentCalendarLayout = current.getButtonLayoutSetting();
			ButtonGroup calendarLayoutButtonGroup = new ButtonGroup();
			for (CalendarLayoutSetting cl : CalendarLayoutSetting.values()) {
				JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(cl.getGuiName(), cl == currentCalendarLayout);
				calendarLayoutButtonGroup.add(menuItem);
				menuItem.addActionListener(ignored -> newSettingsConsumer.accept(new GuiSettings(cl)));
				calendarLayoutMenu.add(menuItem);
			}
			menu.add(calendarLayoutMenu);
		}
		{
			JMenu dataDirMenu = new JMenu("Data directory");
			dataDirMenu.setMnemonic('D');
			{
				JMenuItem openDataDir = new JMenuItem("Open data directory");
				openDataDir.setMnemonic('O');
				openDataDir.addActionListener(ignored -> {
					JOptionPane.showMessageDialog(
						JOptionPane.getFrameForComponent(menu),
						"Edit the files only if you know what you're doing.",
						"Be careful",
						JOptionPane.WARNING_MESSAGE
					);
					Path dataDir = dataDirSupplier.getDataDir();
					System.out.println("Opening '" + dataDir.toAbsolutePath() + "'...");
					Desktop desktop = Desktop.getDesktop();
					if (desktop.isSupported(Desktop.Action.OPEN)) {
						try {
							desktop.open(dataDir.toFile());
						} catch (IOException e) {
							showDataDirError(menu, dataDir);
						}
					} else {
						showDataDirError(menu, dataDir);
					}
				});
				dataDirMenu.add(openDataDir);
			}
			{
				JMenuItem customDataDirMenuItem = new JMenuItem("Custom data directory");
				customDataDirMenuItem.setMnemonic('U');
				customDataDirMenuItem.addActionListener(ignored -> {
					final int confirmedBefore = JOptionPane.showConfirmDialog(
						JOptionPane.getFrameForComponent(menu),
						"Setting custom data directory is an advanced action." +
							" Make sure you know what you are doing with the directory." +
							" Are you sure you want to continue?",
						"Advanced setting",
						JOptionPane.YES_NO_OPTION
					);
					if (confirmedBefore != JOptionPane.YES_OPTION) {
						System.out.println("Aborted choosing custom directory after first warning.");
						return;
					}

					Optional<Path> maybePath = CustomDataDirectoryDialog.show(JOptionPane.getFrameForComponent(menu));

					if (maybePath.isEmpty()) {
						System.out.println("Aborted choosing custom directory after file chooser.");
						return;
					}
					final Path newDataDir = maybePath.orElseThrow();
					if (dataDirSupplier.getDataDir().equals(newDataDir)) {
						System.out.println("Same directory was chosen: " + newDataDir.toAbsolutePath());
						System.out.println("Aborted changing data directory.");
						return;
					}
					if (!Files.exists(newDataDir) || !Files.isDirectory(newDataDir)) {
						JOptionPane.showMessageDialog(
							JOptionPane.getFrameForComponent(menu),
							"Could not find directory '" + newDataDir + "'.",
							"Error",
							JOptionPane.ERROR_MESSAGE
						);
						return;
					}
					final int confirmedAfter = JOptionPane.showConfirmDialog(
						JOptionPane.getFrameForComponent(menu),
						getSecondDataDirWarningMessage(newDataDir),
						"Set custom data directory?",
						JOptionPane.YES_NO_OPTION
					);
					if (confirmedAfter != JOptionPane.YES_OPTION) {
						System.out.println("Aborted choosing custom directory after second warning.");
						return;
					}
					newDataDirConsumer.accept(newDataDir);
				});
				dataDirMenu.add(customDataDirMenuItem);
			}
			menu.add(dataDirMenu);
		}
		return menu;
	}

	private static void showDataDirError(JMenu menu, Path dataDir) {
		JPanel message = new JPanel(new BorderLayout());
		{
			message.add(new JLabel("Could not open the data directory automatically."), BorderLayout.NORTH);
			message.add(new JLabel("You can copy the path from the field below."), BorderLayout.CENTER);
			JTextField textField = new JTextField(dataDir.toAbsolutePath().toString());
			textField.setEditable(false);
			message.add(textField, BorderLayout.SOUTH);
		}
		JOptionPane.showMessageDialog(
			JOptionPane.getFrameForComponent(menu),
			message,
			"Error",
			JOptionPane.ERROR_MESSAGE
		);
	}

	private static Object getSecondDataDirWarningMessage(Path newDataDir) {
		JPanel message = new JPanel(new BorderLayout());
		message.add(new JLabel("Are you sure you want to use '" + newDataDir.toAbsolutePath() + "' as custom data dir?"),
			BorderLayout.CENTER);
		message.add(new JLabel("All files in the directory will be overwritten."), BorderLayout.SOUTH);
		return message;
	}
}
