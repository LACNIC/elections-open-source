package net.lacnic.elections.adminweb.ui.admin.commissioner;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.validators.AuditorValidator;
import net.lacnic.elections.domain.Commissioner;


public class AddCommissionerPanel extends Panel {

	private static final long serialVersionUID = -4400633632996398779L;
	private Commissioner commissioner;


	public AddCommissionerPanel(String id) {
		super(id);
		commissioner = new Commissioner();

		Form<Void> formCommissioner = new Form<>("formCommissioner");
		add(formCommissioner);

		final TextField<String> nameTextField = new TextField<>("name", new PropertyModel<>(commissioner, "name"));
		nameTextField.setRequired(true);
		nameTextField.add(StringValidator.maximumLength(40));
		nameTextField.setType(String.class);
		formCommissioner.add(nameTextField);

		final EmailTextField emailTextField = new EmailTextField("mail", new PropertyModel<>(commissioner, "mail"));
		emailTextField.setRequired(true);
		emailTextField.add(StringValidator.maximumLength(60));
		formCommissioner.add(emailTextField);

		formCommissioner.add(new AuditorValidator(nameTextField, emailTextField));

		formCommissioner.add(new Button("add") {
			private static final long serialVersionUID = 752002053034917334L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				boolean result = AppContext.getInstance().getManagerBeanRemote().addCommissioner(getCommissioner().getName(), getCommissioner().getMail(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
				if (result) {
					SecurityUtils.info(getString("commissionerAddSuccessAdd"));
					setResponsePage(CommissionersDashboard.class);
				} else
					getSession().error(getString("commissionerAddErrorAdd"));
			}
		});

	}


	public Commissioner getCommissioner() {
		return commissioner;
	}

	public void setCommissioner(Commissioner commissioner) {
		this.commissioner = commissioner;
	}

}
