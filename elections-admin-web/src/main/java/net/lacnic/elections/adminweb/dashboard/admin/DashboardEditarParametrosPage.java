package net.lacnic.elections.adminweb.dashboard.admin;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Parameter;

public class DashboardEditarParametrosPage extends DashboardAdminBasePage {

	private static final long serialVersionUID = -2448828548697597193L;
	private String valor;

	public DashboardEditarParametrosPage(PageParameters params) {
		super(params);
		Parameter p = AppContext.getInstance().getManagerBeanRemote().getParameter(UtilsParameters.getClaveId(params));
		valor = p.getValue();

		Form<Void> form = new Form<>("forParametro");
		add(form);
		add(new FeedbackPanel("feedback"));
		final TextField<String> claveTextField = new TextField<>("claveP", new PropertyModel<>(p, "clave"));
		claveTextField.setEnabled(false);
		form.add(claveTextField);

		final TextField<String> valorTextField = new TextField<>("valorP", new PropertyModel<>(p, "valor"));
		form.add(valorTextField);

		form.add(new Link<Void>("cancelarEdicionP") {

			private static final long serialVersionUID = 3972998899429185663L;

			@Override
			public void onClick() {
				setResponsePage(DashboardParametros.class);
			}

		});

		form.add(new Button("agregar") {

			private static final long serialVersionUID = 2088009878385305837L;

			@Override
			public void onSubmit() {
				if (!(valor.equalsIgnoreCase(p.getValue()))) {
					AppContext.getInstance().getManagerBeanRemote().editParameter(p, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					getSession().info(getString("advEditParamExito"));
				}
				setResponsePage(DashboardParametros.class);
			}
		});

	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

}