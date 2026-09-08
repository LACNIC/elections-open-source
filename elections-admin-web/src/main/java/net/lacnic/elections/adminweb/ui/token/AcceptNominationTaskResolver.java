package net.lacnic.elections.adminweb.ui.token;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.wicket.util.BootstrapCssClasses;
import net.lacnic.elections.campus.CampusClient;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.ElectionPresetConfigurations;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.TaskDependencyLevel;

public class AcceptNominationTaskResolver {

	public static final String TASK_PARAM = "task";
	public static final String MODE_PARAM = "mode";

	private final String token;

	public AcceptNominationTaskResolver(String token) {
		this.token = token;
	}

	public AcceptNominationTaskResolution resolve(String requestedTaskValue, String requestedModeValue) {
		Nomination nomination = AppContext.getInstance().getPreNominationBeanRemote().verifyAcceptNominationAccess(token);
		if (nomination == null || nomination.getCandidate() == null || nomination.getCandidate().getTaskProgress() == null || nomination.getCandidate().getTaskProgress().isEmpty()) {
			return new AcceptNominationTaskResolution(nomination, nomination != null ? nomination.getCandidate() : null, new ArrayList<>(), null, AcceptNominationTaskMode.VIEW,
					AcceptNominationTaskMode.VIEW, false, false, false, null);
		}

		Candidate candidate = nomination.getCandidate();
		List<CandidateElectionTaskProgress> orderedTaskProgressList = filterConfiguredTaskProgress(candidate);
		orderedTaskProgressList.sort(Comparator
				.comparingInt(this::resolveDisplayOrderSortValue)
				.thenComparingLong(this::resolveTaskIdSortValue));

		boolean nominationWindowClosed = isNominationWindowClosed(orderedTaskProgressList);
		List<AcceptNominationTaskItem> tasks = buildTaskItems(orderedTaskProgressList);

		ElectionTaskKey selectedTaskKey = resolveSelectedTaskKey(requestedTaskValue, tasks);
		AcceptNominationTaskItem selectedTask = findTaskByKey(tasks, selectedTaskKey);

		if (selectedTask == null) {
			return new AcceptNominationTaskResolution(nomination, candidate, tasks, null, AcceptNominationTaskMode.VIEW, AcceptNominationTaskMode.VIEW, false,
					nominationWindowClosed, false, null);
		}

		boolean forceViewByCandidateStatus = isReadOnlyByCandidateStatus(candidate);
		boolean forceViewByNominationWindow = nominationWindowClosed;
		AcceptNominationTaskMode defaultMode = (forceViewByCandidateStatus || forceViewByNominationWindow)
				? AcceptNominationTaskMode.VIEW
				: (selectedTask.isCompleted() ? AcceptNominationTaskMode.VIEW : AcceptNominationTaskMode.COMPLETE);
		AcceptNominationTaskMode requestedMode = AcceptNominationTaskMode.fromParameter(requestedModeValue, defaultMode);
		AcceptNominationTaskMode normalizedMode = forceViewByCandidateStatus ? AcceptNominationTaskMode.VIEW : normalizeRequestedMode(selectedTask, requestedMode);

		boolean deadlinePassed = isTaskDeadlinePassed(selectedTask);
		boolean profileEditLocked = isProfileEditLocked(candidate, selectedTask);
		String restrictionMessageKey = forceViewByCandidateStatus
				? null
				: resolveRestrictionMessageKey(selectedTask, requestedMode, normalizedMode, deadlinePassed, nominationWindowClosed, profileEditLocked);
		AcceptNominationTaskMode effectiveMode = forceViewByCandidateStatus ? AcceptNominationTaskMode.VIEW : (restrictionMessageKey != null ? AcceptNominationTaskMode.VIEW : normalizedMode);

		return new AcceptNominationTaskResolution(nomination, candidate, tasks, selectedTask, requestedMode, effectiveMode, deadlinePassed, nominationWindowClosed,
				profileEditLocked, restrictionMessageKey);
	}

