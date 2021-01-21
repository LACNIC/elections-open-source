package net.lacnic.siselecciones.ejb;

import java.util.List;

import net.lacnic.siselecciones.data.HealthCheck;
import net.lacnic.siselecciones.data.Participacion;
import net.lacnic.siselecciones.dominio.EleccionLight;
import net.ripe.ipresource.IpResourceSet;

public interface MonitorEleccionesEJB {

	public HealthCheck obtenerDatosWS();

	public HealthCheck actualizarHCDatosWS();

	List<Participacion> obtenerParticipacionesOrgId(String org);

	List<EleccionLight> obtenerEleccionesLightDesc();

	public String getWsAuthToken();

	public IpResourceSet getWsIPsHabilitadas();
}
