package net.lacnic.elections.domain.services.publicelection;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class PublicElectionVisibilityReport implements Serializable {

	private static final long serialVersionUID = -3846053232934332756L;

	private final Map<String, Boolean> sections = new LinkedHashMap<>();
	private final Map<String, String> reasons = new LinkedHashMap<>();

	public Map<String, Boolean> getSections() {
		return sections;
	}

	public void setSections(Map<String, Boolean> sections) {
		this.sections.clear();
		if (sections != null) {
			this.sections.putAll(sections);
		}
	}

	public Map<String, String> getReasons() {
		return reasons;
	}

	public void setReasons(Map<String, String> reasons) {
		this.reasons.clear();
		if (reasons != null) {
			this.reasons.putAll(reasons);
		}
	}
}
