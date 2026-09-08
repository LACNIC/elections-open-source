package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.tasks.ElectionTasksDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.validators.CandidatePictureUploadValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.utils.LinksUtils;

public class ElectionCandidatesDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = -8712299592904499634L;
	private Election election;


	public ElectionCandidatesDashboard(PageParameters params) {
		super(params);

		// Check if election is closed (user might be using a direct link to get to this page)
		Election election = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));
		if(election.isClosed()) {
			setResponsePage(ErrorElectionClosed.class);
			return;
		} else {
			setElection(election);
		}
		boolean showCandidateForm = UtilsParameters.isNewCandidate(params);
		add(new FeedbackPanel("feedback"));
		add(new ElectionCandidateForm("electionCandidateForm", election, showCandidateForm));
		add(new ManageElectionTabsPanel("tabsPanel", election, "tabCandidates"));
		add(new CandidatesHeaderActionsPanel("candidatesHeaderActions", election, showCandidateForm));
		add(new CandidatesListPanel("candidatesListPanel", election));
	}


	public final class ElectionCandidateForm extends Form<Void> {
		private static final long serialVersionUID = 2351447413365706203L;

		public ElectionCandidateForm(String id, final Election election, boolean showCandidateForm) {
			super(id);

			setFileMaxSize(Bytes.bytes(CandidatePictureUploadValidator.MAX_CANDIDATE_INPUT_SIZE_BYTES));
			WebMarkupContainer candidateCreateContainer = new WebMarkupContainer("candidateCreateContainer");
			candidateCreateContainer.setVisible(showCandidateForm);
			candidateCreateContainer.add(new AddCandidatePanel("addCandidatePanel", election));
			add(candidateCreateContainer);

			final List<Candidate> summaryCandidates = loadCandidates(election);
			WebMarkupContainer candidateSummaryContainer = new WebMarkupContainer("candidateSummaryContainer");
			candidateSummaryContainer.setVisible(!showCandidateForm);
			add(candidateSummaryContainer);

			WebMarkupContainer candidateSummaryTableContainer = new WebMarkupContainer("candidateSummaryTableContainer");
			candidateSummaryTableContainer.setVisible(!summaryCandidates.isEmpty());
			candidateSummaryContainer.add(candidateSummaryTableContainer);
			candidateSummaryTableContainer.add(new ListView<Candidate>("candidateSummaryRows", summaryCandidates) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<Candidate> item) {
					Candidate candidate = item.getModelObject();
					item.add(new Label("candidateSummaryName", valueOrDash(candidate != null ? candidate.getName() : null)));
					item.add(new Label("candidateSummaryMail", valueOrDash(resolveSummaryMail(candidate))));

					String publicProfileLink = resolveSummaryPublicProfileLink(candidate);
					ExternalLink publicProfileLinkComponent = new ExternalLink("candidateSummaryPublicProfileLink", hasText(publicProfileLink) ? publicProfileLink : "#");
					publicProfileLinkComponent.setVisible(hasText(publicProfileLink));
					publicProfileLinkComponent.add(new Label("candidateSummaryPublicProfileText", publicProfileLink));
					item.add(publicProfileLinkComponent);
					item.add(new Label("candidateSummaryPublicProfileEmpty", getString("candidateManagemenListNoLink")).setVisible(!hasText(publicProfileLink)));
					item.add(new Label("candidateSummaryStatus", resolveSummaryStatus(candidate)));
				}
			});
			Label candidateSummaryEmpty = new Label("candidateSummaryEmpty", Model.of(getString("candidateManagementSummaryEmpty")));
			candidateSummaryEmpty.setVisible(summaryCandidates.isEmpty());
			candidateSummaryContainer.add(candidateSummaryEmpty);

			Link<Void> back = new Link<Void>("back") {
				private static final long serialVersionUID = -2540140657992430113L;

				@Override
				public void onClick() {
					setResponsePage(ElectionTasksDashboard.class, UtilsParameters.getId(election.getElectionId()));
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

			Link<Void> markDoneNext = new Link<Void>("markDoneNext") {
				private static final long serialVersionUID = 3541306696081347007L;

				@Override
				public void onClick() {
					AppContext.getInstance().getManagerBeanRemote().persistElectionCandidatesSet(
						election.getElectionId(),
						election.getTitleSpanish(),
						SecurityUtils.getUserAdminId(),
						SecurityUtils.getClientIp()
					);
					setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(election.getElectionId()));
				}
			};
			add(markDoneNext);

			// navigation handled by markDoneNext
		}

		private List<Candidate> loadCandidates(Election election) {
			if (election == null) {
				return new ArrayList<>();
			}
			List<Candidate> candidates = AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(election.getElectionId());
			return candidates != null ? candidates : new ArrayList<Candidate>();
		}

		private String resolveSummaryMail(Candidate candidate) {
			if (candidate == null || candidate.isAbstention()) {
				return null;
			}
			return candidate.getMail();
		}

		private String resolveSummaryPublicProfileLink(Candidate candidate) {
			if (candidate == null || candidate.isAbstention()) {
				return "";
			}
			String explicitLink = candidate.getLinkSpanish();
			if (hasText(explicitLink)) {
				return explicitLink.trim();
			}
			return buildPublicCandidateProfileLink(candidate);
		}

		private String buildPublicCandidateProfileLink(Candidate candidate) {
			if (candidate == null || candidate.getElection() == null || candidate.getCandidateId() <= 0L) {
				return "";
			}
			String publicElectionToken = candidate.getElection().getPublicElectionToken();
			if (!hasText(publicElectionToken)) {
				return "";
			}
			return LinksUtils.buildPublicCandidateProfileLink(publicElectionToken, candidate.getCandidateId());
		}

		private String resolveSummaryStatus(Candidate candidate) {
			if (candidate == null) {
				return getString("candidateManagemenListCampusNoStatus");
			}
			return getString(resolveStatusKey(candidate.getStatus()));
		}

		private String resolveStatusKey(CandidateStatus status) {
			if (status == null) {
				return "candidateManagemenListCampusNoStatus";
			}
			switch (status) {
			case INCOMPLETE:
				return "candidateStatusOptionIncomplete";
			case PRECOMPLETE:
				return "candidateStatusOptionPreComplete";
			case COMPLETE:
				return "candidateStatusOptionComplete";
			case REJECTED:
				return "candidateStatusOptionRejected";
			case CONFIRMED_AND_PUBLISHED:
				return "candidateStatusOptionConfirmedAndPublished";
			default:
				return "candidateManagemenListCampusNoStatus";
			}
		}

		private String valueOrDash(String value) {
			return hasText(value) ? value : "-";
		}

		private boolean hasText(String value) {
			return value != null && !value.trim().isEmpty();
		}
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

}
