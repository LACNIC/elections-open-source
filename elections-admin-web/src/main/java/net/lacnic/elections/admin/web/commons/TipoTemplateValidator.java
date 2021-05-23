package net.lacnic.elections.admin.web.commons;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.domain.TemplateEleccion;

public class TipoTemplateValidator implements IValidator<String> {

	private static final long serialVersionUID = 9018437443482309026L;

	public TipoTemplateValidator() { }

	@Override
	public void validate(IValidatable<String> validatable) {
		String tipo = validatable.getValue();
		TemplateEleccion templateEleccion = AppContext.getInstance().getManagerBeanRemote().obtenerTemplate(tipo, 0L);
		if (templateEleccion != null)
			validatable.error(new ValidationError("Ya existe este tipo de template base"));

	}
}