package net.lacnic.elections.ejb;

import java.util.List;

import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participation;
import net.lacnic.elections.domain.ElectionLight;
import net.ripe.ipresource.IpResourceSet;


public interface ElectionsMonitorEJB {

	public HealthCheck getHealthCheckData();

	public HealthCheck updateHealthCheckData();

	List<Participation> getOrganizationParticipations(String org);

	List<ElectionLight> getElectionsLightAllOrderStartDateDesc();

	public String getWsAuthToken();

	public IpResourceSet getWsAuthorizedIps();

}
