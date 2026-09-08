package net.lacnic.elections.adminweb.ui.token;

import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class CandidateBiographyUtils {

	private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(?i)<[a-z!/][^>]*>");
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s\\u00A0]+");

	private CandidateBiographyUtils() {
	}

	public static String toRenderableMarkup(String biography) {
		if (!hasText(biography) || "-".equals(biography.trim())) {
			return biography;
		}
		if (containsHtml(biography)) {
			return biography;
		}
		return biography.replace("\r\n", "\n")
				.replace('\r', '\n')
				.replace("\n", "<br/>");
	}

	public static String toPlainText(String biography) {
		if (!hasText(biography) || "-".equals(biography.trim())) {
			return biography;
		}
		Document document = Jsoup.parseBodyFragment(biography);
		document.select("script,style,noscript").remove();
		for (Element item : document.select("li")) {
			item.prependText("- ");
			item.appendText(" ");
		}
		return WHITESPACE_PATTERN.matcher(document.body().text()).replaceAll(" ").trim();
	}

	public static String toPlainTextSnippet(String biography) {
		return toPlainText(biography);
	}

	public static String toPlainTextSnippet(String biography, int maxChars) {
		String normalized = toPlainText(biography);
		return truncateWithEllipsis(normalized, maxChars);
	}

	private static String truncateWithEllipsis(String value, int maxChars) {
		if (!hasText(value) || maxChars <= 0) {
			return "";
		}
		String normalized = value.trim();
		if (normalized.length() <= maxChars) {
			return normalized;
		}
		int hardLimit = Math.max(1, maxChars - 3);
		int spaceCut = normalized.lastIndexOf(' ', hardLimit);
		if (spaceCut < hardLimit / 2) {
			spaceCut = hardLimit;
		}
		return normalized.substring(0, spaceCut).trim() + "...";
	}

	private static boolean containsHtml(String value) {
		return hasText(value) && HTML_TAG_PATTERN.matcher(value).find();
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
