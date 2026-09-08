package net.lacnic.elections.data;

import net.lacnic.elections.domain.pre.SyncRun;
import net.lacnic.elections.utils.Constants;

public final class MilacnicSyncResult {
	public int processedRows;
	public int createdRows;
	public int updatedRows;
	public int deletedRows;
	public long durationMs;
	public Integer wsStatusCode;
	public Long wsElapsedMs;
	public Integer debtorOrganizationsCount;
	public Integer brazilOrganizationsCount;
	public Integer predictedMemberDeactivations;
	public Integer unchangedExistingRows;
	public String healthIndicators;
	public String status = Constants.SYNC_STATUS_SUCCESS;
	public String message;
	public String syncRunId;
	public SyncRun syncRun;
}
