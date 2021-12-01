package dev.andrybak.resoday.gui.util;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Utility methods for handling hyperlinks in {@link JComponent}s.
 */
public final class Hyperlinks {
	private static final String XDG_OPEN_EXECUTABLE = "xdg-open";

	private Hyperlinks() {
		throw new UnsupportedOperationException();
	}

	public static void setUpHyperlinkListener(JTextPane textPane) {
		textPane.addHyperlinkListener(hyperlinkEvent -> {
			HyperlinkEvent.EventType eventType = hyperlinkEvent.getEventType();
			if (HyperlinkEvent.EventType.ENTERED.equals(eventType)) {
				textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (HyperlinkEvent.EventType.EXITED.equals(eventType)) {
				textPane.setCursor(Cursor.getDefaultCursor());
			} else if (HyperlinkEvent.EventType.ACTIVATED.equals(eventType)) {
				URL url = hyperlinkEvent.getURL();
				showUrl(url, () -> showUrlFallback(textPane, url));
			}
		});
	}

	private static void showUrl(URL url, Runnable fallback) {
		if (url == null) {
			return;
		}
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(url.toURI());
			} catch (IOException | URISyntaxException e) {
				throw new IllegalStateException("Bad URLs in about text", e);
			} catch (UnsupportedOperationException e) {
				fallback.run();
			}
		} else {
			String pathEnvVar;
			try {
				pathEnvVar = System.getenv("PATH");
			} catch (SecurityException ignored) {
				fallback.run();
				return;
			}
			if (pathEnvVar == null) {
				pathEnvVar = "";
			}
			// https://stackoverflow.com/a/23539220/1083697
			boolean existsInPath = Stream.of(pathEnvVar.split(Pattern.quote(File.pathSeparator)))
				.map(Paths::get)
				.anyMatch(path -> Files.isExecutable(path.resolve(XDG_OPEN_EXECUTABLE)));
			if (existsInPath) {
				try {
					Runtime.getRuntime().exec(XDG_OPEN_EXECUTABLE + " " + url);
				} catch (IOException ignored) {
					fallback.run();
				}
			} else {
				fallback.run();
			}
		}
	}

	private static void showUrlFallback(JComponent parent, URL url) {
		System.out.println("Fallback for " + url);
		JPanel content = new JPanel(new BorderLayout());
		{
			content.add(new JLabel("Could not open URL:"), BorderLayout.NORTH);
		}
		{
			JTextField urlField = new JTextField(url.toString());
			urlField.setText(url.toString());
			content.add(urlField, BorderLayout.CENTER);
		}
		JOptionPane.showMessageDialog(parent, content);
	}
}
