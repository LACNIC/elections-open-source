package net.lacnic.elections.adminweb.ui.login;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.ElectionsWebAdminSession;
import net.lacnic.elections.adminweb.web.bases.DashboardPublicBasePage;
import net.lacnic.elections.domain.Customization;


public class LoginDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1L;
	
	private Customization customization;
	private String loginTitle;

	public LoginDashboard(PageParameters params) {
		super(params);
		
		customization = AppContext.getInstance().getManagerBeanRemote().getCustomization();
		loginTitle = customization.getLoginTitle();
		
		((ElectionsWebAdminSession) WebSession.get()).invalidateNow();
		add(new FeedbackPanel("feedbackPanel"));
		
		final Label tituloLoginLabel = new Label("loginTitle", new PropertyModel<>(LoginDashboard.this, "loginTitle"));
		add(tituloLoginLabel);
		
		add(new LoginPanel("loginPanel"));
		add(new LoginLogoPanel("logoPanel"));
	}

	@Override
	protected Class validateToken(PageParameters params) {
		return null;
	}

}
