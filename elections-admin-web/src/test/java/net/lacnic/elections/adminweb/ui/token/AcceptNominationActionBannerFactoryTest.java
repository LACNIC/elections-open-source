package net.lacnic.elections.adminweb.ui.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.adminweb.ui.token.AcceptNominationActionBannerFactory.AcceptNominationActionBanner;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;

class AcceptNominationActionBannerFactoryTest {

	@Test
	void genericPendingBannersCanTargetDifferentAvailableTasks() {
		List<AcceptNominationTaskItem> tasks = Arrays.asList(
				task(ElectionTaskKey.PROFILE, CandidateElectionTaskStatus.NOT_STARTED, false, null),
				task(ElectionTaskKey.COUNTRIES, CandidateElectionTaskStatus.NOT_STARTED, false, null),
				task(ElectionTaskKey.INCOMPATIBILITIES, CandidateElectionTaskStatus.NOT_STARTED, false, null));

		AcceptNominationActionBanner countriesBanner = new AcceptNominationActionBannerFactory(new SequenceRandom(1, 0, 0, 0, 0))
				.build(resolution(tasks));
		AcceptNominationActionBanner incompatibilitiesBanner = new AcceptNominationActionBannerFactory(new SequenceRandom(2, 0, 0, 0, 0))
				.build(resolution(tasks));

		assertEquals(ElectionTaskKey.COUNTRIES, countriesBanner.getTargetTaskKey());
		assertEquals(ElectionTaskKey.INCOMPATIBILITIES, incompatibilitiesBanner.getTargetTaskKey());
	}

	@Test
	void unlockBlockedTasksMessageIsCandidateWhenOneAvailableTaskBlocksOthers() {
		List<AcceptNominationTaskItem> tasks = Arrays.asList(
				task(ElectionTaskKey.PROFILE, CandidateElectionTaskStatus.NOT_STARTED, false, null),
				task(ElectionTaskKey.COURSE, CandidateElectionTaskStatus.NOT_STARTED, true, null),
				task(ElectionTaskKey.ORG_SUPPORTS, CandidateElectionTaskStatus.NOT_STARTED, true, null));

		AcceptNominationActionBanner banner = new AcceptNominationActionBannerFactory(new SequenceRandom(0, 0, 0, 0, 0, 0))
				.build(resolution(tasks));

		assertTrue(banner.getMessageKey().startsWith("acceptNominationActionBannerUnlockBlockedTasks."));
		assertEquals(ElectionTaskKey.PROFILE, banner.getTargetTaskKey());
		assertEquals(2, banner.getMessageParams()[0]);
	}

	@Test
	void notStartedBannerDoesNotTargetBlockedTasks() {
		List<AcceptNominationTaskItem> tasks = Arrays.asList(
				task(ElectionTaskKey.PROFILE, CandidateElectionTaskStatus.STARTED, false, null),
				task(ElectionTaskKey.EVALUATION, CandidateElectionTaskStatus.NOT_STARTED, true, null));

		AcceptNominationActionBanner banner = new AcceptNominationActionBannerFactory(new SequenceRandom(0, 0, 0, 0, 0, 1))
				.build(resolution(tasks));

		assertEquals(ElectionTaskKey.PROFILE, banner.getTargetTaskKey());
	}

	@Test
	void bannerDoesNotExposeActionWhenOnlyPendingTasksAreBlocked() {
		List<AcceptNominationTaskItem> tasks = Arrays.asList(
				task(ElectionTaskKey.COURSE, CandidateElectionTaskStatus.NOT_STARTED, true, null),
				task(ElectionTaskKey.EVALUATION, CandidateElectionTaskStatus.NOT_STARTED, true, null));

		AcceptNominationActionBanner banner = new AcceptNominationActionBannerFactory(new SequenceRandom(0, 0, 0, 0))
				.build(resolution(tasks));

		assertNull(banner.getTargetTaskKey());
	}

	@Test
	void trainingBannerDoesNotRequestCampusEnrollmentWithoutCampusConfiguration() {
		List<AcceptNominationTaskItem> tasks = List.of(
				task(ElectionTaskKey.COURSE, CandidateElectionTaskStatus.NOT_STARTED, false, null));

		AcceptNominationActionBanner banner = new AcceptNominationActionBannerFactory(new SequenceRandom(0, 0, 0, 0, 0, 0))
				.build(resolution(tasks));

		assertFalse(banner.getMessageKey().startsWith("acceptNominationActionBannerTrainingEnroll"));
	}

	@Test
	void deadlineDaysMessageCanSuggestTaskWithUpcomingDeadline() {
		List<AcceptNominationTaskItem> tasks = List.of(
				task(ElectionTaskKey.COUNTRIES, CandidateElectionTaskStatus.NOT_STARTED, false, daysFromNow(10)));

		AcceptNominationActionBanner banner = new AcceptNominationActionBannerFactory(new SequenceRandom(0, 0, 0, 0, 0, 0, 0))
				.build(resolution(tasks));

		assertTrue(banner.getMessageKey().startsWith("acceptNominationActionBannerDeadlineDays."));
		assertEquals(ElectionTaskKey.COUNTRIES, banner.getTargetTaskKey());
		assertEquals(10L, banner.getMessageParams()[0]);
	}

	private AcceptNominationTaskResolution resolution(List<AcceptNominationTaskItem> tasks) {
		Candidate candidate = new Candidate();
		Nomination nomination = new Nomination();
		nomination.setCandidate(candidate);
		return new AcceptNominationTaskResolution(nomination, candidate, tasks, tasks.get(0), AcceptNominationTaskMode.COMPLETE,
				AcceptNominationTaskMode.COMPLETE, false, false, false, null);
	}

	private AcceptNominationTaskItem task(ElectionTaskKey taskKey, CandidateElectionTaskStatus status, boolean blocked, Date deadlineDate) {
		boolean completed = status == CandidateElectionTaskStatus.COMPLETED;
		return new AcceptNominationTaskItem(null, taskKey, taskKey.name(), status, blocked, completed, null, null, deadlineDate,
				"status", "badge", completed ? "100%" : "0%");
	}

	private static Date daysFromNow(int days) {
		long day = 24L * 60L * 60L * 1000L;
		return new Date(System.currentTimeMillis() + days * day);
	}

	private static final class SequenceRandom implements IntUnaryOperator {

		private final int[] values;
		private int index;

		private SequenceRandom(int... values) {
			this.values = values == null ? new int[0] : values;
		}

		@Override
		public int applyAsInt(int operand) {
			if (values.length == 0 || index >= values.length) {
				return 0;
			}
			return values[index++];
		}
	}
}
