package dev.andrybak.resoday.gui.edithabits;

import dev.andrybak.resoday.gui.Dialogs;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * Allows editing names and visibility status of habits.
 */
public class EditHabitsDialog extends JDialog {

	private final Map<String, Row> rows;
	private JPanel rowsPanel;

	private EditHabitsDialog(Owner owner, Map<String, Row> rows) {
		super(owner.getParent(), "Edit habits", ModalityType.APPLICATION_MODAL);
		this.rows = rows;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel content = new JPanel(new BorderLayout());
		createRowsPanel(owner);

		content.add(rowsPanel, BorderLayout.NORTH);
		setContentPane(content);

		Dialogs.setUpEscapeKeyClosing(this, content);
	}

	public static List<Row> show(Owner owner, List<Row> originalRows) {
		EditHabitsDialog d = create(owner, originalRows);
		d.setVisible(true);
		d.dispose();
		return d.getResultRows();
	}

	private static EditHabitsDialog create(Owner owner, List<Row> originalRows) {
		Map<String, Row> rows = new HashMap<>();
		for (int i = 0, n = originalRows.size(); i < n; i++) {
			Row originalRow = originalRows.get(i);
			if (rows.put(originalRow.getId(), originalRow.withIndex(i)) != null) {
				throw new IllegalArgumentException("Duplicate ID: " + originalRow.getId());
			}
		}

		EditHabitsDialog d = new EditHabitsDialog(owner, rows);
		d.pack();
		d.setLocationRelativeTo(owner.getParent());

		return d;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Edit habits dialog demo");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JPanel content = new JPanel();
		content.setPreferredSize(new Dimension(400, 250));
		frame.setContentPane(content);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((int)(screenSize.getWidth() / 2), (int)(screenSize.getHeight() / 2));
		frame.setVisible(true);

		List<Row> rows = new ArrayList<>();
		rows.add(new Row("000", "Hello", Row.Status.VISIBLE));
		rows.add(new Row("001", "World", Row.Status.HIDDEN));
		rows.add(new Row("002", "Foo", Row.Status.VISIBLE));
		System.out.println("Showing dialog with " + rows.size() + " rows");
		List<Row> resultRows = show(() -> frame, rows);
		System.out.println("Result rows: ");
		for (Row r : resultRows) {
			System.out.println("\t" + r);
		}
		frame.dispose();
	}

	private List<Row> getResultRows() {
		return rows.values().stream()
			.sorted(Comparator.comparingInt(Row::getIndex))
			.collect(toList());
	}

	private void createRowsPanel(Owner owner) {
		JPanel rowsPanel = new JPanel(new HabitListLayout());
		rowsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		List<Row> sortedCurrent = getResultRows();
		for (Row row : sortedCurrent) {
			{
				JLabel habitNameLabel = new JLabel(row.getName());
				rowsPanel.add(habitNameLabel, HabitListLayout.Column.NAME);
				if (row.getStatus() == Row.Status.VISIBLE) {
					habitNameLabel.setFont(habitNameLabel.getFont().deriveFont(Font.BOLD));
				} else {
					Font currentFont = habitNameLabel.getFont();
					int newStyle = currentFont.getStyle() & (~Font.BOLD);
					habitNameLabel.setFont(currentFont.deriveFont(newStyle));
				}
			}
			{
				JButton visibilityButton = new JButton(switch (row.getStatus()) {
					case VISIBLE -> "Hide";
					case HIDDEN -> "Show";
				});
				visibilityButton.addActionListener(ignored -> {
					switch (row.getStatus()) {
					case VISIBLE -> row.hide();
					case HIDDEN -> row.show();
					}
					recreateRowsPanel(owner);
				});
				rowsPanel.add(visibilityButton, HabitListLayout.Column.HIDE_SHOW_BUTTON);
			}
			{
				BasicArrowButton upButton = new BasicArrowButton(BasicArrowButton.NORTH);
				upButton.addActionListener(ignored -> {
					swap(rows, row.getIndex(), row.getIndex() - 1);
					recreateRowsPanel(owner);
				});
				rowsPanel.add(upButton, HabitListLayout.Column.MOVE_UP);
				if (row.getIndex() == 0) {
					upButton.setEnabled(false);
				}
			}
			{
				BasicArrowButton downButton = new BasicArrowButton(BasicArrowButton.SOUTH);
				downButton.addActionListener(ignored -> {
					swap(rows, row.getIndex(), row.getIndex() + 1);
					recreateRowsPanel(owner);
				});
				rowsPanel.add(downButton, HabitListLayout.Column.MOVE_DOWN);
				if (row.getIndex() == rows.size() - 1) {
					downButton.setEnabled(false);
				}
			}
		}
		this.rowsPanel = rowsPanel;
		getContentPane().add(rowsPanel, BorderLayout.CENTER);
	}

