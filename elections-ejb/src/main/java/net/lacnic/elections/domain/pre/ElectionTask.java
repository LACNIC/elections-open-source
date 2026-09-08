package net.lacnic.elections.domain.pre;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import net.lacnic.elections.domain.Election;

@Entity
public class ElectionTask implements Serializable {

	private static final long serialVersionUID = 4973436827531979536L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq")
	@SequenceGenerator(name = "task_seq", sequenceName = "task_seq", allocationSize = 1, initialValue = 50000)
	@Column(name = "id")
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id", nullable = false)
	private Election election;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_calendar_id", nullable = false)
	private ElectionCalendar electionCalendar;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ElectionTaskKey taskKey;

	@Column(nullable = false)
	private boolean publicable;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private TaskDependencyLevel dependencyLevel;

	@Column(nullable = true)
	private Integer displayOrder;

	@OneToMany(mappedBy = "electionTask", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CandidateElectionTaskProgress> candidateProgress;

	public ElectionTask() {
		// Default constructor for JPA
	}

	public ElectionTask(Election election, ElectionCalendar electionCalendar, ElectionTaskKey taskKey, TaskDependencyLevel dependencyLevel, boolean publicable) {
		this(election, electionCalendar, taskKey, dependencyLevel, null, publicable);
	}

	public ElectionTask(Election election, ElectionCalendar electionCalendar, ElectionTaskKey taskKey, TaskDependencyLevel dependencyLevel, Integer displayOrder, boolean publicable) {
		this.election = election;
		this.electionCalendar = electionCalendar;
		this.taskKey = taskKey;
		setDependencyLevel(dependencyLevel);
		this.displayOrder = displayOrder;
		this.publicable = publicable;
	}

	public long getId() {
		return id;
	}

	public Election getElection() {
		return election;
	}

	public ElectionTaskKey getTaskKey() {
		return taskKey;
	}

	public ElectionCalendar getElectionCalendar() {
		return electionCalendar;
	}

	public boolean isPublicable() {
		return publicable;
	}

	public TaskDependencyLevel getDependencyLevel() {
		return dependencyLevel != null ? dependencyLevel : TaskDependencyLevel.LEVEL_1;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public void setElectionCalendar(ElectionCalendar electionCalendar) {
		this.electionCalendar = electionCalendar;
	}

	public void setTaskKey(ElectionTaskKey taskKey) {
		this.taskKey = taskKey;
	}

	public void setPublicable(boolean publicable) {
		this.publicable = publicable;
	}

	public void setDependencyLevel(TaskDependencyLevel dependencyLevel) {
		this.dependencyLevel = dependencyLevel != null ? dependencyLevel : TaskDependencyLevel.LEVEL_1;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}
}
