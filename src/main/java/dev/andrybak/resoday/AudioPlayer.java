package dev.andrybak.resoday;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

class AudioPlayer implements YearHistoryListener {
	private static final String POSITIVE_AUDIO = "positive.wav";
	private static final String NEGATIVE_AUDIO = "negative.wav";

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

	@Override
	public void onTurnOn(LocalDate d) {
		playSound(POSITIVE_AUDIO);
	}

	@Override
	public void onTurnOff(LocalDate d) {
		playSound(NEGATIVE_AUDIO);
	}

}
