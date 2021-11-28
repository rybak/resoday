package dev.andrybak.resoday.gui.edithabits;

import javax.swing.JOptionPane;
import java.awt.Window;

public final class DeleteHabitDialog {
	private static final String QUESTION_TEMPLATE = "Do you really want to delete '%s'?\n" +
		"This operation is irreversible.";
	private static final String TITLE_TEMPLATE = "Delete '%s'?";

	private DeleteHabitDialog() {
		throw new UnsupportedOperationException();
	}

	public static Response show(Window parent, String name) {
		int response = JOptionPane.showConfirmDialog(parent, String.format(QUESTION_TEMPLATE, name),
			String.format(TITLE_TEMPLATE, name), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		return response == JOptionPane.YES_OPTION ? Response.YES : Response.NO;
	}

	public enum Response {
		YES,
		NO
	}
}
