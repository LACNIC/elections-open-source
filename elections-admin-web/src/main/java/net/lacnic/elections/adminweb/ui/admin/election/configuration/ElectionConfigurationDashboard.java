package net.lacnic.elections.adminweb.ui.admin.election.configuration;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.ui.components.OnOffSwitch;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;


@AuthorizeInstantiation("elections-only-one")
public class ElectionConfigurationDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1392182581021963077L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public ElectionConfigurationDashboard(PageParameters params) {
		super(params);
		try {
			ServletContext context = ((WebApplication) WebApplication.get()).getServletContext();
			String filePath = context.getRealPath("/");

			long electionId = UtilsParameters.getIdAsLong(params);
			Election election = AppContext.getInstance().getManagerBeanRemote().getElection(electionId);

			add(new Label("electionTitle", election.getTitle(getLanguage())));

			add(new OnOffSwitch("votingLinkAvailable", new PropertyModel<>(election, "votingLinkAvailable")) {
				private static final long serialVersionUID = -8757419944224081522L;

				@Override
				protected void action() {
					AppContext.getInstance().getManagerBeanRemote().setVoteLinkStatus(electionId, election.isVotingLinkAvailable(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
				}
			});

			OnOffSwitch resultLinkAvailable = new OnOffSwitch("resultLinkAvailable", new PropertyModel<>(election, "resultLinkAvailable")) {
				private static final long serialVersionUID = -3214185498258791153L;

				@Override
				protected void action() {
					AppContext.getInstance().getManagerBeanRemote().setResultsLinkStatus(electionId, election.isResultLinkAvailable(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());

				}
			};
			resultLinkAvailable.setEnabled(election.isFinished());
			add(resultLinkAvailable);

			OnOffSwitch auditorLinkAvailable = new OnOffSwitch("auditorLinkAvailable", new PropertyModel<>(election, "auditorLinkAvailable")) {
				private static final long serialVersionUID = -2218718810528752527L;

				@Override
				protected void action() {
					AppContext.getInstance().getManagerBeanRemote().setAuditLinkStatus(electionId, election.isAuditorLinkAvailable(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
				}
			};
			auditorLinkAvailable.setEnabled(election.isFinished());
			add(auditorLinkAvailable);

			OnOffSwitch revisionRequest = new OnOffSwitch("revisionRequest", new PropertyModel<>(election, "revisionRequest")) {
				private static final long serialVersionUID = 476112946371106638L;

				@Override
				protected void action() {
					AppContext.getInstance().getManagerBeanRemote().requestElectionRevision(electionId, election.isRevisionRequest(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					setResponsePage(ElectionConfigurationDashboard.class, UtilsParameters.getId(electionId));
				}
			};
			add(revisionRequest);

			Link<Void> back = new Link<Void>("back") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						setResponsePage(SecurityUtils.getHomePage());
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(back);

			File rolesDocument = AppContext.getInstance().getVoterBeanRemote().getElectionRolesRevisionDocument(filePath);
			add(new DownloadLink("rolesDocument", rolesDocument));

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}
