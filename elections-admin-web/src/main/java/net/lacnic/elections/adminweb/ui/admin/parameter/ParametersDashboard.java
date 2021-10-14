package net.lacnic.elections.adminweb.ui.admin.parameter;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;


public class ParametersDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = 4727744662113996465L;


	public ParametersDashboard(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));		
		add(new AddParameterPanel("addParameter"));
		add(new ParametersListPanel("parametersList"));
	}

}
