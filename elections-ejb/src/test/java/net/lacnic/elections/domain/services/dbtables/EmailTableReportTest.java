package net.lacnic.elections.domain.services.dbtables;

import java.util.Date;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.EmailHistory;
import net.lacnic.elections.utils.DateTimeUtils;

 class EmailTableReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorFromEmail() {
		Election election = new Election();
		election.setElectionId(22L);

		Email email = new Email();
		email.setEmailId(5L);
		email.setRecipients("one@x.com");
		email.setSender("sender@x.com");
		email.setCc("cc@x.com");
		email.setBcc("bcc@x.com");
		email.setSubject("hello");
		email.setBody("body");
		email.setSent(true);
		Date createdDate = new Date(1700002400000L);
		email.setCreatedDate(createdDate);
		email.setTemplateType("election");
		email.setElection(election);

		EmailTableReport report = new EmailTableReport(email);

		assertEquals(5L, report.getEmailId().longValue());
		assertEquals("one@x.com", report.getRecipients());
		assertEquals("sender@x.com", report.getSender());
		assertEquals("cc@x.com", report.getCc());
		assertEquals("bcc@x.com", report.getBcc());
		assertEquals("hello", report.getSubject());
		assertEquals(Boolean.TRUE, report.getSent());
		assertEquals(22L, report.getElectionId().longValue());
		assertEquals("election", report.getTemplateType());
		assertEquals(DateTimeUtils.getTableServicesDateTimeString(createdDate), report.getCreatedDate());
	}

	@org.junit.jupiter.api.Test

	 void testConstructorFromEmailHistory() {
		EmailHistory history = new EmailHistory();
		history.setEmailHistoryId(6L);
		history.setRecipients("two@x.com");
		history.setSender("sender2@x.com");
		history.setCc("cc2@x.com");
		history.setBcc("bcc2@x.com");
		history.setSubject("hola");
		history.setBody("cuerpo");
		history.setSent(false);
		Date createdDate = new Date(1700002400000L);
		history.setCreatedDate(createdDate);
		history.setTemplateType("history");
		history.setElectionId(33L);

		EmailTableReport report = new EmailTableReport(history);

		assertEquals(6L, report.getEmailId().longValue());
		assertEquals("two@x.com", report.getRecipients());
		assertEquals(Boolean.FALSE, report.getSent());
		assertEquals("history", report.getTemplateType());
		assertEquals(33L, report.getElectionId().longValue());
		assertEquals(DateTimeUtils.getTableServicesDateTimeString(createdDate), report.getCreatedDate());
	}
}
