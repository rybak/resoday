package dev.andrybak.resoday.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BaseMultiResolutionImage;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class Logo {
	private static final String APP_ICON_64_FILENAME = "resoday-icon-64px.png";
	private static final String APP_ICON_32_FILENAME = "resoday-icon-32px.png";

	private Logo() {
		throw new UnsupportedOperationException();
	}

	public static Image getFixedResolutionImage() {
		return getImage(APP_ICON_64_FILENAME);
	}

	public static List<Image> getImages() {
		return List.of(
			getImage(APP_ICON_32_FILENAME),
			getFixedResolutionImage()
		);
	}

	public static Image getMultiResolutionImage() {
		Image icon32 = getImage(APP_ICON_32_FILENAME);
		Image icon64 = getFixedResolutionImage();
		return new BaseMultiResolutionImage(icon32, icon64);
	}

	private static Image getImage(String filename) {
		URL url = Objects.requireNonNull(MainGui.class.getResource(filename));
		return Toolkit.getDefaultToolkit().getImage(url);
	}
}
