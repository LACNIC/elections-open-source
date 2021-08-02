package net.lacnic.elections.ws.services;

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

import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participation;
import net.lacnic.elections.ws.app.AppContext;
import net.lacnic.elections.ws.auth.WebServiceAuthentication;


@Path("/")
public class ElectionsService implements Serializable {

	private static final long serialVersionUID = 3362059132566116897L;

	private static final Logger appLogger = LogManager.getLogger("servicesAppLogger");

	@GET
	@Path("/hc")
	@Produces("application/json; charset=UTF-8")
	public Response getHC(@Context final HttpServletRequest request) {
		try {
			Response preResponse = WebServiceAuthentication.authenticate(request);
			if (preResponse != null)
				return preResponse;
			HealthCheck healthCheck = AppContext.getInstance().getMonitorBeanRemote().getHealthCheckData();
			Response response = Response.ok(healthCheck).build();
			return response;
		} catch (Exception e) {
			appLogger.error(e);
			return Response.ok(e.getLocalizedMessage()).build();
		}
	}

	@GET
	@Path("/participations/{org:.*}")
	@Produces("application/json; charset=UTF-8")
	public Response getParticipations(@Context final HttpServletRequest request, @PathParam("org") final String org) {
		try {
			Response preResponse = WebServiceAuthentication.authenticate(request);
			if (preResponse != null)
				return preResponse;
			List<Participation> participations = AppContext.getInstance().getMonitorBeanRemote().getOrganizationParticipations(org);
			return Response.ok(participations).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.ok(e.getLocalizedMessage()).build();
		}
	}

	@GET
	@Path("/elections")
	@Produces("application/json; charset=UTF-8")
	public Response getElections(@Context final HttpServletRequest request) {
		try {
			Response preResponse = WebServiceAuthentication.authenticate(request);
			if (preResponse != null)
				return preResponse;
			return Response.ok(AppContext.getInstance().getMonitorBeanRemote().getElectionsLightAllOrderStartDateDesc()).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.ok(e.getLocalizedMessage()).build();
		}
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
