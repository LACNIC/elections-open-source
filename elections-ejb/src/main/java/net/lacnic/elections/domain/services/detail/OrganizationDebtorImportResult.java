package net.lacnic.elections.domain.services.detail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrganizationDebtorImportResult implements Serializable {

	private static final long serialVersionUID = -4291227396618402477L;

	private int processedRows;
	private int updatedRows;
	private List<String> orgIds = new ArrayList<>();
	private List<String> missingOrgIds = new ArrayList<>();

	public int getProcessedRows() {
		return processedRows;
	}

	public void setProcessedRows(int processedRows) {
		this.processedRows = processedRows;
	}

	public int getUpdatedRows() {
		return updatedRows;
	}

	public void setUpdatedRows(int updatedRows) {
		this.updatedRows = updatedRows;
	}

	public List<String> getOrgIds() {
		return orgIds;
	}

	public void setOrgIds(List<String> orgIds) {
		this.orgIds = orgIds;
	}

	public List<String> getMissingOrgIds() {
		return missingOrgIds;
	}

	public void setMissingOrgIds(List<String> missingOrgIds) {
		this.missingOrgIds = missingOrgIds;
	}
}
