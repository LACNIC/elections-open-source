package net.lacnic.elections.domain.services.dbtables;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.RecipientType;

 class ElectionEmailTemplateTableReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorCopiesFieldsAndHandlesNulls() {
		Election election = new Election();
		election.setElectionId(66L);

		ElectionEmailTemplate template = new ElectionEmailTemplate();
		template.setElectionEmailTemplateId(3L);
		template.setTemplateType("reminder");
		template.setSubjectSP("ASP");
		template.setSubjectPT("APT");
		template.setSubjectEN("AEN");
		template.setBodySP("bodyS");
		template.setBodyPT("bodyP");
		template.setBodyEN("bodyE");
		template.setRecipientType(RecipientType.ORGANIZATION);
		template.setElection(election);

		ElectionEmailTemplateTableReport report = new ElectionEmailTemplateTableReport(template);
		assertEquals(3L, report.getElectionEmailTemplateId());
		assertEquals("reminder", report.getTemplateType());
		assertEquals("ASP", report.getSubjectSP());
		assertEquals("APT", report.getSubjectPT());
		assertEquals("AEN", report.getSubjectEN());
		assertEquals("bodyS", report.getBodySP());
		assertEquals("bodyP", report.getBodyPT());
		assertEquals("bodyE", report.getBodyEN());
		assertEquals(RecipientType.ORGANIZATION.getDescription(), report.getRecipientType());
		assertEquals(66L, report.getElectionId().longValue());

		ElectionEmailTemplate templateWithoutElection = new ElectionEmailTemplate();
		templateWithoutElection.setElectionEmailTemplateId(4L);
		templateWithoutElection.setTemplateType("base");

		ElectionEmailTemplateTableReport reportWithoutElection = new ElectionEmailTemplateTableReport(templateWithoutElection);
		assertEquals(4L, reportWithoutElection.getElectionEmailTemplateId());
		assertEquals("", reportWithoutElection.getRecipientType());
		assertEquals(0L, reportWithoutElection.getElectionId().longValue());
	}
}
