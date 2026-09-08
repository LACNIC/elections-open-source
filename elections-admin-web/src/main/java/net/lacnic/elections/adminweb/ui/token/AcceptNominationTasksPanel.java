package net.lacnic.elections.adminweb.ui.token;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

import net.lacnic.elections.adminweb.app.SecurityUtils;

import net.lacnic.elections.adminweb.wicket.util.BootstrapCssClasses;
import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class AcceptNominationTasksPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final String token;

	public AcceptNominationTasksPanel(String id, String token) {
		super(id);
		this.token = token;

		TasksViewData viewData = loadTasksViewData();
		setVisible(viewData.visible);

		WebMarkupContainer tasksTableContainer = new WebMarkupContainer("tasksTableContainer");
		add(tasksTableContainer);

		tasksTableContainer.add(new ListView<TaskRow>("tasksRows", viewData.rows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<TaskRow> item) {
				TaskRow row = item.getModelObject();
				if (row.locked) {
					item.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "opacity-50"));
				}
				item.add(new Label("taskName", row.taskName));

				Label statusLabel = new Label("taskStatus", new ResourceModel(row.statusLabelKey));
				statusLabel.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge badge-label " + row.statusBadgeClass));
				item.add(statusLabel);
				WebMarkupContainer taskLockedBadge = new WebMarkupContainer("taskLockedBadge");
				taskLockedBadge.setVisible(row.locked);
				String lockPopoverKey = row.lockedByPeriod ? "acceptNominationTasksLockPeriodEndedPopover" : "acceptNominationTasksLockedPopover";
				String lockBadgeClass = row.lockedByPeriod ? "badge badge-label badge-soft-danger ms-1" : "badge badge-label badge-soft-secondary ms-1";
				taskLockedBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  lockBadgeClass));
				taskLockedBadge.add(AttributeModifier.replace("data-bs-content", new ResourceModel(lockPopoverKey)));
				item.add(taskLockedBadge);

				item.add(new Label("taskStartDate", row.startDate));
				item.add(new Label("taskEndDate", row.endDate));
				item.add(new Label("taskDeadlineDate", row.deadlineDate));
				item.add(new Label("taskProgress", row.progress));

				WebMarkupContainer taskActions = new WebMarkupContainer("taskActions");
				if (row.locked) {
					taskActions.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "pe-none"));
				}
				WebMarkupContainer actionView = new WebMarkupContainer("actionView");
				actionView.setVisible(row.completed);
				taskActions.add(actionView);

				WebMarkupContainer actionEdit = new WebMarkupContainer("actionEdit");
				actionEdit.setVisible(row.completed);
				taskActions.add(actionEdit);

				WebMarkupContainer actionComplete = new WebMarkupContainer("actionComplete");
				actionComplete.setVisible(!row.completed);
				taskActions.add(actionComplete);

				item.add(taskActions);
			}
		});
	}

	private TasksViewData loadTasksViewData() {
		AcceptNominationTaskResolution resolution = new AcceptNominationTaskResolver(token).resolve(null, null);
		if (resolution == null || !resolution.isValid()) {
			return TasksViewData.hidden();
		}

		List<TaskRow> rows = new ArrayList<>();
		for (AcceptNominationTaskItem task : resolution.getTasks()) {
			rows.add(toTaskRow(task, resolution.isNominationWindowClosed()));
		}
		return TasksViewData.visible(rows);
	}

	private TaskRow toTaskRow(AcceptNominationTaskItem task, boolean nominationWindowClosed) {
		String taskName = task != null ? task.getTaskName() : "-";
		String statusLabelKey = task != null ? task.getStatusLabelKey() : TokenResourceKeys.ACCEPT_NOMINATION_TASKS_STATUS_PENDING;
		String statusBadgeClass = task != null ? task.getStatusBadgeClass() : BootstrapCssClasses.BADGE_SOFT_DANGER;
		String startDate = formatDateTime(task != null ? task.getStartDate() : null);
		String deadlineDate = formatDate(task != null ? task.getDeadlineDate() : null);
		String endDate = formatDate(task != null ? task.getEndDate() : null);
		String progress = task != null ? task.getProgressText() : "0%";
		boolean completed = task != null && task.isCompleted();
		boolean lockedByDependency = task != null && task.isBlocked();
		boolean lockedByPeriod = nominationWindowClosed;
		boolean locked = lockedByDependency || lockedByPeriod;
		return new TaskRow(taskName, statusLabelKey, statusBadgeClass, startDate, deadlineDate, endDate, progress, locked, completed, lockedByPeriod);
	}

	private String formatDate(Date date) {
		if (date == null) {
			return "-";
		}
		DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, SecurityUtils.getLocale());
		return formatter.format(date);
	}

	private String formatDateTime(Date date) {
		if (date == null) {
			return "-";
		}
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, SecurityUtils.getLocale());
		return formatter.format(date);
	}

	private static class TasksViewData {
		private final boolean visible;
		private final List<TaskRow> rows;

		private TasksViewData(boolean visible, List<TaskRow> rows) {
			this.visible = visible;
			this.rows = rows;
		}

		private static TasksViewData visible(List<TaskRow> rows) {
			return new TasksViewData(true, rows);
		}

		private static TasksViewData hidden() {
			return new TasksViewData(false, new ArrayList<>());
		}
	}

	private static class TaskRow {
		private final String taskName;
		private final String statusLabelKey;
		private final String statusBadgeClass;
		private final String startDate;
		private final String deadlineDate;
		private final String endDate;
		private final String progress;
		private final boolean locked;
		private final boolean completed;
		private final boolean lockedByPeriod;

		private TaskRow(String taskName, String statusLabelKey, String statusBadgeClass, String startDate, String deadlineDate, String endDate, String progress, boolean locked,
				boolean completed, boolean lockedByPeriod) {
			this.taskName = taskName;
			this.statusLabelKey = statusLabelKey;
			this.statusBadgeClass = statusBadgeClass;
			this.startDate = startDate;
			this.deadlineDate = deadlineDate;
			this.endDate = endDate;
			this.progress = progress;
			this.locked = locked;
			this.completed = completed;
			this.lockedByPeriod = lockedByPeriod;
		}
	}
}
