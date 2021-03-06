package net.lacnic.siselecciones.admin.dashboard.admin;

import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.siselecciones.admin.web.commons.GestionEleccionStatusPanel;
import net.lacnic.siselecciones.admin.web.elecciones.CamposEleccionDetallePanel;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.ejb.ManagerEleccionesEJB;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardGestionEleccion extends DashboardAdminBasePage {

	private static final long serialVersionUID = 2749798787618064089L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private Eleccion eleccion;

	public DashboardGestionEleccion(PageParameters params) {
		super(params);
		eleccion = new Eleccion();
		eleccion.setLinkEspanol((Contexto.getInstance().getManagerBeanRemote().obtenerWebsitePorDefecto()));
		eleccion.setRemitentePorDefecto(Contexto.getInstance().getManagerBeanRemote().obtenerRemitentePorDefecto());
		if (UtilsParameters.isId(params)) {
			setEleccion(Contexto.getInstance().getManagerBeanRemote().obtenerEleccion(UtilsParameters.getIdAsLong(params)));
			getEleccion().initStringsFechaInicioyFin();
		}
		add(new FeedbackPanel("feedback"));
		add(new GestionEleccionStatusPanel("tabDetalle", eleccion));
		add(new NuevaEleccionForm("eleccionDetalleForm"));
	}

	public final class NuevaEleccionForm extends Form<Void> {

		private static final long serialVersionUID = -5221887812611102034L;

		private ManagerEleccionesEJB managerBeanRemote;
		CamposEleccionDetallePanel camposEleccionDetallePanel;

		public NuevaEleccionForm(String id) {
			super(id);
			try {
				camposEleccionDetallePanel = new CamposEleccionDetallePanel("campos", eleccion);
				add(camposEleccionDetallePanel);

				Button submitButton = new Button("submit") {

					private static final long serialVersionUID = 1073607359256986749L;

					@Override
					public void onSubmit() {
						try {
							boolean esNueva = true;
							boolean esSupra = false;
							Date dtIniOrig = null;
							Eleccion elecOrig;

							managerBeanRemote = Contexto.getInstance().getManagerBeanRemote();

							// Valido si la elección esta junta a otra, entonces NO puedo modificar la fecha de inicio
							if (eleccion.getIdEleccion() == 0) {
								esNueva = true;
							} else {
								esNueva = false;
								esSupra = managerBeanRemote.isSupraEleccion(eleccion.getIdEleccion());
								if (esSupra) {
									elecOrig = managerBeanRemote.obtenerEleccion(eleccion.getIdEleccion());
									dtIniOrig = elecOrig.getFechaInicio();

								}
							};

							eleccion.initDatesFechaInicioyFin();
							if (eleccion.getFechaInicio().after(eleccion.getFechaFin())) {
								error(getString("electionManagementErrorFechas"));
							} else if ((!esNueva) && (esSupra) && (dtIniOrig.compareTo(eleccion.getFechaInicio())!= 0)) {
								error(getString("electionManagementErrorFecElecSupra"));
							} else {
								copiarTextos();
								Eleccion eleccionNueva;
								if (eleccion.getIdEleccion() == 0) {
									getSession().info(getString("electionManagementExitoCreate"));
									eleccionNueva = managerBeanRemote.actualizarEleccion(eleccion, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
								} else {
									getSession().info(getString("electionManagementExitoUpdate"));
									eleccionNueva = managerBeanRemote.actualizarEleccion(eleccion, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
								}
								setResponsePage(DashboardGestionPadron.class, UtilsParameters.getId(eleccionNueva.getIdEleccion()));

							}
						} catch (Exception e) {
							error(e.getMessage());
						}
					}
				};
				add(submitButton);

				Link<Void> saltarLink = new Link<Void>("saltar") {

					private static final long serialVersionUID = 2172660804449339859L;

					@Override
					public void onClick() {
						setResponsePage(DashboardHomePage.class);
					}
				};

				add(saltarLink);

			} catch (Exception e) {
				appLogger.error(e);
				error(e.getMessage());
			}
		}

		public void copiarTextos() throws Exception {
			if (eleccion.isSolosp()) {
				eleccion.copiarDescripcionesIdiomaAlResto("SP");
				eleccion.copiarTitulosIdiomaAlResto("SP");
				eleccion.copiarUrlsIdiomaAlResto("SP");
			}
		}

	}

	public Eleccion getEleccion() {
		return eleccion;
	}

	public void setEleccion(Eleccion eleccion) {
		this.eleccion = eleccion;
	}
}