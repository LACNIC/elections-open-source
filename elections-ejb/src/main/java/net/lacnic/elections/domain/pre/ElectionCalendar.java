package net.lacnic.elections.domain.pre;

import java.io.Serializable;
import java.util.Date;
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
public class ElectionCalendar implements Serializable {

	private static final long serialVersionUID = 8168181270757692751L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "election_event_seq")
	@SequenceGenerator(name = "election_event_seq", sequenceName = "election_event_seq", allocationSize = 1, initialValue = 80000)
	@Column(name = "id")
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id", nullable = false)
	private Election election;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ElectionCalendarKey calendarKey;

	@Column(nullable = true)
	private Date startDate;

	@Column(nullable = true)
	private Date endDate;

	@Column(nullable = false)
	private boolean publicable;

	@OneToMany(mappedBy = "electionCalendar", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ElectionTask> tasks;

	public ElectionCalendar() {
		// Default constructor for JPA
	}

	public ElectionCalendar(Election election, ElectionCalendarKey calendarKey, Date startDate, Date endDate, boolean publicable) {
		this.election = election;
		this.calendarKey = calendarKey;
		this.startDate = startDate;
		this.endDate = endDate;
		this.publicable = publicable;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public ElectionCalendarKey getCalendarKey() {
		return calendarKey;
	}

	public void setCalendarKey(ElectionCalendarKey calendarKey) {
		this.calendarKey = calendarKey;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean isPublicable() {
		return publicable;
	}

	public void setPublicable(boolean publicable) {
		this.publicable = publicable;
	}

	public List<ElectionTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<ElectionTask> tasks) {
		this.tasks = tasks;
	}

	public Date getEndDate() {
		return endDate;
	}
}
