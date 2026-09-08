package net.lacnic.elections.adminweb.ui.components;

import java.util.Arrays;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import net.lacnic.elections.domain.LanguageCode;


public class DropDownLanguage extends DropDownChoice<LanguageCode> {

	private static final long serialVersionUID = 4766509011662393037L;


	public DropDownLanguage(IModel<LanguageCode> model) {
		super("language");
		setChoices(Arrays.asList(LanguageCode.values()));
		setModel(model);
		setRequired(true);
		setChoiceRenderer(new ChoiceRenderer<LanguageCode>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(LanguageCode language) {
				if (language == null) {
					return "";
				}
				switch (language) {
				case EN:
					return "English";
				case PT:
					return "Português";
				case SP:
				default:
					return "Español";
				}
			}

			@Override
			public String getIdValue(LanguageCode language, int index) {
				return language == null ? null : language.getCode();
			}
		});
	}

	@Override
	protected String getNullKeyDisplayValue() {
		return "Language";
	}

}
