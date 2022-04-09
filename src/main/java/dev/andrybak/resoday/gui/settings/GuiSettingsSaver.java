package dev.andrybak.resoday.gui.settings;

import dev.andrybak.resoday.settings.gui.GuiSettings;

import java.nio.file.Path;
import java.util.Objects;

public final class GuiSettingsSaver {
	private GuiSettings oldSettings;

	public void save(Path configDir, GuiSettings newSettings) {
		if (Objects.equals(oldSettings, newSettings)) {
			return;
		}
		if (newSettings.save(configDir)) {
			oldSettings = newSettings;
		}
	}

}
