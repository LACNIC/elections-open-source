package net.lacnic.siselecciones.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.dashboard.error.Error401;
import net.lacnic.siselecciones.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.siselecciones.admin.web.commons.DropDownEleccion;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.dominio.UsuarioAdmin;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardEditarAdministrador extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;

	private String email;
	private Eleccion eleccionAutorizado;

	public DashboardEditarAdministrador(PageParameters params) {
		super(params);
		if (SecurityUtils.getIdEleccionAutorizado() != 0 && !UtilsParameters.getAdminId(params).equalsIgnoreCase(SecurityUtils.getAdminId()))
			setResponsePage(Error401.class);
		UsuarioAdmin admin = Contexto.getInstance().getManagerBeanRemote().obtenerUsuarioAdmin(UtilsParameters.getAdminId(params));

		email = admin.getEmail();
		eleccionAutorizado = new Eleccion(admin.getIdEleccionAutorizado());
		if (admin.getIdEleccionAutorizado() != 0)
			eleccionAutorizado.setTituloEspanol(Contexto.getInstance().getManagerBeanRemote().obtenerEleccion(admin.getIdEleccionAutorizado()).getTituloEspanol());

		Form<Void> formAdmin = new Form<>("formAdmin");
		add(formAdmin);
		add(new FeedbackPanel("feedback"));

		formAdmin.add(new TextField<String>("username", new PropertyModel<>(admin, "userId")).setEnabled(false));
		final EmailTextField emailTextField = new EmailTextField("email", new PropertyModel<>(admin, "email"));
		emailTextField.setRequired(true);
		emailTextField.add(StringValidator.maximumLength(40));
		formAdmin.add(emailTextField);
		DropDownEleccion dropDownElecciones = new DropDownEleccion("eleccionAutorizada", new PropertyModel<>(DashboardEditarAdministrador.this, "eleccionAutorizado"));
		formAdmin.add(dropDownElecciones);

		formAdmin.add(new Button("editar") {

			private static final long serialVersionUID = -3530314990745212166L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				if (!(email.equalsIgnoreCase(admin.getEmail()) && getEleccionAutorizado().getIdEleccion() == admin.getIdEleccionAutorizado().longValue())) {
					Contexto.getInstance().getManagerBeanRemote().editarUsuarioAdmin(admin, email, getEleccionAutorizado().getIdEleccion(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
					getSession().info(getString("adminUserEditUsrExito"));
				}
				setResponsePage(DashboardAdministradores.class);
			}

		});

		formAdmin.add(new Link<Void>("cancelarEditar") {

			private static final long serialVersionUID = -8589438527976080382L;

			@Override
			public void onClick() {
				setResponsePage(DashboardAdministradores.class);
			}

		});
	}

	public Eleccion getEleccionAutorizado() {
		return eleccionAutorizado;
	}

	public void setEleccionAutorizado(Eleccion eleccionAutorizado) {
		this.eleccionAutorizado = eleccionAutorizado;
	}

}