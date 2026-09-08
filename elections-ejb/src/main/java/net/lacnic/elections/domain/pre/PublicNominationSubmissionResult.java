package net.lacnic.elections.domain.pre;

import java.io.Serializable;

public class PublicNominationSubmissionResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean success;
	private String messageKey;

	public static PublicNominationSubmissionResult success(String messageKey) {
		PublicNominationSubmissionResult result = new PublicNominationSubmissionResult();
		result.setSuccess(true);
		result.setMessageKey(messageKey);
		return result;
	}

	public static PublicNominationSubmissionResult failure(String messageKey) {
		PublicNominationSubmissionResult result = new PublicNominationSubmissionResult();
		result.setSuccess(false);
		result.setMessageKey(messageKey);
		return result;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}
}
