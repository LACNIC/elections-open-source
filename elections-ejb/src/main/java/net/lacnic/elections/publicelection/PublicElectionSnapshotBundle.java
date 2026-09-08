package net.lacnic.elections.publicelection;

import net.lacnic.elections.domain.services.publicelection.PublicElectionCoreSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionOfficialResultSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionPhotoSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionRollSnapshot;

public class PublicElectionSnapshotBundle {

	private final PublicElectionCoreSnapshot coreSnapshot;
	private final PublicElectionRollSnapshot rollSnapshot;
	private final PublicElectionPhotoSnapshot photoSnapshot;
	private final PublicElectionOfficialResultSnapshot officialResultSnapshot;

	public PublicElectionSnapshotBundle(
			PublicElectionCoreSnapshot coreSnapshot,
			PublicElectionRollSnapshot rollSnapshot,
			PublicElectionPhotoSnapshot photoSnapshot,
			PublicElectionOfficialResultSnapshot officialResultSnapshot) {
		this.coreSnapshot = coreSnapshot;
		this.rollSnapshot = rollSnapshot;
		this.photoSnapshot = photoSnapshot;
		this.officialResultSnapshot = officialResultSnapshot;
	}

	public PublicElectionCoreSnapshot getCoreSnapshot() {
		return coreSnapshot;
	}

	public PublicElectionRollSnapshot getRollSnapshot() {
		return rollSnapshot;
	}

	public PublicElectionPhotoSnapshot getPhotoSnapshot() {
		return photoSnapshot;
	}

	public PublicElectionOfficialResultSnapshot getOfficialResultSnapshot() {
		return officialResultSnapshot;
	}
}
