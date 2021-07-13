package net.lacnic.elections.adminweb.ui.admin.election.detail;

import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.ejb.ElectionsManagerEJB;


@AuthorizeInstantiation("elections-only-one")
public class ElectionDetailDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = 2749798787618064089L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private Election election;


	public ElectionDetailDashboard(PageParameters params) {
		super(params);
		election = new Election();
		election.setLinkSpanish((AppContext.getInstance().getManagerBeanRemote().getDefaultWebsite()));
		election.setDefaultSender(AppContext.getInstance().getManagerBeanRemote().getDefaultSender());
		if (UtilsParameters.isId(params)) {
			setElection(AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params)));
			getElection().initStringsStartEndDates();
		}
		add(new FeedbackPanel("feedback"));
		add(new ManageElectionTabsPanel("tabsPanel", election));
		add(new NewElectionForm("newElectionForm"));
	}


	public final class NewElectionForm extends Form<Void> {
		private static final long serialVersionUID = -5221887812611102034L;

		private ElectionsManagerEJB managerBeanRemote;
		ElectionDetailPanel electionDetailPanel;

		public NewElectionForm(String id) {
			super(id);
			try {
				electionDetailPanel = new ElectionDetailPanel("fields", election);
				add(electionDetailPanel);

				Button submitButton = new Button("submit") {
					private static final long serialVersionUID = 1073607359256986749L;

					@Override
					public void onSubmit() {
						try {
							boolean isNew = true;
							boolean isJoint = false;
							Election originalElection;
							Date originalElectionStartDate = null;

							managerBeanRemote = AppContext.getInstance().getManagerBeanRemote();

							// Check if election is joint with another, if so, start date cannot be modified
							if (election.getElectionId() == 0) {
								isNew = true;
							} else {
								isNew = false;
								isJoint = managerBeanRemote.isJointElection(election.getElectionId());
								if (isJoint) {
									originalElection = managerBeanRemote.getElection(election.getElectionId());
									originalElectionStartDate = originalElection.getStartDate();
								}
							};

							election.initDatesStartEndDates();
							if (election.getStartDate().after(election.getEndDate())) {
								error(getString("electionManagementErrorDates"));
							} else if ((!isNew) && (isJoint) && (originalElectionStartDate.compareTo(election.getStartDate())!= 0)) {
								error(getString("electionManagementErrorJointDates"));
							} else {
								copyTexts();
								Election newElection;
								if (election.getElectionId() == 0) {
									getSession().info(getString("electionManagementCreateSuccess"));
									newElection = managerBeanRemote.updateElection(election, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
								} else {
									getSession().info(getString("electionManagementUpdateSuccess"));
									newElection = managerBeanRemote.updateElection(election, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
								}

								setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(newElection.getElectionId()));
							}
						} catch (Exception e) {
							error(e.getMessage());
						}
					}
				};
				add(submitButton);

				Link<Void> cancelButton = new Link<Void>("cancel") {
					private static final long serialVersionUID = 2172660804449339859L;

					@Override
					public void onClick() {
						setResponsePage(ElectionsDashboard.class);
					}
				};
				add(cancelButton);

			} catch (Exception e) {
				appLogger.error(e);
				error(e.getMessage());
			}
		}

		public void copyTexts() throws Exception {
			if (election.isOnlySp()) {
				election.copyLanguageDescriptions("SP");
				election.copyLanguageTitles("SP");
				election.copyLanguageURLs("SP");
			}
		}

	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

}
