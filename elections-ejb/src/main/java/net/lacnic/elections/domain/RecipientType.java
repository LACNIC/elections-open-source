package net.lacnic.elections.domain;

/**
 * Enum for different recipient types for emails sent from the system
 */
public enum RecipientType {
	VOTERS("It will be sent to all election's voters"), 
	VOTERS_BR("It will be sent to all election's voters with country Brazil."), 
	VOTERS_MX("It will be sent to all election's voters with country Mexico."), 
	VOTERS_NOT_VOTED_YET_TWO_ELECTIONS("It will only be sent to voters who have not yet voted in either of the two elections or in one of them."), 
	VOTERS_NOT_VOTED_YET_TWO_ELECTIONS_BR("It will only be sent to voters who have not yet voted in either of the two elections, or in one of them, with country Brazil."), 
	VOTERS_NOT_VOTED_YET_TWO_ELECTIONS_MX("It will only be sent to voters who have not yet voted in either of the two elections, or in one of them, with country Mexico."), 
	VOTERS_NOT_VOTED_YET("It will only be sent to election's voters who have not yet voted."), 
	VOTERS_NOT_VOTED_YET_BR("It will only be sent to election's voters who have not yet voted, with country Brazil."), 
	VOTERS_NOT_VOTED_YET_MX("It will only be sent to election's voters who have not yet voted, with country Mexico."), 
	VOTERS_ALREADY_VOTED("It will only be sent to those voters who have already voted in the election."), 
	VOTERS_ALREADY_VOTED_BR("It will only be sent to those voters who have already voted in the election, with country Brazil."), 
	VOTERS_ALREADY_VOTED_MX("It will only be sent to those voters who have already voted in the election, with country Mexico."), 
	AUDITORS("It will be sent to all los auditores de la elección"), 
	AUDITORS_NOT_AGREED_CONFORMITY_YET("It will only be sent to those auditors who have not yet agreed conformity with the election."), 
	AUDITORS_ALREADY_AGREED_CONFORMITY("It will only be sent to those auditors who have already agreed conformity with the election.");


	private String description;


	private RecipientType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
