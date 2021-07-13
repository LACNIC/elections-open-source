package net.lacnic.elections.adminweb.ui.admin.parameter;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;


public class AddParameterPanel extends Panel {

	private static final long serialVersionUID = -6525820489640825347L;

	private String key;
	private String value;


	public AddParameterPanel(String id) {
		super(id);

		Form<Void> form = new Form<>("parameterForm");
		add(form);
		final TextField<String> keyTextField = new TextField<>("key", new PropertyModel<>(AddParameterPanel.this, "key"));
		keyTextField.setRequired(true);
		form.add(keyTextField);

		final TextField<String> valueTextField = new TextField<>("value", new PropertyModel<>(AddParameterPanel.this, "value"));
		valueTextField.setRequired(true);
		form.add(valueTextField);

		form.add(new Button("add") {
			private static final long serialVersionUID = -5016929436027874663L;

			@Override
			public void onSubmit() {
				boolean result = AppContext.getInstance().getManagerBeanRemote().addParameter(key, value, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
				if (result) {
					SecurityUtils.info(getString("advAddParamSuccess"));
					setResponsePage(ParametersDashboard.class);
				} else {
					SecurityUtils.error(getString("advAddParamError"));
				}
			}
		});
	}


	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
