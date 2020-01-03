package dev.andrybak.fuladve;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Andrei Rybak
 */
public class EveryDayCalendar {
	private static final Year currentYear = Year.now();

	private static final DateTimeFormatter CALENDAR_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM");
	private static final DateTimeFormatter DAY_BUTTON_FORMATTER = DateTimeFormatter.ofPattern("dd");
	private static final String POSITIVE_AUDIO = "positive.wav";
	private static final String NEGATIVE_AUDIO = "negative.wav";

	private final AdventHistory history;
	private final Path statePath;

	private final JFrame window = new JFrame("Every Day Calendar " + currentYear);
	private final JPanel content;

	EveryDayCalendar(String statePathStr) {
		statePath = Paths.get(statePathStr);
		history = readState(statePath);
		System.out.println("Read " + history.size() + " dates.");
		content = new JPanel(new GridBagLayout());
		fillInUI();
	}

	private void fillInUI() {
		GridBagConstraints gbc = new GridBagConstraints();

		for (Month m : Month.values()) {
			gbc.gridx = m.getValue() - 1;
			gbc.gridy = 0;
			content.add(new JLabel(MONTH_LABEL_FORMATTER.format(m)), gbc);
		}

		LocalDate currentYearStart = LocalDate.ofYearDay(currentYear.getValue(), 1);
		LocalDate nextYearStart = LocalDate.ofYearDay(currentYear.plusYears(1).getValue(), 1);
		for (LocalDate i = currentYearStart; i.isBefore(nextYearStart); i = i.plusDays(1)) {
			final LocalDate d = i; // for final inside lambdas
			gbc.gridx = d.getMonthValue() - 1;
			gbc.gridy = d.getDayOfMonth();
			JToggleButton b = new JToggleButton(DAY_BUTTON_FORMATTER.format(d));
			b.setSelected(history.isTurnedOn(d));
			b.addActionListener(ignored -> {
				if (b.isSelected()) {
					System.out.println("Turned on " + d);
					history.turnOn(d);
					playSound(POSITIVE_AUDIO);
				} else {
					System.out.println("Turned off " + d);
					history.turnOff(d);
					playSound(NEGATIVE_AUDIO);
				}
				System.out.println(history.serialize().toString());
			});
			content.add(b, gbc);
		}
	}

	private static AdventHistory readState(Path statePath) {
		if (!Files.exists(statePath)) {
			System.out.println("No saved state.");
			return new AdventHistory();
		}
		try {
			List<String> lines = Files.readAllLines(statePath);
			System.out.println("\tRead " + lines.size() + " lines...");
			if (lines.isEmpty())
				return new AdventHistory();
			List<LocalDate> dates = new ArrayList<>();
			for (String line : lines) {
				try {
					TemporalAccessor t = CALENDAR_DAY_FORMATTER.parse(line);
					LocalDate candidate = LocalDate.of(
						t.get(ChronoField.YEAR),
						t.get(ChronoField.MONTH_OF_YEAR),
						t.get(ChronoField.DAY_OF_MONTH)
					);
					dates.add(candidate);
				} catch (DateTimeException | ArithmeticException e) {
					System.err.println("Could not read value: " + sanitize(line));
				}
			}
			return new AdventHistory(dates);
		} catch (IOException e) {
			System.err.println("Could not read " + statePath);
			return new AdventHistory();
		}
	}

	private void saveState(Path statePath) {
		try {
			Files.write(statePath,
				history.serialize().stream()
					.map(CALENDAR_DAY_FORMATTER::format)
					.collect(toList())
			);
			System.out.println("Saved state.");
			System.out.println(history.size() + " dates.");
		} catch (IOException e) {
			System.err.println("Could not save current state in '" + statePath + "'.");
		}
	}

	private void go() {
		window.setMinimumSize(new Dimension(640, 480));
		window.setSize(800, 600);
		window.setContentPane(content);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		window.setVisible(true);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveState(statePath);
			}
		});
	}

	private static String sanitize(String s) {
		if (s.length() < 20)
			return s;
		return s.substring(0, 20);
	}

	private static void playSound(String resourceName) {
		System.out.println("Playing sound: " + resourceName);
		try (
			InputStream resource = EveryDayCalendar.class.getResourceAsStream(resourceName);
			BufferedInputStream buffered = new BufferedInputStream(resource);
			AudioInputStream a = AudioSystem.getAudioInputStream(buffered);
		) {
			Clip c = AudioSystem.getClip(null);
			c.open(a);
			c.start();
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			System.err.println("Could not play sound.");
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		if (args.length < 1) {
			System.err.println("Which file to open?");
			System.exit(1);
			return;
		}
		new EveryDayCalendar(args[0]).go();
	}

}
