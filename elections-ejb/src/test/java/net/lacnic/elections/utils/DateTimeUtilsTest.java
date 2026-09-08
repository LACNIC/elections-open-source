package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;

class DateTimeUtilsTest {

	@Test
	void getTableServicesDateTimeStringShouldFormatDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2026, Calendar.APRIL, 13, 15, 45, 3);
		calendar.set(Calendar.MILLISECOND, 0);
		Date date = calendar.getTime();

		assertEquals("13/04/2026 15:45", DateTimeUtils.getTableServicesDateTimeString(date));
		assertEquals("13/04/2026 15:45", DateTimeUtils.getElectionDateTimeString(date));
	}

	@Test
	void getDateFormatMethodsShouldReturnNullWhenDateIsNull() {
		assertNull(DateTimeUtils.getTableServicesDateTimeString(null));
		assertNull(DateTimeUtils.getElectionDateTimeString(null));
	}
}
