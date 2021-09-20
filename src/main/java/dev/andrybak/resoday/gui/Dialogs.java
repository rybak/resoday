package dev.andrybak.resoday.gui;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class Dialogs {
	private Dialogs() {
		throw new UnsupportedOperationException();
	}

	public static void setUpEscapeKeyClosing(JDialog d, JComponent c) {
		Object escapeCloseActionKey = new Object();
		c.getActionMap().put(escapeCloseActionKey, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				d.dispose();
			}
		});
		c.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escapeCloseActionKey);
	}
}
