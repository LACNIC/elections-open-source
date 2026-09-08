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
@Table(name = "sync_run")
public class SyncRun implements Serializable {

	public static final String SYNC_TYPE_ORGANIZATIONS = "ORGANIZATIONS";
	public static final String SYNC_TYPE_CENSUS = "CENSUS";

	private static final long serialVersionUID = -4671056517782209452L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sync_run_seq")
	@SequenceGenerator(name = "sync_run_seq", sequenceName = "sync_run_seq", allocationSize = 1, initialValue = 98000)
	@Column(name = "id")
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id", nullable = false)
	private Election election;

	@Column(nullable = false, length = 64)
	private String syncRunId;

	@Column(nullable = false)
	private Date syncAt;

	@Column(nullable = false, length = 32)
	private String status;

	@Column(name = "sync_type", nullable = false, length = 32)
	private String syncType;

	@Column(nullable = false)
	private int processedRows;

	@Column(nullable = false)
	private int createdRows;

	@Column(nullable = false)
	private int updatedRows;

	@Column(name = "deleted_rows", nullable = false)
	private int deletedRows;

	@Column(name = "duration_ms", nullable = true)
	private Long durationMs;

	@Column(name = "ws_status_code", nullable = true)
	private Integer wsStatusCode;

	@Column(name = "ws_elapsed_ms", nullable = true)
	private Long wsElapsedMs;

	@Column(name = "health_indicators", nullable = true, length = 1000)
	private String healthIndicators;

	@Column(nullable = true, length = 1000)
	private String message;

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

	public String getSyncRunId() {
		return syncRunId;
	}

	public void setSyncRunId(String syncRunId) {
		this.syncRunId = syncRunId;
	}

	public Date getSyncAt() {
		return syncAt;
	}

	public void setSyncAt(Date syncAt) {
		this.syncAt = syncAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSyncType() {
		return syncType;
	}

	public void setSyncType(String syncType) {
		this.syncType = syncType;
	}

	public int getProcessedRows() {
		return processedRows;
	}

	public void setProcessedRows(int processedRows) {
		this.processedRows = processedRows;
	}

	public int getCreatedRows() {
		return createdRows;
	}

	public void setCreatedRows(int createdRows) {
		this.createdRows = createdRows;
	}

	public int getUpdatedRows() {
		return updatedRows;
	}

	public void setUpdatedRows(int updatedRows) {
		this.updatedRows = updatedRows;
	}

	public int getDeletedRows() {
		return deletedRows;
	}

	public void setDeletedRows(int deletedRows) {
		this.deletedRows = deletedRows;
	}

	public Long getDurationMs() {
		return durationMs;
	}

	public void setDurationMs(Long durationMs) {
		this.durationMs = durationMs;
	}

	public Integer getWsStatusCode() {
		return wsStatusCode;
	}

	public void setWsStatusCode(Integer wsStatusCode) {
		this.wsStatusCode = wsStatusCode;
	}

	public Long getWsElapsedMs() {
		return wsElapsedMs;
	}

	public void setWsElapsedMs(Long wsElapsedMs) {
		this.wsElapsedMs = wsElapsedMs;
	}

	public String getHealthIndicators() {
		return healthIndicators;
	}

	public void setHealthIndicators(String healthIndicators) {
		this.healthIndicators = healthIndicators;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
