package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Parametro;

public class DashboardEditarParametrosPage extends DashboardAdminBasePage {

	private static final long serialVersionUID = -2448828548697597193L;
	private String valor;

	public DashboardEditarParametrosPage(PageParameters params) {
		super(params);
		Parametro p = AppContext.getInstance().getManagerBeanRemote().getParametro(UtilsParameters.getClaveId(params));
		valor = p.getValor();

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
				if (!(valor.equalsIgnoreCase(p.getValor()))) {
					AppContext.getInstance().getManagerBeanRemote().editarParametro(p, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
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