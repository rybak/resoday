package dev.andrybak.resoday.gui;

import dev.andrybak.resoday.YearHistory;
import dev.andrybak.resoday.gui.edithabits.ReorderHabitsDialog;
import dev.andrybak.resoday.gui.settings.CalendarLayoutSettingProvider;
import dev.andrybak.resoday.gui.settings.DataDirSupplier;
import dev.andrybak.resoday.storage.SortOrder;

import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Histories {
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
	 * Show {@link ReorderHabitsDialog} and reorder habits in this {@code Histories} according to user inputs.
	 *
	 * @param editCallback called, if something was edited
	 */
	public void reorder(Window parent, DataDirSupplier dataDirSupplier,
		CalendarLayoutSettingProvider calendarLayoutSettingProvider,
		Runnable editCallback)
	{
		List<ReorderHabitsDialog.Row> inputRows = new ArrayList<>();
		for (int i = 0; i < histories.size(); i++) {
			YearHistory history = histories.get(i);
			String id = history.getId();
			String name = history.getName();
			ReorderHabitsDialog.Row row = new ReorderHabitsDialog.Row(id, name, switch (history.getVisibility()) {
				case VISIBLE -> ReorderHabitsDialog.Row.Status.VISIBLE;
				case HIDDEN -> ReorderHabitsDialog.Row.Status.HIDDEN;
			}, i);
			inputRows.add(row);
		}
		ReorderHabitsDialog.show(parent, inputRows, outputRows -> {
			if (inputRows.equals(outputRows)) {
				return;
			}

			Map<String, YearHistory> map = histories.stream().collect(Collectors.toMap(
				YearHistory::getId,
				Function.identity()
			));
			int origSize = histories.size();
			histories.clear();
			panels.values().forEach(HistoryPanel::close);
			panels.clear();
			for (ReorderHabitsDialog.Row outputRow : outputRows) {
				YearHistory history = map.get(outputRow.getId());
				history.setVisibility(switch (outputRow.getStatus()) {
					case VISIBLE -> YearHistory.Visibility.VISIBLE;
					case HIDDEN -> YearHistory.Visibility.HIDDEN;
				});
				if (history.getVisibility() == YearHistory.Visibility.VISIBLE) {
					panels.put(history.getId(), new HistoryPanel(history, calendarLayoutSettingProvider));
				}
				histories.add(history);
			}
			assert visibleHistories().count() == panels.size() : "visibleHistories should be in sync with panels";
			if (histories.size() != origSize) {
				throw new UnsupportedOperationException("Deleting habits via " + ReorderHabitsDialog.class +
					" is not supported yet");
			}
			List<String> inputOrder = inputRows.stream()
				.map(ReorderHabitsDialog.Row::getId)
				.toList();
			List<String> outputOrder = outputRows.stream()
				.map(ReorderHabitsDialog.Row::getId)
				.collect(Collectors.toList());
			if (!inputOrder.equals(outputOrder)) {
				SortOrder.save(dataDirSupplier, outputOrder);
			}
			editCallback.run();
		});
	}

	public void forEachHistory(Consumer<YearHistory> action) {
		histories.forEach(action);
	}

	public void forEachPanel(Consumer<HistoryPanel> action) {
		panels.values().forEach(action);
	}

	public void hide(String historyId) {
		HistoryPanel removed = panels.remove(historyId);
		removed.close();
	}

	public void rename(String id, String newHabitName) {
		Optional<YearHistory> maybeHistory = histories.stream()
			.filter(h -> h.getId().equals(id))
			.findAny();
		if (maybeHistory.isEmpty()) {
			System.err.println("Warning: could not rename history ID='" + id + "'");
			return;
		}
		maybeHistory.orElseThrow().setName(newHabitName);
	}

	public void delete(String historyId) {
		panels.remove(historyId);
		Optional<YearHistory> maybeHistory = histories.stream()
			.filter(h -> h.getId().equals(historyId))
			.findAny();
		if (maybeHistory.isEmpty()) {
			System.err.println("Warning: could not delete history ID='" + historyId + "'");
			return;
		}
		YearHistory yearHistory = maybeHistory.orElseThrow();
		histories.remove(yearHistory);
		yearHistory.delete();
	}
}
