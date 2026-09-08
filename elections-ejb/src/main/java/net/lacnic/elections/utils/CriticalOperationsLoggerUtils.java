package net.lacnic.elections.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CriticalOperationsLoggerUtils {

	private static final Logger criticalOperationsLogger = LoggerFactory.getLogger("ejbCriticalOperationsLogger");
	private static final String NOT_AVAILABLE = "-";
	private static final int MAX_DETAIL_LENGTH = 500;
	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");
	private static final Pattern TOKEN_QUERY_PARAM_PATTERN = Pattern.compile("(?i)([?&](?:token|acceptNominationToken)=)([^&\\s]+)");
	private static final Pattern TOKEN_VALUE_PATTERN = Pattern.compile("(?i)(\\b(?:token|acceptNominationToken)\\s*(?:=|:)\\s*)([^\\s,&]+)");
	private static final Pattern BEARER_VALUE_PATTERN = Pattern.compile("(?i)(\\bBearer\\s+)([^\\s]+)");

	private CriticalOperationsLoggerUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String buildCriticalOperationLog(String ip, String user, String operation, Long electionId, String detail) {
		StringBuilder logBuilder = new StringBuilder(300);
		logBuilder.append("|").append(sanitizeActor(user));
		logBuilder.append("|").append(OffsetDateTime.now().format(TIMESTAMP_FORMATTER));
		logBuilder.append("|").append(sanitizeIp(ip));
		logBuilder.append("|").append(valueOrDash(operation));
		logBuilder.append("|").append(electionId == null ? NOT_AVAILABLE : electionId);
		logBuilder.append("|").append(sanitizeDetail(detail));
		logBuilder.append("|");
		return logBuilder.toString();
	}

	public static void logCriticalOperation(String ip, String user, String operation, Long electionId) {
		criticalOperationsLogger.info(buildCriticalOperationLog(ip, user, operation, electionId, null));
	}

	private static String sanitizeActor(String actor) {
		if (!hasText(actor)) {
			return NOT_AVAILABLE;
		}
		return actor.trim();
	}

	private static String sanitizeIp(String ip) {
		if (!hasText(ip)) {
			return NOT_AVAILABLE;
		}
		return ip.trim();
	}

	private static String sanitizeDetail(String detail) {
		if (!hasText(detail)) {
			return NOT_AVAILABLE;
		}
		String cleanDetail = detail.trim().replace('\n', ' ').replace('\r', ' ');
		cleanDetail = maskPattern(cleanDetail, TOKEN_QUERY_PARAM_PATTERN);
		cleanDetail = maskPattern(cleanDetail, TOKEN_VALUE_PATTERN);
		cleanDetail = maskPattern(cleanDetail, BEARER_VALUE_PATTERN);
		if (cleanDetail.length() > MAX_DETAIL_LENGTH) {
			return cleanDetail.substring(0, MAX_DETAIL_LENGTH) + "...";
		}
		return cleanDetail;
	}

	private static String maskPattern(String value, Pattern pattern) {
		Matcher matcher = pattern.matcher(value);
		StringBuffer maskedValue = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(maskedValue, Matcher.quoteReplacement(matcher.group(1) + "[REDACTED]"));
		}
		matcher.appendTail(maskedValue);
		return maskedValue.toString();
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private static String valueOrDash(String value) {
		if (!hasText(value)) {
			return NOT_AVAILABLE;
		}
		return value.trim();
	}
}
