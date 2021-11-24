package dev.andrybak.resoday.gui;

import dev.andrybak.resoday.SortOrder;
import dev.andrybak.resoday.YearHistory;
import dev.andrybak.resoday.gui.edithabits.EditHabitsDialog;

import java.awt.Window;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Histories {
	private final List<YearHistory> histories = new ArrayList<>();
	private final Map<String, HistoryPanel> panels = new HashMap<>();

	public void add(YearHistory yearHistory, HistoryPanel panel) {
		histories.add(yearHistory);
		if (panel != null) {
			if (!yearHistory.getId().equals(panel.getHistoryId())) {
				throw new IllegalArgumentException("Adding panel '" + panel.getHistoryId() +
					"' for history ID='" + yearHistory.getId());
			}
			panels.put(yearHistory.getId(), panel);
		}
	}

	public Stream<YearHistory> histories() {
		return histories.stream();
	}

	public Stream<YearHistory> visibleHistories() {
		return histories.stream()
			.filter(h -> h.getVisibility() == YearHistory.Visibility.VISIBLE);
	}

	public List<HistoryPanel> getOrderedPanels() {
		return visibleHistories()
			.map(YearHistory::getId)
			.map(panels::get)
			.collect(Collectors.toList());
	}

	/**
	 * @return {@code true}, if something was edited, {@code false} otherwise
	 */
	public boolean edit(Window parent, Path dir) {
		List<EditHabitsDialog.Row> inputRows = new ArrayList<>();
		for (int i = 0; i < histories.size(); i++) {
			YearHistory history = histories.get(i);
			String id = history.getId();
			String name = history.getName();
			EditHabitsDialog.Row row = new EditHabitsDialog.Row(id, name, switch (history.getVisibility()) {
				case VISIBLE -> EditHabitsDialog.Row.Status.VISIBLE;
				case HIDDEN -> EditHabitsDialog.Row.Status.HIDDEN;
			}, i);
			inputRows.add(row);
		}
		List<EditHabitsDialog.Row> outputRows = EditHabitsDialog.show(() -> parent, inputRows);
		if (inputRows.equals(outputRows)) {
			return false;
		}

		Map<String, YearHistory> map = histories.stream().collect(Collectors.toMap(
			YearHistory::getId,
			Function.identity()
		));
		int origSize = histories.size();
		histories.clear();
		for (EditHabitsDialog.Row outputRow : outputRows) {
			YearHistory history = map.get(outputRow.getId());
			history.setVisibility(switch (outputRow.getStatus()) {
				case VISIBLE -> YearHistory.Visibility.VISIBLE;
				case HIDDEN -> YearHistory.Visibility.HIDDEN;
			});
			if (history.getVisibility() == YearHistory.Visibility.VISIBLE) {
				panels.put(history.getId(), new HistoryPanel(history));
			}
			histories.add(history);
		}
		if (histories.size() != origSize) {
			throw new UnsupportedOperationException("Deleting habits via " + EditHabitsDialog.class +
				" is not supported yet");
		}
		List<String> inputOrder = inputRows.stream().map(EditHabitsDialog.Row::getId).collect(Collectors.toList());
		List<String> outputOrder = outputRows.stream().map(EditHabitsDialog.Row::getId).collect(Collectors.toList());
		if (!inputOrder.equals(outputOrder)) {
			SortOrder.save(dir, outputOrder);
		}
		return true;
	}

	public void forEachHistory(Consumer<YearHistory> action) {
		histories.forEach(action);
	}

	public void forEachPanel(Consumer<HistoryPanel> action) {
		panels.values().forEach(action);
	}

	public void hide(String historyId) {
		panels.remove(historyId);
	}
}
