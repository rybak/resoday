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
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Main {
	private static final Year currentYear = Year.now();

	private static final String APP_NAME = "Every Day Calendar";
	private static final DateTimeFormatter CALENDAR_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final String POSITIVE_AUDIO = "positive.wav";
	private static final String NEGATIVE_AUDIO = "negative.wav";

	private final YearHistory history;
	private Year shownYear;
	private JPanel shownYearPanel;
	private final Path statePath;

	private final JFrame window = new JFrame(APP_NAME);
	private final JPanel content;

	Main(String statePathStr) {
		statePath = Paths.get(statePathStr);
		history = readState(statePath);
		System.out.println("Read " + history.size() + " dates.");
		content = new JPanel(new BorderLayout());

		JButton pastButton = new JButton("<");
		pastButton.addActionListener(ignored -> {
			shownYear = shownYear.minusYears(1);
			recreateShownYearPanel();
		});
		setArrowButtonWidth(pastButton);
		content.add(pastButton, BorderLayout.WEST);

		JButton futureButton = new JButton(">");
		futureButton.addActionListener(ignored -> {
			shownYear = shownYear.plusYears(1);
			recreateShownYearPanel();
		});
		setArrowButtonWidth(futureButton);
		content.add(futureButton, BorderLayout.EAST);

		shownYear = history.years()
			.boxed()
			.min(Comparator.comparingInt(y -> Math.abs(currentYear.getValue() - y)))
			.map(Year::of)
			.orElse(currentYear);
		createShownYearPanel();
	}

	private void createShownYearPanel() {
		shownYearPanel = new YearPanel(history, shownYear,
			d -> playSound(POSITIVE_AUDIO),
			d -> playSound(NEGATIVE_AUDIO)
		);
		content.add(shownYearPanel, BorderLayout.CENTER);
	}

	private void recreateShownYearPanel() {
		content.remove(shownYearPanel);
		createShownYearPanel();
		content.revalidate();
		content.repaint();
	}

	private void setArrowButtonWidth(JButton b) {
		Dimension size = new Dimension(
			(int) new JLabel("XXXXXXXX").getPreferredSize().getWidth(),
			Short.MAX_VALUE
		);
		b.setPreferredSize(size);
		b.setMinimumSize(size);
	}

	private YearHistory readState(Path statePath) {
		window.setTitle(statePath.getFileName() + " - " + APP_NAME);
		if (!Files.exists(statePath)) {
			System.out.println("No saved state.");
			return new YearHistory();
		}
		try {
			List<String> lines = Files.readAllLines(statePath);
			System.out.println("\tRead " + lines.size() + " lines...");
			if (lines.isEmpty())
				return new YearHistory();
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
			return new YearHistory(dates);
		} catch (IOException e) {
			System.err.println("Could not read " + statePath);
			return new YearHistory();
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
		window.setContentPane(content);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.pack();
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
			InputStream resource = Main.class.getResourceAsStream(resourceName);
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
		new Main(args[0]).go();
	}
}
