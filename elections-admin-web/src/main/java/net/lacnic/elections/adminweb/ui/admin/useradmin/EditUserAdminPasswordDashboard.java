package net.lacnic.elections.adminweb.ui.admin.useradmin;

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
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.ui.error.Error401;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.adminweb.wicket.util.UtilsString;
import net.lacnic.elections.domain.UserAdmin;


@AuthorizeInstantiation("elections-only-one")
public class EditUserAdminPasswordDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;

	private String newPassword1;
	private String newPassword2;
	private String currentPassword;


	public EditUserAdminPasswordDashboard(PageParameters params) {
		super(params);

		if (SecurityUtils.getAuthorizedElectionId() !=0 && !UtilsParameters.getAdminId(params).equalsIgnoreCase(SecurityUtils.getUserAdminId()))
			setResponsePage(Error401.class);

		UserAdmin userAdmin = AppContext.getInstance().getManagerBeanRemote().getUserAdmin(UtilsParameters.getAdminId(params));
		currentPassword = userAdmin.getPassword();
		add(new FeedbackPanel("feedback"));
		Form<Void> passwordForm = new Form<>("passwordForm");
		add(passwordForm);

		final PasswordTextField newPasswordField1 = new PasswordTextField("newPassword1", new PropertyModel<>(this, "newPassword1"));
		newPasswordField1.setRequired(true);
		newPasswordField1.add(StringValidator.maximumLength(20));
		newPasswordField1.setType(String.class);
		passwordForm.add(newPasswordField1);

		final PasswordTextField newPasswordField2 = new PasswordTextField("newPassword2", new PropertyModel<>(this, "newPassword2"));
		newPasswordField2.setRequired(true);
		newPasswordField2.add(StringValidator.maximumLength(20));
		newPasswordField2.setType(String.class);
		passwordForm.add(newPasswordField2);

		passwordForm.add(new Button("save") {
			private static final long serialVersionUID = 1847560105124145049L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				if (getNewPassword1().equals(getNewPassword2())) {
					if (!(UtilsString.wantHashMd5(getNewPassword1()).equalsIgnoreCase(currentPassword))) {
						AppContext.getInstance().getManagerBeanRemote().editAdminUserPassword(userAdmin.getUserAdminId(), UtilsString.wantHashMd5(getNewPassword1()), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						getSession().info(getString("adminUserEditPwdSuccess"));
					}
					if(userAdmin.getAuthorizedElectionId()==0)
						setResponsePage(UserAdminsDashboard.class);
					else
						setResponsePage(ElectionsDashboard.class);
				} else {
					getSession().error(getString("adminUserEditError"));					
					setResponsePage(EditUserAdminPasswordDashboard.this);
				}
			}
		});

		passwordForm.add(new Link<Void>("cancelEdit") {
			private static final long serialVersionUID = 6196400777384359018L;

			@Override
			public void onClick() {
				if(userAdmin.getAuthorizedElectionId()==0)
					setResponsePage(UserAdminsDashboard.class);
				else
					setResponsePage(ElectionsDashboard.class);
			}
		});
	}


	public String getNewPassword1() {
		return newPassword1;
	}

	public void setNewPassword1(String newPassword1) {
		this.newPassword1 = newPassword1;
	}

	public String getNewPassword2() {
		return newPassword2;
	}

	public void setNewPassword2(String newPassword2) {
		this.newPassword2 = newPassword2;
	}

}
