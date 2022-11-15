package dev.andrybak.resoday.gui.stats;

import dev.andrybak.resoday.gui.Histories;
import dev.andrybak.resoday.gui.util.Dialogs;
import dev.andrybak.resoday.gui.util.ScrollPanes;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.time.LocalDate;
import java.util.NavigableSet;

public class StatisticsDialog {
	private static JDialog create(Window parent, Histories histories) {
		JDialog d = new JDialog(parent, "Statistics", Dialog.ModalityType.APPLICATION_MODAL);
		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		JPanel content = new JPanel(new BorderLayout());
		Dialogs.setUpEscapeKeyClosing(d, content);

		{
			JTable table = createStatsTable(histories);
			content.add(ScrollPanes.regular(table), BorderLayout.CENTER);
		}

		d.setContentPane(content);
		d.pack();
		d.setLocationRelativeTo(parent);

		return d;
	}

	private static JTable createStatsTable(Histories histories) {
		DefaultTableModel tableModel = new DefaultTableModel(
			new Object[]{
				"Name     ",
				"# of days",
				"Oldest   ",
				"Newest   ",
			},
			0
		);
		histories.forEachHistory(h -> {
			NavigableSet<LocalDate> ns = h.toNavigableSet();
			tableModel.addRow(new Object[]{
				h.getName(),
				ns.size(),
				ns.first(),
				ns.last()
			});
		});
		return new JTable(tableModel);
	}

	public static void show(Window parent, Histories histories) {
		JDialog d = create(parent, histories);
		d.setVisible(true);
	}
}
