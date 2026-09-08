package net.lacnic.elections.domain.pre;

import java.io.Serializable;

import net.lacnic.elections.domain.Candidate;

public class CandidateTrainingCredentialsRequestResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean success;
	private Candidate candidate;
	private String errorMessage;
	private String debugDetails;

	public static CandidateTrainingCredentialsRequestResult success(Candidate candidate) {
		CandidateTrainingCredentialsRequestResult result = new CandidateTrainingCredentialsRequestResult();
		result.setSuccess(true);
		result.setCandidate(candidate);
		return result;
	}

	public static CandidateTrainingCredentialsRequestResult failure(String errorMessage, String debugDetails) {
		CandidateTrainingCredentialsRequestResult result = new CandidateTrainingCredentialsRequestResult();
		result.setSuccess(false);
		result.setErrorMessage(errorMessage);
		result.setDebugDetails(debugDetails);
		return result;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getDebugDetails() {
		return debugDetails;
	}

	public void setDebugDetails(String debugDetails) {
		this.debugDetails = debugDetails;
	}
}

