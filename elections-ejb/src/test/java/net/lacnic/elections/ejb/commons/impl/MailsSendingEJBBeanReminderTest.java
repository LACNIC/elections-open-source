package net.lacnic.elections.ejb.commons.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.utils.EmailTemplateType;

class MailsSendingEJBBeanReminderTest {

	@Test
	void templateEmailShouldNotBePersistedWhenTemplateIsMissing() throws Exception {
		EntityManager em = mock(EntityManager.class);
		TypedQuery<ElectionCalendar> calendarQuery = mock(TypedQuery.class, Answers.RETURNS_SELF);
		TypedQuery<ElectionEmailTemplate> templateQuery = mock(TypedQuery.class, Answers.RETURNS_SELF);
		when(calendarQuery.getResultList()).thenReturn(Collections.emptyList());
		when(templateQuery.getResultList()).thenReturn(Collections.emptyList());
		when(em.createQuery(anyString(), eq(ElectionCalendar.class))).thenReturn(calendarQuery);
		when(em.createQuery(anyString(), eq(ElectionEmailTemplate.class))).thenReturn(templateQuery);

		MailsSendingEJBBean bean = new MailsSendingEJBBean();
		setEntityManager(bean, em);
		Election election = new Election();
		election.setElectionId(95L);

		bean.queueTemplateEmail(election, EmailTemplateType.CANDIDATE_REMINDER, "candidate@example.net", null, LanguageCode.SP, Collections.emptyMap());

		verify(em, never()).persist(any(Email.class));
	}

	private void setEntityManager(MailsSendingEJBBean bean, EntityManager em) throws Exception {
		Field field = MailsSendingEJBBean.class.getDeclaredField("em");
		field.setAccessible(true);
		field.set(bean, em);
	}
}
