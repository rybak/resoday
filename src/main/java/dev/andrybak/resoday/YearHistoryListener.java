package dev.andrybak.resoday;

import java.time.LocalDate;

public interface YearHistoryListener {
	void onTurnOn(LocalDate d);

	void onTurnOff(LocalDate d);
}
