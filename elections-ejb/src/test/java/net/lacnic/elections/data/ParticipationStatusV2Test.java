package net.lacnic.elections.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ParticipationStatusV2Test {

	@Test
	void shouldExposeExpectedEnumValues() {
		assertEquals(3, ParticipationStatusV2.values().length);
		assertEquals(ParticipationStatusV2.AVAILABLE, ParticipationStatusV2.valueOf("AVAILABLE"));
		assertEquals(ParticipationStatusV2.USED, ParticipationStatusV2.valueOf("USED"));
		assertEquals(ParticipationStatusV2.BLOCKED, ParticipationStatusV2.valueOf("BLOCKED"));
	}
}
