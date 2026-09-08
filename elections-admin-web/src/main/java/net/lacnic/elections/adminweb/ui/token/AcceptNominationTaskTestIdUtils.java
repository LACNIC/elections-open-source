package net.lacnic.elections.adminweb.ui.token;

import net.lacnic.elections.domain.pre.ElectionTaskKey;

public final class AcceptNominationTaskTestIdUtils {

	private AcceptNominationTaskTestIdUtils() {
	}

	public static String buildTaskScopedTestId(String prefix, ElectionTaskKey taskKey) {
		if (taskKey == null) {
			return prefix;
		}
		StringBuilder suffix = new StringBuilder();
		for (String segment : taskKey.name().toLowerCase().split("_")) {
			if (segment.isEmpty()) {
				continue;
			}
			suffix.append(Character.toUpperCase(segment.charAt(0)));
			if (segment.length() > 1) {
				suffix.append(segment.substring(1));
			}
		}
		return prefix + suffix;
	}
}
