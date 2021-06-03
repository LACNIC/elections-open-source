package net.lacnic.elections.adminweb.dashboard.user;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.error.ErrorVotacionNoComenzada;
import net.lacnic.elections.adminweb.ui.error.ErrorVotacionNoPublica;
import net.lacnic.elections.adminweb.web.bases.DashboardPublicBasePage;
import net.lacnic.elections.domain.UserVoter;

public class DashboardVotar extends DashboardPublicBasePage {

	private static final long serialVersionUID = -867241975964848115L;

	public Class validateToken(PageParameters params) {
		UserVoter upd = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccess(getToken());

		// sacar false
		if (upd == null) {
			AppContext.getInstance().getVoterBeanRemote().saveFailedAccessIp(getIP());
			return Error404.class;
		} else {

			setEleccion(upd.getElection());

			if (!getEleccion().isStarted()) {
				setResponsePage(ErrorVotacionNoComenzada.class, getPageParameters());
			} else if (!getEleccion().isEnabledToVote()) {
				return ErrorVotacionNoPublica.class;
			}

			if (AppContext.getInstance().getVoterBeanRemote().electionIsSimple(getEleccion().getElectionId()))
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