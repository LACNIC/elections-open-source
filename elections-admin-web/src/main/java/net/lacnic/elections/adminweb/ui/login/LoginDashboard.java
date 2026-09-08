package net.lacnic.elections.adminweb.ui.login;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;
import net.lacnic.elections.domain.Customization;


public class LoginDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1L;
	
	private Customization customization;

	public LoginDashboard(PageParameters params) {
		super(params);
		
		customization = AppContext.getInstance().getManagerBeanRemote().getCustomization();
		
		add(new FeedbackPanel("feedbackPanel"));
		add(new Label("loginTitle", customization.getLoginTitle()));
		
		add(new LoginPanel("loginPanel"));
		add(new LoginLogoPanel("logoPanel"));
	}

	@Override
	protected Class validateToken(PageParameters params) {
		return null;
	}

}
