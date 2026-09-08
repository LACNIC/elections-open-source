package net.lacnic.elections.utils;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public final class HtmlSanitizerUtils {

	private static final PolicyFactory STRICT_POLICY = new HtmlPolicyBuilder().toFactory();

	private HtmlSanitizerUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String sanitizeStrict(String value) {
		if (value == null) {
			return null;
		}
		return STRICT_POLICY.sanitize(value);
	}

	public static String sanitizeStrictToNull(String value) {
		String sanitized = sanitizeStrict(value);
		if (sanitized == null) {
			return null;
		}
		String trimmed = sanitized.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
