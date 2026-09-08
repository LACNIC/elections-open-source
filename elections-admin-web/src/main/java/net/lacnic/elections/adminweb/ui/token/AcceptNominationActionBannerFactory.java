package net.lacnic.elections.adminweb.ui.token;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

import net.lacnic.elections.adminweb.wicket.util.BootstrapCssClasses;
import net.lacnic.elections.campus.CampusClient;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateCampusCourseStatus;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateEvaluationStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;

public class AcceptNominationActionBannerFactory {

	private static final int URGENT_DEADLINE_HOURS = 24;
	private static final int SOON_DEADLINE_HOURS = 72;
	private static final int MESSAGE_VARIANTS = 4;
	private static final int UNLOCK_BLOCKED_TASKS_MESSAGE_VARIANTS = 3;
	private static final int DEADLINE_DAYS_MESSAGE_VARIANTS = 3;
	private static final int WARNING_SELECTION_PROBABILITY_PERCENT = 70;
	private static final String DEFAULT_ELECTION_RECIPIENT_TOKEN = "$receptorpordefectoelecci\u00f3n";

	private final IntUnaryOperator randomIndex;

	public AcceptNominationActionBannerFactory() {
		this(bound -> ThreadLocalRandom.current().nextInt(bound));
	}

	AcceptNominationActionBannerFactory(IntUnaryOperator randomIndex) {
		this.randomIndex = randomIndex != null ? randomIndex : bound -> ThreadLocalRandom.current().nextInt(bound);
	}

