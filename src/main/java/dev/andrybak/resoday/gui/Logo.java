package dev.andrybak.resoday.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Objects;

public class Logo {
	private static final String APP_ICON_64_FILENAME = "resoday-icon-64px.png";

	private Logo() {
		throw new UnsupportedOperationException();
	}

	public static Image getFixedResolutionImage() {
		URL resodayIconUrl = Objects.requireNonNull(MainGui.class.getResource(APP_ICON_64_FILENAME));
		return Toolkit.getDefaultToolkit().getImage(resodayIconUrl);
	}
}
