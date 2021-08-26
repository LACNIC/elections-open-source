package net.lacnic.elections.domain.services;

import java.io.Serializable;

import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.utils.DateTimeUtils;


public class ActivityReportTable implements Serializable {

	private static final long serialVersionUID = 240604553108694223L;

	private Long activityId;
	private String description;
	private String userName;
	private String timestamp;
	private String activityType;
	private Long electionId;
	private String ip;


	public ActivityReportTable() { }

	public ActivityReportTable(Activity activity) {
		this.activityId = activity.getActivityId();
		this.description = activity.getDescription();
		this.userName = activity.getUserName();
		this.timestamp = DateTimeUtils.getTableServicesDateTimeString(activity.getTimestamp());
		this.activityType = activity.getActivityType().toString();
		this.electionId = activity.getElectionId();
		this.ip = activity.getIp();
	}


	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
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

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
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

}
