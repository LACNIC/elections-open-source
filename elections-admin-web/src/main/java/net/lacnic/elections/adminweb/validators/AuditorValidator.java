package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

import net.lacnic.elections.adminweb.app.AppContext;


public class AuditorValidator extends AbstractFormValidator {

	private static final long serialVersionUID = -5248942461722760103L;

	private final FormComponent[] components;
	private final long electionId;


	public AuditorValidator(FormComponent name, FormComponent mail) {
		this(0L, name, mail);
	}

	public AuditorValidator(long electionId, FormComponent name, FormComponent mail) {
		this.components = new FormComponent[] { name, mail };
		this.electionId = electionId;
	}


	public FormComponent[] getDependentFormComponents() {
		return components;
	}

	public void validate(Form form) {
		boolean alreadyExists = false;
		if (electionId == 0) {
			alreadyExists = AppContext.getInstance().getManagerBeanRemote().commissionerExists(components[0].getValue(), (components[1].getValue()));
			if (alreadyExists)
				components[0].error("Commissioner already exists.");
		} else {
			alreadyExists = AppContext.getInstance().getManagerBeanRemote().auditorExists(electionId, components[0].getValue(), (components[1].getValue()));
			if (alreadyExists)
				components[0].error("Auditor already exists for this election.");
		}
	}

}
