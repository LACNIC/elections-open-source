package net.lacnic.elections.adminweb.ui.admin.election.prereports;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.utils.DateTimeUtils;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class PreElectionReportsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String BADGE_ROUNDED_PILL_CLASS = "badge rounded-pill ";
	private static final String KEY_CANDIDATE_MANAGEMENT_COMMON_OPEN_LINK = "candidateManagemenCommonOpenLink";
	private static final String KEY_CANDIDATE_MANAGEMENT_LIST_NO_LINK = "candidateManagemenListNoLink";
	private static final String BADGE_DANGER_SUBTLE_CLASS = "bg-danger-subtle text-danger-emphasis";
	private static final String BADGE_SECONDARY_SUBTLE_CLASS = "bg-secondary-subtle text-secondary-emphasis";
	private static final String BADGE_SUCCESS_SUBTLE_CLASS = "bg-success-subtle text-success-emphasis";
	private static final String TABLE_LIGHT_CLASS = "table-light";
	private static final String TABLE_DANGER_CLASS = "table-danger";
	private static final String KEY_AUDITOR_PROGRESS_NO_FIRST_REACTION = "candidateManagemenAuditorProgressNoFirstReaction";
	private static final String KEY_AUDITOR_PROGRESS_NO_FINAL_DECISION = "candidateManagemenAuditorProgressNoFinalDecision";
	private final Election election;

	public PreElectionReportsPanel(String id, Election election) {
		super(id);
		this.election = election;

		try {
			List<Nomination> electionNominations = AppContext.getInstance().getManagerBeanRemote().getElectionNominations(election.getElectionId());
			final List<ElectionCalendar> electionCalendars = new ArrayList<>(AppContext.getInstance().getManagerBeanRemote().getElectionCalendars(election.getElectionId()));
			final List<Nomination> pendingNominations = new ArrayList<>();
			final List<Nomination> acceptedNominations = new ArrayList<>();
			for (Nomination nomination : electionNominations) {
				if (nomination == null) {
					continue;
				}
				if (nomination.getCandidate() == null) {
					pendingNominations.add(nomination);
				} else if (isAcceptedNomination(nomination)) {
					acceptedNominations.add(nomination);
				}
			}

			final List<SupportNomination> supportNominations = new ArrayList<>(AppContext.getInstance().getManagerBeanRemote().getElectionSupportNominations(election.getElectionId()));
			final List<ElectionTask> electionTasks = new ArrayList<>(AppContext.getInstance().getManagerBeanRemote().getElectionTasks(election.getElectionId()));
			electionTasks.sort(Comparator
					.comparingInt((ElectionTask task) -> task != null && task.getDisplayOrder() != null ? task.getDisplayOrder() : Integer.MAX_VALUE)
					.thenComparingLong(task -> task != null ? task.getId() : Long.MAX_VALUE));
			final List<LinkAvailabilityRow> linkAvailabilityRows = buildLinkAvailabilityRows(election, electionCalendars, electionTasks);
			final List<NominationProgressRow> nominationProgressRows = buildNominationProgressRows(electionNominations, electionTasks);

			final List<Auditor> allElectionAuditors = new ArrayList<>(AppContext.getInstance().getManagerBeanRemote().getElectionAuditors(election.getElectionId()));
			final List<Auditor> electionAuditors = new ArrayList<>();
			for (Auditor auditor : allElectionAuditors) {
				if (auditor != null && auditor.isCommissioner()) {
					electionAuditors.add(auditor);
				}
			}
			electionAuditors.sort(Comparator.comparing(auditor -> auditor != null && auditor.getName() != null ? auditor.getName().toLowerCase(Locale.ROOT) : ""));
			final List<AuditorCandidateDecision> auditorDecisions = AppContext.getInstance().getManagerBeanRemote().getElectionAuditorCandidateDecisions(election.getElectionId());
			final List<AuditorDecisionRow> auditorDecisionRows = buildAuditorDecisionRows(electionNominations, electionAuditors, auditorDecisions);

			add(new ListView<LinkAvailabilityRow>("linkAvailabilityList", linkAvailabilityRows) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<LinkAvailabilityRow> item) {
					LinkAvailabilityRow row = item.getModelObject();
					item.add(new Label("linkName", row.getName()));
					item.add(new Label("linkRoute", row.getRoute()));

					Label manualState = new Label("linkManualState", row.getManualStateLabel());
					manualState.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, BADGE_ROUNDED_PILL_CLASS + row.getManualStateBadgeClass()));
					item.add(manualState);

					Label currentStatus = new Label("linkCurrentStatus", row.getCurrentStatusLabel());
					currentStatus.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, BADGE_ROUNDED_PILL_CLASS + row.getCurrentStatusBadgeClass()));
					item.add(currentStatus);

					Label accessibleNow = new Label("linkAccessibleNow", row.getAccessibleNowLabel());
					accessibleNow.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, BADGE_ROUNDED_PILL_CLASS + row.getAccessibleNowBadgeClass()));
					item.add(accessibleNow);

					item.add(new Label("linkRule", row.getRuleLabel()));
					item.add(new Label("linkAccessibleFrom", row.getAccessibleFromLabel()));
					item.add(new Label("linkAccessibleUntil", row.getAccessibleUntilLabel()));
				}
			});

			add(new ListView<Nomination>("acceptedNominationsList", acceptedNominations) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<Nomination> item) {
					Nomination nomination = item.getModelObject();
					Candidate candidate = nomination != null ? nomination.getCandidate() : null;

					item.add(new Label("acceptedNominationId", nomination != null ? String.valueOf(nomination.getId()) : "-"));
					item.add(new Label("acceptedNominationStatus", nomination != null && nomination.getStatus() != null ? nomination.getStatus().name() : "-"));
					item.add(new Label("acceptedNominationOrganization", buildOrganizationSummary(nomination)));
					item.add(new Label("acceptedNominationNominatedName", nomination != null ? valueOrDash(nomination.getNominationName()) : "-"));
					item.add(new Label("acceptedNominationNominatedEmail", nomination != null ? valueOrDash(nomination.getNominationEmail()) : "-"));

					Label acceptedCandidateSummary = new Label("acceptedNominationCandidate", buildCandidateSummary(candidate, nomination));
					acceptedCandidateSummary.setEscapeModelStrings(false);
					item.add(acceptedCandidateSummary);
					item.add(new Label("acceptedNominationCandidateStatus", resolveCandidateStatus(candidate)));

					String completionLink = nomination != null ? nomination.getAcceptNominationLink() : null;
					ExternalLink acceptedNominationLink = new ExternalLink(
							"acceptedNominationLink",
							hasText(completionLink) ? completionLink : "#",
							getString(KEY_CANDIDATE_MANAGEMENT_COMMON_OPEN_LINK));
					acceptedNominationLink.setVisible(hasText(completionLink));
					item.add(acceptedNominationLink);
					item.add(new Label("acceptedNominationLinkEmpty", getString(KEY_CANDIDATE_MANAGEMENT_LIST_NO_LINK)).setVisible(!hasText(completionLink)));

					Link<Void> acceptedNominationSendReminder = new Link<Void>("acceptedNominationSendReminder") {
						private static final long serialVersionUID = 1L;

						@Override
						public void onClick() {
							if (candidate == null) {
								getSession().error(getString("preElectionReportsAcceptedReminderNoCandidate"));
								setResponsePage(PreElectionReportsDashboard.class, UtilsParameters.getId(election.getElectionId()));
								return;
							}
							boolean queued = AppContext.getInstance().getManagerBeanRemote().sendCandidateReminder(
									election.getElectionId(),
									candidate.getCandidateId(),
									SecurityUtils.getUserAdminId(),
									SecurityUtils.getClientIp());
							if (queued) {
								getSession().info(getString("candidateManagemenListSendReminderSuccess"));
							} else {
								getSession().error(getString("candidateManagemenListSendReminderError"));
							}
							setResponsePage(PreElectionReportsDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					acceptedNominationSendReminder.setVisible(candidate != null);
					item.add(acceptedNominationSendReminder);
				}
			});
			add(new Label("noAcceptedNominations", new ResourceModel("preElectionReportsAcceptedEmpty")).setVisible(acceptedNominations.isEmpty()));

			add(new ListView<Nomination>("pendingNominationsList", pendingNominations) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<Nomination> item) {
					Nomination nomination = item.getModelObject();
					item.add(new Label("pendingNominationId", String.valueOf(nomination.getId())));
					item.add(new Label("pendingNominationStatus", nomination.getStatus() != null ? nomination.getStatus().name() : "-"));
					item.add(new Label("pendingNominationDate", valueOrDash(nomination.getNominationDate())));
					item.add(new Label("pendingNominationOrganization", buildOrganizationSummary(nomination)));
					item.add(new Label("pendingNominationName", valueOrDash(nomination.getNominationName())));
					item.add(new Label("pendingNominationEmail", valueOrDash(nomination.getNominationEmail())));

					String pendingLink = nomination.getAcceptNominationLink();
					ExternalLink pendingNominationLink = new ExternalLink(
							"pendingNominationLink",
							hasText(pendingLink) ? pendingLink : "#",
							getString(KEY_CANDIDATE_MANAGEMENT_COMMON_OPEN_LINK));
					pendingNominationLink.setVisible(hasText(pendingLink));
					item.add(pendingNominationLink);

					item.add(new Label("pendingNominationLinkEmpty", getString(KEY_CANDIDATE_MANAGEMENT_LIST_NO_LINK)).setVisible(!hasText(pendingLink)));

					Link<Void> pendingNominationSendReminder = new Link<Void>("pendingNominationSendReminder") {
						private static final long serialVersionUID = 1L;

						@Override
						public void onClick() {
							boolean queued = AppContext.getInstance().getManagerBeanRemote().sendNominationReminder(
									election.getElectionId(),
									nomination.getId(),
									SecurityUtils.getUserAdminId(),
									SecurityUtils.getClientIp());
							if (queued) {
								getSession().info(getString("candidateManagemenPendingNominationsReminderSuccess"));
							} else {
								getSession().error(getString("candidateManagemenPendingNominationsReminderError"));
							}
							setResponsePage(PreElectionReportsDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					item.add(pendingNominationSendReminder);
				}
			});
			add(new Label("noPendingNominations", new ResourceModel("candidateManagemenPendingNominationsEmpty")).setVisible(pendingNominations.isEmpty()));

			add(new ListView<SupportNomination>("supportNominationsList", supportNominations) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<SupportNomination> item) {
					SupportNomination supportNomination = item.getModelObject();
					Nomination nomination = supportNomination != null ? supportNomination.getNomination() : null;
					Candidate candidate = nomination != null ? nomination.getCandidate() : null;

					item.add(new Label("supportNominationId", supportNomination != null ? String.valueOf(supportNomination.getId()) : "-"));
					item.add(new Label("supportNominationStatus", supportNomination != null && supportNomination.getSupportStatus() != null ? supportNomination.getSupportStatus().name() : "-"));
					item.add(new Label("supportNominationNominationId", nomination != null ? String.valueOf(nomination.getId()) : "-"));

					Label supportCandidateSummary = new Label("supportNominationCandidate", buildCandidateSummary(candidate, nomination));
					supportCandidateSummary.setEscapeModelStrings(false);
					item.add(supportCandidateSummary);

					item.add(new Label("supportNominationOrganization", buildSupportOrganizationSummary(supportNomination)));
					item.add(new Label("supportNominationContactName", resolveSupportContactName(supportNomination)));
					item.add(new Label("supportNominationContactEmail", resolveSupportContactEmail(supportNomination)));

					String supportLink = supportNomination != null ? supportNomination.getSupportNominationLink() : null;
					ExternalLink supportNominationLink = new ExternalLink(
							"supportNominationLink",
							hasText(supportLink) ? supportLink : "#",
							getString(KEY_CANDIDATE_MANAGEMENT_COMMON_OPEN_LINK));
					supportNominationLink.setVisible(hasText(supportLink));
					item.add(supportNominationLink);
					item.add(new Label("supportNominationLinkEmpty", getString(KEY_CANDIDATE_MANAGEMENT_LIST_NO_LINK)).setVisible(!hasText(supportLink)));

					Link<Void> supportNominationSendReminder = new Link<Void>("supportNominationSendReminder") {
						private static final long serialVersionUID = 1L;

						@Override
						public void onClick() {
							boolean queued = AppContext.getInstance().getManagerBeanRemote().sendSupportReminder(
									election.getElectionId(),
									supportNomination.getId(),
									SecurityUtils.getUserAdminId(),
									SecurityUtils.getClientIp());
							if (queued) {
								getSession().info(getString("candidateManagemenSupportsReminderSuccess"));
							} else {
								getSession().error(getString("candidateManagemenSupportsReminderError"));
							}
							setResponsePage(PreElectionReportsDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					item.add(supportNominationSendReminder);
				}
			});
			add(new Label("noSupportNominations", new ResourceModel("candidateManagemenSupportsEmpty")).setVisible(supportNominations.isEmpty()));

			add(new ListView<ElectionTask>("taskProgressHeaders", electionTasks) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<ElectionTask> item) {
					ElectionTask task = item.getModelObject();
					ElectionTaskKey taskKey = task != null ? task.getTaskKey() : null;
					item.add(new Label("taskHeaderLabel", resolveTaskLabel(taskKey)));
				}
			});

			add(new ListView<NominationProgressRow>("nominationProgressList", nominationProgressRows) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<NominationProgressRow> item) {
					NominationProgressRow row = item.getModelObject();
					Nomination nomination = row.getNomination();
					Candidate candidate = row.getCandidate();

					item.add(new Label("progressNominationId", nomination != null ? String.valueOf(nomination.getId()) : "-"));
					item.add(new Label("progressNominationStatus", nomination != null && nomination.getStatus() != null ? nomination.getStatus().name() : "-"));

					Label progressCandidateSummary = new Label("progressCandidateSummary", buildCandidateSummary(candidate, nomination));
					progressCandidateSummary.setEscapeModelStrings(false);
					item.add(progressCandidateSummary);
					item.add(new Label("progressCandidateStatus", resolveCandidateStatus(candidate)));

					item.add(new ListView<TaskProgressCell>("taskProgressCells", row.getTaskCells()) {
						private static final long serialVersionUID = 1L;

						@Override
						protected void populateItem(ListItem<TaskProgressCell> cellItem) {
							TaskProgressCell cell = cellItem.getModelObject();
							cellItem.add(new Label("taskProgressValue", new ResourceModel(cell.getStatusKey())));
							cellItem.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-center fw-semibold " + cell.getCellClass()));
						}
					});
				}
			});
			add(new Label("noNominationProgressRows", new ResourceModel("candidateManagemenProgressEmpty")).setVisible(nominationProgressRows.isEmpty()));

			add(new ListView<Auditor>("auditorDecisionHeaders", electionAuditors) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<Auditor> item) {
					Auditor auditor = item.getModelObject();
					String label = auditor != null ? valueOrDash(auditor.getName()) : "-";
					item.add(new Label("auditorHeaderLabel", label));
				}
			});
			add(new ListView<Auditor>("auditorDecisionStageHeaders", electionAuditors) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<Auditor> item) {
				}
			});

			add(new ListView<AuditorDecisionRow>("auditorDecisionList", auditorDecisionRows) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<AuditorDecisionRow> item) {
					AuditorDecisionRow row = item.getModelObject();
					Nomination nomination = row.getNomination();
					Candidate candidate = row.getCandidate();

					item.add(new Label("auditorProgressNominationId", nomination != null ? String.valueOf(nomination.getId()) : "-"));
					item.add(new Label("auditorProgressNominationStatus", nomination != null && nomination.getStatus() != null ? nomination.getStatus().name() : "-"));

					Label auditorProgressCandidateSummary = new Label("auditorProgressCandidateSummary", buildCandidateSummary(candidate, nomination));
					auditorProgressCandidateSummary.setEscapeModelStrings(false);
					item.add(auditorProgressCandidateSummary);
					item.add(new Label("auditorProgressCandidateStatus", resolveCandidateStatus(candidate)));

					item.add(new ListView<AuditorDecisionCell>("auditorDecisionCells", row.getAuditorCells()) {
						private static final long serialVersionUID = 1L;

						@Override
						protected void populateItem(ListItem<AuditorDecisionCell> cellItem) {
							AuditorDecisionCell cell = cellItem.getModelObject();

							WebMarkupContainer firstReactionCell = new WebMarkupContainer("auditorFirstReactionCell");
							firstReactionCell.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,
									"text-center auditor-decision-stage-cell " + cell.getFirstReactionCellClass()));
							firstReactionCell.add(new Label("auditorFirstReactionValue", new ResourceModel(cell.getFirstReactionStatusKey())));
							Label firstReactionDate = new Label("auditorFirstReactionDate", cell.getFirstReactionDateLabel());
							firstReactionDate.setVisible(cell.hasFirstReactionDate());
							firstReactionCell.add(firstReactionDate);
							cellItem.add(firstReactionCell);

							WebMarkupContainer finalDecisionCell = new WebMarkupContainer("auditorFinalDecisionCell");
							finalDecisionCell.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,
									"text-center auditor-decision-stage-cell " + cell.getFinalDecisionCellClass()));
							finalDecisionCell.add(new Label("auditorFinalDecisionValue", new ResourceModel(cell.getFinalDecisionStatusKey())));
							Label finalDecisionDate = new Label("auditorFinalDecisionDate", cell.getFinalDecisionDateLabel());
							finalDecisionDate.setVisible(cell.hasFinalDecisionDate());
							finalDecisionCell.add(finalDecisionDate);
							cellItem.add(finalDecisionCell);
						}
					});
				}
			});
			add(new Label("noAuditorDecisionRows", new ResourceModel("candidateManagemenAuditorProgressEmpty")).setVisible(auditorDecisionRows.isEmpty()));

		} catch (Exception e) {
			appLogger.error("Error building pre-election reports panel. electionId={}", election.getElectionId(), e);
			throw new IllegalStateException("Could not build pre-election reports panel", e);
		}
	}

	private boolean isAcceptedNomination(Nomination nomination) {
		if (nomination == null || nomination.getStatus() == null || nomination.getCandidate() == null) {
			return false;
		}
		return nomination.getStatus() == NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getStatus() == NominationStatus.APPROVED;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String buildOrganizationSummary(Nomination nomination) {
		if (nomination == null || nomination.getOrganization() == null) {
			return "-";
		}
		String orgId = valueOrDash(nomination.getOrganization().getOrgId());
		String orgName = valueOrDash(nomination.getOrganization().getName());
		return orgId + " - " + orgName;
	}

	private String buildSupportOrganizationSummary(SupportNomination supportNomination) {
		if (supportNomination == null || supportNomination.getSupportingOrganization() == null) {
			return "-";
		}
		String orgId = valueOrDash(supportNomination.getSupportingOrganization().getOrgId());
		String orgName = valueOrDash(supportNomination.getSupportingOrganization().getName());
		return orgId + " - " + orgName;
	}

	private String resolveSupportContactName(SupportNomination supportNomination) {
		if (supportNomination == null) {
			return "-";
		}
		if (hasText(supportNomination.getSupportingContactName())) {
			return supportNomination.getSupportingContactName();
		}
		if (supportNomination.getSupportingOrganization() != null) {
			return valueOrDash(supportNomination.getSupportingOrganization().getMembershipContactName());
		}
		return "-";
	}

	private String resolveSupportContactEmail(SupportNomination supportNomination) {
		if (supportNomination == null) {
			return "-";
		}
		if (hasText(supportNomination.getSupportingContactEmail())) {
			return supportNomination.getSupportingContactEmail();
		}
		if (supportNomination.getSupportingOrganization() != null) {
			return valueOrDash(supportNomination.getSupportingOrganization().getMembershipContactEmail());
		}
		return "-";
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private String resolveCandidateStatus(Candidate candidate) {
		if (candidate == null || candidate.getStatus() == null) {
			return "-";
		}
		if (candidate.getStatus() == CandidateStatus.PRECOMPLETE) {
			return "PRECOMPLETE";
		}
		return candidate.getStatus().name();
	}

	private String buildCandidateSummary(Candidate candidate, Nomination nomination) {
		if (candidate == null) {
			return "-";
		}
		StringBuilder summary = new StringBuilder(128);
		summary.append("<div><strong>")
				.append(escaped(valueOrDash(candidate.getName())))
				.append("</strong> (ID: ")
				.append(candidate.getCandidateId())
				.append(")</div>");
		String email = candidate.getMail();
		if (!hasText(email) && nomination != null) {
			email = nomination.getNominationEmail();
		}
		summary.append("<div class=\"text-muted\">")
				.append(escaped(valueOrDash(email)))
				.append("</div>");
		return summary.toString();
	}

	private String escaped(String value) {
		return Strings.escapeMarkup(valueOrDash(value)).toString();
	}

	private String resolveTaskLabel(ElectionTaskKey taskKey) {
		if (taskKey == null) {
			return "-";
		}
		String resourceKey = "electionTaskKey." + taskKey.name();
		try {
			return getString(resourceKey);
		} catch (Exception ignored) {
			String normalized = taskKey.name().toLowerCase(Locale.ROOT).replace('_', ' ');
			return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
		}
	}

	private List<LinkAvailabilityRow> buildLinkAvailabilityRows(Election election, List<ElectionCalendar> calendars, List<ElectionTask> electionTasks) {
		Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey = indexCalendarsByKey(calendars);
		TaskWindow nominationTaskWindow = resolveNominationTaskWindow(electionTasks);
		Date now = new Date();

		List<LinkAvailabilityRow> rows = new ArrayList<>();
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkPublicElection"),
				"/token/public-election",
				election.isPublicElectionLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED),
				ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
				false,
				now));
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkElectoralRollPublication"),
				"/token/public-election?action=roll",
				election.isPublicElectionLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_4_SINGLE_PADRON_PUBLISHED),
				ElectionCalendarKey.N_4_SINGLE_PADRON_PUBLISHED,
				false,
				now));
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkAuditDashboard"),
				"/token/audit",
				election.isAuditorLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED),
				ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
				false,
				now));
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkDoNomination"),
				"/token/organization/do-nomination",
				election.isDoNominationLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES),
				ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
				true,
				now));
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkNominationConditions"),
				"Flujo interno / AcceptNominationConditionsPage",
				election.isNominationTasksLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES),
				ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
				true,
				now));
		rows.add(buildDynamicLinkAvailabilityRow(
				getString("preElectionReportsLinkNominationTasks"),
				"/token/nomination/tasks",
				election.isNominationTasksLinkAvailable(),
				nominationTaskWindow,
				now));
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkSupportNomination"),
				"/token/nomination/support",
				election.isNominationSupportLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES),
				ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
				true,
				now));
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkAuditCandidateDetail"),
				"/token/audit/candidate",
				election.isAuditorLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED),
				ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
				false,
				now));
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkAuditCandidateFirstReaction"),
				"Flujo interno / AuditPublicCandidateDetailPage",
				election.isAuditorLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION),
				ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION,
				true,
				now));
		rows.add(buildCalendarGroupLinkAvailabilityRow(
				getString("preElectionReportsLinkAuditCandidateFinalDecision"),
				"Flujo interno / AuditPublicCandidateDetailPage",
				election.isAuditorLinkAvailable(),
				calendarsByKey,
				now,
				ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION,
				ElectionCalendarKey.N_8_PERIODO_EVALUATIONS_VALIDATION));
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkCandidateQuestions"),
				"Flujo interno / AskCandidateQuestionPage",
				election.isPublicElectionLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS),
				ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS,
				true,
				now));
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkVote"),
				"/token/vote",
				election.isVotingLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_16_PERIODO_VOTING),
				ElectionCalendarKey.N_16_PERIODO_VOTING,
				true,
				now));
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkAuditResults"),
				"/token/audit/result",
				election.isAuditorLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT),
				ElectionCalendarKey.N_18_PERIODO_CE_AUDIT,
				true,
				now));
		rows.add(buildCalendarLinkAvailabilityRow(
				getString("preElectionReportsLinkResults"),
				"/token/result",
				election.isResultLinkAvailable(),
				calendarsByKey.get(ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED),
				ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED,
				false,
				now));
		return rows;
	}

	private Map<ElectionCalendarKey, ElectionCalendar> indexCalendarsByKey(List<ElectionCalendar> calendars) {
		Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey = new HashMap<>();
		if (calendars == null) {
			return calendarsByKey;
		}
		for (ElectionCalendar calendar : calendars) {
			if (calendar == null || calendar.getCalendarKey() == null) {
				continue;
			}
			calendarsByKey.put(calendar.getCalendarKey(), calendar);
		}
		return calendarsByKey;
	}

	private LinkAvailabilityRow buildCalendarLinkAvailabilityRow(String name, String route, boolean manualEnabled,
			ElectionCalendar calendar, ElectionCalendarKey key, boolean useCalendarEndDate, Date now) {
		boolean configured = calendar != null;
		Date start = calendar != null ? calendar.getStartDate() : null;
		Date end = configured && useCalendarEndDate ? calendar.getEndDate() : null;
		String ruleLabel = configured ? resolveCalendarRuleLabel(key) : getString("preElectionReportsLinksRuleNoCalendar");
		return buildLinkAvailabilityRow(name, route, manualEnabled, configured, ruleLabel, start, end, now);
	}

	private LinkAvailabilityRow buildDynamicLinkAvailabilityRow(String name, String route, boolean manualEnabled,
			TaskWindow taskWindow, Date now) {
		return buildLinkAvailabilityRow(name, route, manualEnabled, taskWindow.isConfigured(), taskWindow.getRuleLabel(),
				taskWindow.getStart(), taskWindow.getEnd(), now);
	}

	private LinkAvailabilityRow buildCalendarGroupLinkAvailabilityRow(String name, String route, boolean manualEnabled,
			Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey, Date now, ElectionCalendarKey... keys) {
		TaskWindow calendarWindow = resolveCalendarGroupWindow(calendarsByKey, now, keys);
		return buildLinkAvailabilityRow(name, route, manualEnabled, calendarWindow.isConfigured(), calendarWindow.getRuleLabel(),
				calendarWindow.getStart(), calendarWindow.getEnd(), calendarWindow.isActiveAt(now), now);
	}

	private LinkAvailabilityRow buildLinkAvailabilityRow(String name, String route, boolean manualEnabled, boolean configured,
			String ruleLabel, Date start, Date end, Date now) {
		boolean activeInWindow = configured && !((start != null && now.before(start)) || (end != null && now.after(end)));
		return buildLinkAvailabilityRow(name, route, manualEnabled, configured, ruleLabel, start, end, activeInWindow, now);
	}

	private LinkAvailabilityRow buildLinkAvailabilityRow(String name, String route, boolean manualEnabled, boolean configured,
			String ruleLabel, Date start, Date end, boolean activeInWindow, Date now) {
		boolean beforeWindow = start != null && now.before(start);
		boolean afterWindow = end != null && now.after(end);
		boolean accessibleNow = manualEnabled && configured && activeInWindow;

		String currentStatusLabel;
		String currentStatusBadgeClass;
		if (!manualEnabled) {
			currentStatusLabel = getString("preElectionReportsLinksStatusManualOff");
			currentStatusBadgeClass = BADGE_DANGER_SUBTLE_CLASS;
		} else if (!configured) {
			currentStatusLabel = getString("preElectionReportsLinksStatusNotConfigured");
			currentStatusBadgeClass = BADGE_SECONDARY_SUBTLE_CLASS;
		} else if (activeInWindow) {
			currentStatusLabel = getString("preElectionReportsLinksStatusAvailable");
			currentStatusBadgeClass = BADGE_SUCCESS_SUBTLE_CLASS;
		} else if (beforeWindow) {
			currentStatusLabel = getString("preElectionReportsLinksStatusPending");
			currentStatusBadgeClass = "bg-warning-subtle text-warning-emphasis";
		} else if (afterWindow) {
			currentStatusLabel = getString("preElectionReportsLinksStatusClosed");
			currentStatusBadgeClass = "bg-dark-subtle text-dark-emphasis";
		} else {
			currentStatusLabel = getString("preElectionReportsLinksStatusPending");
			currentStatusBadgeClass = "bg-warning-subtle text-warning-emphasis";
		}

		String manualStateLabel = manualEnabled
				? getString("preElectionReportsLinksManualOn")
				: getString("preElectionReportsLinksManualOff");
		String manualStateBadgeClass = manualEnabled
				? BADGE_SUCCESS_SUBTLE_CLASS
				: BADGE_DANGER_SUBTLE_CLASS;
		String accessibleNowLabel = accessibleNow
				? getString("preElectionReportsLinksAccessibleYes")
				: getString("preElectionReportsLinksAccessibleNo");
		String accessibleNowBadgeClass = accessibleNow
				? BADGE_SUCCESS_SUBTLE_CLASS
				: BADGE_DANGER_SUBTLE_CLASS;

		String accessibleFromLabel = configured ? formatDateTime(start) : "-";
		String accessibleUntilLabel = !configured ? "-"
				: end != null ? formatDateTime(end) : getString("preElectionReportsLinksNoEnd");

		return new LinkAvailabilityRow(
				name,
				route,
				manualStateLabel,
				manualStateBadgeClass,
				currentStatusLabel,
				currentStatusBadgeClass,
				accessibleNowLabel,
				accessibleNowBadgeClass,
				ruleLabel,
				accessibleFromLabel,
				accessibleUntilLabel);
	}

	private TaskWindow resolveCalendarGroupWindow(Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey, Date now, ElectionCalendarKey... keys) {
		Date earliestStart = null;
		Date latestEnd = null;
		boolean activeNow = false;
		Set<ElectionCalendarKey> configuredKeys = new LinkedHashSet<>();
		for (ElectionCalendarKey key : keys) {
			ElectionCalendar calendar = calendarsByKey != null ? calendarsByKey.get(key) : null;
			if (calendar == null || calendar.getStartDate() == null) {
				continue;
			}
			configuredKeys.add(key);
			Date start = calendar.getStartDate();
			Date end = calendar.getEndDate() != null ? calendar.getEndDate() : start;
			if (earliestStart == null || start.before(earliestStart)) {
				earliestStart = start;
			}
			if (latestEnd == null || end.after(latestEnd)) {
				latestEnd = end;
			}
			if (now != null && !now.before(start) && !now.after(end)) {
				activeNow = true;
			}
		}
		if (configuredKeys.isEmpty()) {
			return new TaskWindow(null, null, false, getString("preElectionReportsLinksRuleNoCalendar"), false);
		}
		StringBuilder calendarsLabel = new StringBuilder();
		for (ElectionCalendarKey key : configuredKeys) {
			if (calendarsLabel.length() > 0) {
				calendarsLabel.append(", ");
			}
			calendarsLabel.append(resolveCalendarRuleLabel(key));
		}
		return new TaskWindow(
				earliestStart,
				latestEnd,
				true,
				MessageFormat.format(getString("preElectionReportsLinksRuleCalendarWindows"), calendarsLabel.toString()),
				activeNow);
	}

	private TaskWindow resolveNominationTaskWindow(List<ElectionTask> electionTasks) {
		if (electionTasks == null || electionTasks.isEmpty()) {
			return new TaskWindow(null, null, false, getString("preElectionReportsLinksRuleNoTasks"));
		}
		Date earliestStart = null;
		Date latestEnd = null;
		Set<ElectionCalendarKey> calendarKeys = new LinkedHashSet<>();
		for (ElectionTask electionTask : electionTasks) {
			if (electionTask == null || electionTask.getElectionCalendar() == null || electionTask.getElectionCalendar().getCalendarKey() == null) {
				continue;
			}
			ElectionCalendar calendar = electionTask.getElectionCalendar();
			calendarKeys.add(calendar.getCalendarKey());
			Date start = calendar.getStartDate();
			Date end = calendar.getEndDate() != null ? calendar.getEndDate() : calendar.getStartDate();
			if (start != null && (earliestStart == null || start.before(earliestStart))) {
				earliestStart = start;
			}
			if (end != null && (latestEnd == null || end.after(latestEnd))) {
				latestEnd = end;
			}
		}
		if (calendarKeys.isEmpty()) {
			return new TaskWindow(null, null, false, getString("preElectionReportsLinksRuleNoTaskCalendars"));
		}
		StringBuilder calendarsLabel = new StringBuilder();
		for (ElectionCalendarKey calendarKey : calendarKeys) {
			if (calendarsLabel.length() > 0) {
				calendarsLabel.append(", ");
			}
			calendarsLabel.append(resolveCalendarRuleLabel(calendarKey));
		}
		return new TaskWindow(
				earliestStart,
				latestEnd,
				true,
				MessageFormat.format(getString("preElectionReportsLinksRuleDynamicTasks"), calendarsLabel.toString()));
	}

	private String resolveCalendarRuleLabel(ElectionCalendarKey key) {
		if (key == null) {
			return getString("preElectionReportsLinksRuleNoCalendar");
		}
		String label;
		try {
			label = getString("electionCalendarKey." + key.name());
		} catch (Exception ignored) {
			label = key.name();
		}
		return label + " (" + key.name() + ")";
	}

	private String formatDateTime(Date date) {
		if (date == null) {
			return "-";
		}
		int diffUtc = election != null ? election.getDiffUTC() : 0;
		Date displayDate = new DateTime(date).plusHours(diffUtc).toDate();
		return DateTimeUtils.getElectionDateTimeString(displayDate) + " (UTC+" + diffUtc + ")";
	}

	private List<NominationProgressRow> buildNominationProgressRows(List<Nomination> nominations, List<ElectionTask> electionTasks) {
		if (nominations == null || nominations.isEmpty()) {
			return Collections.emptyList();
		}
		List<NominationProgressRow> rows = new ArrayList<>();
		for (Nomination nomination : nominations) {
			if (nomination == null) {
				continue;
			}
			Candidate detailedCandidate = resolveDetailedCandidate(nomination);
			Map<ElectionTaskKey, CandidateElectionTaskStatus> statusByTask = indexTaskStatusByKey(detailedCandidate);

			List<TaskProgressCell> cells = new ArrayList<>();
			for (ElectionTask electionTask : electionTasks) {
				ElectionTaskKey taskKey = electionTask != null ? electionTask.getTaskKey() : null;
				if (taskKey == null) {
					continue;
				}
				cells.add(buildTaskProgressCell(statusByTask.get(taskKey), detailedCandidate != null));
			}
			rows.add(new NominationProgressRow(nomination, detailedCandidate, cells));
		}
		return rows;
	}

	private Candidate resolveDetailedCandidate(Nomination nomination) {
		if (nomination == null || nomination.getCandidate() == null) {
			return null;
		}
		String token = nomination.getAcceptNominationToken();
		if (!hasText(token)) {
			return nomination.getCandidate();
		}
		try {
			Nomination detailedNomination = AppContext.getInstance().getPreNominationBeanRemote().verifyAcceptNominationAccess(token);
			if (detailedNomination != null && detailedNomination.getCandidate() != null) {
				return detailedNomination.getCandidate();
			}
		} catch (Exception e) {
			appLogger.error("Error loading detailed nomination for progress table. nominationId={}", nomination.getId(), e);
		}
		return nomination.getCandidate();
	}

	private Map<ElectionTaskKey, CandidateElectionTaskStatus> indexTaskStatusByKey(Candidate candidate) {
		Map<ElectionTaskKey, CandidateElectionTaskStatus> statusByTask = new HashMap<>();
		if (candidate == null) {
			return statusByTask;
		}
		for (CandidateElectionTaskProgress taskProgress : safeTaskProgress(candidate.getTaskProgress())) {
			if (taskProgress == null || taskProgress.getElectionTask() == null || taskProgress.getElectionTask().getTaskKey() == null) {
				continue;
			}
			statusByTask.put(taskProgress.getElectionTask().getTaskKey(), taskProgress.getStatus());
		}
		return statusByTask;
	}

	private List<CandidateElectionTaskProgress> safeTaskProgress(List<CandidateElectionTaskProgress> taskProgress) {
		if (taskProgress == null) {
			return Collections.emptyList();
		}
		try {
			taskProgress.size();
			return taskProgress;
		} catch (RuntimeException e) {
			return Collections.emptyList();
		}
	}

	private TaskProgressCell buildTaskProgressCell(CandidateElectionTaskStatus status, boolean hasCandidate) {
		if (!hasCandidate) {
			return new TaskProgressCell("candidateManagemenProgressNoCandidate", TABLE_LIGHT_CLASS);
		}
		if (status == null || status == CandidateElectionTaskStatus.NOT_STARTED) {
			return new TaskProgressCell("candidateManagemenProgressNotStarted", TABLE_DANGER_CLASS);
		}
		switch (status) {
		case STARTED:
			return new TaskProgressCell("candidateManagemenProgressStarted", "table-warning");
		case COMPLETED:
			return new TaskProgressCell("candidateManagemenProgressCompleted", "table-success");
		case OMITTED:
			return new TaskProgressCell("candidateManagemenProgressOmitted", "table-secondary");
		case NOT_STARTED:
		default:
			return new TaskProgressCell("candidateManagemenProgressNotStarted", TABLE_DANGER_CLASS);
		}
	}

	private List<AuditorDecisionRow> buildAuditorDecisionRows(List<Nomination> nominations, List<Auditor> auditors, List<AuditorCandidateDecision> decisions) {
		if (nominations == null || nominations.isEmpty()) {
			return Collections.emptyList();
		}

		Map<Long, Map<Long, AuditorDecisionSnapshot>> decisionMatrix = buildDecisionMatrix(decisions);
		List<AuditorDecisionRow> rows = new ArrayList<>();
		for (Nomination nomination : nominations) {
			if (nomination == null) {
				continue;
			}
			Candidate detailedCandidate = resolveDetailedCandidate(nomination);
			Long candidateId = detailedCandidate != null ? detailedCandidate.getCandidateId() : null;
			Map<Long, AuditorDecisionSnapshot> decisionByAuditor = candidateId != null ? decisionMatrix.get(candidateId) : null;

			List<AuditorDecisionCell> cells = new ArrayList<>();
			for (Auditor auditor : auditors) {
				Long auditorId = auditor != null ? auditor.getAuditorId() : null;
				AuditorDecisionSnapshot snapshot = auditorId != null && decisionByAuditor != null ? decisionByAuditor.get(auditorId) : null;
				cells.add(buildAuditorDecisionCell(snapshot, detailedCandidate != null));
			}
			rows.add(new AuditorDecisionRow(nomination, detailedCandidate, cells));
		}
		return rows;
	}

	private Map<Long, Map<Long, AuditorDecisionSnapshot>> buildDecisionMatrix(List<AuditorCandidateDecision> decisions) {
		Map<Long, Map<Long, AuditorDecisionSnapshot>> matrix = new HashMap<>();
		if (decisions == null || decisions.isEmpty()) {
			return matrix;
		}
		for (AuditorCandidateDecision decision : decisions) {
			if (decision == null) {
				continue;
			}
			Long candidateId = decision.getCandidate() != null ? decision.getCandidate().getCandidateId() : null;
			Long auditorId = decision.getAuditor() != null ? decision.getAuditor().getAuditorId() : null;
			if (candidateId == null || auditorId == null) {
				continue;
			}
			AuditorDecisionSnapshot snapshot = new AuditorDecisionSnapshot(
					decision.getDecisionStatus(),
					decision.getDecisionDate(),
					decision.getPreDecisionStatus(),
					decision.getPreDecisionDate(),
					decision.getPreapprovedDate(),
					decision.getFinalDecisionStatus(),
					decision.getFinalDecisionDate(),
					decision.getApprovedDate());
			matrix.computeIfAbsent(candidateId, key -> new HashMap<>()).put(auditorId, snapshot);
		}
		return matrix;
	}

	private AuditorDecisionCell buildAuditorDecisionCell(AuditorDecisionSnapshot snapshot, boolean hasCandidate) {
		if (!hasCandidate) {
			DecisionStageCell noCandidate = new DecisionStageCell(
					"candidateManagemenAuditorProgressNoCandidate",
					TABLE_LIGHT_CLASS,
					"-");
			return new AuditorDecisionCell(
					noCandidate.getStatusKey(),
					noCandidate.getCellClass(),
					noCandidate.getDateLabel(),
					noCandidate.getStatusKey(),
					noCandidate.getCellClass(),
					noCandidate.getDateLabel());
		}

		DecisionStageCell firstReaction = buildFirstReactionStageCell(snapshot);
		DecisionStageCell finalDecision = buildFinalDecisionStageCell(snapshot);

		return new AuditorDecisionCell(
				firstReaction.getStatusKey(),
				firstReaction.getCellClass(),
				firstReaction.getDateLabel(),
				finalDecision.getStatusKey(),
				finalDecision.getCellClass(),
				finalDecision.getDateLabel());
	}

	private DecisionStageCell buildFirstReactionStageCell(AuditorDecisionSnapshot snapshot) {
		if (snapshot == null) {
			return new DecisionStageCell(
					KEY_AUDITOR_PROGRESS_NO_FIRST_REACTION,
					TABLE_DANGER_CLASS,
					"-");
		}

		AuditorCandidateDecisionStatus status = resolveFirstReactionStatus(snapshot);
		if (status == null) {
			return new DecisionStageCell(
					KEY_AUDITOR_PROGRESS_NO_FIRST_REACTION,
					TABLE_DANGER_CLASS,
					"-");
		}

		return new DecisionStageCell(
				resolveDecisionStatusResourceKey(status),
				resolveDecisionStatusCellClass(status),
				formatDateTime(resolveFirstReactionDate(snapshot)));
	}

	private DecisionStageCell buildFinalDecisionStageCell(AuditorDecisionSnapshot snapshot) {
		if (snapshot == null) {
			return new DecisionStageCell(
					KEY_AUDITOR_PROGRESS_NO_FINAL_DECISION,
					TABLE_DANGER_CLASS,
					"-");
		}

		AuditorCandidateDecisionStatus status = resolveFinalDecisionStatus(snapshot);
		if (status == null) {
			return new DecisionStageCell(
					KEY_AUDITOR_PROGRESS_NO_FINAL_DECISION,
					TABLE_DANGER_CLASS,
					"-");
		}

		return new DecisionStageCell(
				resolveDecisionStatusResourceKey(status),
				resolveDecisionStatusCellClass(status),
				formatDateTime(resolveFinalDecisionDate(snapshot)));
	}

	private AuditorCandidateDecisionStatus resolveFirstReactionStatus(AuditorDecisionSnapshot snapshot) {
		if (snapshot == null) {
			return null;
		}
		if (snapshot.getPreDecisionStatus() != null) {
			return snapshot.getPreDecisionStatus();
		}
		AuditorCandidateDecisionStatus legacyStatus = snapshot.getDecisionStatus();
		if (legacyStatus == AuditorCandidateDecisionStatus.PREAPPROVED
				|| legacyStatus == AuditorCandidateDecisionStatus.REJECTED) {
			return legacyStatus;
		}
		return null;
	}

	private AuditorCandidateDecisionStatus resolveFinalDecisionStatus(AuditorDecisionSnapshot snapshot) {
		if (snapshot == null) {
			return null;
		}
		if (snapshot.getFinalDecisionStatus() != null) {
			return snapshot.getFinalDecisionStatus();
		}
		AuditorCandidateDecisionStatus legacyStatus = snapshot.getDecisionStatus();
		if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED
				|| legacyStatus == AuditorCandidateDecisionStatus.REJECTED) {
			return legacyStatus;
		}
		return null;
	}

	private Date resolveFirstReactionDate(AuditorDecisionSnapshot snapshot) {
		return firstNonNull(snapshot != null ? snapshot.getPreDecisionDate() : null,
				snapshot != null ? snapshot.getPreapprovedDate() : null,
				snapshot != null ? snapshot.getDecisionDate() : null);
	}

	private Date resolveFinalDecisionDate(AuditorDecisionSnapshot snapshot) {
		return firstNonNull(snapshot != null ? snapshot.getFinalDecisionDate() : null,
				snapshot != null ? snapshot.getApprovedDate() : null,
				snapshot != null ? snapshot.getDecisionDate() : null);
	}

	private Date firstNonNull(Date... dates) {
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

	private String resolveDecisionStatusResourceKey(AuditorCandidateDecisionStatus status) {
		if (status == null) {
			return "candidateManagemenAuditorProgressNoDecision";
		}
		switch (status) {
		case ANALYZING:
			return "auditPublicV2DecisionPending";
		case PREAPPROVED:
			return "auditPublicV2DecisionPreApproved";
		case APPROVED:
			return "auditPublicV2DecisionApproved";
		case REJECTED:
			return "auditPublicV2DecisionRejected";
		case NO_APPLY:
			return "auditPublicV2DecisionNoApply";
		default:
			return "candidateManagemenAuditorProgressNoDecision";
		}
	}

	private String resolveDecisionStatusCellClass(AuditorCandidateDecisionStatus status) {
		if (status == null) {
			return TABLE_DANGER_CLASS;
		}
		switch (status) {
		case ANALYZING:
			return "table-warning";
		case PREAPPROVED:
		case APPROVED:
			return "table-success";
		case REJECTED:
			return TABLE_DANGER_CLASS;
		case NO_APPLY:
			return "table-secondary";
		default:
			return TABLE_DANGER_CLASS;
		}
	}

	private static class TaskWindow implements Serializable {
		private static final long serialVersionUID = 1L;

		private final Date start;
		private final Date end;
		private final boolean configured;
		private final String ruleLabel;
		private final Boolean activeAtNow;

		TaskWindow(Date start, Date end, boolean configured, String ruleLabel) {
			this(start, end, configured, ruleLabel, null);
		}

		TaskWindow(Date start, Date end, boolean configured, String ruleLabel, Boolean activeAtNow) {
			this.start = start;
			this.end = end;
			this.configured = configured;
			this.ruleLabel = ruleLabel;
			this.activeAtNow = activeAtNow;
		}

		public Date getStart() {
			return start;
		}

		public Date getEnd() {
			return end;
		}

		public boolean isConfigured() {
			return configured;
		}

		public boolean isActiveAt(Date now) {
			if (activeAtNow != null) {
				return activeAtNow.booleanValue();
			}
			if (!configured || now == null) {
				return false;
			}
			boolean beforeWindow = start != null && now.before(start);
			boolean afterWindow = end != null && now.after(end);
			return !beforeWindow && !afterWindow;
		}

		public String getRuleLabel() {
			return ruleLabel;
		}
	}

	private static class LinkAvailabilityRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String name;
		private final String route;
		private final String manualStateLabel;
		private final String manualStateBadgeClass;
		private final String currentStatusLabel;
		private final String currentStatusBadgeClass;
		private final String accessibleNowLabel;
		private final String accessibleNowBadgeClass;
		private final String ruleLabel;
		private final String accessibleFromLabel;
		private final String accessibleUntilLabel;

		LinkAvailabilityRow(String name, String route, String manualStateLabel, String manualStateBadgeClass,
				String currentStatusLabel, String currentStatusBadgeClass, String accessibleNowLabel,
				String accessibleNowBadgeClass, String ruleLabel, String accessibleFromLabel,
				String accessibleUntilLabel) {
			this.name = name;
			this.route = route;
			this.manualStateLabel = manualStateLabel;
			this.manualStateBadgeClass = manualStateBadgeClass;
			this.currentStatusLabel = currentStatusLabel;
			this.currentStatusBadgeClass = currentStatusBadgeClass;
			this.accessibleNowLabel = accessibleNowLabel;
			this.accessibleNowBadgeClass = accessibleNowBadgeClass;
			this.ruleLabel = ruleLabel;
			this.accessibleFromLabel = accessibleFromLabel;
			this.accessibleUntilLabel = accessibleUntilLabel;
		}

		public String getName() {
			return name;
		}

		public String getRoute() {
			return route;
		}

		public String getManualStateLabel() {
			return manualStateLabel;
		}

		public String getManualStateBadgeClass() {
			return manualStateBadgeClass;
		}

		public String getCurrentStatusLabel() {
			return currentStatusLabel;
		}

		public String getCurrentStatusBadgeClass() {
			return currentStatusBadgeClass;
		}

		public String getAccessibleNowLabel() {
			return accessibleNowLabel;
		}

		public String getAccessibleNowBadgeClass() {
			return accessibleNowBadgeClass;
		}

		public String getRuleLabel() {
			return ruleLabel;
		}

		public String getAccessibleFromLabel() {
			return accessibleFromLabel;
		}

		public String getAccessibleUntilLabel() {
			return accessibleUntilLabel;
		}
	}

	private static class NominationProgressRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private final Nomination nomination;
		private final Candidate candidate;
		private final List<TaskProgressCell> taskCells;

		NominationProgressRow(Nomination nomination, Candidate candidate, List<TaskProgressCell> taskCells) {
			this.nomination = nomination;
			this.candidate = candidate;
			this.taskCells = taskCells;
		}

		public Nomination getNomination() {
			return nomination;
		}

		public Candidate getCandidate() {
			return candidate;
		}

		public List<TaskProgressCell> getTaskCells() {
			return taskCells;
		}
	}

	private static class TaskProgressCell implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String statusKey;
		private final String cellClass;

		TaskProgressCell(String statusKey, String cellClass) {
			this.statusKey = statusKey;
			this.cellClass = cellClass;
		}

		public String getStatusKey() {
			return statusKey;
		}

		public String getCellClass() {
			return cellClass;
		}
	}

	private static class AuditorDecisionRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private final Nomination nomination;
		private final Candidate candidate;
		private final List<AuditorDecisionCell> auditorCells;

		AuditorDecisionRow(Nomination nomination, Candidate candidate, List<AuditorDecisionCell> auditorCells) {
			this.nomination = nomination;
			this.candidate = candidate;
			this.auditorCells = auditorCells;
		}

		public Nomination getNomination() {
			return nomination;
		}

		public Candidate getCandidate() {
			return candidate;
		}

		public List<AuditorDecisionCell> getAuditorCells() {
			return auditorCells;
		}
	}

	private static class AuditorDecisionCell implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String firstReactionStatusKey;
		private final String firstReactionCellClass;
		private final String firstReactionDateLabel;
		private final String finalDecisionStatusKey;
		private final String finalDecisionCellClass;
		private final String finalDecisionDateLabel;

		AuditorDecisionCell(
				String firstReactionStatusKey,
				String firstReactionCellClass,
				String firstReactionDateLabel,
				String finalDecisionStatusKey,
				String finalDecisionCellClass,
				String finalDecisionDateLabel) {
			this.firstReactionStatusKey = firstReactionStatusKey;
			this.firstReactionCellClass = firstReactionCellClass;
			this.firstReactionDateLabel = firstReactionDateLabel;
			this.finalDecisionStatusKey = finalDecisionStatusKey;
			this.finalDecisionCellClass = finalDecisionCellClass;
			this.finalDecisionDateLabel = finalDecisionDateLabel;
		}

		public String getFirstReactionStatusKey() {
			return firstReactionStatusKey;
		}

		public String getFirstReactionCellClass() {
			return firstReactionCellClass;
		}

		public String getFirstReactionDateLabel() {
			return firstReactionDateLabel;
		}

		public boolean hasFirstReactionDate() {
			return firstReactionDateLabel != null && !"-".equals(firstReactionDateLabel);
		}

		public String getFinalDecisionStatusKey() {
			return finalDecisionStatusKey;
		}

		public String getFinalDecisionCellClass() {
			return finalDecisionCellClass;
		}

		public String getFinalDecisionDateLabel() {
			return finalDecisionDateLabel;
		}

		public boolean hasFinalDecisionDate() {
			return finalDecisionDateLabel != null && !"-".equals(finalDecisionDateLabel);
		}
	}

	private static class DecisionStageCell implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String statusKey;
		private final String cellClass;
		private final String dateLabel;

		DecisionStageCell(String statusKey, String cellClass, String dateLabel) {
			this.statusKey = statusKey;
			this.cellClass = cellClass;
			this.dateLabel = dateLabel;
		}

		public String getStatusKey() {
			return statusKey;
		}

		public String getCellClass() {
			return cellClass;
		}

		public String getDateLabel() {
			return dateLabel;
		}
	}

	private static class AuditorDecisionSnapshot implements Serializable {
		private static final long serialVersionUID = 1L;

		private final AuditorCandidateDecisionStatus decisionStatus;
		private final Date decisionDate;
		private final AuditorCandidateDecisionStatus preDecisionStatus;
		private final Date preDecisionDate;
		private final Date preapprovedDate;
		private final AuditorCandidateDecisionStatus finalDecisionStatus;
		private final Date finalDecisionDate;
		private final Date approvedDate;

		AuditorDecisionSnapshot(
				AuditorCandidateDecisionStatus decisionStatus,
				Date decisionDate,
				AuditorCandidateDecisionStatus preDecisionStatus,
				Date preDecisionDate,
				Date preapprovedDate,
				AuditorCandidateDecisionStatus finalDecisionStatus,
				Date finalDecisionDate,
				Date approvedDate) {
			this.decisionStatus = decisionStatus;
			this.decisionDate = decisionDate;
			this.preDecisionStatus = preDecisionStatus;
			this.preDecisionDate = preDecisionDate;
			this.preapprovedDate = preapprovedDate;
			this.finalDecisionStatus = finalDecisionStatus;
			this.finalDecisionDate = finalDecisionDate;
			this.approvedDate = approvedDate;
		}

		public AuditorCandidateDecisionStatus getDecisionStatus() {
			return decisionStatus;
		}

		public Date getDecisionDate() {
			return decisionDate;
		}

		public AuditorCandidateDecisionStatus getPreDecisionStatus() {
			return preDecisionStatus;
		}

		public Date getPreDecisionDate() {
			return preDecisionDate;
		}

		public Date getPreapprovedDate() {
			return preapprovedDate;
		}

		public AuditorCandidateDecisionStatus getFinalDecisionStatus() {
			return finalDecisionStatus;
		}

		public Date getFinalDecisionDate() {
			return finalDecisionDate;
		}

		public Date getApprovedDate() {
			return approvedDate;
		}
	}
}
