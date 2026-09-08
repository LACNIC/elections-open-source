package net.lacnic.elections.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public enum LanguageCode {
	SP("SP", "es", List.of("sp", "es", "spanish", "espanol", "español")),
	EN("EN", "en", List.of("en", "english")),
	PT("PT", "pt", List.of("pt", "portuguese", "portugues", "português"));

	private final String code;
	private final String localeCode;
	private final List<String> aliases;

	LanguageCode(String code, String localeCode, List<String> aliases) {
		this.code = code;
		this.localeCode = localeCode;
		this.aliases = aliases;
	}

	public String getCode() {
		return code;
	}

	public String getLocaleCode() {
		return localeCode;
	}

	public Locale toLocale() {
		return new Locale(localeCode);
	}

	public static LanguageCode fromValue(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim().replace('_', '-');
		if (normalized.isEmpty()) {
			return null;
		}

		String lower = normalized.toLowerCase(Locale.ROOT);
		for (LanguageCode language : values()) {
			if (language.matches(lower)) {
				return language;
			}
		}
		return null;
	}

	public static LanguageCode fromValueOrDefault(String value, LanguageCode defaultLanguage) {
		LanguageCode resolved = fromValue(value);
		return resolved != null ? resolved : defaultLanguage;
	}

	public static LanguageCode fromLocale(Locale locale) {
		if (locale == null) {
			return null;
		}
		return fromValue(locale.getLanguage());
	}

	public static List<String> canonicalCodes() {
		return Arrays.stream(values()).map(LanguageCode::getCode).collect(Collectors.toList());
	}

	private boolean matches(String normalizedLowerValue) {
		if (normalizedLowerValue == null) {
			return false;
		}
		if (aliases.contains(normalizedLowerValue)) {
			return true;
		}
		String codeLower = code.toLowerCase(Locale.ROOT);
		if (normalizedLowerValue.startsWith(localeCode + "-") || normalizedLowerValue.startsWith(codeLower + "-")) {
			return true;
		}
		return aliases.stream()
				.filter(Objects::nonNull)
				.filter(alias -> alias.length() > 2)
				.anyMatch(normalizedLowerValue::contains);
	}
}
