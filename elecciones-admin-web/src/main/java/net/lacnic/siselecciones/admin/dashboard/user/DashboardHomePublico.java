package net.lacnic.siselecciones.admin.dashboard.user;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardHomePage;
import net.lacnic.siselecciones.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.siselecciones.dominio.Personalizacion;
import net.lacnic.siselecciones.admin.app.Contexto;

public class DashboardHomePublico extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1392182581021963077L;
	
	private Personalizacion personalizacion;
	
	private String homeHtml;

	public DashboardHomePublico(PageParameters params) {
		super(params);
		
		Boolean showHome = true;
		
		personalizacion = Contexto.getInstance().getManagerBeanRemote().getPersonalizacion();
		showHome = personalizacion.isShowHome();
		homeHtml = personalizacion.getHomeHtml();
		
		WebMarkupContainer homeContainer = new WebMarkupContainer("homeContainer");
		WebMarkupContainer homeCustom = new WebMarkupContainer("homeCustom");
		Label homeHtmlLabel = new Label("homeHtml", new PropertyModel<>(DashboardHomePublico.this, "homeHtml"));
		add(homeContainer);
		homeHtmlLabel.setEscapeModelStrings(false);
		homeCustom.add(homeHtmlLabel).setEscapeModelStrings(false);
		add(homeCustom);
		
		if (showHome) {
			homeContainer.setVisible(false);
		} else {
			homeCustom.setVisible(false);
		}

		if (SecurityUtils.isSignedIn())
			setResponsePage(DashboardHomePage.class);

		
		
	}

	@Override
	protected Class validarToken(PageParameters params) {
		return null;
	}

}
