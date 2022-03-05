package dev.andrybak.resoday.gui.help;

public class Version {
	private static final String DIRTY_DEVELOPMENT_BUILD = "dirty development build";

	public static String extractVersion() {
		String versionFromPackage = versionFromPackage();
		if (versionFromPackage == null) {
			return DIRTY_DEVELOPMENT_BUILD;
		}
		return versionFromPackage;
	}

	public static String extractTitle() {
		return Version.class.getPackage().getImplementationTitle();
	}

	private static String versionFromPackage() {
		return Version.class.getPackage().getImplementationVersion();
	}
}
