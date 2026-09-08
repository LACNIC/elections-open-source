package net.lacnic.elections.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

class AsyncProcessingDataCoverageTest {

	@Test
	void asyncProcessingErrorShouldInitializeAndAllowMutations() {
		AsyncProcessingError empty = new AsyncProcessingError();
		assertNotNull(empty.getCreatedAt());

		AsyncProcessingError withValues = new AsyncProcessingError("error.key", 9, "fila invalida", "trace");
		assertEquals("error.key", withValues.getMessageKey());
		assertEquals(Integer.valueOf(9), withValues.getErrorRow());
		assertEquals("fila invalida", withValues.getErrorInfo());
		assertEquals("trace", withValues.getTechnicalDetail());
		assertNotNull(withValues.getCreatedAt());

		Date customDate = new Date(1234L);
		withValues.setMessageKey("other.key");
		withValues.setErrorRow(15);
		withValues.setErrorInfo("otro error");
		withValues.setTechnicalDetail("detalle");
		withValues.setCreatedAt(customDate);

		assertEquals("other.key", withValues.getMessageKey());
		assertEquals(Integer.valueOf(15), withValues.getErrorRow());
		assertEquals("otro error", withValues.getErrorInfo());
		assertEquals("detalle", withValues.getTechnicalDetail());
		assertSame(customDate, withValues.getCreatedAt());
	}

	@Test
	void asyncProcessingProgressConstructorsAndCopyShouldWork() {
		AsyncProcessingProgress defaultPhase = new AsyncProcessingProgress(1, 10, 2, 3, 4, 5, 6, 7, 100L);
		assertEquals(1, defaultPhase.getProcessedRows());
		assertEquals(10, defaultPhase.getTotalRows());
		assertEquals(2, defaultPhase.getCreatedRows());
		assertEquals(3, defaultPhase.getUpdatedRows());
		assertEquals(4, defaultPhase.getDeletedRows());
		assertEquals(5, defaultPhase.getTotalCreatedRows());
		assertEquals(6, defaultPhase.getTotalUpdatedRows());
		assertEquals(7, defaultPhase.getTotalDeletedRows());
		assertEquals(100L, defaultPhase.getUpdatedAt());
		assertEquals(AsyncProcessingProgress.PHASE_PLANNED, defaultPhase.getPhase());
		assertNull(defaultPhase.getOperationKey());

		AsyncProcessingProgress customPhase = new AsyncProcessingProgress(8, 20, 9, 10, 11, 12, 13, 14, 200L, AsyncProcessingProgress.PHASE_PREPARING);
		assertEquals(AsyncProcessingProgress.PHASE_PREPARING, customPhase.getPhase());
		assertNull(customPhase.getOperationKey());

		AsyncProcessingProgress full = new AsyncProcessingProgress(
				15, 30, 16, 17, 18, 19, 20, 21, 300L,
				AsyncProcessingProgress.PHASE_PLANNED,
				AsyncProcessingProgress.OPERATION_CENSUS_REGENERATE_VOTE_LINK);
		assertEquals(AsyncProcessingProgress.OPERATION_CENSUS_REGENERATE_VOTE_LINK, full.getOperationKey());

		AsyncProcessingProgress copy = new AsyncProcessingProgress(full);
		assertEquals(full.getProcessedRows(), copy.getProcessedRows());
		assertEquals(full.getTotalRows(), copy.getTotalRows());
		assertEquals(full.getCreatedRows(), copy.getCreatedRows());
		assertEquals(full.getUpdatedRows(), copy.getUpdatedRows());
		assertEquals(full.getDeletedRows(), copy.getDeletedRows());
		assertEquals(full.getTotalCreatedRows(), copy.getTotalCreatedRows());
		assertEquals(full.getTotalUpdatedRows(), copy.getTotalUpdatedRows());
		assertEquals(full.getTotalDeletedRows(), copy.getTotalDeletedRows());
		assertEquals(full.getUpdatedAt(), copy.getUpdatedAt());
		assertEquals(full.getPhase(), copy.getPhase());
		assertEquals(full.getOperationKey(), copy.getOperationKey());
	}

