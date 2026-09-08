package net.lacnic.elections.adminweb.ui.token;

public enum AcceptNominationTaskMode {
	VIEW("view"),
	EDIT("edit"),
	COMPLETE("complete");

	private final String parameterValue;

	AcceptNominationTaskMode(String parameterValue) {
		this.parameterValue = parameterValue;
	}

	public String getParameterValue() {
		return parameterValue;
	}

	public static AcceptNominationTaskMode fromParameter(String value, AcceptNominationTaskMode defaultValue) {
		if (value == null || value.isEmpty()) {
			return defaultValue;
		}

		for (AcceptNominationTaskMode mode : values()) {
			if (mode.parameterValue.equalsIgnoreCase(value)) {
				return mode;
			}
		}

		return defaultValue;
	}
}