	public AcceptNominationActionBanner build(AcceptNominationTaskResolution resolution) {
		if (resolution == null || resolution.getTasks() == null || resolution.getTasks().isEmpty()) {
			return createBanner(TokenResourceKeys.ACCEPT_NOMINATION_ACTION_BANNER_DEFAULT, MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING, null, AcceptNominationTaskMode.VIEW);
		}
		CandidateStatus candidateStatus = resolveCandidateStatus(resolution.getCandidate());
		if (candidateStatus == CandidateStatus.COMPLETE) {
			return createBanner("acceptNominationActionBannerCandidateComplete", 1, BootstrapCssClasses.BG_INFO, null, AcceptNominationTaskMode.VIEW);
		}
		if (candidateStatus == CandidateStatus.REJECTED) {
			return createBanner("acceptNominationActionBannerCandidateRejected", 1, BootstrapCssClasses.BG_DANGER, null, AcceptNominationTaskMode.VIEW);
		}
		if (candidateStatus == CandidateStatus.CONFIRMED_AND_PUBLISHED) {
			return createBanner("acceptNominationActionBannerCandidateApproved", 1, "bg-success", null, AcceptNominationTaskMode.VIEW);
		}

		List<AcceptNominationTaskItem> tasks = resolution.getTasks();
		Date now = new Date();
		List<AcceptNominationTaskItem> availablePendingTasks = findAvailablePendingTasks(tasks, now);
		int blockedPendingTaskCount = countBlockedPendingTasks(tasks);
		AcceptNominationTaskItem onlyTaskBlockingOthers = resolveOnlyTaskBlockingOthers(availablePendingTasks, blockedPendingTaskCount);

		List<AcceptNominationActionBanner> criticalCandidates = new ArrayList<>();
		List<AcceptNominationActionBanner> warningCandidates = new ArrayList<>();
		List<AcceptNominationActionBanner> regularCandidates = new ArrayList<>();

		DeadlineInsight deadlineInsight = analyzeDeadlines(tasks, now);
		if (deadlineInsight.overdueCount > 0) {
			criticalCandidates.add(createBanner("acceptNominationActionBannerDeadlineOverdue", MESSAGE_VARIANTS, BootstrapCssClasses.BG_DANGER,
					null, AcceptNominationTaskMode.VIEW, deadlineInsight.overdueCount, resolveDefaultRecipient(resolution)));
		} else if (deadlineInsight.nearestUpcomingHours > 0 && deadlineInsight.nearestUpcomingTask != null) {
			if (deadlineInsight.nearestUpcomingHours <= URGENT_DEADLINE_HOURS) {
				criticalCandidates.add(createBanner("acceptNominationActionBannerDeadlineUrgent", MESSAGE_VARIANTS, BootstrapCssClasses.BG_DANGER,
						resolveTaskKey(deadlineInsight.nearestUpcomingTask), AcceptNominationTaskMode.COMPLETE, deadlineInsight.nearestUpcomingHours));
			} else if (deadlineInsight.nearestUpcomingHours <= SOON_DEADLINE_HOURS) {
				warningCandidates.add(createBanner("acceptNominationActionBannerDeadlineSoon", MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
						resolveTaskKey(deadlineInsight.nearestUpcomingTask), AcceptNominationTaskMode.COMPLETE, deadlineInsight.nearestUpcomingHours));
			}
		}
		if (onlyTaskBlockingOthers != null) {
			regularCandidates.add(createBanner("acceptNominationActionBannerUnlockBlockedTasks", UNLOCK_BLOCKED_TASKS_MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
					resolveTaskKey(onlyTaskBlockingOthers), AcceptNominationTaskMode.COMPLETE, blockedPendingTaskCount));
		}
		AcceptNominationTaskItem deadlineDaysTask = findRandomUpcomingDeadlineTask(availablePendingTasks, now, SOON_DEADLINE_HOURS);
		if (deadlineDaysTask != null) {
			regularCandidates.add(createBanner("acceptNominationActionBannerDeadlineDays", DEADLINE_DAYS_MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
					resolveTaskKey(deadlineDaysTask), AcceptNominationTaskMode.COMPLETE, calculateDaysUntil(deadlineDaysTask.getDeadlineDate(), now)));
		}

		SupportInsight supportInsight = summarizeSupports(resolution.getNomination());
		boolean prioritizeSupportBanners = !hasAvailablePendingNonSupportTask(tasks, now);
		List<AcceptNominationActionBanner> supportCandidatesBucket = prioritizeSupportBanners ? warningCandidates : regularCandidates;

		AcceptNominationTaskItem orgSupportsTask = findTask(tasks, ElectionTaskKey.ORG_SUPPORTS);
		if (isAvailablePendingTask(orgSupportsTask, now)) {
			if (supportInsight.orgProposedCount > 0) {
				supportCandidatesBucket.add(createBanner("acceptNominationActionBannerOrgSupportsPendingApproval", MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
						ElectionTaskKey.ORG_SUPPORTS, AcceptNominationTaskMode.COMPLETE, supportInsight.orgProposedCount));
			}
			if (supportInsight.orgActiveCount == 0) {
				supportCandidatesBucket.add(createBanner("acceptNominationActionBannerOrgSupportsMissingRequests", MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
						ElectionTaskKey.ORG_SUPPORTS, AcceptNominationTaskMode.COMPLETE));
			}
		}

		AcceptNominationTaskItem userSupportsTask = findPendingUserSupportsTask(tasks, now);
		if (userSupportsTask != null) {
			if (supportInsight.userProposedCount > 0) {
				supportCandidatesBucket.add(createBanner("acceptNominationActionBannerUserSupportsPendingApproval", MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
						resolveTaskKey(userSupportsTask), AcceptNominationTaskMode.COMPLETE, supportInsight.userProposedCount));
			}
			int requiredSupports = resolveRequiredUserSupports(userSupportsTask.getTaskKey());
			int missingSupports = Math.max(0, requiredSupports - supportInsight.userActiveCount);
			if (missingSupports > 0) {
				supportCandidatesBucket.add(createBanner("acceptNominationActionBannerUserSupportsMissingRequests", MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
						resolveTaskKey(userSupportsTask), AcceptNominationTaskMode.COMPLETE, missingSupports));
			}
		}

		AcceptNominationTaskItem courseTask = findTask(tasks, ElectionTaskKey.COURSE);
		AcceptNominationTaskItem evaluationTask = findTask(tasks, ElectionTaskKey.EVALUATION);
		CandidateCampusCourseStatus campusStatus = resolveCampusStatus(resolution.getCandidate());
		CandidateEvaluationStatus evaluationStatus = resolveEvaluationStatus(resolution.getCandidate());
		if (isAvailablePendingTask(courseTask, now) && isCampusTrainingEnabled(resolution.getCandidate())) {
			ElectionTaskKey trainingTaskKey = resolveTaskKey(courseTask);
			if (campusStatus == CandidateCampusCourseStatus.PENDING) {
				regularCandidates.add(createBanner("acceptNominationActionBannerTrainingEnroll", MESSAGE_VARIANTS, BootstrapCssClasses.BG_INFO,
						trainingTaskKey, AcceptNominationTaskMode.COMPLETE));
			} else if (campusStatus == CandidateCampusCourseStatus.SENT || campusStatus == CandidateCampusCourseStatus.STARTED) {
				warningCandidates.add(createBanner("acceptNominationActionBannerTrainingPending", MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
						trainingTaskKey, AcceptNominationTaskMode.COMPLETE));
			}
		}
		if (isAvailablePendingTask(evaluationTask, now)) {
			ElectionTaskKey evaluationTaskKey = resolveTaskKey(evaluationTask);
			if (evaluationStatus == CandidateEvaluationStatus.PENDING) {
				regularCandidates.add(createBanner("acceptNominationActionBannerTrainingEnroll", MESSAGE_VARIANTS, BootstrapCssClasses.BG_INFO,
						evaluationTaskKey, AcceptNominationTaskMode.COMPLETE));
			} else if (evaluationStatus == CandidateEvaluationStatus.CREDENTIALS_REQUESTED || evaluationStatus == CandidateEvaluationStatus.CREDENTIALS_SENT) {
				warningCandidates.add(createBanner("acceptNominationActionBannerTrainingPending", MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
						evaluationTaskKey, AcceptNominationTaskMode.COMPLETE));
			}
		}

		AcceptNominationTaskItem randomNotStartedTask = findRandomTaskByStatus(tasks, CandidateElectionTaskStatus.NOT_STARTED, true, now);
		if (randomNotStartedTask != null) {
			regularCandidates.add(createBanner("acceptNominationActionBannerTaskNotStarted", MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
					resolveTaskKey(randomNotStartedTask), AcceptNominationTaskMode.COMPLETE));
		}

		int actionablePendingTaskCount = countPendingTasks(tasks, true, now);
		int pendingTaskCount = countPendingTasks(tasks, false, now);
		AcceptNominationTaskItem defaultPendingTarget = findRandomPendingTask(tasks, true, now);
		if (actionablePendingTaskCount > 0) {
			regularCandidates.add(createBanner("acceptNominationActionBannerPendingTasks", MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
					resolveTaskKey(defaultPendingTarget), AcceptNominationTaskMode.COMPLETE, actionablePendingTaskCount));
		} else if (pendingTaskCount > 0) {
			regularCandidates.add(createBanner(TokenResourceKeys.ACCEPT_NOMINATION_ACTION_BANNER_DEFAULT, MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING,
					null, AcceptNominationTaskMode.VIEW));
		} else {
			regularCandidates.add(createBanner("acceptNominationActionBannerAllCompleted", MESSAGE_VARIANTS, "bg-success", null, AcceptNominationTaskMode.VIEW));
		}

		if (!criticalCandidates.isEmpty()) {
			return randomFrom(criticalCandidates);
		}
		if (!warningCandidates.isEmpty() && !regularCandidates.isEmpty()) {
			int probability = nextIndex(100);
			return probability < WARNING_SELECTION_PROBABILITY_PERCENT ? randomFrom(warningCandidates) : randomFrom(regularCandidates);
		}
		if (!warningCandidates.isEmpty()) {
			return randomFrom(warningCandidates);
		}
		if (!regularCandidates.isEmpty()) {
			return randomFrom(regularCandidates);
		}

		return createBanner(TokenResourceKeys.ACCEPT_NOMINATION_ACTION_BANNER_DEFAULT, MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING, resolveTaskKey(defaultPendingTarget), AcceptNominationTaskMode.COMPLETE);
	}

