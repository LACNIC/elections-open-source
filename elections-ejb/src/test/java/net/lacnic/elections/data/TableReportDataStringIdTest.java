package net.lacnic.elections.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TableReportDataStringIdTest {

	@Test
	void shouldStoreAndUpdateStringIdentifierData() {
		TableReportDataStringId data = new TableReportDataStringId("A", "Desc A");

		assertEquals("A", data.getId());
		assertEquals("Desc A", data.getDescription());

		data.setId("B");
		data.setDescription("Desc B");
		assertEquals("B", data.getId());
		assertEquals("Desc B", data.getDescription());
	}
}
