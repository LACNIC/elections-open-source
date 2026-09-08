package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.lacnic.elections.data.AsyncProcessingError;
import net.lacnic.elections.data.AsyncProcessingProgress;
import net.lacnic.elections.data.AsyncProcessingType;
import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.domain.services.publicelection.PublicElectionsSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCoreSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionOfficialResultSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionPhotoSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionRollSnapshot;
import org.junit.jupiter.api.Test;

class ElectionsCachesTest {

	private void clearRateLimitCaches() {
		ElectionsCaches.clearAiTextImprovementRateLimitCache();
		ElectionsCaches.clearCampusProgressCheckRateLimitCache();
		ElectionsCaches.clearPublicFailedAccessRateLimitCache();
		ElectionsCaches.clearLoginCaptchaRateLimitCache();
		ElectionsCaches.replaceParametersCache(null);
	}

	@Test
	void incrementAiTextImprovementAttemptShouldNormalizeScopeKey() {
		clearRateLimitCaches();
		assertEquals(0, ElectionsCaches.incrementAiTextImprovementAttempt((String) null));
		assertEquals(0, ElectionsCaches.incrementAiTextImprovementAttempt("   "));

		int first = ElectionsCaches.incrementAiTextImprovementAttempt("candidate:42");
		int second = ElectionsCaches.incrementAiTextImprovementAttempt("CANDIDATE:42");

		assertEquals(1, first);
		assertEquals(2, second);
	}

	@Test
	void incrementAiTextImprovementAttemptForCandidateShouldIgnoreInvalidId() {
		assertEquals(0, ElectionsCaches.incrementAiTextImprovementAttempt(0));
		assertEquals(0, ElectionsCaches.incrementAiTextImprovementAttempt(-12));
	}

	@Test
	void incrementCampusProgressCheckAttemptShouldBePerKey() {
		clearRateLimitCaches();
		assertEquals(1, ElectionsCaches.incrementCampusProgressCheckAttempt("btn-a"));
		assertEquals(2, ElectionsCaches.incrementCampusProgressCheckAttempt("BTN-A"));
		assertEquals(1, ElectionsCaches.incrementCampusProgressCheckAttempt("btn-b"));
	}

	@Test
	void incrementPublicFailedAccessAttemptAndGetAttempts() {
		clearRateLimitCaches();
		String ip = "190.0.0.1";
		assertEquals(0, ElectionsCaches.getPublicFailedAccessAttempts(ip));
		assertEquals(1, ElectionsCaches.incrementPublicFailedAccessAttempt(ip));
		assertEquals(2, ElectionsCaches.incrementPublicFailedAccessAttempt("190.0.0.1 "));
		assertEquals(2, ElectionsCaches.getPublicFailedAccessAttempts(ip));
		ElectionsCaches.clearPublicFailedAccessRateLimitCache();
		assertEquals(0, ElectionsCaches.getPublicFailedAccessAttempts(ip));
	}

	@Test
	void publicElectionsSnapshotShouldStoreAndRetrieve() {
		PublicElectionsSnapshot snapshot = new PublicElectionsSnapshot();
		ElectionsCaches.putPublicElectionsSnapshot(snapshot);
		assertSame(snapshot, ElectionsCaches.getPublicElectionsSnapshot());
		ElectionsCaches.clearPublicElectionSnapshotCache();
		assertNull(ElectionsCaches.getPublicElectionsSnapshot());
	}

	@Test
	void healthCheckShouldStoreAndClear() {
		HealthCheck healthCheck = new HealthCheck(1, 2L, 3L, 4L, 5L, 6L, null);

		ElectionsCaches.putHealthCheck(healthCheck);

		assertSame(healthCheck, ElectionsCaches.getHealthCheck());
		ElectionsCaches.clearHealthCheckCache();
		assertNull(ElectionsCaches.getHealthCheck());
	}

