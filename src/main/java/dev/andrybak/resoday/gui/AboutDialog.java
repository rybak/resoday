package dev.andrybak.resoday.gui;

import dev.andrybak.resoday.StringConstants;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

class AboutDialog {

	private static JDialog create(Component parentComponent) {
		JDialog d = new JDialog(SwingUtilities.getWindowAncestor(parentComponent), "About " + StringConstants.APP_NAME_GUI,
			Dialog.ModalityType.MODELESS);

		JPanel content = new JPanel(null);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		JLabel label = new JLabel(StringConstants.APP_NAME_GUI + " by Andrei Rybak © 2020");
		content.add(Box.createVerticalStrut(10));
		content.add(label);
		content.add(Box.createVerticalStrut(20));

		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setText("This application emulates the " +
			"<a href=\"http://www.simonegiertz.com/every-day-calendar\">Every Day Calendar</a> by Simone Giertz " +
			"shown in " +
			"<a href=\"https://www.youtube.com/watch?v=Pm9CQn07OjU&t=4m26s\">Veritasium video titled " +
			"<i>Why Most Resolutions Fail &amp; How To Succeed</i></a>.");
		textPane.setEditable(false);
		JTextField urlFallbackDisplay = new JTextField("");
		setUpHyperlinkListener(textPane, urlFallbackDisplay);
		textPane.setBackground(label.getBackground());
		content.add(textPane);
		content.add(urlFallbackDisplay);
		content.add(Box.createVerticalStrut(10));

		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Dialogs.setUpEscapeKeyClosing(d, content);
		d.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				d.dispose();
			}
		});
		d.setUndecorated(true);
		d.setContentPane(content);
		d.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		d.setSize(((int)(screenSize.getWidth() / 4.5)), ((int)(screenSize.getHeight() / 5)));
		d.setResizable(false);
		d.setLocationRelativeTo(parentComponent);

		return d;
	}

	public static void show(Component parentComponent) {
		JDialog d = create(parentComponent);
		d.setVisible(true);
	}

	private static void setUpHyperlinkListener(JTextPane textPane, JTextField urlFallbackDisplay) {
		urlFallbackDisplay.setVisible(false);
		urlFallbackDisplay.setEditable(false);
		textPane.addHyperlinkListener(hyperlinkEvent -> {
			HyperlinkEvent.EventType eventType = hyperlinkEvent.getEventType();
			if (HyperlinkEvent.EventType.ENTERED.equals(eventType)) {
				textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (HyperlinkEvent.EventType.EXITED.equals(eventType)) {
				textPane.setCursor(Cursor.getDefaultCursor());
			} else if (HyperlinkEvent.EventType.ACTIVATED.equals(eventType)) {
				URL url = hyperlinkEvent.getURL();
				showUrl(url, () -> showUrlFallback(urlFallbackDisplay, url));
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
			fallback.run();
		}
	}

	private static void showUrlFallback(JTextField urlField, URL url) {
		System.out.println("Fallback for " + url);
		urlField.setText(url.toString());
		urlField.setVisible(true);
		urlField.getParent().revalidate();
		urlField.getParent().repaint();
	}

	/**
	 * Used for testing.
	 */
	public static void main(String... args) {
		JFrame frame = new JFrame("About dialog demo");
		JPanel content = new JPanel();
		frame.setContentPane(content);
		frame.setVisible(true);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((int)(screenSize.getWidth() / 2), (int)(screenSize.getHeight() / 2));
		JDialog aboutDialog = create(content);
		aboutDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				frame.dispose();
			}
		});
		aboutDialog.setVisible(true);
	}
}
