package net.lacnic.elections.admin.web.panel.avanzadas;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.admin.DashboardParametros;

public class AddParametrosPanel extends Panel {

	private static final long serialVersionUID = -6525820489640825347L;
	private String clave;
	private String valor;

	public AddParametrosPanel(String id) {
		super(id);

		Form<Void> form = new Form<>("forParametro");
		add(form);
		final TextField<String> claveTextField = new TextField<>("claveP", new PropertyModel<>(AddParametrosPanel.this, "clave"));
		claveTextField.setRequired(true);
		form.add(claveTextField);

		final TextField<String> valorTextField = new TextField<>("valorP", new PropertyModel<>(AddParametrosPanel.this, "valor"));
		valorTextField.setRequired(true);
		form.add(valorTextField);

		form.add(new Button("agregar") {

			private static final long serialVersionUID = -5016929436027874663L;

			@Override
			public void onSubmit() {
				boolean agregarP = AppContext.getInstance().getManagerBeanRemote().agregarParametro(getClave(), getValor(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
				if (agregarP) {
					SecurityUtils.info(getString("advAddParamExito"));
					setResponsePage(DashboardParametros.class);
				} else {
					SecurityUtils.error(getString("advAddParamError"));
				}
			}
		});
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}
}