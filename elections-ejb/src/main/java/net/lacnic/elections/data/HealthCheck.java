package net.lacnic.elections.data;

import java.io.Serializable;
import java.util.List;


public class HealthCheck implements Serializable {

	private static final long serialVersionUID = -8887832519452826861L;

	private int sendAttempts;
	private long failedAccessIps;
	private long failedAccessSum;
	private long mailsTotal;
	private long mailsPending;
	private long mailsSent;
	private List<ElectionReport> elections;


	public HealthCheck(int sendAttempts, long failedAccessIps, long failedAccessSum, long mailsTotal, long mailsPending, long mailsSent, List<ElectionReport> elections) {
		this.sendAttempts = sendAttempts;
		this.failedAccessIps = failedAccessIps;
		this.failedAccessSum = failedAccessSum;
		this.mailsTotal = mailsTotal;
		this.mailsPending = mailsPending;
		this.mailsSent = mailsSent;
		this.elections = elections;
	}


	public int getSendAttempts() {
		return sendAttempts;
	}

	public void setSendAttempts(int sendAttempts) {
		this.sendAttempts = sendAttempts;
	}

	public long getFailedAccessIps() {
		return failedAccessIps;
	}

	public void setFailedAccessIps(long failedAccessIps) {
		this.failedAccessIps = failedAccessIps;
	}

	public long getFailedAccessSum() {
		return failedAccessSum;
	}

	public void setFailedAccessSum(long failedAccessSum) {
		this.failedAccessSum = failedAccessSum;
	}

	public long getMailsTotal() {
		return mailsTotal;
	}

	public void setMailsTotal(long mailsTotal) {
		this.mailsTotal = mailsTotal;
	}

	public long getMailsPending() {
		return mailsPending;
	}

	public void setMailsPending(long mailsPending) {
		this.mailsPending = mailsPending;
	}

	public long getMailsSent() {
		return mailsSent;
	}

	public void setMailsSent(long mailsSent) {
		this.mailsSent = mailsSent;
	}

	public List<ElectionReport> getElections() {
		return elections;
	}

	public void setElections(List<ElectionReport> elections) {
		this.elections = elections;
	}

}
