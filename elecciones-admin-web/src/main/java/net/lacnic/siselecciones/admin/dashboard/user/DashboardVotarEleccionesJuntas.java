package net.lacnic.siselecciones.admin.dashboard.user;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.dashboard.error.Error404;
import net.lacnic.siselecciones.admin.dashboard.error.ErrorVotacionNoComenzada;
import net.lacnic.siselecciones.admin.dashboard.error.ErrorVotacionNoPublica;
import net.lacnic.siselecciones.admin.panel.user.DetalleVoteEleccionJuntasPanel;
import net.lacnic.siselecciones.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.dominio.UsuarioPadron;

public class DashboardVotarEleccionesJuntas extends DashboardPublicBasePage {

	private static final long serialVersionUID = -867241975964848115L;
	private UsuarioPadron[] upds;

	@Override
	public Class validarToken(PageParameters params) {
		upds = Contexto.getInstance().getVotanteBeanRemote().verificarAccesoUPEleccionJunta(getToken());

		if (upds == null) {
			Contexto.getInstance().getVotanteBeanRemote().intentoFallidoIp(getIP());
			return Error404.class;
		} else {

			setElecciones(new Eleccion[] { upds[0].getEleccion(), upds[1].getEleccion() });
			if (!upds[0].getEleccion().isComenzo()) { // Definir si validamos
				// las dos o una sola
				setResponsePage(ErrorVotacionNoComenzada.class, getPageParameters());
			} else if (!upds[0].getEleccion().isHabilitadaParaVotar()) {
				return ErrorVotacionNoPublica.class;
			}
		}
		return null;
	}

	public DashboardVotarEleccionesJuntas(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedbackPanel"));

		WebMarkupContainer noVotoCompleto = new WebMarkupContainer("noVotoCompleto");
		noVotoCompleto.setVisible(!upds[0].isYaVoto() || !upds[1].isYaVoto());
		add(noVotoCompleto);
		
		add(new Label("votante", upds[0].getInformacionDelVotanteCompleta()));
		add(new DetalleVoteEleccionJuntasPanel("detalleEleccion1", params, upds[0].getEleccion(), upds[0], true));
		add(new DetalleVoteEleccionJuntasPanel("detalleEleccion2", params, upds[1].getEleccion(), upds[1], false));
	}

}