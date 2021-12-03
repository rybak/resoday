package dev.andrybak.resoday.gui.help;

import dev.andrybak.resoday.gui.util.Dialogs;
import dev.andrybak.resoday.gui.util.ScrollPanes;
import dev.andrybak.resoday.storage.SerializableYearHistory;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DebugDialog {
	private static final String[] SYSTEM_PROPERTY_KEYS = {
		"os.arch",
		"os.name",
		"os.version",
		"java.version",
		"java.vendor",
		"java.vendor.url",
		"file.encoding",
	};

	public static void show(Window parent) {
		JDialog d = create(parent);
		d.setVisible(true);
	}

	private static JDialog create(Window parent) {
		JDialog d = new JDialog(parent, "Debug", Dialog.ModalityType.APPLICATION_MODAL);
		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		JPanel content = new JPanel(new BorderLayout());
		Dialogs.setUpEscapeKeyClosing(d, content);

		JTable table = createTable();
		content.add(ScrollPanes.regular(table), BorderLayout.CENTER);
		{
			JButton copyButton = new JButton("Copy");
			copyButton.addActionListener(ignored -> copyToClipboard(table));
			content.add(copyButton, BorderLayout.SOUTH);
		}

		d.setContentPane(content);
		d.pack();
		d.setLocationRelativeTo(parent);
		return d;
	}

	private static void copyToClipboard(JTable table) {
		int n = table.getModel().getRowCount();
		int m = table.getModel().getColumnCount();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				sb.append(table.getModel().getValueAt(i, j).toString());
				if (j < m - 1) {
					sb.append('\t');
				}
			}
			sb.append('\n');
		}
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(sb.toString()), null);
	}

	private static JTable createTable() {
		DefaultTableModel tableModel = new DefaultTableModel(
			new Object[]{
				"Key        ",
				"Value      "
			},
			0
		);
		tableModel.addRow(new Object[]{"System.currentTimeMillis", System.currentTimeMillis()});
		tableModel.addRow(new Object[]{"ZonedDateTime.now (UTC)", ZonedDateTime.now(ZoneId.of("UTC"))});
		for (String key : SYSTEM_PROPERTY_KEYS) {
			String value = System.getProperty(key);
			tableModel.addRow(new Object[]{key, value});
		}
		Runtime runtime = Runtime.getRuntime();
		tableModel.addRow(new Object[]{"Runtime.availableProcessors", runtime.availableProcessors()});
		tableModel.addRow(new Object[]{"Runtime.maxMemory", runtime.maxMemory()});
		tableModel.addRow(new Object[]{"Runtime.totalMemory", runtime.totalMemory()});
		tableModel.addRow(new Object[]{"Runtime.version", Runtime.version()});
		tableModel.addRow(new Object[]{"Resoday.title", Version.extractTitle()});
		tableModel.addRow(new Object[]{"Resoday.version", Version.extractVersion()});
		tableModel.addRow(new Object[]{"Resoday.formatVersion", SerializableYearHistory.CURRENT_FORMAT_VERSION});
		return new JTable(tableModel);
	}

	/**
	 * Used for testing.
	 */
	public static void main(String... args) {
		JFrame frame = new JFrame("Debug dialog demo");
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
