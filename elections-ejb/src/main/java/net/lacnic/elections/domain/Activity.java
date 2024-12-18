package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class Activity implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "activity_seq")
	@SequenceGenerator(name = "activity_seq", sequenceName = "activity_seq", allocationSize = 1)
	@Column(name = "activity_id")
	private long activityId;

	@Column(nullable = false)
	private String userName;

	@Column(nullable = true, name = "election_id")
	private Long electionId;

	@Column(nullable = false)
	private String ip;

	@Column(nullable = false)
	private Date timestamp;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ActivityType activityType;

	@Column(columnDefinition = "TEXT")
	private String description;

	public Activity() {
		// Default initialization
	}

	public long getActivityId() {
		return activityId;
	}

	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public ActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
