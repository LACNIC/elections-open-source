package net.lacnic.elections.adminweb.ui.admin.election.detail;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.admin.election.call.ElectionCallDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.create.ElectionCreateDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.adminweb.validators.ElectionManualManagementScopeValidator;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.ejb.ElectionsManagerEJB;

public class ElectionDetailDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 2749798787618064089L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private Election election;

	public ElectionDetailDashboard(PageParameters params) {
		super(params);

		long electionId = UtilsParameters.getIdAsLong(params);

		// Check if election is closed (user might be using a direct link to get to this page)
		if (UtilsParameters.isId(params) && electionId > 0) {
			Election electionAux = reloadAndEnforceElectionAccess(electionId);
			if (electionAux.isClosed()) {
				setResponsePage(ErrorElectionClosed.class);
				return;
			}
			setElection(electionAux);
		} else {
			setResponsePage(ElectionCreateDashboard.class);
			return;
		}

		add(new FeedbackPanel("feedback"));
		add(new ManageElectionTabsPanel("tabsPanel", election, "tabDetail"));
		add(new NewElectionForm("newElectionForm"));
	}

	public final class NewElectionForm extends Form<Void> {
		private static final long serialVersionUID = -5221887812611102034L;

		private transient ElectionsManagerEJB managerBeanRemote;
		ElectionDetailPanel electionDetailPanel;

		public NewElectionForm(String id) {
				super(id);
				try {
					electionDetailPanel = new ElectionDetailPanel("fields", election);
					add(electionDetailPanel);
					addManualManagementScopeValidator();

				Button submitButton = new Button("submit") {
					private static final long serialVersionUID = 1073607359256986749L;

					@Override
					public void onSubmit() {
						try {
							managerBeanRemote = AppContext.getInstance().getManagerBeanRemote();
							if (election.getElectionId() <= 0) {
								setResponsePage(ElectionCreateDashboard.class);
								return;
							}
							Election currentElection = reloadAndEnforceElectionAccess(election.getElectionId());
							if (currentElection.isClosed()) {
								setResponsePage(ErrorElectionClosed.class);
								return;
							}
							election.applyRestrictedCountryCodes();
							copyTexts();
							Election updatedElection = managerBeanRemote.updateElection(election, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
							getSession().info(getString("electionManagementUpdateSuccess"));
							setResponsePage(ElectionCallDashboard.class, UtilsParameters.getId(updatedElection.getElectionId()));
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
				appLogger.error(e.getMessage(), e);
				error(e.getMessage());
			}
		}

		@SuppressWarnings("unchecked")
		private void addManualManagementScopeValidator() {
			FormComponent<ElectionType> electionType = (FormComponent<ElectionType>) electionDetailPanel.get("electionType");
			FormComponent<Boolean> manageOrganizationsManual = (FormComponent<Boolean>) electionDetailPanel.get("manageOrganizationsManual");
			FormComponent<Boolean> manageVotersManual = (FormComponent<Boolean>) electionDetailPanel.get("manageVotersManual");
			add(new ElectionManualManagementScopeValidator(electionType, manageOrganizationsManual, manageVotersManual));
		}

			public void copyTexts() throws Exception {
				if (election.isOnlySp()) {
					election.copyLanguageDescriptions(LanguageCode.SP.getCode());
					election.copyLanguageTitles(LanguageCode.SP.getCode());
					election.copyLanguageURLs(LanguageCode.SP.getCode());
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
