package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class IpAccess implements Serializable {

	private static final long serialVersionUID = -1584886907691554042L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ipaccess_seq")
	@SequenceGenerator(name = "ipaccess_seq", sequenceName = "ipaccess_seq", allocationSize = 1)
	@Column(name = "ipaccess_id")
	private long ipAccessId;

	@Column(nullable = false)
	private int attemptCount;

	@Column(nullable = false)
	private String ip;

	@Column(nullable = false)
	private Date lastAttemptDate;

	@Column(nullable = false)
	private Date firstAttemptDate;

	public IpAccess() {
		// Default initialization
	}

	public long getIpAccessId() {
		return ipAccessId;
	}

	public void setIpAccessId(long ipAccessId) {
		this.ipAccessId = ipAccessId;
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public void setAttemptCount(int attemptCount) {
		this.attemptCount = attemptCount;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getLastAttemptDate() {
		return lastAttemptDate;
	}

	public void setLastAttemptDate(Date lastAttemptDate) {
		this.lastAttemptDate = lastAttemptDate;
	}

	public Date getFirstAttemptDate() {
		return firstAttemptDate;
	}

	public void setFirstAttemptDate(Date firstAttemptDate) {
		this.firstAttemptDate = firstAttemptDate;
	}

}
