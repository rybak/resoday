package dev.andrybak.resoday;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Objects;

class AudioPlayer implements YearHistoryListener {
	private static final String POSITIVE_AUDIO = "positive.wav";
	private static final String NEGATIVE_AUDIO = "negative.wav";
	private static final int MAX_SOUNDS = 10;
	private int currentlyPlaying = 0;

	private void playSound(String resourceName) {
		if (currentlyPlaying >= MAX_SOUNDS)
			return;
		currentlyPlaying++;
		System.out.println("Playing sound: " + resourceName);
		Clip c = null;
		try (
			InputStream resource = MainGui.class.getResourceAsStream(resourceName);
			BufferedInputStream buffered = new BufferedInputStream(Objects.requireNonNull(resource,
				() -> "Could not find resource '" + resourceName + "'"
			));
			AudioInputStream a = AudioSystem.getAudioInputStream(buffered)
		) {
			c = (Clip)AudioSystem.getLine(new DataLine.Info(Clip.class, a.getFormat()));
			c.open(a);
			final Clip finalClip = c;
			c.addLineListener(lineEvent -> {
				if (lineEvent.getType() == LineEvent.Type.STOP) {
					finalClip.close();
					currentlyPlaying--;
				}
			});
			c.start();
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			if (c != null)
				c.close(); // it's autocloseable, but we want sound to _play_ for its duration before closing
			System.err.println("Could not play sound.");
			e.printStackTrace();
			currentlyPlaying--;
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
