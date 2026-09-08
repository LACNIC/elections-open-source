package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.RestrictedCountriesFormatter;
import net.lacnic.elections.adminweb.wicket.util.RestrictedCountriesMessageResolver;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.pre.CandidateCountryLink;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.utils.CountryUtils;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class GenericNominationCountriesManagementPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();

	private Candidate candidate;
	private final String token;

	private String mainCountryCode;
	private boolean mainQCitizen;
	private boolean mainQResidenceOver5y;
	private boolean mainQLongEmploymentOrAdvisory5y;
	private boolean mainQFamilyResidenceOver5y;
	private boolean mainQInternetCommunityOrgParticipation;
	private boolean mainQEligibleForCitizenship;

	private String otherCountryCode;
	private boolean otherQCitizen;
	private boolean otherQResidenceOver5y;
	private boolean otherQLongEmploymentOrAdvisory5y;
	private boolean otherQFamilyResidenceOver5y;
	private boolean otherQInternetCommunityOrgParticipation;
	private boolean otherQEligibleForCitizenship;
	private Integer editingOtherCountryIndex;

	private final List<CountryLinkRow> otherCountryLinks = new ArrayList<>();
	private final List<String> primaryCountryCodes = new ArrayList<>();
	private final List<String> otherCountryCodes = new ArrayList<>();
	private final Map<String, String> countryLabelByCode = new LinkedHashMap<>();
	private final Set<String> restrictedPrimaryCountryCodes = new HashSet<>();

	private String restrictedCountriesAlertMessage;
	private String restrictedCountriesSelectionAlertMessage;

	public GenericNominationCountriesManagementPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		this.candidate = resolution.getCandidate();
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";
		add(buildTaskTitle("cardTitle"));

		loadCountryOptions();
		loadRestrictedPrimaryCountries();
		loadFormState();

		Label restrictionMessage = new Label("restrictionMessage", getTaskResolution().hasModeRestriction() ? getString(getTaskResolution().getRestrictionMessageKey()) : "");
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);

		Form<Void> countriesForm = new Form<>("countriesForm");
		countriesForm.setOutputMarkupId(true);
		add(countriesForm);

		Label restrictedCountriesAlert = new Label("restrictedCountriesAlert", StringUtils.defaultString(restrictedCountriesAlertMessage));
		restrictedCountriesAlert.setOutputMarkupPlaceholderTag(true);
		restrictedCountriesAlert.setVisible(StringUtils.isNotBlank(restrictedCountriesAlertMessage));
		countriesForm.add(restrictedCountriesAlert);

		Label restrictedCountriesSelectionAlert = new Label("restrictedCountriesSelectionAlert", new PropertyModel<>(this, "restrictedCountriesSelectionAlertMessage")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(StringUtils.isNotBlank(getRestrictedCountriesSelectionAlertMessage()));
			}
		};
		restrictedCountriesSelectionAlert.setOutputMarkupPlaceholderTag(true);
		countriesForm.add(restrictedCountriesSelectionAlert);

		DropDownChoice<String> mainCountryChoice = buildGroupedCountryChoice("mainCountryCode", new PropertyModel<>(this, "mainCountryCode"), primaryCountryCodes, false);
		mainCountryChoice.setNullValid(true);
		mainCountryChoice.setOutputMarkupId(true);
		countriesForm.add(mainCountryChoice);

		WebMarkupContainer mainCountryLinkTypesContainer = new WebMarkupContainer("mainCountryLinkTypesContainer") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(!isSelectedMainCountryRestricted());
			}
		};
		mainCountryLinkTypesContainer.setOutputMarkupId(true);
		countriesForm.add(mainCountryLinkTypesContainer);

		mainCountryLinkTypesContainer.add(new CheckBox("mainQCitizen", new PropertyModel<>(this, "mainQCitizen")));
		mainCountryLinkTypesContainer.add(new CheckBox("mainQResidenceOver5y", new PropertyModel<>(this, "mainQResidenceOver5y")));
		mainCountryLinkTypesContainer.add(new CheckBox("mainQLongEmploymentOrAdvisory5y", new PropertyModel<>(this, "mainQLongEmploymentOrAdvisory5y")));
		mainCountryLinkTypesContainer.add(new CheckBox("mainQFamilyResidenceOver5y", new PropertyModel<>(this, "mainQFamilyResidenceOver5y")));
		mainCountryLinkTypesContainer.add(new CheckBox("mainQInternetCommunityOrgParticipation", new PropertyModel<>(this, "mainQInternetCommunityOrgParticipation")));
		mainCountryLinkTypesContainer.add(new CheckBox("mainQEligibleForCitizenship", new PropertyModel<>(this, "mainQEligibleForCitizenship")));

		DropDownChoice<String> otherCountryChoice = buildGroupedCountryChoice("otherCountryCode", new PropertyModel<>(this, "otherCountryCode"), otherCountryCodes, true);
		otherCountryChoice.setNullValid(true);
		countriesForm.add(otherCountryChoice);

		countriesForm.add(new CheckBox("otherQCitizen", new PropertyModel<>(this, "otherQCitizen")));
		countriesForm.add(new CheckBox("otherQResidenceOver5y", new PropertyModel<>(this, "otherQResidenceOver5y")));
		countriesForm.add(new CheckBox("otherQLongEmploymentOrAdvisory5y", new PropertyModel<>(this, "otherQLongEmploymentOrAdvisory5y")));
		countriesForm.add(new CheckBox("otherQFamilyResidenceOver5y", new PropertyModel<>(this, "otherQFamilyResidenceOver5y")));
		countriesForm.add(new CheckBox("otherQInternetCommunityOrgParticipation", new PropertyModel<>(this, "otherQInternetCommunityOrgParticipation")));
		countriesForm.add(new CheckBox("otherQEligibleForCitizenship", new PropertyModel<>(this, "otherQEligibleForCitizenship")));

		WebMarkupContainer countriesTableContainer = new WebMarkupContainer("countriesTableContainer");
		countriesTableContainer.setOutputMarkupPlaceholderTag(true);
		countriesTableContainer.setOutputMarkupId(true);
		countriesForm.add(countriesTableContainer);

		Label noCountriesLabel = new Label("noCountriesLabel", getString("acceptNominationCountriesNoRowsOther"));
		noCountriesLabel.setOutputMarkupPlaceholderTag(true);
		noCountriesLabel.setVisible(otherCountryLinks.isEmpty());
		countriesTableContainer.add(noCountriesLabel);

		ListView<CountryLinkRow> countryRows = new ListView<>("countryRows", otherCountryLinks) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CountryLinkRow> item) {
				CountryLinkRow row = item.getModelObject();
				item.add(new ButtonDeleteWithConfirmation("removeRowButton", item.getIndex()) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onConfirm() {
						removeOtherCountryLink(item.getIndex());
						noCountriesLabel.setVisible(otherCountryLinks.isEmpty());
					}
				});
				item.add(new Label("countryLabel", resolveCountryLabel(row.getCountryCode())));
				item.add(new Label("qCitizenLabel", formatBoolean(row.isQCitizen())));
				item.add(new Label("qResidenceOver5yLabel", formatBoolean(row.isQResidenceOver5y())));
				item.add(new Label("qLongEmploymentOrAdvisory5yLabel", formatBoolean(row.isQLongEmploymentOrAdvisory5y())));
				item.add(new Label("qFamilyResidenceOver5yLabel", formatBoolean(row.isQFamilyResidenceOver5y())));
				item.add(new Label("qInternetCommunityOrgParticipationLabel", formatBoolean(row.isQInternetCommunityOrgParticipation())));
				item.add(new Label("qEligibleForCitizenshipLabel", formatBoolean(row.isQEligibleForCitizenship())));

				AjaxLink<Void> editRowButton = new AjaxLink<Void>("editRowButton") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						editingOtherCountryIndex = item.getIndex();
						otherCountryCode = row.getCountryCode();
						otherQCitizen = row.isQCitizen();
						otherQResidenceOver5y = row.isQResidenceOver5y();
						otherQLongEmploymentOrAdvisory5y = row.isQLongEmploymentOrAdvisory5y();
						otherQFamilyResidenceOver5y = row.isQFamilyResidenceOver5y();
						otherQInternetCommunityOrgParticipation = row.isQInternetCommunityOrgParticipation();
						otherQEligibleForCitizenship = row.isQEligibleForCitizenship();
						target.add(countriesForm);
						addExternalFeedback(target);
					}
				};
				item.add(editRowButton);
			}
		};
		countriesTableContainer.add(countryRows);

		AjaxButton addCountryLinkButton = new AjaxButton("addCountryLinkButton", countriesForm) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				if (!validateOtherCountryEntry()) {
					addExternalFeedback(target);
					return;
				}

				upsertOtherCountryLink();
				noCountriesLabel.setVisible(otherCountryLinks.isEmpty());
				target.add(countriesForm);
				addExternalFeedback(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				addExternalFeedback(target);
			}
		};
		countriesForm.add(addCountryLinkButton);

		countriesForm.add(new BookmarkablePageLink<Void>("backButton", GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token)));

		Button continueLaterButton = new Button("continueLaterButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(!isSelectedMainCountryRestricted());
			}

			@Override
			public void onSubmit() {
				if (!ensureCanContinueLater()) {
					return;
				}
				boolean shouldWarnCitizenshipRequirement = shouldWarnCitizenshipRequirement();
				if (!persistCountries(false)) {
					return;
				}
				if (shouldWarnCitizenshipRequirement) {
					getSession().warn(getString("acceptNominationCountriesCitizenshipRequirementWarning"));
				}
				requestTaskStatusChange(CandidateElectionTaskStatus.STARTED);
			}
		};
		continueLaterButton.setOutputMarkupId(true);
		continueLaterButton.setOutputMarkupPlaceholderTag(true);
		continueLaterButton.setVisible(canContinueLater());
		countriesForm.add(continueLaterButton);

		Button finishAndSendButton = new Button("finishAndSendButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(!isSelectedMainCountryRestricted());
			}

			@Override
			public void onSubmit() {
				boolean shouldWarnCitizenshipRequirement = shouldWarnCitizenshipRequirement();
				if (!persistCountries(true)) {
					return;
				}
				if (shouldWarnCitizenshipRequirement) {
					getSession().warn(getString("acceptNominationCountriesCitizenshipRequirementWarning"));
				}
				requestTaskStatusChange(CandidateElectionTaskStatus.COMPLETED);
			}
		};
		finishAndSendButton.setOutputMarkupId(true);
		countriesForm.add(finishAndSendButton);

		mainCountryChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (isSelectedMainCountryRestricted()) {
					clearMainCountryLinkTypes();
				}
				target.add(restrictedCountriesSelectionAlert);
				target.add(mainCountryLinkTypesContainer);
				target.add(continueLaterButton);
				target.add(finishAndSendButton);
				addExternalFeedback(target);
			}
		});
	}

	private void addExternalFeedback(AjaxRequestTarget target) {
		addPageFeedback(target);
	}

	private ChoiceRenderer<String> buildCountryChoiceRenderer() {
		return new ChoiceRenderer<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(String object) {
				if (object == null) {
					return "";
				}
				String label = countryLabelByCode.get(object);
				return label != null ? label : object;
			}
		};
	}

	private DropDownChoice<String> buildGroupedCountryChoice(String id, IModel<String> model, List<String> choices, boolean includeOtherCountriesGroup) {
		final ChoiceRenderer<String> choiceRenderer = buildCountryChoiceRenderer();
		return new DropDownChoice<String>(id, model, choices, choiceRenderer) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
				List<? extends String> countryChoices = getChoices();
				AppendingStringBuffer buffer = new AppendingStringBuffer((countryChoices.size() * 64) + 32);
				String selectedValue = getValue();

				buffer.append(getDefaultChoice(selectedValue));

				List<CountryOption> lacnicOptions = new ArrayList<>();
				List<CountryOption> otherOptions = new ArrayList<>();

				for (int index = 0; index < countryChoices.size(); index++) {
					String countryCode = normalizeCode(countryChoices.get(index));
					if (countryCode == null) {
						continue;
					}
					if (COUNTRY_UTILS.isLacnicCoverageCountryCode(countryCode)) {
						lacnicOptions.add(new CountryOption(countryCode, index));
					} else {
						otherOptions.add(new CountryOption(countryCode, index));
					}
				}

				appendCountryGroup(buffer, getString("acceptNominationCountriesGroupLacnic"), lacnicOptions, selectedValue, choiceRenderer);
				if (includeOtherCountriesGroup) {
					appendCountryGroup(buffer, getString("acceptNominationCountriesGroupOtherCountries"), otherOptions, selectedValue, choiceRenderer);
				}

				buffer.append('\n');
				replaceComponentTagBody(markupStream, openTag, buffer);
			}
		};
	}

	private void appendCountryGroup(AppendingStringBuffer buffer, String label, List<CountryOption> options, String selectedValue, ChoiceRenderer<String> choiceRenderer) {
		if (options.isEmpty()) {
			return;
		}
		buffer.append("\n<optgroup label=\"");
		buffer.append(Strings.escapeMarkup(label));
		buffer.append("\">");
		for (CountryOption option : options) {
			appendCountryOption(buffer, option.countryCode, option.index, selectedValue, choiceRenderer);
		}
		buffer.append("\n</optgroup>");
	}

	private void appendCountryOption(AppendingStringBuffer buffer, String countryCode, int index, String selectedValue, ChoiceRenderer<String> choiceRenderer) {
		String optionId = choiceRenderer.getIdValue(countryCode, index);
		Object displayValue = choiceRenderer.getDisplayValue(countryCode);
		String optionLabel = displayValue == null ? "" : displayValue.toString();

		buffer.append("\n<option ");
		if (StringUtils.equals(optionId, selectedValue)) {
			buffer.append("selected=\"selected\" ");
		}
		buffer.append("value=\"");
		buffer.append(Strings.escapeMarkup(optionId));
		buffer.append("\">");
		buffer.append(Strings.escapeMarkup(optionLabel));
		buffer.append("</option>");
	}

	private static final class CountryOption implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String countryCode;
		private final int index;

		private CountryOption(String countryCode, int index) {
			this.countryCode = countryCode;
			this.index = index;
		}
	}

	private void loadCountryOptions() {
		primaryCountryCodes.clear();
		otherCountryCodes.clear();
		countryLabelByCode.clear();

		for (String countryCode : COUNTRY_UTILS.getLacnicCoverageIds()) {
			String normalizedCode = normalizeCode(countryCode);
			if (normalizedCode == null) {
				continue;
			}
			if (!primaryCountryCodes.contains(normalizedCode)) {
				primaryCountryCodes.add(normalizedCode);
			}
			countryLabelByCode.putIfAbsent(normalizedCode, resolveFallbackCountryLabel(normalizedCode));
		}

		for (String countryCode : COUNTRY_UTILS.getIdsListLacnicFirst(false)) {
			String normalizedCode = normalizeCode(countryCode);
			if (normalizedCode == null) {
				continue;
			}
			if (!otherCountryCodes.contains(normalizedCode)) {
				otherCountryCodes.add(normalizedCode);
			}
			countryLabelByCode.putIfAbsent(normalizedCode, resolveFallbackCountryLabel(normalizedCode));
		}
	}

	private void loadRestrictedPrimaryCountries() {
		restrictedPrimaryCountryCodes.clear();

		if (candidate == null || candidate.getElection() == null || candidate.getElection().getRestrictedCountryCodes() == null) {
			restrictedCountriesAlertMessage = "";
			restrictedCountriesSelectionAlertMessage = "";
			return;
		}

		for (String restrictedCode : candidate.getElection().getRestrictedCountryCodes()) {
			String normalizedCode = normalizeCode(restrictedCode);
			if (normalizedCode == null) {
				continue;
			}
			restrictedPrimaryCountryCodes.add(normalizedCode);
		}

		List<String> restrictedCountryCodes = new ArrayList<>();
		for (String primaryCountryCode : primaryCountryCodes) {
			if (restrictedPrimaryCountryCodes.contains(primaryCountryCode)) {
				restrictedCountryCodes.add(primaryCountryCode);
			}
		}

		if (restrictedCountryCodes.isEmpty()) {
			restrictedCountriesAlertMessage = "";
			restrictedCountriesSelectionAlertMessage = "";
			return;
		}

		String restrictedCountries = RestrictedCountriesFormatter.format(restrictedCountryCodes, getLocale());
		restrictedCountriesAlertMessage = RestrictedCountriesMessageResolver.resolve(this,
				candidate.getElection().getEffectiveElectionType(),
				restrictedCountryCodes);
		restrictedCountriesSelectionAlertMessage = MessageFormat.format(getString("acceptNominationCountriesRestrictedSelectionAlert"), restrictedCountries);
	}

	private void loadFormState() {
		mainCountryCode = null;
		mainQCitizen = false;
		mainQResidenceOver5y = false;
		mainQLongEmploymentOrAdvisory5y = false;
		mainQFamilyResidenceOver5y = false;
		mainQInternetCommunityOrgParticipation = false;
		mainQEligibleForCitizenship = false;

		otherCountryLinks.clear();
		clearOtherCountryFormState();

		if (candidate == null || candidate.getCountryLinks() == null) {
			return;
		}

		Set<String> seenOtherCodes = new HashSet<>();
		for (CandidateCountryLink persistedLink : candidate.getCountryLinks()) {
			if (persistedLink == null || StringUtils.isBlank(persistedLink.getCountryCode())) {
				continue;
			}

			String normalizedCode = normalizeCountryCodeForUi(persistedLink.getCountryCode(), persistedLink.isPrimaryCountry());
			if (normalizedCode == null) {
				continue;
			}

			if (persistedLink.isPrimaryCountry() && StringUtils.isBlank(mainCountryCode)) {
				mainCountryCode = normalizedCode;
				ensurePrimaryCountryOptionExists(normalizedCode);
				mainQCitizen = persistedLink.isQCitizen();
				mainQResidenceOver5y = persistedLink.isQResidenceOver5y();
				mainQLongEmploymentOrAdvisory5y = persistedLink.isQLongEmploymentOrAdvisory5y();
				mainQFamilyResidenceOver5y = persistedLink.isQFamilyResidenceOver5y();
				mainQInternetCommunityOrgParticipation = persistedLink.isQInternetCommunityOrgParticipation();
				mainQEligibleForCitizenship = persistedLink.isQEligibleForCitizenship();
				continue;
			}

			if (StringUtils.equals(normalizedCode, mainCountryCode) || seenOtherCodes.contains(normalizedCode)) {
				continue;
			}

			ensureOtherCountryOptionExists(normalizedCode);
			CountryLinkRow row = new CountryLinkRow();
			row.setCountryCode(normalizedCode);
			row.setQCitizen(persistedLink.isQCitizen());
			row.setQResidenceOver5y(persistedLink.isQResidenceOver5y());
			row.setQLongEmploymentOrAdvisory5y(persistedLink.isQLongEmploymentOrAdvisory5y());
			row.setQFamilyResidenceOver5y(persistedLink.isQFamilyResidenceOver5y());
			row.setQInternetCommunityOrgParticipation(persistedLink.isQInternetCommunityOrgParticipation());
			row.setQEligibleForCitizenship(persistedLink.isQEligibleForCitizenship());
			otherCountryLinks.add(row);
			seenOtherCodes.add(normalizedCode);
		}
	}

	private String normalizeCountryCodeForUi(String code, boolean primaryCountry) {
		String normalizedCode = normalizeCode(code);
		if (normalizedCode == null) {
			return null;
		}
		if (primaryCountry && !COUNTRY_UTILS.isLacnicCoverageCountryCode(normalizedCode)) {
			return null;
		}
		return normalizedCode;
	}

	private void ensurePrimaryCountryOptionExists(String code) {
		if (code == null) {
			return;
		}
		if (!COUNTRY_UTILS.isLacnicCoverageCountryCode(code)) {
			return;
		}
		if (!primaryCountryCodes.contains(code)) {
			primaryCountryCodes.add(code);
		}
		ensureOtherCountryOptionExists(code);
	}

	private void ensureOtherCountryOptionExists(String code) {
		if (code == null) {
			return;
		}
		if (!otherCountryCodes.contains(code)) {
			otherCountryCodes.add(code);
		}
		if (!countryLabelByCode.containsKey(code)) {
			countryLabelByCode.put(code, resolveFallbackCountryLabel(code));
		}
	}

	private String resolveFallbackCountryLabel(String code) {
		String normalizedCode = normalizeCode(code);
		if (normalizedCode == null) {
			return "-";
		}
		return COUNTRY_UTILS.getDisplayLabel(normalizedCode, getLocale(), true);
	}

	private boolean validateOtherCountryEntry() {
		boolean valid = true;

		String normalizedOtherCountryCode = normalizeCode(otherCountryCode);
		if (normalizedOtherCountryCode == null) {
			error(getString("acceptNominationCountriesValidationSelectOtherCountry"));
			valid = false;
		}

		if (!hasAtLeastOneOtherCountryLinkSelected()) {
			error(getString("acceptNominationCountriesValidationSelectAtLeastOneOtherLink"));
			valid = false;
		}

		if (normalizedOtherCountryCode != null && StringUtils.equals(normalizedOtherCountryCode, normalizeCode(mainCountryCode))) {
			error(getString("acceptNominationCountriesValidationAlreadyMainCountry"));
			valid = false;
		}

		if (normalizedOtherCountryCode != null && isDuplicatedOtherCountry(normalizedOtherCountryCode)) {
			error(getString("acceptNominationCountriesValidationDuplicateOtherCountry"));
			valid = false;
		}

		return valid;
	}

	private boolean isDuplicatedOtherCountry(String normalizedOtherCountryCode) {
		for (int i = 0; i < otherCountryLinks.size(); i++) {
			if (editingOtherCountryIndex != null && editingOtherCountryIndex == i) {
				continue;
			}
			CountryLinkRow currentRow = otherCountryLinks.get(i);
			if (currentRow != null && StringUtils.equals(normalizedOtherCountryCode, normalizeCode(currentRow.getCountryCode()))) {
				return true;
			}
		}
		return false;
	}

	private void upsertOtherCountryLink() {
		CountryLinkRow row = new CountryLinkRow();
		row.setCountryCode(normalizeCode(otherCountryCode));
		row.setQCitizen(otherQCitizen);
		row.setQResidenceOver5y(otherQResidenceOver5y);
		row.setQLongEmploymentOrAdvisory5y(otherQLongEmploymentOrAdvisory5y);
		row.setQFamilyResidenceOver5y(otherQFamilyResidenceOver5y);
		row.setQInternetCommunityOrgParticipation(otherQInternetCommunityOrgParticipation);
		row.setQEligibleForCitizenship(otherQEligibleForCitizenship);

		ensureOtherCountryOptionExists(row.getCountryCode());
		if (editingOtherCountryIndex != null && editingOtherCountryIndex >= 0 && editingOtherCountryIndex < otherCountryLinks.size()) {
			otherCountryLinks.set(editingOtherCountryIndex, row);
		} else {
			otherCountryLinks.add(row);
		}

		clearOtherCountryFormState();
	}

	private void removeOtherCountryLink(int index) {
		if (index < 0 || index >= otherCountryLinks.size()) {
			return;
		}

		otherCountryLinks.remove(index);
		if (editingOtherCountryIndex != null && editingOtherCountryIndex == index) {
			clearOtherCountryFormState();
			return;
		}
		if (editingOtherCountryIndex != null && editingOtherCountryIndex > index) {
			editingOtherCountryIndex = editingOtherCountryIndex - 1;
		}
	}

	private void clearOtherCountryFormState() {
		otherCountryCode = null;
		otherQCitizen = false;
		otherQResidenceOver5y = false;
		otherQLongEmploymentOrAdvisory5y = false;
		otherQFamilyResidenceOver5y = false;
		otherQInternetCommunityOrgParticipation = false;
		otherQEligibleForCitizenship = false;
		editingOtherCountryIndex = null;
	}

	private void clearMainCountryLinkTypes() {
		mainQCitizen = false;
		mainQResidenceOver5y = false;
		mainQLongEmploymentOrAdvisory5y = false;
		mainQFamilyResidenceOver5y = false;
		mainQInternetCommunityOrgParticipation = false;
		mainQEligibleForCitizenship = false;
	}

	private boolean persistCountries(boolean completeSubmission) {
		if (!validateCountriesForSubmit(completeSubmission)) {
			return false;
		}

		Candidate candidateData = new Candidate();
		candidateData.setCountryLinks(buildCountryLinksToPersist());

		Candidate updatedCandidate = AppContext.getInstance().getPreNominationBeanRemote().saveCandidateCountries(token, candidateData, CANDIDATE_LINK_ACTIVITY_ACTOR,
				SecurityUtils.getClientIp());
		if (updatedCandidate == null) {
			error(getString("acceptNominationCountriesSaveError"));
			return false;
		}

		candidate = updatedCandidate;
		return true;
	}

	private boolean validateCountriesForSubmit(boolean completeSubmission) {
		boolean valid = true;
		String normalizedMainCountryCode = normalizeCode(mainCountryCode);

		if (normalizedMainCountryCode != null && !COUNTRY_UTILS.isLacnicCoverageCountryCode(normalizedMainCountryCode)) {
			error(getString("acceptNominationCountriesValidationMainCountryLacnic"));
			valid = false;
		}

		if (normalizedMainCountryCode != null && restrictedPrimaryCountryCodes.contains(normalizedMainCountryCode)) {
			error(getString("acceptNominationCountriesValidationMainCountryRestricted"));
			valid = false;
		}

		if (!completeSubmission) {
			return valid;
		}

		if (normalizedMainCountryCode == null) {
			error(getString("acceptNominationCountriesValidationMainCountryRequired"));
			valid = false;
		}

		if (normalizedMainCountryCode != null && !hasAtLeastOneMainCountryLinkSelected()) {
			error(getString("acceptNominationCountriesValidationMainCountryAtLeastOneLink"));
			valid = false;
		}

		return valid;
	}

	private List<CandidateCountryLink> buildCountryLinksToPersist() {
		List<CandidateCountryLink> linksToPersist = new ArrayList<>();
		Set<String> usedCountryCodes = new HashSet<>();

		String normalizedMainCountryCode = normalizeCode(mainCountryCode);
		if (normalizedMainCountryCode != null) {
			linksToPersist.add(createCountryLink(normalizedMainCountryCode, true, mainQCitizen, mainQResidenceOver5y, mainQLongEmploymentOrAdvisory5y,
					mainQFamilyResidenceOver5y, mainQInternetCommunityOrgParticipation, mainQEligibleForCitizenship));
			usedCountryCodes.add(normalizedMainCountryCode);
		}

		for (CountryLinkRow otherCountryLink : otherCountryLinks) {
			if (otherCountryLink == null) {
				continue;
			}
			String normalizedOtherCountryCode = normalizeCode(otherCountryLink.getCountryCode());
			if (normalizedOtherCountryCode == null || usedCountryCodes.contains(normalizedOtherCountryCode)) {
				continue;
			}

			linksToPersist.add(createCountryLink(normalizedOtherCountryCode, false, otherCountryLink.isQCitizen(), otherCountryLink.isQResidenceOver5y(),
					otherCountryLink.isQLongEmploymentOrAdvisory5y(), otherCountryLink.isQFamilyResidenceOver5y(), otherCountryLink.isQInternetCommunityOrgParticipation(),
					otherCountryLink.isQEligibleForCitizenship()));
			usedCountryCodes.add(normalizedOtherCountryCode);
		}

		return linksToPersist;
	}

	private CandidateCountryLink createCountryLink(String countryCode, boolean primaryCountry, boolean qCitizen, boolean qResidenceOver5y, boolean qLongEmploymentOrAdvisory5y,
			boolean qFamilyResidenceOver5y, boolean qInternetCommunityOrgParticipation, boolean qEligibleForCitizenship) {
		CandidateCountryLink link = new CandidateCountryLink();
		link.setCountryCode(countryCode);
		link.setPrimaryCountry(primaryCountry);
		link.setQCitizen(qCitizen);
		link.setQResidenceOver5y(qResidenceOver5y);
		link.setQLongEmploymentOrAdvisory5y(qLongEmploymentOrAdvisory5y);
		link.setQFamilyResidenceOver5y(qFamilyResidenceOver5y);
		link.setQInternetCommunityOrgParticipation(qInternetCommunityOrgParticipation);
		link.setQEligibleForCitizenship(qEligibleForCitizenship);
		return link;
	}

	private boolean hasAtLeastOneMainCountryLinkSelected() {
		return mainQCitizen
				|| mainQResidenceOver5y
				|| mainQLongEmploymentOrAdvisory5y
				|| mainQFamilyResidenceOver5y
				|| mainQInternetCommunityOrgParticipation
				|| mainQEligibleForCitizenship;
	}

	private boolean hasAtLeastOneOtherCountryLinkSelected() {
		return otherQCitizen
				|| otherQResidenceOver5y
				|| otherQLongEmploymentOrAdvisory5y
				|| otherQFamilyResidenceOver5y
				|| otherQInternetCommunityOrgParticipation
				|| otherQEligibleForCitizenship;
	}

	private boolean hasCitizenshipInLacnicRegion() {
		String normalizedMainCountryCode = normalizeCode(mainCountryCode);
		if (mainQCitizen && isLacnicCountryCode(normalizedMainCountryCode)) {
			return true;
		}

		for (CountryLinkRow otherCountryLink : otherCountryLinks) {
			if (otherCountryLink == null || !otherCountryLink.isQCitizen()) {
				continue;
			}
			if (isLacnicCountryCode(normalizeCode(otherCountryLink.getCountryCode()))) {
				return true;
			}
		}
		return false;
	}

	private boolean shouldWarnCitizenshipRequirement() {
		return isStatutoryElection() && hasAnyCountrySelected() && !hasCitizenshipInLacnicRegion();
	}

	private boolean hasAnyCountrySelected() {
		return normalizeCode(mainCountryCode) != null || !otherCountryLinks.isEmpty();
	}

	private boolean isSelectedMainCountryRestricted() {
		String normalizedMainCountryCode = normalizeCode(mainCountryCode);
		return normalizedMainCountryCode != null && restrictedPrimaryCountryCodes.contains(normalizedMainCountryCode);
	}

	public String getRestrictedCountriesSelectionAlertMessage() {
		return isSelectedMainCountryRestricted() ? StringUtils.defaultString(restrictedCountriesSelectionAlertMessage) : "";
	}

	private boolean isStatutoryElection() {
		return candidate != null
				&& candidate.getElection() != null
				&& ElectionCategory.STATUTORY == candidate.getElection().getCategory();
	}

	private boolean isLacnicCountryCode(String countryCode) {
		return COUNTRY_UTILS.isLacnicCoverageCountryCode(countryCode);
	}

	private String normalizeCode(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		String normalizedCode = value.trim().toUpperCase(Locale.ROOT);
		return normalizedCode.length() == 2 ? normalizedCode : null;
	}

	private String resolveCountryLabel(String code) {
		String normalizedCode = normalizeCode(code);
		if (normalizedCode == null) {
			return "-";
		}
		String label = countryLabelByCode.get(normalizedCode);
		if (label != null) {
			return label;
		}
		return resolveFallbackCountryLabel(normalizedCode);
	}

	private String formatBoolean(boolean value) {
		return value ? getString("acceptNominationIncompatibilitiesOptionYes") : getString("acceptNominationIncompatibilitiesOptionNo");
	}

	public String getMainCountryCode() {
		return mainCountryCode;
	}

	public void setMainCountryCode(String mainCountryCode) {
		this.mainCountryCode = mainCountryCode;
	}

	public boolean isMainQCitizen() {
		return mainQCitizen;
	}

	public void setMainQCitizen(boolean mainQCitizen) {
		this.mainQCitizen = mainQCitizen;
	}

	public boolean isMainQResidenceOver5y() {
		return mainQResidenceOver5y;
	}

	public void setMainQResidenceOver5y(boolean mainQResidenceOver5y) {
		this.mainQResidenceOver5y = mainQResidenceOver5y;
	}

	public boolean isMainQLongEmploymentOrAdvisory5y() {
		return mainQLongEmploymentOrAdvisory5y;
	}

	public void setMainQLongEmploymentOrAdvisory5y(boolean mainQLongEmploymentOrAdvisory5y) {
		this.mainQLongEmploymentOrAdvisory5y = mainQLongEmploymentOrAdvisory5y;
	}

	public boolean isMainQFamilyResidenceOver5y() {
		return mainQFamilyResidenceOver5y;
	}

	public void setMainQFamilyResidenceOver5y(boolean mainQFamilyResidenceOver5y) {
		this.mainQFamilyResidenceOver5y = mainQFamilyResidenceOver5y;
	}

	public boolean isMainQInternetCommunityOrgParticipation() {
		return mainQInternetCommunityOrgParticipation;
	}

	public void setMainQInternetCommunityOrgParticipation(boolean mainQInternetCommunityOrgParticipation) {
		this.mainQInternetCommunityOrgParticipation = mainQInternetCommunityOrgParticipation;
	}

	public boolean isMainQEligibleForCitizenship() {
		return mainQEligibleForCitizenship;
	}

	public void setMainQEligibleForCitizenship(boolean mainQEligibleForCitizenship) {
		this.mainQEligibleForCitizenship = mainQEligibleForCitizenship;
	}

	public String getOtherCountryCode() {
		return otherCountryCode;
	}

	public void setOtherCountryCode(String otherCountryCode) {
		this.otherCountryCode = otherCountryCode;
	}

	public boolean isOtherQCitizen() {
		return otherQCitizen;
	}

	public void setOtherQCitizen(boolean otherQCitizen) {
		this.otherQCitizen = otherQCitizen;
	}

	public boolean isOtherQResidenceOver5y() {
		return otherQResidenceOver5y;
	}

	public void setOtherQResidenceOver5y(boolean otherQResidenceOver5y) {
		this.otherQResidenceOver5y = otherQResidenceOver5y;
	}

	public boolean isOtherQLongEmploymentOrAdvisory5y() {
		return otherQLongEmploymentOrAdvisory5y;
	}

	public void setOtherQLongEmploymentOrAdvisory5y(boolean otherQLongEmploymentOrAdvisory5y) {
		this.otherQLongEmploymentOrAdvisory5y = otherQLongEmploymentOrAdvisory5y;
	}

	public boolean isOtherQFamilyResidenceOver5y() {
		return otherQFamilyResidenceOver5y;
	}

	public void setOtherQFamilyResidenceOver5y(boolean otherQFamilyResidenceOver5y) {
		this.otherQFamilyResidenceOver5y = otherQFamilyResidenceOver5y;
	}

	public boolean isOtherQInternetCommunityOrgParticipation() {
		return otherQInternetCommunityOrgParticipation;
	}

	public void setOtherQInternetCommunityOrgParticipation(boolean otherQInternetCommunityOrgParticipation) {
		this.otherQInternetCommunityOrgParticipation = otherQInternetCommunityOrgParticipation;
	}

	public boolean isOtherQEligibleForCitizenship() {
		return otherQEligibleForCitizenship;
	}

	public void setOtherQEligibleForCitizenship(boolean otherQEligibleForCitizenship) {
		this.otherQEligibleForCitizenship = otherQEligibleForCitizenship;
	}

	public List<String> getPrimaryCountryCodes() {
		return primaryCountryCodes;
	}

	public List<String> getOtherCountryCodes() {
		return otherCountryCodes;
	}

	private static final class CountryLinkRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private String countryCode;
		private boolean qCitizen;
		private boolean qResidenceOver5y;
		private boolean qLongEmploymentOrAdvisory5y;
		private boolean qFamilyResidenceOver5y;
		private boolean qInternetCommunityOrgParticipation;
		private boolean qEligibleForCitizenship;

		public String getCountryCode() {
			return countryCode;
		}

		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}

		public boolean isQCitizen() {
			return qCitizen;
		}

		public void setQCitizen(boolean qCitizen) {
			this.qCitizen = qCitizen;
		}

		public boolean isQResidenceOver5y() {
			return qResidenceOver5y;
		}

		public void setQResidenceOver5y(boolean qResidenceOver5y) {
			this.qResidenceOver5y = qResidenceOver5y;
		}

		public boolean isQLongEmploymentOrAdvisory5y() {
			return qLongEmploymentOrAdvisory5y;
		}

		public void setQLongEmploymentOrAdvisory5y(boolean qLongEmploymentOrAdvisory5y) {
			this.qLongEmploymentOrAdvisory5y = qLongEmploymentOrAdvisory5y;
		}

		public boolean isQFamilyResidenceOver5y() {
			return qFamilyResidenceOver5y;
		}

		public void setQFamilyResidenceOver5y(boolean qFamilyResidenceOver5y) {
			this.qFamilyResidenceOver5y = qFamilyResidenceOver5y;
		}

		public boolean isQInternetCommunityOrgParticipation() {
			return qInternetCommunityOrgParticipation;
		}

		public void setQInternetCommunityOrgParticipation(boolean qInternetCommunityOrgParticipation) {
			this.qInternetCommunityOrgParticipation = qInternetCommunityOrgParticipation;
		}

		public boolean isQEligibleForCitizenship() {
			return qEligibleForCitizenship;
		}

		public void setQEligibleForCitizenship(boolean qEligibleForCitizenship) {
			this.qEligibleForCitizenship = qEligibleForCitizenship;
		}
	}
}
