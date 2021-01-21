package net.lacnic.siselecciones.admin.dashboard.user;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.dashboard.error.Error404;
import net.lacnic.siselecciones.admin.dashboard.error.ErrorVotacionNoComenzada;
import net.lacnic.siselecciones.admin.dashboard.error.ErrorVotacionNoPublica;
import net.lacnic.siselecciones.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.siselecciones.dominio.UsuarioPadron;

public class DashboardVotar extends DashboardPublicBasePage {

	private static final long serialVersionUID = -867241975964848115L;

	public Class validarToken(PageParameters params) {
		UsuarioPadron upd = Contexto.getInstance().getVotanteBeanRemote().verificarAccesoUP(getToken());

		// sacar false
		if (upd == null) {
			Contexto.getInstance().getVotanteBeanRemote().intentoFallidoIp(getIP());
			return Error404.class;
		} else {

			setEleccion(upd.getEleccion());

			if (!getEleccion().isComenzo()) {
				setResponsePage(ErrorVotacionNoComenzada.class, getPageParameters());
			} else if (!getEleccion().isHabilitadaParaVotar()) {
				return ErrorVotacionNoPublica.class;
			}

			if (Contexto.getInstance().getVotanteBeanRemote().isEleccionSimple(getEleccion().getIdEleccion()))
				setResponsePage(DashboardVotarEleccionSimple.class, params);
			else
				setResponsePage(DashboardVotarEleccionesJuntas.class, params);
		}
		return null;
	}

	public DashboardVotar(PageParameters params) {
		super(params);
		/**
		 * Comentario para Gonza -> el metodo validar toquen de arriba se llama
		 * en la linea 21 de la clase DashboardPublicBasePage ( la clase de la
		 * que se herededa) El validar ya estaba de antes y verifica que el
		 * usuario exista en esa votación que la ip desde donde vota no este
		 * bloqueada que la elección haya comenzado y que este habilitada para
		 * votar solo le agregue que redirija a la pantalla de votación vieja o
		 * a la nueva que hay que hacer
		 */
	}

}