package dev.andrybak.resoday.gui.help;

public class Version {

	public static String extractVersion() {
		String versionFromPackage = versionFromPackage();
		if (versionFromPackage == null) {
			return "dirty development build";
		}
		return versionFromPackage;
	}

	private static String versionFromPackage() {
		return Version.class.getPackage().getImplementationVersion();
	}
}
