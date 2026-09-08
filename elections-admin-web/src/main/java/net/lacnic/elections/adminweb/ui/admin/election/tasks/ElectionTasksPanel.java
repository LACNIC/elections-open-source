package net.lacnic.elections.adminweb.ui.admin.election.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.campus.CampusClient;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.ElectionPresetConfigurations;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.TaskDependencyLevel;
import net.lacnic.elections.utils.DateTimeUtils;

public class ElectionTasksPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private final Election election;
	private final boolean campusConfigured;

	private ElectionTaskKey addTaskKey;
	private CalendarOption addCalendarOption;
	private TaskDependencyLevel addDependencyLevel;
	private Integer addDisplayOrder;
	private boolean addPublicable;

	public ElectionTasksPanel(String id, Election election) {
		super(id);
		this.election = election;
		this.campusConfigured = CampusClient.isCampusIntegrationEnabled();

		List<ElectionCalendar> electionCalendars = loadElectionCalendars();
		List<CalendarOption> calendarOptions = buildCalendarOptions(electionCalendars);
		List<TaskDependencyLevel> dependencyLevels = Arrays.asList(TaskDependencyLevel.values());
		List<TaskRowModel> taskRows = loadTaskRows(calendarOptions);
		List<ElectionTaskKey> availableTaskKeys = loadAvailableTaskKeys(taskRows);
		boolean campusTrainingEnabled = CampusClient.isCampusTrainingEnabled(election);

		if (calendarOptions.isEmpty()) {
			error(getString("electionTasksManagementNoCalendars"));
		}

		if (!calendarOptions.isEmpty()) {
			addCalendarOption = calendarOptions.get(0);
		}
		if (!dependencyLevels.isEmpty()) {
			addDependencyLevel = dependencyLevels.get(0);
		}

		add(buildCurrentTasksSection(taskRows, calendarOptions, dependencyLevels, campusTrainingEnabled));
		add(buildAddTaskSection(availableTaskKeys, calendarOptions, dependencyLevels, campusTrainingEnabled));
	}

	private WebMarkupContainer buildCurrentTasksSection(List<TaskRowModel> taskRows, List<CalendarOption> calendarOptions, List<TaskDependencyLevel> dependencyLevels,
			boolean campusTrainingEnabled) {
		WebMarkupContainer currentTasksContainer = new WebMarkupContainer("currentTasksContainer");

		Form<Void> currentTasksForm = new Form<>("currentTasksForm");
		currentTasksContainer.add(currentTasksForm);

		ListView<TaskRowModel> currentTasksRows = new ListView<TaskRowModel>("currentTasksRows", taskRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<TaskRowModel> item) {
				TaskRowModel row = item.getModelObject();
				item.add(new Label("taskKeyLabel", getTaskLabel(row.getTaskKey())));

				DropDownChoice<CalendarOption> rowCalendarChoice = new DropDownChoice<>("rowCalendar", new PropertyModel<>(row, "calendarOption"), calendarOptions, CALENDAR_RENDERER);
				rowCalendarChoice.setRequired(true);
				item.add(rowCalendarChoice);

				DropDownChoice<TaskDependencyLevel> rowDependencyLevelChoice = new DropDownChoice<>("rowDependencyLevel", new PropertyModel<>(row, "dependencyLevel"), dependencyLevels,
						DEPENDENCY_LEVEL_RENDERER);
				rowDependencyLevelChoice.setRequired(true);
				item.add(rowDependencyLevelChoice);

				NumberTextField<Integer> rowDisplayOrderField = new NumberTextField<>("rowDisplayOrder", new PropertyModel<>(row, "displayOrder"), Integer.class);
				rowDisplayOrderField.setRequired(false);
				item.add(rowDisplayOrderField);

				boolean publicableEnabled = canConfigurePublicable(row.getTaskKey(), campusTrainingEnabled);
				if (!publicableEnabled) {
					row.setPublicable(false);
				}
				CheckBox rowPublicable = new CheckBox("rowPublicable", new PropertyModel<>(row, "publicable"));
				rowPublicable.setEnabled(publicableEnabled);
				item.add(rowPublicable);

				item.add(new ButtonDeleteWithConfirmation("removeTaskButton", row.getTaskId()) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onConfirm() {
						boolean ok = AppContext.getInstance().getManagerBeanRemote().removeElectionTask(row.getTaskId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						if (ok) {
							getSession().info(getString("electionTasksManagementRemoveSuccess"));
						} else {
							getSession().error(getString("electionTasksManagementRemoveBlockedProgress"));
						}
						reloadPage();
					}
				});

				Link<Void> moveUpTask = new Link<Void>("moveUp") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						boolean ok = AppContext.getInstance().getManagerBeanRemote().moveElectionTaskUp(row.getTaskId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						if (ok) {
							getSession().info(getString("electionTasksManagementUpdateSuccess"));
						} else {
							getSession().error(getString("electionTasksManagementUpdateError"));
						}
						reloadPage();
					}
				};
				moveUpTask.setVisible(item.getIndex() > 0);
				item.add(moveUpTask);

				Link<Void> moveDownTask = new Link<Void>("moveDown") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						boolean ok = AppContext.getInstance().getManagerBeanRemote().moveElectionTaskDown(row.getTaskId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						if (ok) {
							getSession().info(getString("electionTasksManagementUpdateSuccess"));
						} else {
							getSession().error(getString("electionTasksManagementUpdateError"));
						}
						reloadPage();
					}
				};
				moveDownTask.setVisible(item.getIndex() < taskRows.size() - 1);
				item.add(moveDownTask);
			}
		};
		currentTasksRows.setVisible(!taskRows.isEmpty());
		currentTasksForm.add(currentTasksRows);
		currentTasksForm.add(new Button("saveAllTasksButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (taskRows.isEmpty()) {
					getSession().error(getString("electionTasksManagementUpdateError"));
					reloadPage();
					return;
				}

				boolean allOk = true;
				for (TaskRowModel row : taskRows) {
					if (row == null || row.getCalendarOption() == null) {
						allOk = false;
						continue;
					}
					boolean ok = AppContext.getInstance().getManagerBeanRemote().editElectionTask(row.getTaskId(), row.getCalendarOption().getId(), row.getDependencyLevel(),
							row.getDisplayOrder(), row.isPublicable(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					if (!ok) {
						allOk = false;
					}
				}

				if (allOk) {
					getSession().info(getString("electionTasksManagementUpdateSuccess"));
				} else {
					getSession().error(getString("electionTasksManagementUpdateError"));
				}
				reloadPage();
			}
		}.setVisible(!taskRows.isEmpty()));

		return currentTasksContainer;
	}

	private WebMarkupContainer buildAddTaskSection(List<ElectionTaskKey> availableTaskKeys, List<CalendarOption> calendarOptions, List<TaskDependencyLevel> dependencyLevels,
			boolean campusTrainingEnabled) {
		WebMarkupContainer addTaskContainer = new WebMarkupContainer("addTaskContainer");

		Form<Void> addTaskForm = new Form<Void>("addTaskForm") {
			private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit() {
					if (addTaskKey == null || addCalendarOption == null || addDependencyLevel == null) {
						getSession().error(getString("electionTasksManagementAddError"));
						return;
					}
					if (!canConfigurePublicable(addTaskKey, campusTrainingEnabled)) {
						addPublicable = false;
					}

					boolean ok = AppContext.getInstance().getManagerBeanRemote().addElectionTask(election.getElectionId(), addTaskKey, addCalendarOption.getId(), addDependencyLevel, addDisplayOrder,
							addPublicable, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					if (ok) {
						getSession().info(getString("electionTasksManagementAddSuccess"));
					} else {
						getSession().error(getString("electionTasksManagementAddError"));
					}
					reloadPage();
				}
			};

		CheckBox addPublicableCheckBox = new CheckBox("addTaskPublicable", new PropertyModel<>(this, "addPublicable"));
		addPublicableCheckBox.setOutputMarkupId(true);
		addPublicableCheckBox.setEnabled(!availableTaskKeys.isEmpty() && !calendarOptions.isEmpty());

		DropDownChoice<ElectionTaskKey> addTaskChoice = new DropDownChoice<>("addTaskKey", new PropertyModel<>(this, "addTaskKey"), availableTaskKeys, TASK_KEY_RENDERER);
		addTaskChoice.setNullValid(false);
		addTaskChoice.setRequired(true);
		addTaskChoice.setEnabled(!availableTaskKeys.isEmpty() && !calendarOptions.isEmpty());
		addTaskChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				boolean enabled = !availableTaskKeys.isEmpty() && !calendarOptions.isEmpty()
						&& canConfigurePublicable(addTaskKey, campusTrainingEnabled);
				if (!enabled) {
					addPublicable = false;
				}
				addPublicableCheckBox.setEnabled(enabled);
				target.add(addPublicableCheckBox);
			}
		});
		addTaskForm.add(addTaskChoice);

		DropDownChoice<CalendarOption> addCalendarChoice = new DropDownChoice<>("addTaskCalendar", new PropertyModel<>(this, "addCalendarOption"), calendarOptions, CALENDAR_RENDERER);
		addCalendarChoice.setNullValid(false);
		addCalendarChoice.setRequired(true);
		addCalendarChoice.setEnabled(!availableTaskKeys.isEmpty() && !calendarOptions.isEmpty());
		addTaskForm.add(addCalendarChoice);

		DropDownChoice<TaskDependencyLevel> addDependencyLevelChoice = new DropDownChoice<>("addTaskDependencyLevel", new PropertyModel<>(this, "addDependencyLevel"), dependencyLevels,
				DEPENDENCY_LEVEL_RENDERER);
		addDependencyLevelChoice.setNullValid(false);
		addDependencyLevelChoice.setRequired(true);
		addDependencyLevelChoice.setEnabled(!availableTaskKeys.isEmpty() && !calendarOptions.isEmpty());
		addTaskForm.add(addDependencyLevelChoice);

		NumberTextField<Integer> addDisplayOrderField = new NumberTextField<>("addTaskDisplayOrder", new PropertyModel<>(this, "addDisplayOrder"), Integer.class);
		addDisplayOrderField.setRequired(false);
		addDisplayOrderField.setEnabled(!availableTaskKeys.isEmpty() && !calendarOptions.isEmpty());
		addTaskForm.add(addDisplayOrderField);

		addTaskForm.add(addPublicableCheckBox);

		Button addTaskButton = new Button("addTaskButton");
		addTaskButton.setEnabled(!availableTaskKeys.isEmpty() && !calendarOptions.isEmpty());
		addTaskForm.add(addTaskButton);

		addTaskContainer.add(addTaskForm);
		return addTaskContainer;
	}

	static boolean canConfigurePublicable(ElectionTaskKey taskKey, boolean campusTrainingEnabled) {
		return taskKey != ElectionTaskKey.COURSE || campusTrainingEnabled;
	}

	private List<ElectionCalendar> loadElectionCalendars() {
		try {
			return AppContext.getInstance().getManagerBeanRemote().getElectionCalendars(election.getElectionId());
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	private List<TaskRowModel> loadTaskRows(List<CalendarOption> calendarOptions) {
		try {
			List<ElectionTask> electionTasks = AppContext.getInstance().getManagerBeanRemote().getElectionTasks(election.getElectionId());
			return electionTasks.stream().sorted(Comparator
					.comparingInt(this::resolveDisplayOrderSortValue)
					.thenComparingLong(ElectionTask::getId))
					.filter(task -> isTaskAvailable(task.getTaskKey()))
					.map(task -> {
				TaskRowModel row = new TaskRowModel();
				row.setTaskId(task.getId());
				row.setTaskKey(task.getTaskKey());
				row.setDependencyLevel(task.getDependencyLevel() != null ? task.getDependencyLevel() : TaskDependencyLevel.LEVEL_1);
				row.setDisplayOrder(task.getDisplayOrder());
				row.setPublicable(task.isPublicable());
				row.setCalendarOption(findCalendarOption(task.getElectionCalendar() != null ? task.getElectionCalendar().getId() : 0L, calendarOptions));
				return row;
			}).collect(Collectors.toList());
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	private List<ElectionTaskKey> loadAvailableTaskKeys(List<TaskRowModel> taskRows) {
		List<ElectionTaskKey> usedTaskKeys = taskRows.stream().map(TaskRowModel::getTaskKey).collect(Collectors.toList());
		List<ElectionTaskKey> availableTaskKeys = new ArrayList<>();
		for (ElectionTaskKey taskKey : ElectionTaskKey.values()) {
			if (!usedTaskKeys.contains(taskKey) && isTaskAvailable(taskKey)) {
				availableTaskKeys.add(taskKey);
			}
		}
		return availableTaskKeys;
	}

	private boolean isTaskAvailable(ElectionTaskKey taskKey) {
		return ElectionPresetConfigurations.isPresetTaskEnabled(taskKey, campusConfigured);
	}

	private List<CalendarOption> buildCalendarOptions(List<ElectionCalendar> electionCalendars) {
		List<CalendarOption> options = new ArrayList<>();
		for (ElectionCalendar electionCalendar : electionCalendars) {
			String label = getCalendarLabel(electionCalendar);
			options.add(new CalendarOption(electionCalendar.getId(), label));
		}
		return options;
	}

	private String getCalendarLabel(ElectionCalendar electionCalendar) {
		if (electionCalendar.getCalendarKey() == null) {
			return String.valueOf(electionCalendar.getId());
		}
		ElectionCalendarKey key = electionCalendar.getCalendarKey();
		StringBuilder label = new StringBuilder(getShortCalendarDescription(key));
		String stageCode = buildStageCode(key);
		if (stageCode != null && !stageCode.isBlank()) {
			label.append(" (").append(stageCode).append(")");
		}
		String dateRange = buildCalendarDateRange(electionCalendar);
		if (dateRange != null && !dateRange.isBlank()) {
			label.append(" (").append(dateRange).append(")");
		}
		return label.toString();
	}

	private String getShortCalendarDescription(ElectionCalendarKey key) {
		String language = getLocale() != null ? getLocale().getLanguage() : "es";
		String description;
		if ("en".equals(language)) {
			description = key.getDescriptionEN();
		} else if ("pt".equals(language)) {
			description = key.getDescriptionPT();
		} else {
			description = key.getDescriptionES();
		}

		if (description == null || description.isBlank()) {
			return key.name();
		}
		int firstSentenceEnd = description.indexOf('.');
		if (firstSentenceEnd > 0) {
			return description.substring(0, firstSentenceEnd).trim();
		}
		return description.trim();
	}

	private String buildStageCode(ElectionCalendarKey key) {
		if (key == null) {
			return null;
		}
		String keyName = key.name();
		if (!keyName.startsWith("N_")) {
			return keyName;
		}
		int secondUnderscore = keyName.indexOf('_', 2);
		if (secondUnderscore > 2) {
			return "N" + keyName.substring(2, secondUnderscore);
		}
		return "N" + keyName.substring(2);
	}

	private String buildCalendarDateRange(ElectionCalendar electionCalendar) {
		if (electionCalendar.getStartDate() == null) {
			return null;
		}
		String startDate = formatCalendarDate(electionCalendar.getStartDate());
		if (electionCalendar.getEndDate() == null) {
			return startDate;
		}
		return startDate + " - " + formatCalendarDate(electionCalendar.getEndDate());
	}

	private String formatCalendarDate(java.util.Date date) {
		return new DateTime(date).plusHours(election.getDiffUTC()).toString(DateTimeUtils.ELECTION_DATE_FORMAT);
	}

	private CalendarOption findCalendarOption(long calendarId, List<CalendarOption> calendarOptions) {
		for (CalendarOption option : calendarOptions) {
			if (option.getId() == calendarId) {
				return option;
			}
		}
		return calendarOptions.isEmpty() ? null : calendarOptions.get(0);
	}

	private String getTaskLabel(ElectionTaskKey taskKey) {
		String screenSpecificKey = "electionTasksManagementTaskKey." + taskKey.name();
		try {
			return getString(screenSpecificKey);
		} catch (Exception ignored) {
			// Fallback to shared task key labels when no screen-specific label exists.
		}
		String resourceKey = "electionTaskKey." + taskKey.name();
		try {
			return getString(resourceKey);
		} catch (Exception ignored) {
			String normalized = taskKey.name().toLowerCase(Locale.ROOT).replace('_', ' ');
			return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
		}
	}

	private String getDependencyLevelLabel(TaskDependencyLevel dependencyLevel) {
		if (dependencyLevel == null) {
			return "-";
		}
		String resourceKey = "electionTaskDependencyLevel." + dependencyLevel.name();
		try {
			return getString(resourceKey);
		} catch (Exception ignored) {
			String normalized = dependencyLevel.name().toLowerCase(Locale.ROOT).replace('_', ' ');
			return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
		}
	}

	private int resolveDisplayOrderSortValue(ElectionTask task) {
		if (task == null || task.getDisplayOrder() == null) {
			return Integer.MAX_VALUE;
		}
		return task.getDisplayOrder();
	}

	private void reloadPage() {
		PageParameters params = UtilsParameters.getId(election.getElectionId());
		setResponsePage(ElectionTasksDashboard.class, params);
	}

	private final IChoiceRenderer<ElectionTaskKey> TASK_KEY_RENDERER = new IChoiceRenderer<ElectionTaskKey>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(ElectionTaskKey object) {
			return getTaskLabel(object);
		}

		@Override
		public String getIdValue(ElectionTaskKey object, int index) {
			return object.name();
		}
	};

	private final IChoiceRenderer<CalendarOption> CALENDAR_RENDERER = new IChoiceRenderer<CalendarOption>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(CalendarOption object) {
			return object.getLabel();
		}

		@Override
		public String getIdValue(CalendarOption object, int index) {
			return String.valueOf(object.getId());
		}
	};

	private final IChoiceRenderer<TaskDependencyLevel> DEPENDENCY_LEVEL_RENDERER = new IChoiceRenderer<TaskDependencyLevel>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(TaskDependencyLevel object) {
			return getDependencyLevelLabel(object);
		}

		@Override
		public String getIdValue(TaskDependencyLevel object, int index) {
			return object.name();
		}
	};

	public static class TaskRowModel implements Serializable {
		private static final long serialVersionUID = 1L;
		private long taskId;
		private ElectionTaskKey taskKey;
		private CalendarOption calendarOption;
		private TaskDependencyLevel dependencyLevel;
		private Integer displayOrder;
		private boolean publicable;

		public long getTaskId() {
			return taskId;
		}

		public void setTaskId(long taskId) {
			this.taskId = taskId;
		}

		public ElectionTaskKey getTaskKey() {
			return taskKey;
		}

		public void setTaskKey(ElectionTaskKey taskKey) {
			this.taskKey = taskKey;
		}

		public CalendarOption getCalendarOption() {
			return calendarOption;
		}

		public void setCalendarOption(CalendarOption calendarOption) {
			this.calendarOption = calendarOption;
		}

		public TaskDependencyLevel getDependencyLevel() {
			return dependencyLevel;
		}

		public void setDependencyLevel(TaskDependencyLevel dependencyLevel) {
			this.dependencyLevel = dependencyLevel;
		}

		public Integer getDisplayOrder() {
			return displayOrder;
		}

		public void setDisplayOrder(Integer displayOrder) {
			this.displayOrder = displayOrder;
		}

		public boolean isPublicable() {
			return publicable;
		}

		public void setPublicable(boolean publicable) {
			this.publicable = publicable;
		}
	}

	public static class CalendarOption implements Serializable {
		private static final long serialVersionUID = 1L;
		private final long id;
		private final String label;

		public CalendarOption(long id, String label) {
			this.id = id;
			this.label = label;
		}

		public long getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
