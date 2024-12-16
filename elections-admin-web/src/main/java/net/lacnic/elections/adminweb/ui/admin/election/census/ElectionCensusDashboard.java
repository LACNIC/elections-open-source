package net.lacnic.elections.adminweb.ui.admin.election.census;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.exception.CensusValidationException;

@AuthorizeInstantiation("elections-only-one")
public class ElectionCensusDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1538700499093907394L;

	private Election election;
	private UserVoter userVoter = new UserVoter();

	public ElectionCensusDashboard(PageParameters params) {
		super(params);

		// Check if election is closed (user might be using a direct link to get to this
		// page)
		Election fetchedElection = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));
		if (fetchedElection.isClosed()) {
			setResponsePage(ErrorElectionClosed.class);
			return;
		} else {
			setElection(fetchedElection);
		}
		add(new FeedbackPanel("feedback"));
		add(new ManageElectionTabsPanel("tabsPanel", fetchedElection));
		add(new ElectionCensusForm("electionCensusForm", fetchedElection));

		Form<Void> userVoterForm = new Form<>("userVoterForm");
		userVoterForm.add(new AddUserVoterPanel("addUserVoterPanel", userVoter));
		add(userVoterForm);

		userVoterForm.add(new Button("addUserVoter") {
			private static final long serialVersionUID = -5396612472447055621L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				try {
					AppContext.getInstance().getManagerBeanRemote().addUserVoter(getElection().getElectionId(), getUserVoter(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					getSession().info(getString("censusManagementSuccess"));
					setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(getElection().getElectionId()));
				} catch (CensusValidationException e) {
					error(getString(e.getMessage()));
				}
			}
		});

		add(new AjaxLazyLoadPanel<CensusListPanel>("censusListPanel") {
			private static final long serialVersionUID = 8165854022863553760L;

			@Override
			public CensusListPanel getLazyLoadComponent(String markupId) {
				return new CensusListPanel(markupId, fetchedElection);
			}
		});

	}

	public final class ElectionCensusForm extends Form<Void> {
		private static final long serialVersionUID = 2351447413365706203L;

		public ElectionCensusForm(String id, final Election election) {
			super(id);

			UploadCensusFilePanel uploadCensusFilePanel = new UploadCensusFilePanel("uploadCensusFilePanel", election, ElectionCensusDashboard.class);
			uploadCensusFilePanel.setOutputMarkupId(true);
			add(uploadCensusFilePanel);

			Button next = new Button("next") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onSubmit() {
					try {
						setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			next.setEnabled(getElection().isElectorsSet());
			add(next);

			Link<Void> skip = new Link<Void>("skip") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(skip);

			Link<Void> back = new Link<Void>("back") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						setResponsePage(ElectionDetailDashboard.class, UtilsParameters.getId(election.getElectionId()));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(back);

		}
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public UserVoter getUserVoter() {
		return userVoter;
	}

	public void setUserVoter(UserVoter userVoter) {
		this.userVoter = userVoter;
	}

}
