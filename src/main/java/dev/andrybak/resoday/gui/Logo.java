package dev.andrybak.resoday.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BaseMultiResolutionImage;
import java.net.URL;
import java.util.Objects;

public class Logo {
	private static final String APP_ICON_64_FILENAME = "resoday-icon-64px.png";
	private static final String APP_ICON_32_FILENAME = "resoday-icon-32px.png";

	private Logo() {
		throw new UnsupportedOperationException();
	}

	public static Image getResodayImage() {
		URL resodayIcon64Url = Objects.requireNonNull(MainGui.class.getResource(APP_ICON_64_FILENAME));
		URL resodayIcon32Url = Objects.requireNonNull(MainGui.class.getResource(APP_ICON_32_FILENAME));
		var icon64 = Toolkit.getDefaultToolkit().getImage(resodayIcon64Url);
		var icon32 = Toolkit.getDefaultToolkit().getImage(resodayIcon32Url);
		return new BaseMultiResolutionImage(icon32, icon64);
	}
}
