package net.lacnic.elections.adminweb.ui.admin.useradmin;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.adminweb.ui.bases.DashboardManagerBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;


public class EditUserAdminDashboard extends DashboardManagerBasePage {

	private static final long serialVersionUID = -4584362258132685785L;

	private String email;

	public EditUserAdminDashboard(PageParameters params) {
		super(params);
		if (!SecurityUtils.isLocalAuthentication()) {
			SecurityUtils.error(getString("userAdminLocalAccessDenied"));
			throw new RestartResponseException(SecurityUtils.getHomePage());
		}
		UserAdmin userAdmin = AppContext.getInstance().getManagerBeanRemote().getUserAdmin(UtilsParameters.getAdminId(params));

		email = userAdmin.getEmail();

		Form<Void> formUserAdmin = new Form<>("formUserAdmin");
		add(formUserAdmin);
		add(new FeedbackPanel("feedback"));

		TextField<String> nameTextField = new TextField<>("userAdminId", new PropertyModel<>(userAdmin, "userAdminId"));
		nameTextField.setEnabled(false);
		formUserAdmin.add(nameTextField);

		final EmailTextField emailTextField = new EmailTextField("email", new PropertyModel<>(userAdmin, "email"));
		emailTextField.setRequired(true);
		emailTextField.add(StringValidator.maximumLength(40));
		formUserAdmin.add(emailTextField);

		formUserAdmin.add(new Button("edit") {
			private static final long serialVersionUID = -3530314990745212166L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				if (!email.equalsIgnoreCase(userAdmin.getEmail())) {
					AppContext.getInstance().getManagerBeanRemote().editUserAdmin(userAdmin, email, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					getSession().info(getString("adminUserEditUsrSuccess"));
				}
				setResponsePage(UserAdminsDashboard.class);
			}
		});

		formUserAdmin.add(new Link<Void>("cancelEdit") {
			private static final long serialVersionUID = -8589438527976080382L;

			@Override
			public void onClick() {
				setResponsePage(UserAdminsDashboard.class);
			}
		});
	}

}
