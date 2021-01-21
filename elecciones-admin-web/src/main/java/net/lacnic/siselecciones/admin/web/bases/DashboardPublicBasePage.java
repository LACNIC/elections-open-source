package net.lacnic.siselecciones.admin.web.bases;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.web.commons.TopBarPublic;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Eleccion;

public abstract class DashboardPublicBasePage extends WebPage {

	private static final long serialVersionUID = 6861984885215804314L;

	private Eleccion eleccion;
	private Eleccion[] elecciones;
	private String token;

	public DashboardPublicBasePage(PageParameters params) {
		setToken(UtilsParameters.getToken(params));
		Class classError = validarToken(params);
		if (classError != null) {
			setResponsePage(classError);
		}
		add(new TopBarPublic("topBarPublic"));
	}

	public String getIP() {
		WebClientInfo info = (WebClientInfo) getSession().getClientInfo();
		return info.getProperties().getRemoteAddress();
	}

	protected abstract Class validarToken(PageParameters params);

	public Eleccion getEleccion() {
		return eleccion;
	}

	public void setEleccion(Eleccion eleccion) {
		this.eleccion = eleccion;
	}

	public String getToken() {
		return token;
	}

	public String getIdioma() {
		return SecurityUtils.getLocale().getDisplayName();
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Eleccion[] getElecciones() {
		return elecciones;
	}

	public void setElecciones(Eleccion[] elecciones) {
		this.elecciones = elecciones;
	}

}
