package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateWorkOrganization;
import net.lacnic.elections.domain.pre.WorkOrganizationType;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class GenericNominationOrganizationsManagementPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";
	private static final int MAX_ORGANIZATION_NAME_LENGTH = 1000;
	private static final int MAX_ORGANIZATION_GROUP_LENGTH = 1000;

	private Candidate candidate;
	private final String token;

	private Boolean hasOrganizationRelationship;
	private String organizationName;
	private String organizationGroup;
	private WorkOrganizationType workOrganizationType;
	private Integer editingOrganizationIndex;
	private final List<CandidateWorkOrganization> workOrganizations = new ArrayList<>();

	public GenericNominationOrganizationsManagementPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		this.candidate = resolution.getCandidate();
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";
		add(buildTaskTitle("cardTitle"));
		loadFormState();

		Label restrictionMessage = new Label("restrictionMessage", getTaskResolution().hasModeRestriction() ? getString(getTaskResolution().getRestrictionMessageKey()) : "");
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);

		Form<Void> organizationsForm = new Form<>("organizationsForm");
		add(organizationsForm);

		RadioGroup<Boolean> hasOrganizationGroup = new RadioGroup<>("hasOrganizationGroup", new PropertyModel<>(this, "hasOrganizationRelationship"));
		hasOrganizationGroup.add(new Radio<>("yes", Model.of(Boolean.TRUE)));
		hasOrganizationGroup.add(new Radio<>("no", Model.of(Boolean.FALSE)));
		organizationsForm.add(hasOrganizationGroup);

		WebMarkupContainer organizationDetailsContainer = new WebMarkupContainer("organizationDetailsContainer");
		organizationDetailsContainer.setOutputMarkupPlaceholderTag(true);
		organizationDetailsContainer.setOutputMarkupId(true);
		organizationDetailsContainer.setVisible(Boolean.TRUE.equals(hasOrganizationRelationship));
		organizationsForm.add(organizationDetailsContainer);

		TextField<String> organizationNameField = new TextField<>("organizationName", new PropertyModel<>(this, "organizationName"));
		organizationNameField.setOutputMarkupId(true);
		organizationDetailsContainer.add(organizationNameField);

		AiAssistTextAreaPanel organizationGroupEditor = new AiAssistTextAreaPanel(
				"organizationGroupEditor",
				new PropertyModel<>(this, "organizationGroup"),
				null,
				this::addExternalFeedback,
				(originalText, instruction, styleContext) -> null,
				isReadOnlyMode(),
				true,
				false,
				false,
				false,
				MAX_ORGANIZATION_GROUP_LENGTH,
				3);
		organizationDetailsContainer.add(organizationGroupEditor);

		RadioGroup<WorkOrganizationType> relationshipTypeGroup = new RadioGroup<>("relationshipTypeGroup", new PropertyModel<>(this, "workOrganizationType"));
		relationshipTypeGroup.add(new Radio<>("paid", Model.of(WorkOrganizationType.PAID)));
		relationshipTypeGroup.add(new Radio<>("adHonorem", Model.of(WorkOrganizationType.AD_HONOREM)));
		organizationDetailsContainer.add(relationshipTypeGroup);

		WebMarkupContainer organizationsTableContainer = new WebMarkupContainer("organizationsTableContainer");
		organizationsTableContainer.setOutputMarkupPlaceholderTag(true);
		organizationsTableContainer.setOutputMarkupId(true);
		organizationDetailsContainer.add(organizationsTableContainer);

		Label noOrganizationsLabel = new Label("noOrganizationsLabel", getString("acceptNominationOrganizationsNoRows"));
		noOrganizationsLabel.setOutputMarkupPlaceholderTag(true);
		noOrganizationsLabel.setVisible(workOrganizations.isEmpty());
		organizationsTableContainer.add(noOrganizationsLabel);

		ListView<CandidateWorkOrganization> organizationsRows = new ListView<>("organizationsRows", workOrganizations) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CandidateWorkOrganization> item) {
				CandidateWorkOrganization organization = item.getModelObject();
				item.add(new Label("organizationNameCell", StringUtils.defaultString(organization.getOrganizationName())));
				item.add(new Label("organizationGroupCell", StringUtils.defaultIfBlank(organization.getOrganizationGroup(), "-")));
				item.add(new Label("organizationTypeCell", getOrganizationTypeLabel(organization.getWorkOrganizationType())));

				AjaxLink<Void> editRowButton = new AjaxLink<Void>("editRowButton") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						editingOrganizationIndex = item.getIndex();
						organizationName = organization.getOrganizationName();
						organizationGroup = organization.getOrganizationGroup();
						workOrganizationType = organization.getWorkOrganizationType();
						target.add(organizationDetailsContainer);
						addExternalFeedback(target);
					}
				};
				item.add(editRowButton);

				item.add(new ButtonDeleteWithConfirmation("removeRowButton", item.getIndex()) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onConfirm() {
						removeOrganization(item.getIndex());
						noOrganizationsLabel.setVisible(workOrganizations.isEmpty());
					}
				});
			}
		};
		organizationsTableContainer.add(organizationsRows);

		AjaxButton addOrganizationButton = new AjaxButton("addOrganizationButton", organizationsForm) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				if (!validateOrganizationEntry()) {
					addExternalFeedback(target);
					return;
				}

				upsertOrganization();
				noOrganizationsLabel.setVisible(workOrganizations.isEmpty());
				target.add(organizationDetailsContainer);
				addExternalFeedback(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				addExternalFeedback(target);
			}
		};
		organizationDetailsContainer.add(addOrganizationButton);

		hasOrganizationGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				organizationDetailsContainer.setVisible(Boolean.TRUE.equals(hasOrganizationRelationship));
				if (!Boolean.TRUE.equals(hasOrganizationRelationship)) {
					clearOrganizationFormState();
				}
				target.add(organizationDetailsContainer);
				addExternalFeedback(target);
			}
		});

		organizationsForm.add(new BookmarkablePageLink<Void>("backButton", GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token)));

		Button continueLaterButton = new Button("continueLaterButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (!ensureCanContinueLater()) {
					return;
				}
				if (!persistOrganizations(false)) {
					return;
				}
				requestTaskStatusChange(CandidateElectionTaskStatus.STARTED);
			}
		};
		continueLaterButton.setVisible(canContinueLater());
		organizationsForm.add(continueLaterButton);

		organizationsForm.add(new Button("finishAndSendButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (!persistOrganizations(true)) {
					return;
				}
				requestTaskStatusChange(CandidateElectionTaskStatus.COMPLETED);
			}
		});
	}

	private void addExternalFeedback(AjaxRequestTarget target) {
		addPageFeedback(target);
	}

	private void loadFormState() {
		workOrganizations.clear();
		clearOrganizationFormState();

		if (candidate != null && candidate.getWorkOrganizations() != null) {
			for (CandidateWorkOrganization organization : candidate.getWorkOrganizations()) {
				if (organization == null || StringUtils.isBlank(organization.getOrganizationName()) || organization.getWorkOrganizationType() == null) {
					continue;
				}
				CandidateWorkOrganization copy = new CandidateWorkOrganization();
				copy.setOrganizationName(StringUtils.trimToEmpty(organization.getOrganizationName()));
				copy.setOrganizationGroup(StringUtils.trimToNull(organization.getOrganizationGroup()));
				copy.setWorkOrganizationType(organization.getWorkOrganizationType());
				workOrganizations.add(copy);
			}
		}

		if (isUntouchedTaskInCompleteMode()) {
			hasOrganizationRelationship = null;
			return;
		}

		if (candidate == null) {
			hasOrganizationRelationship = null;
			return;
		}

		hasOrganizationRelationship = Boolean.TRUE.equals(candidate.isQUnemployed()) ? Boolean.FALSE : Boolean.TRUE;
	}

	private boolean isUntouchedTaskInCompleteMode() {
		if (!isCompleteMode() || getSelectedTask() == null) {
			return false;
		}
		Date startDate = getSelectedTask().getStartDate();
		Date endDate = getSelectedTask().getEndDate();
		return startDate == null && endDate == null;
	}

	private void clearOrganizationFormState() {
		organizationName = "";
		organizationGroup = "";
		workOrganizationType = null;
		editingOrganizationIndex = null;
	}

	private boolean validateOrganizationEntry() {
		boolean valid = true;
		if (!Boolean.TRUE.equals(hasOrganizationRelationship)) {
			SecurityUtils.error(getString("acceptNominationOrganizationsRequireYesForRows"));
			valid = false;
		}

		String normalizedName = StringUtils.trimToEmpty(organizationName);
		if (StringUtils.isBlank(normalizedName)) {
			SecurityUtils.error(getString("acceptNominationOrganizationsNameRequired"));
			valid = false;
		}
		if (normalizedName.length() > MAX_ORGANIZATION_NAME_LENGTH) {
			SecurityUtils.error(getString("acceptNominationOrganizationsNameMaxLength"));
			valid = false;
		}
		String normalizedGroup = StringUtils.trimToEmpty(organizationGroup);
		if (normalizedGroup.length() > MAX_ORGANIZATION_GROUP_LENGTH) {
			SecurityUtils.error(getString("acceptNominationOrganizationsGroupMaxLength"));
			valid = false;
		}
		if (workOrganizationType == null) {
			SecurityUtils.error(getString("acceptNominationOrganizationsTypeRequired"));
			valid = false;
		}
		return valid;
	}

	private void upsertOrganization() {
		CandidateWorkOrganization organization = new CandidateWorkOrganization();
		organization.setOrganizationName(StringUtils.trimToEmpty(organizationName));
		organization.setOrganizationGroup(StringUtils.trimToNull(organizationGroup));
		organization.setWorkOrganizationType(workOrganizationType);

		if (editingOrganizationIndex != null && editingOrganizationIndex >= 0 && editingOrganizationIndex < workOrganizations.size()) {
			workOrganizations.set(editingOrganizationIndex, organization);
		} else {
			workOrganizations.add(organization);
		}

		clearOrganizationFormState();
	}

	private void removeOrganization(int index) {
		if (index < 0 || index >= workOrganizations.size()) {
			return;
		}
		workOrganizations.remove(index);
		if (editingOrganizationIndex != null && editingOrganizationIndex == index) {
			clearOrganizationFormState();
			return;
		}
		if (editingOrganizationIndex != null && editingOrganizationIndex > index) {
			editingOrganizationIndex = editingOrganizationIndex - 1;
		}
	}

	private boolean persistOrganizations(boolean completeSubmission) {
		if (!validateOrganizationsForSubmit(completeSubmission)) {
			return false;
		}

		if (hasOrganizationRelationship == null && !completeSubmission) {
			return true;
		}

		Candidate candidateData = new Candidate();
		candidateData.setQUnemployed(Boolean.FALSE.equals(hasOrganizationRelationship));
		candidateData.setWorkOrganizations(buildOrganizationsToPersist());

		Candidate updatedCandidate = AppContext.getInstance().getPreNominationBeanRemote().saveCandidateOrganizations(token, candidateData, CANDIDATE_LINK_ACTIVITY_ACTOR,
				SecurityUtils.getClientIp());
		if (updatedCandidate == null) {
			SecurityUtils.error(getString("acceptNominationOrganizationsSaveError"));
			return false;
		}

		candidate = updatedCandidate;
		loadFormState();
		return true;
	}

	private boolean validateOrganizationsForSubmit(boolean completeSubmission) {
		if (!completeSubmission) {
			return true;
		}

		boolean valid = true;
		if (hasOrganizationRelationship == null) {
			SecurityUtils.error(getString("acceptNominationOrganizationsQuestionRequired"));
			valid = false;
		}

		if (Boolean.TRUE.equals(hasOrganizationRelationship) && workOrganizations.isEmpty()) {
			SecurityUtils.error(getString("acceptNominationOrganizationsAtLeastOneRequired"));
			valid = false;
		}

		return valid;
	}

	private List<CandidateWorkOrganization> buildOrganizationsToPersist() {
		List<CandidateWorkOrganization> organizationsToPersist = new ArrayList<>();
		if (!Boolean.TRUE.equals(hasOrganizationRelationship)) {
			return organizationsToPersist;
		}

		for (CandidateWorkOrganization organization : workOrganizations) {
			if (organization == null || StringUtils.isBlank(organization.getOrganizationName()) || organization.getWorkOrganizationType() == null) {
				continue;
			}
			CandidateWorkOrganization copy = new CandidateWorkOrganization();
			copy.setOrganizationName(StringUtils.trimToEmpty(organization.getOrganizationName()));
			copy.setOrganizationGroup(StringUtils.trimToNull(organization.getOrganizationGroup()));
			copy.setWorkOrganizationType(organization.getWorkOrganizationType());
			organizationsToPersist.add(copy);
		}

		return organizationsToPersist;
	}

	private String getOrganizationTypeLabel(WorkOrganizationType type) {
		if (type == WorkOrganizationType.PAID) {
			return getString("acceptNominationOrganizationsTypePaidShort");
		}
		if (type == WorkOrganizationType.AD_HONOREM) {
			return getString("acceptNominationOrganizationsTypeAdHonoremShort");
		}
		return "";
	}

	public Boolean getHasOrganizationRelationship() {
		return hasOrganizationRelationship;
	}

	public void setHasOrganizationRelationship(Boolean hasOrganizationRelationship) {
		this.hasOrganizationRelationship = hasOrganizationRelationship;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getOrganizationGroup() {
		return organizationGroup;
	}

	public void setOrganizationGroup(String organizationGroup) {
		this.organizationGroup = organizationGroup;
	}

	public WorkOrganizationType getWorkOrganizationType() {
		return workOrganizationType;
	}

	public void setWorkOrganizationType(WorkOrganizationType workOrganizationType) {
		this.workOrganizationType = workOrganizationType;
	}
}
