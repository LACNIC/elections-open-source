package net.lacnic.elections.domain.services.publicelection;

import java.io.Serializable;
import java.util.Date;

public class PublicElectionSnapshotMetadata implements Serializable {

	private static final long serialVersionUID = -3175437030674100464L;

	private Long electionId;
	private Date lastUpdatedUtc;
	private Date nextRefreshUtc;
	private Date nextBusinessTransitionUtc;
	private Boolean stale;
	private String refreshStatus;
	private String refreshMessage;

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

	public Date getLastUpdatedUtc() {
		return lastUpdatedUtc;
	}

	public void setLastUpdatedUtc(Date lastUpdatedUtc) {
		this.lastUpdatedUtc = lastUpdatedUtc;
	}

	public Date getNextRefreshUtc() {
		return nextRefreshUtc;
	}

	public void setNextRefreshUtc(Date nextRefreshUtc) {
		this.nextRefreshUtc = nextRefreshUtc;
	}

	public Date getNextBusinessTransitionUtc() {
		return nextBusinessTransitionUtc;
	}

	public void setNextBusinessTransitionUtc(Date nextBusinessTransitionUtc) {
		this.nextBusinessTransitionUtc = nextBusinessTransitionUtc;
	}

	public Boolean getStale() {
		return stale;
	}

	public void setStale(Boolean stale) {
		this.stale = stale;
	}

	public String getRefreshStatus() {
		return refreshStatus;
	}

	public void setRefreshStatus(String refreshStatus) {
		this.refreshStatus = refreshStatus;
	}

	public String getRefreshMessage() {
		return refreshMessage;
	}

	public void setRefreshMessage(String refreshMessage) {
		this.refreshMessage = refreshMessage;
	}
}
