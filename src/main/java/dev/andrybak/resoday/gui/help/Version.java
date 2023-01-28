package dev.andrybak.resoday.gui.help;

import dev.andrybak.resoday.StringConstants;

public class Version {
	private static final String DIRTY_DEVELOPMENT_BUILD = "dirty development build";

	private Version() {
		throw new UnsupportedOperationException();
	}

	public static String extractVersion() {
		String versionFromPackage = versionFromPackage();
		if (versionFromPackage == null) {
			return DIRTY_DEVELOPMENT_BUILD;
		}
		return versionFromPackage;
	}

	public static String extractTitle() {
		String t = Version.class.getPackage().getImplementationTitle();
		if (t != null) {
			return t;
		}
		return StringConstants.APP_NAME_GUI + " – " + DIRTY_DEVELOPMENT_BUILD;
	}

	private static String versionFromPackage() {
		return Version.class.getPackage().getImplementationVersion();
	}
}
