package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DaoPackageSmokeTest {

	@Test
	void shouldAllowInvokingAllMethodsForActivityDao() {
		assertDoesNotThrow(() -> validateDaoMethods(ActivityDao.class), ActivityDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForAuditorCandidateDecisionDao() {
		assertDoesNotThrow(() -> validateDaoMethods(AuditorCandidateDecisionDao.class),
				AuditorCandidateDecisionDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForAuditorDao() {
		assertDoesNotThrow(() -> validateDaoMethods(AuditorDao.class), AuditorDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForCandidateDao() {
		assertDoesNotThrow(() -> validateDaoMethods(CandidateDao.class), CandidateDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForCandidateElectionTaskProgressDao() {
		assertDoesNotThrow(() -> validateDaoMethods(CandidateElectionTaskProgressDao.class),
				CandidateElectionTaskProgressDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForCandidateQuestionDao() {
		assertDoesNotThrow(() -> validateDaoMethods(CandidateQuestionDao.class),
				CandidateQuestionDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForCommissionerDao() {
		assertDoesNotThrow(() -> validateDaoMethods(CommissionerDao.class), CommissionerDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForCustomizationDao() {
		assertDoesNotThrow(() -> validateDaoMethods(CustomizationDao.class), CustomizationDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForElectionCalendarDao() {
		assertDoesNotThrow(() -> validateDaoMethods(ElectionCalendarDao.class), ElectionCalendarDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForElectionDao() {
		assertDoesNotThrow(() -> validateDaoMethods(ElectionDao.class), ElectionDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForElectionEmailTemplateDao() {
		assertDoesNotThrow(() -> validateDaoMethods(ElectionEmailTemplateDao.class),
				ElectionEmailTemplateDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForElectionTaskDao() {
		assertDoesNotThrow(() -> validateDaoMethods(ElectionTaskDao.class), ElectionTaskDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForEmailDao() {
		assertDoesNotThrow(() -> validateDaoMethods(EmailDao.class), EmailDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForIpAccessDao() {
		assertDoesNotThrow(() -> validateDaoMethods(IpAccessDao.class), IpAccessDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForJointElectionDao() {
		assertDoesNotThrow(() -> validateDaoMethods(JointElectionDao.class), JointElectionDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForNominationDao() {
		assertDoesNotThrow(() -> validateDaoMethods(NominationDao.class), NominationDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForOrganizationDao() {
		assertDoesNotThrow(() -> validateDaoMethods(OrganizationDao.class), OrganizationDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForParameterDao() {
		assertDoesNotThrow(() -> validateDaoMethods(ParameterDao.class), ParameterDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForPublicElectionPageDao() {
		assertDoesNotThrow(() -> validateDaoMethods(PublicElectionPageDao.class), PublicElectionPageDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForReportDao() {
		assertDoesNotThrow(() -> validateDaoMethods(ReportDao.class), ReportDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForSupportNominationDao() {
		assertDoesNotThrow(() -> validateDaoMethods(SupportNominationDao.class), SupportNominationDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForSyncRunDao() {
		assertDoesNotThrow(() -> validateDaoMethods(SyncRunDao.class), SyncRunDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForUserAdminDao() {
		assertDoesNotThrow(() -> validateDaoMethods(UserAdminDao.class), UserAdminDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForUserVoterDao() {
		assertDoesNotThrow(() -> validateDaoMethods(UserVoterDao.class), UserVoterDao.class.getSimpleName());
	}

	@Test
	void shouldAllowInvokingAllMethodsForVoteDao() {
		assertDoesNotThrow(() -> validateDaoMethods(VoteDao.class), VoteDao.class.getSimpleName());
	}

	private void validateDaoMethods(Class<?> daoClass) throws Exception {
		EntityManager entityManager = Mockito.mock(EntityManager.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString())).thenAnswer(invocation -> {
			String query = invocation.getArgument(0, String.class);
			return createQueryMock(query);
		});
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class)))
				.thenAnswer(invocation -> {
					String query = invocation.getArgument(0, String.class);
					Class<?> resultType = invocation.getArgument(1);
					return createTypedQueryMock(query, resultType);
				});
		Mockito.when(entityManager.find(Mockito.any(Class.class), Mockito.any())).thenReturn(null);

		Constructor<?> ctor = daoClass.getConstructor(EntityManager.class);
		Object dao = ctor.newInstance(entityManager);
		assertNotNull(dao);
		assertInstanceOf(daoClass, dao);

		for (Method method : daoClass.getDeclaredMethods()) {
			if (Modifier.isStatic(method.getModifiers()) || method.isSynthetic()) {
				continue;
			}
			if (Modifier.isPrivate(method.getModifiers())) {
				continue;
			}
			Object[] arguments = createArguments(method.getParameterTypes());
			assertDoesNotThrow(() -> method.invoke(dao, arguments),
						daoClass.getSimpleName() + "#" + method.getName());
		}
	}

	private Object[] createArguments(Class<?>[] parameterTypes) {
		Object[] arguments = new Object[parameterTypes.length];
		for (int index = 0; index < parameterTypes.length; index++) {
			arguments[index] = createArgument(parameterTypes[index]);
		}
		return arguments;
	}

	private Object createArgument(Class<?> type) {
		if (type == byte.class) {
			return (byte) 1;
		}
		if (type == Byte.class) {
			return Byte.valueOf((byte) 1);
		}
		if (type == short.class) {
			return (short) 2;
		}
		if (type == Short.class) {
			return Short.valueOf((short) 2);
		}
		if (type == int.class) {
			return 1;
		}
		if (type == Integer.class) {
			return 1;
		}
		if (type == long.class) {
			return 1L;
		}
		if (type == Long.class) {
			return 1L;
		}
		if (type == boolean.class) {
			return true;
		}
		if (type == Boolean.class) {
			return true;
		}
		if (type == float.class) {
			return 1.5f;
		}
		if (type == Float.class) {
			return 1.5f;
		}
		if (type == double.class) {
			return 2.5d;
		}
		if (type == Double.class) {
			return 2.5d;
		}
		if (type == char.class) {
			return 'a';
		}
		if (type == Character.class) {
			return 'a';
		}
		if (type == String.class) {
			return "sample";
		}
		if (type == Date.class) {
			return new Date(0L);
		}
		if (type == Timestamp.class) {
			return new Timestamp(0L);
		}
		if (type == List.class || ArrayList.class.isAssignableFrom(type)) {
			return new ArrayList<>();
		}
		if (type == Set.class || HashSet.class.isAssignableFrom(type)) {
			return new HashSet<>();
		}
		if (type == Map.class || HashMap.class.isAssignableFrom(type)) {
			return new HashMap<>();
		}
		if (type == Locale.class) {
			return Locale.ROOT;
		}
		if (type.isEnum()) {
			return type.getEnumConstants()[0];
		}
		if (type.isArray()) {
			return Array.newInstance(type.getComponentType(), 0);
		}
		return Mockito.mock(type);
	}

	private Query createQueryMock(String queryText) {
		Query query = Mockito.mock(Query.class);
		configureQueryMock(query, queryText, null);
		return query;
	}

	private TypedQuery<?> createTypedQueryMock(String queryText, Class<?> resultType) {
		TypedQuery<?> query = Mockito.mock(TypedQuery.class);
		configureQueryMock(query, queryText, resultType);
		return query;
	}

	private void configureQueryMock(Query query, String queryText, Class<?> resultType) {
		Mockito.when(query.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(query);
		Mockito.when(query.setParameter(Mockito.anyString(), Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.setParameter(Mockito.anyString(), Mockito.anyLong())).thenReturn(query);
		Mockito.when(query.setParameter(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(query);
		Mockito.when(query.setParameter(Mockito.anyString(), Mockito.anyDouble())).thenReturn(query);
		Mockito.when(query.setFirstResult(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.setMaxResults(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(Collections.emptyList());
		Mockito.when(query.getResultStream()).thenReturn(Stream.empty());
		Mockito.when(query.executeUpdate()).thenReturn(1);
		Mockito.when(query.getSingleResult()).thenReturn(querySingleResult(queryText, resultType));
	}

	private Object querySingleResult(String queryText, Class<?> resultType) {
		if (queryText == null) {
			return null;
		}
		String normalized = queryText.toUpperCase(Locale.ROOT);
		if (normalized.contains("COUNT(") || normalized.contains("COALESCE(SUM")) {
			return 0L;
		}
		if (resultType != null) {
			if (resultType == Integer.class || resultType == int.class) {
				return 1;
			}
			if (resultType == Long.class || resultType == long.class) {
				return 1L;
			}
			if (resultType == Boolean.class || resultType == boolean.class) {
				return true;
			}
			if (resultType.isArray()) {
				return Array.newInstance(resultType.getComponentType(), 0);
			}
			if (resultType != null && !resultType.isPrimitive() && !resultType.isEnum() && !java.lang.String.class.equals(resultType)) {
				return Mockito.mock(resultType);
			}
			if (resultType == String.class) {
				return "";
			}
		}
		if (normalized.contains("CANDIDATEORDER")) {
			return 1;
		}
		return null;
	}
}
