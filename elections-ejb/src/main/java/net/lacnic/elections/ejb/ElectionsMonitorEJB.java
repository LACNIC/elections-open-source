package net.lacnic.elections.ejb;

import java.util.List;

import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participation;
import net.lacnic.elections.domain.ActivityReportTable;
import net.lacnic.elections.domain.ElectionLight;
import net.ripe.ipresource.IpResourceSet;

import net.lacnic.elections.data.TablesReportData;
import net.lacnic.elections.domain.ElectionReportTable;


public interface ElectionsMonitorEJB {

	public HealthCheck getHealthCheckData();

	public HealthCheck updateHealthCheckData();

	public List<Participation> getOrganizationParticipations(String org);

	public List<ElectionLight> getElectionsLightAllOrderStartDateDesc();

	public String getWsAuthToken();

	public String getWsAuthMethod();

	public String getWsLacnicAuthUrl();

	public IpResourceSet getWsAuthorizedIps();
	
	public List<TablesReportData> getElectionsData();
	
	public ElectionReportTable getElectionTableReport(Long id);
	
	public List<TablesReportData> getActivitiesData();
	
	public ActivityReportTable getActivityTableReport(Long id);

}
