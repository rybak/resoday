package dev.andrybak.resoday.gui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

class AddHabitDialog {
	private static final String NEW_HABIT_NAME_BASE = "New habit";

	private AddHabitDialog() {
	}

	static void show(Component parentComponent, Predicate<String> usedChecker, Consumer<String> newHabitNameConsumer) {
		JDialog d = create(parentComponent, usedChecker, newHabitNameConsumer);
		d.setVisible(true);
	}

	private static JDialog create(Component parentComponent, Predicate<String> usedChecker,
		Consumer<String> newHabitNameConsumer)
	{
		JDialog d = new JDialog(SwingUtilities.getWindowAncestor(parentComponent), "Add habit",
			Dialog.ModalityType.APPLICATION_MODAL);

		JPanel content = new JPanel();

		String newNamePlaceholder = chooseNewHabitName(usedChecker);
		JTextField nameInput = new JTextField(newNamePlaceholder, 15);
		nameInput.setToolTipText("Enter the name of the habit");
		content.add(nameInput);

		JButton addButton = new JButton("+");
		ActionListener returnName = ignored -> {
			String newName = nameInput.getText().trim();
			if (!isGoodName(usedChecker, newName)) {
				return;
			}
			newHabitNameConsumer.accept(newName);
			d.dispose();
		};
		addButton.addActionListener(returnName);
		content.add(addButton);

		JPopupMenu nameUsedPopup = createNameUsedPopup();

		nameInput.addActionListener(returnName);
		nameInput.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkName();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkName();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				checkName();
			}

			private void checkName() {
				checkCurrentCandidate(usedChecker, nameInput, addButton, nameUsedPopup);
			}
		});

		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Dialogs.setUpEscapeKeyClosing(d, content);
		Dialogs.setUpEscapeKeyClosing(d, nameInput);
		d.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				d.dispose();
			}
		});
		d.setContentPane(content);
		d.pack();
		d.setLocationRelativeTo(parentComponent);

		return d;
	}

	private static void checkCurrentCandidate(Predicate<String> usedChecker, JTextField nameInput, JButton addButton,
		JPopupMenu nameUsedPopup)
	{
		String candidateName = nameInput.getText().trim();
		if (usedChecker.test(candidateName)) {
			Dimension targetSize = nameInput.getSize();
			nameUsedPopup.show(nameInput, (int)(targetSize.getWidth() / 2), (int)targetSize.getHeight());
		} else {
			nameUsedPopup.setVisible(false);
		}
		addButton.setEnabled(isGoodName(usedChecker, candidateName));
	}

	private static boolean isGoodName(Predicate<String> usedChecker, String candidateName) {
		boolean nameIsEmpty = candidateName.isEmpty();
		boolean nameIsUsed = usedChecker.test(candidateName);
		return !nameIsEmpty && !nameIsUsed;
	}

	private static String chooseNewHabitName(Predicate<String> usedChecker) {
		String name = NEW_HABIT_NAME_BASE;
		int i = 1;
		while (usedChecker.test(name)) {
			name = NEW_HABIT_NAME_BASE + " " + i;
			i++;
		}
		return name;
	}

	private static JPopupMenu createNameUsedPopup() {
		JPopupMenu nameUsedPopup = new JPopupMenu();
		JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		JLabel nameUsedLabel = new JLabel("This name is already used");
		wrapper.add(nameUsedLabel);
		nameUsedPopup.add(wrapper);
		nameUsedPopup.setFocusable(false); // so that popup can't steal focus from `nameInput`
		return nameUsedPopup;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Add habit dialog demo");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JPanel content = new JPanel();
		content.setPreferredSize(new Dimension(400, 250));
		frame.setContentPane(content);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((int)(screenSize.getWidth() / 2), (int)(screenSize.getHeight() / 2));
		frame.setVisible(true);

		Set<String> names = Set.of("My habit", "New habit");
		JDialog addHabitDialog = create(content, names::contains, name -> {
			System.out.println("Got " + name);
			System.exit(0);
		});
		addHabitDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				System.out.println("Closing demo...");
				System.exit(0);
			}
		});
		addHabitDialog.setVisible(true);
	}
}
