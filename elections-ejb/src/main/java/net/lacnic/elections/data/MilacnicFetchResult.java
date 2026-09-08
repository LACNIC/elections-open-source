package net.lacnic.elections.data;

import java.util.Collections;
import java.util.List;

public final class MilacnicFetchResult {
	public List<MilacnicOrganizationRecord> organizations = Collections.emptyList();
	public Integer statusCode;
	public Long elapsedMs;
}
