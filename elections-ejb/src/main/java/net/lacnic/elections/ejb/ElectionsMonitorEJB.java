package net.lacnic.elections.ejb;

import java.util.List;

import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participation;
import net.lacnic.elections.domain.ElectionLight;
import net.ripe.ipresource.IpResourceSet;


public interface ElectionsMonitorEJB {

	public HealthCheck getHealthCheckData();

	public HealthCheck updateHealthCheckData();

	public List<Participation> getOrganizationParticipations(String org);

	public List<ElectionLight> getElectionsLightAllOrderStartDateDesc();

	public String getWsAuthToken();

	public String getWsAuthMethod();

	public String getWsLacnicAuthUrl();

	public IpResourceSet getWsAuthorizedIps();

}
