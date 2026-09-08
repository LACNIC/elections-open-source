package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;
import net.lacnic.elections.utils.Constants;

public class ManageCandidateSupportsDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private SupportNomination selectedSupport;
	private String correctionComment;
	private Boolean correctionConfirmed = Boolean.FALSE;

	public ManageCandidateSupportsDashboard(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedback"));
		long electionId = UtilsParameters.getIdAsLong(params);
		long candidateId = UtilsParameters.getCandidateAsLong(params);
		Candidate candidate = AppContext.getInstance().getManagerBeanRemote().getCandidate(candidateId);
		if (!isValidCandidate(candidate, electionId)) {
			getSession().error(getString("candidateSupportsCandidateNotFound"));
			setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			return;
		}

		List<SupportNomination> supports = safeSupports(
				AppContext.getInstance().getManagerBeanRemote().getCandidateSupportNominations(candidateId));
		List<SupportNomination> actionableSupports = filterSupportsWithStatus(supports);
		Nomination nomination = AppContext.getInstance().getManagerBeanRemote().getNominationByCandidateId(candidateId);
		Map<ElectionTaskKey, CandidateElectionTaskStatus> supportTaskStatuses = AppContext.getInstance()
				.getManagerBeanRemote().getCandidateSupportTaskStatuses(candidateId);
		boolean correctionAllowed = isCorrectionAllowed(candidate, nomination);

		add(new Label("candidateName", valueOrDash(candidate.getName())));
		add(new Label("candidateId", String.valueOf(candidate.getCandidateId())));
		add(new Label("candidateMail", valueOrDash(candidate.getMail())));
		add(new Label("candidateStatus", resolveCandidateStatus(candidate.getStatus())));
		add(new Label("organizationSupportTaskStatus", resolveTaskStatus(supportTaskStatuses, ElectionTaskKey.ORG_SUPPORTS)));
		add(new Label("userSupportTwoTaskStatus", resolveTaskStatus(supportTaskStatuses, ElectionTaskKey.USER_SUPPORTS_2)));
		add(new Label("userSupportFiveTaskStatus", resolveTaskStatus(supportTaskStatuses, ElectionTaskKey.USER_SUPPORTS_5)));

		add(new ListView<SupportNomination>("supports", supports) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<SupportNomination> item) {
				SupportNomination support = item.getModelObject();
				item.add(new Label("supportId", String.valueOf(support.getId())));
				item.add(new Label("supportType", resolveSupportType(support)));
				item.add(new Label("supportParty", resolveSupportParty(support)));
				item.add(new Label("supportStatus", resolveSupportStatus(support.getSupportStatus())));
				item.add(new Label("supportResponseDate", formatResponseDate(support)));
			}
		});
		add(new Label("noSupports", getString("candidateSupportsEmpty")).setVisible(supports.isEmpty()));

		Form<Void> correctionForm = new Form<>("correctionForm");
		correctionForm.setVisible(correctionAllowed && !actionableSupports.isEmpty());
		add(correctionForm);

		DropDownChoice<SupportNomination> supportChoice = new DropDownChoice<>(
				"selectedSupport",
				new PropertyModel<>(this, "selectedSupport"),
				actionableSupports,
				SUPPORT_RENDERER);
		supportChoice.setRequired(true);
		correctionForm.add(supportChoice);

		TextArea<String> commentField = new TextArea<>("correctionComment", new PropertyModel<>(this, "correctionComment"));
		commentField.setRequired(true);
		commentField.add(StringValidator.maximumLength(Constants.ADMINISTRATIVE_SUPPORT_REJECTION_REASON_MAX_LENGTH));
		correctionForm.add(commentField);

		CheckBox confirmationField = new CheckBox("correctionConfirmed", new PropertyModel<>(this, "correctionConfirmed"));
		confirmationField.add(new RequiredConfirmationValidator());
		correctionForm.add(confirmationField);

		correctionForm.add(new Button("rejectSupport") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				rejectSupport(electionId, candidateId);
			}
		});
		correctionForm.add(new Button("approveSupport") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				correctRejectedSupport(electionId, candidateId, true);
			}
		});
		correctionForm.add(new Button("returnToProposal") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				correctRejectedSupport(electionId, candidateId, false);
			}
		});

		String unavailableMessage = correctionAllowed
				? getString("candidateSupportsNoActionable")
				: getString("candidateSupportsRejectionUnavailable");
		add(new Label("rejectionUnavailable", unavailableMessage)
				.setVisible(!correctionAllowed || actionableSupports.isEmpty()));

		add(new Link<Void>("back") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			}
		});
	}

	private void rejectSupport(long electionId, long candidateId) {
		try {
			boolean rejected = selectedSupport != null && AppContext.getInstance().getManagerBeanRemote().rejectCandidateSupport(
					candidateId,
					selectedSupport.getId(),
					correctionComment,
					SecurityUtils.getUserAdminId(),
					SecurityUtils.getClientIp());
			if (!rejected) {
				getSession().error(getString("candidateSupportsRejectError"));
				return;
			}
			getSession().info(getString("candidateSupportsRejectSuccess"));
			PageParameters responseParameters = UtilsParameters.getId(electionId);
			responseParameters.add(UtilsParameters.getCandidateText(), candidateId);
			setResponsePage(ManageCandidateSupportsDashboard.class, responseParameters);
		} catch (RuntimeException e) {
			appLogger.error("Could not reject candidate support. candidateId={}", candidateId, e);
			getSession().error(getString("candidateSupportsRejectError"));
		}
	}

	private void correctRejectedSupport(long electionId, long candidateId, boolean approve) {
		long supportNominationId = selectedSupport != null ? selectedSupport.getId() : 0L;
		try {
			boolean corrected = selectedSupport != null && (approve
					? AppContext.getInstance().getManagerBeanRemote().approveRejectedCandidateSupport(
							candidateId, supportNominationId, correctionComment, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp())
					: AppContext.getInstance().getManagerBeanRemote().returnRejectedCandidateSupportToProposal(
							candidateId, supportNominationId, correctionComment, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp()));
			if (!corrected) {
				getSession().error(getString(approve ? "candidateSupportsApproveError" : "candidateSupportsReturnError"));
				return;
			}
			getSession().info(getString(approve ? "candidateSupportsApproveSuccess" : "candidateSupportsReturnSuccess"));
			reloadDashboard(electionId, candidateId);
		} catch (RuntimeException e) {
			appLogger.error("Could not correct rejected candidate support. candidateId={}, supportNominationId={}", candidateId, supportNominationId, e);
			getSession().error(getString(approve ? "candidateSupportsApproveError" : "candidateSupportsReturnError"));
		}
	}

	private void reloadDashboard(long electionId, long candidateId) {
		PageParameters responseParameters = UtilsParameters.getId(electionId);
		responseParameters.add(UtilsParameters.getCandidateText(), candidateId);
		setResponsePage(ManageCandidateSupportsDashboard.class, responseParameters);
	}

	private boolean isValidCandidate(Candidate candidate, long electionId) {
		return candidate != null
				&& candidate.getElection() != null
				&& candidate.getElection().getElectionId() == electionId;
	}

	private boolean isCorrectionAllowed(Candidate candidate, Nomination nomination) {
		if (candidate == null || nomination == null || nomination.getCandidate() == null) {
			return false;
		}
		CandidateStatus status = candidate.getStatus();
		return nomination.getCandidate().getCandidateId() == candidate.getCandidateId()
				&& nomination.getStatus() == NominationStatus.ACCEPTED_BY_CANDIDATE
				&& status != CandidateStatus.COMPLETE
				&& status != CandidateStatus.REJECTED
				&& status != CandidateStatus.CONFIRMED_AND_PUBLISHED;
	}

	private List<SupportNomination> safeSupports(List<SupportNomination> supports) {
		return supports == null ? Collections.emptyList() : new ArrayList<>(supports);
	}

	private List<SupportNomination> filterSupportsWithStatus(List<SupportNomination> supports) {
		List<SupportNomination> actionable = new ArrayList<>();
		for (SupportNomination support : supports) {
			if (support != null && support.getSupportStatus() != null) {
				actionable.add(support);
			}
		}
		return actionable;
	}

	private String resolveSupportType(SupportNomination support) {
		return getString(support != null && support.getSupportingOrganization() != null
				? "candidateSupportsTypeOrganization"
				: "candidateSupportsTypeUser");
	}

	private String resolveSupportParty(SupportNomination support) {
		if (support == null) {
			return "-";
		}
		String contact = joinNonEmpty(support.getSupportingContactName(), support.getSupportingContactEmail());
		Organization organization = support.getSupportingOrganization();
		if (organization == null) {
			return valueOrDash(contact);
		}
		String organizationSummary = joinNonEmpty(organization.getOrgId(), organization.getName());
		return hasText(contact) ? organizationSummary + " — " + contact : valueOrDash(organizationSummary);
	}

	private String resolveSupportStatus(SupportStatus status) {
		return getString("candidateSupportsStatus" + (status != null ? status.name() : "Unknown"));
	}

	private String resolveCandidateStatus(CandidateStatus status) {
		return status != null ? status.name() : "-";
	}

	private String resolveTaskStatus(Map<ElectionTaskKey, CandidateElectionTaskStatus> statuses, ElectionTaskKey taskKey) {
		CandidateElectionTaskStatus status = statuses != null ? statuses.get(taskKey) : null;
		return status == null
				? getString("candidateSupportsTaskAbsent")
				: getString("candidateSupportsTaskStatus" + status.name());
	}

	private String formatResponseDate(SupportNomination support) {
		if (support == null || support.getSupportResponseInstant() == null) {
			return "-";
		}
		return new SimpleDateFormat("dd/MM/yyyy HH:mm", SecurityUtils.getLocale()).format(support.getSupportResponseInstant());
	}

	private String renderSupportChoice(SupportNomination support) {
		return support == null ? "" : "#" + support.getId() + " · " + resolveSupportStatus(support.getSupportStatus())
				+ " · " + resolveSupportType(support) + " · " + resolveSupportParty(support);
	}

	private String joinNonEmpty(String first, String second) {
		if (!hasText(first)) {
			return hasText(second) ? second.trim() : "";
		}
		return hasText(second) ? first.trim() + " · " + second.trim() : first.trim();
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value.trim() : "-";
	}

	private final IChoiceRenderer<SupportNomination> SUPPORT_RENDERER = new IChoiceRenderer<SupportNomination>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(SupportNomination object) {
			return renderSupportChoice(object);
		}

		@Override
		public String getIdValue(SupportNomination object, int index) {
			return object != null ? String.valueOf(object.getId()) : "";
		}
	};

	private static final class RequiredConfirmationValidator implements IValidator<Boolean> {
		private static final long serialVersionUID = 1L;

		@Override
		public void validate(IValidatable<Boolean> validatable) {
			if (!Boolean.TRUE.equals(validatable.getValue())) {
				validatable.error(new ValidationError().addKey("candidateSupportsConfirmationRequired"));
			}
		}
	}
}
