package net.lacnic.elections.admin.dashboard.user;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.dashboard.error.Error404;
import net.lacnic.elections.admin.dashboard.error.ErrorVotacionNoComenzada;
import net.lacnic.elections.admin.dashboard.error.ErrorVotacionNoPublica;
import net.lacnic.elections.admin.panel.user.DetalleVoteEleccionJuntasPanel;
import net.lacnic.elections.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;

public class DashboardVotarEleccionesJuntas extends DashboardPublicBasePage {

	private static final long serialVersionUID = -867241975964848115L;
	private UserVoter[] upds;

	@Override
	public Class validarToken(PageParameters params) {
		upds = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccessJointElection(getToken());

		if (upds == null) {
			AppContext.getInstance().getVoterBeanRemote().saveFailedAccessIp(getIP());
			return Error404.class;
		} else {

			setElecciones(new Election[] { upds[0].getElection(), upds[1].getElection() });
			if (!upds[0].getElection().isStarted()) { // Definir si validamos
				// las dos o una sola
				setResponsePage(ErrorVotacionNoComenzada.class, getPageParameters());
			} else if (!upds[0].getElection().isEnabledToVote()) {
				return ErrorVotacionNoPublica.class;
			}
		}
		return null;
	}

	public DashboardVotarEleccionesJuntas(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedbackPanel"));

		WebMarkupContainer noVotoCompleto = new WebMarkupContainer("noVotoCompleto");
		noVotoCompleto.setVisible(!upds[0].isVoted() || !upds[1].isVoted());
		add(noVotoCompleto);
		
		add(new Label("votante", upds[0].getCompleteVoterInformation()));
		add(new DetalleVoteEleccionJuntasPanel("detalleEleccion1", params, upds[0].getElection(), upds[0], true));
		add(new DetalleVoteEleccionJuntasPanel("detalleEleccion2", params, upds[1].getElection(), upds[1], false));
	}

}