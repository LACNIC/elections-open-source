package net.lacnic.elections.domain.pre;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import net.lacnic.elections.domain.Election;

@Entity
@Table(name = "sync_audit")
public class SyncAudit implements Serializable {

	private static final long serialVersionUID = -5230319978821982220L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sync_audit_seq")
	@SequenceGenerator(name = "sync_audit_seq", sequenceName = "sync_audit_seq", allocationSize = 1, initialValue = 97000)
	@Column(name = "id")
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id", nullable = false)
	private Election election;

	@Column(nullable = false)
	private String orgId;

	@Column(nullable = false, length = 32)
	private String fieldName;

	@Column(name = "old_text", columnDefinition = "text")
	private String oldText;

	@Column(name = "new_text", columnDefinition = "text")
	private String newText;

	@Column(nullable = false, length = 64)
	private String syncRunId;

	@Column(nullable = false)
	private Date eventDate;

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

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getOldText() {
		return oldText;
	}

	public void setOldText(String oldText) {
		this.oldText = oldText;
	}

	public String getNewText() {
		return newText;
	}

	public void setNewText(String newText) {
		this.newText = newText;
	}

	public String getSyncRunId() {
		return syncRunId;
	}

	public void setSyncRunId(String syncRunId) {
		this.syncRunId = syncRunId;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
}
