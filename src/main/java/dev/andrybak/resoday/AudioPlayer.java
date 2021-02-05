package dev.andrybak.resoday;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Timer;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class AudioPlayer implements YearHistoryListener {
	private static final String POSITIVE_AUDIO = "positive.wav";
	private static final String NEGATIVE_AUDIO = "negative.wav";
	private static final int MAX_SOUNDS = 10;
	/**
	 * Assume that all audio clips are shorter than 10 seconds.
	 */
	private static final Duration AUDIO_TIMEOUT = Duration.ofSeconds(10);

	private final Timer closeTimer;
	private List<Clip> toClose = new ArrayList<>();

	AudioPlayer() {
		closeTimer = new Timer((int)AUDIO_TIMEOUT.toMillis(), ignored -> {
			for (Clip c : toClose)
				c.close(); // avoid leaking OS audio resources
			toClose.clear();
		});
		closeTimer.setRepeats(false);
	}

	private void playSound(String resourceName) {
		if (toClose.size() >= MAX_SOUNDS)
			return;
		System.out.println("Playing sound: " + resourceName);
		Clip c = null;
		try (
			InputStream resource = Main.class.getResourceAsStream(resourceName);
			BufferedInputStream buffered = new BufferedInputStream(resource);
			AudioInputStream a = AudioSystem.getAudioInputStream(buffered);
		) {
			c = AudioSystem.getClip(null);
			c.open(a);
			c.start();
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			System.err.println("Could not play sound.");
			e.printStackTrace();
		} finally {
			if (c != null)
				toClose.add(c);
			closeTimer.start();
		}
	}

	@Override
	public void onTurnOn(LocalDate d) {
		playSound(POSITIVE_AUDIO);
	}

	@Override
	public void onTurnOff(LocalDate d) {
		playSound(NEGATIVE_AUDIO);
	}

}
