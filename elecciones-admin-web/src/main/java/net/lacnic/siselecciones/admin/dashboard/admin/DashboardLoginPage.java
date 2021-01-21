package net.lacnic.siselecciones.admin.dashboard.admin;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.SisEleccionesManagerSession;
import net.lacnic.siselecciones.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.siselecciones.admin.web.commons.FooterLogoPanel;
import net.lacnic.siselecciones.admin.web.commons.LoginPanel;

public class DashboardLoginPage extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1L;

	public DashboardLoginPage(PageParameters params) {
		super(params);
		((SisEleccionesManagerSession) WebSession.get()).invalidateNow();
		add(new FeedbackPanel("feedbackPanel"));
		add(new LoginPanel("loginPanel"));
		add(new FooterLogoPanel("footerPanel"));

	}

	@Override
	protected Class validarToken(PageParameters params) {
		return null;
	}

}
