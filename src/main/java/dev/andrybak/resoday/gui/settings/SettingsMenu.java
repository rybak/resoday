package dev.andrybak.resoday.gui.settings;

import dev.andrybak.resoday.settings.gui.CalendarLayoutSetting;
import dev.andrybak.resoday.settings.gui.GuiSettings;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import java.util.function.Consumer;

public final class SettingsMenu {
	public static JMenu create(GuiSettings current, Consumer<GuiSettings> newSettingsConsumer) {
		JMenu menu = new JMenu("Settings");
		menu.setMnemonic('S');
		{
			JMenu calendarLayoutMenu = new JMenu("Calendar layout");
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
		return menu;
	}
}
