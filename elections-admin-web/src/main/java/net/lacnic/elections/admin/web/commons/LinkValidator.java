package net.lacnic.elections.admin.web.commons;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class LinkValidator implements IValidator<String> {

	private static final long serialVersionUID = 211536707226327476L;

	public LinkValidator() { }

	@Override
	public void validate(IValidatable<String> validatable) {
		String bio = validatable.getValue();
		boolean esLink = bio.contains("<a>") ||bio.contains("<a ");
		if (esLink)
			validatable.error(new ValidationError("La biografia no puede contener links, esto afectaria el comportamiento de la pantalla de votación. Pegue el texto sin formato o utilice el campo Web información para agregar un enlace"));
	}

}
