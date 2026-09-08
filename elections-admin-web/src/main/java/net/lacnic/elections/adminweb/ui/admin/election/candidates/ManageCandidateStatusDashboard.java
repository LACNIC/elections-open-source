package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EmailTemplatesDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.utils.EmailTemplateType;

public class ManageCandidateStatusDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private Candidate candidate;
	private CandidateStatus candidateStatus;
	private String comment;
	private String confirmedAndPublishedWarningMessage;

	public ManageCandidateStatusDashboard(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedback"));

		long electionId = UtilsParameters.getIdAsLong(params);
		long candidateId = UtilsParameters.getCandidateAsLong(params);
		candidate = AppContext.getInstance().getManagerBeanRemote().getCandidate(candidateId);

		if (!isValidCandidate(candidate, electionId)) {
			getSession().error(getString("candidateAnswersCandidateNotFound"));
			setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			return;
		}

		candidateStatus = candidate.getStatus();

		add(new Label("candidateName", valueOrDash(candidate.getName())));
		add(new Label("candidateId", String.valueOf(candidate.getCandidateId())));
		add(new Label("candidateMail", valueOrDash(candidate.getMail())));
		add(new Label("currentStatus", resolveStatusLabel(candidate.getStatus())));

		Form<Void> candidateStatusForm = new Form<>("candidateStatusForm");
		add(candidateStatusForm);

		List<CandidateStatus> statusOptions = Arrays.asList(CandidateStatus.values());
		DropDownChoice<CandidateStatus> candidateStatusField = new DropDownChoice<>(
				"candidateStatus",
				new PropertyModel<>(this, "candidateStatus"),
				statusOptions,
				CANDIDATE_STATUS_RENDERER);
		candidateStatusField.setRequired(true);
		candidateStatusForm.add(candidateStatusField);
		ExternalLink candidateConfirmedTemplateLink = new ExternalLink(
				"candidateConfirmedTemplateLink",
				resolveConfirmedAndPublishedTemplateUrl(electionId));
		candidateConfirmedTemplateLink.add(AttributeModifier.replace("target", "_blank"));
		candidateConfirmedTemplateLink.add(AttributeModifier.replace("rel", "noopener noreferrer"));
		candidateStatusForm.add(candidateConfirmedTemplateLink);

		confirmedAndPublishedWarningMessage = resolveConfirmedAndPublishedWarningMessage(
				electionId,
				candidate.getCandidateId(),
				candidateStatus);
		WebMarkupContainer confirmedAndPublishedWarning = new WebMarkupContainer("confirmedAndPublishedWarning");
		confirmedAndPublishedWarning.setOutputMarkupPlaceholderTag(true);
		confirmedAndPublishedWarning.setVisible(isConfirmedAndPublishedSelected(candidateStatus));
		WebMarkupContainer confirmedAndPublishedWarningMessageWrapper = new WebMarkupContainer("confirmedAndPublishedWarningMessageWrapper");
		confirmedAndPublishedWarningMessageWrapper.setOutputMarkupPlaceholderTag(true);
		confirmedAndPublishedWarningMessageWrapper.setVisible(hasText(confirmedAndPublishedWarningMessage));
		confirmedAndPublishedWarningMessageWrapper.add(new Label("confirmedAndPublishedWarningMessage", new PropertyModel<>(this, "confirmedAndPublishedWarningMessage")));
		confirmedAndPublishedWarning.add(confirmedAndPublishedWarningMessageWrapper);
		candidateStatusForm.add(confirmedAndPublishedWarning);

		candidateStatusField.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				confirmedAndPublishedWarningMessage = resolveConfirmedAndPublishedWarningMessage(
						electionId,
						candidate.getCandidateId(),
						candidateStatus);
				confirmedAndPublishedWarning.setVisible(isConfirmedAndPublishedSelected(candidateStatus));
				confirmedAndPublishedWarningMessageWrapper.setVisible(hasText(confirmedAndPublishedWarningMessage));
				target.add(confirmedAndPublishedWarning);
			}
		});

		TextArea<String> commentField = new TextArea<>("comment", new PropertyModel<>(this, "comment"));
		commentField.add(StringValidator.maximumLength(4000));
		candidateStatusForm.add(commentField);

		candidateStatusForm.add(new Button("saveWithEmail") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				persistCandidateStatusChange(electionId, true);
			}
		});

		candidateStatusForm.add(new Button("saveWithoutEmail") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				persistCandidateStatusChange(electionId, false);
			}
		});

		candidateStatusForm.add(new Link<Void>("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			}
		});
	}

	private boolean isValidCandidate(Candidate candidate, long electionId) {
		return candidate != null
				&& candidate.getElection() != null
				&& candidate.getElection().getElectionId() == electionId;
	}

	private void persistCandidateStatusChange(long electionId, boolean sendConfirmedAndPublishedEmail) {
		try {
			boolean updated = AppContext.getInstance().getManagerBeanRemote().updateCandidateStatus(
					candidate.getCandidateId(),
					candidateStatus,
					comment,
					sendConfirmedAndPublishedEmail,
					SecurityUtils.getUserAdminId(),
					SecurityUtils.getClientIp());
			if (!updated) {
				appLogger.warn("No se aplicó actualización de estado para candidateId={}, newStatus={}", candidate.getCandidateId(), candidateStatus);
			}
			getSession().info(getString("candidateStatusSaveSuccess"));
			setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			getSession().error(getString("candidateStatusSaveError"));
			setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
		}
	}

	private String resolveStatusLabel(CandidateStatus status) {
		return getString(resolveStatusKey(status));
	}

	private String resolveStatusKey(CandidateStatus status) {
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

	private boolean isConfirmedAndPublishedSelected(CandidateStatus status) {
		return status == CandidateStatus.CONFIRMED_AND_PUBLISHED;
	}

	private String resolveConfirmedAndPublishedTemplateUrl(long electionId) {
		CharSequence templatesUrl = urlFor(EmailTemplatesDashboard.class, UtilsParameters.getId(electionId));
		return templatesUrl.toString() + "#" + resolveTemplateAnchorId(EmailTemplateType.CANDIDATE_CONFIRMED_AND_PUBLISHED.getKey());
	}

	private String resolveTemplateAnchorId(String templateType) {
		if (!hasText(templateType)) {
			return "template";
		}
		return "template-" + templateType.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
	}

	private String resolveConfirmedAndPublishedWarningMessage(long electionId, long candidateId, CandidateStatus status) {
		if (status != CandidateStatus.CONFIRMED_AND_PUBLISHED) {
			return null;
		}

		AuditorApprovalSummary summary = resolveAuditorApprovalSummary(electionId, candidateId);
		if (summary.getApproved() >= summary.getTotal() && summary.getTotal() > 0) {
			return null;
		}
		return new StringResourceModel("candidateStatusSaveConfirmedAndPublishedAuditSummary", this, null)
				.setParameters(summary.getApproved(), summary.getTotal())
				.getString();
	}

	private AuditorApprovalSummary resolveAuditorApprovalSummary(long electionId, long candidateId) {
		Set<Long> requiredAuditorIds = resolveRequiredAuditorIds(electionId);
		long approvedAuditors = resolveApprovedAuditors(electionId, candidateId, requiredAuditorIds);
		return new AuditorApprovalSummary(approvedAuditors, requiredAuditorIds.size());
	}

	private Set<Long> resolveRequiredAuditorIds(long electionId) {
		Set<Long> requiredAuditorIds = new HashSet<>();
		try {
			List<Auditor> auditors = AppContext.getInstance().getManagerBeanRemote().getElectionAuditors(electionId);
			if (auditors == null) {
				return requiredAuditorIds;
			}
			for (Auditor auditor : auditors) {
				if (auditor != null && auditor.isCommissioner()) {
					requiredAuditorIds.add(auditor.getAuditorId());
				}
			}
		} catch (Exception e) {
			appLogger.warn("No se pudo resolver auditores requeridos para electionId={}", electionId, e);
		}
		return requiredAuditorIds;
	}

	private long resolveApprovedAuditors(long electionId, long candidateId, Set<Long> requiredAuditorIds) {
		if (requiredAuditorIds == null || requiredAuditorIds.isEmpty()) {
			return 0L;
		}
		try {
			List<AuditorCandidateDecision> decisions = AppContext.getInstance().getManagerBeanRemote().getElectionAuditorCandidateDecisions(electionId);
			Set<Long> approvedAuditorIds = new HashSet<>();
			for (AuditorCandidateDecision decision : decisions) {
				if (decision == null) {
					continue;
				}
				Long decisionCandidateId = readEntityLongField(readFieldValue(decision, "candidate"), "candidateId");
				if (decisionCandidateId == null || decisionCandidateId.longValue() != candidateId) {
					continue;
				}
				Object statusValue = readFieldValue(decision, "finalDecisionStatus");
				if (!(statusValue instanceof AuditorCandidateDecisionStatus)) {
					statusValue = readFieldValue(decision, "decisionStatus");
				}
				if (!(statusValue instanceof AuditorCandidateDecisionStatus)
						|| statusValue != AuditorCandidateDecisionStatus.APPROVED) {
					continue;
				}
				Long auditorId = readEntityLongField(readFieldValue(decision, "auditor"), "auditorId");
				if (auditorId != null && requiredAuditorIds.contains(auditorId)) {
					approvedAuditorIds.add(auditorId);
				}
			}
			return approvedAuditorIds.size();
		} catch (Exception e) {
			appLogger.warn("No se pudo resolver aprobaciones de auditoría para electionId={}, candidateId={}", electionId, candidateId, e);
			return 0L;
		}
	}

	private Object readFieldValue(Object instance, String fieldName) {
		if (instance == null || fieldName == null) {
			return null;
		}
		try {
			java.lang.reflect.Field field = instance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(instance);
		} catch (Exception e) {
			return null;
		}
	}

	private Long readEntityLongField(Object instance, String fieldName) {
		Object value = readFieldValue(instance, fieldName);
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		return null;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private final IChoiceRenderer<CandidateStatus> CANDIDATE_STATUS_RENDERER = new IChoiceRenderer<CandidateStatus>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(CandidateStatus object) {
			return resolveStatusLabel(object);
		}

		@Override
		public String getIdValue(CandidateStatus object, int index) {
			return object != null ? object.name() : "";
		}
	};

	public CandidateStatus getCandidateStatus() {
		return candidateStatus;
	}

	public void setCandidateStatus(CandidateStatus candidateStatus) {
		this.candidateStatus = candidateStatus;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	private static final class AuditorApprovalSummary {
		private final long approved;
		private final long total;

		private AuditorApprovalSummary(long approved, long total) {
			this.approved = approved;
			this.total = total;
		}

		private long getApproved() {
			return approved;
		}

		private long getTotal() {
			return total;
		}
	}
}
