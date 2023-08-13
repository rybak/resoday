package dev.andrybak.resoday.settings.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class GuiSettings {
	public static final GuiSettings DEFAULT = new GuiSettings(CalendarLayoutSetting.COLUMNS_SIMONE_GIERTZ);
	private static final Path GUI_SETTINGS_FILE = Paths.get("gui_settings.json");
	private static final Gson GSON = new GsonBuilder().create();
	private final CalendarLayoutSetting calendarLayoutSetting;

	public GuiSettings(CalendarLayoutSetting calendarLayoutSetting) {
		this.calendarLayoutSetting = Objects.requireNonNull(calendarLayoutSetting);
	}

	public static GuiSettings read(Path configDir) {
		Path source = configDir.resolve(GUI_SETTINGS_FILE);
		if (Files.notExists(source)) {
			return DEFAULT;
		}
		if (!Files.isReadable(source)) {
			JOptionPane.showMessageDialog(
				JOptionPane.getRootFrame(),
				String.format("Cannot open file '%s'", source),
				"Error",
				JOptionPane.ERROR_MESSAGE
			);
			System.err.printf("Can't read '%s'%n", source);
			return DEFAULT;
		}
		try (BufferedReader r = Files.newBufferedReader(source)) {
			return GSON.fromJson(r, GuiSettings.class);
		} catch (Exception e) {
			System.err.println("Could not read from '" + source + "'");
			e.printStackTrace();
			return DEFAULT;
		}
	}

	public boolean save(Path configDir) {
		Path destination = configDir.resolve(GUI_SETTINGS_FILE);
		try (Writer w = Files.newBufferedWriter(destination)) {
			GSON.toJson(this, w);
			return true;
		} catch (IOException e) {
			System.err.println("Could not write to '" + destination + "'");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Default calendar layout for habits that don't specify their own setting.
	 */
	public CalendarLayoutSetting getButtonLayoutSetting() {
		return calendarLayoutSetting;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GuiSettings that = (GuiSettings)o;
		return calendarLayoutSetting == that.calendarLayoutSetting;
	}

	@Override
	public int hashCode() {
		return Objects.hash(calendarLayoutSetting);
	}
}
