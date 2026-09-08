package net.lacnic.elections.adminweb.ui.token.page;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenTopHeaderPanel;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.results.CandidateCodesPanel;
import net.lacnic.elections.adminweb.ui.results.ElectionResultsPanel;
import net.lacnic.elections.adminweb.ui.results.audit.AuditorConformityPanel;
import net.lacnic.elections.adminweb.ui.results.audit.MoreInformationForAuditPanel;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.ReminderFrequency;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;

public class AuditPublicResultsPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private Auditor auditor;
	private Election election;
	private String resultsDecisionDeadlineDate = "-";
	private String resultsDecisionDeadlineRemaining = "-";

	public AuditPublicResultsPage() {
		this(new PageParameters());
	}

	public AuditPublicResultsPage(PageParameters params) {
		super(params);

		try {
			CalendarRange resultsDecisionRange = resolveResultsDecisionWindow();
			resultsDecisionDeadlineDate = resultsDecisionRange.getEnd() != null
					? formatDateTime(resultsDecisionRange.getEnd())
					: getString("auditPublicCandidateDetailDecisionDeadlineNotConfigured");
			resultsDecisionDeadlineRemaining = resolveDecisionDeadlineRemaining(resultsDecisionRange.getEnd());

			if (auditor != null && auditor.isAgreedConformity() && auditor.isCommissioner()) {
				getSession().info(getString("auditorAgreedConfority"));
			}

			boolean revisionRequested = auditor != null && auditor.getElection() != null && auditor.getElection().isRevisionRequest();
			if (auditor != null && auditor.isRevisionAvailable() && revisionRequested && auditor.isCommissioner()) {
				getSession().info(getString("revisionActive"));
			}

			add(new FeedbackPanel("feedbackPanel"));

			WebMarkupContainer noRevision = new WebMarkupContainer("noRevision");
			noRevision.setVisibilityAllowed(!revisionRequested);
			noRevision.add(new Label("title", election != null ? election.getTitle(getLanguage()) : "-"));
			Label description = new Label("description", election != null ? election.getDescription(getLanguage()) : "-");
			description.setEscapeModelStrings(false);
			noRevision.add(description);
			noRevision.add(new AuditorConformityPanel(
					"auditorAgreeConformityPanel",
					auditor,
					resultsDecisionDeadlineDate,
					resultsDecisionDeadlineRemaining));
			noRevision.add(new ElectionResultsPanel("resultsPanel", election != null ? election.getElectionId() : 0L));
			noRevision.add(new CandidateCodesPanel("candidateCodesPanel", election != null ? election.getElectionId() : 0L));
			noRevision.add(new MoreInformationForAuditPanel("moreInformationForAuditPanel", election != null ? election.getElectionId() : 0L));
			add(noRevision);
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

	@Override
	protected Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params) {
		auditor = AppContext.getInstance().getVoterBeanRemote().verifyAuditorResultAccess(getToken());
		if (auditor == null) {
			return Error404.class;
		}
		if (auditor.getElection() == null) {
			return Error404.class;
		}
		election = auditor.getElection();
		setElection(election);
		setWhereAmI("Resultados públicos de auditoría");
		setContextClass(Auditor.class.getName());
		setContextData("auditorId: " + auditor.getAuditorId() + "\n"
				+ "auditorName: " + auditor.getName() + "\n"
				+ "electionId: " + election.getElectionId() + "\n"
				+ "revisionAvailable: " + auditor.isRevisionAvailable());
		setHeaderUserDisplay(auditor.getName());
		setHeaderNotificationStatusFromReminderFrequency(auditor.getReminderFrequency());
		return null;
	}

	private String getLanguage() {
		return SecurityUtils.getLocale().getLanguage();
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
	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		if (election != null && !election.isAuditorLinkAvailable()) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.ACCESS_NOT_AVAILABLE,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_18_PERIODO_CE_AUDIT,
					null,
					null));
		}
		ElectionCalendar calendar = findPublicCalendar(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT);
		if (calendar == null || calendar.getStartDate() == null) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.RESULTS_NOT_PUBLIC,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_18_PERIODO_CE_AUDIT,
					null,
					null));
		}
		TokenAccessGate.AccessBlock before = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.RESULTS_NOT_PUBLIC,
				PublicAccessDeniedPage.CountdownTargetDate.START,
				ElectionCalendarKey.N_18_PERIODO_CE_AUDIT,
				null,
				null);
		TokenAccessGate.AccessBlock after = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.RESULTS_NOT_PUBLIC,
				PublicAccessDeniedPage.CountdownTargetDate.END,
				ElectionCalendarKey.N_18_PERIODO_CE_AUDIT,
				null,
				null);
		Date end = calendar.getEndDate() != null ? calendar.getEndDate() : calendar.getStartDate();
		return TokenAccessGate.of(calendar.getStartDate(), end, before, after);
	}

	private CalendarRange resolveResultsDecisionWindow() {
		ElectionCalendar ceAuditWindow = findPublicCalendar(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT);
		if (ceAuditWindow != null && ceAuditWindow.getStartDate() != null) {
			Date ceEnd = ceAuditWindow.getEndDate() != null ? ceAuditWindow.getEndDate() : ceAuditWindow.getStartDate();
			return new CalendarRange(ceAuditWindow.getStartDate(), ceEnd);
		}
		return new CalendarRange(null, null);
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

	private String formatDateTime(Date date) {
		if (date == null) {
			return "-";
		}
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, SecurityUtils.getLocale());
		return formatter.format(date);
	}

	private static final class CalendarRange implements Serializable {
		private static final long serialVersionUID = 1L;
		private final Date start;
		private final Date end;

		private CalendarRange(Date start, Date end) {
			this.start = start;
			this.end = end;
		}

		Date getStart() {
			return start;
		}

		Date getEnd() {
			return end;
		}
	}
}