	private AcceptNominationActionBanner randomFrom(List<AcceptNominationActionBanner> candidates) {
		if (candidates == null || candidates.isEmpty()) {
			return createBanner(TokenResourceKeys.ACCEPT_NOMINATION_ACTION_BANNER_DEFAULT, MESSAGE_VARIANTS, BootstrapCssClasses.BG_WARNING, null, AcceptNominationTaskMode.VIEW);
		}
		int index = nextIndex(candidates.size());
		return candidates.get(index);
	}

	private AcceptNominationActionBanner createBanner(String messagePrefix, int variants, String toneCssClass, ElectionTaskKey taskKey, AcceptNominationTaskMode mode,
			Object... params) {
		return new AcceptNominationActionBanner(pickMessageVariant(messagePrefix, variants), toneCssClass, taskKey, mode, params);
	}

	private String pickMessageVariant(String messagePrefix, int variants) {
		if (messagePrefix == null || messagePrefix.isEmpty() || variants <= 1) {
			return messagePrefix;
		}
		int variant = nextIndex(variants) + 1;
		return messagePrefix + "." + variant;
	}

	private int nextIndex(int bound) {
		if (bound <= 0) {
			return 0;
		}
		return Math.floorMod(randomIndex.applyAsInt(bound), bound);
	}

