package net.lacnic.elections.adminweb.ui.admin.commissioner;

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
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Commissioner;


public class EditCommissionerDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;
	private String name;
	private String mail;

	public EditCommissionerDashboard(PageParameters params) {
		super(params);

		Commissioner commissioner = AppContext.getInstance().getManagerBeanRemote().getCommissioner(UtilsParameters.getAuditAsLong(params));
		mail = commissioner.getMail();
		name = commissioner.getName();

		Form<Void> formCommissioner = new Form<>("formCommissioner");
		add(formCommissioner);
		add(new FeedbackPanel("feedback"));

		TextField<String> nameTextField = new TextField<>("name", new PropertyModel<>(commissioner, "name"));
		nameTextField.setRequired(true);
		nameTextField.add(StringValidator.maximumLength(50));
		formCommissioner.add(nameTextField);

		final EmailTextField emailTextField = new EmailTextField("mail", new PropertyModel<>(commissioner, "mail"));
		emailTextField.setRequired(true);
		emailTextField.add(StringValidator.maximumLength(50));
		formCommissioner.add(emailTextField);

		formCommissioner.add(new Button("edit") {
			private static final long serialVersionUID = 6611681748469691425L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				if (!(mail.equalsIgnoreCase(commissioner.getMail())) || !(name.equalsIgnoreCase(commissioner.getName()))) {
					AppContext.getInstance().getManagerBeanRemote().editCommissioner(commissioner, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					SecurityUtils.info(getString("commissionerEditSuccess"));
				}
				setResponsePage(CommissionersDashboard.class);
			}
		});

		formCommissioner.add(new Link<Void>("cancelEdit") {
			private static final long serialVersionUID = -541452708581823315L;

			@Override
			public void onClick() {
				setResponsePage(CommissionersDashboard.class);
			}
		});
	}

}