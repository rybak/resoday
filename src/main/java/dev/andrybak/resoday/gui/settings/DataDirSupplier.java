package dev.andrybak.resoday.gui.settings;

import java.nio.file.Path;

@FunctionalInterface
public interface DataDirSupplier {
	Path getDataDir();
}
