package net.lacnic.elections.admin.web.commons;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

import net.lacnic.elections.admin.app.AppContext;

public class AuditorValidator extends AbstractFormValidator {

	private static final long serialVersionUID = -5248942461722760103L;

	private final FormComponent[] components;
	private final long idEleccion;

	public AuditorValidator(FormComponent nombre, FormComponent email) {
		this(0L, nombre, email);
	}

	public AuditorValidator(long idEleccion, FormComponent nombre, FormComponent email) {
		this.components = new FormComponent[] { nombre, email };
		this.idEleccion = idEleccion;
	}

	public FormComponent[] getDependentFormComponents() {
		return components;
	}

	public void validate(Form form) {
		boolean yaExiste = false;
		if (idEleccion == 0) {
			yaExiste = AppContext.getInstance().getManagerBeanRemote().commissionerExists(components[0].getValue(), (components[1].getValue()));
			if (yaExiste)
				components[0].error("Ya existe este comisionado");
		} else {
			yaExiste = AppContext.getInstance().getManagerBeanRemote().auditorExists(idEleccion, components[0].getValue(), (components[1].getValue()));
			if (yaExiste)
				components[0].error("Ya existe este auditor para esta elección");
		}
	}
}