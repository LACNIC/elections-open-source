package net.lacnic.elections.data;

import java.io.Serializable;


public class ElectionReport implements Serializable {

	private static final long serialVersionUID = 972280016790017893L;

	private String electionName;
	private long usersVoted;
	private long usersNotVoted;
	private long usersTotal;
	private long pendingMails;


	public ElectionReport(String electionName, long usersVoted, long usersNotVoted, long usersTotal, long pendingMails) {
		this.electionName = electionName;
		this.usersVoted = usersVoted;
		this.usersNotVoted = usersNotVoted;
		this.usersTotal = usersTotal;
		this.pendingMails = pendingMails;
	}


	public String getElectionName() {
		return electionName;
	}

	public void setElectionName(String electionName) {
		this.electionName = electionName;
	}

	public long getUsersVoted() {
		return usersVoted;
	}

	public void setUsersVoted(long usersVoted) {
		this.usersVoted = usersVoted;
	}

	public long getUsersNotVoted() {
		return usersNotVoted;
	}

	public void setUsersNotVoted(long usersNotVoted) {
		this.usersNotVoted = usersNotVoted;
	}

	public long getUsersTotal() {
		return usersTotal;
	}

	public void setUsersTotal(long usersTotal) {
		this.usersTotal = usersTotal;
	}

	public long getPendingMails() {
		return pendingMails;
	}

	public void setPendingMails(long pendingMails) {
		this.pendingMails = pendingMails;
	}

}
