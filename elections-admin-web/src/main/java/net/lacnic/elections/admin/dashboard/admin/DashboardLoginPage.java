package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SisEleccionesManagerSession;
import net.lacnic.elections.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.elections.admin.web.commons.FooterLogoPanel;
import net.lacnic.elections.admin.web.commons.LoginPanel;
import net.lacnic.elections.domain.Customization;

public class DashboardLoginPage extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1L;
	
	private Customization personalizacion;
	private String tituloLogin;

	public DashboardLoginPage(PageParameters params) {
		super(params);
		
		personalizacion = AppContext.getInstance().getManagerBeanRemote().getPersonalizacion();
		tituloLogin = personalizacion.getLoginTitle();
		
		((SisEleccionesManagerSession) WebSession.get()).invalidateNow();
		add(new FeedbackPanel("feedbackPanel"));
		
		final Label tituloLoginLabel = new Label("tituloLogin", new PropertyModel<>(DashboardLoginPage.this, "tituloLogin"));
		add(tituloLoginLabel);
		
		add(new LoginPanel("loginPanel"));
		add(new FooterLogoPanel("footerPanel"));

	}

	@Override
	protected Class validarToken(PageParameters params) {
		return null;
	}

}
