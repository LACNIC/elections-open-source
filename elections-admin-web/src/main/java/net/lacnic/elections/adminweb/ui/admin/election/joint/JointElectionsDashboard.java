package net.lacnic.elections.adminweb.ui.admin.election.joint;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.ui.bases.DashboardManagerBasePage;


public class JointElectionsDashboard extends DashboardManagerBasePage {

	private static final long serialVersionUID = -7174020647398765740L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");


	public JointElectionsDashboard() {
		super(new PageParameters());

		try {
			FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
			feedbackPanel.setOutputMarkupPlaceholderTag(true);
			add(feedbackPanel);

			EditJointElectionsPanel editJointElectionsPanel = new EditJointElectionsPanel("editJointElectionsPanel");
			add(editJointElectionsPanel);

			JointElectionsListPanel jointElectionsListPanel = new JointElectionsListPanel("jointElectionsListPanel");
			add(jointElectionsListPanel);
		} catch (Exception e) {
			setResponsePage(getApplication().getHomePage());
			appLogger.error(e.getMessage(), e);
		}
	}

}
