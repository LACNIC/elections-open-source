package net.lacnic.elections.admin.web.commons;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import net.lacnic.elections.utils.UtilsPaises;

public class DropDownPais extends DropDownChoice<String> {

	private static final long serialVersionUID = 4766509011662393037L;

	public DropDownPais(IModel<String> model) {
		super("pais");
		setChoices(new UtilsPaises().getIdPaises());
		setModel(model);
		setNullValid(true);
	}

}
