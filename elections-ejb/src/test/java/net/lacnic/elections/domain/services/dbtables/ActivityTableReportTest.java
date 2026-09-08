package net.lacnic.elections.domain.services.dbtables;

import java.util.Date;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.utils.DateTimeUtils;

 class ActivityTableReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorCopiesActivityFieldsAndFormatsTimestamp() {
		Activity activity = new Activity("alice", 10L, "127.0.0.1", ActivityType.ADD_CANDIDATE, "Created");
		activity.setActivityId(77L);
		Date timestamp = new Date(1700000000000L);
		activity.setTimestamp(timestamp);

		ActivityTableReport report = new ActivityTableReport(activity);

		assertEquals(77L, report.getActivityId().longValue());
		assertEquals("alice", report.getUserName());
		assertEquals(10L, report.getElectionId().longValue());
		assertEquals("127.0.0.1", report.getIp());
		assertEquals("Created", report.getDescription());
		assertEquals(ActivityType.ADD_CANDIDATE.toString(), report.getActivityType());
		assertEquals(DateTimeUtils.getTableServicesDateTimeString(timestamp), report.getTimestamp());
	}
}
