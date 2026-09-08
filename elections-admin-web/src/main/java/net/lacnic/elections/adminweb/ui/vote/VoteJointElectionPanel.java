package net.lacnic.elections.adminweb.ui.vote;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.domain.UserVoter;

public class VoteJointElectionPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public VoteJointElectionPanel(String id, PageParameters params, UserVoter[] userVoters, Class<? extends IRequestablePage> votePageClass) {
		super(id);

		add(new FeedbackPanel("feedbackPanel"));

		WebMarkupContainer voteNotComplete = new WebMarkupContainer("voteNotComplete");
		voteNotComplete.setVisible(!userVoters[0].isVoted() || !userVoters[1].isVoted());
		add(voteNotComplete);

		add(new Label("voter", userVoters[0].getCompleteVoterInformation()));
		add(new VoteJointElectionDetailPanel("election1Detail", params, userVoters[0], true, votePageClass));
		add(new VoteJointElectionDetailPanel("election2Detail", params, userVoters[1], false, votePageClass));
	}
}