	/**
	 * Candidate progress can outlive an election task that was removed or disabled.
	 * The nomination task page must reflect the election configuration, not stale
	 * progress rows, especially for Campus tasks.
	 */
	private List<CandidateElectionTaskProgress> filterConfiguredTaskProgress(Candidate candidate) {
		List<CandidateElectionTaskProgress> filtered = new ArrayList<>();
		if (candidate == null || candidate.getTaskProgress() == null || candidate.getElection() == null) {
			return filtered;
		}
		java.util.Set<ElectionTaskKey> configuredKeys = new java.util.HashSet<>();
		try {
			for (ElectionTask task : AppContext.getInstance().getManagerBeanRemote().getElectionTasks(candidate.getElection().getElectionId())) {
				if (task != null && task.getTaskKey() != null) {
					configuredKeys.add(task.getTaskKey());
				}
			}
		} catch (Exception e) {
			// Preserve the existing view if configuration cannot be read.
			return new ArrayList<>(candidate.getTaskProgress());
		}
		boolean campusConfigured = CampusClient.isCampusIntegrationEnabled();
		for (CandidateElectionTaskProgress progress : candidate.getTaskProgress()) {
			ElectionTask task = progress != null ? progress.getElectionTask() : null;
			ElectionTaskKey key = task != null ? task.getTaskKey() : null;
			if (key != null && configuredKeys.contains(key)
					&& ElectionPresetConfigurations.isPresetTaskEnabled(key, campusConfigured)) {
				filtered.add(progress);
			}
		}
		return filtered;
	}

	private List<AcceptNominationTaskItem> buildTaskItems(List<CandidateElectionTaskProgress> taskProgressList) {
		List<AcceptNominationTaskItem> rows = new ArrayList<>();
		for (CandidateElectionTaskProgress taskProgress : taskProgressList) {
			CandidateElectionTaskStatus status = taskProgress.getStatus();
			ElectionTask electionTask = taskProgress.getElectionTask();
			ElectionTaskKey taskKey = electionTask != null ? electionTask.getTaskKey() : null;

			boolean completed = status == CandidateElectionTaskStatus.COMPLETED;
			boolean blocked = isBlockedByDependencyLevelRule(taskProgress, taskProgressList);

			String statusLabelKey = resolveStatusLabelKey(status, blocked);
			String statusBadgeClass = resolveStatusBadgeClass(status, blocked);
			String progressText = resolveProgress(status, blocked);
			Date startDate = taskProgress.getStartDate();
			Date endDate = taskProgress.getEndDate();
			Date deadlineDate = electionTask != null && electionTask.getElectionCalendar() != null ? electionTask.getElectionCalendar().getEndDate() : null;
			String taskName = taskKey != null ? humanizeTaskKey(taskKey.name()) : "-";

			rows.add(new AcceptNominationTaskItem(taskProgress, taskKey, taskName, status, blocked, completed, startDate, endDate, deadlineDate, statusLabelKey, statusBadgeClass, progressText));
		}
		return rows;
	}

	private ElectionTaskKey resolveSelectedTaskKey(String requestedTaskValue, List<AcceptNominationTaskItem> tasks) {
		ElectionTaskKey requestedTaskKey = parseTaskKey(requestedTaskValue);
		if (requestedTaskKey != null && findTaskByKey(tasks, requestedTaskKey) != null) {
			return requestedTaskKey;
		}

		AcceptNominationTaskItem profileTask = findTaskByKey(tasks, ElectionTaskKey.PROFILE);
		if (profileTask != null) {
			return ElectionTaskKey.PROFILE;
		}

		return tasks.isEmpty() ? null : tasks.get(0).getTaskKey();
	}

	private AcceptNominationTaskItem findTaskByKey(List<AcceptNominationTaskItem> tasks, ElectionTaskKey taskKey) {
		if (taskKey == null) {
			return null;
		}
		for (AcceptNominationTaskItem item : tasks) {
			if (item.getTaskKey() == taskKey) {
				return item;
			}
		}
		return null;
	}

