package dev.andrybak.resoday.gui.help;

import dev.andrybak.resoday.StringConstants;
import dev.andrybak.resoday.gui.Dialogs;
import dev.andrybak.resoday.gui.Hyperlinks;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public final class AboutDialog {
	private static final String ABOUT_HTML_RESOURCE_FILENAME = "about.html";

	private static JDialog create(Window parent) {
		JDialog d = new JDialog(parent, "About " + StringConstants.APP_NAME_GUI,
			Dialog.ModalityType.APPLICATION_MODAL);

		JPanel content = new JPanel(null);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		JLabel label = new JLabel(StringConstants.APP_NAME_GUI + " by Andrei Rybak © 2020–2021");
		{
			Font labelFont = label.getFont();
			label.setFont(labelFont.deriveFont(labelFont.getSize2D() * 1.3f));
		}
		content.add(Box.createVerticalStrut(10));
		content.add(label);
		content.add(Box.createVerticalStrut(20));

		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		try {
			textPane.setPage(AboutDialog.class.getResource(ABOUT_HTML_RESOURCE_FILENAME));
		} catch (IOException e) {
			textPane.setText(e.getMessage());
			System.err.println("Could not open '" + ABOUT_HTML_RESOURCE_FILENAME + "': " + e);
			e.printStackTrace();
		}
		textPane.setEditable(false);
		Hyperlinks.setUpHyperlinkListener(textPane);
		textPane.setBackground(label.getBackground());
		content.add(textPane);
		content.add(Box.createVerticalStrut(10));

		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Dialogs.setUpEscapeKeyClosing(d, content);
		d.setContentPane(content);
		d.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		d.setSize(((int)(screenSize.getWidth() / 4.5)), ((int)(screenSize.getHeight() / 5)));
		d.setResizable(false);
		d.setLocationRelativeTo(parent);

		return d;
	}

	public static void show(Window parent) {
		JDialog d = create(parent);
		d.setVisible(true);
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
		JDialog aboutDialog = create(frame);
		aboutDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				frame.dispose();
			}
		});
		aboutDialog.setVisible(true);
	}
}
