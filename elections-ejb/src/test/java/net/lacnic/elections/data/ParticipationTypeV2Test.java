package net.lacnic.elections.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ParticipationTypeV2Test {

	@Test
	void shouldExposeExpectedEnumValues() {
		assertEquals(3, ParticipationTypeV2.values().length);
		assertEquals(ParticipationTypeV2.VOTE, ParticipationTypeV2.valueOf("VOTE"));
		assertEquals(ParticipationTypeV2.NOMINATION, ParticipationTypeV2.valueOf("NOMINATION"));
		assertEquals(ParticipationTypeV2.SUPPORT, ParticipationTypeV2.valueOf("SUPPORT"));
	}
}
