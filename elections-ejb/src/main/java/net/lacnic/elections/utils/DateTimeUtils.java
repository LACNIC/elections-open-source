package net.lacnic.elections.utils;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DateTimeUtils {

	private static final String TABLE_SERVICES_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
	public static final String ELECTION_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
	public static final String ELECTION_DATE_FORMAT = "dd/MM/yyyy";
	public static final String ELECTION_TIME_FORMAT = "HH:mm";


	public static String getTableServicesDateTimeString(Date dateTime) {
		if(dateTime != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(TABLE_SERVICES_DATE_TIME_FORMAT);
			return dateFormat.format(dateTime);
		}
		return null;
	}

	public static String getElectionDateTimeString(Date dateTime) {
		if(dateTime != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(ELECTION_DATE_TIME_FORMAT);
			return dateFormat.format(dateTime);
		}
		return null;
	}

}
