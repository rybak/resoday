package dev.andrybak.resoday.gui.help;

public class Version {

	public static String extractVersion() {
		String versionFromPackage = versionFromPackage();
		if (versionFromPackage == null) {
			return "dirty development build";
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
