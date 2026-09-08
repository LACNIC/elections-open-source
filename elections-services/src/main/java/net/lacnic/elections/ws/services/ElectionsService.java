package net.lacnic.elections.ws.services;

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participation;
import net.lacnic.elections.data.ParticipationV2;
import net.lacnic.elections.domain.services.detail.ElectionDetailReport;
import net.lacnic.elections.domain.services.detail.ElectionParticipationDetailReport;
import net.lacnic.elections.domain.services.detail.OrganizationVoterDetailReport;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCoreSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionPhotoSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionRollSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionsSnapshot;
import net.lacnic.elections.ws.app.AppContext;
import net.lacnic.elections.ws.auth.WebServiceAuthentication;
import net.lacnic.elections.ws.services.util.PagingUtil;

@Path("/")
public class ElectionsService implements Serializable {

	private static final long serialVersionUID = 3362059132566116897L;

	private static final Logger appLogger = LoggerFactory.getLogger("servicesAppLogger");

	@GET
	@Path("/hc")
	@Produces("application/json; charset=UTF-8")
	public Response getHC(@Context final HttpServletRequest request) {
		try {
			Response preResponse = WebServiceAuthentication.authenticatePublicInformation(request);
			if (preResponse != null)
				return preResponse;
			HealthCheck healthCheck = AppContext.getInstance().getMonitorBeanRemote().getHealthCheckData();
			Response response = Response.ok(healthCheck).build();
			return response;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			Response response = Response.ok(new HealthCheck("Health check unavailable")).build();
			return response;
		}
	}

