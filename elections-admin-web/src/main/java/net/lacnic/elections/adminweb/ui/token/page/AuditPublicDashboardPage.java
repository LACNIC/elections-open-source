package net.lacnic.elections.adminweb.ui.token.page;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenTopHeaderPanel;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.results.audit.AuditorRevisionPanel;
import net.lacnic.elections.adminweb.ui.token.PublicCalendarTimelinePanel;
import net.lacnic.elections.adminweb.ui.token.PublicCalendarTimelinePanel.TimelineEntryView;
import net.lacnic.elections.adminweb.ui.token.TokenResourceKeys;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ReminderFrequency;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.Nomination;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class AuditPublicDashboardPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;
	private static final String BADGE_BASE_CLASS = "badge border text-uppercase fw-semibold";
	private static final String BADGE_COLOR_SUCCESS = "text-bg-success";
	private static final String BADGE_COLOR_WARNING = "text-bg-warning";
	private static final String BADGE_COLOR_SECONDARY = "text-bg-secondary";
	private static final String KEY_AUDIT_PUBLIC_V2_DECISION_APPROVED = "auditPublicV2DecisionApproved";
	private static final String KEY_AUDIT_PUBLIC_V2_DECISION_REJECTED = "auditPublicV2DecisionRejected";
	private static final String KEY_AUDIT_PUBLIC_V2_DECISION_NO_APPLY = "auditPublicV2DecisionNoApply";
	private static final String KEY_AUDIT_PUBLIC_V2_AVAILABILITY_PERIOD_NOT_CONFIGURED = "auditPublicV2AvailabilityPeriodNotConfigured";
	private static final String KEY_AUDIT_PUBLIC_V2_PHASE_CALENDAR = "auditPublicV2PhaseCalendar";
	private static final String KEY_AUDIT_PUBLIC_V2_PHASE_CALL = "auditPublicV2PhaseCall";
	private static final String KEY_AUDIT_PUBLIC_V2_PHASE_CENSUS = "auditPublicV2PhaseCensus";
	private static final String KEY_AUDIT_PUBLIC_V2_PHASE_CANDIDATES = "auditPublicV2PhaseCandidates";
	private static final String KEY_AUDIT_PUBLIC_V2_PHASE_VOTING = "auditPublicV2PhaseVoting";
	private static final String KEY_AUDIT_PUBLIC_V2_PHASE_AUDIT = "auditPublicV2PhaseAudit";
	private static final String KEY_AUDIT_PUBLIC_V2_PHASE_RESULTS = "auditPublicV2PhaseResults";
	private static final String KEY_AUDIT_PUBLIC_V2_UNTITLED_MILESTONE = "auditPublicV2UntitledMilestone";
	private static final String KEY_COMMISSIONERS_DESCRIPTION = "publicElectionModernCommissionersDescription";
	private static final String KEY_COMMISSIONERS_DESCRIPTION_WITHOUT_PREAPPROVAL = "publicElectionModernCommissionersDescriptionWithoutPreapproval";
	private Auditor auditor;
	private Election election;

	private List<TimelineEntryView> timelineItems = Collections.emptyList();
	private List<CandidateAuditRow> candidateRows = Collections.emptyList();
	private List<CommissionerParticipationRow> commissionerRows = Collections.emptyList();
	private List<Auditor> decisionAuditors = Collections.emptyList();
	private List<DecisionMatrixRow> decisionMatrixRows = Collections.emptyList();

	public AuditPublicDashboardPage() {
		this(new PageParameters());
	}

	public AuditPublicDashboardPage(PageParameters params) {
		super(params);

		if (auditor == null || election == null) {
			add(new PublicCalendarTimelinePanel("timelinePanel", Collections.<TimelineEntryView>emptyList(), true));
			add(buildEmptyAuditorRevisionPanel());
			add(buildEmptyCandidateAuditCard());
			add(buildEmptyAuditResultsContainer());
			add(buildEmptyCandidateAvailabilityCard());
			add(buildEmptyResultsAvailabilityCard());
			add(buildEmptyMatrixContainer());
			add(buildEmptyCommissionersCard());
			return;
		}

		loadData();

		add(new PublicCalendarTimelinePanel("timelinePanel", timelineItems, true));

		add(new AuditorRevisionPanel("auditorRevisionPanel", auditor));
		add(buildCandidateAuditCard());
		add(buildAuditResultsContainer());
		add(buildCandidateAvailabilityCard());
		add(buildResultsAvailabilityCard());
		add(buildMatrixContainer());
		add(buildCommissionersCard());
	}

	@Override
	protected boolean isGlobalFeedbackEnabled() {
		return true;
	}

	private WebMarkupContainer buildEmptyAuditorRevisionPanel() {
		WebMarkupContainer container = new WebMarkupContainer("auditorRevisionPanel");
		container.setVisible(false);
		container.setOutputMarkupPlaceholderTag(true);
		return container;
	}

	@Override
	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionAudit");
	}

	@Override
	protected PublicTokenTopHeaderPanel.NotificationMode resolveTopHeaderNotificationMode() {
		return PublicTokenTopHeaderPanel.NotificationMode.AUDIT;
	}

	@Override
	protected ReminderFrequency resolveTopHeaderReminderFrequency() {
		return auditor != null ? auditor.getReminderFrequency() : ReminderFrequency.defaultValue();
	}

	@Override
	protected Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params) {
		auditor = AppContext.getInstance().getVoterBeanRemote().verifyAuditorResultAccess(getToken());
		if (auditor == null) {
			return Error404.class;
		}

		election = auditor.getElection();
		if (election == null) {
			return Error404.class;
		}
		setElection(election);
		setWhereAmI("Tablero público de auditoría");
		setContextClass(Auditor.class.getName());
		setContextData("auditorId: " + auditor.getAuditorId() + "\n"
				+ "auditorName: " + auditor.getName() + "\n"
				+ "electionId: " + election.getElectionId() + "\n"
				+ "reminderFrequency: " + auditor.getReminderFrequency());

		setHeaderUserDisplay(auditor.getName());
		setHeaderNotificationStatusFromReminderFrequency(auditor.getReminderFrequency());
		return null;
	}

	@Override
	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		if (election != null && !election.isAuditorLinkAvailable()) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.ACCESS_NOT_AVAILABLE,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
					null,
					null));
		}
		ElectionCalendar calendar = findPublicCalendar(ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED);
		if (calendar == null || calendar.getStartDate() == null) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.AUDIT_NOT_PUBLIC,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
					null,
					null));
		}
		TokenAccessGate.AccessBlock before = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.AUDIT_NOT_PUBLIC,
				PublicAccessDeniedPage.CountdownTargetDate.START,
				ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
				null,
				null);
		return TokenAccessGate.of(calendar.getStartDate(), null, before, null);
	}

	private void loadData() {
		long electionId = election.getElectionId();
		timelineItems = buildTimelineItems(electionId);
		Map<Long, Map<Long, MatrixDecisionView>> matrix = buildDecisionMatrix(electionId);
		Map<Long, AuditorCandidateDecision> currentAuditorDecisions = buildCurrentAuditorDecisionMap(electionId);
		decisionAuditors = loadDecisionAuditors(electionId);
		candidateRows = buildCandidateRows(electionId, currentAuditorDecisions);
		commissionerRows = buildCommissionerParticipationRows(electionId, candidateRows);
		decisionMatrixRows = buildDecisionMatrixRows(candidateRows, decisionAuditors, matrix);
	}

	private List<TimelineEntryView> buildTimelineItems(long electionId) {
		List<ElectionCalendar> calendars = safeElectionCalendars(electionId);
		if (calendars.isEmpty()) {
			return Collections.emptyList();
		}

		List<ElectionCalendar> publicCalendars = new ArrayList<>();
		for (ElectionCalendar calendar : calendars) {
			if (calendar != null && isPublicCalendar(calendar)) {
				publicCalendars.add(calendar);
			}
		}

		if (publicCalendars.isEmpty()) {
			return Collections.emptyList();
		}

		publicCalendars.sort(new Comparator<ElectionCalendar>() {
			@Override
			public int compare(ElectionCalendar a, ElectionCalendar b) {
				Date aStart = a != null ? a.getStartDate() : null;
				Date bStart = b != null ? b.getStartDate() : null;
				if (aStart == null && bStart == null) {
					return compareCalendarKey(a, b);
				}
				if (aStart == null) {
					return 1;
				}
				if (bStart == null) {
					return -1;
				}
				int startCompare = aStart.compareTo(bStart);
				if (startCompare != 0) {
					return startCompare;
				}
				return compareCalendarKey(a, b);
			}
		});

		List<TimelineEntryView> items = new ArrayList<>();
		for (ElectionCalendar calendar : publicCalendars) {
			ElectionCalendarKey key = calendar.getCalendarKey();
			String description = resolveCalendarDescription(key);
			String title = extractTitle(description);
			String phase = resolvePhaseLabel(key);
			Date timelineEndDate = resolveTimelineEndDate(calendar);
			String schedule = buildScheduleLabel(calendar.getStartDate(), timelineEndDate);
			int sortOrder = key != null ? key.ordinal() : Integer.MAX_VALUE;
			items.add(new TimelineEntryView(calendar.getStartDate(), timelineEndDate, sortOrder, title, schedule, phase, null));
		}
		return items;
	}

	private int compareCalendarKey(ElectionCalendar a, ElectionCalendar b) {
		ElectionCalendarKey aKey = a != null ? a.getCalendarKey() : null;
		ElectionCalendarKey bKey = b != null ? b.getCalendarKey() : null;
		if (aKey == null && bKey == null) {
			return 0;
		}
		if (aKey == null) {
			return 1;
		}
		if (bKey == null) {
			return -1;
		}
		return Integer.compare(aKey.ordinal(), bKey.ordinal());
	}

	private Date resolveTimelineEndDate(ElectionCalendar calendar) {
		if (calendar == null || isSingleCalendarKey(calendar.getCalendarKey())) {
			return null;
		}
		return calendar.getEndDate();
	}

	private boolean isSingleCalendarKey(ElectionCalendarKey key) {
		return key != null && key.name().contains("_SINGLE_");
	}

	private Map<Long, Map<Long, MatrixDecisionView>> buildDecisionMatrix(long electionId) {
		Map<Long, Map<Long, MatrixDecisionView>> matrix = new HashMap<>();
		List<AuditorCandidateDecision> decisions = safeAuditorCandidateDecisions(electionId);
		for (AuditorCandidateDecision decision : decisions) {
			if (decision == null) {
				continue;
			}
			Long candidateId = readEntityLongField(readFieldValue(decision, "candidate"), "candidateId");
			Long auditorId = readEntityLongField(readFieldValue(decision, "auditor"), "auditorId");
			if (candidateId == null || auditorId == null) {
				continue;
			}
			matrix.computeIfAbsent(candidateId, key -> new HashMap<>()).put(auditorId, buildMatrixDecisionView(decision));
		}
		return matrix;
	}

	private Map<Long, AuditorCandidateDecision> buildCurrentAuditorDecisionMap(long electionId) {
		Map<Long, AuditorCandidateDecision> decisionsByCandidate = new HashMap<>();
		if (auditor == null) {
			return decisionsByCandidate;
		}
		Long currentAuditorId = Long.valueOf(auditor.getAuditorId());
		for (AuditorCandidateDecision decision : safeAuditorCandidateDecisions(electionId)) {
			if (decision == null) {
				continue;
			}
			Long decisionAuditorId = readEntityLongField(readFieldValue(decision, "auditor"), "auditorId");
			Long candidateId = readEntityLongField(readFieldValue(decision, "candidate"), "candidateId");
			if (currentAuditorId.equals(decisionAuditorId) && candidateId != null) {
				decisionsByCandidate.put(candidateId, decision);
			}
		}
		return decisionsByCandidate;
	}

	private MatrixDecisionView buildMatrixDecisionView(AuditorCandidateDecision decision) {
		if (decision == null) {
			return MatrixDecisionView.empty();
		}
		return new MatrixDecisionView(
				readDecisionStatusField(decision, "decisionStatus"),
				readDecisionStatusField(decision, "preDecisionStatus"),
				readDateField(decision, "preapprovedDate"),
				readDecisionStatusField(decision, "finalDecisionStatus"),
				readDateField(decision, "approvedDate"));
	}

	private AuditorCandidateDecisionStatus readDecisionStatusField(AuditorCandidateDecision decision, String fieldName) {
		Object statusValue = readFieldValue(decision, fieldName);
		return statusValue instanceof AuditorCandidateDecisionStatus ? (AuditorCandidateDecisionStatus) statusValue : null;
	}

	private Date readDateField(AuditorCandidateDecision decision, String fieldName) {
		Object dateValue = readFieldValue(decision, fieldName);
		return dateValue instanceof Date ? (Date) dateValue : null;
	}

	private List<Auditor> loadDecisionAuditors(long electionId) {
		List<Auditor> auditors = safeAuditors(electionId);
		if (auditors.isEmpty()) {
			return Collections.emptyList();
		}

		List<Auditor> commissioners = new ArrayList<>();
		for (Auditor auditorItem : auditors) {
			if (auditorItem != null && auditorItem.isCommissioner()) {
				commissioners.add(auditorItem);
			}
		}

		List<Auditor> source = commissioners.isEmpty() ? auditors : commissioners;
		source.sort(new Comparator<Auditor>() {
			@Override
			public int compare(Auditor a, Auditor b) {
				String aName = a != null && a.getName() != null ? a.getName().toLowerCase(Locale.ROOT) : "";
				String bName = b != null && b.getName() != null ? b.getName().toLowerCase(Locale.ROOT) : "";
				return aName.compareTo(bName);
			}
		});
		return source;
	}

	private List<CandidateAuditRow> buildCandidateRows(long electionId, Map<Long, AuditorCandidateDecision> currentAuditorDecisions) {
		List<Nomination> nominations = safeNominations(electionId);
		if (nominations.isEmpty()) {
			return Collections.emptyList();
		}

		List<CandidateAuditRow> rows = new ArrayList<>();
		Set<Long> seenCandidateIds = new HashSet<>();
		for (Nomination nomination : nominations) {
			if (nomination == null || nomination.getCandidate() == null) {
				continue;
			}
			Candidate baseCandidate = nomination.getCandidate();
			if (baseCandidate.getCandidateId() <= 0 || !seenCandidateIds.add(baseCandidate.getCandidateId())) {
				continue;
			}
			Candidate detailedCandidate = resolveDetailedCandidate(baseCandidate.getCandidateId(), baseCandidate);
			if (detailedCandidate == null) {
				continue;
			}
			if (!isVisibleCandidateStatusForAuditor(detailedCandidate.getStatus())) {
				continue;
			}

			DecisionBadge badge = toCandidateStatusBadge(detailedCandidate.getStatus());
			Long candidateId = Long.valueOf(detailedCandidate.getCandidateId());
			AuditorCandidateDecision currentAuditorDecision = currentAuditorDecisions != null ? currentAuditorDecisions.get(candidateId) : null;

			rows.add(new CandidateAuditRow(
					detailedCandidate.getCandidateId(),
					detailedCandidate.getCandidateOrder(),
					detailedCandidate.getName(),
					detailedCandidate.getStatus(),
					detailedCandidate.getPictureInfo(),
					detailedCandidate.getPictureExtension(),
					badge.getText(),
					badge.getCssClass(),
					resolveCandidateActionText(detailedCandidate.getStatus(), currentAuditorDecision),
					resolveCandidateActionCssClass(detailedCandidate.getStatus(), currentAuditorDecision)));
		}

		rows.sort(new Comparator<CandidateAuditRow>() {
			@Override
			public int compare(CandidateAuditRow a, CandidateAuditRow b) {
				int orderCompare = Integer.compare(a.getCandidateOrder(), b.getCandidateOrder());
				if (orderCompare != 0) {
					return orderCompare;
				}
				String aName = a.getCandidateName() != null ? a.getCandidateName().toLowerCase(Locale.ROOT) : "";
				String bName = b.getCandidateName() != null ? b.getCandidateName().toLowerCase(Locale.ROOT) : "";
				return aName.compareTo(bName);
			}
		});
		return rows;
	}

	private boolean isVisibleCandidateStatusForAuditor(CandidateStatus status) {
		return status == CandidateStatus.PRECOMPLETE
				|| status == CandidateStatus.CONFIRMED_AND_PUBLISHED
				|| status == CandidateStatus.COMPLETE
				|| status == CandidateStatus.REJECTED;
	}

	private String resolveCandidateActionText(CandidateStatus status, AuditorCandidateDecision decision) {
		if (isCandidateActionRequired(status, decision) && status == CandidateStatus.PRECOMPLETE) {
			return getString("auditPublicV2FirstVerificationAction");
		}
		if (isCandidateActionRequired(status, decision) && status == CandidateStatus.COMPLETE) {
			return getString("auditPublicV2FinalVerificationAction");
		}
		return getString("auditPublicV2ViewDetail");
	}

	private String resolveCandidateActionCssClass(CandidateStatus status, AuditorCandidateDecision decision) {
		return isCandidateActionRequired(status, decision) ? "btn btn-sm btn-warning" : "btn btn-sm btn-secondary";
	}

	private boolean isCandidateActionRequired(CandidateStatus status, AuditorCandidateDecision decision) {
		if (auditor == null || !auditor.isCommissioner()) {
			return false;
		}
		return (status == CandidateStatus.PRECOMPLETE && !hasPreStageReaction(decision, status))
				|| (status == CandidateStatus.COMPLETE && !hasFinalStageReaction(decision, status));
	}

	private boolean hasPreStageReaction(AuditorCandidateDecision decision, CandidateStatus candidateStatus) {
		if (decision == null) {
			return false;
		}
		AuditorCandidateDecisionStatus status = readDecisionStatusField(decision, "preDecisionStatus");
		if (isReactionStatus(status)) {
			return true;
		}
		AuditorCandidateDecisionStatus legacyStatus = readDecisionStatusField(decision, "decisionStatus");
		if (legacyStatus == AuditorCandidateDecisionStatus.PREAPPROVED) {
			return true;
		}
		if (legacyStatus == AuditorCandidateDecisionStatus.REJECTED
				&& isLikelyLegacyPrecompleteRejectedMilestone(
						readDateField(decision, "preapprovedDate"),
						readDateField(decision, "approvedDate"))) {
			return true;
		}
		return candidateStatus == CandidateStatus.PRECOMPLETE
				&& legacyStatus == AuditorCandidateDecisionStatus.REJECTED;
	}

	private boolean hasFinalStageReaction(AuditorCandidateDecision decision, CandidateStatus candidateStatus) {
		if (decision == null) {
			return false;
		}
		AuditorCandidateDecisionStatus status = readDecisionStatusField(decision, "finalDecisionStatus");
		if (isReactionStatus(status)) {
			return true;
		}
		AuditorCandidateDecisionStatus legacyStatus = readDecisionStatusField(decision, "decisionStatus");
		if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED) {
			return true;
		}
		if (legacyStatus == AuditorCandidateDecisionStatus.REJECTED
				&& readDateField(decision, "approvedDate") != null
				&& !isLikelyLegacyPrecompleteRejectedMilestone(
						readDateField(decision, "preapprovedDate"),
						readDateField(decision, "approvedDate"))) {
			return true;
		}
		return false;
	}

	private boolean isReactionStatus(AuditorCandidateDecisionStatus status) {
		return status == AuditorCandidateDecisionStatus.PREAPPROVED
				|| status == AuditorCandidateDecisionStatus.APPROVED
				|| status == AuditorCandidateDecisionStatus.REJECTED;
	}

	private List<DecisionMatrixRow> buildDecisionMatrixRows(List<CandidateAuditRow> rows, List<Auditor> auditors,
			Map<Long, Map<Long, MatrixDecisionView>> matrix) {
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}

		List<DecisionMatrixRow> result = new ArrayList<>();
		for (CandidateAuditRow row : rows) {
			Map<Long, MatrixDecisionView> decisionByAuditor = matrix.get(row.getCandidateId());
			List<DecisionCellView> cells = new ArrayList<>();

			for (Auditor auditorItem : auditors) {
				Long auditorId = auditorItem != null ? auditorItem.getAuditorId() : null;
				MatrixDecisionView decision = auditorId != null && decisionByAuditor != null ? decisionByAuditor.get(auditorId) : MatrixDecisionView.empty();
				cells.add(buildDecisionCell(row.getCandidateStatus(), decision));
			}

			result.add(new DecisionMatrixRow(row.getCandidateName(), cells));
		}
		return result;
	}

	private DecisionCellView buildDecisionCell(CandidateStatus candidateStatus, MatrixDecisionView decision) {
		MatrixDecisionView safeDecision = decision != null ? decision : MatrixDecisionView.empty();
		List<DecisionBadge> badges = new ArrayList<>();
		AuditorCandidateDecisionStatus preStatus = resolvePreMatrixDecisionStatus(safeDecision, candidateStatus);
		if (isPreDecisionMatrixVisible(candidateStatus, preStatus)) {
			badges.add(toMatrixDecisionBadge(preStatus, MatrixDecisionStage.PRECOMPLETE));
		}
		AuditorCandidateDecisionStatus finalStatus = resolveFinalMatrixDecisionStatus(safeDecision, candidateStatus);
		if (isFinalDecisionMatrixVisible(candidateStatus, finalStatus)) {
			badges.add(toMatrixDecisionBadge(finalStatus, MatrixDecisionStage.COMPLETE));
		}
		if (badges.isEmpty()) {
			badges.add(toDecisionBadge(null));
		}
		return new DecisionCellView(badges);
	}

	private AuditorCandidateDecisionStatus resolvePreMatrixDecisionStatus(MatrixDecisionView decision, CandidateStatus candidateStatus) {
		if (decision == null) {
			return null;
		}
		AuditorCandidateDecisionStatus status = decision.getPreStatus();
		if (status == null) {
			AuditorCandidateDecisionStatus legacyStatus = decision.getLegacyStatus();
			if (legacyStatus == AuditorCandidateDecisionStatus.PREAPPROVED
					|| (legacyStatus == AuditorCandidateDecisionStatus.REJECTED
							&& isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate()))) {
				status = legacyStatus;
			}
		}
		if (candidateStatus == CandidateStatus.PRECOMPLETE && status == null) {
			status = decision.getLegacyStatus();
			if (status == AuditorCandidateDecisionStatus.APPROVED || status == AuditorCandidateDecisionStatus.NO_APPLY) {
				status = null;
			}
		}
		return status;
	}

	private AuditorCandidateDecisionStatus resolveFinalMatrixDecisionStatus(MatrixDecisionView decision, CandidateStatus candidateStatus) {
		if (decision == null) {
			return null;
		}
		AuditorCandidateDecisionStatus status = decision.getFinalStatus();
		if (status == null) {
			AuditorCandidateDecisionStatus legacyStatus = decision.getLegacyStatus();
			if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED
					|| (legacyStatus == AuditorCandidateDecisionStatus.REJECTED
							&& decision.getApprovedDate() != null
							&& !isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate()))) {
				status = legacyStatus;
			}
		}
		if (candidateStatus == CandidateStatus.COMPLETE && status == null) {
			status = decision.getLegacyStatus() == AuditorCandidateDecisionStatus.APPROVED ? AuditorCandidateDecisionStatus.APPROVED : null;
		}
		return status;
	}

	private boolean isPreDecisionMatrixVisible(CandidateStatus candidateStatus, AuditorCandidateDecisionStatus status) {
		if (candidateStatus == CandidateStatus.PRECOMPLETE) {
			return true;
		}
		return isReactionStatus(status);
	}

	private boolean isFinalDecisionMatrixVisible(CandidateStatus candidateStatus, AuditorCandidateDecisionStatus status) {
		if (candidateStatus == CandidateStatus.COMPLETE
				|| candidateStatus == CandidateStatus.CONFIRMED_AND_PUBLISHED
				|| candidateStatus == CandidateStatus.REJECTED) {
			return true;
		}
		return isReactionStatus(status);
	}

	private WebMarkupContainer buildCandidatesContainer() {
		WebMarkupContainer container = new WebMarkupContainer("candidatesTableContainer");
		container.setVisible(!candidateRows.isEmpty());
		container.setOutputMarkupPlaceholderTag(true);

		container.add(new ListView<CandidateAuditRow>("candidateRows", candidateRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CandidateAuditRow> item) {
				CandidateAuditRow row = item.getModelObject();
				item.add(new Label("candidateName", Model.of(row.getCandidateName())));
				item.add(buildCandidatePicture("candidatePicture", row));

				Label myDecisionBadge = new Label("myDecisionBadge", row.getMyDecisionText());
				myDecisionBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getMyDecisionCssClass()));
				item.add(myDecisionBadge);

				PageParameters params = buildTokenPageParameters();
				params.add(UtilsParameters.getCandidateText(), row.getCandidateId());
				BookmarkablePageLink<Void> detailLink = new BookmarkablePageLink<>("viewDetailLink", AuditPublicCandidateDetailPage.class, params);
				detailLink.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, row.getActionCssClass()));
				detailLink.add(new Label("viewDetailLinkLabel", row.getActionText()));
				item.add(detailLink);
			}
		});
		return container;
	}

	private WebMarkupContainer buildCandidateAuditCard() {
		WebMarkupContainer container = new WebMarkupContainer("candidateAuditCard");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(isCandidateAuditCardVisible());
		Date firstReactionDeadline = resolveFirstCandidateReactionDeadline();
		Date finalDecisionDeadline = resolveFinalCandidateDecisionDeadline();
		WebMarkupContainer firstReactionDeadlineContainer = new WebMarkupContainer("candidateFirstReactionDeadlineContainer");
		firstReactionDeadlineContainer.setOutputMarkupPlaceholderTag(true);
		firstReactionDeadlineContainer.setVisible(hasEvaluationsValidationPeriod());
		firstReactionDeadlineContainer.add(new Label("candidateFirstReactionDeadlineDate", resolveDeadlineDateLabel(firstReactionDeadline)));
		firstReactionDeadlineContainer.add(new Label("candidateFirstReactionDeadlineRemaining", resolveDecisionDeadlineRemaining(firstReactionDeadline)));
		container.add(firstReactionDeadlineContainer);
		container.add(new Label("candidateFinalDecisionDeadlineDate", resolveDeadlineDateLabel(finalDecisionDeadline)));
		container.add(new Label("candidateFinalDecisionDeadlineRemaining", resolveDecisionDeadlineRemaining(finalDecisionDeadline)));
		container.add(buildCandidatesContainer());
		container.add(buildCandidateRowsEmptyContainer());
		return container;
	}

	private WebMarkupContainer buildEmptyCandidateAuditCard() {
		WebMarkupContainer container = new WebMarkupContainer("candidateAuditCard");
		container.setVisible(false);
		container.setOutputMarkupPlaceholderTag(true);
		WebMarkupContainer firstReactionDeadlineContainer = new WebMarkupContainer("candidateFirstReactionDeadlineContainer");
		firstReactionDeadlineContainer.setVisible(false);
		firstReactionDeadlineContainer.setOutputMarkupPlaceholderTag(true);
		firstReactionDeadlineContainer.add(new Label("candidateFirstReactionDeadlineDate", "-"));
		firstReactionDeadlineContainer.add(new Label("candidateFirstReactionDeadlineRemaining", "-"));
		container.add(firstReactionDeadlineContainer);
		container.add(new Label("candidateFinalDecisionDeadlineDate", "-"));
		container.add(new Label("candidateFinalDecisionDeadlineRemaining", "-"));
		container.add(buildEmptyCandidatesContainer());
		WebMarkupContainer emptyRowsContainer = new WebMarkupContainer("candidateRowsEmptyContainer");
		emptyRowsContainer.setVisible(false);
		emptyRowsContainer.setOutputMarkupPlaceholderTag(true);
		container.add(emptyRowsContainer);
		return container;
	}

	private boolean isCandidateAuditCardVisible() {
		return isCandidateAuditEnabledForCurrentElection();
	}

	private boolean isCandidateAuditEnabledForCurrentElection() {
		return resolveCandidateAuditWindowState().getStatus() == AvailabilityStatus.ACTIVE;
	}

	private WebMarkupContainer buildEmptyCandidatesContainer() {
		WebMarkupContainer container = new WebMarkupContainer("candidatesTableContainer");
		container.setVisible(false);
		container.setOutputMarkupPlaceholderTag(true);
		container.add(new ListView<CandidateAuditRow>("candidateRows", Collections.<CandidateAuditRow>emptyList()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<CandidateAuditRow> item) {
				// Intencionalmente vacio: placeholder de Wicket cuando no hay candidatos.
			}
		});
		return container;
	}

	private WebMarkupContainer buildCandidateRowsEmptyContainer() {
		WebMarkupContainer container = new WebMarkupContainer("candidateRowsEmptyContainer");
		container.setVisible(candidateRows.isEmpty());
		container.setOutputMarkupPlaceholderTag(true);
		return container;
	}

	private WebMarkupContainer buildCommissionersCard() {
		WebMarkupContainer container = new WebMarkupContainer("commissionersCard");
		container.setVisible(isCandidateFormsVerificationDefined());
		container.setOutputMarkupPlaceholderTag(true);
		final boolean showPreapprovalColumn = hasEvaluationsValidationPeriod();

		String commissionersDescriptionKey = showPreapprovalColumn
				? KEY_COMMISSIONERS_DESCRIPTION
				: KEY_COMMISSIONERS_DESCRIPTION_WITHOUT_PREAPPROVAL;
		container.add(new Label("commissionersDescription", getString(commissionersDescriptionKey)));

		WebMarkupContainer commissionerEmpty = new WebMarkupContainer("commissionerEmpty");
		commissionerEmpty.setVisible(commissionerRows.isEmpty());
		container.add(commissionerEmpty);

		WebMarkupContainer commissionerTable = new WebMarkupContainer("commissionerTable");
		commissionerTable.setOutputMarkupPlaceholderTag(true);
		commissionerTable.setVisible(!commissionerRows.isEmpty());
		WebMarkupContainer commissionerPreapprovalHeader = new WebMarkupContainer("commissionerPreapprovalHeader");
		commissionerPreapprovalHeader.setVisible(showPreapprovalColumn);
		commissionerTable.add(commissionerPreapprovalHeader);
		commissionerTable.add(new ListView<CommissionerParticipationRow>("commissionerRows", commissionerRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CommissionerParticipationRow> item) {
				CommissionerParticipationRow row = item.getModelObject();
				item.add(new Label("commissionerName", row.getCommissionerName()));
				Label commissionerPreapproval = new Label("commissionerPreapproval", row.getPreapprovalLabel());
				commissionerPreapproval.setVisible(showPreapprovalColumn);
				item.add(commissionerPreapproval);
				item.add(new Label("commissionerApproval", row.getApprovalLabel()));
				Label electionAuditBadge = new Label("commissionerElectionAudit", row.getElectionAuditLabel());
				electionAuditBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getElectionAuditCssClass()));
				item.add(electionAuditBadge);
			}
		});
		container.add(commissionerTable);

		return container;
	}

	private WebMarkupContainer buildEmptyCommissionersCard() {
		WebMarkupContainer container = new WebMarkupContainer("commissionersCard");
		container.setVisible(false);
		container.setOutputMarkupPlaceholderTag(true);
		container.add(new Label("commissionersDescription", "-"));

		WebMarkupContainer commissionerEmpty = new WebMarkupContainer("commissionerEmpty");
		commissionerEmpty.setVisible(false);
		container.add(commissionerEmpty);

		WebMarkupContainer commissionerTable = new WebMarkupContainer("commissionerTable");
		commissionerTable.setVisible(false);
		commissionerTable.setOutputMarkupPlaceholderTag(true);
		WebMarkupContainer commissionerPreapprovalHeader = new WebMarkupContainer("commissionerPreapprovalHeader");
		commissionerPreapprovalHeader.setVisible(false);
		commissionerTable.add(commissionerPreapprovalHeader);
		commissionerTable.add(new ListView<CommissionerParticipationRow>("commissionerRows", Collections.<CommissionerParticipationRow>emptyList()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CommissionerParticipationRow> item) {
				// Intencionalmente vacio: placeholder de Wicket cuando no hay comisionados.
			}
		});
		container.add(commissionerTable);
		return container;
	}

	private List<CommissionerParticipationRow> buildCommissionerParticipationRows(long electionId, List<CandidateAuditRow> visibleCandidateRows) {
		Set<Long> visibleCandidateIds = new HashSet<Long>();
		for (CandidateAuditRow candidateRow : visibleCandidateRows != null ? visibleCandidateRows : Collections.<CandidateAuditRow>emptyList()) {
			if (candidateRow == null || candidateRow.getCandidateId() <= 0L) {
				continue;
			}
			visibleCandidateIds.add(candidateRow.getCandidateId());
		}
		int totalCandidates = visibleCandidateIds.size();

		Map<Long, Set<Long>> preapprovedByAuditor = new HashMap<Long, Set<Long>>();
		Map<Long, Set<Long>> approvedByAuditor = new HashMap<Long, Set<Long>>();
		for (AuditorCandidateDecision decision : safeAuditorCandidateDecisions(electionId)) {
			if (decision == null || decision.getAuditor() == null || decision.getCandidate() == null) {
				continue;
			}
			Long candidateId = decision.getCandidate().getCandidateId();
			if (candidateId == null || !visibleCandidateIds.contains(candidateId)) {
				continue;
			}
			long auditorId = decision.getAuditor().getAuditorId();
			if (hasPreapprovalDecision(decision)) {
				preapprovedByAuditor.computeIfAbsent(Long.valueOf(auditorId), key -> new HashSet<Long>()).add(candidateId);
			}
			if (hasFinalApprovalDecision(decision)) {
				approvedByAuditor.computeIfAbsent(Long.valueOf(auditorId), key -> new HashSet<Long>()).add(candidateId);
			}
		}

		List<CommissionerParticipationRow> rows = new ArrayList<CommissionerParticipationRow>();
		for (Auditor auditorItem : safeAuditors(electionId)) {
			if (auditorItem == null || !auditorItem.isCommissioner()) {
				continue;
			}
			long auditorId = auditorItem.getAuditorId();
			int preapprovedCount = preapprovedByAuditor.containsKey(Long.valueOf(auditorId))
					? preapprovedByAuditor.get(Long.valueOf(auditorId)).size()
					: 0;
			int approvedCount = approvedByAuditor.containsKey(Long.valueOf(auditorId))
					? approvedByAuditor.get(Long.valueOf(auditorId)).size()
					: 0;
			boolean electionAuditCompleted = auditorItem.isAgreedConformity();
			String electionAuditLabel = electionAuditCompleted
					? getString("publicElectionCommissionerAuditCompleted")
					: getString("publicElectionCommissionerAuditPending");
			rows.add(new CommissionerParticipationRow(
					valueOrPlaceholder(auditorItem.getName()),
					preapprovedCount,
					totalCandidates,
					approvedCount,
					totalCandidates,
					electionAuditCompleted,
					electionAuditLabel));
		}

		rows.sort(new Comparator<CommissionerParticipationRow>() {
			@Override
			public int compare(CommissionerParticipationRow a, CommissionerParticipationRow b) {
				String left = a != null ? valueOrPlaceholder(a.getCommissionerName()) : "";
				String right = b != null ? valueOrPlaceholder(b.getCommissionerName()) : "";
				return left.compareToIgnoreCase(right);
			}
		});
		return rows;
	}

	private boolean hasEvaluationsValidationPeriod() {
		return toCalendarWindow(findPublicCalendar(ElectionCalendarKey.N_8_PERIODO_EVALUATIONS_VALIDATION)) != null;
	}

	private boolean hasPreapprovalDecision(AuditorCandidateDecision decision) {
		if (decision == null) {
			return false;
		}
		AuditorCandidateDecisionStatus preStatus = readDecisionStatusField(decision, "preDecisionStatus");
		if (preStatus != null) {
			return preStatus == AuditorCandidateDecisionStatus.PREAPPROVED;
		}
		AuditorCandidateDecisionStatus legacyStatus = readDecisionStatusField(decision, "decisionStatus");
		if (legacyStatus == AuditorCandidateDecisionStatus.PREAPPROVED) {
			return true;
		}
		if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED && decision.getPreapprovedDate() != null) {
			return true;
		}
		return legacyStatus == AuditorCandidateDecisionStatus.REJECTED
				&& decision.getPreapprovedDate() != null
				&& decision.getApprovedDate() != null
				&& !isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate());
	}

	private boolean hasFinalApprovalDecision(AuditorCandidateDecision decision) {
		if (decision == null) {
			return false;
		}
		AuditorCandidateDecisionStatus finalStatus = readDecisionStatusField(decision, "finalDecisionStatus");
		if (finalStatus != null) {
			return finalStatus == AuditorCandidateDecisionStatus.APPROVED;
		}
		return readDecisionStatusField(decision, "decisionStatus") == AuditorCandidateDecisionStatus.APPROVED;
	}

	private boolean isLikelyLegacyPrecompleteRejectedMilestone(Date preapprovedDate, Date approvedDate) {
		if (preapprovedDate == null || approvedDate == null) {
			return false;
		}
		long diffMillis = Math.abs(preapprovedDate.getTime() - approvedDate.getTime());
		return diffMillis <= 1000L;
	}

	private WebMarkupContainer buildAuditResultsContainer() {
		WebMarkupContainer container = new WebMarkupContainer("auditResultsContainer");
		container.setVisible(isAuditResultsEnabledForCurrentElection());
		container.setOutputMarkupPlaceholderTag(true);
		Date resultsDeadline = resolveResultsActionDeadline();
		container.add(new Label("resultsActionDeadlineDate", resolveDeadlineDateLabel(resultsDeadline)));
		container.add(new Label("resultsActionDeadlineRemaining", resolveDecisionDeadlineRemaining(resultsDeadline)));

		boolean auditResultsCompleted = isAuditResultsCompletedByCurrentAuditor();

		WebMarkupContainer pendingContainer = new WebMarkupContainer("auditResultsPendingContainer");
		pendingContainer.setOutputMarkupPlaceholderTag(true);
		pendingContainer.setVisible(!auditResultsCompleted);
		PageParameters resultsParams = buildTokenPageParameters();
		pendingContainer.add(new BookmarkablePageLink<Void>("auditResultsLink", AuditPublicResultsPage.class, resultsParams));
		container.add(pendingContainer);

		WebMarkupContainer doneContainer = new WebMarkupContainer("auditResultsDoneContainer");
		doneContainer.setOutputMarkupPlaceholderTag(true);
		doneContainer.setVisible(auditResultsCompleted);
		container.add(doneContainer);

		return container;
	}

	private boolean isAuditResultsEnabledForCurrentElection() {
		return resolveResultsAuditWindowState().getStatus() == AvailabilityStatus.ACTIVE;
	}

	private boolean isAuditResultsCompletedByCurrentAuditor() {
		return auditor != null && auditor.isCommissioner() && auditor.isAgreedConformity();
	}

	private WebMarkupContainer buildEmptyAuditResultsContainer() {
		WebMarkupContainer container = new WebMarkupContainer("auditResultsContainer");
		container.setVisible(false);
		container.setOutputMarkupPlaceholderTag(true);
		container.add(new Label("resultsActionDeadlineDate", "-"));
		container.add(new Label("resultsActionDeadlineRemaining", "-"));

		WebMarkupContainer pendingContainer = new WebMarkupContainer("auditResultsPendingContainer");
		pendingContainer.setVisible(false);
		pendingContainer.setOutputMarkupPlaceholderTag(true);
		BookmarkablePageLink<Void> auditResultsLink = new BookmarkablePageLink<Void>("auditResultsLink", AuditPublicResultsPage.class, new PageParameters());
		auditResultsLink.setVisible(false);
		pendingContainer.add(auditResultsLink);
		container.add(pendingContainer);

		WebMarkupContainer doneContainer = new WebMarkupContainer("auditResultsDoneContainer");
		doneContainer.setVisible(false);
		doneContainer.setOutputMarkupPlaceholderTag(true);
		container.add(doneContainer);

		return container;
	}

	private WebMarkupContainer buildCandidateAvailabilityCard() {
		WebMarkupContainer container = new WebMarkupContainer("candidateAvailabilityCard");
		container.setOutputMarkupPlaceholderTag(true);
		AvailabilityWindowState candidateWindowState = resolveCandidateAuditWindowState();
		container.setVisible(isCandidateFormsVerificationDefined()
				&& candidateWindowState.getStatus() != AvailabilityStatus.ACTIVE);

		Label candidateStatusBadge = new Label("candidateAvailabilityStatusBadge", resolveAvailabilityStatusText(candidateWindowState.getStatus()));
		candidateStatusBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveAvailabilityBadgeClass(candidateWindowState.getStatus())));
		container.add(candidateStatusBadge);
		Label candidateAvailabilityPeriodNotConfigured = new Label(
				"candidateAvailabilityPeriodNotConfigured",
				resolveAvailabilityPeriodNotConfiguredLabel(candidateWindowState));
		candidateAvailabilityPeriodNotConfigured.setOutputMarkupPlaceholderTag(true);
		candidateAvailabilityPeriodNotConfigured.setVisible(candidateWindowState.getStatus() == AvailabilityStatus.NOT_CONFIGURED);
		container.add(candidateAvailabilityPeriodNotConfigured);

		WebMarkupContainer candidateAvailabilityPeriodContent = new WebMarkupContainer("candidateAvailabilityPeriodContent");
		candidateAvailabilityPeriodContent.setOutputMarkupPlaceholderTag(true);
		candidateAvailabilityPeriodContent.setVisible(candidateWindowState.getStatus() != AvailabilityStatus.NOT_CONFIGURED);
		candidateAvailabilityPeriodContent.add(new Label("candidateAvailabilityStartDate", resolveAvailabilityDateLabel(candidateWindowState.getStartDate())));
		candidateAvailabilityPeriodContent.add(new Label("candidateAvailabilityEndDate", resolveAvailabilityDateLabel(candidateWindowState.getEndDate())));
		container.add(candidateAvailabilityPeriodContent);
		return container;
	}

	private WebMarkupContainer buildResultsAvailabilityCard() {
		WebMarkupContainer container = new WebMarkupContainer("resultsAvailabilityCard");
		container.setOutputMarkupPlaceholderTag(true);
		AvailabilityWindowState resultsWindowState = resolveResultsAuditWindowState();
		container.setVisible(isCeAuditDefined()
				&& resultsWindowState.getStatus() != AvailabilityStatus.ACTIVE);

		Label resultsStatusBadge = new Label("resultsAvailabilityStatusBadge", resolveAvailabilityStatusText(resultsWindowState.getStatus()));
		resultsStatusBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveAvailabilityBadgeClass(resultsWindowState.getStatus())));
		container.add(resultsStatusBadge);
		Label resultsAvailabilityPeriodNotConfigured = new Label(
				"resultsAvailabilityPeriodNotConfigured",
				resolveAvailabilityPeriodNotConfiguredLabel(resultsWindowState));
		resultsAvailabilityPeriodNotConfigured.setOutputMarkupPlaceholderTag(true);
		resultsAvailabilityPeriodNotConfigured.setVisible(resultsWindowState.getStatus() == AvailabilityStatus.NOT_CONFIGURED);
		container.add(resultsAvailabilityPeriodNotConfigured);

		WebMarkupContainer resultsAvailabilityPeriodContent = new WebMarkupContainer("resultsAvailabilityPeriodContent");
		resultsAvailabilityPeriodContent.setOutputMarkupPlaceholderTag(true);
		resultsAvailabilityPeriodContent.setVisible(resultsWindowState.getStatus() != AvailabilityStatus.NOT_CONFIGURED);
		resultsAvailabilityPeriodContent.add(new Label("resultsAvailabilityStartDate", resolveAvailabilityDateLabel(resultsWindowState.getStartDate())));
		resultsAvailabilityPeriodContent.add(new Label("resultsAvailabilityEndDate", resolveAvailabilityDateLabel(resultsWindowState.getEndDate())));
		container.add(resultsAvailabilityPeriodContent);
		return container;
	}

	private WebMarkupContainer buildEmptyCandidateAvailabilityCard() {
		WebMarkupContainer container = new WebMarkupContainer("candidateAvailabilityCard");
		container.setVisible(false);
		container.setOutputMarkupPlaceholderTag(true);
		container.add(new Label("candidateAvailabilityStatusBadge", "-"));
		Label candidateAvailabilityPeriodNotConfigured = new Label("candidateAvailabilityPeriodNotConfigured", "-");
		candidateAvailabilityPeriodNotConfigured.setVisible(false);
		candidateAvailabilityPeriodNotConfigured.setOutputMarkupPlaceholderTag(true);
		container.add(candidateAvailabilityPeriodNotConfigured);
		WebMarkupContainer candidateAvailabilityPeriodContent = new WebMarkupContainer("candidateAvailabilityPeriodContent");
		candidateAvailabilityPeriodContent.setVisible(false);
		candidateAvailabilityPeriodContent.setOutputMarkupPlaceholderTag(true);
		candidateAvailabilityPeriodContent.add(new Label("candidateAvailabilityStartDate", "-"));
		candidateAvailabilityPeriodContent.add(new Label("candidateAvailabilityEndDate", "-"));
		container.add(candidateAvailabilityPeriodContent);
		return container;
	}

	private WebMarkupContainer buildEmptyResultsAvailabilityCard() {
		WebMarkupContainer container = new WebMarkupContainer("resultsAvailabilityCard");
		container.setVisible(false);
		container.setOutputMarkupPlaceholderTag(true);
		container.add(new Label("resultsAvailabilityStatusBadge", "-"));
		Label resultsAvailabilityPeriodNotConfigured = new Label("resultsAvailabilityPeriodNotConfigured", "-");
		resultsAvailabilityPeriodNotConfigured.setVisible(false);
		resultsAvailabilityPeriodNotConfigured.setOutputMarkupPlaceholderTag(true);
		container.add(resultsAvailabilityPeriodNotConfigured);
		WebMarkupContainer resultsAvailabilityPeriodContent = new WebMarkupContainer("resultsAvailabilityPeriodContent");
		resultsAvailabilityPeriodContent.setVisible(false);
		resultsAvailabilityPeriodContent.setOutputMarkupPlaceholderTag(true);
		resultsAvailabilityPeriodContent.add(new Label("resultsAvailabilityStartDate", "-"));
		resultsAvailabilityPeriodContent.add(new Label("resultsAvailabilityEndDate", "-"));
		container.add(resultsAvailabilityPeriodContent);
		return container;
	}

	private AvailabilityWindowState resolveCandidateAuditWindowState() {
		if (election == null || !election.isAuditorLinkAvailable()) {
			return AvailabilityWindowState.notConfigured();
		}
		CalendarWindow n7 = toCalendarWindow(findPublicCalendar(ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION));
		if (n7 == null) {
			return AvailabilityWindowState.notConfigured();
		}
		CalendarWindow n8 = toCalendarWindow(findPublicCalendar(ElectionCalendarKey.N_8_PERIODO_EVALUATIONS_VALIDATION));

		Date earliestStart = null;
		Date latestEnd = null;
		boolean activeNow = false;
		Date now = new Date();

		CalendarWindow[] windows = { n7, n8 };
		for (CalendarWindow window : windows) {
			if (window == null) {
				continue;
			}
			Date start = window.getStartDate();
			Date end = window.getEndDate();
			if (earliestStart == null || start.before(earliestStart)) {
				earliestStart = start;
			}
			if (latestEnd == null || end.after(latestEnd)) {
				latestEnd = end;
			}
			if (!now.before(start) && !now.after(end)) {
				activeNow = true;
			}
		}

		if (earliestStart == null) {
			return AvailabilityWindowState.notConfigured();
		}
		if (activeNow) {
			return AvailabilityWindowState.active(earliestStart, latestEnd);
		}

		if (now.before(earliestStart)) {
			return AvailabilityWindowState.upcoming(earliestStart, latestEnd);
		}
		if (now.after(latestEnd)) {
			return AvailabilityWindowState.closed(earliestStart, latestEnd);
		}
		return AvailabilityWindowState.upcoming(earliestStart, latestEnd);
	}

	private AvailabilityWindowState resolveResultsAuditWindowState() {
		if (election == null || !election.isAuditorLinkAvailable()) {
			return AvailabilityWindowState.notConfigured();
		}
		CalendarWindow auditWindow = toCalendarWindow(findPublicCalendar(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT));
		if (auditWindow == null) {
			return AvailabilityWindowState.notConfigured();
		}
		Date start = auditWindow.getStartDate();
		Date end = auditWindow.getEndDate();
		Date now = new Date();
		if (now.before(start)) {
			return AvailabilityWindowState.upcoming(start, end);
		}
		if (now.after(end)) {
			return AvailabilityWindowState.closed(start, end);
		}
		return AvailabilityWindowState.active(start, end);
	}

	private Date resolveFirstCandidateReactionDeadline() {
		Date n7Deadline = resolveCalendarDeadline(ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION);
		if (n7Deadline != null) {
			return n7Deadline;
		}
		return resolveCalendarDeadline(ElectionCalendarKey.N_6_PERIODO_ADDITIONAL_EVALUATION);
	}

	private Date resolveFinalCandidateDecisionDeadline() {
		Date n7Deadline = resolveCalendarDeadline(ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION);
		Date n8Deadline = resolveCalendarDeadline(ElectionCalendarKey.N_8_PERIODO_EVALUATIONS_VALIDATION);
		if (n7Deadline == null) {
			return n8Deadline;
		}
		if (n8Deadline == null) {
			return n7Deadline;
		}
		return n7Deadline.after(n8Deadline) ? n7Deadline : n8Deadline;
	}

	private Date resolveCalendarDeadline(ElectionCalendarKey key) {
		ElectionCalendar calendar = findPublicCalendar(key);
		if (calendar == null || calendar.getStartDate() == null) {
			return null;
		}
		return calendar.getEndDate() != null ? calendar.getEndDate() : calendar.getStartDate();
	}

	private Date resolveResultsActionDeadline() {
		ElectionCalendar ceAuditWindow = findPublicCalendar(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT);
		if (ceAuditWindow != null && ceAuditWindow.getStartDate() != null) {
			return ceAuditWindow.getEndDate() != null ? ceAuditWindow.getEndDate() : ceAuditWindow.getStartDate();
		}
		return null;
	}

	private CalendarWindow toCalendarWindow(ElectionCalendar calendar) {
		if (calendar == null || calendar.getStartDate() == null) {
			return null;
		}
		Date startDate = calendar.getStartDate();
		Date endDate = calendar.getEndDate() != null ? calendar.getEndDate() : startDate;
		return new CalendarWindow(startDate, endDate);
	}

	private boolean isCandidateFormsVerificationDefined() {
		return isCalendarWindowDefined(ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION);
	}

	private boolean isCeAuditDefined() {
		return isCalendarWindowDefined(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT);
	}

	private boolean isCalendarWindowDefined(ElectionCalendarKey key) {
		return toCalendarWindow(findPublicCalendar(key)) != null;
	}

	private String resolveAvailabilityStatusText(AvailabilityStatus status) {
		if (status == AvailabilityStatus.ACTIVE) {
			return getString("auditPublicV2AvailabilityStatusActive");
		}
		if (status == AvailabilityStatus.UPCOMING) {
			return getString("auditPublicV2AvailabilityStatusUpcoming");
		}
		if (status == AvailabilityStatus.CLOSED) {
			return getString("auditPublicV2AvailabilityStatusClosed");
		}
		return getString("auditPublicV2AvailabilityStatusNotConfigured");
	}

	private String resolveAvailabilityBadgeClass(AvailabilityStatus status) {
		if (status == AvailabilityStatus.ACTIVE) {
			return buildBadgeClass(BADGE_COLOR_SUCCESS);
		}
		if (status == AvailabilityStatus.UPCOMING) {
			return buildBadgeClass(BADGE_COLOR_WARNING);
		}
		if (status == AvailabilityStatus.CLOSED) {
			return buildBadgeClass(BADGE_COLOR_SECONDARY);
		}
		return buildBadgeClass("text-bg-dark");
	}

	private String resolveAvailabilityPeriodNotConfiguredLabel(AvailabilityWindowState state) {
		if (state == null || state.getStatus() == AvailabilityStatus.NOT_CONFIGURED) {
			return getString(KEY_AUDIT_PUBLIC_V2_AVAILABILITY_PERIOD_NOT_CONFIGURED);
		}
		return "";
	}

	private String resolveAvailabilityDateLabel(Date date) {
		return date != null ? buildScheduleLabel(date, null) : "-";
	}

	private String resolveDeadlineDateLabel(Date deadline) {
		return deadline != null
				? buildScheduleLabel(deadline, null)
				: getString("auditPublicCandidateDetailDecisionDeadlineNotConfigured");
	}

	private String resolveDecisionDeadlineRemaining(Date deadline) {
		if (deadline == null) {
			return getString("auditPublicCandidateDetailDecisionDeadlineNotConfigured");
		}
		Date now = new Date();
		if (now.after(deadline)) {
			return getString("auditPublicCandidateDetailDecisionDeadlineExpired");
		}
		long remainingMillis = deadline.getTime() - now.getTime();
		long remainingHours = Math.max(1L, (remainingMillis + (60L * 60L * 1000L - 1L)) / (60L * 60L * 1000L));
		if (remainingHours < 24L) {
			return MessageFormat.format(getString("auditPublicCandidateDetailDecisionDeadlineHoursRemaining"), Long.valueOf(remainingHours));
		}
		long remainingDays = Math.max(1L, (remainingMillis + (24L * 60L * 60L * 1000L - 1L)) / (24L * 60L * 60L * 1000L));
		if (remainingDays <= 1L) {
			return getString("auditPublicCandidateDetailDecisionDeadlineToday");
		}
		return MessageFormat.format(getString("auditPublicCandidateDetailDecisionDeadlineDaysRemaining"), Long.valueOf(remainingDays));
	}

	private WebMarkupContainer buildMatrixContainer() {
		WebMarkupContainer container = new WebMarkupContainer("decisionMatrixContainer");
		container.setVisible(isCandidateFormsVerificationDefined()
				&& !decisionMatrixRows.isEmpty()
				&& !decisionAuditors.isEmpty());
		container.setOutputMarkupPlaceholderTag(true);

		container.add(new ListView<Auditor>("auditorHeaders", decisionAuditors) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Auditor> item) {
				Auditor auditorItem = item.getModelObject();
				item.add(new Label("auditorName", auditorItem != null ? auditorItem.getName() : "-"));
			}
		});

		container.add(new ListView<DecisionMatrixRow>("decisionRows", decisionMatrixRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<DecisionMatrixRow> item) {
				DecisionMatrixRow row = item.getModelObject();
					item.add(new Label("matrixCandidateName", row.getCandidateName()));

					item.add(new ListView<DecisionCellView>("auditorCells", row.getCells()) {
						private static final long serialVersionUID = 1L;

						@Override
						protected void populateItem(ListItem<DecisionCellView> cellItem) {
							DecisionCellView cell = cellItem.getModelObject();
							cellItem.add(new ListView<DecisionBadge>("decisionBadges", cell.getBadges()) {
								private static final long serialVersionUID = 1L;

								@Override
								protected void populateItem(ListItem<DecisionBadge> badgeItem) {
									DecisionBadge badgeView = badgeItem.getModelObject();
									Label badge = new Label("decisionBadge", badgeView.getText());
									badge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  badgeView.getCssClass()));
									badgeItem.add(badge);
								}
							});
						}
					});
				}
			});

		return container;
	}

	private WebMarkupContainer buildEmptyMatrixContainer() {
		WebMarkupContainer container = new WebMarkupContainer("decisionMatrixContainer");
		container.setVisible(false);
		container.setOutputMarkupPlaceholderTag(true);
		container.add(new ListView<Auditor>("auditorHeaders", Collections.<Auditor>emptyList()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<Auditor> item) {
				// Intencionalmente vacio: placeholder de Wicket cuando no hay auditores.
			}
		});
		container.add(new ListView<DecisionMatrixRow>("decisionRows", Collections.<DecisionMatrixRow>emptyList()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<DecisionMatrixRow> item) {
				// Intencionalmente vacio: placeholder de Wicket cuando no hay filas.
			}
		});
		return container;
	}

	private Component buildCandidatePicture(String id, CandidateAuditRow row) {
		if (row.getPictureInfo() != null && row.getPictureInfo().length > 0) {
			String extension = hasText(row.getPictureExtension()) ? row.getPictureExtension() : "jpg";
			NonCachingImage image = new NonCachingImage(id, new ImageResource(row.getPictureInfo(), extension));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "rounded-circle candidate-photo-sepia"));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_WIDTH, "32"));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_HEIGHT, "32"));
			return image;
		}
		ContextImage image = new ContextImage(id, "image/default_candidate_photo.jpg");
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "rounded-circle candidate-photo-sepia"));
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_WIDTH, "32"));
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_HEIGHT, "32"));
		return image;
	}

	private DecisionBadge toDecisionBadge(AuditorCandidateDecisionStatus status) {
		if (status == null) {
			return new DecisionBadge(getString(TokenResourceKeys.AUDIT_PUBLIC_V2_DECISION_PENDING), buildBadgeClass(BADGE_COLOR_WARNING));
		}
		switch (status) {
		case PREAPPROVED:
			return new DecisionBadge(getString("auditPublicV2DecisionPreApproved"), buildBadgeClass(BADGE_COLOR_SUCCESS));
		case APPROVED:
			return new DecisionBadge(getString(KEY_AUDIT_PUBLIC_V2_DECISION_APPROVED), buildBadgeClass(BADGE_COLOR_SUCCESS));
		case REJECTED:
			return new DecisionBadge(getString(KEY_AUDIT_PUBLIC_V2_DECISION_REJECTED), buildBadgeClass("text-bg-danger"));
		case ANALYZING:
			return new DecisionBadge(getString(TokenResourceKeys.AUDIT_PUBLIC_V2_DECISION_PENDING), buildBadgeClass(BADGE_COLOR_WARNING));
		case NO_APPLY:
			return new DecisionBadge(getString(KEY_AUDIT_PUBLIC_V2_DECISION_NO_APPLY), buildBadgeClass(BADGE_COLOR_SECONDARY));
		default:
			return new DecisionBadge(getString(TokenResourceKeys.AUDIT_PUBLIC_V2_DECISION_PENDING), buildBadgeClass(BADGE_COLOR_WARNING));
		}
	}

	private DecisionBadge toMatrixDecisionBadge(AuditorCandidateDecisionStatus status, MatrixDecisionStage stage) {
		if (status == null || status == AuditorCandidateDecisionStatus.ANALYZING) {
			String key = stage == MatrixDecisionStage.PRECOMPLETE
					? "auditPublicV2PreDecisionPending"
					: "auditPublicV2FinalDecisionPending";
			return new DecisionBadge(getString(key), buildBadgeClass(BADGE_COLOR_WARNING));
		}
		if (status == AuditorCandidateDecisionStatus.REJECTED) {
			String key = stage == MatrixDecisionStage.PRECOMPLETE
					? "auditPublicV2PreDecisionRejected"
					: "auditPublicV2FinalDecisionRejected";
			return new DecisionBadge(getString(key), buildBadgeClass("text-bg-danger"));
		}
		return toDecisionBadge(status);
	}

	private DecisionBadge toCandidateStatusBadge(CandidateStatus status) {
		if (status == null) {
			return new DecisionBadge(getString(TokenResourceKeys.AUDIT_PUBLIC_V2_DECISION_PENDING), buildBadgeClass(BADGE_COLOR_WARNING));
		}
		switch (status) {
		case CONFIRMED_AND_PUBLISHED:
			return new DecisionBadge(getString("candidateStatusOptionConfirmedAndPublished"), buildBadgeClass(BADGE_COLOR_SUCCESS));
		case REJECTED:
			return new DecisionBadge(getString("candidateStatusOptionRejected"), buildBadgeClass("text-bg-danger"));
		case COMPLETE:
			return new DecisionBadge(getString("candidateStatusOptionComplete"), buildBadgeClass("text-bg-info"));
		case PRECOMPLETE:
			return new DecisionBadge(getString("candidateStatusOptionPreComplete"), buildBadgeClass(BADGE_COLOR_WARNING));
		case INCOMPLETE:
		default:
			return new DecisionBadge(getString("candidateStatusOptionIncomplete"), buildBadgeClass(BADGE_COLOR_SECONDARY));
		}
	}

	private static String buildBadgeClass(String colorClass) {
		return BADGE_BASE_CLASS + " " + colorClass;
	}

	private String resolveCalendarDescription(ElectionCalendarKey key) {
		if (key == null) {
			return "";
		}
		String language = SecurityUtils.getLocale().getLanguage();
		if ("en".equalsIgnoreCase(language)) {
			return key.getDescriptionEN();
		}
		if ("pt".equalsIgnoreCase(language)) {
			return key.getDescriptionPT();
		}
		return key.getDescriptionES();
	}

	private String extractTitle(String description) {
		if (!hasText(description)) {
			return getString(KEY_AUDIT_PUBLIC_V2_UNTITLED_MILESTONE);
		}
		int index = description.indexOf('.');
		if (index > 0) {
			return description.substring(0, index).trim();
		}
		return description;
	}

	private String resolvePhaseLabel(ElectionCalendarKey key) {
		if (key == null) {
			return getString(KEY_AUDIT_PUBLIC_V2_PHASE_CALENDAR);
		}
		String name = key.name();
		if (name.contains("CALL_FOR_CANDIDATES")) {
			return getString(KEY_AUDIT_PUBLIC_V2_PHASE_CALL);
		}
		if (name.contains("PADRON")) {
			return getString(KEY_AUDIT_PUBLIC_V2_PHASE_CENSUS);
		}
		if (name.contains("CANDIDATE") || name.contains("EVALUATION")) {
			return getString(KEY_AUDIT_PUBLIC_V2_PHASE_CANDIDATES);
		}
		if (name.contains("VOTING")) {
			return getString(KEY_AUDIT_PUBLIC_V2_PHASE_VOTING);
		}
		if (name.contains("AUDIT")) {
			return getString(KEY_AUDIT_PUBLIC_V2_PHASE_AUDIT);
		}
		if (name.contains("RESULT")) {
			return getString(KEY_AUDIT_PUBLIC_V2_PHASE_RESULTS);
		}
		return getString(KEY_AUDIT_PUBLIC_V2_PHASE_CALENDAR);
	}

	private String buildScheduleLabel(Date startDate, Date endDate) {
		if (startDate == null) {
			return "-";
		}
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, SecurityUtils.getLocale());
		if (endDate != null) {
			return formatter.format(startDate) + " - " + formatter.format(endDate);
		}
		return formatter.format(startDate);
	}

	private List<ElectionCalendar> safeElectionCalendars(long electionId) {
		try {
			List<ElectionCalendar> calendars = AppContext.getInstance().getManagerBeanRemote().getElectionCalendars(electionId);
			return calendars != null ? calendars : Collections.<ElectionCalendar>emptyList();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private List<Nomination> safeNominations(long electionId) {
		try {
			List<Nomination> nominations = AppContext.getInstance().getManagerBeanRemote().getElectionNominations(electionId);
			return nominations != null ? nominations : Collections.<Nomination>emptyList();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private List<Auditor> safeAuditors(long electionId) {
		try {
			List<Auditor> auditors = AppContext.getInstance().getManagerBeanRemote().getElectionAuditors(electionId);
			return auditors != null ? auditors : Collections.<Auditor>emptyList();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private List<AuditorCandidateDecision> safeAuditorCandidateDecisions(long electionId) {
		try {
			List<AuditorCandidateDecision> decisions = AppContext.getInstance().getManagerBeanRemote().getElectionAuditorCandidateDecisions(electionId);
			return decisions != null ? decisions : Collections.<AuditorCandidateDecision>emptyList();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private Candidate resolveDetailedCandidate(long candidateId, Candidate fallback) {
		try {
			Candidate detailed = AppContext.getInstance().getManagerBeanRemote().getCandidate(candidateId);
			return detailed != null ? detailed : fallback;
		} catch (Exception e) {
			return fallback;
		}
	}

	private boolean isPublicCalendar(ElectionCalendar calendar) {
		Object publicable = readFieldValue(calendar, "publicable");
		return publicable instanceof Boolean && ((Boolean) publicable).booleanValue();
	}

	private Object readFieldValue(Object instance, String fieldName) {
		if (instance == null || fieldName == null) {
			return null;
		}
		try {
			Field field = instance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(instance);
		} catch (Exception e) {
			return null;
		}
	}

	private Long readEntityLongField(Object instance, String fieldName) {
		Object value = readFieldValue(instance, fieldName);
		if (value == null) {
			return null;
		}
		try {
			return Long.valueOf(String.valueOf(value));
		} catch (Exception e) {
			return null;
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String valueOrPlaceholder(String value) {
		return hasText(value) ? value : "-";
	}

	private enum AvailabilityStatus {
		NOT_CONFIGURED,
		UPCOMING,
		ACTIVE,
		CLOSED
	}

	private enum MatrixDecisionStage {
		PRECOMPLETE,
		COMPLETE
	}

	private static final class AvailabilityWindowState implements Serializable {
		private static final long serialVersionUID = 1L;
		private final AvailabilityStatus status;
		private final Date startDate;
		private final Date endDate;

		private AvailabilityWindowState(AvailabilityStatus status, Date startDate, Date endDate) {
			this.status = status;
			this.startDate = startDate;
			this.endDate = endDate;
		}

		static AvailabilityWindowState notConfigured() {
			return new AvailabilityWindowState(AvailabilityStatus.NOT_CONFIGURED, null, null);
		}

		static AvailabilityWindowState upcoming(Date startDate, Date endDate) {
			return new AvailabilityWindowState(AvailabilityStatus.UPCOMING, startDate, endDate);
		}

		static AvailabilityWindowState active(Date startDate, Date endDate) {
			return new AvailabilityWindowState(AvailabilityStatus.ACTIVE, startDate, endDate);
		}

		static AvailabilityWindowState closed(Date startDate, Date endDate) {
			return new AvailabilityWindowState(AvailabilityStatus.CLOSED, startDate, endDate);
		}

		AvailabilityStatus getStatus() {
			return status;
		}

		Date getStartDate() {
			return startDate;
		}

		Date getEndDate() {
			return endDate;
		}
	}

	private static final class CalendarWindow implements Serializable {
		private static final long serialVersionUID = 1L;
		private final Date startDate;
		private final Date endDate;

		private CalendarWindow(Date startDate, Date endDate) {
			this.startDate = startDate;
			this.endDate = endDate;
		}

		Date getStartDate() {
			return startDate;
		}

		Date getEndDate() {
			return endDate;
		}
	}

	private static class DecisionBadge implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String text;
		private final String cssClass;

		DecisionBadge(String text, String cssClass) {
			this.text = text;
			this.cssClass = cssClass;
		}

		public String getText() {
			return text;
		}

		public String getCssClass() {
			return cssClass;
		}
	}

	private static class CandidateAuditRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private final long candidateId;
		private final int candidateOrder;
		private final String candidateName;
		private final CandidateStatus candidateStatus;
		private final byte[] pictureInfo;
		private final String pictureExtension;
		private final String myDecisionText;
		private final String myDecisionCssClass;
		private final String actionText;
		private final String actionCssClass;

		CandidateAuditRow(long candidateId, int candidateOrder, String candidateName, CandidateStatus candidateStatus, byte[] pictureInfo, String pictureExtension, String myDecisionText, String myDecisionCssClass, String actionText, String actionCssClass) {
			this.candidateId = candidateId;
			this.candidateOrder = candidateOrder;
			this.candidateName = candidateName;
			this.candidateStatus = candidateStatus;
			this.pictureInfo = pictureInfo;
			this.pictureExtension = pictureExtension;
			this.myDecisionText = myDecisionText;
			this.myDecisionCssClass = myDecisionCssClass;
			this.actionText = actionText;
			this.actionCssClass = actionCssClass;
		}

		public long getCandidateId() {
			return candidateId;
		}

		public int getCandidateOrder() {
			return candidateOrder;
		}

		public String getCandidateName() {
			return candidateName;
		}

		public CandidateStatus getCandidateStatus() {
			return candidateStatus;
		}

		public byte[] getPictureInfo() {
			return pictureInfo;
		}

		public String getPictureExtension() {
			return pictureExtension;
		}

		public String getMyDecisionText() {
			return myDecisionText;
		}

		public String getMyDecisionCssClass() {
			return myDecisionCssClass;
		}

		public String getActionText() {
			return actionText;
		}

		public String getActionCssClass() {
			return actionCssClass;
		}
	}

	private static class CommissionerParticipationRow implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String commissionerName;
		private final int preapprovalCount;
		private final int preapprovalTotal;
		private final int approvalCount;
		private final int approvalTotal;
		private final boolean electionAuditCompleted;
		private final String electionAuditLabel;

		CommissionerParticipationRow(String commissionerName, int preapprovalCount, int preapprovalTotal,
				int approvalCount, int approvalTotal, boolean electionAuditCompleted, String electionAuditLabel) {
			this.commissionerName = commissionerName;
			this.preapprovalCount = preapprovalCount;
			this.preapprovalTotal = preapprovalTotal;
			this.approvalCount = approvalCount;
			this.approvalTotal = approvalTotal;
			this.electionAuditCompleted = electionAuditCompleted;
			this.electionAuditLabel = electionAuditLabel;
		}

		public String getCommissionerName() {
			return commissionerName;
		}

		public String getPreapprovalLabel() {
			return preapprovalCount + "/" + preapprovalTotal;
		}

		public String getApprovalLabel() {
			return approvalCount + "/" + approvalTotal;
		}

		public String getElectionAuditLabel() {
			return electionAuditLabel;
		}

		public String getElectionAuditCssClass() {
			return electionAuditCompleted ? buildBadgeClass(BADGE_COLOR_SUCCESS) : buildBadgeClass(BADGE_COLOR_WARNING);
		}
	}

	private static class DecisionCellView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final List<DecisionBadge> badges;

		DecisionCellView(List<DecisionBadge> badges) {
			this.badges = badges != null ? badges : Collections.<DecisionBadge>emptyList();
		}

		public List<DecisionBadge> getBadges() {
			return badges;
		}
	}

	private static class MatrixDecisionView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final AuditorCandidateDecisionStatus legacyStatus;
		private final AuditorCandidateDecisionStatus preStatus;
		private final Date preapprovedDate;
		private final AuditorCandidateDecisionStatus finalStatus;
		private final Date approvedDate;

		MatrixDecisionView(AuditorCandidateDecisionStatus legacyStatus, AuditorCandidateDecisionStatus preStatus, Date preapprovedDate,
				AuditorCandidateDecisionStatus finalStatus, Date approvedDate) {
			this.legacyStatus = legacyStatus;
			this.preStatus = preStatus;
			this.preapprovedDate = preapprovedDate;
			this.finalStatus = finalStatus;
			this.approvedDate = approvedDate;
		}

		static MatrixDecisionView empty() {
			return new MatrixDecisionView(null, null, null, null, null);
		}

		AuditorCandidateDecisionStatus getLegacyStatus() {
			return legacyStatus;
		}

		AuditorCandidateDecisionStatus getPreStatus() {
			return preStatus;
		}

		Date getPreapprovedDate() {
			return preapprovedDate;
		}

		AuditorCandidateDecisionStatus getFinalStatus() {
			return finalStatus;
		}

		Date getApprovedDate() {
			return approvedDate;
		}
	}

	private static class DecisionMatrixRow implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String candidateName;
		private final List<DecisionCellView> cells;

		DecisionMatrixRow(String candidateName, List<DecisionCellView> cells) {
			this.candidateName = candidateName;
			this.cells = cells;
		}

		public String getCandidateName() {
			return candidateName;
		}

		public List<DecisionCellView> getCells() {
			return cells;
		}
	}
}
