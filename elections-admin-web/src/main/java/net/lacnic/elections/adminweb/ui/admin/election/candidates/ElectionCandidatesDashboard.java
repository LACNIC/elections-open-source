package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ActivityType;


@AuthorizeInstantiation("elections-only-one")
public class ElectionCandidatesDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -8712299592904499634L;
	private Election election;


	public ElectionCandidatesDashboard(PageParameters params) {
		super(params);

		setElection(AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params)));
		add(new FeedbackPanel("feedback"));
		add(new ElectionCandidateForm("electionCandidateForm", election));
		add(new ManageElectionTabsPanel("tabsPanel", election));
		add(new CandidatesListPanel("candidatesListPanel", election));
	}


	public final class ElectionCandidateForm extends Form<Void> {
		private static final long serialVersionUID = 2351447413365706203L;

		public ElectionCandidateForm(String id, final Election election) {
			super(id);

			setFileMaxSize(Bytes.kilobytes(2048));
			AddCandidatePanel addCandidatePanel = new AddCandidatePanel("addCandidatePanel", election);
			add(addCandidatePanel);

			Link<Void> back = new Link<Void>("back") {
				private static final long serialVersionUID = -2540140657992430113L;

				@Override
				public void onClick() {
					setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
				}
			};
			add(back);

			Link<Void> skip = new Link<Void>("skip") {
				private static final long serialVersionUID = 832866944403935918L;

				@Override
				public void onClick() {
					setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(election.getElectionId()));
				}
			};
			add(skip);

			Link<Void> next = new Link<Void>("next") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						String description = SecurityUtils.getUserAdminId().toUpperCase() + getString("candidateManagementSuccessAddCandidates") + getElection().getTitleSpanish();
						AppContext.getInstance().getManagerBeanRemote().persistActivity(SecurityUtils.getUserAdminId(), ActivityType.ADD_CANDIDATES, description, SecurityUtils.getClientIp(), getElection().getElectionId());
						setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(election.getElectionId()));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(next);
			next.setEnabled(election.isCandidatesSet());
		}
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

}
