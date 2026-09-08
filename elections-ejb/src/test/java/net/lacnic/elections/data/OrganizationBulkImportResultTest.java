package net.lacnic.elections.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class OrganizationBulkImportResultTest {

	@Test
	void shouldStoreRowsErrorsAndErrorReport() {
		OrganizationBulkImportResult result = new OrganizationBulkImportResult();
		byte[] report = new byte[] { 1, 2, 3 };

		result.setProcessedRows(100);
		result.setCreatedRows(20);
		result.setUpdatedRows(40);
		result.setDeletedRows(5);
		result.setErrors(Arrays.asList("row 2", "row 9"));
		result.setErrorReport(report);

		assertEquals(100, result.getProcessedRows());
		assertEquals(20, result.getCreatedRows());
		assertEquals(40, result.getUpdatedRows());
		assertEquals(5, result.getDeletedRows());
		assertTrue(result.hasErrors());
		assertEquals(2, result.getErrors().size());
		assertArrayEquals(report, result.getErrorReport());

		result.setErrors(List.of());
		assertFalse(result.hasErrors());
	}
}
