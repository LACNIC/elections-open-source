package net.lacnic.elections.adminweb.ui.results.audit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.error.ErrorAuditNotPublic;
import net.lacnic.elections.adminweb.ui.results.CandidateCodesPanel;
import net.lacnic.elections.adminweb.ui.results.ElectionResultsPanel;
import net.lacnic.elections.domain.Auditor;


public class AuditDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = 5362590873487534369L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private Auditor auditor;


	public AuditDashboard(final PageParameters params) {
		super(params);
		try {
			if (auditor.isAgreedConformity() && auditor.isCommissioner())
				getSession().info(getString("auditorAgreedConfority"));

			boolean revisionRequested = auditor.getElection().isRevisionRequest();
			if (auditor.isRevisionAvailable() && revisionRequested && auditor.isCommissioner())
				getSession().info(getString("revisionActive"));

			add(new FeedbackPanel("feedbackPanel"));

			// si se solicita revisión el auditor solo puede ver el panel de habilitación de revisión
			AuditorRevisionPanel auditorRevisionPanel = new AuditorRevisionPanel("auditorRevisionPanel", auditor);
			auditorRevisionPanel.setVisibilityAllowed(revisionRequested);
			add(auditorRevisionPanel);

			WebMarkupContainer noRevision = new WebMarkupContainer("noRevision");
			noRevision.setVisibilityAllowed(!revisionRequested);
			noRevision.add(new Label("title", getElection().getTitle(getLanguage())));
			Label description = new Label("description", getElection().getDescription(getLanguage()));
			description.setEscapeModelStrings(false);
			noRevision.add(description);
			noRevision.add(new AuditorConformityPanel("auditorAgreeConformityPanel", auditor));
			noRevision.add(new ElectionResultsPanel("resultsPanel", getElection().getElectionId()));
			noRevision.add(new CandidateCodesPanel("candidateCodesPanel", getElection().getElectionId()));
			noRevision.add(new MoreInformationForAuditPanel("moreInformationForAuditPanel", getElection().getElectionId()));
			add(noRevision);

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public Class validateToken(PageParameters params) {
		auditor = AppContext.getInstance().getVoterBeanRemote().verifyAuditorResultAccess(getToken());
		if (auditor == null) {
			AppContext.getInstance().getVoterBeanRemote().saveFailedAccessIp(getIP());
			return Error404.class;
		} else {
			setElection(auditor.getElection());
			if (!auditor.getElection().isAuditorLinkAvailable()) {
				return ErrorAuditNotPublic.class;
			}
		}
		return null;

	}

}