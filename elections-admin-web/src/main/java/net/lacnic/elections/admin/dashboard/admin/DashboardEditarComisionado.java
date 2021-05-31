package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.markup.html.form.Button;
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
import net.lacnic.elections.domain.Commissioner;

public class DashboardEditarComisionado extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;
	private String nombre;
	private String email;

	public DashboardEditarComisionado(PageParameters params) {
		super(params);

		Commissioner comisionado = AppContext.getInstance().getManagerBeanRemote().obtenerComisionado(UtilsParameters.getAuditAsLong(params));
		email = comisionado.getMail();
		nombre = comisionado.getName();

		Form<Void> formComisionado = new Form<>("formComisionado");
		add(formComisionado);
		add(new FeedbackPanel("feedback"));

		TextField<String> nombreText = new TextField<>("nombre", new PropertyModel<>(comisionado, "nombre"));
		nombreText.setRequired(true);
		nombreText.add(StringValidator.maximumLength(50));
		formComisionado.add(nombreText);

		final EmailTextField emailTextField = new EmailTextField("email", new PropertyModel<>(comisionado, "mail"));
		emailTextField.setRequired(true);
		emailTextField.add(StringValidator.maximumLength(50));
		formComisionado.add(emailTextField);

		formComisionado.add(new Button("editar") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6611681748469691425L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				if (!(email.equalsIgnoreCase(comisionado.getMail())) || !(nombre.equalsIgnoreCase(comisionado.getName()))) {
					AppContext.getInstance().getManagerBeanRemote().editarComisionado(comisionado, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
					SecurityUtils.info(getString("commissionerEditExito"));
				}
				setResponsePage(DashboardComisionados.class);
			}

		});

		formComisionado.add(new Link<Void>("cancelarEditar") {

			private static final long serialVersionUID = -541452708581823315L;

			@Override
			public void onClick() {
				setResponsePage(DashboardComisionados.class);
			}

		});
	}

}