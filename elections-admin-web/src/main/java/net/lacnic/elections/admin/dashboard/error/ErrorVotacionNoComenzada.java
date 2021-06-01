package net.lacnic.elections.admin.dashboard.error;

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.dashboard.user.DashboardVotar;
import net.lacnic.elections.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.elections.domain.UserVoter;

public class ErrorVotacionNoComenzada extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1392182581021963077L;
	private String counter;
	private long distance;

	public ErrorVotacionNoComenzada(PageParameters params) {
		super(params);

		calcularCounter();

		Label countDown = new Label("counter", new PropertyModel<>(ErrorVotacionNoComenzada.this, "counter"));
		countDown.setOutputMarkupId(true);
		add(countDown);

		add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(10)) {

			private static final long serialVersionUID = 462616116072800705L;

			@Override
			protected void onPostProcessTarget(AjaxRequestTarget target) {
				super.onPostProcessTarget(target);
				calcularCounter();
				target.add(countDown);
				if (distance < 0) {
					setResponsePage(DashboardVotar.class, getPageParameters());
				}
			}

		});

		add(new BookmarkablePageLink<Void>("recargar", ErrorVotacionNoComenzada.class, params));

	}

	@Override
	public Class validarToken(PageParameters params) {
		UserVoter upd = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccess(getToken());
		if (upd == null) {
			AppContext.getInstance().getVoterBeanRemote().saveFailedAccessIp(getIP());
			return Error404.class;
		} else {
			setEleccion(upd.getElection());
			if (getEleccion().isStarted()) {
				setResponsePage(DashboardVotar.class, getPageParameters());
			} else if (getEleccion().isFinished() || !getEleccion().isVotingLinkAvailable()) {
				setResponsePage(ErrorVotacionNoPublica.class, getPageParameters());
			}
		}
		return null;
	}

	private void calcularCounter() {
		distance = getEleccion().getStartDate().getTime() - new Date().getTime();
		long days = distance / (1000 * 60 * 60 * 24);
		long hours = (distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		long minutes = (distance % (1000 * 60 * 60)) / (1000 * 60);
		long seconds = (distance % (1000 * 60)) / 1000;

		setCounter("" + days + "d " + "" + hours + "h " + "" + minutes + "m " + "" + seconds + "s");
	}

	public String getCounter() {
		return counter;
	}

	public void setCounter(String counter) {
		this.counter = counter;
	}

}
