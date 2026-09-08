package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
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
import net.lacnic.elections.adminweb.validators.OrganizationSupportSearchValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;
import net.lacnic.elections.utils.CountryUtils;
import net.lacnic.elections.utils.Constants;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class GenericNominationOrgSupportsManagementPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final int MAX_ACTIVE_ORG_SUPPORTS = 2;
	private static final String EMAIL_MASK_LOCAL = "*****";
	private static final String CONTACT_ID_MASK = "...";
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();

	private final String token;

	private SupportIdentifierType identifierType = SupportIdentifierType.ORG_ID;
	private String identifierValue;
	private OrganizationSummary searchedOrganization;
	private final List<SupportRequestRow> supportRequestRows = new ArrayList<>();
	private int activeSupportsCount = 0;

	private WebMarkupContainer searchContainer;
	private WebMarkupContainer foundOrganizationContainer;
	private WebMarkupContainer sendRequestActionContainer;
	private WebMarkupContainer membershipContactConflictAlert;
	private WebMarkupContainer waitingAlert;
	private Label noSupportsLabel;
	private final String supportRecipient;

	public GenericNominationOrgSupportsManagementPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(buildTaskTitle("cardTitle"));
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";
		this.supportRecipient = resolveSupportRecipient();

		loadSupportRequests();

		Label restrictionMessage = new Label("restrictionMessage", getTaskResolution().hasModeRestriction() ? getString(getTaskResolution().getRestrictionMessageKey()) : "");
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);

		Form<Void> orgSupportsForm = new Form<>("orgSupportsForm");
		add(orgSupportsForm);

		Label requiredSupportsHint = new Label("requiredSupportsHint", new StringResourceModel("acceptNominationOrgSupportsRequiredHint", this, null).setParameters(MAX_ACTIVE_ORG_SUPPORTS));
		orgSupportsForm.add(requiredSupportsHint);

		int missingSupportsCount = resolveMissingSupportsCount(MAX_ACTIVE_ORG_SUPPORTS, activeSupportsCount);
		Label progressHint = new Label("progressHint", new StringResourceModel("acceptNominationOrgSupportsProgressHint", this, null).setParameters(activeSupportsCount, MAX_ACTIVE_ORG_SUPPORTS));
		progressHint.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  missingSupportsCount == 0 ? "text-success mb-3" : "text-muted mb-3"));
		orgSupportsForm.add(progressHint);

		searchContainer = new WebMarkupContainer("searchContainer");
		searchContainer.setVisible(canRequestSupport());
		orgSupportsForm.add(searchContainer);

		DropDownChoice<SupportIdentifierType> identifierTypeChoice = new DropDownChoice<>("identifierType", new PropertyModel<>(this, "identifierType"),
				Arrays.asList(SupportIdentifierType.values()), new ChoiceRenderer<SupportIdentifierType>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(SupportIdentifierType object) {
						return getString(object.getLabelKey());
					}
				});
		identifierTypeChoice.setNullValid(false);
		searchContainer.add(identifierTypeChoice);

		TextField<String> identifierValueField = new TextField<>("identifierValue", new PropertyModel<>(this, "identifierValue"));
		searchContainer.add(identifierValueField);

		OrganizationSupportSearchValidator organizationSupportSearchValidator = new OrganizationSupportSearchValidator(
				identifierTypeChoice,
				identifierValueField,
				token,
				supportRecipient);
		orgSupportsForm.add(organizationSupportSearchValidator);

		searchContainer.add(new Button("searchOrganizationButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (!canRequestSupport()) {
					SecurityUtils.error(getString("acceptNominationOrgSupportsRequestInvalidState"));
					searchedOrganization = null;
					refreshVisibility();
					return;
				}
				Organization organization = organizationSupportSearchValidator.getFoundOrganization();
				if (organizationSupportSearchValidator.getSelectedIdentifierType() != null) {
					identifierType = organizationSupportSearchValidator.getSelectedIdentifierType();
				}
				if (StringUtils.isNotBlank(organizationSupportSearchValidator.getNormalizedIdentifierValue())) {
					identifierValue = organizationSupportSearchValidator.getNormalizedIdentifierValue();
				}
				searchedOrganization = organization != null
						? OrganizationSummary.from(organization, getString("acceptNominationOrgSupportsContactIdDisplay"), resolveCountryLabel(organization.getCountry()))
						: null;
				refreshVisibility();
			}

			@Override
			public void onError() {
				searchedOrganization = null;
				refreshVisibility();
			}
		});

		foundOrganizationContainer = new WebMarkupContainer("foundOrganizationContainer");
		foundOrganizationContainer.setVisible(canRequestSupport() && searchedOrganization != null);
		searchContainer.add(foundOrganizationContainer);

		foundOrganizationContainer.add(new Label("foundOrganizationName", new PropertyModel<>(this, "searchedOrganization.name")));
		foundOrganizationContainer.add(new Label("foundOrganizationCountry", new PropertyModel<>(this, "searchedOrganization.country")));
		foundOrganizationContainer.add(new Label("foundOrganizationContact", new PropertyModel<>(this, "searchedOrganization.membershipContactDisplay")));

		membershipContactConflictAlert = new WebMarkupContainer("membershipContactConflictAlert");
		membershipContactConflictAlert.setVisible(canRequestSupport() && searchedOrganization != null && isMembershipContactConflict());
		membershipContactConflictAlert.add(new Label("membershipContactConflictMessage",
				new StringResourceModel("acceptNominationOrgSupportsMembershipContactMatchesCandidate", this, null)
						.setParameters(StringUtils.defaultIfBlank(supportRecipient, "-"))));
		searchContainer.add(membershipContactConflictAlert);

		sendRequestActionContainer = new WebMarkupContainer("sendRequestActionContainer");
		sendRequestActionContainer.setVisible(canRequestSupport() && searchedOrganization != null && !isMembershipContactConflict());
		searchContainer.add(sendRequestActionContainer);

		sendRequestActionContainer.add(new Button("sendSupportRequestButton") {
			private static final long serialVersionUID = 1L;

			{
				setDefaultFormProcessing(false);
			}

			@Override
			public void onSubmit() {
				requestOrganizationSupport();
			}
		});

		waitingAlert = new WebMarkupContainer("waitingAlert");
		waitingAlert.setVisible(showWaitingAlert());
		orgSupportsForm.add(waitingAlert);

		noSupportsLabel = new Label("noSupportsLabel", getString("acceptNominationOrgSupportsNoRows"));
		noSupportsLabel.setVisible(supportRequestRows.isEmpty());
		orgSupportsForm.add(noSupportsLabel);

		ListView<SupportRequestRow> supportRows = new ListView<>("supportRows", supportRequestRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<SupportRequestRow> item) {
				SupportRequestRow row = item.getModelObject();
				item.add(new Label("supportOrganizationLabel", StringUtils.defaultIfBlank(row.getOrganizationLabel(), "-")));
				item.add(new Label("supportCountryLabel", StringUtils.defaultIfBlank(row.getCountry(), "-")));
				item.add(new Label("supportContactLabel", StringUtils.defaultIfBlank(row.getMembershipContactDisplay(), "-")));

				Label statusLabel = new Label("supportStatusLabel", getString(resolveSupportStatusLabelKey(row.getSupportStatus())));
				statusLabel.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge " + resolveSupportStatusBadgeClass(row.getSupportStatus())));
				item.add(statusLabel);
			}
		};
		orgSupportsForm.add(supportRows);

		orgSupportsForm.add(new BookmarkablePageLink<Void>("backButton", GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token)));

		Button continueLaterButton = new Button("continueLaterButton") {
			private static final long serialVersionUID = 1L;

			{
				setDefaultFormProcessing(false);
			}

			@Override
			public void onSubmit() {
				if (!ensureCanContinueLater()) {
					return;
				}
				requestTaskStatusChange(CandidateElectionTaskStatus.STARTED);
			}
		};
		continueLaterButton.setVisible(canContinueLater() && canRequestSupport());
		orgSupportsForm.add(continueLaterButton);

		refreshVisibility();
	}

	private void requestOrganizationSupport() {
		if (!canRequestSupport()) {
			SecurityUtils.error(getString("acceptNominationOrgSupportsRequestInvalidState"));
			refreshVisibility();
			return;
		}
		if (searchedOrganization == null || searchedOrganization.getId() <= 0) {
			SecurityUtils.error(getString("acceptNominationOrgSupportsRequestNeedsSearch"));
			refreshVisibility();
			return;
		}
		if (hasSupportRequestForOrganization(searchedOrganization.getId())) {
			SecurityUtils.error(getString("acceptNominationOrgSupportsAlreadyRequested"));
			refreshVisibility();
			return;
		}
		if (isMembershipContactConflict()) {
			SecurityUtils.error(new StringResourceModel("acceptNominationOrgSupportsMembershipContactMatchesCandidate", this, null)
					.setParameters(StringUtils.defaultIfBlank(supportRecipient, "-"))
					.getString());
			refreshVisibility();
			return;
		}

		boolean requested = AppContext.getInstance().getPreNominationBeanRemote()
				.requestCandidateOrganizationSupport(token, searchedOrganization.getId(), CANDIDATE_LINK_ACTIVITY_ACTOR, SecurityUtils.getClientIp());
		if (!requested) {
			SecurityUtils.error(getString("acceptNominationOrgSupportsRequestError"));
			refreshVisibility();
			return;
		}

		SecurityUtils.info(getString("acceptNominationOrgSupportsRequestSuccess"));
		setResponsePage(GenericAcceptNominationTasksPage.class, buildCurrentTaskPageParameters(AcceptNominationTaskMode.COMPLETE));
	}

	private void loadSupportRequests() {
		supportRequestRows.clear();
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
			if (support.getSupportingOrganization() == null) {
				continue;
			}

			SupportStatus status = support.getSupportStatus() != null ? support.getSupportStatus() : SupportStatus.PROPOSED;
			if (isActiveSupportStatus(status)) {
				activeSupportsCount++;
			}

			SupportRequestRow row = new SupportRequestRow();
			row.setSupportNominationId(support.getId());
			row.setSupportStatus(status);

			Organization supportingOrganization = support.getSupportingOrganization();
			row.setSupportingOrganizationId(supportingOrganization.getId());
			String orgId = StringUtils.trimToEmpty(supportingOrganization.getOrgId());
			String orgName = StringUtils.trimToEmpty(supportingOrganization.getName());
			if (StringUtils.isNotBlank(orgName) && StringUtils.isNotBlank(orgId)) {
				row.setOrganizationLabel(orgName + " (" + orgId + ")");
			} else if (StringUtils.isNotBlank(orgName)) {
				row.setOrganizationLabel(orgName);
			} else if (StringUtils.isNotBlank(orgId)) {
				row.setOrganizationLabel(orgId);
			}
			row.setCountry(resolveCountryLabel(supportingOrganization.getCountry()));

			String contactName = StringUtils.trimToNull(supportingOrganization.getMembershipContactName());
			String contactId = StringUtils.trimToNull(supportingOrganization.getMembershipContactId());
			String contactEmail = StringUtils.trimToNull(supportingOrganization.getMembershipContactEmail());
			row.setMembershipContactDisplay(formatSupportingContact(contactName, contactId, contactEmail));

			supportRequestRows.add(row);
		}
	}

	private boolean canRequestSupport() {
		if (!canContinueLater()) {
			return false;
		}
		return activeSupportsCount < MAX_ACTIVE_ORG_SUPPORTS;
	}

	private boolean showWaitingAlert() {
		return activeSupportsCount >= MAX_ACTIVE_ORG_SUPPORTS;
	}

	private void refreshVisibility() {
		if (searchContainer != null) {
			searchContainer.setVisible(canRequestSupport());
		}
		if (foundOrganizationContainer != null) {
			foundOrganizationContainer.setVisible(canRequestSupport() && searchedOrganization != null);
		}
		if (membershipContactConflictAlert != null) {
			membershipContactConflictAlert.setVisible(canRequestSupport() && searchedOrganization != null && isMembershipContactConflict());
		}
		if (sendRequestActionContainer != null) {
			sendRequestActionContainer.setVisible(canRequestSupport() && searchedOrganization != null && !isMembershipContactConflict());
		}
		if (waitingAlert != null) {
			waitingAlert.setVisible(showWaitingAlert());
		}
		if (noSupportsLabel != null) {
			noSupportsLabel.setVisible(supportRequestRows.isEmpty());
		}
	}

	private boolean hasSupportRequestForOrganization(long organizationId) {
		for (SupportRequestRow row : supportRequestRows) {
			if (row.getSupportingOrganizationId() == organizationId) {
				return true;
			}
		}
		return false;
	}

	private boolean isActiveSupportStatus(SupportStatus status) {
		return status == SupportStatus.PROPOSED || status == SupportStatus.ACCEPTED || status == SupportStatus.APPROVED;
	}

	private String resolveSupportStatusLabelKey(SupportStatus status) {
		if (status == null) {
			return "acceptNominationOrgSupportsStatusProposed";
		}
		switch (status) {
		case ACCEPTED:
			return "acceptNominationOrgSupportsStatusAccepted";
		case REJECTED:
			return "acceptNominationOrgSupportsStatusRejected";
		case INVALID:
			return "acceptNominationOrgSupportsStatusInvalid";
		case APPROVED:
			return "acceptNominationOrgSupportsStatusApproved";
		case PROPOSED:
		default:
			return "acceptNominationOrgSupportsStatusProposed";
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

	private String normalizeIdentifierValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return value.trim().toUpperCase(Locale.ROOT);
	}

	private String resolveCountryLabel(String countryCode) {
		String normalizedCode = COUNTRY_UTILS.normalizeCountryCode(countryCode);
		if (normalizedCode == null) {
			return "-";
		}
		return COUNTRY_UTILS.getDisplayLabel(normalizedCode, getLocale(), true);
	}

	private int resolveMissingSupportsCount(int required, int current) {
		return Math.max(required - current, 0);
	}

	private boolean isMembershipContactConflict() {
		if (searchedOrganization == null) {
			return false;
		}
		String candidateEmail = normalizeEmail(resolveCandidateEmail());
		String membershipContactEmail = normalizeEmail(searchedOrganization.getMembershipContactEmail());
		return candidateEmail != null && membershipContactEmail != null && candidateEmail.equals(membershipContactEmail);
	}

	private String resolveCandidateEmail() {
		AcceptNominationTaskResolution resolution = getTaskResolution();
		if (resolution == null) {
			return null;
		}
		Candidate candidate = resolution.getCandidate();
		return candidate != null ? candidate.getMail() : null;
	}

	private String normalizeEmail(String email) {
		String normalized = StringUtils.trimToNull(email);
		return normalized != null ? normalized.toLowerCase(Locale.ROOT) : null;
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

	private String formatSupportingContact(String name, String contactId, String email) {
		List<String> values = new ArrayList<>();
		if (StringUtils.isNotBlank(name)) {
			values.add(name.trim());
		}
		if (StringUtils.isNotBlank(contactId)) {
			values.add(formatContactId(contactId));
		}
		if (StringUtils.isNotBlank(email)) {
			values.add(maskEmail(email));
		}
		if (values.isEmpty()) {
			return "-";
		}
		return String.join(" · ", values);
	}

	private String formatContactId(String contactId) {
		String pattern = StringUtils.defaultIfBlank(getString("acceptNominationOrgSupportsContactIdDisplay"), "ID {0}");
		return MessageFormat.format(pattern, maskContactIdValue(contactId));
	}

	private String maskEmail(String email) {
		if (StringUtils.isBlank(email)) {
			return "";
		}
		String trimmed = email.trim();
		int atIndex = trimmed.indexOf('@');
		if (atIndex <= 0 || atIndex == trimmed.length() - 1) {
			return trimmed;
		}

		String localPart = trimmed.substring(0, atIndex);
		String domainPart = trimmed.substring(atIndex + 1);
		String maskedLocal = localPart.length() <= 2 ? localPart.charAt(0) + EMAIL_MASK_LOCAL : localPart.substring(0, 2) + EMAIL_MASK_LOCAL;

		int dotIndex = domainPart.lastIndexOf('.');
		if (dotIndex <= 0 || dotIndex == domainPart.length() - 1) {
			String maskedDomain = domainPart.length() <= 2 ? domainPart.charAt(0) + "****" : domainPart.substring(0, 2) + "****";
			return maskedLocal + "@" + maskedDomain;
		}

		String domainName = domainPart.substring(0, dotIndex);
		String domainSuffix = domainPart.substring(dotIndex);
		String maskedDomainName = domainName.length() <= 2 ? domainName.charAt(0) + "****" : domainName.substring(0, 2) + "****";
		return maskedLocal + "@" + maskedDomainName + domainSuffix;
	}

	private static String maskContactIdValue(String contactId) {
		String trimmed = StringUtils.trimToEmpty(contactId);
		if (trimmed.isEmpty()) {
			return "";
		}
		return trimmed.substring(0, 1) + CONTACT_ID_MASK;
	}

	private PageParameters buildCurrentTaskPageParameters(AcceptNominationTaskMode mode) {
		PageParameters params = UtilsParameters.getToken(token);
		if (getSelectedTask() != null && getSelectedTask().getTaskKey() != null) {
			params.add(AcceptNominationTaskResolver.TASK_PARAM, getSelectedTask().getTaskKey().name());
		}
		params.add(AcceptNominationTaskResolver.MODE_PARAM, mode.getParameterValue());
		return params;
	}

	public SupportIdentifierType getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(SupportIdentifierType identifierType) {
		this.identifierType = identifierType;
	}

	public String getIdentifierValue() {
		return identifierValue;
	}

	public void setIdentifierValue(String identifierValue) {
		this.identifierValue = identifierValue;
	}

	public OrganizationSummary getSearchedOrganization() {
		return searchedOrganization;
	}

	public enum SupportIdentifierType {
		ORG_ID("acceptNominationOrgSupportsIdentifierTypeOrgId"),
		CNPJ("acceptNominationOrgSupportsIdentifierTypeCnpj"),
		ASN("acceptNominationOrgSupportsIdentifierTypeAsn");

		private final String labelKey;

		SupportIdentifierType(String labelKey) {
			this.labelKey = labelKey;
		}

		public String getLabelKey() {
			return labelKey;
		}
	}

	private static final class OrganizationSummary implements Serializable {
		private static final long serialVersionUID = 1L;

		private long id;
		private String name;
		private String country;
		private String membershipContactEmail;
		private String membershipContactDisplay;

		static OrganizationSummary from(Organization organization, String contactIdDisplayPattern, String countryLabel) {
			OrganizationSummary summary = new OrganizationSummary();
			summary.id = organization.getId();
			String orgId = StringUtils.trimToEmpty(organization.getOrgId());
			String orgName = StringUtils.trimToEmpty(organization.getName());
			if (StringUtils.isNotBlank(orgName) && StringUtils.isNotBlank(orgId)) {
				summary.name = orgName + " (" + orgId + ")";
			} else if (StringUtils.isNotBlank(orgName)) {
				summary.name = orgName;
			} else if (StringUtils.isNotBlank(orgId)) {
				summary.name = orgId;
			} else {
				summary.name = "-";
			}
			summary.country = StringUtils.defaultIfBlank(StringUtils.trimToNull(countryLabel), "-");
			String contactName = StringUtils.trimToNull(organization.getMembershipContactName());
			String contactId = StringUtils.trimToNull(organization.getMembershipContactId());
			String contactEmail = StringUtils.trimToNull(organization.getMembershipContactEmail());
			summary.membershipContactEmail = contactEmail;
			summary.membershipContactDisplay = buildContactDisplay(contactName, contactId, contactEmail, contactIdDisplayPattern);
			return summary;
		}

		private static String buildContactDisplay(String name, String contactId, String email, String contactIdDisplayPattern) {
			String pattern = StringUtils.defaultIfBlank(contactIdDisplayPattern, "ID {0}");
			List<String> values = new ArrayList<>();
			if (StringUtils.isNotBlank(name)) {
				values.add(name.trim());
			}
			if (StringUtils.isNotBlank(contactId)) {
				values.add(MessageFormat.format(pattern, maskContactIdValue(contactId)));
			}
			if (StringUtils.isNotBlank(email)) {
				values.add(maskEmailValue(email));
			}
			if (values.isEmpty()) {
				return "-";
			}
			return String.join(" · ", values);
		}

		private static String maskEmailValue(String email) {
			String trimmed = StringUtils.trimToEmpty(email);
			int atIndex = trimmed.indexOf('@');
			if (atIndex <= 0 || atIndex == trimmed.length() - 1) {
				return trimmed;
			}

			String localPart = trimmed.substring(0, atIndex);
			String domainPart = trimmed.substring(atIndex + 1);
			String maskedLocal = localPart.length() <= 2 ? localPart.charAt(0) + EMAIL_MASK_LOCAL : localPart.substring(0, 2) + EMAIL_MASK_LOCAL;

			int dotIndex = domainPart.lastIndexOf('.');
			if (dotIndex <= 0 || dotIndex == domainPart.length() - 1) {
				String maskedDomain = domainPart.length() <= 2 ? domainPart.charAt(0) + "****" : domainPart.substring(0, 2) + "****";
				return maskedLocal + "@" + maskedDomain;
			}

			String domainName = domainPart.substring(0, dotIndex);
			String domainSuffix = domainPart.substring(dotIndex);
			String maskedDomainName = domainName.length() <= 2 ? domainName.charAt(0) + "****" : domainName.substring(0, 2) + "****";
			return maskedLocal + "@" + maskedDomainName + domainSuffix;
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getCountry() {
			return country;
		}

		public String getMembershipContactDisplay() {
			return membershipContactDisplay;
		}

		public String getMembershipContactEmail() {
			return membershipContactEmail;
		}
	}

	private static final class SupportRequestRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private long supportNominationId;
		private long supportingOrganizationId;
		private String organizationLabel;
		private String country;
		private String membershipContactDisplay;
		private SupportStatus supportStatus;

		public long getSupportNominationId() {
			return supportNominationId;
		}

		public void setSupportNominationId(long supportNominationId) {
			this.supportNominationId = supportNominationId;
		}

		public long getSupportingOrganizationId() {
			return supportingOrganizationId;
		}

		public void setSupportingOrganizationId(long supportingOrganizationId) {
			this.supportingOrganizationId = supportingOrganizationId;
		}

		public String getOrganizationLabel() {
			return organizationLabel;
		}

		public void setOrganizationLabel(String organizationLabel) {
			this.organizationLabel = organizationLabel;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getMembershipContactDisplay() {
			return membershipContactDisplay;
		}

		public void setMembershipContactDisplay(String membershipContactDisplay) {
			this.membershipContactDisplay = membershipContactDisplay;
		}

		public SupportStatus getSupportStatus() {
			return supportStatus;
		}

		public void setSupportStatus(SupportStatus supportStatus) {
			this.supportStatus = supportStatus;
		}
	}

}
