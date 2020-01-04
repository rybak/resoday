package dev.andrybak.fuladve;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.Comparator;

class HistoryPanel extends JPanel {
	private static final Year currentYear = Year.now();

	private static final String POSITIVE_AUDIO = "positive.wav";
	private static final String NEGATIVE_AUDIO = "negative.wav";

	private final YearHistory history;
	private final Path statePath;
	private Year shownYear;
	private JPanel shownYearPanel;

	HistoryPanel(String statePathStr) {
		super(new BorderLayout());
		statePath = Paths.get(statePathStr);
		history = YearHistory.read(statePath);
		System.out.println("Read " + history.size() + " dates.");

		JButton pastButton = new JButton("<");
		pastButton.addActionListener(ignored -> {
			shownYear = shownYear.minusYears(1);
			recreateShownYearPanel();
		});
		setArrowButtonWidth(pastButton);
		this.add(pastButton, BorderLayout.WEST);

		JButton futureButton = new JButton(">");
		futureButton.addActionListener(ignored -> {
			shownYear = shownYear.plusYears(1);
			recreateShownYearPanel();
		});
		setArrowButtonWidth(futureButton);
		this.add(futureButton, BorderLayout.EAST);

		shownYear = history.years()
			.boxed()
			.min(Comparator.comparingInt(y -> Math.abs(currentYear.getValue() - y)))
			.map(Year::of)
			.orElse(currentYear);
		createShownYearPanel();
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

	private void createShownYearPanel() {
		shownYearPanel = new YearPanel(history, shownYear,
			d -> playSound(POSITIVE_AUDIO),
			d -> playSound(NEGATIVE_AUDIO)
		);
		this.add(shownYearPanel, BorderLayout.CENTER);
	}

	private void recreateShownYearPanel() {
		this.remove(shownYearPanel);
		createShownYearPanel();
		this.revalidate();
		this.repaint();
	}

	private void setArrowButtonWidth(JButton b) {
		Dimension size = new Dimension(
			(int) new JLabel("XXXXXXXX").getPreferredSize().getWidth(),
			Short.MAX_VALUE
		);
		b.setPreferredSize(size);
		b.setMinimumSize(size);
	}

	void save() {
		history.saveTo(statePath);
	}

	Path getPath() {
		return statePath;
	}
}
