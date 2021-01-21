package net.lacnic.siselecciones.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.siselecciones.admin.web.commons.GestionEleccionStatusPanel;
import net.lacnic.siselecciones.admin.web.elecciones.CamposEleccionCandidatosPanel;
import net.lacnic.siselecciones.admin.web.elecciones.ListaEdicionCandidatosPanel;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.dominio.TipoActividad;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardGestionCandidatos extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;
	private Eleccion eleccion;

	public DashboardGestionCandidatos(PageParameters params) {
		super(params);

		setEleccion(Contexto.getInstance().getManagerBeanRemote().obtenerEleccion(UtilsParameters.getIdAsLong(params)));
		add(new FeedbackPanel("feedback"));
		add(new CandidatosEleccionForm("eleccionCandidatosForm", eleccion));
		add(new GestionEleccionStatusPanel("tabCandidatos", eleccion));
		add(new ListaEdicionCandidatosPanel("listaCandidatosPanel", eleccion));
	}

	public final class CandidatosEleccionForm extends Form<Void> {

		private static final long serialVersionUID = 2351447413365706203L;

		public CandidatosEleccionForm(String id, final Eleccion eleccion) {
			super(id);
			setFileMaxSize(Bytes.kilobytes(2048));
			CamposEleccionCandidatosPanel camposEleccionCandidatosPanel = new CamposEleccionCandidatosPanel("campos", eleccion);
			add(camposEleccionCandidatosPanel);

			Link<Void> submitButton = new Link<Void>("siguiente") {

				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						String descripcion = SecurityUtils.getAdminId().toUpperCase() + getString("candidateManagemenExitoAddCandidates") + getEleccion().getTituloEspanol();
						Contexto.getInstance().getManagerBeanRemote().persistirActividad(SecurityUtils.getAdminId(), TipoActividad.AGREGAR_CANDIDATOS, descripcion, SecurityUtils.getIPClient(), getEleccion().getIdEleccion());
						setResponsePage(DashboardGestionAuditores.class, UtilsParameters.getId(eleccion.getIdEleccion()));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}

			};
			add(submitButton);
			submitButton.setEnabled(eleccion.isCandidatosSeteado());

			Link<Void> saltarLink = new Link<Void>("saltar") {

				private static final long serialVersionUID = 832866944403935918L;

				@Override
				public void onClick() {
					setResponsePage(DashboardGestionAuditores.class, UtilsParameters.getId(eleccion.getIdEleccion()));
				}
			};
			add(saltarLink);

			Link<Void> atras = new Link<Void>("atras") {

				private static final long serialVersionUID = -2540140657992430113L;

				@Override
				public void onClick() {
					setResponsePage(DashboardGestionPadron.class, UtilsParameters.getId(eleccion.getIdEleccion()));
				}
			};
			add(atras);

		}
	}

	public Eleccion getEleccion() {
		return eleccion;
	}

	public void setEleccion(Eleccion eleccion) {
		this.eleccion = eleccion;
	}
}