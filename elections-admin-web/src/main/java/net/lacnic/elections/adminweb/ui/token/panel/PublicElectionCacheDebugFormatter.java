package net.lacnic.elections.adminweb.ui.token.panel;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

final class PublicElectionCacheDebugFormatter {

	private static final int MAX_DEPTH = 8;
	private static final int MAX_COLLECTION_ITEMS = 30;
	private static final String INDENT = "  ";

	private PublicElectionCacheDebugFormatter() {
	}

	static String format(Object value) {
		StringBuilder sb = new StringBuilder();
		appendValue(sb, value, 0);
		return sb.toString();
	}

	private static void appendValue(StringBuilder sb, Object value, int depth) {
		if (value == null) {
			sb.append("null");
			return;
		}
		if (depth >= MAX_DEPTH) {
			sb.append('"').append("<max-depth>").append('"');
			return;
		}
		if (value instanceof String) {
			sb.append('"').append(escape((String) value)).append('"');
			return;
		}
		if (value instanceof Number || value instanceof Boolean) {
			sb.append(String.valueOf(value));
			return;
		}
		if (value instanceof Date) {
			sb.append('"').append(formatDate((Date) value)).append('"');
			return;
		}
		if (value instanceof Enum<?>) {
			sb.append('"').append(((Enum<?>) value).name()).append('"');
			return;
		}
		Class<?> valueClass = value.getClass();
		if (valueClass.isArray()) {
			appendArray(sb, value, depth);
			return;
		}
		if (value instanceof Map<?, ?>) {
			appendMap(sb, (Map<?, ?>) value, depth);
			return;
		}
		if (value instanceof Collection<?>) {
			appendCollection(sb, (Collection<?>) value, depth);
			return;
		}
		appendObject(sb, value, depth);
	}

	private static void appendArray(StringBuilder sb, Object value, int depth) {
		if (value instanceof byte[]) {
			sb.append("{\n");
			appendIndent(sb, depth + 1);
			sb.append("\"type\": \"byte[]\",\n");
			appendIndent(sb, depth + 1);
			sb.append("\"length\": ").append(((byte[]) value).length).append('\n');
			appendIndent(sb, depth);
			sb.append('}');
			return;
		}
		int length = Array.getLength(value);
		sb.append("[\n");
		int limit = Math.min(length, MAX_COLLECTION_ITEMS);
		for (int i = 0; i < limit; i++) {
			appendIndent(sb, depth + 1);
			appendValue(sb, Array.get(value, i), depth + 1);
			if (i < limit - 1 || length > limit) {
				sb.append(',');
			}
			sb.append('\n');
		}
		if (length > limit) {
			appendIndent(sb, depth + 1);
			sb.append("\"... ").append(length - limit).append(" more items\"");
			sb.append('\n');
		}
		appendIndent(sb, depth);
		sb.append(']');
	}

	private static void appendCollection(StringBuilder sb, Collection<?> values, int depth) {
		sb.append("[\n");
		List<?> items = values instanceof List<?> ? (List<?>) values : new ArrayList<Object>(values);
		int limit = Math.min(items.size(), MAX_COLLECTION_ITEMS);
		for (int i = 0; i < limit; i++) {
			appendIndent(sb, depth + 1);
			appendValue(sb, items.get(i), depth + 1);
			if (i < limit - 1 || items.size() > limit) {
				sb.append(',');
			}
			sb.append('\n');
		}
		if (items.size() > limit) {
			appendIndent(sb, depth + 1);
			sb.append("\"... ").append(items.size() - limit).append(" more items\"");
			sb.append('\n');
		}
		appendIndent(sb, depth);
		sb.append(']');
	}

	private static void appendMap(StringBuilder sb, Map<?, ?> value, int depth) {
		sb.append("{\n");
		List<Map.Entry<?, ?>> entries = new ArrayList<Map.Entry<?, ?>>(value.entrySet());
		int limit = Math.min(entries.size(), MAX_COLLECTION_ITEMS);
		for (int i = 0; i < limit; i++) {
			Map.Entry<?, ?> entry = entries.get(i);
			appendIndent(sb, depth + 1);
			sb.append('"').append(escape(String.valueOf(entry.getKey()))).append("\": ");
			appendValue(sb, entry.getValue(), depth + 1);
			if (i < limit - 1 || entries.size() > limit) {
				sb.append(',');
			}
			sb.append('\n');
		}
		if (entries.size() > limit) {
			appendIndent(sb, depth + 1);
			sb.append("\"...\": \"").append(entries.size() - limit).append(" more entries\"");
			sb.append('\n');
		}
		appendIndent(sb, depth);
		sb.append('}');
	}

	private static void appendObject(StringBuilder sb, Object value, int depth) {
		List<Field> fields = resolveFields(value.getClass());
		if (fields.isEmpty()) {
			sb.append('"').append(escape(String.valueOf(value))).append('"');
			return;
		}
		sb.append("{\n");
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			appendIndent(sb, depth + 1);
			sb.append('"').append(escape(field.getName())).append("\": ");
			try {
				appendValue(sb, field.get(value), depth + 1);
			} catch (IllegalAccessException e) {
				sb.append('"').append("<inaccessible>").append('"');
			}
			if (i < fields.size() - 1) {
				sb.append(',');
			}
			sb.append('\n');
		}
		appendIndent(sb, depth);
		sb.append('}');
	}

	private static List<Field> resolveFields(Class<?> type) {
		Field[] publicFields = type.getFields();
		if (publicFields == null || publicFields.length == 0) {
			return Collections.emptyList();
		}
		List<Field> fields = new ArrayList<Field>(Arrays.asList(publicFields));
		Collections.sort(fields, new Comparator<Field>() {
			@Override
			public int compare(Field a, Field b) {
				return a.getName().compareTo(b.getName());
			}
		});
		List<Field> filtered = new ArrayList<Field>();
		for (Field field : fields) {
			if (field == null || Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
				continue;
			}
			filtered.add(field);
		}
		return filtered;
	}

	private static void appendIndent(StringBuilder sb, int depth) {
		for (int i = 0; i < depth; i++) {
			sb.append(INDENT);
		}
	}

	private static String formatDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

	private static String escape(String value) {
		if (value == null) {
			return "";
		}
		return value
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\r", "\\r")
				.replace("\n", "\\n")
				.replace("\t", "\\t");
	}
}
