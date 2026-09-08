package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import org.junit.jupiter.api.Test;

class CandidateQuestionDaoTest {

	@Test
	void shouldGetCandidateQuestionById() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateQuestion> query = mock(TypedQuery.class);
		CandidateQuestion question = mock(CandidateQuestion.class);
		when(em.createQuery(anyString(), eq(CandidateQuestion.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(question);

		CandidateQuestionDao dao = new CandidateQuestionDao(em);
		CandidateQuestion result = dao.getCandidateQuestion(12L);

		assertSame(question, result);
	}

	@Test
	void shouldGetElectionCandidateQuestions() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateQuestion> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(CandidateQuestion.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(CandidateQuestion.class), mock(CandidateQuestion.class)));

		CandidateQuestionDao dao = new CandidateQuestionDao(em);
		List<CandidateQuestion> result = dao.getElectionCandidateQuestions(15L);

		assertNotNull(result);
		assertEquals(2, result.size());
	}

	@Test
	void shouldGetElectionCandidateQuestionsForPublicElectionPage() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateQuestion> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(CandidateQuestion.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(CandidateQuestion.class)));

		CandidateQuestionDao dao = new CandidateQuestionDao(em);
		List<CandidateQuestion> result = dao.getElectionCandidateQuestionsForPublicElectionPage(15L);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void shouldGetCandidateQuestionsByCandidateId() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateQuestion> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(CandidateQuestion.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(CandidateQuestion.class)));

		CandidateQuestionDao dao = new CandidateQuestionDao(em);
		List<CandidateQuestion> result = dao.getCandidateQuestionsByCandidateId(77L);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void shouldGetCandidateQuestionByIdAndCandidateId() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateQuestion> query = mock(TypedQuery.class);
		CandidateQuestion question = mock(CandidateQuestion.class);
		when(em.createQuery(anyString(), eq(CandidateQuestion.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(question));

		CandidateQuestionDao dao = new CandidateQuestionDao(em);
		CandidateQuestion result = dao.getCandidateQuestionByIdAndCandidateId(44L, 3L);

		assertSame(question, result);
	}

	@Test
	void shouldGetCandidateQuestionByIdAndCandidateIdReturnsNullWhenNoResult() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateQuestion> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(CandidateQuestion.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of());

		CandidateQuestionDao dao = new CandidateQuestionDao(em);
		CandidateQuestion result = dao.getCandidateQuestionByIdAndCandidateId(44L, 3L);

		assertNull(result);
	}
}
