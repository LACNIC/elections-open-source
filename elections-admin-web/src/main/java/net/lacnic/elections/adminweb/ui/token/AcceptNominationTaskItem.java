package net.lacnic.elections.adminweb.ui.token;

import java.io.Serializable;
import java.util.Date;

import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.ElectionTaskKey;

public class AcceptNominationTaskItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private final CandidateElectionTaskProgress taskProgress;
	private final ElectionTaskKey taskKey;
	private final String taskName;
	private final CandidateElectionTaskStatus status;
	private final boolean blocked;
	private final boolean completed;
	private final Date startDate;
	private final Date endDate;
	private final Date deadlineDate;
	private final String statusLabelKey;
	private final String statusBadgeClass;
	private final String progressText;

	public AcceptNominationTaskItem(CandidateElectionTaskProgress taskProgress, ElectionTaskKey taskKey, String taskName, CandidateElectionTaskStatus status, boolean blocked, boolean completed,
			Date startDate, Date endDate, Date deadlineDate, String statusLabelKey, String statusBadgeClass, String progressText) {
		this.taskProgress = taskProgress;
		this.taskKey = taskKey;
		this.taskName = taskName;
		this.status = status;
		this.blocked = blocked;
		this.completed = completed;
		this.startDate = startDate;
		this.endDate = endDate;
		this.deadlineDate = deadlineDate;
		this.statusLabelKey = statusLabelKey;
		this.statusBadgeClass = statusBadgeClass;
		this.progressText = progressText;
	}

	public CandidateElectionTaskProgress getTaskProgress() {
		return taskProgress;
	}

	public ElectionTaskKey getTaskKey() {
		return taskKey;
	}

	public String getTaskName() {
		return taskName;
	}

	public CandidateElectionTaskStatus getStatus() {
		return status;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public boolean isCompleted() {
		return completed;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public Date getDeadlineDate() {
		return deadlineDate;
	}

	public String getStatusLabelKey() {
		return statusLabelKey;
	}

	public String getStatusBadgeClass() {
		return statusBadgeClass;
	}

	public String getProgressText() {
		return progressText;
	}
}
