package net.lacnic.elections.admin.web.panel.admin;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.admin.DashboardAdministradores;
import net.lacnic.elections.admin.web.commons.DropDownEleccion;
import net.lacnic.elections.admin.web.commons.UtilsString;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserAdmin;

public class AgregarAdminPanel extends Panel {

	private static final long serialVersionUID = -4400633632996398779L;
	private UserAdmin admin;
	private String password = "";
	private Election eleccionAutorizado;

	public AgregarAdminPanel(String id) {
		super(id);
		admin = new UserAdmin();

		Form<Void> formAdmin = new Form<>("formAdmin");
		add(formAdmin);

		eleccionAutorizado = new Election(0);

		final TextField<String> usernameTextField = new TextField<>("username", new PropertyModel<>(admin, "userId"));
		usernameTextField.setRequired(true);
		usernameTextField.add(StringValidator.maximumLength(40));
		usernameTextField.setType(String.class);
		formAdmin.add(usernameTextField);

		final EmailTextField emailTextField = new EmailTextField("email", new PropertyModel<>(admin, "email"));
		emailTextField.setRequired(true);
		emailTextField.add(StringValidator.maximumLength(40));
		formAdmin.add(emailTextField);

		final PasswordTextField claveTextField = new PasswordTextField("password", new PropertyModel<>(this, "password"));
		claveTextField.setRequired(true);
		claveTextField.add(StringValidator.maximumLength(40));
		claveTextField.setType(String.class);
		formAdmin.add(claveTextField);

		DropDownEleccion dropDownElecciones = new DropDownEleccion("eleccionAutorizada", new PropertyModel<>(AgregarAdminPanel.this, "eleccionAutorizado"));
		formAdmin.add(dropDownElecciones);

		formAdmin.add(new Button("agregar") {

			private static final long serialVersionUID = 6181993609698314612L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				admin.setIdElectionAuthorized(getEleccionAutorizado().getIdElection());
				admin.setPassword(UtilsString.wantHashMd5(getPassword()));

				boolean adminUser = AppContext.getInstance().getManagerBeanRemote().agregarUsuarioAdmin(admin, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
				if (adminUser) {
					SecurityUtils.info(getString("adminUserAddExitoAdd"));
					setResponsePage(DashboardAdministradores.class);
				} else
					getSession().error(getString("adminUserAddErrorAdd"));
			}

		});

	}

	public UserAdmin getAdmin() {
		return admin;
	}

	public void setAdmin(UserAdmin admin) {
		this.admin = admin;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Election getEleccionAutorizado() {
		return eleccionAutorizado;
	}

	public void setEleccionAutorizado(Election eleccionAutorizado) {
		this.eleccionAutorizado = eleccionAutorizado;
	}

}
