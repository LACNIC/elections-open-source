package net.lacnic.elections.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;

public enum ReminderFrequency {
	MONDAY_WEDNESDAY_FRIDAY,
	TUESDAY_THURSDAY,
	MONDAY_TO_FRIDAY,
	EVERY_DAY,
	DISABLED;

	public static ReminderFrequency defaultValue() {
		return MONDAY_TO_FRIDAY;
	}

	public boolean isEnabledOn(LocalDate date) {
		if (date == null) {
			return false;
		}
		return includesDay(date.getDayOfWeek());
	}

	public boolean includesDay(DayOfWeek dayOfWeek) {
		if (dayOfWeek == null) {
			return false;
		}
		switch (this) {
		case MONDAY_WEDNESDAY_FRIDAY:
			return dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.WEDNESDAY || dayOfWeek == DayOfWeek.FRIDAY;
		case TUESDAY_THURSDAY:
			return dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.THURSDAY;
		case MONDAY_TO_FRIDAY:
			return dayOfWeek.getValue() >= DayOfWeek.MONDAY.getValue() && dayOfWeek.getValue() <= DayOfWeek.FRIDAY.getValue();
		case EVERY_DAY:
			return true;
		case DISABLED:
		default:
			return false;
		}
	}
}
