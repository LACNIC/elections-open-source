package net.lacnic.elections.adminweb.ui.admin.election.auditors;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.ReminderFrequency;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStage;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;


public class EditAuditorDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = -4584362258132685785L;
	private static final int MAX_DECISION_COMMENT_LENGTH = 4000;
	private static final List<AuditorCandidateDecisionStatus> PRE_DECISION_STATUS_OPTIONS = Arrays.asList(
			AuditorCandidateDecisionStatus.PREAPPROVED,
			AuditorCandidateDecisionStatus.REJECTED);
	private static final List<AuditorCandidateDecisionStatus> FINAL_DECISION_STATUS_OPTIONS = Arrays.asList(
			AuditorCandidateDecisionStatus.APPROVED,
			AuditorCandidateDecisionStatus.REJECTED);

	private Auditor auditor;
	private String name;
	private String mail;
	private boolean commissioner;
	private ReminderFrequency reminderFrequency;


	public EditAuditorDashboard(PageParameters params) {
		super(params);

		long auditorId = UtilsParameters.getAuditAsLong(params);
		auditor = AppContext.getInstance().getManagerBeanRemote().getAuditor(auditorId);
		mail = auditor.getMail();
		name = auditor.getName();
		commissioner = auditor.isCommissioner();
		reminderFrequency = auditor.getReminderFrequency();

		add(new FeedbackPanel("feedback"));
		Form<Void> auditorForm = new Form<>("auditorForm");
		add(auditorForm);

		TextField<String> nameTextField = new TextField<>("name", new PropertyModel<>(auditor, "name"));
		nameTextField.setRequired(true);
		nameTextField.add(StringValidator.maximumLength(255));
		auditorForm.add(nameTextField);

		final EmailTextField mailTextField = new EmailTextField("mail", new PropertyModel<>(auditor, "mail"));
		mailTextField.setRequired(true);
		mailTextField.add(StringValidator.maximumLength(40));
		auditorForm.add(mailTextField);

		DropDownChoice<ReminderFrequency> reminderFrequencyField = new DropDownChoice<>(
				"reminderFrequency",
				new PropertyModel<>(auditor, "reminderFrequency"),
				Arrays.asList(ReminderFrequency.values()),
				REMINDER_FREQUENCY_RENDERER);
		reminderFrequencyField.setNullValid(false);
		reminderFrequencyField.setRequired(true);
		auditorForm.add(reminderFrequencyField);

		auditorForm.add(new CheckBox("commissionerCheckbox", new PropertyModel<>(auditor, "commissioner")));

		auditorForm.add(new Button("save") {
			private static final long serialVersionUID = 5845761991376797119L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				// Save only if something changed
				if (!(mail.equalsIgnoreCase(auditor.getMail()))
						|| !(name.equalsIgnoreCase(auditor.getName()))
						|| !(commissioner == auditor.isCommissioner())
						|| reminderFrequency != auditor.getReminderFrequency()) {
					AppContext.getInstance().getManagerBeanRemote().editAuditor(getAuditor(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					getSession().info(getString("auditorEditSuccess"));
				}
				setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(auditor.getElection().getElectionId()));
			}
		});

		auditorForm.add(new Link<Void>("cancel") {
			private static final long serialVersionUID = -8699657879088465106L;

			@Override
			public void onClick() {
				setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(auditor.getElection().getElectionId()));
			}
		});

		add(buildAuditorDecisionsCard());
	}

	private WebMarkupContainer buildAuditorDecisionsCard() {
		List<AuditorDecisionEditRow> decisionRows = buildAuditorDecisionRows();
		WebMarkupContainer card = new WebMarkupContainer("auditorDecisionsCard");
		card.setVisible(auditor != null && auditor.isCommissioner());

		Label noDecisionRows = new Label("noDecisionRows", getString("auditorEditDecisionsEmpty"));
		noDecisionRows.setVisible(decisionRows.isEmpty());
		card.add(noDecisionRows);

		WebMarkupContainer rowsContainer = new WebMarkupContainer("auditorDecisionRowsContainer");
		rowsContainer.setVisible(!decisionRows.isEmpty());
		card.add(rowsContainer);

		rowsContainer.add(new ListView<AuditorDecisionEditRow>("auditorDecisionRows", decisionRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AuditorDecisionEditRow> item) {
				AuditorDecisionEditRow row = item.getModelObject();
				Form<Void> decisionForm = new Form<>("decisionForm");
				item.add(decisionForm);

				decisionForm.add(new Label("candidateName", row.getCandidateName()));
				decisionForm.add(new Label("candidateStatus", row.getCandidateStatusLabel()));

				WebMarkupContainer preDecisionContainer = new WebMarkupContainer("preDecisionContainer");
				preDecisionContainer.setVisible(row.isPreDecisionEditable());
				decisionForm.add(preDecisionContainer);
				DropDownChoice<AuditorCandidateDecisionStatus> preDecisionStatus = new DropDownChoice<>(
						"preDecisionStatus",
						new PropertyModel<>(row, "preDecisionStatus"),
						PRE_DECISION_STATUS_OPTIONS,
						AUDITOR_DECISION_STATUS_RENDERER);
				preDecisionStatus.setRequired(true);
				preDecisionContainer.add(preDecisionStatus);
				TextArea<String> preDecisionComment = new TextArea<>("preDecisionComment", new PropertyModel<>(row, "preDecisionComment"));
				preDecisionComment.add(StringValidator.maximumLength(MAX_DECISION_COMMENT_LENGTH));
				preDecisionContainer.add(preDecisionComment);
				preDecisionContainer.add(new Label("preDecisionDate", row.getPreDecisionDateLabel()));

				WebMarkupContainer finalDecisionContainer = new WebMarkupContainer("finalDecisionContainer");
				finalDecisionContainer.setVisible(row.isFinalDecisionEditable());
				decisionForm.add(finalDecisionContainer);
				DropDownChoice<AuditorCandidateDecisionStatus> finalDecisionStatus = new DropDownChoice<>(
						"finalDecisionStatus",
						new PropertyModel<>(row, "finalDecisionStatus"),
						FINAL_DECISION_STATUS_OPTIONS,
						AUDITOR_DECISION_STATUS_RENDERER);
				finalDecisionStatus.setRequired(true);
				finalDecisionContainer.add(finalDecisionStatus);
				TextArea<String> finalDecisionComment = new TextArea<>("finalDecisionComment", new PropertyModel<>(row, "finalDecisionComment"));
				finalDecisionComment.add(StringValidator.maximumLength(MAX_DECISION_COMMENT_LENGTH));
				finalDecisionContainer.add(finalDecisionComment);
				finalDecisionContainer.add(new Label("finalDecisionDate", row.getFinalDecisionDateLabel()));

				decisionForm.add(new Button("saveDecision") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onSubmit() {
						saveAuditorDecisionRow(row);
					}
				});
			}
		});

		return card;
	}

	private List<AuditorDecisionEditRow> buildAuditorDecisionRows() {
		if (auditor == null || auditor.getElection() == null || !auditor.isCommissioner()) {
			return Collections.emptyList();
		}

		long electionId = auditor.getElection().getElectionId();
		List<Candidate> candidates = AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(electionId);
		List<AuditorCandidateDecision> decisions = AppContext.getInstance().getManagerBeanRemote().getElectionAuditorCandidateDecisions(electionId);
		Map<Long, AuditorCandidateDecision> decisionByCandidateId = new HashMap<>();
		for (AuditorCandidateDecision decision : decisions != null ? decisions : Collections.<AuditorCandidateDecision>emptyList()) {
			if (decision == null || decision.getAuditor() == null || decision.getCandidate() == null) {
				continue;
			}
			if (decision.getAuditor().getAuditorId() != auditor.getAuditorId()) {
				continue;
			}
			decisionByCandidateId.put(Long.valueOf(decision.getCandidate().getCandidateId()), decision);
		}

		List<AuditorDecisionEditRow> rows = new ArrayList<>();
		for (Candidate candidate : candidates != null ? candidates : Collections.<Candidate>emptyList()) {
			if (candidate == null || candidate.isAbstention()) {
				continue;
			}
			AuditorCandidateDecision decision = decisionByCandidateId.get(Long.valueOf(candidate.getCandidateId()));
			if (decision == null) {
				continue;
			}
			AuditorDecisionEditRow row = buildAuditorDecisionRow(candidate, decision);
			if (row.hasEditableDecision()) {
				rows.add(row);
			}
		}
		return rows;
	}

	private AuditorDecisionEditRow buildAuditorDecisionRow(Candidate candidate, AuditorCandidateDecision decision) {
		StageDecision preDecision = resolvePreDecision(decision, candidate.getStatus());
		StageDecision finalDecision = resolveFinalDecision(decision);
		return new AuditorDecisionEditRow(
				candidate.getCandidateId(),
				valueOrDash(candidate.getName()),
				resolveCandidateStatusLabel(candidate.getStatus()),
				preDecision.getStatus(),
				preDecision.getComment(),
				formatDateTime(preDecision.getDate()),
				finalDecision.getStatus(),
				finalDecision.getComment(),
				formatDateTime(finalDecision.getDate()));
	}

	private StageDecision resolvePreDecision(AuditorCandidateDecision decision, CandidateStatus candidateStatus) {
		if (decision == null) {
			return StageDecision.empty();
		}
		AuditorCandidateDecisionStatus status = decision.getPreDecisionStatus();
		Date date = decision.getPreDecisionDate();
		String comment = decision.getPreDecisionComment();
		if (status != null && !isPreVerificationStatus(status)) {
			status = null;
			date = null;
			comment = null;
		}
		if (status == null) {
			AuditorCandidateDecisionStatus legacyStatus = decision.getDecisionStatus();
			if (legacyStatus == AuditorCandidateDecisionStatus.PREAPPROVED) {
				status = AuditorCandidateDecisionStatus.PREAPPROVED;
				date = firstNonNullDate(decision.getPreapprovedDate(), decision.getDecisionDate());
			} else if (legacyStatus == AuditorCandidateDecisionStatus.REJECTED) {
				if (candidateStatus == CandidateStatus.PRECOMPLETE
						|| decision.getApprovedDate() == null
						|| isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate())) {
					status = AuditorCandidateDecisionStatus.REJECTED;
					date = firstNonNullDate(decision.getPreapprovedDate(), decision.getDecisionDate());
				} else if (decision.getPreapprovedDate() != null) {
					status = AuditorCandidateDecisionStatus.PREAPPROVED;
					date = decision.getPreapprovedDate();
				}
			} else if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED && decision.getPreapprovedDate() != null) {
				status = AuditorCandidateDecisionStatus.PREAPPROVED;
				date = decision.getPreapprovedDate();
			}
		}
		return new StageDecision(status, date, comment);
	}

	private StageDecision resolveFinalDecision(AuditorCandidateDecision decision) {
		if (decision == null) {
			return StageDecision.empty();
		}
		AuditorCandidateDecisionStatus status = decision.getFinalDecisionStatus();
		Date date = decision.getFinalDecisionDate();
		String comment = decision.getFinalDecisionComment();
		if (status != null && !isFinalVerificationStatus(status)) {
			status = null;
			date = null;
			comment = null;
		}
		if (status == null) {
			AuditorCandidateDecisionStatus legacyStatus = decision.getDecisionStatus();
			if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED) {
				status = AuditorCandidateDecisionStatus.APPROVED;
				date = firstNonNullDate(decision.getApprovedDate(), decision.getDecisionDate());
			} else if (legacyStatus == AuditorCandidateDecisionStatus.REJECTED
					&& decision.getApprovedDate() != null
					&& !isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate())) {
				status = AuditorCandidateDecisionStatus.REJECTED;
				date = firstNonNullDate(decision.getApprovedDate(), decision.getDecisionDate());
			}
		}
		return new StageDecision(status, date, comment);
	}

	private void saveAuditorDecisionRow(AuditorDecisionEditRow row) {
		if (row == null || auditor == null) {
			getSession().error(getString("auditorEditDecisionSaveError"));
			return;
		}

		boolean hasChanges = false;
		boolean saved = true;
		if (row.isPreDecisionChanged()) {
			hasChanges = true;
			saved = updateAuditorDecisionStage(row, AuditorCandidateDecisionStage.PRE_VERIFICATION, row.getPreDecisionStatus(), row.getPreDecisionComment());
		}
		if (saved && row.isFinalDecisionChanged()) {
			hasChanges = true;
			saved = updateAuditorDecisionStage(row, AuditorCandidateDecisionStage.FINAL_VERIFICATION, row.getFinalDecisionStatus(), row.getFinalDecisionComment());
		}

		if (!hasChanges) {
			getSession().info(getString("auditorEditDecisionNoChanges"));
			return;
		}
		if (saved) {
			getSession().info(getString("auditorEditDecisionSaveSuccess"));
			setResponsePage(EditAuditorDashboard.class, UtilsParameters.getAudit(auditor.getAuditorId()));
		} else {
			getSession().error(getString("auditorEditDecisionSaveError"));
		}
	}

	private boolean updateAuditorDecisionStage(AuditorDecisionEditRow row, AuditorCandidateDecisionStage stage, AuditorCandidateDecisionStatus status, String comment) {
		if (row == null || stage == null || status == null) {
			return false;
		}
		return AppContext.getInstance().getManagerBeanRemote().updateAuditorCandidateDecisionStageStatus(
				auditor.getAuditorId(),
				row.getCandidateId(),
				stage,
				status,
				SecurityUtils.getUserAdminId(),
				SecurityUtils.getClientIp(),
				comment);
	}

	private boolean isPreVerificationStatus(AuditorCandidateDecisionStatus status) {
		return status == AuditorCandidateDecisionStatus.PREAPPROVED
				|| status == AuditorCandidateDecisionStatus.REJECTED;
	}

	private boolean isFinalVerificationStatus(AuditorCandidateDecisionStatus status) {
		return status == AuditorCandidateDecisionStatus.APPROVED
				|| status == AuditorCandidateDecisionStatus.REJECTED;
	}

	private boolean isLikelyLegacyPrecompleteRejectedMilestone(Date preapprovedDate, Date approvedDate) {
		if (preapprovedDate == null || approvedDate == null) {
			return false;
		}
		long diffMillis = Math.abs(preapprovedDate.getTime() - approvedDate.getTime());
		return diffMillis <= 1000L;
	}

	private Date firstNonNullDate(Date... dates) {
		if (dates == null) {
			return null;
		}
		for (Date date : dates) {
			if (date != null) {
				return date;
			}
		}
		return null;
	}

	private String formatDateTime(Date date) {
		if (date == null) {
			return "-";
		}
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, SecurityUtils.getLocale());
		return formatter.format(date);
	}

	private String resolveCandidateStatusLabel(CandidateStatus status) {
		return getString(resolveCandidateStatusKey(status));
	}

	private String resolveCandidateStatusKey(CandidateStatus status) {
		if (status == null) {
			return "candidateManagemenListCampusNoStatus";
		}
		switch (status) {
		case INCOMPLETE:
			return "candidateStatusOptionIncomplete";
		case PRECOMPLETE:
			return "candidateStatusOptionPreComplete";
		case COMPLETE:
			return "candidateStatusOptionComplete";
		case REJECTED:
			return "candidateStatusOptionRejected";
		case CONFIRMED_AND_PUBLISHED:
			return "candidateStatusOptionConfirmedAndPublished";
		default:
			return "candidateManagemenListCampusNoStatus";
		}
	}

	private String resolveDecisionStatusResourceKey(AuditorCandidateDecisionStatus status) {
		if (status == null) {
			return "candidateManagemenAuditorProgressNoDecision";
		}
		switch (status) {
		case PREAPPROVED:
			return "auditPublicV2DecisionPreApproved";
		case APPROVED:
			return "auditPublicV2DecisionApproved";
		case REJECTED:
			return "auditPublicV2DecisionRejected";
		case ANALYZING:
			return "auditPublicV2DecisionPending";
		case NO_APPLY:
			return "auditPublicV2DecisionNoApply";
		default:
			return "candidateManagemenAuditorProgressNoDecision";
		}
	}

	private String valueOrDash(String value) {
		return value != null && !value.trim().isEmpty() ? value : "-";
	}

	private final IChoiceRenderer<ReminderFrequency> REMINDER_FREQUENCY_RENDERER = new IChoiceRenderer<ReminderFrequency>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(ReminderFrequency object) {
			return getString("reminderFrequency." + object.name());
		}

		@Override
		public String getIdValue(ReminderFrequency object, int index) {
			return object.name();
		}
	};

	private final IChoiceRenderer<AuditorCandidateDecisionStatus> AUDITOR_DECISION_STATUS_RENDERER = new IChoiceRenderer<AuditorCandidateDecisionStatus>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(AuditorCandidateDecisionStatus object) {
			return getString(resolveDecisionStatusResourceKey(object));
		}

		@Override
		public String getIdValue(AuditorCandidateDecisionStatus object, int index) {
			return object.name();
		}
	};

	private static final class StageDecision implements Serializable {
		private static final long serialVersionUID = 1L;
		private final AuditorCandidateDecisionStatus status;
		private final Date date;
		private final String comment;

		private StageDecision(AuditorCandidateDecisionStatus status, Date date, String comment) {
			this.status = status;
			this.date = date;
			this.comment = comment;
		}

		private static StageDecision empty() {
			return new StageDecision(null, null, null);
		}

		private AuditorCandidateDecisionStatus getStatus() {
			return status;
		}

		private Date getDate() {
			return date;
		}

		private String getComment() {
			return comment;
		}
	}

	private static final class AuditorDecisionEditRow implements Serializable {
		private static final long serialVersionUID = 1L;
		private final long candidateId;
		private final String candidateName;
		private final String candidateStatusLabel;
		private final AuditorCandidateDecisionStatus originalPreDecisionStatus;
		private final String originalPreDecisionComment;
		private final String preDecisionDateLabel;
		private final AuditorCandidateDecisionStatus originalFinalDecisionStatus;
		private final String originalFinalDecisionComment;
		private final String finalDecisionDateLabel;
		private AuditorCandidateDecisionStatus preDecisionStatus;
		private String preDecisionComment;
		private AuditorCandidateDecisionStatus finalDecisionStatus;
		private String finalDecisionComment;

		private AuditorDecisionEditRow(
				long candidateId,
				String candidateName,
				String candidateStatusLabel,
				AuditorCandidateDecisionStatus preDecisionStatus,
				String preDecisionComment,
				String preDecisionDateLabel,
				AuditorCandidateDecisionStatus finalDecisionStatus,
				String finalDecisionComment,
				String finalDecisionDateLabel) {
			this.candidateId = candidateId;
			this.candidateName = candidateName;
			this.candidateStatusLabel = candidateStatusLabel;
			this.originalPreDecisionStatus = preDecisionStatus;
			this.originalPreDecisionComment = valueOrEmpty(preDecisionComment);
			this.preDecisionStatus = preDecisionStatus;
			this.preDecisionComment = valueOrEmpty(preDecisionComment);
			this.preDecisionDateLabel = preDecisionDateLabel;
			this.originalFinalDecisionStatus = finalDecisionStatus;
			this.originalFinalDecisionComment = valueOrEmpty(finalDecisionComment);
			this.finalDecisionStatus = finalDecisionStatus;
			this.finalDecisionComment = valueOrEmpty(finalDecisionComment);
			this.finalDecisionDateLabel = finalDecisionDateLabel;
		}

		public long getCandidateId() {
			return candidateId;
		}

		public String getCandidateName() {
			return candidateName;
		}

		public String getCandidateStatusLabel() {
			return candidateStatusLabel;
		}

		public AuditorCandidateDecisionStatus getPreDecisionStatus() {
			return preDecisionStatus;
		}

		public void setPreDecisionStatus(AuditorCandidateDecisionStatus preDecisionStatus) {
			this.preDecisionStatus = preDecisionStatus;
		}

		public String getPreDecisionComment() {
			return preDecisionComment;
		}

		public void setPreDecisionComment(String preDecisionComment) {
			this.preDecisionComment = valueOrEmpty(preDecisionComment);
		}

		public String getPreDecisionDateLabel() {
			return preDecisionDateLabel;
		}

		public AuditorCandidateDecisionStatus getFinalDecisionStatus() {
			return finalDecisionStatus;
		}

		public void setFinalDecisionStatus(AuditorCandidateDecisionStatus finalDecisionStatus) {
			this.finalDecisionStatus = finalDecisionStatus;
		}

		public String getFinalDecisionComment() {
			return finalDecisionComment;
		}

		public void setFinalDecisionComment(String finalDecisionComment) {
			this.finalDecisionComment = valueOrEmpty(finalDecisionComment);
		}

		public String getFinalDecisionDateLabel() {
			return finalDecisionDateLabel;
		}

		private boolean hasEditableDecision() {
			return isPreDecisionEditable() || isFinalDecisionEditable();
		}

		private boolean isPreDecisionEditable() {
			return originalPreDecisionStatus != null;
		}

		private boolean isFinalDecisionEditable() {
			return originalFinalDecisionStatus != null;
		}

		private boolean isPreDecisionChanged() {
			return isPreDecisionEditable()
					&& (preDecisionStatus != originalPreDecisionStatus
							|| !normalizeText(preDecisionComment).equals(normalizeText(originalPreDecisionComment)));
		}

		private boolean isFinalDecisionChanged() {
			return isFinalDecisionEditable()
					&& (finalDecisionStatus != originalFinalDecisionStatus
							|| !normalizeText(finalDecisionComment).equals(normalizeText(originalFinalDecisionComment)));
		}

		private static String valueOrEmpty(String value) {
			return value != null ? value : "";
		}

		private static String normalizeText(String value) {
			return value != null ? value.trim() : "";
		}
	}


	public Auditor getAuditor() {
		return auditor;
	}

	public void setAuditor(Auditor auditor) {
		this.auditor = auditor;
	}

}
