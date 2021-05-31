package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardEditarAuditor extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;
	private Auditor auditor;
	private String nombre;
	private String email;

	public DashboardEditarAuditor(PageParameters params) {
		super(params);
		long idAuditor = UtilsParameters.getAuditAsLong(params);
		auditor = AppContext.getInstance().getManagerBeanRemote().obtenerAuditor(idAuditor);
		email = auditor.getMail();
		nombre = auditor.getName();
		add(new FeedbackPanel("feedback"));
		Form<Void> formAuditor = new Form<>("formAuditor");
		add(formAuditor);

		TextField<String> nombreAuditor = new TextField<>("nombre", new PropertyModel<>(auditor, "nombre"));
		nombreAuditor.setRequired(true);
		nombreAuditor.add(StringValidator.maximumLength(255));
		formAuditor.add(nombreAuditor);

		final EmailTextField emailAuditor = new EmailTextField("email", new PropertyModel<>(auditor, "mail"));
		emailAuditor.setRequired(true);
		emailAuditor.add(StringValidator.maximumLength(40));
		formAuditor.add(emailAuditor);

		formAuditor.add(new CheckBox("checkComisionado", new PropertyModel<>(auditor, "comisionado")));

		formAuditor.add(new Button("editar") {

			private static final long serialVersionUID = 5845761991376797119L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				if (!(email.equalsIgnoreCase(auditor.getMail())) || !(nombre.equalsIgnoreCase(auditor.getName()))) {
					AppContext.getInstance().getManagerBeanRemote().editarAuditor(getAuditor(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
					getSession().info(getString("auditorEditExito"));
				}
				setResponsePage(DashboardGestionAuditores.class, UtilsParameters.getId(auditor.getElection().getElectionId()));
			}

		});

		formAuditor.add(new Link<Void>("cancelarEditar") {

			private static final long serialVersionUID = -8699657879088465106L;

			@Override
			public void onClick() {
				setResponsePage(DashboardGestionAuditores.class, UtilsParameters.getId(auditor.getElection().getElectionId()));
			}

		});
	}

	public Auditor getAuditor() {
		return auditor;
	}

	public void setAuditor(Auditor auditor) {
		this.auditor = auditor;
	}

}