	@GET
	@Path("/old/participations/{org:.*}")
	@Produces("application/json; charset=UTF-8")
	public Response getParticipations(@Context final HttpServletRequest request, @PathParam("org") final String org) {
		try {
			Response preResponse = WebServiceAuthentication.authenticate(request);
			if (preResponse != null)
				return preResponse;
			List<Participation> participations = AppContext.getInstance().getMonitorBeanRemote().getOrganizationParticipations(org);
			return Response.ok(participations).build();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Response.serverError().build();
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
			appLogger.error(e.getMessage(), e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/old/participaciones/{org:.*}")
	@Produces("application/json; charset=UTF-8")
	public Response getParticipaciones(@Context final HttpServletRequest request, @PathParam("org") final String org) {
		try {
			Response preResponse = WebServiceAuthentication.authenticate(request);
			if (preResponse != null)
				return preResponse;
			List<Participation> participations = AppContext.getInstance().getMonitorBeanRemote().getOrganizationParticipations(org);
			return Response.ok(participations).build();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/participations/{org:.*}")
	@Produces("application/json; charset=UTF-8")
	public Response getParticipationsV2(@Context final HttpServletRequest request, @PathParam("org") final String org) {
		try {
			Response preResponse = WebServiceAuthentication.authenticate(request);
			if (preResponse != null)
				return preResponse;
			List<ParticipationV2> participations = AppContext.getInstance().getMonitorBeanRemote().getOrganizationParticipationsV2(org);
			return Response.ok(participations).build();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/participaciones/{org:.*}")
	@Produces("application/json; charset=UTF-8")
	public Response getParticipacionesV2(@Context final HttpServletRequest request, @PathParam("org") final String org) {
		return getParticipationsV2(request, org);
	}

	@GET
	@Path("/elecciones")
	@Produces("application/json; charset=UTF-8")
	public Response getElecciones(@Context final HttpServletRequest request) {
		try {
			Response preResponse = WebServiceAuthentication.authenticate(request);
			if (preResponse != null)
				return preResponse;
			return Response.ok(AppContext.getInstance().getMonitorBeanRemote().getElectionsLightAllOrderStartDateDesc()).build();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Response.serverError().build();
		}
	}

	/*** Elections detail ***/

	@GET
	@Path("/electionsDetail{path: .*}")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionsDetail(@Context final HttpServletRequest request) {
		return getElectionsDetail(request, null, null);
	}

	@GET
	@Path("/electionsDetail/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionsDetail(@Context final HttpServletRequest request, @PathParam("pageSize") Integer pageSize, @PathParam("offset") Integer offset) {
		try {
			// Validate paging parameters
			if (PagingUtil.validatePagingParameters(pageSize, offset)) {
				// Authenticate
				Response authResponse = WebServiceAuthentication.authenticate(request);

				// If auth response not null then authentication failed, return
				// auth response
				if (authResponse != null) {
					return authResponse;
				}

				// Auth OK, return requested data
				List<ElectionDetailReport> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getElectionsDetailReport(pageSize, offset);
				return Response.ok(listTablesReportData).build();
			} else {
				return Response.ok(PagingUtil.getPagingInfoResponse(request.getScheme(), request.getServerName(), request.getServerPort(), "/electionsDetail")).build();
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/electionDetail/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionDetail(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth
			// response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			ElectionDetailReport electionReportData = AppContext.getInstance().getMonitorBeanRemote().getElectionDetailReport(id);
			if (electionReportData != null) {
				return Response.ok(electionReportData).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Response.serverError().build();
		}
	}

	/*** Elections participations by mail ***/

	@GET
	@Path("/electionsParticipationsByEmail{path: .*}")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionsParticipationsByEmail(@Context final HttpServletRequest request) {
		return getElectionsParticipationsByEmail(request, null, null, null);
	}

	@GET
	@Path("/electionsParticipationsByEmail/{email}/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionsParticipationsByEmail(@Context final HttpServletRequest request, @PathParam("email") String email, @PathParam("pageSize") Integer pageSize, @PathParam("offset") Integer offset) {
		try {
			// Validate paging parameters
			if (PagingUtil.validatePagingParameters(pageSize, offset)) {
				// Authenticate
				Response authResponse = WebServiceAuthentication.authenticate(request);

				// If auth response not null then authentication failed, return
				// auth response
				if (authResponse != null) {
					return authResponse;
				}

				// Auth OK, return requested data
				List<ElectionParticipationDetailReport> participationsDetailList = AppContext.getInstance().getMonitorBeanRemote().getElectionsParticipationsByEmail(email, pageSize, offset);
				return Response.ok(participationsDetailList).build();
			} else {
				return Response.ok(PagingUtil.getPagingInfoResponse(request.getScheme(), request.getServerName(), request.getServerPort(), "/electionsParticipationsByEmail/<email>")).build();
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Response.serverError().build();
		}
	}

	/*** Elections participations by organization ***/

	@GET
	@Path("/electionsParticipationsByOrg{path: .*}")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionsParticipationsByOrganization(@Context final HttpServletRequest request) {
		return getElectionsParticipationsByOrganization(request, null, null, null);
	}

	@GET
	@Path("/electionsParticipationsByOrg/{orgID}/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionsParticipationsByOrganization(@Context final HttpServletRequest request, @PathParam("orgID") String orgID, @PathParam("pageSize") Integer pageSize, @PathParam("offset") Integer offset) {
		try {
			// Validate paging parameters
			if (PagingUtil.validatePagingParameters(pageSize, offset)) {
				// Authenticate
				Response authResponse = WebServiceAuthentication.authenticate(request);

				// If auth response not null then authentication failed, return
				// auth response
				if (authResponse != null) {
					return authResponse;
				}

				// Auth OK, return requested data
				List<OrganizationVoterDetailReport> participationsDetailList = AppContext.getInstance().getMonitorBeanRemote().getElectionsParticipationsByOrgId(orgID, pageSize, offset);
				return Response.ok(participationsDetailList).build();
			} else {
				return Response.ok(PagingUtil.getPagingInfoResponse(request.getScheme(), request.getServerName(), request.getServerPort(), "/electionsParticipationsByOrg/<orgID>")).build();
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/v2/elections")
	@Produces("application/json; charset=UTF-8")
	public Response getPublicElectionsSnapshot(@Context final HttpServletRequest request) {
		return getPublicSnapshotResponse(request, new Supplier<PublicElectionsSnapshot>() {
			@Override
			public PublicElectionsSnapshot get() {
				return AppContext.getInstance().getMonitorBeanRemote().getPublicElectionsSnapshot();
			}
		});
	}

	@GET
	@Path("/v2/elections/{id}/public-snapshot/core")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionPublicSnapshotCore(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		return getPublicSnapshotResponse(request, new Supplier<PublicElectionCoreSnapshot>() {
			@Override
			public PublicElectionCoreSnapshot get() {
				return AppContext.getInstance().getMonitorBeanRemote().getPublicElectionCoreSnapshot(id);
			}
		});
	}

	@GET
	@Path("/v2/elections/{id}/public-snapshot/roll")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionPublicSnapshotRoll(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		return getPublicSnapshotResponse(request, new Supplier<PublicElectionRollSnapshot>() {
			@Override
			public PublicElectionRollSnapshot get() {
				return AppContext.getInstance().getMonitorBeanRemote().getPublicElectionRollSnapshot(id);
			}
		});
	}

	@GET
	@Path("/v2/elections/{id}/public-snapshot/photos")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionPublicSnapshotPhotos(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		return getPublicSnapshotResponse(request, new Supplier<PublicElectionPhotoSnapshot>() {
			@Override
			public PublicElectionPhotoSnapshot get() {
				return AppContext.getInstance().getMonitorBeanRemote().getPublicElectionPhotoSnapshot(id);
			}
		});
	}

	private Response getPublicSnapshotResponse(HttpServletRequest request, Supplier<?> snapshotSupplier) {
		try {
			Response authResponse = WebServiceAuthentication.authenticatePublicInformation(request);
			if (authResponse != null) {
				return authResponse;
			}

			Object snapshot = snapshotSupplier.get();
			if (snapshot != null) {
				return Response.ok(snapshot).build();
			}
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Response.serverError().build();
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
