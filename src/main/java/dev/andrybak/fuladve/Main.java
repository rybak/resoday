package dev.andrybak.fuladve;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
	private static final String APP_NAME = "Every Day Calendar";

	private final JFrame window = new JFrame(APP_NAME);
	private final JPanel content;
	private final HistoryPanel historyPanel;

	Main(String statePathStr) {
		content = new JPanel(new BorderLayout());
		historyPanel = new HistoryPanel(statePathStr);
		content.add(historyPanel, BorderLayout.CENTER);
	}

	public static void main(String... args) {
		if (args.length < 1) {
			System.err.println("Which file to open?");
			System.exit(1);
			return;
		}
		new Main(args[0]).go();
	}

	private void go() {
		window.setTitle(historyPanel.getPath().getFileName() + " - " + APP_NAME);
		window.setMinimumSize(new Dimension(640, 480));
		window.setContentPane(content);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				historyPanel.save();
			}
		});
	}
}
