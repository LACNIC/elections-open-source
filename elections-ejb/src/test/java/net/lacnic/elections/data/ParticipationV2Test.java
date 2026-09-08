package net.lacnic.elections.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;

class ParticipationV2Test {

	@Test
	void shouldStoreAllParticipationFields() {
		ParticipationV2 participation = new ParticipationV2();
		Date start = new Date(1700000000000L);
		Date end = new Date(1700000100000L);

		participation.setOrgId("ORG-1");
		participation.setEmail("user@example.com");
		participation.setElectionTitleSP("Titulo ES");
		participation.setElectionTitleEN("Title EN");
		participation.setElectionTitlePT("Titulo PT");
		participation.setName("Nombre");
		participation.setCountry("UY");
		participation.setElectionStartDate(start);
		participation.setElectionEndDate(end);
		participation.setCategory("BOARD");
		participation.setElectionLinkSP("https://sp");
		participation.setElectionLinkEN("https://en");
		participation.setElectionLinkPT("https://pt");
		participation.setLink("https://vote");
		participation.setParticipationType(ParticipationTypeV2.NOMINATION);
		participation.setStatus(ParticipationStatusV2.AVAILABLE);

		assertEquals("ORG-1", participation.getOrgId());
		assertEquals("user@example.com", participation.getEmail());
		assertEquals("Titulo ES", participation.getElectionTitleSP());
		assertEquals("Title EN", participation.getElectionTitleEN());
		assertEquals("Titulo PT", participation.getElectionTitlePT());
		assertEquals("Nombre", participation.getName());
		assertEquals("UY", participation.getCountry());
		assertEquals(start, participation.getElectionStartDate());
		assertEquals(end, participation.getElectionEndDate());
		assertEquals("BOARD", participation.getCategory());
		assertEquals("https://sp", participation.getElectionLinkSP());
		assertEquals("https://en", participation.getElectionLinkEN());
		assertEquals("https://pt", participation.getElectionLinkPT());
		assertEquals("https://vote", participation.getLink());
		assertEquals(ParticipationTypeV2.NOMINATION, participation.getParticipationType());
		assertEquals(ParticipationStatusV2.AVAILABLE, participation.getStatus());
	}
}
