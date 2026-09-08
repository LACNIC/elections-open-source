package net.lacnic.elections.domain.pre;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CandidateDeclarationDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	private CandidateDeclarationCode code;
	private CandidateDeclarationInputType inputType;
	private String title;
	private String descriptionHtml;
	private String regulationLinkLabel;
	private String regulationLinkUrl;
	private String declarationText;
	private List<CandidateDeclarationOptionDefinition> options = new ArrayList<>();
	private boolean required;

	public CandidateDeclarationCode getCode() {
		return code;
	}

	public void setCode(CandidateDeclarationCode code) {
		this.code = code;
	}

	public CandidateDeclarationInputType getInputType() {
		return inputType;
	}

	public void setInputType(CandidateDeclarationInputType inputType) {
		this.inputType = inputType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescriptionHtml() {
		return descriptionHtml;
	}

	public void setDescriptionHtml(String descriptionHtml) {
		this.descriptionHtml = descriptionHtml;
	}

	public String getRegulationLinkLabel() {
		return regulationLinkLabel;
	}

	public void setRegulationLinkLabel(String regulationLinkLabel) {
		this.regulationLinkLabel = regulationLinkLabel;
	}

	public String getRegulationLinkUrl() {
		return regulationLinkUrl;
	}

	public void setRegulationLinkUrl(String regulationLinkUrl) {
		this.regulationLinkUrl = regulationLinkUrl;
	}

	public String getDeclarationText() {
		return declarationText;
	}

	public void setDeclarationText(String declarationText) {
		this.declarationText = declarationText;
	}

	public List<CandidateDeclarationOptionDefinition> getOptions() {
		return options;
	}

	public void setOptions(List<CandidateDeclarationOptionDefinition> options) {
		this.options = options;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
}
