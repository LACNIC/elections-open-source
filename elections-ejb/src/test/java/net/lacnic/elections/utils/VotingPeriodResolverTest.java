package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionLight;

class VotingPeriodResolverTest {

	@Test
	void windowDatesShouldBeNullWhenEntityManagerIsMissingOrElectionIdIsInvalid() {
		assertNull(VotingPeriodResolver.getVotingStartDate(null, 1L));
		assertNull(VotingPeriodResolver.getVotingEndDate(null, 1L));
		assertNull(VotingPeriodResolver.getNominationStartDate(null, 1L));
		assertNull(VotingPeriodResolver.getNominationEndDate(null, 1L));

		assertNull(VotingPeriodResolver.getVotingStartDate(null, 0L));
		assertNull(VotingPeriodResolver.getVotingEndDate(null, -1L));
		assertNull(VotingPeriodResolver.getNominationStartDate(null, -1L));
		assertNull(VotingPeriodResolver.getNominationEndDate(null, 0L));
	}

	@Test
	void isVotingStartedOrFinishedShouldReturnFalseWhenReferenceDataIsMissing() {
		Date reference = new Date();

		assertFalse(VotingPeriodResolver.isVotingStarted(null, 1L, reference));
		assertFalse(VotingPeriodResolver.isVotingStarted(null, 1L, null));
		assertFalse(VotingPeriodResolver.isVotingFinished(null, 1L, reference));
		assertFalse(VotingPeriodResolver.isVotingFinished(null, 1L, null));
	}

	@Test
	void applyVotingWindowMethodsShouldNotMutateExistingDatesWhenEntityManagerIsMissing() {
		Date start = new Date(1_000L);
		Date end = new Date(2_000L);
		Date nominationStart = new Date(3_000L);
		Date nominationEnd = new Date(4_000L);

		Election election = new Election();
		election.setElectionId(11L);
		election.setVotingPeriodStartDate(start);
		election.setVotingPeriodEndDate(end);

		VotingPeriodResolver.applyVotingWindow((EntityManager) null, election);
		ElectionLight nominationElection = new ElectionLight();
		nominationElection.setElectionId(11L);
		nominationElection.setNominationPeriodStartDate(nominationStart);
		nominationElection.setNominationPeriodEndDate(nominationEnd);
		VotingPeriodResolver.applyNominationWindow((EntityManager) null, nominationElection);

		assertNull(election.getVotingPeriodStartDate());
		assertNull(election.getVotingPeriodEndDate());
		assertNull(nominationElection.getNominationPeriodStartDate());
		assertNull(nominationElection.getNominationPeriodEndDate());
	}

	@Test
	void applyVotingWindowForLightsShouldSkipProcessingWhenManagerOrCollectionIsInvalid() {
		ElectionLight electionLight = new ElectionLight();
		electionLight.setElectionId(22L);
		Date start = new Date(5_000L);
		Date end = new Date(6_000L);
		Date nominationStart = new Date(7_000L);
		Date nominationEnd = new Date(8_000L);
		electionLight.setVotingPeriodStartDate(start);
		electionLight.setVotingPeriodEndDate(end);
		electionLight.setNominationPeriodStartDate(nominationStart);
		electionLight.setNominationPeriodEndDate(nominationEnd);

		assertDoesNotThrow(() -> VotingPeriodResolver.applyVotingWindowToLight((EntityManager) null, Collections.<ElectionLight>emptyList()));
		assertDoesNotThrow(() -> VotingPeriodResolver.applyVotingWindowToLight((EntityManager) null, Arrays.asList(electionLight)));
		assertDoesNotThrow(() -> VotingPeriodResolver.applyNominationWindowToLight((EntityManager) null, Arrays.asList(electionLight)));

		assertSame(start, electionLight.getVotingPeriodStartDate());
		assertSame(end, electionLight.getVotingPeriodEndDate());
		assertSame(nominationStart, electionLight.getNominationPeriodStartDate());
		assertSame(nominationEnd, electionLight.getNominationPeriodEndDate());
	}
}
