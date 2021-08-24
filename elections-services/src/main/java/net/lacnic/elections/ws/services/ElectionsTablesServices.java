package net.lacnic.elections.ws.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.data.TablesReportData;
import net.lacnic.elections.domain.Customization;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.services.ActivityReportTable;
import net.lacnic.elections.domain.services.AuditorReportTable;
import net.lacnic.elections.domain.services.CandidateReportTable;
import net.lacnic.elections.domain.services.CommissionerReportTable;
import net.lacnic.elections.domain.services.ElectionReportTable;
import net.lacnic.elections.domain.services.EmailReportTable;
import net.lacnic.elections.ws.app.AppContext;
import net.lacnic.elections.ws.auth.WebServiceAuthentication;


@Path("/tables")
public class ElectionsTablesServices implements Serializable {

	private static final long serialVersionUID = 1494739698166288624L;

	private static final Logger appLogger = LogManager.getLogger("servicesAppLogger");



	@GET
	@Path("/activities")
	@Produces("application/json; charset=UTF-8")
	public Response getActivities(@Context final HttpServletRequest request) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getActivitiesBasicData();
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/activity/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getActivity(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			ActivityReportTable activityReport = AppContext.getInstance().getMonitorBeanRemote().getActivityTableReport(id);
			if(activityReport != null) {
				return Response.ok(activityReport).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/auditors")
	@Produces("application/json; charset=UTF-8")
	public Response getAuditors(@Context final HttpServletRequest request) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getAuditorsBasicData();
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/auditor/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getAuditor(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			AuditorReportTable auditorReport = AppContext.getInstance().getMonitorBeanRemote().getAuditorTableReport(id);
			if(auditorReport != null) {
				return Response.ok(auditorReport).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/candidates")
	@Produces("application/json; charset=UTF-8")
	public Response getCandidates(@Context final HttpServletRequest request) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getCandidatesBasicData();
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/candidate/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getCandidate(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			CandidateReportTable candidateReport = AppContext.getInstance().getMonitorBeanRemote().getCandidateTableReport(id);
			if(candidateReport != null) {
				return Response.ok(candidateReport).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/commissioners")
	@Produces("application/json; charset=UTF-8")
	public Response getCommissioners(@Context final HttpServletRequest request) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getCommissionersBasicData();
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/commissioner/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getCommissioner(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			CommissionerReportTable auditorReportTable = AppContext.getInstance().getMonitorBeanRemote().getCommissionerTableReport(id);
			if(auditorReportTable != null) {
				return Response.ok(auditorReportTable).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/elections")
	@Produces("application/json; charset=UTF-8")
	public Response getElections(@Context final HttpServletRequest request) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getElectionsBasicData();
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/election/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getElection(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			ElectionReportTable electionReport = AppContext.getInstance().getMonitorBeanRemote().getElectionTableReport(id);
			if(electionReport != null) {
				return Response.ok(electionReport).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/emails")
	@Produces("application/json; charset=UTF-8")
	public Response getEmails(@Context final HttpServletRequest request) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getEmailsBasicData();
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/email/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getEmail(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			EmailReportTable emailReport = AppContext.getInstance().getMonitorBeanRemote().getEmailTableReport(id);
			if(emailReport != null) {
				return Response.ok(emailReport).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/emailshistory")
	@Produces("application/json; charset=UTF-8")
	public Response getEmailsHistory(@Context final HttpServletRequest request) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getEmailsHistoryBasicData();
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/emailhistory/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getEmailHistory(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			EmailReportTable emailHistoryReport = AppContext.getInstance().getMonitorBeanRemote().getEmailHistoryTableReport(id);
			if(emailHistoryReport != null) {
				return Response.ok(emailHistoryReport).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/ipaccesses")
	@Produces("application/json; charset=UTF-8")
	public Response getIpAccess(@Context final HttpServletRequest request) {
		try {			
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getIpAccessesBasicData();
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/ipaccess/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getIpAccessId(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			IpAccess ipAccess = AppContext.getInstance().getMonitorBeanRemote().getIpAccessTableReport(id);
			if(ipAccess != null) {
				return Response.ok(ipAccess).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/customizations")
	@Produces("application/json; charset=UTF-8")
	public Response getCustomizations(@Context final HttpServletRequest request) {

		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
		
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getCustomizationsBasicData();
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/customization/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getCustomization(@Context final HttpServletRequest request, @PathParam("id") Long id) {

		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			Customization customization = AppContext.getInstance().getMonitorBeanRemote().getCustomizationBasicData(id);
			if(customization != null) {
				return Response.ok(customization).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/jointelections")
	@Produces("application/json; charset=UTF-8")
	public Response getJointElections(@Context final HttpServletRequest request) {

		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
		
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getJointElectionsBasicData();
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/jointelection/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getJointElection(@Context final HttpServletRequest request, @PathParam("id") Long id) {

		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);
	
			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			JointElection jointelection = AppContext.getInstance().getMonitorBeanRemote().getJointElectionBasicData(id);
			if(jointelection != null) {
				return Response.ok(jointelection).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}
	
}
