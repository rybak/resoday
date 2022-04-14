package dev.andrybak.resoday.gui.help;

import dev.andrybak.resoday.StringConstants;
import dev.andrybak.resoday.gui.util.Dialogs;
import dev.andrybak.resoday.gui.util.ScrollPanes;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public final class HelpDialog {
	private static final int MIN_SIZE_PIXELS = 400;
	private static final String HELP_HTML_RESOURCE_FILENAME = "help.html";
	private static final String HTML_UTF_8_CONTENT_TYPE = "text/html;charset=UTF-8";

	private static JDialog create(Window parent) {
		JDialog d = new JDialog(parent, StringConstants.APP_NAME_GUI + " help",
			Dialog.ModalityType.APPLICATION_MODAL);
		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		d.setContentPane(createHelpPanel(d));

		d.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		d.setSize(Math.max(MIN_SIZE_PIXELS, ((int)(screenSize.getWidth() * 0.3))),
			Math.max(MIN_SIZE_PIXELS, ((int)(screenSize.getHeight() * 0.25))));
		d.setLocationRelativeTo(parent);

		return d;
	}

	private static JPanel createHelpPanel(JDialog d) {
		JPanel helpPanel = new JPanel(new BorderLayout());

		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		/*
		 * We don't want to do `textPane.setFocusable(false)`, because it makes it impossible for the
		 * user to select text.  And text panes that do not allow selecting text are dumb.
		 * However, since JTextPane takes away focus, it means that it will it up escape key presses
		 * mean for the JDialog.  So we have to set up a "separate" hotkey.
		 */
		Dialogs.setUpEscapeKeyClosing(d, textPane);
		textPane.setContentType(HTML_UTF_8_CONTENT_TYPE);
		try {
			textPane.setPage(HelpDialog.class.getResource(HELP_HTML_RESOURCE_FILENAME));
		} catch (IOException e) {
			textPane.setText(e.getMessage());
			System.err.println("Could not open '" + HELP_HTML_RESOURCE_FILENAME + "': " + e);
			e.printStackTrace();
		}
		textPane.setBackground(helpPanel.getBackground());
		helpPanel.add(ScrollPanes.vertical(textPane), BorderLayout.CENTER);

		return helpPanel;
	}

	public static void show(Window parent) {
		JDialog d = create(parent);
		d.setVisible(true);
	}

	/**
	 * Used for testing.
	 */
	public static void main(String... args) {
		JFrame frame = new JFrame("Help dialog demo");
		JPanel content = new JPanel();
		frame.setContentPane(content);
		frame.setVisible(true);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((int)(screenSize.getWidth() / 2), (int)(screenSize.getHeight() / 2));
		JDialog d = create(frame);
		d.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				frame.dispose();
			}
		});
		d.setVisible(true);
	}
}
