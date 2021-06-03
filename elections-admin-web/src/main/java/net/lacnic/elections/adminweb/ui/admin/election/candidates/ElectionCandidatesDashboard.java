package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.web.elecciones.CamposEleccionCandidatosPanel;
import net.lacnic.elections.adminweb.web.elecciones.ListaEdicionCandidatosPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ActivityType;


@AuthorizeInstantiation("elections-only-one")
public class ElectionCandidatesDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;
	private Election eleccion;

	public ElectionCandidatesDashboard(PageParameters params) {
		super(params);

		setEleccion(AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params)));
		add(new FeedbackPanel("feedback"));
		add(new CandidatosEleccionForm("eleccionCandidatosForm", eleccion));
		add(new ManageElectionTabsPanel("tabsPanel", eleccion));
		add(new ListaEdicionCandidatosPanel("listaCandidatosPanel", eleccion));
	}

	public final class CandidatosEleccionForm extends Form<Void> {

		private static final long serialVersionUID = 2351447413365706203L;

		public CandidatosEleccionForm(String id, final Election eleccion) {
			super(id);
			setFileMaxSize(Bytes.kilobytes(2048));
			CamposEleccionCandidatosPanel camposEleccionCandidatosPanel = new CamposEleccionCandidatosPanel("campos", eleccion);
			add(camposEleccionCandidatosPanel);

			Link<Void> submitButton = new Link<Void>("siguiente") {

				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						String descripcion = SecurityUtils.getUserAdminId().toUpperCase() + getString("candidateManagemenExitoAddCandidates") + getEleccion().getTitleSpanish();
						AppContext.getInstance().getManagerBeanRemote().persistActivity(SecurityUtils.getUserAdminId(), ActivityType.ADD_CANDIDATES, descripcion, SecurityUtils.getClientIp(), getEleccion().getElectionId());
						setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(eleccion.getElectionId()));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}

			};
			add(submitButton);
			submitButton.setEnabled(eleccion.isCandidatesSet());

			Link<Void> saltarLink = new Link<Void>("saltar") {

				private static final long serialVersionUID = 832866944403935918L;

				@Override
				public void onClick() {
					setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(eleccion.getElectionId()));
				}
			};
			add(saltarLink);

			Link<Void> atras = new Link<Void>("atras") {

				private static final long serialVersionUID = -2540140657992430113L;

				@Override
				public void onClick() {
					setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(eleccion.getElectionId()));
				}
			};
			add(atras);

		}
	}

	public Election getEleccion() {
		return eleccion;
	}

	public void setEleccion(Election eleccion) {
		this.eleccion = eleccion;
	}
}