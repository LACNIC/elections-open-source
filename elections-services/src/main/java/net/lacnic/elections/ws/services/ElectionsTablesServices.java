package net.lacnic.elections.ws.services;

import java.io.Serializable;
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

import net.lacnic.elections.data.TableReportDataStringId;
import net.lacnic.elections.data.TablesReportDataLongId;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.services.dbtables.ActivityTableReport;
import net.lacnic.elections.domain.services.dbtables.AuditorTableReport;
import net.lacnic.elections.domain.services.dbtables.CandidateTableReport;
import net.lacnic.elections.domain.services.dbtables.CommissionerTableReport;
import net.lacnic.elections.domain.services.dbtables.CustomizationTableReport;
import net.lacnic.elections.domain.services.dbtables.ElectionTableReport;
import net.lacnic.elections.domain.services.dbtables.EmailTableReport;
import net.lacnic.elections.domain.services.dbtables.UserAdminTableReport;
import net.lacnic.elections.domain.services.dbtables.UserVoterTableReport;
import net.lacnic.elections.domain.services.dbtables.VoteTableReport;
import net.lacnic.elections.ws.app.AppContext;
import net.lacnic.elections.ws.auth.WebServiceAuthentication;


@Path("/tables")
public class ElectionsTablesServices implements Serializable {

	private static final long serialVersionUID = 1494739698166288624L;

	private static final Logger appLogger = LogManager.getLogger("servicesAppLogger");



	@GET
	@Path("/activities/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getActivities(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getActivitiesBasicData(pageSize, offset);
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
			ActivityTableReport activityReport = AppContext.getInstance().getMonitorBeanRemote().getActivityTableReport(id);
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
	@Path("/auditors/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getAuditors(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getAuditorsBasicData(pageSize, offset);
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
			AuditorTableReport auditorReport = AppContext.getInstance().getMonitorBeanRemote().getAuditorTableReport(id);
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
	@Path("/candidates/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getCandidates(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getCandidatesBasicData(pageSize, offset);
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
			CandidateTableReport candidateReport = AppContext.getInstance().getMonitorBeanRemote().getCandidateTableReport(id);
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
	@Path("/commissioners/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getCommissioners(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getCommissionersBasicData(pageSize, offset);
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
			CommissionerTableReport auditorReportTable = AppContext.getInstance().getMonitorBeanRemote().getCommissionerTableReport(id);
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
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getCustomizationsBasicData();
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
			CustomizationTableReport customizationReportTable = AppContext.getInstance().getMonitorBeanRemote().getCustomizationTableReport(id);
			if(customizationReportTable != null) {
				return Response.ok(customizationReportTable).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/elections/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getElections(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getElectionsBasicData(pageSize, offset);
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
			ElectionTableReport electionReport = AppContext.getInstance().getMonitorBeanRemote().getElectionTableReport(id);
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
	@Path("/electionemailtemplates/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionEmailTemplates(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getElectionEmailTemplatesBasicData(pageSize, offset);
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/electionemailtemplate/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getElectionEmailTemplate(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			ElectionEmailTemplate electionEmailTemplate = AppContext.getInstance().getMonitorBeanRemote().getElectionEmailTemplateTableReport(id);
			if(electionEmailTemplate != null) {
				return Response.ok(electionEmailTemplate).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/emails/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getEmails(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getEmailsBasicData(pageSize, offset);
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
			EmailTableReport emailReport = AppContext.getInstance().getMonitorBeanRemote().getEmailTableReport(id);
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
	@Path("/emailshistory/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getEmailsHistory(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getEmailsHistoryBasicData(pageSize, offset);
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
			EmailTableReport emailHistoryReport = AppContext.getInstance().getMonitorBeanRemote().getEmailHistoryTableReport(id);
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
	@Path("/ipaccesses/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getIpAccesses(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {			
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getIpAccessesBasicData(pageSize, offset);
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/ipaccess/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getIpAccess(@Context final HttpServletRequest request, @PathParam("id") Long id) {
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
	@Path("/jointelections/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getJointElections(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getJointElectionsBasicData(pageSize, offset);
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
			JointElection jointElection = AppContext.getInstance().getMonitorBeanRemote().getJointElectionTableReport(id);
			if(jointElection != null) {
				return Response.ok(jointElection).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/parameters")
	@Produces("application/json; charset=UTF-8")
	public Response getParameters(@Context final HttpServletRequest request) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TableReportDataStringId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getParametersBasicData();
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/parameter/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getParameter(@Context final HttpServletRequest request, @PathParam("id") String id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			Parameter parameter = AppContext.getInstance().getMonitorBeanRemote().getParameterReport(id);
			if(parameter != null) {
				return Response.ok(parameter).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/useradmins/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getUserAdmins(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TableReportDataStringId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getUserAdminBasicData(pageSize, offset);
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/useradmin/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getUserAdmin(@Context final HttpServletRequest request, @PathParam("id") String id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			UserAdminTableReport userAdminReport = AppContext.getInstance().getMonitorBeanRemote().getUserAdminReportTable(id);
			if(userAdminReport != null) {
				return Response.ok(userAdminReport).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/uservoters/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getUserVoters(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getUserVotersBasicData(pageSize, offset);
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/uservoter/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getUserVoter(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}
			// Auth OK, return requested data
			UserVoterTableReport userVoterReport = AppContext.getInstance().getMonitorBeanRemote().getUserVoterReportTable(id);
			if(userVoterReport != null) {
				return Response.ok(userVoterReport).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/votes/{pageSize}/{offset}")
	@Produces("application/json; charset=UTF-8")
	public Response getVotes(@Context final HttpServletRequest request, @PathParam("pageSize") int pageSize, @PathParam("offset") int offset) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			List<TablesReportDataLongId> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getVotesBasicData(pageSize, offset);
			return Response.ok(listTablesReportData).build();
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/vote/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getVote(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {
			// Authenticate
			Response authResponse = WebServiceAuthentication.authenticate(request);

			// If auth response not null then authentication failed, return auth response
			if (authResponse != null) {
				return authResponse;
			}

			// Auth OK, return requested data
			VoteTableReport voteReport = AppContext.getInstance().getMonitorBeanRemote().getVoteTableReport(id);
			if(voteReport != null) {
				return Response.ok(voteReport).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return Response.serverError().build();
		}
	}

}
