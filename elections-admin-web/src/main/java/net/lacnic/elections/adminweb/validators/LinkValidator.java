package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;


public class LinkValidator implements IValidator<String> {

	private static final long serialVersionUID = 211536707226327476L;


	public LinkValidator() { }


	@Override
	public void validate(IValidatable<String> linkToValidate) {
		String bio = linkToValidate.getValue();
		boolean isLink = bio.contains("<a>") || bio.contains("<a ");
		if (isLink)
			linkToValidate.error(new ValidationError("Bio cannot contain links as it would affect de voting page behaviour. Paste the text without format or use the information field to add a link."));
	}

}
