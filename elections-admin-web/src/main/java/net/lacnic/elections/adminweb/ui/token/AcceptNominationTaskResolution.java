package net.lacnic.elections.adminweb.ui.token;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.Nomination;

public class AcceptNominationTaskResolution implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Nomination nomination;
	private final Candidate candidate;
	private final List<AcceptNominationTaskItem> tasks;
	private final AcceptNominationTaskItem selectedTask;
	private final AcceptNominationTaskMode requestedMode;
	private final AcceptNominationTaskMode effectiveMode;
	private final boolean deadlinePassed;
	private final boolean nominationWindowClosed;
	private final boolean profileEditLocked;
	private final String restrictionMessageKey;

	public AcceptNominationTaskResolution(Nomination nomination, Candidate candidate, List<AcceptNominationTaskItem> tasks, AcceptNominationTaskItem selectedTask,
			AcceptNominationTaskMode requestedMode, AcceptNominationTaskMode effectiveMode, boolean deadlinePassed, boolean nominationWindowClosed, boolean profileEditLocked,
			String restrictionMessageKey) {
		this.nomination = nomination;
		this.candidate = candidate;
		this.tasks = tasks == null ? Collections.emptyList() : tasks;
		this.selectedTask = selectedTask;
		this.requestedMode = requestedMode;
		this.effectiveMode = effectiveMode;
		this.deadlinePassed = deadlinePassed;
		this.nominationWindowClosed = nominationWindowClosed;
		this.profileEditLocked = profileEditLocked;
		this.restrictionMessageKey = restrictionMessageKey;
	}

	public boolean isValid() {
		return nomination != null && candidate != null && selectedTask != null;
	}

	public Nomination getNomination() {
		return nomination;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public List<AcceptNominationTaskItem> getTasks() {
		return tasks;
	}

	public AcceptNominationTaskItem getSelectedTask() {
		return selectedTask;
	}

	public AcceptNominationTaskMode getRequestedMode() {
		return requestedMode;
	}

	public AcceptNominationTaskMode getEffectiveMode() {
		return effectiveMode;
	}

	public boolean isDeadlinePassed() {
		return deadlinePassed;
	}

	public boolean isNominationWindowClosed() {
		return nominationWindowClosed;
	}

	public boolean isProfileEditLocked() {
		return profileEditLocked;
	}

	public boolean isReadOnly() {
		return effectiveMode == AcceptNominationTaskMode.VIEW;
	}

	public String getRestrictionMessageKey() {
		return restrictionMessageKey;
	}

	public boolean hasModeRestriction() {
		return restrictionMessageKey != null && !restrictionMessageKey.isEmpty();
	}
}
