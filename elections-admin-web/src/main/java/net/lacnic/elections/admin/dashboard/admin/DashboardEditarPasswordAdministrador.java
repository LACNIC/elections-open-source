package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.error.Error401;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.commons.UtilsString;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.UserAdmin;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardEditarPasswordAdministrador extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;
	private String password;
	private String password2;
	private String pass;

	public DashboardEditarPasswordAdministrador(PageParameters params) {
		super(params);
		if (SecurityUtils.getIdEleccionAutorizado() !=0 && !UtilsParameters.getAdminId(params).equalsIgnoreCase(SecurityUtils.getAdminId()))
			setResponsePage(Error401.class);
		UserAdmin admin = AppContext.getInstance().getManagerBeanRemote().obtenerUsuarioAdmin(UtilsParameters.getAdminId(params));
		pass = admin.getPassword();
		add(new FeedbackPanel("feedback"));
		Form<Void> formAdmin = new Form<>("formAdmin");
		add(formAdmin);

		final PasswordTextField claveTextField = new PasswordTextField("password", new PropertyModel<>(this, "password"));
		claveTextField.setRequired(true);
		claveTextField.add(StringValidator.maximumLength(20));
		claveTextField.setType(String.class);
		formAdmin.add(claveTextField);

		final PasswordTextField claveTextField2 = new PasswordTextField("password2", new PropertyModel<>(this, "password2"));
		claveTextField2.setRequired(true);
		claveTextField2.add(StringValidator.maximumLength(20));
		claveTextField2.setType(String.class);
		formAdmin.add(claveTextField2);

		formAdmin.add(new Button("editar") {

			private static final long serialVersionUID = 1847560105124145049L;

			@Override
			public void onSubmit() {
				super.onSubmit();

				if (getPassword().equals(getPassword2())) {
					if (!(UtilsString.wantHashMd5(getPassword()).equalsIgnoreCase(pass))) {
						AppContext.getInstance().getManagerBeanRemote().editarPassAdmin(admin.getUserAdminId(), UtilsString.wantHashMd5(getPassword()), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
						getSession().info(getString("adminUserEditExito"));
					}
					if(admin.getIdElectionAuthorized()==0)
						setResponsePage(DashboardAdministradores.class);
					else
						setResponsePage(DashboardHomePage.class);
				} else {
					getSession().error(getString("adminUserEditError"));					
					setResponsePage(DashboardEditarPasswordAdministrador.this);
				}
			}

		});

		formAdmin.add(new Link<Void>("cancelarEditar") {

			private static final long serialVersionUID = 6196400777384359018L;

			@Override
			public void onClick() {
				if(admin.getIdElectionAuthorized()==0)
					setResponsePage(DashboardAdministradores.class);
				else
					setResponsePage(DashboardHomePage.class);
			}

		});
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword2() {
		return password2;
	}

	public void setPassword2(String password2) {
		this.password2 = password2;
	}

}