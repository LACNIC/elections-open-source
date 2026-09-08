package net.lacnic.elections.adminweb.ui.token.page;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenTopHeaderPanel;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.token.page.PublicAccessDeniedPage;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskMode;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskPanelFactory;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolver;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationActionBannerPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTasksNavigationPanel;
import net.lacnic.elections.adminweb.ui.token.CandidateCommunityQuestionsPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.ReminderFrequency;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.Organization;

public class GenericAcceptNominationTasksPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private final FeedbackPanel feedbackPanel;
	private Organization organization;
	private String electionTitle;
	private String organizationName;
	private ReminderFrequency currentReminderFrequency = ReminderFrequency.defaultValue();
	private Nomination nomination;
	private AcceptNominationTaskResolution taskResolution;
	private boolean candidateQuestionsWindowOpen;

	public GenericAcceptNominationTasksPage() {
		this(new PageParameters());
	}

	public GenericAcceptNominationTasksPage(PageParameters params) {
		super(params);

		feedbackPanel = new FeedbackPanel("feedbackPanel");
		feedbackPanel.setOutputMarkupId(true);
		feedbackPanel.setOutputMarkupPlaceholderTag(true);
		add(feedbackPanel);

		AcceptNominationTaskResolver resolver = new AcceptNominationTaskResolver(getToken());
		taskResolution = resolver.resolve(UtilsParameters.getParameters(AcceptNominationTaskResolver.TASK_PARAM, params), UtilsParameters.getParameters(AcceptNominationTaskResolver.MODE_PARAM, params));
		if (redirectToViewIfNominationWindowClosed(taskResolution)) {
			return;
		}
		if (redirectToInProgressForIncompleteTask(taskResolution)) {
			return;
		}
		notifyModeRestrictionIfNeeded(taskResolution);
		candidateQuestionsWindowOpen = isCandidateQuestionsWindowOpen();

		replace(new AcceptNominationActionBannerPanel("belowTopbarContent", getToken(), taskResolution));
		CandidateCommunityQuestionsPanel communityQuestionsPanel = new CandidateCommunityQuestionsPanel("communityQuestionsPanel", getToken(), nomination);
		communityQuestionsPanel.setVisible(candidateQuestionsWindowOpen);
		add(communityQuestionsPanel);
		add(new AcceptNominationTasksNavigationPanel("tasksNavigationPanel", getToken(), taskResolution));
		add(new AcceptNominationTaskPanelFactory().create("taskPanel", taskResolution));
	}

	public FeedbackPanel getFeedbackPanel() {
		return feedbackPanel;
	}

	private boolean redirectToViewIfNominationWindowClosed(AcceptNominationTaskResolution resolution) {
		if (resolution == null || !resolution.isValid() || !resolution.isNominationWindowClosed() || resolution.getSelectedTask() == null
				|| resolution.getSelectedTask().getTaskKey() == null || resolution.getRequestedMode() == null
				|| resolution.getRequestedMode() == AcceptNominationTaskMode.VIEW) {
			return false;
		}

		PageParameters viewParams = buildTokenPageParameters();
		viewParams.add(AcceptNominationTaskResolver.TASK_PARAM, resolution.getSelectedTask().getTaskKey().name());
		viewParams.add(AcceptNominationTaskResolver.MODE_PARAM, AcceptNominationTaskMode.VIEW.getParameterValue());
		setResponsePage(GenericAcceptNominationTasksPage.class, viewParams);
		return true;
	}

	private void notifyModeRestrictionIfNeeded(AcceptNominationTaskResolution resolution) {
		if (resolution == null || !resolution.isValid()) {
			return;
		}
		if (resolution.hasModeRestriction()) {
			getSession().warn(getString(resolution.getRestrictionMessageKey()));
			return;
		}
		if (resolution.getRequestedMode() != resolution.getEffectiveMode()) {
			getSession().warn(getString("acceptNominationTaskRestrictionGeneric"));
		}
	}

	private boolean redirectToInProgressForIncompleteTask(AcceptNominationTaskResolution resolution) {
		if (resolution == null || !resolution.isValid() || resolution.getSelectedTask() == null || resolution.getSelectedTask().getTaskKey() == null
				|| resolution.hasModeRestriction() || resolution.getRequestedMode() == null || resolution.getEffectiveMode() == null) {
			return false;
		}

		boolean shouldRedirect = resolution.getRequestedMode() == AcceptNominationTaskMode.VIEW
				&& resolution.getEffectiveMode() == AcceptNominationTaskMode.COMPLETE
				&& !resolution.getSelectedTask().isCompleted();
		if (!shouldRedirect) {
			return false;
		}

		PageParameters inProgressParams = buildTokenPageParameters();
		inProgressParams.add(AcceptNominationTaskResolver.TASK_PARAM, resolution.getSelectedTask().getTaskKey().name());
		inProgressParams.add(AcceptNominationTaskResolver.MODE_PARAM, AcceptNominationTaskMode.COMPLETE.getParameterValue());
		setResponsePage(GenericAcceptNominationTasksPage.class, inProgressParams);
		return true;
	}

	@Override
	protected Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params) {
		nomination = AppContext.getInstance().getPreNominationBeanRemote().verifyAcceptNominationAccess(getToken());
		if (nomination == null) {
			return Error404.class;
		}
		if (nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE) {
			setElection(nomination.getElection());
			redirectToAccessDeniedPageIfBlocked(resolveNominationAcceptanceAccessGate(nomination));
			throw new RestartResponseException(AcceptNominationConditionsPage.class, buildTokenPageParameters());
		}
		if (nomination.getCandidate() == null || nomination.getCandidate().getTaskProgress() == null || nomination.getCandidate().getTaskProgress().isEmpty()) {
			appLogger.error("Accepted nomination {} for election {} has no task progress configured. candidateId={}",
					nomination.getId(),
					nomination.getElection() != null ? nomination.getElection().getElectionId() : null,
					nomination.getCandidate() != null ? nomination.getCandidate().getCandidateId() : null);
			return Error404.class;
		}

		organization = nomination.getOrganization();
		electionTitle = nomination.getElection() != null ? nomination.getElection().getTitle(SecurityUtils.getLocale().getLanguage()) : null;
		organizationName = organization != null ? organization.getName() : null;

		setElection(nomination.getElection());
		setWhereAmI("Pantalla accept nomination (pagina 3) usando Nomination.acceptNominationToken");
		setContextClass(Nomination.class.getName());
		setContextData(buildContextData(nomination));
		setHeaderUserDisplay(resolveHeaderUserDisplay(nomination));
		currentReminderFrequency = nomination.getCandidate().getReminderFrequency() != null
				? nomination.getCandidate().getReminderFrequency()
				: ReminderFrequency.defaultValue();
		setHeaderNotificationStatusFromReminderFrequency(currentReminderFrequency);
		return null;
	}

	@Override
	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		return resolveNominationTasksAndCandidateQuestionsAccessGate(nomination);
	}

	@Override
	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionNominationTasks");
	}

	@Override
	protected PublicTokenTopHeaderPanel.NotificationMode resolveTopHeaderNotificationMode() {
		return PublicTokenTopHeaderPanel.NotificationMode.NOMINATION;
	}

	@Override
	protected ReminderFrequency resolveTopHeaderReminderFrequency() {
		return currentReminderFrequency != null ? currentReminderFrequency : ReminderFrequency.defaultValue();
	}

	private String resolveHeaderUserDisplay(Nomination currentNomination) {
		if (currentNomination != null && currentNomination.getCandidate() != null && hasText(currentNomination.getCandidate().getName())) {
			return currentNomination.getCandidate().getName();
		}
		if (currentNomination != null && hasText(currentNomination.getNominationName())) {
			return currentNomination.getNominationName();
		}
		return "-";
	}

	private String buildContextData(Nomination nomination) {
		StringBuilder sb = new StringBuilder();
		sb.append("nominationId: ").append(nomination.getId()).append('\n');
		sb.append("status: ").append(nomination.getStatus()).append('\n');
		sb.append("nominationName: ").append(valueOrDash(nomination.getNominationName())).append('\n');
		sb.append("nominationEmail: ").append(valueOrDash(nomination.getNominationEmail())).append('\n');
		sb.append("organizationId: ").append(organization != null ? organization.getId() : "-").append('\n');
		sb.append("organizationName: ").append(valueOrDash(organizationName)).append('\n');
		sb.append("electionTitle: ").append(valueOrDash(electionTitle));

		Candidate candidate = nomination.getCandidate();
		if (candidate != null) {
			sb.append("\n\ncandidateId: ").append(candidate.getCandidateId());
			sb.append("\ncandidateName: ").append(valueOrDash(candidate.getName()));
			sb.append("\ncandidateEmail: ").append(valueOrDash(candidate.getMail()));
			sb.append("\ncandidateStatus: ").append(candidate.getStatus() != null ? candidate.getStatus() : "-");
			sb.append("\ncandidateOrder: ").append(candidate.getCandidateOrder());

			List<CandidateElectionTaskProgress> taskProgressList = candidate.getTaskProgress();
			if (taskProgressList != null && !taskProgressList.isEmpty()) {
				List<CandidateElectionTaskProgress> orderedTaskProgressList = new ArrayList<>(taskProgressList);
				orderedTaskProgressList.sort(Comparator.comparingLong(CandidateElectionTaskProgress::getId));
				sb.append("\n\ncandidateTaskProgress:");
				for (CandidateElectionTaskProgress taskProgress : orderedTaskProgressList) {
					sb.append("\n- progressId: ").append(taskProgress.getId());
					sb.append(", taskId: ").append(taskProgress.getElectionTask() != null ? taskProgress.getElectionTask().getId() : "-");
					sb.append(", taskKey: ").append(taskProgress.getElectionTask() != null && taskProgress.getElectionTask().getTaskKey() != null ? taskProgress.getElectionTask().getTaskKey() : "-");
					sb.append(", status: ").append(taskProgress.getStatus() != null ? taskProgress.getStatus() : "-");
					sb.append(", startDate: ").append(taskProgress.getStartDate() != null ? taskProgress.getStartDate() : "-");
					sb.append(", endDate: ").append(taskProgress.getEndDate() != null ? taskProgress.getEndDate() : "-");
				}
			}
		}

		return sb.toString();
	}

	private String valueOrDash(String value) {
		if (value == null || value.isEmpty()) {
			return "-";
		}
		return value;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private boolean isCandidateQuestionsWindowOpen() {
		ElectionCalendar candidateQuestionsCalendar = findPublicCalendar(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
		if (candidateQuestionsCalendar == null) {
			return false;
		}
		Date now = new Date();
		Date startDate = candidateQuestionsCalendar.getStartDate();
		Date endDate = candidateQuestionsCalendar.getEndDate() != null ? candidateQuestionsCalendar.getEndDate() : startDate;
		if (startDate == null || endDate == null) {
			return false;
		}
		return !now.before(startDate) && !now.after(endDate);
	}

	private TokenAccessGate resolveNominationTasksAndCandidateQuestionsAccessGate(Nomination currentNomination) {
		List<CandidateElectionTaskProgress> progressList = currentNomination != null && currentNomination.getCandidate() != null
				? currentNomination.getCandidate().getTaskProgress()
				: null;
		Date earliestStart = null;
		ElectionCalendarKey earliestKey = null;
		Date latestEnd = null;
		ElectionCalendarKey latestKey = null;
		ElectionCalendarKey fallbackKey = null;

		if (progressList != null) {
			for (CandidateElectionTaskProgress progress : progressList) {
				if (progress == null || progress.getElectionTask() == null) {
					continue;
				}
				ElectionCalendar calendar = progress.getElectionTask().getElectionCalendar();
				if (calendar == null || calendar.getCalendarKey() == null) {
					continue;
				}
				if (fallbackKey == null) {
					fallbackKey = calendar.getCalendarKey();
				}
				Date start = calendar.getStartDate();
				Date end = calendar.getEndDate();
				if (start != null && (earliestStart == null || start.before(earliestStart))) {
					earliestStart = start;
					earliestKey = calendar.getCalendarKey();
				}
				Date effectiveEnd = end != null ? end : start;
				if (effectiveEnd != null && (latestEnd == null || effectiveEnd.after(latestEnd))) {
					latestEnd = effectiveEnd;
					latestKey = calendar.getCalendarKey();
				}
			}
		}

		ElectionCalendar candidateQuestionsCalendar = findPublicCalendar(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
		if (candidateQuestionsCalendar != null) {
			if (fallbackKey == null) {
				fallbackKey = ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS;
			}
			Date questionsStart = candidateQuestionsCalendar.getStartDate();
			Date questionsEnd = candidateQuestionsCalendar.getEndDate() != null ? candidateQuestionsCalendar.getEndDate() : questionsStart;
			if (questionsStart != null && (earliestStart == null || questionsStart.before(earliestStart))) {
				earliestStart = questionsStart;
				earliestKey = ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS;
			}
			if (questionsEnd != null && (latestEnd == null || questionsEnd.after(latestEnd))) {
				latestEnd = questionsEnd;
				latestKey = ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS;
			}
		}

		ElectionCalendarKey accessDeniedKey = earliestKey != null ? earliestKey : (latestKey != null ? latestKey : fallbackKey);
		if (getElection() != null && !getElection().isNominationTasksLinkAvailable()) {
			return TokenAccessGate.always(buildNominationTasksAndCandidateQuestionsAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate.START, accessDeniedKey));
		}
		if (currentNomination == null || currentNomination.getCandidate() == null) {
			return null;
		}
		if (earliestStart == null && latestEnd == null) {
			return TokenAccessGate.always(buildNominationTasksAndCandidateQuestionsAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate.START, accessDeniedKey));
		}

		TokenAccessGate.AccessBlock before = earliestStart != null
				? buildNominationTasksAndCandidateQuestionsAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate.START, earliestKey)
				: null;
		TokenAccessGate.AccessBlock after = latestEnd != null
				? buildNominationTasksAndCandidateQuestionsAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate.END, latestKey)
				: null;
		return TokenAccessGate.of(earliestStart, latestEnd, before, after);
	}

	private TokenAccessGate.AccessBlock buildNominationTasksAndCandidateQuestionsAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate targetDate, ElectionCalendarKey targetKey) {
		return new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.ACCESS_NOT_AVAILABLE,
				targetDate,
				targetKey,
				null,
				null);
	}
}
