package net.lacnic.elections.domain.services.publicelection;

import java.io.Serializable;
import java.util.Date;

public class PublicElectionsSnapshotMetadata implements Serializable {

	private static final long serialVersionUID = 7817209970579804666L;

	public Date lastUpdatedUtc;
	public Date nextRefreshUtc;
	public Boolean stale;
	public String refreshStatus;
	public String refreshMessage;

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
