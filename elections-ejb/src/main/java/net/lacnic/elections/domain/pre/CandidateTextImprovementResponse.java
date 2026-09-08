package net.lacnic.elections.domain.pre;

import java.io.Serializable;

public class CandidateTextImprovementResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String improvedText;
	private final boolean processed;
	private final boolean dailyLimitReached;

	private CandidateTextImprovementResponse(String improvedText, boolean processed, boolean dailyLimitReached) {
		this.improvedText = improvedText;
		this.processed = processed;
		this.dailyLimitReached = dailyLimitReached;
	}

	public static CandidateTextImprovementResponse success(String improvedText) {
		return new CandidateTextImprovementResponse(improvedText, true, false);
	}

	public static CandidateTextImprovementResponse dailyLimitReached() {
		return new CandidateTextImprovementResponse(null, false, true);
	}

	public static CandidateTextImprovementResponse processingError() {
		return new CandidateTextImprovementResponse(null, false, false);
	}

	public String getImprovedText() {
		return improvedText;
	}

	public boolean isProcessed() {
		return processed;
	}

	public boolean isDailyLimitReached() {
		return dailyLimitReached;
	}
}
