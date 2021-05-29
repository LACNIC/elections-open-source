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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aip_seq")
	@SequenceGenerator(name = "aip_seq", sequenceName = "aip_seq", allocationSize = 1)
	private long id;

	@Column(nullable = false)
	private int attemptCount;

	@Column(nullable = false)
	private String ip;

	@Column(nullable = false)
	private Date lastAttemptDate;

	@Column(nullable = false)
	private Date firstAttemptDate;

	public Date getLastAttemptDate() {
		return lastAttemptDate;
	}

	public void setFirstAttemptDate(Date firstAttemptDate) {
		this.firstAttemptDate = firstAttemptDate;
	}

	public long getIdDisabledIp() {
		return id;
	}

	public void setIdDisabledIp(long id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public void setAttemptCount(int attempts) {
		this.attemptCount = attempts;
	}

	public Date getFirstAttemptDate() {
		return firstAttemptDate;
	}

	public void setLastAttemptDate(Date lastAttemptDate) {
		this.lastAttemptDate = lastAttemptDate;
	}

}
