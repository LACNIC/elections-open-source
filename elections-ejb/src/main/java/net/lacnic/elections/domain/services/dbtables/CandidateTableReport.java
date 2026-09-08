package net.lacnic.elections.domain.services.dbtables;

import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.services.detail.CandidateDetailReport;


public class CandidateTableReport extends CandidateDetailReport {

	private static final long serialVersionUID = 7063712150369421531L;

	public CandidateTableReport() { }

	public CandidateTableReport(Candidate candidate) {
		super(candidate);
	}

}
