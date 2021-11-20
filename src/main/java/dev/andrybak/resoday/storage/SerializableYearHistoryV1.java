package dev.andrybak.resoday.storage;

import dev.andrybak.resoday.YearHistory;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public final class SerializableYearHistoryV1 implements Serializable {
	public static final int VERSION = 1;
	@SuppressWarnings("unused") // deserialized
	private List<LocalDate> dates;
	@SuppressWarnings("unused") // deserialized
	private String name;

	public SerializableYearHistory toCurrentVersion() {
		// Note about id==null:
		//   After JSON deserialization, SerializableYearHistory#convert will set the id.
		return new SerializableYearHistory(dates, name, null, YearHistory.Visibility.VISIBLE);
	}
}
