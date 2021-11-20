package dev.andrybak.resoday.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;
import dev.andrybak.resoday.YearHistory;

import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SerializableYearHistory implements Serializable {
	public static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(VersionedYearHistory.class, YearHistoryJsonAdapter.INSTANCE)
		.registerTypeAdapter(LocalDate.class, LocalDateJsonAdapter.INSTANCE)
		.create();

	private static final int CURRENT_FORMAT_VERSION = 2;
	private static final DateTimeFormatter CALENDAR_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final List<LocalDate> dates;
	private final YearHistory.Visibility visibility;
	private String name;
	private String id;

	public SerializableYearHistory(Collection<LocalDate> dates, String name, String id,
		YearHistory.Visibility visibility)
	{
		this.dates = new ArrayList<>(Objects.requireNonNull(dates));
		this.name = Objects.requireNonNull(name);
		this.id = id;
		this.visibility = visibility;
	}

	public static SerializableYearHistory fromJson(String s, String name, String fallbackId) throws JsonParseException
	{
		VersionedYearHistory versionedHistory = GSON.fromJson(s, VersionedYearHistory.class);
		return convert(versionedHistory, name, fallbackId);
	}

	/**
	 * Read a habit file of any JSON-based version (currently: version 1 and version 2).
	 */
	public static SerializableYearHistory fromJson(Reader r, String name, Path path) throws JsonParseException {
		VersionedYearHistory versionedHistory = GSON.fromJson(r, VersionedYearHistory.class);
		return convert(versionedHistory, name, HabitFiles.v0v1PathToId(path));
	}

	private static SerializableYearHistory convert(VersionedYearHistory versionedHistory, String fallbackName,
		String fallbackId)
	{
		if (versionedHistory == null) {
			// empty version 0 file
			return new SerializableYearHistory(Collections.emptyList(), fallbackName,
				fallbackId, YearHistory.Visibility.VISIBLE);
		}
		SerializableYearHistory deserialized = versionedHistory.data;
		if (deserialized.name == null) {
			deserialized.name = fallbackName;
		}
		if (deserialized.id == null) {
			deserialized.id = fallbackId;
		}
		return deserialized;
	}

	public Collection<LocalDate> getDates() {
		return Collections.unmodifiableList(dates);
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public YearHistory.Visibility getVisibility() {
		return visibility == null ? YearHistory.Visibility.VISIBLE : visibility;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SerializableYearHistory that = (SerializableYearHistory)o;
		return dates.equals(that.dates) && Objects.equals(name, that.name) && Objects.equals(id, that.id) && visibility == that.visibility;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dates, name, id, visibility);
	}

	public String toJson() {
		return GSON.toJson(new VersionedYearHistory(this));
	}

	public void writeToJson(Appendable w) throws JsonIOException {
		GSON.toJson(new VersionedYearHistory(this), w);
	}

	@Override
	public String toString() {
		return "SerializableYearHistory{" +
			"name='" + name + '\'' +
			", id='" + id + '\'' +
			", dates=" + dates +
			", visibility=" + visibility +
			'}';
	}

	private enum YearHistoryJsonAdapter implements JsonDeserializer<VersionedYearHistory> {
		INSTANCE;

		static final String VERSION_KEY = "version";
		static final String DATA_KEY = "data";

		@Override
		public VersionedYearHistory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException
		{
			int version = json.getAsJsonObject().get(VERSION_KEY).getAsInt();
			JsonElement data = json.getAsJsonObject().get(DATA_KEY);
			switch (version) {
			case SerializableYearHistoryV1.VERSION -> {
				SerializableYearHistoryV1 v1History = context.deserialize(data, SerializableYearHistoryV1.class);
				return new VersionedYearHistory(v1History.toCurrentVersion());
			}
			case CURRENT_FORMAT_VERSION -> {
				SerializableYearHistory history = context.deserialize(data, SerializableYearHistory.class);
				return new VersionedYearHistory(history);
			}
			default -> throw new JsonParseException("Unknown SerializableYearHistory format version: " + version);
			}
		}
	}

	private enum LocalDateJsonAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
		INSTANCE;

		@Override
		public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException
		{
			try {
				return LocalDate.parse(json.getAsString(), CALENDAR_DAY_FORMATTER);
			} catch (DateTimeParseException e) {
				throw new JsonParseException(e);
			}
		}

		@Override
		public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(CALENDAR_DAY_FORMATTER.format(src));
		}
	}

	private static final class VersionedYearHistory {
		@SuppressWarnings("unused") // version needs to be serialized
		@SerializedName(YearHistoryJsonAdapter.VERSION_KEY)
		final int version;
		@SerializedName(YearHistoryJsonAdapter.DATA_KEY)
		final SerializableYearHistory data;

		VersionedYearHistory(SerializableYearHistory data) {
			this.data = data;
			this.version = CURRENT_FORMAT_VERSION;
		}
	}
}
