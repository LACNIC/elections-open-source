package net.lacnic.elections.adminweb.wicket.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.lacnic.elections.utils.CountryUtils;

public final class RestrictedCountriesFormatter {

	private RestrictedCountriesFormatter() {
	}

	public static String format(List<String> restrictedCountryCodes) {
		return format(restrictedCountryCodes, Locale.ENGLISH);
	}

	public static String format(List<String> restrictedCountryCodes, Locale locale) {
		if (restrictedCountryCodes == null || restrictedCountryCodes.isEmpty()) {
			return "";
		}

		CountryUtils countryUtils = new CountryUtils();
		Set<String> countryLabels = new LinkedHashSet<>();
		for (String code : restrictedCountryCodes) {
			String normalizedCode = countryUtils.normalizeCountryCode(code);
			if (normalizedCode == null) {
				continue;
			}

			String countryName = countryUtils.getNameById(normalizedCode, locale);
			if (countryName == null || countryName.trim().isEmpty() || normalizedCode.equalsIgnoreCase(countryName.trim())) {
				countryLabels.add(normalizedCode);
			} else {
				String trimmedCountryName = countryName.trim();
				String normalizedCountryName = trimmedCountryName.toUpperCase(Locale.ROOT);
				String codeSuffix = "(" + normalizedCode.toUpperCase(Locale.ROOT) + ")";
				if (normalizedCountryName.endsWith(codeSuffix)) {
					countryLabels.add(trimmedCountryName);
				} else {
					countryLabels.add(trimmedCountryName + " (" + normalizedCode + ")");
				}
			}
		}

		StringBuilder displayText = new StringBuilder();
		boolean first = true;
		for (String countryLabel : countryLabels) {
			if (!first) {
				displayText.append(", ");
			}
			displayText.append(countryLabel);
			first = false;
		}

		return displayText.toString();
	}
}
