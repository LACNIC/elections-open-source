package net.lacnic.siselecciones.admin.web.panel.admin;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardComisionados;
import net.lacnic.siselecciones.admin.web.commons.AuditorValidator;
import net.lacnic.siselecciones.dominio.Comisionado;

public class AgregarComisionadoPanel extends Panel {

	private static final long serialVersionUID = -4400633632996398779L;
	private Comisionado comisionado;

	public AgregarComisionadoPanel(String id) {
		super(id);
		comisionado = new Comisionado();

		Form<Void> formComisionado = new Form<>("formComisionado");
		add(formComisionado);

		final TextField<String> comisionadoText = new TextField<>("nombre", new PropertyModel<>(comisionado, "nombre"));
		comisionadoText.setRequired(true);
		comisionadoText.add(StringValidator.maximumLength(40));
		comisionadoText.setType(String.class);
		formComisionado.add(comisionadoText);

		final EmailTextField emailTextField = new EmailTextField("mail", new PropertyModel<>(comisionado, "mail"));
		emailTextField.setRequired(true);
		emailTextField.add(StringValidator.maximumLength(60));
		formComisionado.add(emailTextField);

		formComisionado.add(new AuditorValidator(comisionadoText, emailTextField));

		formComisionado.add(new Button("agregar") {

			private static final long serialVersionUID = 752002053034917334L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				boolean agregarC = Contexto.getInstance().getManagerBeanRemote().agregarComisionado(getComisionado().getNombre(), getComisionado().getMail(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
				if (agregarC) {
					SecurityUtils.info(getString("commissionerAddExitoAdd"));
					setResponsePage(DashboardComisionados.class);
				} else
					getSession().error(getString("commissionerAddErrorAdd"));
			}
		});

	}

	public Comisionado getComisionado() {
		return comisionado;
	}

	public void setComisionado(Comisionado comisionado) {
		this.comisionado = comisionado;
	}

}