	private ElectionTaskKey parseTaskKey(String taskValue) {
		if (taskValue == null || taskValue.isEmpty()) {
			return null;
		}
		try {
			return ElectionTaskKey.valueOf(taskValue.toUpperCase(Locale.ROOT));
		} catch (Exception e) {
			return null;
		}
	}

	private AcceptNominationTaskMode normalizeRequestedMode(AcceptNominationTaskItem selectedTask, AcceptNominationTaskMode requestedMode) {
		if (selectedTask.isCompleted()) {
			if (requestedMode == AcceptNominationTaskMode.COMPLETE) {
				return AcceptNominationTaskMode.VIEW;
			}
			return requestedMode;
		}

		return AcceptNominationTaskMode.COMPLETE;
	}

	private String resolveRestrictionMessageKey(AcceptNominationTaskItem selectedTask, AcceptNominationTaskMode requestedMode, AcceptNominationTaskMode mode, boolean deadlinePassed,
			boolean nominationWindowClosed, boolean profileEditLocked) {
		if (requestedMode == AcceptNominationTaskMode.COMPLETE && selectedTask.isCompleted()) {
			return "acceptNominationTaskRestrictionAlreadyCompleted";
		}

		if (nominationWindowClosed && requestedMode != AcceptNominationTaskMode.VIEW) {
			return "acceptNominationTaskRestrictionNominationWindowClosed";
		}

		if (mode == AcceptNominationTaskMode.VIEW) {
			return null;
		}

		if (deadlinePassed) {
			return "acceptNominationTaskRestrictionDeadlinePassed";
		}

		if (mode == AcceptNominationTaskMode.COMPLETE && selectedTask.isBlocked()) {
			return "acceptNominationTaskRestrictionDependencies";
		}

		return null;
	}

	private boolean isTaskDeadlinePassed(AcceptNominationTaskItem selectedTask) {
		if (selectedTask.getDeadlineDate() == null || selectedTask.isCompleted()) {
			return false;
		}
		return selectedTask.getDeadlineDate().before(new Date());
	}

	private boolean isProfileEditLocked(Candidate candidate, AcceptNominationTaskItem selectedTask) {
		if (candidate == null || selectedTask == null || selectedTask.getTaskKey() != ElectionTaskKey.PROFILE) {
			return false;
		}

		return !isBlank(candidate.getBioEnglish()) && !isBlank(candidate.getBioPortuguese());
	}

	private boolean isNominationWindowClosed(List<CandidateElectionTaskProgress> taskProgressList) {
		Date latestDeadline = null;
		for (CandidateElectionTaskProgress taskProgress : taskProgressList) {
			if (taskProgress == null || taskProgress.getElectionTask() == null || taskProgress.getElectionTask().getElectionCalendar() == null) {
				continue;
			}
			Date deadline = taskProgress.getElectionTask().getElectionCalendar().getEndDate();
			if (deadline == null) {
				continue;
			}
			if (latestDeadline == null || deadline.after(latestDeadline)) {
				latestDeadline = deadline;
			}
		}
		return latestDeadline != null && latestDeadline.before(new Date());
	}

