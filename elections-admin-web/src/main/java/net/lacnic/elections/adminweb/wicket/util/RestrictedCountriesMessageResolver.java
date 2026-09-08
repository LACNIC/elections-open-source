package net.lacnic.elections.adminweb.wicket.util;

import java.text.MessageFormat;
import java.util.List;

import org.apache.wicket.Component;

import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionType;

public final class RestrictedCountriesMessageResolver {

	private static final String RESTRICTED_INFO_KEY_PREFIX = "publicElectionCandidateCallRestrictedInfo.";
	private static final String DEFAULT_RESTRICTED_INFO_KEY = RESTRICTED_INFO_KEY_PREFIX + "default";

	private RestrictedCountriesMessageResolver() {
	}

	public static String resolve(Component component, Election election) {
		if (election == null) {
			return "";
		}
		return resolve(component, election.getEffectiveElectionType(), election.getRestrictedCountryCodes());
	}

	public static String resolve(Component component, ElectionType electionType, List<String> restrictedCountryCodes) {
		if (component == null) {
			return "";
		}

		String restrictedCountries = RestrictedCountriesFormatter.format(restrictedCountryCodes, component.getLocale());
		if (!hasText(restrictedCountries)) {
			return "";
		}

		String template = resolveTemplate(component, electionType);
		if (!hasText(template)) {
			return "";
		}

		return MessageFormat.format(template, restrictedCountries);
	}

	private static String resolveTemplate(Component component, ElectionType electionType) {
		String specificKey = electionType != null
				? RESTRICTED_INFO_KEY_PREFIX + electionType.name()
				: DEFAULT_RESTRICTED_INFO_KEY;
		String template = component.getString(specificKey, null, null);
		if (hasText(template)) {
			return template;
		}
		return component.getString(DEFAULT_RESTRICTED_INFO_KEY, null, null);
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
