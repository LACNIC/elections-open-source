package net.lacnic.elections.adminweb.ui.admin.useradmin;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.DropDownElection;
import net.lacnic.elections.adminweb.web.commons.UtilsString;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserAdmin;


public class AddUserAdminPanel extends Panel {

	private static final long serialVersionUID = -4400633632996398779L;
	private UserAdmin userAdmin;
	private String password = "";
	private Election authorizedElection;

	public AddUserAdminPanel(String id) {
		super(id);
		userAdmin = new UserAdmin();

		Form<Void> formUserAdmin = new Form<>("formUserAdmin");
		add(formUserAdmin);

		authorizedElection = new Election(0);

		final TextField<String> usernameTextField = new TextField<>("userAdminId", new PropertyModel<>(userAdmin, "userAdminId"));
		usernameTextField.setRequired(true);
		usernameTextField.add(StringValidator.maximumLength(40));
		usernameTextField.setType(String.class);
		formUserAdmin.add(usernameTextField);

		final EmailTextField emailTextField = new EmailTextField("email", new PropertyModel<>(userAdmin, "email"));
		emailTextField.setRequired(true);
		emailTextField.add(StringValidator.maximumLength(40));
		formUserAdmin.add(emailTextField);

		final PasswordTextField claveTextField = new PasswordTextField("password", new PropertyModel<>(this, "password"));
		claveTextField.setRequired(true);
		claveTextField.add(StringValidator.maximumLength(40));
		claveTextField.setType(String.class);
		formUserAdmin.add(claveTextField);

		DropDownElection dropDownElecciones = new DropDownElection("authorizedElection", new PropertyModel<>(AddUserAdminPanel.this, "authorizedElection"));
		formUserAdmin.add(dropDownElecciones);

		formUserAdmin.add(new Button("add") {

			private static final long serialVersionUID = 6181993609698314612L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				userAdmin.setAuthorizedElectionId(getAuthorizedElection().getElectionId());
				userAdmin.setPassword(UtilsString.wantHashMd5(getPassword()));

				boolean result = AppContext.getInstance().getManagerBeanRemote().addUserAdmin(userAdmin, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
				if (result) {
					SecurityUtils.info(getString("adminUserAddSuccessAdd"));
					setResponsePage(UserAdminsDashboard.class);
				} else
					getSession().error(getString("adminUserAddErrorAdd"));
			}

		});

	}

	public UserAdmin getUserAdmin() {
		return userAdmin;
	}

	public void setUserAdmin(UserAdmin userAdmin) {
		this.userAdmin = userAdmin;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Election getAuthorizedElection() {
		return authorizedElection;
	}

	public void setAuthorizedElection(Election authorizedElection) {
		this.authorizedElection = authorizedElection;
	}

}
