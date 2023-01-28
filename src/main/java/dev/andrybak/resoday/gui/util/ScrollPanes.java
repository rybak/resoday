package dev.andrybak.resoday.gui.util;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import java.awt.Component;

/**
 * Utility methods for wrapping {@link Component}s and {@link JComponent}s into scrollable panes.
 */
public class ScrollPanes {
	/**
	 * By default, scrollbars in Java Swing are slow to scroll. 15 is an arbitrarily chosen "unit increment", that
	 * seems to be usable.
	 */
	private static final int UNIT_INCREMENT = 15;

	private ScrollPanes() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return {@link JScrollPane} that can only be scrolled up and down.
	 */
	public static JScrollPane vertical(Component view) {
		JScrollPane scrollPane = new JScrollPane(view, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(UNIT_INCREMENT);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(UNIT_INCREMENT);
		return scrollPane;
	}

	/**
	 * Uses default constructor: {@link JScrollPane#JScrollPane()}.
	 */
	public static JScrollPane regular(JComponent view) {
		JScrollPane scrollPane = new JScrollPane(view);
		scrollPane.getVerticalScrollBar().setUnitIncrement(UNIT_INCREMENT);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(UNIT_INCREMENT);
		return scrollPane;
	}
}
