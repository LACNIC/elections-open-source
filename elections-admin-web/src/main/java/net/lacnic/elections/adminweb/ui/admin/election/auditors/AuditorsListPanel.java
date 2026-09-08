package net.lacnic.elections.adminweb.ui.admin.election.auditors;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.ui.components.ButtonViewLink;
import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.utils.LinksUtils;

public class AuditorsListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	public AuditorsListPanel(String id, Election election) {
		super(id);
		try {
			List<Auditor> auditors = AppContext.getInstance().getManagerBeanRemote().getElectionAuditors(election.getElectionId());
			List<AuditorCandidateDecision> decisions = AppContext.getInstance().getManagerBeanRemote().getElectionAuditorCandidateDecisions(election.getElectionId());
			Set<Long> countedCandidateIds = new HashSet<>();
			for (Candidate candidate : AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(election.getElectionId())) {
				if (candidate != null && !candidate.isAbstention()) {
					countedCandidateIds.add(candidate.getCandidateId());
				}
			}
			Map<Long, AuditorDecisionSummary> summaryByAuditor = buildSummaryByAuditor(decisions, countedCandidateIds);
			ListView<Auditor> auditorsDataView = new ListView<Auditor>("auditorsList", auditors) {

				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(final ListItem<Auditor> item) {
					final Auditor currentAuditor = item.getModelObject();

					item.add(new Label("name", currentAuditor.getName()));
					item.add(new Label("mail", currentAuditor.getMail()));
					item.add(new Label("isCommissioner", (currentAuditor.isCommissioner() ? getString("auditorManagementCommissionerYes") : getString("auditorManagementCommissionerNo"))));
					item.add(new Label("agreedConformity", (currentAuditor.isCommissioner() ? (currentAuditor.isAgreedConformity() ? getString("auditorManagementCommissionerYes") : getString("auditorManagementCommissionerNo")) : "-")));

					AuditorDecisionSummary summary = summaryByAuditor.get(currentAuditor.getAuditorId());
					if (summary == null) {
						summary = new AuditorDecisionSummary();
					}
					Label preverificationSummary;
					Label finalVerificationSummary;
					if (currentAuditor.isCommissioner()) {
						preverificationSummary = new Label("preverificationSummary",
								summary.toPreverificationHtml(
										getString("auditorManagementResponsesTotal"),
										getString("auditorManagementResponsesPreapproved"),
										getString("auditorManagementResponsesRejected")));
						preverificationSummary.setEscapeModelStrings(false);
						finalVerificationSummary = new Label("finalVerificationSummary",
								summary.toFinalVerificationHtml(
										getString("auditorManagementResponsesTotal"),
										getString("auditorManagementResponsesApproved"),
										getString("auditorManagementResponsesRejected")));
						finalVerificationSummary.setEscapeModelStrings(false);
					} else {
						preverificationSummary = new Label("preverificationSummary", "-");
						finalVerificationSummary = new Label("finalVerificationSummary", "-");
					}
					item.add(preverificationSummary);
					item.add(finalVerificationSummary);

					String auditorLinkText = LinksUtils.buildAuditorResultsLink(currentAuditor.getResultToken());
					String userAdminId = SecurityUtils.getUserAdminId();
					String activityDescription = userAdminId.toUpperCase() + " vió el link de auditoría de resultados del auditor " + currentAuditor.getName() + " en la elección " + election.getTitleSpanish();
					ButtonViewLink viewLinkButton = new ButtonViewLink("viewLinkButton", currentAuditor.getAuditorId(), auditorLinkText) {
						private static final long serialVersionUID = 3666243113529801997L;

						@Override
						public void registerActivity() {
							AppContext.getInstance().getManagerBeanRemote().persistActivity(SecurityUtils.getUserAdminId(), ActivityType.VIEW_LINK_AUDIT, activityDescription, SecurityUtils.getClientIp(), currentAuditor.getElection().getElectionId());
						}
					};
					item.add(viewLinkButton);

					Link<Void> sendReminder = new Link<Void>("sendReminder") {
						private static final long serialVersionUID = -257830044561861884L;

						@Override
						public void onClick() {
							boolean queued = AppContext.getInstance().getManagerBeanRemote().sendAuditorReminder(
									election.getElectionId(),
									currentAuditor.getAuditorId(),
									SecurityUtils.getUserAdminId(),
									SecurityUtils.getClientIp());
							if (queued) {
								getSession().info(getString("auditorManagementReminderSuccess"));
							} else {
								getSession().error(getString("auditorManagementReminderError"));
							}
							setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					sendReminder.setVisible(currentAuditor.isCommissioner());
					item.add(sendReminder);

					Link<Void> sendRevisionReminder = new Link<Void>("sendRevisionReminder") {
						private static final long serialVersionUID = 420813317785539830L;

						@Override
						public void onClick() {
							boolean queued = AppContext.getInstance().getManagerBeanRemote().sendAuditorRevisionReminder(
									election.getElectionId(),
									currentAuditor.getAuditorId(),
									SecurityUtils.getUserAdminId(),
									SecurityUtils.getClientIp());
							if (queued) {
								getSession().info(getString("auditorManagementRevisionReminderSuccess"));
							} else {
								getSession().error(getString("auditorManagementRevisionReminderError"));
							}
							setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					sendRevisionReminder.setVisible(currentAuditor.isCommissioner() && !currentAuditor.isAgreedConformity());
					item.add(sendRevisionReminder);

					Link<Void> editAuditor = new Link<Void>("editAuditor") {
						private static final long serialVersionUID = -2734403145438500636L;

						@Override
						public void onClick() {
							setResponsePage(EditAuditorDashboard.class, UtilsParameters.getAudit(currentAuditor.getAuditorId()));
						}
					};
					editAuditor.setMarkupId("editAuditor" + item.getIndex());
					item.add(editAuditor);

					ButtonDeleteWithConfirmation buttonDeleteWithConfirmation = new ButtonDeleteWithConfirmation("removeAuditor", currentAuditor.getAuditorId()) {
						private static final long serialVersionUID = 4479213289810959012L;

						@Override
						public void onConfirm() {
							SecurityUtils.info(getString("auditorManagementSuccessDel"));
							AppContext.getInstance().getManagerBeanRemote().removeAuditor(currentAuditor.getAuditorId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
							setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					buttonDeleteWithConfirmation.setMarkupId("removeAuditor" + item.getIndex());
					item.add(buttonDeleteWithConfirmation);
				}
			};
			add(auditorsDataView);

		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

	private Map<Long, AuditorDecisionSummary> buildSummaryByAuditor(List<AuditorCandidateDecision> decisions, Set<Long> countedCandidateIds) {
		Map<Long, AuditorDecisionSummary> summaryByAuditor = new HashMap<>();
		if (decisions == null || decisions.isEmpty()) {
			return summaryByAuditor;
		}
		for (AuditorCandidateDecision decision : decisions) {
			if (decision == null || decision.getAuditor() == null || decision.getCandidate() == null) {
				continue;
			}
			Long auditorId = decision.getAuditor().getAuditorId();
			Long candidateId = decision.getCandidate().getCandidateId();
			if (auditorId == null || candidateId == null || !countedCandidateIds.contains(candidateId)) {
				continue;
			}
			AuditorCandidateDecisionStatus preverificationStatus = resolvePreverificationStatus(decision, decision.getCandidate().getStatus());
			AuditorCandidateDecisionStatus finalVerificationStatus = resolveFinalVerificationStatus(decision);
			if (preverificationStatus == null && finalVerificationStatus == null) {
				continue;
			}
			AuditorDecisionSummary summary = summaryByAuditor.computeIfAbsent(auditorId, key -> new AuditorDecisionSummary());
			summary.incrementPreverification(preverificationStatus);
			summary.incrementFinalVerification(finalVerificationStatus);
		}
		return summaryByAuditor;
	}

	private AuditorCandidateDecisionStatus resolvePreverificationStatus(AuditorCandidateDecision decision, CandidateStatus candidateStatus) {
		if (decision == null) {
			return null;
		}
		AuditorCandidateDecisionStatus preDecisionStatus = decision.getPreDecisionStatus();
		if (preDecisionStatus != null) {
			return isPreverificationStatus(preDecisionStatus) ? preDecisionStatus : null;
		}

		AuditorCandidateDecisionStatus legacyStatus = decision.getDecisionStatus();
		if (legacyStatus == AuditorCandidateDecisionStatus.PREAPPROVED) {
			return AuditorCandidateDecisionStatus.PREAPPROVED;
		}
		if (legacyStatus == AuditorCandidateDecisionStatus.REJECTED) {
			if (candidateStatus == CandidateStatus.PRECOMPLETE
					|| decision.getApprovedDate() == null
					|| isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate())) {
				return AuditorCandidateDecisionStatus.REJECTED;
			}
			if (decision.getPreapprovedDate() != null) {
				return AuditorCandidateDecisionStatus.PREAPPROVED;
			}
		}
		if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED && decision.getPreapprovedDate() != null) {
			return AuditorCandidateDecisionStatus.PREAPPROVED;
		}
		return null;
	}

	private AuditorCandidateDecisionStatus resolveFinalVerificationStatus(AuditorCandidateDecision decision) {
		if (decision == null) {
			return null;
		}
		AuditorCandidateDecisionStatus finalDecisionStatus = decision.getFinalDecisionStatus();
		if (finalDecisionStatus != null) {
			return isFinalVerificationStatus(finalDecisionStatus) ? finalDecisionStatus : null;
		}
		AuditorCandidateDecisionStatus legacyStatus = decision.getDecisionStatus();
		if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED) {
			return AuditorCandidateDecisionStatus.APPROVED;
		}
		if (legacyStatus == AuditorCandidateDecisionStatus.REJECTED
				&& decision.getApprovedDate() != null
				&& !isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate())) {
			return AuditorCandidateDecisionStatus.REJECTED;
		}
		return null;
	}

	private boolean isPreverificationStatus(AuditorCandidateDecisionStatus status) {
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

	private static class AuditorDecisionSummary implements Serializable {
		private static final long serialVersionUID = 1L;
		private int preapproved;
		private int preRejected;
		private int finalApproved;
		private int finalRejected;

		void incrementPreverification(AuditorCandidateDecisionStatus status) {
			if (status == null) {
				return;
			}
			switch (status) {
			case PREAPPROVED:
				preapproved++;
				break;
			case REJECTED:
				preRejected++;
				break;
			default:
				break;
			}
		}

		void incrementFinalVerification(AuditorCandidateDecisionStatus status) {
			if (status == null) {
				return;
			}
			switch (status) {
			case APPROVED:
				finalApproved++;
				break;
			case REJECTED:
				finalRejected++;
				break;
			default:
				break;
			}
		}

		String toPreverificationHtml(String totalLabel, String preapprovedLabel, String rejectedLabel) {
			return toVerificationHtml(totalLabel, preapprovedLabel, preapproved, rejectedLabel, preRejected);
		}

		String toFinalVerificationHtml(String totalLabel, String approvedLabel, String rejectedLabel) {
			return toVerificationHtml(totalLabel, approvedLabel, finalApproved, rejectedLabel, finalRejected);
		}

		private String toVerificationHtml(String totalLabel, String approvedLabel, int approvedCount, String rejectedLabel, int rejectedCount) {
			StringBuilder html = new StringBuilder(320);
			html.append("<div class=\"d-flex flex-column gap-1\">");
			html.append("<div class=\"fw-semibold\">").append(totalLabel).append(": ").append(approvedCount + rejectedCount).append("</div>");
			html.append("<div class=\"d-flex flex-wrap gap-1\">");
			html.append("<span class=\"badge bg-success-subtle text-success-emphasis\">")
					.append(approvedLabel)
					.append(": ")
					.append(approvedCount)
					.append(MarkupLiterals.CLOSE_SPAN_TAG);
			html.append("<span class=\"badge bg-danger-subtle text-danger-emphasis\">")
					.append(rejectedLabel)
					.append(": ")
					.append(rejectedCount)
					.append(MarkupLiterals.CLOSE_SPAN_TAG);
			html.append("</div>");
			html.append("</div>");
			return html.toString();
		}
	}

}
