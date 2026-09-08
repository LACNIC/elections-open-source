package net.lacnic.elections.domain.services.publicelection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PublicElectionPhotoSnapshot implements Serializable {

	private static final long serialVersionUID = -3601555202320386997L;

	private PublicElectionSnapshotMetadata metadata;
	private Long electionId;
	private List<CandidatePhotoData> candidates = new ArrayList<>();

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

	public List<CandidatePhotoData> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<CandidatePhotoData> candidates) {
		this.candidates = candidates;
	}

	public static class CandidatePhotoData implements Serializable {
		private static final long serialVersionUID = 2228081348752369299L;

		private Long candidateId;
		private String pictureUrl;
		private byte[] pictureBytes;
		private String pictureName;
		private String pictureExtension;

		public Long getCandidateId() {
			return candidateId;
		}

		public void setCandidateId(Long candidateId) {
			this.candidateId = candidateId;
		}

		public String getPictureUrl() {
			return pictureUrl;
		}

		public void setPictureUrl(String pictureUrl) {
			this.pictureUrl = pictureUrl;
		}

		public byte[] getPictureBytes() {
			return pictureBytes;
		}

		public void setPictureBytes(byte[] pictureBytes) {
			this.pictureBytes = pictureBytes;
		}

		public String getPictureName() {
			return pictureName;
		}

		public void setPictureName(String pictureName) {
			this.pictureName = pictureName;
		}

		public String getPictureExtension() {
			return pictureExtension;
		}

		public void setPictureExtension(String pictureExtension) {
			this.pictureExtension = pictureExtension;
		}
	}
}
