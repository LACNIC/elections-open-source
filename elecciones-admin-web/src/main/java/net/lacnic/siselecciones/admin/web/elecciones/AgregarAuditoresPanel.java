package net.lacnic.siselecciones.admin.web.elecciones;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionAuditores;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionCandidatos;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardHomePage;
import net.lacnic.siselecciones.admin.web.commons.AuditorValidator;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Auditor;
import net.lacnic.siselecciones.dominio.Eleccion;

public class AgregarAuditoresPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private Auditor auditor;
	boolean checkBoxValueBios = true;

	public AgregarAuditoresPanel(String id, Eleccion eleccion) {
		super(id);
		try {
			Form<Void> form = new Form<>("eleccionAuditoresForm");
			add(form);

			auditor = new Auditor();
			TextField<String> nombreAuditor = new TextField<>("nombre", new PropertyModel<>(auditor, "nombre"));
			nombreAuditor.setRequired(true);
			form.add(nombreAuditor);

			TextField<String> email = new TextField<>("email", new PropertyModel<>(auditor, "mail"));
			email.setRequired(true);
			email.add(EmailAddressValidator.getInstance());
			form.add(email);

			form.add(new AuditorValidator(eleccion.getIdEleccion(), nombreAuditor, email));

			form.add(new CheckBox("checkComisionado", new PropertyModel<>(auditor, "comisionado")));

			SubmitLink agregarAuditorLink = new SubmitLink("agregarAuditor") {

				private static final long serialVersionUID = -6152250993469094986L;

				@Override
				public void onSubmit() {
					super.onSubmit();
					Contexto.getInstance().getManagerBeanRemote().agregarAuditor(eleccion.getIdEleccion(), auditor, eleccion.getTituloEspanol(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
					setResponsePage(DashboardGestionAuditores.class, UtilsParameters.getId(eleccion.getIdEleccion()));
					getSession().info(getString("auditorManagementExitoAdd"));
				}

			};
			form.add(agregarAuditorLink);

			Link<Void> submitButton = new Link<Void>("terminar") {
				
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						Contexto.getInstance().getManagerBeanRemote().persistirAuditoresSeteados(eleccion.getIdEleccion(), eleccion.getTituloEspanol(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
						setResponsePage(DashboardHomePage.class);
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			form.add(submitButton);

			Link<Void> saltarLink = new Link<Void>("saltar") {

				private static final long serialVersionUID = -5077147466274097615L;

				@Override
				public void onClick() {
					setResponsePage(DashboardHomePage.class);
				}
			};
			form.add(saltarLink);

			Link<Void> atras = new Link<Void>("atras") {

				private static final long serialVersionUID = 4841898170457659991L;

				@Override
				public void onClick() {
					setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
				}
			};
			form.add(atras);

		} catch (Exception e) {
			error(getString("auditorManagementErrorAdd"));
		}
	}

}