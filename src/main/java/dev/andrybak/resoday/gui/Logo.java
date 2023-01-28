package dev.andrybak.resoday.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Objects;

public class Logo {
	private static final String APP_ICON_FILENAME = "resoday-icon.png";

	private Logo() {
		throw new UnsupportedOperationException();
	}

	public static Image getResodayImage() {
		URL resodayIconUrl = Objects.requireNonNull(MainGui.class.getResource(APP_ICON_FILENAME));
		return Toolkit.getDefaultToolkit().getImage(resodayIconUrl);
	}
}
