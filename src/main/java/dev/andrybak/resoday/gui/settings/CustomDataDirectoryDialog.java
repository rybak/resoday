package dev.andrybak.resoday.gui.settings;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Component;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class CustomDataDirectoryDialog {
	public static void main(String[] args) {
		JFrame jFrame = new JFrame("Dir chooser demo");
		jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		jFrame.setVisible(true);
		Optional<Path> maybePath = show(jFrame);
		System.out.println(maybePath.map(p -> "Got new path: " + p).orElse("Got no path."));
	}

	public static Optional<Path> show(Component owner) {
		JFileChooser dirChooser = new JFileChooser(Paths.get(".").toFile());
		dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int selectedOption = dirChooser.showDialog(owner, "Select");
		if (selectedOption == JFileChooser.APPROVE_OPTION) {
			File d = dirChooser.getSelectedFile();
			System.out.println("Chosen: " + d);
			return Optional.of(d.toPath());
		} else {
			return Optional.empty();
		}
	}
}