	private void swap(Map<String, Row> rows, int aIndex, int bIndex) {
		Row a = rows.values().stream()
			.filter(r -> r.getIndex() == aIndex)
			.findFirst().orElseThrow();
		Row b = rows.values().stream()
			.filter(r -> r.getIndex() == bIndex)
			.findFirst().orElseThrow();
		a.moveToIndex(bIndex);
		b.moveToIndex(aIndex);
	}

	private void recreateRowsPanel(Owner owner) {
		getContentPane().remove(rowsPanel);
		createRowsPanel(owner);
		revalidate();
		repaint();
	}

	public interface Owner {
		Window getParent();
	}

	public static final class Row {
		private final String id;
		private final String name;
		private Status status;
		private int index;

		/**
		 * Row in the edit dialog represents a single habit being edited.
		 *
		 * @param id     must be unique for every {@link Row} created
		 * @param name   name of the edited item
		 * @param status current visibility of the edited item
		 */
		Row(String id, String name, Status status) {
			this(id, name, status, Integer.MIN_VALUE);
		}

		public Row(String id, String name, Status status, int index) {
			this.id = Objects.requireNonNull(id);
			this.name = Objects.requireNonNull(name);
			this.status = Objects.requireNonNull(status);
			this.index = index;
		}

		public Row withIndex(int index) {
			return new Row(id, name, status, index);
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public Status getStatus() {
			return status;
		}

		public void moveToIndex(int newIndex) {
			index = newIndex;
		}

		public void hide() {
			status = Status.HIDDEN;
		}

		public void show() {
			status = Status.VISIBLE;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Row row = (Row)o;
			return name.equals(row.name) && status == row.status;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, status);
		}

		@Override
		public String toString() {
			return "Row{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", status=" + status +
				'}';
		}

		public int getIndex() {
			return index;
		}

		public enum Status {
			HIDDEN,
			VISIBLE,
			// TODO maybe "Deleted"?
		}
	}

	private static class HabitListLayout implements LayoutManager2 {
		private final Map<Column, List<Component>> columns = new HashMap<>();

		@Override
		public void addLayoutComponent(Component comp, Object constraints) {
			if (!(constraints instanceof Column)) {
				return;
			}
			Column column = ((Column)constraints);

			columns.computeIfAbsent(column, ignored -> new ArrayList<>()).add(comp);
		}

		@Override
		public void removeLayoutComponent(Component comp) {
			columns.values().forEach(list -> list.remove(comp));
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			System.out.println("preferred:");
			return getSizeByGetter(Component::getPreferredSize, parent);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			System.out.println("minimum:");
			return getSizeByGetter(Component::getMinimumSize, parent);
		}

		@Override
		public Dimension maximumLayoutSize(Container target) {
			return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		}

