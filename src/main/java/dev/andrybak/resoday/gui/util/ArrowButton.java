package dev.andrybak.resoday.gui.util;

import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * A {@link JButton} with a triangle arrow pointing in a {@link Direction}.
 * <p>
 * This class is needed, because {@link javax.swing.plaf.basic.BasicArrowButton} does not interact well with focus
 * traversal, because it is designed to be used in scrollbars, not in custom UIs. That is, it is not possible to get
 * focus to a {@code BasicArrowButton} using just the tab key.
 * </p>
 */
public final class ArrowButton extends JButton {
	private final Direction direction;

	public ArrowButton(Direction direction) {
		super("");
		this.direction = direction;
	}

	/**
	 * Paint a neat right triangle (pointing vertex has the angle of 90°) pointing in the {@link #direction}.
	 */
	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D)graphics.create();
		final int w = getWidth();
		final int h = getHeight();
		final int xMiddle = w / 2;
		int mainSize = (int)(w / 3.2); // chosen to approximate size of triangles in BasicArrowButton
		final int left = xMiddle - mainSize;
		final int right = xMiddle + mainSize;
		final int yMiddle = h / 2;
		final int top = yMiddle - mainSize / 2;
		int bottom = yMiddle + mainSize / 2;
		if (bottom - top < mainSize) {
			bottom++;
		}
		Polygon arrow = switch (direction) {
			case UP -> new Polygon(
				new int[]{
					left, xMiddle, right
				},
				new int[]{
					bottom, top, bottom
				},
				3
			);
			case DOWN -> new Polygon(
				new int[]{
					right - 1, xMiddle, left + 1
				},
				new int[]{
					top + 1, bottom, top + 1
				},
				3
			);
		};
		g.setColor(isEnabled() ? getForeground() : getBackground().darker()); // emulating BasicButtonUI.paintText
		g.fillPolygon(arrow);
		if (isFocusPainted() && hasFocus()) {
			// emulating BasicButtonUI.paint
			g.setColor(UIManager.getColor("Button.focus"));
			g.setStroke(new BasicStroke(1f));
			g.drawRect(left - 1, top / 2, mainSize * 2 + 1, h - top);
		}
		g.dispose();
	}

	public enum Direction {
		UP,
		DOWN,
	}
}
