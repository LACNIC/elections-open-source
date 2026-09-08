package net.lacnic.elections.domain.services.detail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

 class OrganizationDebtorImportResultTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testCountersAndListsCanBeSetAndRead() {
		OrganizationDebtorImportResult result = new OrganizationDebtorImportResult();
		List<String> orgIds = new ArrayList<>(Arrays.asList("org1", "org2"));
		List<String> missing = new ArrayList<>(Arrays.asList("org3"));

		result.setProcessedRows(5);
		result.setUpdatedRows(2);
		result.setOrgIds(orgIds);
		result.setMissingOrgIds(missing);

		assertEquals(5, result.getProcessedRows());
		assertEquals(2, result.getUpdatedRows());
		assertSame(orgIds, result.getOrgIds());
		assertSame(missing, result.getMissingOrgIds());
	}
}
