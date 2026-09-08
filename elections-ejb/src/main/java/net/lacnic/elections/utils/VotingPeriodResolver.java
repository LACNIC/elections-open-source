package net.lacnic.elections.utils;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;
import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionLight;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;

public final class VotingPeriodResolver {

	private VotingPeriodResolver() {
	}

	public static Date getVotingStartDate(EntityManager em, long electionId) {
		if (em == null || electionId <= 0) {
			return null;
		}
		return ElectionsDaoFactory.createElectionCalendarDao(em).getCalendarStartDate(electionId, ElectionCalendarKey.N_16_PERIODO_VOTING);
	}

	public static Date getVotingEndDate(EntityManager em, long electionId) {
		if (em == null || electionId <= 0) {
			return null;
		}
		return ElectionsDaoFactory.createElectionCalendarDao(em).getCalendarEndDate(electionId, ElectionCalendarKey.N_16_PERIODO_VOTING);
	}

	public static Date getNominationStartDate(EntityManager em, long electionId) {
		if (em == null || electionId <= 0) {
			return null;
		}
		return ElectionsDaoFactory.createElectionCalendarDao(em).getCalendarStartDate(electionId, ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
	}

	public static Date getNominationEndDate(EntityManager em, long electionId) {
		if (em == null || electionId <= 0) {
			return null;
		}
		return ElectionsDaoFactory.createElectionCalendarDao(em).getCalendarEndDate(electionId, ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
	}

	public static void applyVotingWindow(EntityManager em, Election election) {
		if (election == null) {
			return;
		}
		Date startDate = getVotingStartDate(em, election.getElectionId());
		Date endDate = getVotingEndDate(em, election.getElectionId());
		election.setVotingPeriodStartDate(startDate);
		election.setVotingPeriodEndDate(endDate);
	}

	public static void applyVotingWindow(EntityManager em, Collection<Election> elections) {
		if (em == null || elections == null || elections.isEmpty()) {
			return;
		}
		Map<Long, Date[]> votingWindows = getCalendarWindowByElectionIds(em, extractElectionIds(elections), ElectionCalendarKey.N_16_PERIODO_VOTING);
		for (Election election : elections) {
			if (election == null) {
				continue;
			}
			Date[] window = votingWindows.get(election.getElectionId());
			election.setVotingPeriodStartDate(window != null ? window[0] : null);
			election.setVotingPeriodEndDate(window != null ? window[1] : null);
		}
	}

	public static void applyVotingWindow(EntityManager em, ElectionLight election) {
		if (election == null) {
			return;
		}
		Date startDate = getVotingStartDate(em, election.getElectionId());
		Date endDate = getVotingEndDate(em, election.getElectionId());
		election.setVotingPeriodStartDate(startDate);
		election.setVotingPeriodEndDate(endDate);
	}

	public static void applyNominationWindow(EntityManager em, ElectionLight election) {
		if (election == null) {
			return;
		}
		Date startDate = getNominationStartDate(em, election.getElectionId());
		Date endDate = getNominationEndDate(em, election.getElectionId());
		election.setNominationPeriodStartDate(startDate);
		election.setNominationPeriodEndDate(endDate);
	}

	public static void applyVotingWindowToLight(EntityManager em, Collection<ElectionLight> elections) {
		if (em == null || elections == null || elections.isEmpty()) {
			return;
		}
		Map<Long, Date[]> votingWindows = getCalendarWindowByElectionIds(em, extractElectionLightIds(elections), ElectionCalendarKey.N_16_PERIODO_VOTING);
		for (ElectionLight election : elections) {
			if (election == null) {
				continue;
			}
			Date[] window = votingWindows.get(election.getElectionId());
			election.setVotingPeriodStartDate(window != null ? window[0] : null);
			election.setVotingPeriodEndDate(window != null ? window[1] : null);
		}
	}

	public static void applyNominationWindowToLight(EntityManager em, Collection<ElectionLight> elections) {
		if (em == null || elections == null || elections.isEmpty()) {
			return;
		}
		Map<Long, Date[]> nominationWindows = getCalendarWindowByElectionIds(em, extractElectionLightIds(elections), ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
		for (ElectionLight election : elections) {
			if (election == null) {
				continue;
			}
			Date[] window = nominationWindows.get(election.getElectionId());
			election.setNominationPeriodStartDate(window != null ? window[0] : null);
			election.setNominationPeriodEndDate(window != null ? window[1] : null);
		}
	}

	public static boolean isVotingStarted(EntityManager em, long electionId, Date referenceDate) {
		Date startDate = getVotingStartDate(em, electionId);
		return startDate != null && referenceDate != null && referenceDate.after(startDate);
	}

	public static boolean isVotingFinished(EntityManager em, long electionId, Date referenceDate) {
		Date endDate = getVotingEndDate(em, electionId);
		return endDate != null && referenceDate != null && referenceDate.after(endDate);
	}

	private static Map<Long, Date[]> getCalendarWindowByElectionIds(EntityManager em, Set<Long> electionIds, ElectionCalendarKey calendarKey) {
		if (em == null || electionIds == null || electionIds.isEmpty()) {
			return Map.of();
		}
		return ElectionsDaoFactory.createElectionCalendarDao(em).getCalendarWindowByElectionIds(electionIds, calendarKey);
	}

	private static Set<Long> extractElectionIds(Collection<Election> elections) {
		Set<Long> electionIds = new HashSet<>();
		for (Election election : elections) {
			if (election != null && election.getElectionId() > 0) {
				electionIds.add(election.getElectionId());
			}
		}
		return electionIds;
	}

	private static Set<Long> extractElectionLightIds(Collection<ElectionLight> elections) {
		Set<Long> electionIds = new HashSet<>();
		for (ElectionLight election : elections) {
			if (election != null && election.getElectionId() > 0) {
				electionIds.add(election.getElectionId());
			}
		}
		return electionIds;
	}
}
