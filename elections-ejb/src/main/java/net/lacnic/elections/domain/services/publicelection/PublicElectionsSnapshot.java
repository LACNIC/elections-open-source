package net.lacnic.elections.domain.services.publicelection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.lacnic.elections.domain.ElectionLight;

public class PublicElectionsSnapshot implements Serializable {

	private static final long serialVersionUID = -1628147177252114052L;

	private PublicElectionsSnapshotMetadata metadata;
	private List<ElectionLight> elections = new ArrayList<>();

	public PublicElectionsSnapshotMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(PublicElectionsSnapshotMetadata metadata) {
		this.metadata = metadata;
	}

	public List<ElectionLight> getElections() {
		return elections;
	}

	public void setElections(List<ElectionLight> elections) {
		this.elections = elections;
	}
}