	private boolean isReadOnlyByCandidateStatus(Candidate candidate) {
		if (candidate == null || candidate.getStatus() == null) {
			return false;
		}
		CandidateStatus status = candidate.getStatus();
		return status == CandidateStatus.COMPLETE
				|| status == CandidateStatus.REJECTED
				|| status == CandidateStatus.CONFIRMED_AND_PUBLISHED;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private boolean isBlockedByDependencyLevelRule(CandidateElectionTaskProgress taskProgress, List<CandidateElectionTaskProgress> taskProgressList) {
		ElectionTask electionTask = taskProgress.getElectionTask();
		if (electionTask == null) {
			return false;
		}
		TaskDependencyLevel dependencyLevel = resolveDependencyLevel(electionTask);
		if (dependencyLevel.isFirstLevel()) {
			return false;
		}
		if (taskProgress.getStatus() == CandidateElectionTaskStatus.COMPLETED) {
			return false;
		}
		int previousLevelIndex = dependencyLevel.getIndex() - 1;
		return hasPendingTasksInLevel(taskProgressList, previousLevelIndex);
	}

	private boolean hasPendingTasksInLevel(List<CandidateElectionTaskProgress> taskProgressList, int levelIndex) {
		TaskDependencyLevel targetLevel = TaskDependencyLevel.fromIndex(levelIndex);
		for (CandidateElectionTaskProgress taskProgress : taskProgressList) {
			ElectionTask rowTask = taskProgress.getElectionTask();
			if (rowTask == null) {
				continue;
			}
			TaskDependencyLevel rowLevel = resolveDependencyLevel(rowTask);
			if (rowLevel == targetLevel && taskProgress.getStatus() != CandidateElectionTaskStatus.COMPLETED) {
				return true;
			}
		}
		return false;
	}

	private TaskDependencyLevel resolveDependencyLevel(ElectionTask electionTask) {
		if (electionTask == null || electionTask.getDependencyLevel() == null) {
			return TaskDependencyLevel.LEVEL_1;
		}
		return electionTask.getDependencyLevel();
	}

	private String resolveStatusLabelKey(CandidateElectionTaskStatus status, boolean blocked) {
		if (blocked) {
			return TokenResourceKeys.ACCEPT_NOMINATION_TASKS_STATUS_PENDING;
		}
		if (status == null) {
			return TokenResourceKeys.ACCEPT_NOMINATION_TASKS_STATUS_PENDING;
		}
		switch (status) {
		case COMPLETED:
			return "acceptNominationTasksStatusCompleted";
		case STARTED:
			return "acceptNominationTasksStatusInProgress";
		case OMITTED:
			return "acceptNominationTasksStatusOmitted";
		case NOT_STARTED:
		default:
			return TokenResourceKeys.ACCEPT_NOMINATION_TASKS_STATUS_PENDING;
		}
	}

	private String resolveStatusBadgeClass(CandidateElectionTaskStatus status, boolean blocked) {
		if (blocked) {
			return BootstrapCssClasses.BADGE_SOFT_DANGER;
		}
		if (status == null) {
			return BootstrapCssClasses.BADGE_SOFT_DANGER;
		}
		switch (status) {
		case COMPLETED:
			return "badge-soft-success";
		case STARTED:
			return "badge-soft-warning";
		case OMITTED:
			return "badge-soft-secondary";
		case NOT_STARTED:
		default:
			return BootstrapCssClasses.BADGE_SOFT_DANGER;
		}
	}

	private String resolveProgress(CandidateElectionTaskStatus status, boolean blocked) {
		if (blocked || status == null) {
			return "0%";
		}
		switch (status) {
		case COMPLETED:
			return "100%";
		case STARTED:
			return "50%";
		case OMITTED:
			return "100%";
		case NOT_STARTED:
		default:
			return "0%";
		}
	}

	private String humanizeTaskKey(String value) {
		String normalized = value.toLowerCase(Locale.ROOT).replace('_', ' ');
		return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
	}

	private int resolveDisplayOrderSortValue(CandidateElectionTaskProgress taskProgress) {
		if (taskProgress == null || taskProgress.getElectionTask() == null || taskProgress.getElectionTask().getDisplayOrder() == null) {
			return Integer.MAX_VALUE;
		}
		return taskProgress.getElectionTask().getDisplayOrder();
	}

	private long resolveTaskIdSortValue(CandidateElectionTaskProgress taskProgress) {
		if (taskProgress == null || taskProgress.getElectionTask() == null) {
			return Long.MAX_VALUE;
		}
		return taskProgress.getElectionTask().getId();
	}

}
