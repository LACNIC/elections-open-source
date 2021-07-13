package net.lacnic.elections.adminweb.ui.admin.useradmin;

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

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.ui.components.DropDownElection;
import net.lacnic.elections.adminweb.ui.error.Error401;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserAdmin;


@AuthorizeInstantiation("elections-only-one")
public class EditUserAdminDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;

	private String email;
	private Election authorizedElection;

	public EditUserAdminDashboard(PageParameters params) {
		super(params);
		if (SecurityUtils.getAuthorizedElectionId() != 0 && !UtilsParameters.getAdminId(params).equalsIgnoreCase(SecurityUtils.getUserAdminId()))
			setResponsePage(Error401.class);
		UserAdmin userAdmin = AppContext.getInstance().getManagerBeanRemote().getUserAdmin(UtilsParameters.getAdminId(params));

		email = userAdmin.getEmail();
		authorizedElection = new Election(userAdmin.getAuthorizedElectionId());
		if (userAdmin.getAuthorizedElectionId() != 0)
			authorizedElection.setTitleSpanish(AppContext.getInstance().getManagerBeanRemote().getElection(userAdmin.getAuthorizedElectionId()).getTitleSpanish());

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

		DropDownElection dropDownElecciones = new DropDownElection("authorizedElection", new PropertyModel<>(EditUserAdminDashboard.this, "authorizedElection"));
		formUserAdmin.add(dropDownElecciones);

		formUserAdmin.add(new Button("edit") {
			private static final long serialVersionUID = -3530314990745212166L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				if (!(email.equalsIgnoreCase(userAdmin.getEmail()) && getAuthorizedElection().getElectionId() == userAdmin.getAuthorizedElectionId().longValue())) {
					AppContext.getInstance().getManagerBeanRemote().editUserAdmin(userAdmin, email, getAuthorizedElection().getElectionId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
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

	public Election getAuthorizedElection() {
		return authorizedElection;
	}

	public void setAuthorizedElection(Election authorizedElection) {
		this.authorizedElection = authorizedElection;
	}

}
