package net.lacnic.elections.domain.pre;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.lacnic.elections.domain.ElectionType;

public class CandidateDeclarationsDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	private ElectionType electionType;
	private List<CandidateDeclarationDefinition> declarations = new ArrayList<>();

	public ElectionType getElectionType() {
		return electionType;
	}

	public void setElectionType(ElectionType electionType) {
		this.electionType = electionType;
	}

	public List<CandidateDeclarationDefinition> getDeclarations() {
		return declarations;
	}

	public void setDeclarations(List<CandidateDeclarationDefinition> declarations) {
		this.declarations = declarations;
	}
}
