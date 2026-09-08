package net.lacnic.elections.domain.pre;

import java.io.Serializable;

public class PublicNominationSubmission implements Serializable {

	private static final long serialVersionUID = 1L;

	private String organizationName;
	private String organizationCountry;
	private String nominatorName;
	private String nominatorEmail;
	private String nomineeName;
	private String nomineeEmail;
	private String nomineePhoneNumber;
	private String nominationReason;
	private String uiLanguage;

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getOrganizationCountry() {
		return organizationCountry;
	}

	public void setOrganizationCountry(String organizationCountry) {
		this.organizationCountry = organizationCountry;
	}

	public String getNominatorName() {
		return nominatorName;
	}

	public void setNominatorName(String nominatorName) {
		this.nominatorName = nominatorName;
	}

	public String getNominatorEmail() {
		return nominatorEmail;
	}

	public void setNominatorEmail(String nominatorEmail) {
		this.nominatorEmail = nominatorEmail;
	}

	public String getNomineeName() {
		return nomineeName;
	}

	public void setNomineeName(String nomineeName) {
		this.nomineeName = nomineeName;
	}

	public String getNomineeEmail() {
		return nomineeEmail;
	}

	public void setNomineeEmail(String nomineeEmail) {
		this.nomineeEmail = nomineeEmail;
	}

	public String getNomineePhoneNumber() {
		return nomineePhoneNumber;
	}

	public void setNomineePhoneNumber(String nomineePhoneNumber) {
		this.nomineePhoneNumber = nomineePhoneNumber;
	}

	public String getNominationReason() {
		return nominationReason;
	}

	public void setNominationReason(String nominationReason) {
		this.nominationReason = nominationReason;
	}

	public String getUiLanguage() {
		return uiLanguage;
	}

	public void setUiLanguage(String uiLanguage) {
		this.uiLanguage = uiLanguage;
	}
}
