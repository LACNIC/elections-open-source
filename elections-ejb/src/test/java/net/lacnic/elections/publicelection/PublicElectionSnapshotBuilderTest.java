package net.lacnic.elections.publicelection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.CandidateType;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionAuditorResult;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCoreSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionVoteCountRow;
import org.junit.jupiter.api.Test;

class PublicElectionSnapshotBuilderTest {

	@Test
	void shouldExposeOfficialResultTextsInCoreWithoutLettersWhenNoFilesExist() throws Exception {
		PublicElectionSnapshotBuilder builder = new PublicElectionSnapshotBuilder(null);
		ElectionAuditorResult auditorResult = new ElectionAuditorResult();
		auditorResult.setResultSpanish("Resultado ES");
		auditorResult.setResultEnglish("Result EN");
		auditorResult.setResultPortuguese("Resultado PT");

		Method buildOfficialResultData = PublicElectionSnapshotBuilder.class.getDeclaredMethod(
				"buildOfficialResultData",
				long.class,
				ElectionAuditorResult.class);
		buildOfficialResultData.setAccessible(true);

		PublicElectionCoreSnapshot.OfficialResultData data = (PublicElectionCoreSnapshot.OfficialResultData) buildOfficialResultData.invoke(
				builder,
				11L,
				auditorResult);

		assertEquals("Resultado ES", data.getResultSpanish());
		assertEquals("Result EN", data.getResultEnglish());
		assertEquals("Resultado PT", data.getResultPortuguese());
		assertEquals(null, data.getResultLetterSpanishUrl());
		assertEquals(null, data.getResultLetterEnglishUrl());
		assertEquals(null, data.getResultLetterPortugueseUrl());
	}

	@Test
	void shouldExposeElectionDescriptionsInCoreElectionData() throws Exception {
		PublicElectionSnapshotBuilder builder = new PublicElectionSnapshotBuilder(null);
		Election election = new Election();
		election.setElectionId(11L);
		election.setDescriptionSpanish("Descripcion ES");
		election.setDescriptionEnglish("Description EN");
		election.setDescriptionPortuguese("Descricao PT");

		Method buildElectionData = PublicElectionSnapshotBuilder.class.getDeclaredMethod("buildElectionData", Election.class);
		buildElectionData.setAccessible(true);

		PublicElectionCoreSnapshot.ElectionData data = (PublicElectionCoreSnapshot.ElectionData) buildElectionData.invoke(builder, election);

		assertEquals("Descripcion ES", data.getDescriptionSpanish());
		assertEquals("Description EN", data.getDescriptionEnglish());
		assertEquals("Descricao PT", data.getDescriptionPortuguese());
	}

	@Test
	void shouldIncludePublishedAbstentionInResultRows() throws Exception {
		PublicElectionSnapshotBuilder builder = new PublicElectionSnapshotBuilder(null);
		Candidate candidate = candidate(1L, "Candidate", CandidateType.NORMAL, CandidateStatus.CONFIRMED_AND_PUBLISHED);
		Candidate abstention = candidate(2L, "Abstencion", CandidateType.ABSTENTION, CandidateStatus.CONFIRMED_AND_PUBLISHED);
		Candidate unpublishedAbstention = candidate(3L, "Hidden abstention", CandidateType.ABSTENTION, CandidateStatus.INCOMPLETE);

		Method buildResultCandidates = PublicElectionSnapshotBuilder.class.getDeclaredMethod("buildResultCandidates", List.class, Iterable.class);
		buildResultCandidates.setAccessible(true);

		@SuppressWarnings("unchecked")
		List<Candidate> resultCandidates = (List<Candidate>) buildResultCandidates.invoke(
				builder,
				List.of(candidate),
				List.of(candidate, abstention, unpublishedAbstention));

		assertEquals(2, resultCandidates.size());
		assertTrue(resultCandidates.contains(candidate));
		assertTrue(resultCandidates.contains(abstention));

		Method buildResultSummary = PublicElectionSnapshotBuilder.class.getDeclaredMethod(
				"buildResultSummary",
				List.class,
				List.class,
				List.class,
				java.util.Map.class);
		buildResultSummary.setAccessible(true);

		PublicElectionCoreSnapshot.ResultSummaryData summary = (PublicElectionCoreSnapshot.ResultSummaryData) buildResultSummary.invoke(
				builder,
				List.of(new PublicElectionVoteCountRow(1L, 10L), new PublicElectionVoteCountRow(2L, 5L)),
				resultCandidates,
				new ArrayList<UserVoter>(),
				new EnumMap<ElectionCalendarKey, Object>(ElectionCalendarKey.class));

		assertEquals(15L, summary.getTotalVotes().longValue());
		assertEquals(2, summary.getRows().size());
		assertEquals(Long.valueOf(2L), summary.getRows().get(1).getCandidateId());
		assertEquals(5L, summary.getRows().get(1).getVoteCount().longValue());
	}

	private Candidate candidate(long id, String name, CandidateType type, CandidateStatus status) {
		Candidate candidate = new Candidate();
		candidate.setCandidateId(id);
		candidate.setName(name);
		candidate.setCandidateType(type);
		candidate.setStatus(status);
		return candidate;
	}
}
