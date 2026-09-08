package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.wicket.util.RestrictedCountriesMessageResolver;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateCountryLink;
import net.lacnic.elections.utils.CountryUtils;

public class AcceptNominationCountriesViewPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();

	private final Map<String, String> countryLabelByCode = new LinkedHashMap<>();
	private final List<CountryLinkRow> countryLinks = new ArrayList<>();
	private final List<QuestionRow> questionRows = new ArrayList<>();
	private final Set<String> restrictedPrimaryCountryCodes = new HashSet<>();

	private String restrictedCountriesAlertMessage;

	public AcceptNominationCountriesViewPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(buildTaskTitle("cardTitle"));

		Candidate candidate = resolution.getCandidate();
		loadCountryLabels();
		loadRestrictedPrimaryCountries(candidate);
		loadCandidateData(candidate);

		Label restrictedCountriesAlert = new Label("restrictedCountriesAlert", StringUtils.defaultString(restrictedCountriesAlertMessage));
		restrictedCountriesAlert.setOutputMarkupPlaceholderTag(true);
		restrictedCountriesAlert.setVisible(StringUtils.isNotBlank(restrictedCountriesAlertMessage));
		add(restrictedCountriesAlert);

		WebMarkupContainer countriesTableContainer = new WebMarkupContainer("countriesTableContainer");
		countriesTableContainer.setOutputMarkupPlaceholderTag(true);
		add(countriesTableContainer);

		Label noCountriesLabel = new Label("noCountriesLabel", getString("acceptNominationCountriesNoRowsView"));
		noCountriesLabel.setVisible(countryLinks.isEmpty());
		countriesTableContainer.add(noCountriesLabel);

		countriesTableContainer.add(new ListView<CountryLinkRow>("countryHeaders", countryLinks) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CountryLinkRow> item) {
				CountryLinkRow row = item.getModelObject();
				item.add(new Label("countryHeaderLabel", resolveCountryLabel(row.getCountryCode())));
				WebMarkupContainer representedCountryBadge = new WebMarkupContainer("representedCountryBadge");
				representedCountryBadge.setVisible(row.isPrimaryCountry());
				representedCountryBadge.add(AttributeModifier.replace("data-bs-content", getString("acceptNominationCountriesRepresentedCountryPopover")));
				representedCountryBadge.add(AttributeModifier.replace("title", getString("acceptNominationCountriesRepresentedCountryTitle")));
				item.add(representedCountryBadge);
			}
		});

		countriesTableContainer.add(new ListView<QuestionRow>("questionRows", questionRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<QuestionRow> item) {
				QuestionRow row = item.getModelObject();
				item.add(new Label("questionLabel", row.getQuestionLabel()));
				item.add(new ListView<String>("answerCells", row.getAnswerValues()) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(ListItem<String> answerItem) {
						answerItem.add(new Label("answerCell", answerItem.getModelObject()));
					}
				});
			}
		});
	}

	private void loadCountryLabels() {
		for (String countryCode : COUNTRY_UTILS.getIdsListLacnicFirst(false)) {
			String normalizedCode = normalizeCode(countryCode);
			if (normalizedCode == null) {
				continue;
			}
			countryLabelByCode.put(normalizedCode, COUNTRY_UTILS.getDisplayLabel(normalizedCode, getLocale(), true));
		}
	}

	private void loadRestrictedPrimaryCountries(Candidate candidate) {
		restrictedPrimaryCountryCodes.clear();

		if (candidate == null || candidate.getElection() == null || candidate.getElection().getRestrictedCountryCodes() == null) {
			restrictedCountriesAlertMessage = "";
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
		for (String countryCode : COUNTRY_UTILS.getIdsListLacnicFirst(false)) {
			String normalizedCode = normalizeCode(countryCode);
			if (normalizedCode != null && restrictedPrimaryCountryCodes.contains(normalizedCode)) {
				restrictedCountryCodes.add(normalizedCode);
			}
		}

		restrictedCountriesAlertMessage = RestrictedCountriesMessageResolver.resolve(this,
				candidate.getElection().getEffectiveElectionType(),
				restrictedCountryCodes);
	}

	private void loadCandidateData(Candidate candidate) {
		countryLinks.clear();
		refreshQuestionRows();

		if (candidate == null || candidate.getCountryLinks() == null) {
			return;
		}

		Set<String> seenCountryCodes = new HashSet<>();
		CountryLinkRow primaryCountryLink = null;
		List<CountryLinkRow> nonPrimaryCountryLinks = new ArrayList<>();

		for (CandidateCountryLink persistedLink : candidate.getCountryLinks()) {
			if (persistedLink == null || StringUtils.isBlank(persistedLink.getCountryCode())) {
				continue;
			}

			String normalizedCode = normalizeCountryCodeForUi(persistedLink.getCountryCode(), persistedLink.isPrimaryCountry());
			if (normalizedCode == null) {
				continue;
			}

			if (seenCountryCodes.contains(normalizedCode)) {
				continue;
			}

			CountryLinkRow row = new CountryLinkRow();
			row.setCountryCode(normalizedCode);
			row.setQCitizen(persistedLink.isQCitizen());
			row.setQResidenceOver5y(persistedLink.isQResidenceOver5y());
			row.setQLongEmploymentOrAdvisory5y(persistedLink.isQLongEmploymentOrAdvisory5y());
			row.setQFamilyResidenceOver5y(persistedLink.isQFamilyResidenceOver5y());
			row.setQInternetCommunityOrgParticipation(persistedLink.isQInternetCommunityOrgParticipation());
			row.setQEligibleForCitizenship(persistedLink.isQEligibleForCitizenship());
			if (persistedLink.isPrimaryCountry() && primaryCountryLink == null) {
				row.setPrimaryCountry(true);
				primaryCountryLink = row;
			} else {
				row.setPrimaryCountry(false);
				nonPrimaryCountryLinks.add(row);
			}
			seenCountryCodes.add(normalizedCode);
		}

		if (primaryCountryLink != null) {
			countryLinks.add(primaryCountryLink);
		}
		countryLinks.addAll(nonPrimaryCountryLinks);
		refreshQuestionRows();
	}

	private String normalizeCountryCodeForUi(String code, boolean primaryCountry) {
		String normalizedCode = normalizeCode(code);
		if (normalizedCode == null) {
			return null;
		}
		if (primaryCountry && !COUNTRY_UTILS.isLacnicCoverageCountryCode(normalizedCode)) {
			return normalizedCode;
		}
		return normalizedCode;
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
			return null;
		}
		String label = countryLabelByCode.get(normalizedCode);
		if (label != null) {
			return label;
		}
		return COUNTRY_UTILS.getDisplayLabel(normalizedCode, getLocale(), true);
	}

	private String formatBoolean(boolean value) {
		return value ? getString("acceptNominationIncompatibilitiesOptionYes") : getString("acceptNominationIncompatibilitiesOptionNo");
	}

	private void refreshQuestionRows() {
		questionRows.clear();
		questionRows.add(new QuestionRow(getString("acceptNominationCountriesQuestionCitizen"), buildQuestionAnswers(QuestionKey.CITIZENSHIP)));
		questionRows.add(new QuestionRow(getString("acceptNominationCountriesQuestionResidenceOver5y"), buildQuestionAnswers(QuestionKey.RESIDENCE_OVER_5Y)));
		questionRows.add(new QuestionRow(
				getString("acceptNominationCountriesQuestionLongEmploymentOrAdvisory5y"),
				buildQuestionAnswers(QuestionKey.LONG_EMPLOYMENT_OR_ADVISORY_5Y)));
		questionRows.add(new QuestionRow(getString("acceptNominationCountriesQuestionFamilyResidenceOver5y"), buildQuestionAnswers(QuestionKey.FAMILY_RESIDENCE_OVER_5Y)));
		questionRows.add(new QuestionRow(getString("acceptNominationCountriesQuestionInternetCommunityOrgParticipation"),
				buildQuestionAnswers(QuestionKey.INTERNET_COMMUNITY_ORG_PARTICIPATION)));
		questionRows.add(new QuestionRow(getString("acceptNominationCountriesQuestionEligibleForCitizenship"),
				buildQuestionAnswers(QuestionKey.ELIGIBLE_FOR_CITIZENSHIP)));
	}

	private List<String> buildQuestionAnswers(QuestionKey questionKey) {
		List<String> answers = new ArrayList<>();
		for (CountryLinkRow row : countryLinks) {
			switch (questionKey) {
			case CITIZENSHIP:
				answers.add(formatBoolean(row.isQCitizen()));
				break;
			case RESIDENCE_OVER_5Y:
				answers.add(formatBoolean(row.isQResidenceOver5y()));
				break;
			case LONG_EMPLOYMENT_OR_ADVISORY_5Y:
				answers.add(formatBoolean(row.isQLongEmploymentOrAdvisory5y()));
				break;
			case FAMILY_RESIDENCE_OVER_5Y:
				answers.add(formatBoolean(row.isQFamilyResidenceOver5y()));
				break;
			case INTERNET_COMMUNITY_ORG_PARTICIPATION:
				answers.add(formatBoolean(row.isQInternetCommunityOrgParticipation()));
				break;
			case ELIGIBLE_FOR_CITIZENSHIP:
				answers.add(formatBoolean(row.isQEligibleForCitizenship()));
				break;
			default:
				answers.add("-");
				break;
			}
		}
		return answers;
	}

	private enum QuestionKey {
		CITIZENSHIP,
		RESIDENCE_OVER_5Y,
		LONG_EMPLOYMENT_OR_ADVISORY_5Y,
		FAMILY_RESIDENCE_OVER_5Y,
		INTERNET_COMMUNITY_ORG_PARTICIPATION,
		ELIGIBLE_FOR_CITIZENSHIP
	}

	private static final class QuestionRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String questionLabel;
		private final List<String> answerValues;

		private QuestionRow(String questionLabel, List<String> answerValues) {
			this.questionLabel = questionLabel;
			this.answerValues = answerValues;
		}

		public String getQuestionLabel() {
			return questionLabel;
		}

		public List<String> getAnswerValues() {
			return answerValues;
		}
	}

	private static final class CountryLinkRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private String countryCode;
		private boolean primaryCountry;
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

		public boolean isPrimaryCountry() {
			return primaryCountry;
		}

		public void setPrimaryCountry(boolean primaryCountry) {
			this.primaryCountry = primaryCountry;
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
