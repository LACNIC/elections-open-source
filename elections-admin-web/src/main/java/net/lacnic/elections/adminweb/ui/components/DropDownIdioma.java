package net.lacnic.elections.adminweb.ui.components;

import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import net.lacnic.elections.domain.LanguageCode;

public class DropDownIdioma extends DropDownChoice<LanguageCode> {

	private static final long serialVersionUID = 7324633731156784194L;

	public DropDownIdioma(String id, IModel<LanguageCode> model) {
		super(id, model, Arrays.asList(LanguageCode.values()));
		setChoiceRenderer(new ChoiceRenderer<LanguageCode>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(LanguageCode language) {
				if (language == null) {
					return "";
				}
				Locale locale = Session.exists() ? Session.get().getLocale() : null;
				return DropDownIdioma.this.getString("organizationsManagementLanguageOption" + language.name(), null, resolveLanguageLabel(language, locale));
			}

			@Override
			public String getIdValue(LanguageCode language, int index) {
				return language == null ? null : language.getCode();
			}
		});
		setNullValid(true);
		setRequired(false);
	}

	private String resolveLanguageLabel(LanguageCode language, Locale locale) {
		String normalizedLanguage = locale == null ? "" : locale.getLanguage();
		switch (language) {
		case SP:
			return "pt".equalsIgnoreCase(normalizedLanguage) ? "Espanhol" : ("en".equalsIgnoreCase(normalizedLanguage) ? "Spanish" : "Español");
		case EN:
			return "pt".equalsIgnoreCase(normalizedLanguage) ? "Inglês" : ("en".equalsIgnoreCase(normalizedLanguage) ? "English" : "Inglés");
		case PT:
			return "pt".equalsIgnoreCase(normalizedLanguage) ? "Português" : ("en".equalsIgnoreCase(normalizedLanguage) ? "Portuguese" : "Portugués");
		default:
			return language.name();
		}
	}
}
