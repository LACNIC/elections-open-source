package net.lacnic.elections.data;

import java.io.Serializable;

public class CandidateBioMigrationBatchResult implements Serializable {

	private static final long serialVersionUID = -8181446140432698741L;

	private long totalCandidates;
	private long updatedCandidates;
	private long skippedCandidates;
	private long failedCandidates;

	public long getTotalCandidates() {
		return totalCandidates;
	}

	public void setTotalCandidates(long totalCandidates) {
		this.totalCandidates = totalCandidates;
	}

	public long getUpdatedCandidates() {
		return updatedCandidates;
	}

	public void setUpdatedCandidates(long updatedCandidates) {
		this.updatedCandidates = updatedCandidates;
	}

	public long getSkippedCandidates() {
		return skippedCandidates;
	}

	public void setSkippedCandidates(long skippedCandidates) {
		this.skippedCandidates = skippedCandidates;
	}

	public long getFailedCandidates() {
		return failedCandidates;
	}

	public void setFailedCandidates(long failedCandidates) {
		this.failedCandidates = failedCandidates;
	}

	public void incrementUpdatedCandidates() {
		updatedCandidates++;
	}

	public void incrementSkippedCandidates() {
		skippedCandidates++;
	}

	public void incrementFailedCandidates() {
		failedCandidates++;
	}
}
