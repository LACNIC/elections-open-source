package net.lacnic.elections.utils;

import java.util.ArrayList;
import java.util.List;


public class LanguageUtils {

	private String languagesChar = "SP:Español;PT:Português;EN:English";
	private List<Language> languagesList;
	private List<String> namesList;
	private List<String> idsList;

	public LanguageUtils() {
		this.namesList = new ArrayList<>();
		this.idsList = new ArrayList<>();
		this.languagesList = new ArrayList<>();

		String[] languagesArray = languagesChar.split(";");

		for (String language : languagesArray) {
			String[] languageSplit = language.split(":");
			this.namesList.add(languageSplit[1]);
			this.idsList.add(languageSplit[0]);
			Language newLanguage = new Language(languageSplit[0], languageSplit[1]);
			this.languagesList.add(newLanguage);
		}
	}

	public List<Language> getLanguagesList() {
		return languagesList;
	}

	public List<String> getNamesList() {
		return namesList;
	}

	public List<String> getIdsList() {
		return idsList;
	}

}
