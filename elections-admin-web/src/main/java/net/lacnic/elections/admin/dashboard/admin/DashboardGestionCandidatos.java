package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.commons.GestionEleccionStatusPanel;
import net.lacnic.elections.admin.web.elecciones.CamposEleccionCandidatosPanel;
import net.lacnic.elections.admin.web.elecciones.ListaEdicionCandidatosPanel;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ActivityType;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardGestionCandidatos extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;
	private Election eleccion;

	public DashboardGestionCandidatos(PageParameters params) {
		super(params);

		setEleccion(AppContext.getInstance().getManagerBeanRemote().obtenerEleccion(UtilsParameters.getIdAsLong(params)));
		add(new FeedbackPanel("feedback"));
		add(new CandidatosEleccionForm("eleccionCandidatosForm", eleccion));
		add(new GestionEleccionStatusPanel("tabCandidatos", eleccion));
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
						String descripcion = SecurityUtils.getAdminId().toUpperCase() + getString("candidateManagemenExitoAddCandidates") + getEleccion().getTitleSpanish();
						AppContext.getInstance().getManagerBeanRemote().persistirActividad(SecurityUtils.getAdminId(), ActivityType.ADD_CANDIDATES, descripcion, SecurityUtils.getIPClient(), getEleccion().getElectionId());
						setResponsePage(DashboardGestionAuditores.class, UtilsParameters.getId(eleccion.getElectionId()));
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
					setResponsePage(DashboardGestionAuditores.class, UtilsParameters.getId(eleccion.getElectionId()));
				}
			};
			add(saltarLink);

			Link<Void> atras = new Link<Void>("atras") {

				private static final long serialVersionUID = -2540140657992430113L;

				@Override
				public void onClick() {
					setResponsePage(DashboardGestionPadron.class, UtilsParameters.getId(eleccion.getElectionId()));
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