	@Test
	void asyncProcessingProgressShouldHandleNullCopyAndSetters() {
		AsyncProcessingProgress nullCopy = new AsyncProcessingProgress(null);
		assertEquals(0, nullCopy.getProcessedRows());
		assertEquals(0, nullCopy.getTotalRows());
		assertEquals(0, nullCopy.getCreatedRows());
		assertEquals(0, nullCopy.getUpdatedRows());
		assertEquals(0, nullCopy.getDeletedRows());
		assertEquals(0, nullCopy.getTotalCreatedRows());
		assertEquals(0, nullCopy.getTotalUpdatedRows());
		assertEquals(0, nullCopy.getTotalDeletedRows());
		assertEquals(0L, nullCopy.getUpdatedAt());
		assertNull(nullCopy.getPhase());
		assertNull(nullCopy.getOperationKey());

		nullCopy.setProcessedRows(2);
		nullCopy.setTotalRows(3);
		nullCopy.setCreatedRows(4);
		nullCopy.setUpdatedRows(5);
		nullCopy.setDeletedRows(6);
		nullCopy.setTotalCreatedRows(7);
		nullCopy.setTotalUpdatedRows(8);
		nullCopy.setTotalDeletedRows(9);
		nullCopy.setUpdatedAt(111L);
		nullCopy.setPhase(AsyncProcessingProgress.PHASE_PREPARING);
		nullCopy.setOperationKey(AsyncProcessingProgress.OPERATION_ORGANIZATIONS_REGENERATE_NOMINATION_LINK);

		assertEquals(2, nullCopy.getProcessedRows());
		assertEquals(3, nullCopy.getTotalRows());
		assertEquals(4, nullCopy.getCreatedRows());
		assertEquals(5, nullCopy.getUpdatedRows());
		assertEquals(6, nullCopy.getDeletedRows());
		assertEquals(7, nullCopy.getTotalCreatedRows());
		assertEquals(8, nullCopy.getTotalUpdatedRows());
		assertEquals(9, nullCopy.getTotalDeletedRows());
		assertEquals(111L, nullCopy.getUpdatedAt());
		assertEquals(AsyncProcessingProgress.PHASE_PREPARING, nullCopy.getPhase());
		assertEquals(AsyncProcessingProgress.OPERATION_ORGANIZATIONS_REGENERATE_NOMINATION_LINK, nullCopy.getOperationKey());
	}

	@Test
	void healthCheckShouldExposeAndMutateFields() {
		ElectionReport first = new ElectionReport("Eleccion 1", 11, 4, 15, 2);
		List<ElectionReport> initialElections = new ArrayList<>();
		initialElections.add(first);

		HealthCheck healthCheck = new HealthCheck(3, 7L, 12L, 30L, 5L, 25L, initialElections);
		assertEquals(3, healthCheck.getSendAttempts());
		assertEquals(7L, healthCheck.getFailedAccessIps());
		assertEquals(12L, healthCheck.getFailedAccessSum());
		assertEquals(30L, healthCheck.getMailsTotal());
		assertEquals(5L, healthCheck.getMailsPending());
		assertEquals(25L, healthCheck.getMailsSent());
		assertSame(initialElections, healthCheck.getElections());

		List<ElectionReport> updatedElections = new ArrayList<>();
		updatedElections.add(new ElectionReport("Eleccion 2", 1, 2, 3, 4));
		healthCheck.setSendAttempts(9);
		healthCheck.setFailedAccessIps(20L);
		healthCheck.setFailedAccessSum(33L);
		healthCheck.setMailsTotal(40L);
		healthCheck.setMailsPending(8L);
		healthCheck.setMailsSent(32L);
		healthCheck.setElections(updatedElections);

		assertEquals(9, healthCheck.getSendAttempts());
		assertEquals(20L, healthCheck.getFailedAccessIps());
		assertEquals(33L, healthCheck.getFailedAccessSum());
		assertEquals(40L, healthCheck.getMailsTotal());
		assertEquals(8L, healthCheck.getMailsPending());
		assertEquals(32L, healthCheck.getMailsSent());
		assertSame(updatedElections, healthCheck.getElections());
	}
}
