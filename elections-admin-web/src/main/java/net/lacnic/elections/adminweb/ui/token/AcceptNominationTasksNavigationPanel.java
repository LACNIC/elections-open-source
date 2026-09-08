package net.lacnic.elections.adminweb.ui.token;

import java.text.DateFormat;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.pre.CandidateStatus;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class AcceptNominationTasksNavigationPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final String token;
	private final AcceptNominationTaskResolution resolution;

	public AcceptNominationTasksNavigationPanel(String id, String token, AcceptNominationTaskResolution resolution) {
		super(id);
		this.token = token;
		this.resolution = resolution;

		add(new ListView<AcceptNominationTaskItem>("tasksRows", resolution.getTasks()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AcceptNominationTaskItem> item) {
				AcceptNominationTaskItem row = item.getModelObject();
				boolean readOnlyByCandidateStatus = isReadOnlyByCandidateStatus();
				boolean nominationWindowClosed = resolution.isNominationWindowClosed();
				boolean rowLockedByDependency = row.isBlocked();
				boolean rowLockedByPeriod = nominationWindowClosed;
				boolean rowLocked = rowLockedByDependency || rowLockedByPeriod;

				if (resolution.getSelectedTask() != null && resolution.getSelectedTask().getTaskKey() == row.getTaskKey()) {
					item.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "table-active"));
				}
				if (rowLocked) {
					item.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "opacity-50"));
				}

				String taskNameKey = row.getTaskKey() != null ? "electionTaskKey." + row.getTaskKey().name() : "";
				item.add(new Label("taskName", new ResourceModel(taskNameKey, row.getTaskName())));

				Label statusLabel = new Label("taskStatus", new ResourceModel(row.getStatusLabelKey()));
				statusLabel.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge badge-label " + row.getStatusBadgeClass()));
				statusLabel.add(AttributeModifier.replace("data-testid", AcceptNominationTaskTestIdUtils.buildTaskScopedTestId("taskStatus", row.getTaskKey())));
				item.add(statusLabel);

				item.add(new Label("taskStartDate", formatDateTime(row.getStartDate())));
				item.add(new Label("taskEndDate", formatDateTime(row.getEndDate())));
				item.add(new Label("taskDeadlineDate", formatDate(row.getDeadlineDate())));
				item.add(new Label("taskProgress", row.getProgressText()));

				WebMarkupContainer actionGroup = new WebMarkupContainer("actionGroup");
				actionGroup.add(AttributeModifier.replace("data-testid", AcceptNominationTaskTestIdUtils.buildTaskScopedTestId("actionGroup", row.getTaskKey())));

				boolean showViewAction = readOnlyByCandidateStatus || row.isCompleted();
				boolean showEditAction = !readOnlyByCandidateStatus && row.isCompleted() && !rowLocked;
				WebMarkupContainer optionsGroup = new WebMarkupContainer("optionsGroup");
				optionsGroup.add(AttributeModifier.replace("data-testid", AcceptNominationTaskTestIdUtils.buildTaskScopedTestId("actionOptionsGroup", row.getTaskKey())));
				optionsGroup.setVisible(showViewAction || showEditAction);

				BookmarkablePageLink<Void> actionView = new BookmarkablePageLink<>("actionView", GenericAcceptNominationTasksPage.class,
						buildPageParameters(row.getTaskKey().name(), AcceptNominationTaskMode.VIEW));
				actionView.add(AttributeModifier.replace("data-testid", AcceptNominationTaskTestIdUtils.buildTaskScopedTestId("actionView", row.getTaskKey())));
				actionView.setVisible(showViewAction);
				optionsGroup.add(actionView);

				BookmarkablePageLink<Void> actionEdit = new BookmarkablePageLink<>("actionEdit", GenericAcceptNominationTasksPage.class,
						buildPageParameters(row.getTaskKey().name(), AcceptNominationTaskMode.EDIT));
				actionEdit.add(AttributeModifier.replace("data-testid", AcceptNominationTaskTestIdUtils.buildTaskScopedTestId("actionEdit", row.getTaskKey())));
				actionEdit.setVisible(showEditAction);
				optionsGroup.add(actionEdit);

				actionGroup.add(optionsGroup);

				BookmarkablePageLink<Void> actionComplete = new BookmarkablePageLink<>("actionComplete", GenericAcceptNominationTasksPage.class,
						buildPageParameters(row.getTaskKey().name(), AcceptNominationTaskMode.COMPLETE));
				actionComplete.add(AttributeModifier.replace("data-testid", AcceptNominationTaskTestIdUtils.buildTaskScopedTestId("actionComplete", row.getTaskKey())));
				actionComplete.setVisible(!readOnlyByCandidateStatus && !row.isCompleted() && !rowLocked);
				actionGroup.add(actionComplete);

				WebMarkupContainer taskLockedBadge = new WebMarkupContainer("taskLockedBadge");
				taskLockedBadge.setVisible(rowLocked);
				String lockPopoverKey = rowLockedByPeriod ? "acceptNominationTasksLockPeriodEndedPopover" : "acceptNominationTasksLockedPopover";
				taskLockedBadge.add(AttributeModifier.replace("data-bs-content", new ResourceModel(lockPopoverKey)));
				actionGroup.add(taskLockedBadge);

				item.add(actionGroup);
			}
		});
	}

	private boolean isReadOnlyByCandidateStatus() {
		if (resolution == null || resolution.getCandidate() == null || resolution.getCandidate().getStatus() == null) {
			return false;
		}
		CandidateStatus status = resolution.getCandidate().getStatus();
		return status == CandidateStatus.COMPLETE
				|| status == CandidateStatus.REJECTED
				|| status == CandidateStatus.CONFIRMED_AND_PUBLISHED;
	}

	private PageParameters buildPageParameters(String taskKey, AcceptNominationTaskMode mode) {
		PageParameters params = UtilsParameters.getToken(token);
		params.add(AcceptNominationTaskResolver.TASK_PARAM, taskKey);
		params.add(AcceptNominationTaskResolver.MODE_PARAM, mode.getParameterValue());
		return params;
	}

	private String formatDate(java.util.Date date) {
		if (date == null) {
			return "-";
		}
		DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, SecurityUtils.getLocale());
		return formatter.format(date);
	}

	private String formatDateTime(java.util.Date date) {
		if (date == null) {
			return "-";
		}
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, SecurityUtils.getLocale());
		return formatter.format(date);
	}

}
