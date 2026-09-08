package net.lacnic.elections.data;

import java.io.Serializable;

public class CandidatePhotoBatchResult implements Serializable {

	private static final long serialVersionUID = -723403454976480446L;

	private long totalCandidates;
	private long updatedCandidates;
	private long skippedCandidates;
	private long failedCandidates;
	private long originalTotalBytes;
	private long resultingTotalBytes;

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

	public long getOriginalTotalBytes() {
		return originalTotalBytes;
	}

	public void setOriginalTotalBytes(long originalTotalBytes) {
		this.originalTotalBytes = originalTotalBytes;
	}

	public long getResultingTotalBytes() {
		return resultingTotalBytes;
	}

	public void setResultingTotalBytes(long resultingTotalBytes) {
		this.resultingTotalBytes = resultingTotalBytes;
	}

	public long getSavedBytes() {
		return originalTotalBytes - resultingTotalBytes;
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

	public void addOriginalBytes(long bytes) {
		if (bytes > 0) {
			originalTotalBytes += bytes;
		}
	}

	public void addResultingBytes(long bytes) {
		if (bytes > 0) {
			resultingTotalBytes += bytes;
		}
	}
}
