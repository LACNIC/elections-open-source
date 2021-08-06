package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "activity")
public class ActivityReportTable implements Serializable {

	private static final long serialVersionUID = 240604553108694223L;
	
	@Id
	@Column(name = "activity_id")
	private long activityId;
	
	@Column(columnDefinition="TEXT")
	private String description;
	
	@Column(nullable = false)
	private String userName;

	@Column(nullable = false)
	private Date timestamp;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ActivityType activityType;
	
	@Column(nullable = true, name = "election_id")
	private Long electionId;
	
	public ActivityReportTable() { }

	public long getActivityId() {
		return activityId;
	}

	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}
	
	

}
