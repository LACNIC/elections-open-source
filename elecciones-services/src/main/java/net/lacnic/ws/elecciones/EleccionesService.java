package net.lacnic.ws.elecciones;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.siselecciones.data.HealthCheck;
import net.lacnic.siselecciones.data.Participacion;
import net.lacnic.web.ext.elecciones.utils.Contexto;
import net.ripe.ipresource.IpAddress;
import net.ripe.ipresource.IpResourceSet;

@Path("/")
public class EleccionesService implements Serializable {

	private static final long serialVersionUID = 3362059132566116897L;

	private static final Logger appLogger = LogManager.getLogger("servicesAppLogger");

	@GET
	@Path("/hc")
	@Produces("application/json; charset=UTF-8")
	public Response getHC(@Context final HttpServletRequest request) {
		try {
			Response preResponse = authenticate(request);
			if (preResponse != null)
				return preResponse;
			HealthCheck healthCheck = Contexto.getInstance().getMonitorBeanRemote().obtenerDatosWS();
			Response response = Response.ok(healthCheck).build();
			return response;
		} catch (Exception e) {
			appLogger.error(e);
			return Response.ok(e.getLocalizedMessage()).build();
		}
	}

	@GET
	@Path("/participaciones/{org:.*}")
	@Produces("application/json; charset=UTF-8")
	public Response getParticipaciones(@Context final HttpServletRequest request, @PathParam("org") final String org) {
		try {
			Response preResponse = authenticate(request);
			if (preResponse != null)
				return preResponse;
			List<Participacion> participaciones = Contexto.getInstance().getMonitorBeanRemote().obtenerParticipacionesOrgId(org);
			return Response.ok(participaciones).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.ok(e.getLocalizedMessage()).build();
		}
	}

	@GET
	@Path("/elecciones")
	@Produces("application/json; charset=UTF-8")
	public Response getElecciones(@Context final HttpServletRequest request) {
		try {
			Response preResponse = authenticate(request);
			if (preResponse != null)
				return preResponse;
			return Response.ok(Contexto.getInstance().getMonitorBeanRemote().obtenerEleccionesLightDesc()).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.ok(e.getLocalizedMessage()).build();
		}
	}

	public static String getRemoteAddr(final HttpServletRequest request) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		return ipAddress;
	}

	private Response authenticate(HttpServletRequest request) {
		try {
			String authKeyReceived = request.getHeader("AuthToken");
			String authToken = Contexto.getInstance().getMonitorBeanRemote().getWsAuthToken();
			IpResourceSet listaIPsHabilitadas = Contexto.getInstance().getMonitorBeanRemote().getWsIPsHabilitadas();
			if (authKeyReceived == null || !authKeyReceived.equals(authToken))
				return Response.status(Response.Status.UNAUTHORIZED).entity("Unathorized access, Apikey problem").build();

			String ip = getRemoteAddr(request);
			if (!listaIPsHabilitadas.contains(IpAddress.parse(ip)))
				return Response.status(Response.Status.UNAUTHORIZED).entity("Unathorized access IP problem").build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error, Authenticate").build();
		}
		return null;
	}

	@HEAD
	@PUT
	@POST
	@DELETE
	@Produces("application/json")
	@Path("/{path: .*}")
	public Response invalidMethodResponse() {
		return Response.ok("Not a valid operation.").build();
	}

}
