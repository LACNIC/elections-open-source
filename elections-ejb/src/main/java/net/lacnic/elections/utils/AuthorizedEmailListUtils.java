package net.lacnic.elections.utils;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class AuthorizedEmailListUtils {

	private AuthorizedEmailListUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static Set<String> parseAuthorizedEmails(String rawAuthorizedEmails) {
		Set<String> emails = new LinkedHashSet<>();
		String normalizedList = org.apache.commons.lang3.StringUtils.trimToNull(rawAuthorizedEmails);
		if (normalizedList == null) {
			return emails;
		}

		String[] tokens = normalizedList.split(",");
		for (String token : tokens) {
			String normalizedEmail = normalizeEmail(token);
			if (normalizedEmail != null) {
				emails.add(normalizedEmail);
			}
		}
		return emails;
	}

	public static boolean isEmailAuthorized(String email, String rawAuthorizedEmails) {
		Set<String> authorizedEmails = parseAuthorizedEmails(rawAuthorizedEmails);
		if (authorizedEmails.isEmpty()) {
			return true;
		}

		String normalizedEmail = normalizeEmail(email);
		return normalizedEmail != null && authorizedEmails.contains(normalizedEmail);
	}

	public static String normalizeEmail(String email) {
		String normalizedEmail = org.apache.commons.lang3.StringUtils.trimToNull(email);
		return normalizedEmail != null ? normalizedEmail.toLowerCase(Locale.ROOT) : null;
	}
}
