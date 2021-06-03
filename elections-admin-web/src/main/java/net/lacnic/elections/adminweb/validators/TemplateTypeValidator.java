package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.ElectionEmailTemplate;


public class TemplateTypeValidator implements IValidator<String> {

	private static final long serialVersionUID = 9018437443482309026L;


	public TemplateTypeValidator() { }


	@Override
	public void validate(IValidatable<String> templateToValidate) {
		String templateType = templateToValidate.getValue();
		ElectionEmailTemplate electionEmailTemplate = AppContext.getInstance().getManagerBeanRemote().getEmailTemplate(templateType, 0L);
		if (electionEmailTemplate != null)
			templateToValidate.error(new ValidationError("Base template type already exists"));
	}

}
