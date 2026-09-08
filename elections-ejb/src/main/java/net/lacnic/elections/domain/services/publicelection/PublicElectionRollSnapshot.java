package net.lacnic.elections.domain.services.publicelection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PublicElectionRollSnapshot implements Serializable {

	private static final long serialVersionUID = -7094204402082062057L;

	private PublicElectionSnapshotMetadata metadata;
	private Long electionId;
	private List<RollEntryData> rows = new ArrayList<>();

	public PublicElectionSnapshotMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(PublicElectionSnapshotMetadata metadata) {
		this.metadata = metadata;
	}

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

	public List<RollEntryData> getRows() {
		return rows;
	}

	public void setRows(List<RollEntryData> rows) {
		this.rows = rows;
	}

	public static class RollEntryData implements Serializable {
		private static final long serialVersionUID = -6647357139128140439L;

		private String countryCode;
		private String organizationName;
		private String representativeMask;

		public String getCountryCode() {
			return countryCode;
		}

		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}

		public String getOrganizationName() {
			return organizationName;
		}

		public void setOrganizationName(String organizationName) {
			this.organizationName = organizationName;
		}

		public String getRepresentativeMask() {
			return representativeMask;
		}

		public void setRepresentativeMask(String representativeMask) {
			this.representativeMask = representativeMask;
		}
	}
}