		@Override
		public void layoutContainer(Container target) {
			if (target.getComponentCount() == 0) {
				return;
			}
			if (columns.values().stream().anyMatch(List::isEmpty)) {
				throw new IllegalStateException("Column(s) " +
					columns.entrySet().stream()
						.filter(e -> e.getValue().isEmpty())
						.collect(toList())
					+ " must not be empty");
			}
			if (columns.values().stream().map(List::size).distinct().count() != 1) {
				throw new IllegalStateException("Different sized columns");
			}
			synchronized (target.getTreeLock()) {
				Insets insets = target.getInsets();
				List<Component> hideShowColumn = getHideShowColumn();
				List<Component> nameColumn = getNameColumn();
				List<Component> moveUpColumn = columns.get(Column.MOVE_UP);
				List<Component> moveDownColumn = columns.get(Column.MOVE_DOWN);
				int hideShowWidth = getHideShowWidth(Component::getPreferredSize);
				int rowHeight = hideShowColumn.get(0).getPreferredSize().height;
				@SuppressWarnings("SuspiciousNameCombination") // forcing movement buttons into squares
				final int moveWidth = rowHeight;
				int buttonsWidth = hideShowWidth + moveWidth * 2;

				final int labelLeftX = insets.left;
				final int buttonLeftX = target.getWidth() - insets.right - buttonsWidth;
				int y = insets.top;
				for (int i = 0, n = hideShowColumn.size(); i < n; i++) {
					Component lbl = nameColumn.get(i);
					lbl.setBounds(labelLeftX, y, buttonLeftX - labelLeftX, rowHeight);
					Component hideShowBtn = hideShowColumn.get(i);
					hideShowBtn.setBounds(buttonLeftX, y, hideShowWidth, rowHeight);
					moveUpColumn.get(i).setBounds(buttonLeftX + hideShowWidth, y, moveWidth, rowHeight);
					moveDownColumn.get(i).setBounds(buttonLeftX + hideShowWidth + moveWidth, y, moveWidth, rowHeight);
					y += rowHeight;
				}
			}
		}

		private List<Component> getNameColumn() {
			return columns.get(Column.NAME);
		}

		private List<Component> getHideShowColumn() {
			return columns.get(Column.HIDE_SHOW_BUTTON);
		}

		private Dimension getSizeByGetter(Function<Component, Dimension> dimGetter, Container target) {
			Insets insets = target.getInsets();
			int nameWidth = getColumnWidth(dimGetter, getNameColumn());
			int rowHeight = getHideShowColumn().stream()
				.findFirst()
				.map(dimGetter)
				.map(d -> d.height)
				.orElse(10);
			int height = getHideShowColumn().size() * rowHeight;
			int restWidth = columns.entrySet().stream()
				.filter(e -> e.getKey() != Column.NAME)
				.peek(e -> System.out.println(e.getKey() + " " + getColumnWidth(dimGetter, e.getValue())))
				.mapToInt(e -> switch (e.getKey()) {
					case MOVE_UP, MOVE_DOWN -> rowHeight; // use _height_ as width, because buttons are squares
					default -> getColumnWidth(dimGetter, e.getValue());
				})
				.sum();
			int width = nameWidth + restWidth;
			return new Dimension(
				(insets.left + insets.right) + width,
				(insets.top + insets.bottom) + height
			);
		}

		private int getHideShowWidth(Function<Component, Dimension> dimGetter) {
			return getColumnWidth(dimGetter, getHideShowColumn());
		}

		private int getColumnWidth(Function<Component, Dimension> dimGetter, List<Component> column) {
			return column.stream()
				.mapToInt(c -> dimGetter.apply(c).width)
				.max()
				.orElse(10);
		}

		@Override
		public float getLayoutAlignmentX(Container target) {
			return 0;
		}

		@Override
		public float getLayoutAlignmentY(Container target) {
			return 0;
		}

		@Override
		public void invalidateLayout(Container target) {
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		enum Column {
			NAME,
			HIDE_SHOW_BUTTON,
			MOVE_UP,
			MOVE_DOWN,
		}
	}
}