	private DeadlineInsight analyzeDeadlines(List<AcceptNominationTaskItem> tasks, Date now) {
		DeadlineInsight insight = new DeadlineInsight();
		if (tasks == null || tasks.isEmpty() || now == null) {
			return insight;
		}

		List<AcceptNominationTaskItem> deadlineTasks = new ArrayList<>();
		for (AcceptNominationTaskItem task : tasks) {
			if (!isDependencyActionablePendingTask(task) || task.getDeadlineDate() == null) {
				continue;
			}
			deadlineTasks.add(task);
		}
		if (deadlineTasks.isEmpty()) {
			return insight;
		}

		Collections.sort(deadlineTasks, Comparator.comparing(AcceptNominationTaskItem::getDeadlineDate));
		for (AcceptNominationTaskItem task : deadlineTasks) {
			if (task.getDeadlineDate().before(now)) {
				insight.overdueCount++;
				if (insight.overdueTask == null) {
					insight.overdueTask = task;
				}
			}
		}

		if (insight.overdueCount == 0) {
			AcceptNominationTaskItem nextTask = deadlineTasks.get(0);
			insight.nearestUpcomingTask = nextTask;
			insight.nearestUpcomingHours = calculateHoursUntil(nextTask.getDeadlineDate(), now);
		}

		return insight;
	}

	private long calculateHoursUntil(Date deadline, Date now) {
		if (deadline == null || now == null) {
			return 0;
		}
		long diff = deadline.getTime() - now.getTime();
		if (diff <= 0) {
			return 0;
		}
		long hour = 60L * 60L * 1000L;
		return Math.max(1L, (diff + hour - 1L) / hour);
	}

	private long calculateDaysUntil(Date deadline, Date now) {
		if (deadline == null || now == null) {
			return 0;
		}
		long diff = deadline.getTime() - now.getTime();
		if (diff <= 0) {
			return 0;
		}
		long day = 24L * 60L * 60L * 1000L;
		return Math.max(1L, (diff + day - 1L) / day);
	}

	private SupportInsight summarizeSupports(Nomination nomination) {
		SupportInsight insight = new SupportInsight();
		if (nomination == null || nomination.getSupports() == null) {
			return insight;
		}

		for (SupportNomination supportNomination : nomination.getSupports()) {
			if (supportNomination == null) {
				continue;
			}

			SupportStatus status = supportNomination.getSupportStatus() != null ? supportNomination.getSupportStatus() : SupportStatus.PROPOSED;
			boolean isOrganizationSupport = supportNomination.getSupportingOrganization() != null;
			if (isOrganizationSupport) {
				if (status == SupportStatus.PROPOSED) {
					insight.orgProposedCount++;
				}
				if (isActiveSupportStatus(status)) {
					insight.orgActiveCount++;
				}
				continue;
			}

			if (status == SupportStatus.PROPOSED) {
				insight.userProposedCount++;
			}
			if (isActiveSupportStatus(status)) {
				insight.userActiveCount++;
			}
		}

		return insight;
	}

	private boolean isActiveSupportStatus(SupportStatus status) {
		return status == SupportStatus.PROPOSED || status == SupportStatus.ACCEPTED || status == SupportStatus.APPROVED;
	}

	private int resolveRequiredUserSupports(ElectionTaskKey taskKey) {
		if (taskKey == ElectionTaskKey.USER_SUPPORTS_2) {
			return 2;
		}
		if (taskKey == ElectionTaskKey.USER_SUPPORTS_5) {
			return 5;
		}
		return 0;
	}

	private CandidateCampusCourseStatus resolveCampusStatus(Candidate candidate) {
		if (candidate == null || candidate.getCampusCourseStatus() == null) {
			return CandidateCampusCourseStatus.PENDING;
		}
		return candidate.getCampusCourseStatus();
	}

	private boolean isCampusTrainingEnabled(Candidate candidate) {
		return CampusClient.isCampusTrainingEnabled(candidate != null ? candidate.getElection() : null);
	}

	private CandidateEvaluationStatus resolveEvaluationStatus(Candidate candidate) {
		if (candidate == null || candidate.getEvaluationStatus() == null) {
			return CandidateEvaluationStatus.PENDING;
		}
		return candidate.getEvaluationStatus();
	}

