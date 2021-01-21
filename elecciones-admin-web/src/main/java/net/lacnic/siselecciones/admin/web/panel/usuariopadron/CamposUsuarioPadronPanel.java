package net.lacnic.siselecciones.admin.web.panel.usuariopadron;

import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.siselecciones.admin.web.commons.DropDownIdioma;
import net.lacnic.siselecciones.admin.web.commons.DropDownPais;
import net.lacnic.siselecciones.dominio.UsuarioPadron;

public class CamposUsuarioPadronPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	public CamposUsuarioPadronPanel(String id, UsuarioPadron up) {
		super(id);
		TextField<String> nombre = new TextField<>("nombre", new PropertyModel<>(up, "nombre"));
		nombre.setRequired(true);
		nombre.add(StringValidator.maximumLength(1000));
		add(nombre);

		EmailTextField emailTextField = new EmailTextField("email", new PropertyModel<>(up, "mail"));
		emailTextField.setRequired(true);
		emailTextField.add(StringValidator.maximumLength(250));
		add(emailTextField);

		TextField<Integer> cantVotos = new TextField<>("cantVotos", new PropertyModel<>(up, "cantVotos"));
		cantVotos.add(RangeValidator.range(1, 100));
		cantVotos.setRequired(true);
		add(cantVotos);

		TextField<String> orgId = new TextField<>("orgID", new PropertyModel<>(up, "orgID"));
		orgId.add(StringValidator.maximumLength(1000));
		add(orgId);

		add(new DropDownPais(new PropertyModel<>(up, "pais")));
		add(new DropDownIdioma(new PropertyModel<>(up, "idioma")));

	}

}