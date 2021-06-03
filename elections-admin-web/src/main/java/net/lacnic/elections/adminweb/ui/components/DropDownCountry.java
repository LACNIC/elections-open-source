package net.lacnic.elections.adminweb.ui.components;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import net.lacnic.elections.utils.CountryUtils;


public class DropDownCountry extends DropDownChoice<String> {

	private static final long serialVersionUID = 4766509011662393037L;


	public DropDownCountry(IModel<String> model) {
		super("country");
		setChoices(new CountryUtils().getIdsList());
		setModel(model);
		setNullValid(true);
	}

}
