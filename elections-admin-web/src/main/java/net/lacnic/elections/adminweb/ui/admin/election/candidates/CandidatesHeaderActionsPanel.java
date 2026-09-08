package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.OnOffSwitch;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;

public class CandidatesHeaderActionsPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public CandidatesHeaderActionsPanel(String id, final Election election, boolean showCandidateForm) {
		super(id);

		boolean hasAbstentionCandidate = hasAbstentionCandidate(election);

		add(new Label("randomOrderLabel", getString("candidateManagemenListSort")));
		add(new OnOffSwitch("randomOrder", new PropertyModel<>(election, "randomOrderCandidates")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void action() {
				AppContext.getInstance().getManagerBeanRemote().setSortCandidatesRandomly(election.getElectionId(), election.isRandomOrderCandidates());
				if (election.isRandomOrderCandidates()) {
					getSession().success(getString("candidateManagementRandomOrderEnabled"));
				} else {
					getSession().success(getString("candidateManagementRandomOrderDisabled"));
				}
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		});

		Link<Void> addAbstention = new Link<Void>("addAbstention") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				AppContext.getInstance().getManagerBeanRemote().addAbstentionCandidate(election.getElectionId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
				getSession().info(getString("candidateManagementAbstentionAdded"));
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		addAbstention.setVisible(!hasAbstentionCandidate);
		add(addAbstention);

		BookmarkablePageLink<Void> addNewCandidate = new BookmarkablePageLink<>("addNewCandidate", ElectionCandidatesDashboard.class, UtilsParameters.getNewCandidate(election.getElectionId()));
		addNewCandidate.setVisible(!showCandidateForm);
		add(addNewCandidate);
	}

	private boolean hasAbstentionCandidate(Election election) {
		List<Candidate> candidates = AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(election.getElectionId());
		if (candidates == null) {
			return false;
		}
		for (Candidate candidate : candidates) {
			if (candidate != null && candidate.isAbstention()) {
				return true;
			}
		}
		return false;
	}
}
