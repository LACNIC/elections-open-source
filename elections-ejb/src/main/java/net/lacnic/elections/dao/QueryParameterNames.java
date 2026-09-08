package net.lacnic.elections.dao;

public final class QueryParameterNames {

	public static final String AUDITOR_ID = "auditorId";
	public static final String CALENDAR_KEY = "calendarKey";
	public static final String CANDIDATE_ID = "candidateId";
	public static final String COUNTRY = "country";
	public static final String COUNTRY_CODE = "countryCode";
	public static final String ELECTION_ID = "electionId";
	public static final String ELECTION_IDS = "electionIds";
	public static final String SUPPORTING_ORGANIZATION_ID = "supportingOrganizationId";
	public static final String SYNC_RUN_ID = "syncRunId";
	public static final String SYNC_TYPE = "syncType";
	public static final String TEMPLATE_TYPE = "templateType";

	private QueryParameterNames() {
		throw new IllegalStateException("Utility class");
	}
}
