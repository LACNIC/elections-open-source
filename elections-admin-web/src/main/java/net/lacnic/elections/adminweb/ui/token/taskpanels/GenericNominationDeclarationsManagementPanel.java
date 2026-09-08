package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateDeclarationCode;
import net.lacnic.elections.domain.pre.CandidateDeclarationDefinition;
import net.lacnic.elections.domain.pre.CandidateDeclarationInputType;
import net.lacnic.elections.domain.pre.CandidateDeclarationOptionDefinition;
import net.lacnic.elections.domain.pre.CandidateDeclarationsDefinition;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidatePepDeclaration;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class GenericNominationDeclarationsManagementPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";
	private static final String HTML_ATTRIBUTE_DATA_TESTID = "data-testid";

	private final String token;
	private Candidate candidate;
	private final List<DeclarationFormRow> declarationRows = new ArrayList<>();

	public GenericNominationDeclarationsManagementPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";
		this.candidate = resolution.getCandidate();
		add(buildTaskTitle("cardTitle"));

		CandidateDeclarationsDefinition definitions = loadDeclarationsDefinition(token, SecurityUtils.getLocale().getLanguage());
		initializeRows(definitions);

		Label restrictionMessage = new Label("restrictionMessage", getTaskResolution().hasModeRestriction() ? getString(getTaskResolution().getRestrictionMessageKey()) : "");
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);

		Form<Void> declarationsForm = new Form<>("declarationsForm");
		add(declarationsForm);

		ListView<DeclarationFormRow> declarationRowsView = new ListView<>("declarationRows", declarationRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<DeclarationFormRow> item) {
				DeclarationFormRow row = item.getModelObject();
				item.add(new Label("declarationTitle", StringUtils.defaultString(row.getTitle())));

				Label descriptionHtmlLabel = new Label("descriptionHtml", StringUtils.defaultString(row.getDescriptionHtml()));
				descriptionHtmlLabel.setEscapeModelStrings(false);
				item.add(descriptionHtmlLabel);

				WebMarkupContainer regulationLinkContainer = new WebMarkupContainer("regulationLinkContainer");
				boolean hasRegulationLink = StringUtils.isNotBlank(row.getRegulationLinkUrl()) && StringUtils.isNotBlank(row.getRegulationLinkLabel());
				regulationLinkContainer.setVisible(hasRegulationLink);
				ExternalLink regulationLink = new ExternalLink("regulationLink", hasRegulationLink ? row.getRegulationLinkUrl() : "#");
				regulationLink.add(new Label("regulationLinkLabel", StringUtils.defaultString(row.getRegulationLinkLabel())));
				regulationLinkContainer.add(regulationLink);
				item.add(regulationLinkContainer);

				WebMarkupContainer checkboxContainer = new WebMarkupContainer("checkboxContainer");
				boolean isCheckbox = row.getInputType() == CandidateDeclarationInputType.CHECKBOX;
				checkboxContainer.setVisible(isCheckbox);
				CheckBox acceptCheck = new CheckBox("acceptCheck", new PropertyModel<>(row, "accepted"));
				acceptCheck.setEnabled(!isReadOnlyMode());
				checkboxContainer.add(acceptCheck);
				checkboxContainer.add(new Label("acceptLabel", StringUtils.defaultString(row.getDeclarationText())));
				item.add(checkboxContainer);

				WebMarkupContainer radioContainer = new WebMarkupContainer("radioContainer");
				boolean isRadio = row.getInputType() == CandidateDeclarationInputType.RADIO;
				radioContainer.setVisible(isRadio);
				RadioGroup<CandidatePepDeclaration> pepGroup = new RadioGroup<>("pepGroup", new PropertyModel<>(row, "pepChoice"));
				pepGroup.setEnabled(!isReadOnlyMode());
				pepGroup.add(new ListView<>("pepOptions", row.getPepOptions()) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(ListItem<PepOptionRow> optionItem) {
						PepOptionRow option = optionItem.getModelObject();
						Radio<CandidatePepDeclaration> pepOptionInput = new Radio<>("pepOptionInput", Model.of(option.getValue()));
						pepOptionInput.add(AttributeModifier.replace(HTML_ATTRIBUTE_DATA_TESTID, buildPepOptionInputTestId(row, option)));
						optionItem.add(pepOptionInput);
						optionItem.add(new Label("pepOptionLabel", StringUtils.defaultString(option.getLabel())));
					}
				});
				radioContainer.add(pepGroup);
				item.add(radioContainer);
			}
		};
		declarationsForm.add(declarationRowsView);

		declarationsForm.add(new BookmarkablePageLink<Void>("backButton", GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token)));

		Button finishAndSendButton = new Button("finishAndSendButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (isReadOnlyMode()) {
					return;
				}
				if (!validateDeclarationsForm()) {
					return;
				}
				if (!persistDeclarations()) {
					SecurityUtils.error(getString("acceptNominationDeclarationsSaveError"));
					return;
				}
				requestTaskStatusChange(CandidateElectionTaskStatus.COMPLETED);
			}
		};
		finishAndSendButton.setVisible(isCompleteMode() && !isReadOnlyMode());
		declarationsForm.add(finishAndSendButton);
	}

	protected CandidateDeclarationsDefinition loadDeclarationsDefinition(String token, String language) {
		return AppContext.getInstance().getPreNominationBeanRemote().getCandidateDeclarationsDefinition(token, language);
	}

	private void initializeRows(CandidateDeclarationsDefinition definitions) {
		declarationRows.clear();
		if (definitions == null || definitions.getDeclarations() == null) {
			return;
		}

		for (CandidateDeclarationDefinition definition : definitions.getDeclarations()) {
			if (definition == null || definition.getCode() == null || definition.getInputType() == null) {
				continue;
			}

			DeclarationFormRow row = new DeclarationFormRow();
			row.setCode(definition.getCode());
			row.setInputType(definition.getInputType());
			row.setRequired(definition.isRequired());
			row.setTitle(definition.getTitle());
			row.setRegulationLinkLabel(definition.getRegulationLinkLabel());
			row.setRegulationLinkUrl(definition.getRegulationLinkUrl());
			row.setDeclarationText(definition.getDeclarationText());

			row.setDescriptionHtml(definition.getDescriptionHtml());

			if (definition.getInputType() == CandidateDeclarationInputType.CHECKBOX) {
				row.setAccepted(loadCheckboxAnswer(definition.getCode()));
			} else if (definition.getInputType() == CandidateDeclarationInputType.RADIO) {
				row.setPepChoice(loadPepAnswer());
				if (definition.getOptions() != null) {
					for (CandidateDeclarationOptionDefinition option : definition.getOptions()) {
						if (option == null || StringUtils.isBlank(option.getValue())) {
							continue;
						}
						CandidatePepDeclaration optionValue;
						try {
							optionValue = CandidatePepDeclaration.valueOf(option.getValue());
						} catch (IllegalArgumentException ex) {
							continue;
						}
						PepOptionRow optionRow = new PepOptionRow();
						optionRow.setValue(optionValue);
						optionRow.setLabel(option.getLabel());
						row.getPepOptions().add(optionRow);
					}
				}
			}

			declarationRows.add(row);
		}
	}

	private Boolean loadCheckboxAnswer(CandidateDeclarationCode code) {
		if (candidate == null || code == null) {
			return null;
		}
		switch (code) {
		case INCOMPATIBILITIES_AND_CAPACITIES:
			return candidate.getQDeclarationIncompatibilities();
		case CONFLICTS_OF_INTEREST:
			return candidate.getQDeclarationConflictsOfInterest();
		case COMPETENCIES_AND_SUITABILITY:
			return candidate.getQDeclarationCompetenciesAndSuitability();
		case DISCIPLINARY_REGULATION:
			return candidate.getQDeclarationDisciplinaryRegulation();
		case IANA_KNOWLEDGE:
			return candidate.getQDeclarationIanaKnowledge();
		case ASO_KNOWLEDGE:
			return candidate.getQDeclarationAsoKnowledge();
		case DYNAMIC_COMMITMENTS:
			return candidate.getQDeclarationDynamicCommitments();
		case DATA_USAGE_AND_PUBLICATION:
			return candidate.getQDeclarationDataUsageAndPublication();
		default:
			return null;
		}
	}

	private CandidatePepDeclaration loadPepAnswer() {
		if (candidate == null) {
			return null;
		}
		return candidate.getQDeclarationPep();
	}

	private boolean validateDeclarationsForm() {
		for (DeclarationFormRow row : declarationRows) {
			if (row == null || !row.isRequired()) {
				continue;
			}
			if (row.getInputType() == CandidateDeclarationInputType.CHECKBOX && !Boolean.TRUE.equals(row.getAccepted())) {
				SecurityUtils.error(new StringResourceModel("acceptNominationDeclarationsValidationAccept", this, null).setParameters(row.getTitle()).getString());
				return false;
			}
			if (row.getInputType() == CandidateDeclarationInputType.RADIO && row.getPepChoice() == null) {
				SecurityUtils.error(getString("acceptNominationDeclarationsValidationPep"));
				return false;
			}
		}
		return true;
	}

	private boolean persistDeclarations() {
		try {
			Candidate candidateData = new Candidate();
			candidateData.setQDeclarationIncompatibilities(readCheckboxValue(CandidateDeclarationCode.INCOMPATIBILITIES_AND_CAPACITIES));
			candidateData.setQDeclarationConflictsOfInterest(readCheckboxValue(CandidateDeclarationCode.CONFLICTS_OF_INTEREST));
			candidateData.setQDeclarationCompetenciesAndSuitability(readCheckboxValue(CandidateDeclarationCode.COMPETENCIES_AND_SUITABILITY));
			candidateData.setQDeclarationDisciplinaryRegulation(readCheckboxValue(CandidateDeclarationCode.DISCIPLINARY_REGULATION));
			candidateData.setQDeclarationIanaKnowledge(readCheckboxValue(CandidateDeclarationCode.IANA_KNOWLEDGE));
			candidateData.setQDeclarationAsoKnowledge(readCheckboxValue(CandidateDeclarationCode.ASO_KNOWLEDGE));
			candidateData.setQDeclarationPep(readPepValue());
			candidateData.setQDeclarationDynamicCommitments(readCheckboxValue(CandidateDeclarationCode.DYNAMIC_COMMITMENTS));
			candidateData.setQDeclarationDataUsageAndPublication(readCheckboxValue(CandidateDeclarationCode.DATA_USAGE_AND_PUBLICATION));

			Candidate updatedCandidate = saveDeclarations(token, candidateData, CANDIDATE_LINK_ACTIVITY_ACTOR, SecurityUtils.getClientIp());
			if (updatedCandidate == null) {
				return false;
			}
			candidate = updatedCandidate;
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	protected Candidate saveDeclarations(String token, Candidate candidateData, String actor, String clientIp) {
		return AppContext.getInstance().getPreNominationBeanRemote().saveCandidateDeclarations(token, candidateData, actor, clientIp);
	}

	private Boolean readCheckboxValue(CandidateDeclarationCode code) {
		DeclarationFormRow row = findRow(code);
		if (row == null || row.getInputType() != CandidateDeclarationInputType.CHECKBOX) {
			return null;
		}
		return Boolean.TRUE.equals(row.getAccepted());
	}

	private CandidatePepDeclaration readPepValue() {
		DeclarationFormRow row = findRow(CandidateDeclarationCode.PEP_DECLARATION);
		if (row == null) {
			return null;
		}
		return row.getPepChoice();
	}

	private DeclarationFormRow findRow(CandidateDeclarationCode code) {
		if (code == null) {
			return null;
		}
		for (DeclarationFormRow row : declarationRows) {
			if (row != null && code == row.getCode()) {
				return row;
			}
		}
		return null;
	}

	private String buildPepOptionInputTestId(DeclarationFormRow row, PepOptionRow option) {
		String declarationCode = row != null && row.getCode() != null ? row.getCode().name().toLowerCase(Locale.ROOT) : "unknown";
		String optionValue = option != null && option.getValue() != null ? option.getValue().name().toLowerCase(Locale.ROOT) : "unknown";
		return "pepOptionInput-" + declarationCode + "-" + optionValue;
	}

	private static final class DeclarationFormRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private CandidateDeclarationCode code;
		private CandidateDeclarationInputType inputType;
		private boolean required;
		private String title;
		private String descriptionHtml;
		private String regulationLinkLabel;
		private String regulationLinkUrl;
		private String declarationText;
		private Boolean accepted;
		private CandidatePepDeclaration pepChoice;
		private final List<PepOptionRow> pepOptions = new ArrayList<>();

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

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
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

		public Boolean getAccepted() {
			return accepted;
		}

		public void setAccepted(Boolean accepted) {
			this.accepted = accepted;
		}

		public CandidatePepDeclaration getPepChoice() {
			return pepChoice;
		}

		public void setPepChoice(CandidatePepDeclaration pepChoice) {
			this.pepChoice = pepChoice;
		}

		public List<PepOptionRow> getPepOptions() {
			return pepOptions;
		}
	}

	private static final class PepOptionRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private CandidatePepDeclaration value;
		private String label;

		public CandidatePepDeclaration getValue() {
			return value;
		}

		public void setValue(CandidatePepDeclaration value) {
			this.value = value;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}
}
