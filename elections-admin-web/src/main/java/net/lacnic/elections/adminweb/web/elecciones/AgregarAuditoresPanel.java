package net.lacnic.elections.adminweb.web.elecciones;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.validators.AuditorValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Election;

public class AgregarAuditoresPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private Auditor auditor;
	boolean checkBoxValueBios = true;

	public AgregarAuditoresPanel(String id, Election eleccion) {
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

			form.add(new AuditorValidator(eleccion.getElectionId(), nombreAuditor, email));

			form.add(new CheckBox("checkComisionado", new PropertyModel<>(auditor, "comisionado")));

			SubmitLink agregarAuditorLink = new SubmitLink("agregarAuditor") {

				private static final long serialVersionUID = -6152250993469094986L;

				@Override
				public void onSubmit() {
					super.onSubmit();
					AppContext.getInstance().getManagerBeanRemote().addAuditor(eleccion.getElectionId(), auditor, eleccion.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(eleccion.getElectionId()));
					getSession().info(getString("auditorManagementExitoAdd"));
				}

			};
			form.add(agregarAuditorLink);

			Link<Void> submitButton = new Link<Void>("terminar") {
				
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						AppContext.getInstance().getManagerBeanRemote().persistElectionAuditorsSet(eleccion.getElectionId(), eleccion.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						setResponsePage(ElectionsDashboard.class);
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
					setResponsePage(ElectionsDashboard.class);
				}
			};
			form.add(saltarLink);

			Link<Void> atras = new Link<Void>("atras") {

				private static final long serialVersionUID = 4841898170457659991L;

				@Override
				public void onClick() {
					setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(eleccion.getElectionId()));
				}
			};
			form.add(atras);

		} catch (Exception e) {
			error(getString("auditorManagementErrorAdd"));
		}
	}

}