package net.lacnic.siselecciones.admin.dashboard.admin;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.SisEleccionesManagerSession;
import net.lacnic.siselecciones.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.siselecciones.admin.web.commons.FooterLogoPanel;
import net.lacnic.siselecciones.admin.web.commons.LoginPanel;
import net.lacnic.siselecciones.dominio.Personalizacion;
import net.lacnic.siselecciones.admin.app.Contexto;

public class DashboardLoginPage extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1L;
	
	private Personalizacion personalizacion;
	private String tituloLogin;

	public DashboardLoginPage(PageParameters params) {
		super(params);
		
		personalizacion = Contexto.getInstance().getManagerBeanRemote().getPersonalizacion();
		tituloLogin = personalizacion.getTituloLogin();
		
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
