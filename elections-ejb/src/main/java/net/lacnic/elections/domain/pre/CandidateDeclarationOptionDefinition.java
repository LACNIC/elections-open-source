package net.lacnic.elections.domain.pre;

import java.io.Serializable;

public class CandidateDeclarationOptionDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	private String value;
	private String label;

	public CandidateDeclarationOptionDefinition() {
	}

	public CandidateDeclarationOptionDefinition(String value, String label) {
		this.value = value;
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
