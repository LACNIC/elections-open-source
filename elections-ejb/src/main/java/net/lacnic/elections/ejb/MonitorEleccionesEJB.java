package net.lacnic.elections.ejb;

import java.util.List;

import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participacion;
import net.lacnic.elections.domain.EleccionLight;
import net.ripe.ipresource.IpResourceSet;

public interface MonitorEleccionesEJB {

	public HealthCheck obtenerDatosWS();

	public HealthCheck actualizarHCDatosWS();

	List<Participacion> obtenerParticipacionesOrgId(String org);

	List<EleccionLight> obtenerEleccionesLightDesc();

	public String getWsAuthToken();

	public IpResourceSet getWsIPsHabilitadas();
}
