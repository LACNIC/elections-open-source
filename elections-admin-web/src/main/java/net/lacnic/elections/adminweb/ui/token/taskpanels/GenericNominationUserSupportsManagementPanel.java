package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskMode;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolver;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.validators.AuthorizedSupportContactEmailValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;
import net.lacnic.elections.utils.Constants;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class GenericNominationUserSupportsManagementPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";

	private final String token;
	private final ElectionTaskKey taskKey;
	private final int requiredSupports;

	private String supportContactName;
	private String supportContactEmail;

	private final List<UserSupportRow> supportRows = new ArrayList<>();
	private int activeSupportsCount = 0;

	private WebMarkupContainer requestContainer;
	private WebMarkupContainer waitingAlert;
	private Label noSupportsLabel;
	private final String supportRecipient;

	public GenericNominationUserSupportsManagementPanel(String id, AcceptNominationTaskResolution resolution, ElectionTaskKey taskKey, int requiredSupports) {
		super(id, resolution);
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";
		this.taskKey = taskKey;
		this.requiredSupports = requiredSupports;
		this.supportRecipient = resolveSupportRecipient();

		loadSupportRows();

		add(buildTaskTitle("cardTitle"));

		Label restrictionMessage = new Label("restrictionMessage", getTaskResolution().hasModeRestriction() ? getString(getTaskResolution().getRestrictionMessageKey()) : "");
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);

		Form<Void> userSupportsForm = new Form<>("userSupportsForm");
		add(userSupportsForm);

		Label requiredSupportsHint = new Label("requiredSupportsHint", new StringResourceModel("acceptNominationUserSupportsRequiredHint", this, null).setParameters(requiredSupports));
		userSupportsForm.add(requiredSupportsHint);

		int missingSupportsCount = resolveMissingSupportsCount(requiredSupports, activeSupportsCount);
		Label progressHint = new Label("progressHint", new StringResourceModel("acceptNominationUserSupportsProgressHint", this, null).setParameters(activeSupportsCount, requiredSupports, missingSupportsCount));
		progressHint.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  missingSupportsCount == 0 ? "text-success mb-3" : "text-muted mb-3"));
		userSupportsForm.add(progressHint);

		requestContainer = new WebMarkupContainer("requestContainer");
		requestContainer.setVisible(canRequestSupport());
		userSupportsForm.add(requestContainer);

		requestContainer.add(new TextField<>("supportContactName", new PropertyModel<>(this, "supportContactName")));
		EmailTextField supportContactEmailField = new EmailTextField("supportContactEmail", new PropertyModel<>(this, "supportContactEmail"));
		supportContactEmailField.add(new AuthorizedSupportContactEmailValidator(resolveAuthorizedSupportEmails(), supportRecipient));
		requestContainer.add(supportContactEmailField);

		requestContainer.add(new Button("sendSupportRequestButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				requestUserSupport();
			}
		});

		waitingAlert = new WebMarkupContainer("waitingAlert");
		waitingAlert.setVisible(showWaitingAlert());
		userSupportsForm.add(waitingAlert);

		Label waitingAlertMessage = new Label("waitingAlertMessage", new StringResourceModel("acceptNominationUserSupportsWaitingAlert", this, null).setParameters(requiredSupports));
		waitingAlert.add(waitingAlertMessage);

		noSupportsLabel = new Label("noSupportsLabel", new StringResourceModel("acceptNominationUserSupportsNoRows", this, null));
		noSupportsLabel.setVisible(supportRows.isEmpty());
		userSupportsForm.add(noSupportsLabel);

		ListView<UserSupportRow> supportRowsList = new ListView<>("supportRows", supportRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<UserSupportRow> item) {
				UserSupportRow row = item.getModelObject();
				item.add(new Label("supportContactNameLabel", StringUtils.defaultIfBlank(row.getSupportingContactName(), "-")));
				item.add(new Label("supportContactEmailLabel", StringUtils.defaultIfBlank(row.getSupportingContactEmail(), "-")));

				Label statusLabel = new Label("supportStatusLabel", getString(resolveSupportStatusLabelKey(row.getSupportStatus())));
				statusLabel.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge " + resolveSupportStatusBadgeClass(row.getSupportStatus())));
				item.add(statusLabel);
			}
		};
		userSupportsForm.add(supportRowsList);

		userSupportsForm.add(new BookmarkablePageLink<Void>("backButton", GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token)));

		Button continueLaterButton = new Button("continueLaterButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (!ensureCanContinueLater()) {
					return;
				}
				requestTaskStatusChange(CandidateElectionTaskStatus.STARTED);
			}
		};
		continueLaterButton.setVisible(canContinueLater() && canRequestSupport());
		userSupportsForm.add(continueLaterButton);

		refreshVisibility();
	}

	private void requestUserSupport() {
		if (!canRequestSupport()) {
			SecurityUtils.error(getString("acceptNominationUserSupportsRequestInvalidState"));
			refreshVisibility();
			return;
		}

		String normalizedName = StringUtils.trimToNull(supportContactName);
		String normalizedEmail = StringUtils.trimToNull(supportContactEmail);
		if (StringUtils.isBlank(normalizedName)) {
			SecurityUtils.error(getString("acceptNominationUserSupportsContactNameRequired"));
			refreshVisibility();
			return;
		}
		if (StringUtils.isBlank(normalizedEmail)) {
			SecurityUtils.error(getString("acceptNominationUserSupportsContactEmailRequired"));
			refreshVisibility();
			return;
		}
		if (isSupportContactConflict(normalizedEmail)) {
			SecurityUtils.error(new StringResourceModel("acceptNominationUserSupportsContactMatchesCandidate", this, null)
					.setParameters(StringUtils.defaultIfBlank(supportRecipient, "-"))
					.getString());
			refreshVisibility();
			return;
		}
		if (hasSupportRequestForEmail(normalizedEmail)) {
			SecurityUtils.error(getString("acceptNominationUserSupportsAlreadyRequested"));
			refreshVisibility();
			return;
		}

		boolean requested = AppContext.getInstance().getPreNominationBeanRemote()
				.requestCandidateUserSupport(token, normalizedName, normalizedEmail, taskKey, requiredSupports, CANDIDATE_LINK_ACTIVITY_ACTOR, SecurityUtils.getClientIp());
		if (!requested) {
			SecurityUtils.error(getString("acceptNominationUserSupportsRequestError"));
			refreshVisibility();
			return;
		}

		SecurityUtils.info(getString("acceptNominationUserSupportsRequestSuccess"));
		setResponsePage(GenericAcceptNominationTasksPage.class, buildCurrentTaskPageParameters(AcceptNominationTaskMode.COMPLETE));
	}

	private void loadSupportRows() {
		supportRows.clear();
		activeSupportsCount = 0;

		Nomination nomination = getTaskResolution() != null ? getTaskResolution().getNomination() : null;
		if (nomination == null || nomination.getSupports() == null) {
			return;
		}

		List<SupportNomination> supports = new ArrayList<>(nomination.getSupports());
		supports.sort(Comparator.comparingLong(SupportNomination::getId).reversed());

		for (SupportNomination support : supports) {
			if (support == null) {
				continue;
			}
			if (support.getSupportingOrganization() != null) {
				continue;
			}

			SupportStatus status = support.getSupportStatus() != null ? support.getSupportStatus() : SupportStatus.PROPOSED;
			if (isActiveUserSupportStatus(status)) {
				activeSupportsCount++;
			}

			UserSupportRow row = new UserSupportRow();
			row.setSupportingContactName(StringUtils.trimToNull(support.getSupportingContactName()));
			row.setSupportingContactEmail(StringUtils.trimToNull(support.getSupportingContactEmail()));
			row.setSupportStatus(status);
			supportRows.add(row);
		}
	}

	private boolean canRequestSupport() {
		if (!canContinueLater()) {
			return false;
		}
		return activeSupportsCount < requiredSupports;
	}

	private boolean showWaitingAlert() {
		return activeSupportsCount >= requiredSupports;
	}

	private void refreshVisibility() {
		if (requestContainer != null) {
			requestContainer.setVisible(canRequestSupport());
		}
		if (waitingAlert != null) {
			waitingAlert.setVisible(showWaitingAlert());
		}
		if (noSupportsLabel != null) {
			noSupportsLabel.setVisible(supportRows.isEmpty());
		}
	}

	private boolean isActiveUserSupportStatus(SupportStatus status) {
		return status == SupportStatus.PROPOSED
				|| status == SupportStatus.ACCEPTED
				|| status == SupportStatus.APPROVED;
	}

	private boolean hasSupportRequestForEmail(String email) {
		String normalizedEmail = normalizeEmailForComparison(email);
		if (normalizedEmail == null) {
			return false;
		}
		for (UserSupportRow row : supportRows) {
			if (normalizedEmail.equals(normalizeEmailForComparison(row.getSupportingContactEmail()))) {
				return true;
			}
		}
		return false;
	}

	private String normalizeEmailForComparison(String email) {
		String normalized = StringUtils.trimToNull(email);
		if (normalized == null) {
			return null;
		}
		return normalized.toLowerCase(Locale.ROOT);
	}

	private boolean isSupportContactConflict(String email) {
		String candidateEmail = resolveCandidateEmail();
		String normalizedCandidateEmail = normalizeEmailForComparison(candidateEmail);
		String normalizedSupportEmail = normalizeEmailForComparison(email);
		return normalizedCandidateEmail != null && normalizedSupportEmail != null && normalizedCandidateEmail.equals(normalizedSupportEmail);
	}

	private String resolveCandidateEmail() {
		AcceptNominationTaskResolution resolution = getTaskResolution();
		if (resolution == null) {
			return null;
		}
		Candidate candidate = resolution.getCandidate();
		return candidate != null ? candidate.getMail() : null;
	}

	private String resolveAuthorizedSupportEmails() {
		Nomination nomination = getTaskResolution() != null ? getTaskResolution().getNomination() : null;
		return nomination != null && nomination.getElection() != null ? nomination.getElection().getAuthorizedSupportEmails() : null;
	}

	private String resolveSupportRecipient() {
		if (getTaskResolution() != null && getTaskResolution().getNomination() != null && getTaskResolution().getNomination().getElection() != null) {
			String electionRecipient = StringUtils.trimToNull(getTaskResolution().getNomination().getElection().getDefaultRecipient());
			if (electionRecipient != null) {
				return electionRecipient;
			}
			String electionSender = StringUtils.trimToNull(getTaskResolution().getNomination().getElection().getDefaultSender());
			if (electionSender != null) {
				return electionSender;
			}
		}
		try {
			Parameter parameter = AppContext.getInstance().getManagerBeanRemote().getParameter(Constants.DEFAULT_RECIPIENT);
			if (parameter != null && StringUtils.isNotBlank(parameter.getValue())) {
				return parameter.getValue();
			}
		} catch (Exception ex) {
			// Fallback below.
		}
		try {
			Parameter parameter = AppContext.getInstance().getManagerBeanRemote().getParameter(Constants.DEFAULT_SENDER);
			if (parameter != null && StringUtils.isNotBlank(parameter.getValue())) {
				return parameter.getValue();
			}
		} catch (Exception ex) {
			// Fallback below.
		}
		return "-";
	}

	private int resolveMissingSupportsCount(int required, int current) {
		return Math.max(required - current, 0);
	}

	private String resolveSupportStatusLabelKey(SupportStatus status) {
		if (status == null) {
			return "acceptNominationUserSupportsStatusProposed";
		}
		switch (status) {
		case ACCEPTED:
			return "acceptNominationUserSupportsStatusAccepted";
		case REJECTED:
			return "acceptNominationUserSupportsStatusRejected";
		case INVALID:
			return "acceptNominationUserSupportsStatusInvalid";
		case APPROVED:
			return "acceptNominationUserSupportsStatusApproved";
		case PROPOSED:
		default:
			return "acceptNominationUserSupportsStatusProposed";
		}
	}

	private String resolveSupportStatusBadgeClass(SupportStatus status) {
		if (status == null) {
			return "bg-warning-subtle text-warning-emphasis";
		}
		switch (status) {
		case ACCEPTED:
			return "bg-info-subtle text-info-emphasis";
		case REJECTED:
			return "bg-danger-subtle text-danger-emphasis";
		case INVALID:
			return "bg-secondary-subtle text-secondary-emphasis";
		case APPROVED:
			return "bg-success-subtle text-success-emphasis";
		case PROPOSED:
		default:
			return "bg-warning-subtle text-warning-emphasis";
		}
	}

	private PageParameters buildCurrentTaskPageParameters(AcceptNominationTaskMode mode) {
		PageParameters params = UtilsParameters.getToken(token);
		if (getSelectedTask() != null && getSelectedTask().getTaskKey() != null) {
			params.add(AcceptNominationTaskResolver.TASK_PARAM, getSelectedTask().getTaskKey().name());
		}
		params.add(AcceptNominationTaskResolver.MODE_PARAM, mode.getParameterValue());
		return params;
	}

	public String getSupportContactName() {
		return supportContactName;
	}

	public void setSupportContactName(String supportContactName) {
		this.supportContactName = supportContactName;
	}

	public String getSupportContactEmail() {
		return supportContactEmail;
	}

	public void setSupportContactEmail(String supportContactEmail) {
		this.supportContactEmail = supportContactEmail;
	}

	private static final class UserSupportRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private String supportingContactName;
		private String supportingContactEmail;
		private SupportStatus supportStatus;

		public String getSupportingContactName() {
			return supportingContactName;
		}

		public void setSupportingContactName(String supportingContactName) {
			this.supportingContactName = supportingContactName;
		}

		public String getSupportingContactEmail() {
			return supportingContactEmail;
		}

		public void setSupportingContactEmail(String supportingContactEmail) {
			this.supportingContactEmail = supportingContactEmail;
		}

		public SupportStatus getSupportStatus() {
			return supportStatus;
		}

		public void setSupportStatus(SupportStatus supportStatus) {
			this.supportStatus = supportStatus;
		}
	}
}
