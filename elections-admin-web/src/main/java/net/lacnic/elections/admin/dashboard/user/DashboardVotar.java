package net.lacnic.elections.admin.dashboard.user;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.dashboard.error.Error404;
import net.lacnic.elections.admin.dashboard.error.ErrorVotacionNoComenzada;
import net.lacnic.elections.admin.dashboard.error.ErrorVotacionNoPublica;
import net.lacnic.elections.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.elections.domain.UserVoter;

public class DashboardVotar extends DashboardPublicBasePage {

	private static final long serialVersionUID = -867241975964848115L;

	public Class validarToken(PageParameters params) {
		UserVoter upd = AppContext.getInstance().getVoterBeanRemote().verificarAccesoUP(getToken());

		// sacar false
		if (upd == null) {
			AppContext.getInstance().getVoterBeanRemote().intentoFallidoIp(getIP());
			return Error404.class;
		} else {

			setEleccion(upd.getElection());

			if (!getEleccion().isStarted()) {
				setResponsePage(ErrorVotacionNoComenzada.class, getPageParameters());
			} else if (!getEleccion().isEnabledToVote()) {
				return ErrorVotacionNoPublica.class;
			}

			if (AppContext.getInstance().getVoterBeanRemote().isEleccionSimple(getEleccion().getIdElection()))
				setResponsePage(DashboardVotarEleccionSimple.class, params);
			else
				setResponsePage(DashboardVotarEleccionesJuntas.class, params);
		}
		return null;
	}

	public DashboardVotar(PageParameters params) {
		super(params);
	}

}