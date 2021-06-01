package net.lacnic.elections.admin.web.commons;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;

import net.lacnic.elections.utils.LanguageUtils;

public class DropDownIdioma extends DropDownChoice<String> {

	private static final long serialVersionUID = 4766509011662393037L;

	public DropDownIdioma(IModel<String> model) {
		super("idioma");
		setChoices(new LanguageUtils().getIdsList());
		setModel(model);
		setRequired(true);
		
		this.add(new FormComponentUpdatingBehavior() {

			private static final long serialVersionUID = -61527320105022401L;

			@Override
		    protected void onUpdate() {
		        // do something, page will be rerendered;
		    }
		 
		});
	}

	@Override
	protected String getNullKeyDisplayValue() {
		return "Idioma";
	}

}