	private String resolveDefaultRecipient(AcceptNominationTaskResolution resolution) {
		if (resolution == null || resolution.getNomination() == null || resolution.getNomination().getElection() == null) {
			return DEFAULT_ELECTION_RECIPIENT_TOKEN;
		}
		String defaultRecipient = resolution.getNomination().getElection().getDefaultRecipient();
		if (defaultRecipient != null && !defaultRecipient.trim().isEmpty()) {
			return defaultRecipient.trim();
		}
		String defaultSender = resolution.getNomination().getElection().getDefaultSender();
		if (defaultSender != null && !defaultSender.trim().isEmpty()) {
			return defaultSender.trim();
		}
		return DEFAULT_ELECTION_RECIPIENT_TOKEN;
	}

	private CandidateStatus resolveCandidateStatus(Candidate candidate) {
		if (candidate == null) {
			return null;
		}
		return candidate.getStatus();
	}

	private AcceptNominationTaskItem findTask(List<AcceptNominationTaskItem> tasks, ElectionTaskKey taskKey) {
		if (tasks == null || taskKey == null) {
			return null;
		}
		for (AcceptNominationTaskItem item : tasks) {
			if (item != null && item.getTaskKey() == taskKey) {
				return item;
			}
		}
		return null;
	}

	private AcceptNominationTaskItem findPendingUserSupportsTask(List<AcceptNominationTaskItem> tasks, Date now) {
		AcceptNominationTaskItem task2 = findTask(tasks, ElectionTaskKey.USER_SUPPORTS_2);
		if (isAvailablePendingTask(task2, now)) {
			return task2;
		}
		AcceptNominationTaskItem task5 = findTask(tasks, ElectionTaskKey.USER_SUPPORTS_5);
		if (isAvailablePendingTask(task5, now)) {
			return task5;
		}
		return null;
	}

	private boolean hasAvailablePendingNonSupportTask(List<AcceptNominationTaskItem> tasks, Date now) {
		if (tasks == null || tasks.isEmpty()) {
			return false;
		}
		for (AcceptNominationTaskItem task : tasks) {
			if (!isAvailablePendingTask(task, now) || isSupportTask(task.getTaskKey())) {
				continue;
			}
			return true;
		}
		return false;
	}

	private boolean isSupportTask(ElectionTaskKey taskKey) {
		return taskKey == ElectionTaskKey.ORG_SUPPORTS || taskKey == ElectionTaskKey.USER_SUPPORTS_2 || taskKey == ElectionTaskKey.USER_SUPPORTS_5;
	}

	private AcceptNominationTaskItem findRandomTaskByStatus(List<AcceptNominationTaskItem> tasks, CandidateElectionTaskStatus status, boolean onlyActionable, Date now) {
		if (tasks == null || status == null) {
			return null;
		}
		List<AcceptNominationTaskItem> matches = new ArrayList<>();
		for (AcceptNominationTaskItem task : tasks) {
			if (task == null || task.getStatus() != status || isResolvedTask(task)) {
				continue;
			}
			if (onlyActionable && !isAvailablePendingTask(task, now)) {
				continue;
			}
			matches.add(task);
		}
		if (!matches.isEmpty()) {
			return randomTaskFrom(matches);
		}
		return null;
	}

	private AcceptNominationTaskItem findRandomPendingTask(List<AcceptNominationTaskItem> tasks, boolean onlyActionable, Date now) {
		if (tasks == null) {
			return null;
		}
		List<AcceptNominationTaskItem> matches = new ArrayList<>();
		for (AcceptNominationTaskItem task : tasks) {
			if (!isPendingTask(task)) {
				continue;
			}
			if (onlyActionable && !isAvailablePendingTask(task, now)) {
				continue;
			}
			matches.add(task);
		}
		if (!matches.isEmpty()) {
			return randomTaskFrom(matches);
		}
		return null;
	}

	private AcceptNominationTaskItem randomTaskFrom(List<AcceptNominationTaskItem> tasks) {
		if (tasks == null || tasks.isEmpty()) {
			return null;
		}
		return tasks.get(nextIndex(tasks.size()));
	}

	private AcceptNominationTaskItem findRandomUpcomingDeadlineTask(List<AcceptNominationTaskItem> tasks, Date now, long minimumHoursUntilDeadline) {
		if (tasks == null || tasks.isEmpty() || now == null) {
			return null;
		}
		List<AcceptNominationTaskItem> matches = new ArrayList<>();
		for (AcceptNominationTaskItem task : tasks) {
			if (task == null || task.getDeadlineDate() == null) {
				continue;
			}
			long hoursUntilDeadline = calculateHoursUntil(task.getDeadlineDate(), now);
			if (hoursUntilDeadline > minimumHoursUntilDeadline) {
				matches.add(task);
			}
		}
		return randomTaskFrom(matches);
	}

