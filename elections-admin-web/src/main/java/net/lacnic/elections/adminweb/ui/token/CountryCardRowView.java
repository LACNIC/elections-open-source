package net.lacnic.elections.adminweb.ui.token;

public interface CountryCardRowView {

	String getCountryLabel();

	boolean isPrimaryCountry();

	boolean isRestrictedCountry();

	String getQCitizen();

	String getQResidenceOver5y();

	String getQLongEmploymentOrAdvisory5y();

	String getQFamilyResidenceOver5y();

	String getQInternetCommunityOrgParticipation();

	String getQEligibleForCitizenship();
}
