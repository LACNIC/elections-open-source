package net.lacnic.elections.adminweb.ui.admin.parameter;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Parameter;


public class EditParameterDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -2448828548697597193L;

	private String value;


	public EditParameterDashboard(PageParameters params) {
		super(params);

		Parameter parameter = AppContext.getInstance().getManagerBeanRemote().getParameter(UtilsParameters.getClaveId(params));
		value = parameter.getValue();

		Form<Void> form = new Form<>("parameterForm");
		add(form);
		add(new FeedbackPanel("feedback"));

		final TextField<String> keyTextField = new TextField<>("key", new PropertyModel<>(parameter, "key"));
		keyTextField.setEnabled(false);
		form.add(keyTextField);

		final TextField<String> valueTextField = new TextField<>("value", new PropertyModel<>(parameter, "value"));
		form.add(valueTextField);

		form.add(new Link<Void>("cancel") {
			private static final long serialVersionUID = 3972998899429185663L;

			@Override
			public void onClick() {
				setResponsePage(ParametersDashboard.class);
			}
		});

		form.add(new Button("save") {
			private static final long serialVersionUID = 2088009878385305837L;

			@Override
			public void onSubmit() {
				if (!(value.equalsIgnoreCase(parameter.getValue()))) {
					AppContext.getInstance().getManagerBeanRemote().editParameter(parameter, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					getSession().info(getString("advEditParamExito"));
				}
				setResponsePage(ParametersDashboard.class);
			}
		});

	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