	private List<AcceptNominationTaskItem> findAvailablePendingTasks(List<AcceptNominationTaskItem> tasks, Date now) {
		List<AcceptNominationTaskItem> matches = new ArrayList<>();
		if (tasks == null || tasks.isEmpty()) {
			return matches;
		}
		for (AcceptNominationTaskItem task : tasks) {
			if (isAvailablePendingTask(task, now)) {
				matches.add(task);
			}
		}
		return matches;
	}

	private AcceptNominationTaskItem resolveOnlyTaskBlockingOthers(List<AcceptNominationTaskItem> availablePendingTasks, int blockedPendingTaskCount) {
		if (availablePendingTasks == null || availablePendingTasks.size() != 1 || blockedPendingTaskCount <= 0) {
			return null;
		}
		return availablePendingTasks.get(0);
	}

	private int countPendingTasks(List<AcceptNominationTaskItem> tasks, boolean onlyActionable, Date now) {
		if (tasks == null) {
			return 0;
		}
		int count = 0;
		for (AcceptNominationTaskItem task : tasks) {
			if (!isPendingTask(task)) {
				continue;
			}
			if (onlyActionable && !isAvailablePendingTask(task, now)) {
				continue;
			}
			count++;
		}
		return count;
	}

	private int countBlockedPendingTasks(List<AcceptNominationTaskItem> tasks) {
		if (tasks == null) {
			return 0;
		}
		int count = 0;
		for (AcceptNominationTaskItem task : tasks) {
			if (task != null && task.isBlocked() && isPendingTask(task)) {
				count++;
			}
		}
		return count;
	}

	private ElectionTaskKey resolveTaskKey(AcceptNominationTaskItem taskItem) {
		return taskItem != null ? taskItem.getTaskKey() : null;
	}

	private boolean isPendingTask(AcceptNominationTaskItem taskItem) {
		return taskItem != null && !isResolvedTask(taskItem);
	}

	private boolean isDependencyActionablePendingTask(AcceptNominationTaskItem taskItem) {
		return taskItem != null && !taskItem.isBlocked() && isPendingTask(taskItem);
	}

	private boolean isAvailablePendingTask(AcceptNominationTaskItem taskItem, Date now) {
		if (!isDependencyActionablePendingTask(taskItem)) {
			return false;
		}
		if (taskItem.getDeadlineDate() == null || now == null) {
			return true;
		}
		return !taskItem.getDeadlineDate().before(now);
	}

	private boolean isResolvedTask(AcceptNominationTaskItem taskItem) {
		if (taskItem == null) {
			return false;
		}
		CandidateElectionTaskStatus status = taskItem.getStatus();
		return status == CandidateElectionTaskStatus.COMPLETED || status == CandidateElectionTaskStatus.OMITTED;
	}

	private static final class DeadlineInsight {
		private int overdueCount;
		private AcceptNominationTaskItem overdueTask;
		private long nearestUpcomingHours;
		private AcceptNominationTaskItem nearestUpcomingTask;
	}

	private static final class SupportInsight {
		private int orgProposedCount;
		private int orgActiveCount;
		private int userProposedCount;
		private int userActiveCount;
	}

	public static final class AcceptNominationActionBanner implements Serializable {

		private static final long serialVersionUID = 1L;

		private final String messageKey;
		private final String toneCssClass;
		private final ElectionTaskKey targetTaskKey;
		private final AcceptNominationTaskMode targetMode;
		private transient final Object[] messageParams;

		public AcceptNominationActionBanner(String messageKey, String toneCssClass, ElectionTaskKey targetTaskKey, AcceptNominationTaskMode targetMode, Object... messageParams) {
			this.messageKey = messageKey;
			this.toneCssClass = toneCssClass;
			this.targetTaskKey = targetTaskKey;
			this.targetMode = targetMode;
			this.messageParams = messageParams;
		}

		public String getMessageKey() {
			return messageKey;
		}

		public String getToneCssClass() {
			return toneCssClass;
		}

		public ElectionTaskKey getTargetTaskKey() {
			return targetTaskKey;
		}

		public AcceptNominationTaskMode getTargetMode() {
			return targetMode;
		}

		public Object[] getMessageParams() {
			return messageParams;
		}
	}
}
