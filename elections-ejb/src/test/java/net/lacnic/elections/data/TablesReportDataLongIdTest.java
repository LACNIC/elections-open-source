package net.lacnic.elections.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TablesReportDataLongIdTest {

	@Test
	void shouldStoreAndUpdateLongIdentifierData() {
		TablesReportDataLongId data = new TablesReportDataLongId(10L, "Desc 10");

		assertEquals(10L, data.getId());
		assertEquals("Desc 10", data.getDescription());

		data.setId(11L);
		data.setDescription("Desc 11");
		assertEquals(11L, data.getId());
		assertEquals("Desc 11", data.getDescription());
	}
}
