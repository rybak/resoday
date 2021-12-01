package dev.andrybak.resoday.gui.edithabits;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

public class HideHabitDialog {
	 public static Response show(Component parentComponent, String habitName) {
		int result = JOptionPane.showConfirmDialog(parentComponent,
			"Do you really want to hide habit '" + habitName + "'?",
			"Hide habit '" + habitName + "'",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE
		);
		if (result == JOptionPane.YES_OPTION) {
			return Response.YES;
		} else {
			return Response.NO;
		}
	}

	public static void main(String... args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Hide habit dialog demo");
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			JPanel content = new JPanel();
			content.setPreferredSize(new Dimension(400, 250));
			frame.setContentPane(content);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation((int)(screenSize.getWidth() / 2), (int)(screenSize.getHeight() / 2));
			frame.setVisible(true);

			Response r = show(content, "foobar");
			System.out.println("Got response " + r);
			System.exit(0);
		});
	}

	public enum Response {
		YES,
		NO
	}
}
