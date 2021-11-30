package dev.andrybak.resoday.gui.help;

import dev.andrybak.resoday.StringConstants;
import dev.andrybak.resoday.gui.Dialogs;
import dev.andrybak.resoday.gui.Hyperlinks;
import dev.andrybak.resoday.gui.ScrollPanes;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
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
	private static final String LICENSE_TXT_RESOURCE_FILENAME = "LICENSE.txt";
	private static final String LICENSE_HTML_RESOURCE_FILENAME = "license.html";
	private static final String THIRD_PARTY_HTML_RESOURCE_FILENAME = "third-party-software.html";

	private static JDialog create(Window parent) {
		JDialog d = new JDialog(parent, "About " + StringConstants.APP_NAME_GUI,
			Dialog.ModalityType.APPLICATION_MODAL);
		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		JPanel content = new JPanel(new BorderLayout());
		Dialogs.setUpEscapeKeyClosing(d, content);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab(StringConstants.APP_NAME_GUI, createResodayTab(d));
		tabs.addTab("License", createLicenseTab(d));
		tabs.addTab("Third-party software", createThirdPartyTab(d));
		content.add(tabs, BorderLayout.CENTER);

		d.setContentPane(content);
		d.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		d.setSize(((int)(screenSize.getWidth() * 0.3)), ((int)(screenSize.getHeight() * 0.25)));
		d.setLocationRelativeTo(parent);

		return d;
	}

	private static JPanel createResodayTab(JDialog d) {
		JPanel resodayTab = new JPanel(new BorderLayout());

		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		/*
		 * We don't want to do `textPane.setFocusable(false)`, because it makes it impossible for the
		 * user to select text.  And text panes that do not allow selecting text are dumb.
		 * However, since JTextPane takes away focus, it means that it will it up escape key presses
		 * mean for the JDialog.  So we have to set up a "separate" hotkey.
		 */
		Dialogs.setUpEscapeKeyClosing(d, textPane); // JTextPane takes away focus,
		textPane.setContentType("text/html");
		try {
			textPane.setPage(AboutDialog.class.getResource(ABOUT_HTML_RESOURCE_FILENAME));
		} catch (IOException e) {
			textPane.setText(e.getMessage());
			System.err.println("Could not open '" + ABOUT_HTML_RESOURCE_FILENAME + "': " + e);
			e.printStackTrace();
		}
		Hyperlinks.setUpHyperlinkListener(textPane);
		textPane.setBackground(resodayTab.getBackground());
		resodayTab.add(ScrollPanes.vertical(textPane), BorderLayout.CENTER);

		return resodayTab;
	}

	private static JPanel createLicenseTab(JDialog d) {
		JPanel licenseTab = new JPanel(new BorderLayout());
		{
			JTextPane header = new JTextPane();
			header.setEditable(false);
			Dialogs.setUpEscapeKeyClosing(d, header);
			try {
				header.setPage(AboutDialog.class.getResource(LICENSE_HTML_RESOURCE_FILENAME));
			} catch (IOException e) {
				header.setText(e.getMessage());
				System.err.println("Could not open '" + LICENSE_HTML_RESOURCE_FILENAME + "': " + e);
				e.printStackTrace();
			}
			licenseTab.add(header, BorderLayout.NORTH);
		}
		{
			JTextPane licenseTextPane = new JTextPane();
			licenseTextPane.setEditable(false);
			licenseTextPane.setContentType("text/plain");
			licenseTextPane.setFont(Font.getFont("monospace"));
			Dialogs.setUpEscapeKeyClosing(d, licenseTextPane);
			try {
				licenseTextPane.setPage(AboutDialog.class.getResource(LICENSE_TXT_RESOURCE_FILENAME));
			} catch (IOException e) {
				licenseTextPane.setText(e.getMessage());
				System.err.println("Could not open '" + LICENSE_TXT_RESOURCE_FILENAME + "': " + e);
				e.printStackTrace();
			}
			licenseTab.add(ScrollPanes.regular(licenseTextPane), BorderLayout.CENTER);
		}
		return licenseTab;
	}

	private static JPanel createThirdPartyTab(JDialog d) {
		JPanel thirdPartyTab = new JPanel(new BorderLayout());
		{
			JTextPane thirdPartyTextPane = new JTextPane();
			thirdPartyTextPane.setEditable(false);
			thirdPartyTextPane.setContentType("text/html");
			Hyperlinks.setUpHyperlinkListener(thirdPartyTextPane);
			Dialogs.setUpEscapeKeyClosing(d, thirdPartyTextPane);
			try {
				thirdPartyTextPane.setPage(AboutDialog.class.getResource(THIRD_PARTY_HTML_RESOURCE_FILENAME));
			} catch (IOException e) {
				thirdPartyTextPane.setText(e.getMessage());
				System.err.println("Could not open '" + THIRD_PARTY_HTML_RESOURCE_FILENAME + "': " + e);
				e.printStackTrace();
			}
			thirdPartyTab.add(ScrollPanes.regular(thirdPartyTextPane), BorderLayout.CENTER);
		}
		return thirdPartyTab;
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