	@Test
	void shouldCacheElectionSpecificSnapshots() {
		PublicElectionCoreSnapshot coreSnapshot = new PublicElectionCoreSnapshot();
		PublicElectionRollSnapshot rollSnapshot = new PublicElectionRollSnapshot();
		PublicElectionPhotoSnapshot photoSnapshot = new PublicElectionPhotoSnapshot();
		PublicElectionOfficialResultSnapshot officialResultSnapshot = new PublicElectionOfficialResultSnapshot();

		ElectionsCaches.putPublicElectionCoreSnapshot(100L, coreSnapshot);
		ElectionsCaches.putPublicElectionRollSnapshot(100L, rollSnapshot);
		ElectionsCaches.putPublicElectionPhotoSnapshot(100L, photoSnapshot);
		ElectionsCaches.putPublicElectionOfficialResultSnapshot(100L, officialResultSnapshot);

		assertSame(coreSnapshot, ElectionsCaches.getPublicElectionCoreSnapshot(100L));
		assertSame(rollSnapshot, ElectionsCaches.getPublicElectionRollSnapshot(100L));
		assertSame(photoSnapshot, ElectionsCaches.getPublicElectionPhotoSnapshot(100L));
		assertSame(officialResultSnapshot, ElectionsCaches.getPublicElectionOfficialResultSnapshot(100L));

		ElectionsCaches.clearPublicElectionSnapshotCache();
		assertNull(ElectionsCaches.getPublicElectionCoreSnapshot(100L));
		assertNull(ElectionsCaches.getPublicElectionRollSnapshot(100L));
		assertNull(ElectionsCaches.getPublicElectionPhotoSnapshot(100L));
		assertNull(ElectionsCaches.getPublicElectionOfficialResultSnapshot(100L));
	}

	@Test
	void shouldStartAndFinishCensusProcessingLifeCycle() {
		Long electionId = 111L;
		assertEquals(false, ElectionsCaches.isElectionProcessing(electionId));
		assertEquals(true, ElectionsCaches.startCensusProcessing(electionId));
		assertEquals(true, ElectionsCaches.isElectionProcessing(electionId));
		assertEquals(true, ElectionsCaches.isCensusProcessing(electionId));
		assertEquals(AsyncProcessingType.CENSUS, ElectionsCaches.getElectionProcessingType(electionId));
		ElectionsCaches.finishCensusProcessing(electionId);
		assertEquals(false, ElectionsCaches.isElectionProcessing(electionId));
	}

	@Test
	void shouldNotStartProcessingTwiceForSameElection() {
		Long electionId = 333L;
		assertEquals(true, ElectionsCaches.startOrganizationsProcessing(electionId));
		assertEquals(false, ElectionsCaches.startOrganizationsProcessing(electionId));
		ElectionsCaches.finishOrganizationsProcessing(electionId);
		assertEquals(false, ElectionsCaches.isOrganizationsProcessing(electionId));
	}

	@Test
	void shouldSetAndClearCensusProcessingError() {
		Long electionId = 222L;
		AsyncProcessingError error = new AsyncProcessingError("error.key", 10, "msg", "detail");
		ElectionsCaches.setCensusProcessingError(electionId, error);
		assertNotNull(ElectionsCaches.getCensusProcessingError(electionId));
		ElectionsCaches.setCensusProcessingError(electionId, new AsyncProcessingError("   ", null, "x", "y"));
		assertNull(ElectionsCaches.getCensusProcessingError(electionId));
	}

	@Test
	void shouldReturnCopyOfProgressSnapshots() {
		Long electionId = 444L;
		ElectionsCaches.updateCensusProcessingProgress(electionId, 2, 10, 1, 1, 0, 0, 0, 0, AsyncProcessingProgress.PHASE_PLANNED);
		AsyncProcessingProgress progress = ElectionsCaches.getCensusProcessingProgress(electionId);
		assertNotNull(progress);
		progress.setProcessedRows(99);

		AsyncProcessingProgress fresh = ElectionsCaches.getCensusProcessingProgress(electionId);
		assertEquals(2, fresh.getProcessedRows());
		ElectionsCaches.finishCensusProcessing(electionId);
	}
}
