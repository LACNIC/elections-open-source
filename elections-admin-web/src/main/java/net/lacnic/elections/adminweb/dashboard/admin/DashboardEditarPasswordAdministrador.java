package net.lacnic.elections.adminweb.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.useradmin.UserAdminsDashboard;
import net.lacnic.elections.adminweb.ui.error.Error401;
import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.web.commons.UtilsString;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.UserAdmin;

@AuthorizeInstantiation("elections-only-one")
public class DashboardEditarPasswordAdministrador extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;
	private String password;
	private String password2;
	private String pass;

	public DashboardEditarPasswordAdministrador(PageParameters params) {
		super(params);
		if (SecurityUtils.getAuthorizedElectionId() !=0 && !UtilsParameters.getAdminId(params).equalsIgnoreCase(SecurityUtils.getUserAdminId()))
			setResponsePage(Error401.class);
		UserAdmin admin = AppContext.getInstance().getManagerBeanRemote().getUserAdmin(UtilsParameters.getAdminId(params));
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
						AppContext.getInstance().getManagerBeanRemote().editAdminUserPassword(admin.getUserAdminId(), UtilsString.wantHashMd5(getPassword()), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						getSession().info(getString("adminUserEditExito"));
					}
					if(admin.getAuthorizedElectionId()==0)
						setResponsePage(UserAdminsDashboard.class);
					else
						setResponsePage(ElectionsDashboard.class);
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
				if(admin.getAuthorizedElectionId()==0)
					setResponsePage(UserAdminsDashboard.class);
				else
					setResponsePage(ElectionsDashboard.class);
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