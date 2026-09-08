package net.lacnic.elections.domain.pre;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import net.lacnic.elections.domain.Candidate;

@Entity
@Table(name = "candidate_election_task_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "candidate_id", "election_task_id" }))
public class CandidateElectionTaskProgress implements Serializable {

	private static final long serialVersionUID = -8826207005971440410L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidate_election_task_progress_seq")
	@SequenceGenerator(name = "candidate_election_task_progress_seq", sequenceName = "candidate_election_task_progress_seq", allocationSize = 1, initialValue = 95000)
	@Column(name = "id")
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "candidate_id", nullable = false)
	private Candidate candidate;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_task_id", nullable = false)
	private ElectionTask electionTask;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CandidateElectionTaskStatus status;

	@Column(nullable = true)
	private Date startDate;

	@Column(nullable = true)
	private Date endDate;

	public CandidateElectionTaskProgress() {
		// Default constructor for JPA
	}

	public CandidateElectionTaskProgress(Candidate candidate, ElectionTask electionTask, CandidateElectionTaskStatus status) {
		this.candidate = candidate;
		this.electionTask = electionTask;
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public ElectionTask getElectionTask() {
		return electionTask;
	}

	public CandidateElectionTaskStatus getStatus() {
		return status;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setStatus(CandidateElectionTaskStatus status) {
		this.status = status;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
