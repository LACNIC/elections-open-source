package net.lacnic.elections.adminweb.ui.admin.election;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;

public class ElectionsDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 9015321061696368394L;


	public ElectionsDashboard(PageParameters params) {
		super(params);
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		add(new ElectionsListPanel("electionsList", params));
	}

}
