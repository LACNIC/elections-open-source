package net.lacnic.elections.admin.dashboard.admin;

import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.commons.GestionEleccionStatusPanel;
import net.lacnic.elections.admin.web.elecciones.CamposEleccionDetallePanel;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.ejb.ElectionsManagerEJB;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardGestionEleccion extends DashboardAdminBasePage {

	private static final long serialVersionUID = 2749798787618064089L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private Election eleccion;

	public DashboardGestionEleccion(PageParameters params) {
		super(params);
		eleccion = new Election();
		eleccion.setLinkSpanish((AppContext.getInstance().getManagerBeanRemote().getDefaultWebsite()));
		eleccion.setDefaultSender(AppContext.getInstance().getManagerBeanRemote().getDefaultSender());
		if (UtilsParameters.isId(params)) {
			setEleccion(AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params)));
			getEleccion().initStringsStartEndDates();
		}
		add(new FeedbackPanel("feedback"));
		add(new GestionEleccionStatusPanel("tabDetalle", eleccion));
		add(new NuevaEleccionForm("eleccionDetalleForm"));
	}

	public final class NuevaEleccionForm extends Form<Void> {

		private static final long serialVersionUID = -5221887812611102034L;

		private ElectionsManagerEJB managerBeanRemote;
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
							Election elecOrig;

							managerBeanRemote = AppContext.getInstance().getManagerBeanRemote();

							// Valido si la elección esta junta a otra, entonces NO puedo modificar la fecha de inicio
							if (eleccion.getElectionId() == 0) {
								esNueva = true;
							} else {
								esNueva = false;
								esSupra = managerBeanRemote.isJointElection(eleccion.getElectionId());
								if (esSupra) {
									elecOrig = managerBeanRemote.getElection(eleccion.getElectionId());
									dtIniOrig = elecOrig.getStartDate();

								}
							};

							eleccion.initDatesStartEndDates();
							if (eleccion.getStartDate().after(eleccion.getEndDate())) {
								error(getString("electionManagementErrorFechas"));
							} else if ((!esNueva) && (esSupra) && (dtIniOrig.compareTo(eleccion.getStartDate())!= 0)) {
								error(getString("electionManagementErrorFecElecSupra"));
							} else {
								copiarTextos();
								Election eleccionNueva;
								if (eleccion.getElectionId() == 0) {
									getSession().info(getString("electionManagementExitoCreate"));
									eleccionNueva = managerBeanRemote.updateElection(eleccion, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
								} else {
									getSession().info(getString("electionManagementExitoUpdate"));
									eleccionNueva = managerBeanRemote.updateElection(eleccion, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
								}
								setResponsePage(DashboardGestionPadron.class, UtilsParameters.getId(eleccionNueva.getElectionId()));

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
			if (eleccion.isOnlySp()) {
				eleccion.copyLanguageDescriptions("SP");
				eleccion.copyLanguageTitles("SP");
				eleccion.copyLanguageURLs("SP");
			}
		}

	}

	public Election getEleccion() {
		return eleccion;
	}

	public void setEleccion(Election eleccion) {
		this.eleccion = eleccion;
	}
}