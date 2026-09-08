package net.lacnic.elections.domain.pre;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.LanguageCode;

 class CandidateQuestionAndTaskTest extends TestCase {

	 static Test suite() {
		return new TestSuite(CandidateQuestionAndTaskTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testCandidateQuestionDefaultsAndQuestionTextFlow() {
		CandidateQuestion question = new CandidateQuestion();

		assertEquals(CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC, question.getStatus());
		assertEquals(CandidateQuestionOwner.LACNIC, question.getCurrentOwner());
		assertNotNull(question.getCreationDate());
		assertNotNull(question.getUpdateDate());

		question.applyQuestionText(LanguageCode.EN, "Question in english");
		assertEquals(LanguageCode.EN, question.getQuestionLanguage());
		assertEquals("Question in english", question.getQuestionEnglish());
		assertNull(question.getQuestionSpanish());
		assertNull(question.getQuestionPortuguese());
		assertEquals("Question in english", question.getQuestionPreview());

		question.applyQuestionText(LanguageCode.SP, "Pregunta en español");
		assertEquals("Pregunta en español", question.getQuestionSpanish());
		assertEquals("Pregunta en español", question.getQuestionPreview());

		question.applyQuestionText(LanguageCode.PT, "Pergunta em português");
		assertEquals("Pergunta em português", question.getQuestionPortuguese());
		assertEquals("Pregunta en español", question.getQuestionPreview());
	}

	@org.junit.jupiter.api.Test

	 void testCandidateQuestionStatusLabelsAndActionFallbacks() {
		assertEquals(CandidateQuestionOwner.CANDIDATE, CandidateQuestionStatus.QUESTION_READY_FOR_CANDIDATE.resolveOwner());
		assertEquals(CandidateQuestionOwner.NONE, CandidateQuestionStatus.PUBLISHED.resolveOwner());
		assertEquals(CandidateQuestionOwner.LACNIC, CandidateQuestionStatus.ANSWER_SUBMITTED_BY_CANDIDATE.resolveOwner());

		assertEquals("Pregunta recibida por la organización", CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC.resolveLabel(null));
		assertEquals("Question received by the organization", CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC.resolveLabel(LanguageCode.EN));
		assertEquals("Pergunta recebida pela organização", CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC.resolveLabel(LanguageCode.PT));

		assertEquals("Revisar la pregunta (traducirla) y definir si es una pregunta válida para enviar al candidato o rechazarla.",
				CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC.resolveActionRequired(null));
	}

	@org.junit.jupiter.api.Test

	 void testTaskDependencyLevelAndElectionTaskDefaults() {
		assertEquals(TaskDependencyLevel.LEVEL_1, TaskDependencyLevel.fromIndex(1));
		assertEquals(TaskDependencyLevel.LEVEL_6, TaskDependencyLevel.fromIndex(6));
		assertEquals(TaskDependencyLevel.LEVEL_1, TaskDependencyLevel.fromIndex(99));

		ElectionTask electionTask = new ElectionTask();
		assertEquals(TaskDependencyLevel.LEVEL_1, electionTask.getDependencyLevel());

		ElectionTask task = new ElectionTask(new Election(), new ElectionCalendar(), ElectionTaskKey.PROFILE, null, 20, true);
		assertEquals(TaskDependencyLevel.LEVEL_1, task.getDependencyLevel());
		assertEquals(ElectionTaskKey.PROFILE, task.getTaskKey());
		assertEquals(Integer.valueOf(20), task.getDisplayOrder());
		assertTrue(task.isPublicable());
	}

	@org.junit.jupiter.api.Test

	 void testNominationConstructorMapsFields() {
		Election election = new Election();
		Organization org = new Organization();
		Nomination nomination = new Nomination(election, org, NominationStatus.PROPOSED, "tkn", "2024-01-01", "Alice", "alice@example.org", "+5981111", "Reason");

		assertEquals("tkn", nomination.getAcceptNominationToken());
		assertEquals("2024-01-01", nomination.getNominationDate());
		assertEquals("Alice", nomination.getNominationName());
		assertEquals("alice@example.org", nomination.getNominationEmail());
		assertEquals("+5981111", nomination.getNominationPhoneNumber());
		assertEquals("Reason", nomination.getNominationReason());
		assertEquals("Reason", nomination.getNominationReasonSpanish());
		assertEquals("Reason", nomination.getNominationReasonEnglish());
		assertEquals("Reason", nomination.getNominationReasonPortuguese());
		assertEquals("Reason", nomination.getNominationReason(LanguageCode.EN.getLocaleCode()));
		assertEquals(NominationStatus.PROPOSED, nomination.getStatus());
		assertSame(election, nomination.getElection());
		assertSame(org, nomination.getOrganization());
	}

	@org.junit.jupiter.api.Test

	 void testCandidateElectionTaskProgressTracksDates() {
		Candidate candidate = new Candidate();
		ElectionTask task = new ElectionTask(new Election(), new ElectionCalendar(), ElectionTaskKey.PROFILE, TaskDependencyLevel.LEVEL_2, 10, true);
		CandidateElectionTaskProgress progress = new CandidateElectionTaskProgress(candidate, task, CandidateElectionTaskStatus.STARTED);

		Date now = new Date();
		progress.setStartDate(now);
		progress.setEndDate(now);
		progress.setStatus(CandidateElectionTaskStatus.COMPLETED);

		assertEquals(CandidateElectionTaskStatus.COMPLETED, progress.getStatus());
		assertSame(candidate, progress.getCandidate());
		assertSame(task, progress.getElectionTask());
		assertEquals(now, progress.getStartDate());
		assertEquals(now, progress.getEndDate());
	}
}
