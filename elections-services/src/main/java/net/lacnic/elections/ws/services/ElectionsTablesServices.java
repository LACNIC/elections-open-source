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


import net.lacnic.elections.data.TablesReportData;
import net.lacnic.elections.domain.ActivityReportTable;
import net.lacnic.elections.domain.ElectionReportTable;
import net.lacnic.elections.ws.app.AppContext;
import net.lacnic.elections.ws.auth.WebServiceAuthentication;

@Path("/tables")
public class ElectionsTablesServices implements Serializable {
	
	private static final long serialVersionUID = 1494739698166288624L;
	
	private static final Logger appLogger = LogManager.getLogger("servicesAppLogger");

	@GET
	@Path("/election")
	@Produces("application/json; charset=UTF-8")
	public Response getElections(@Context final HttpServletRequest request) {
		try {
			System.out.println("Holaaa");
			Response preResponse = WebServiceAuthentication.authenticate(request);
			if (preResponse != null) {
				System.out.println("repsondo null");
				return preResponse;
			};	
			System.out.println("repsondo list?");
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getElectionsData();
			Response response = Response.ok(listTablesReportData).build();
			
			return response;			
			
		} catch (Exception e) {
			appLogger.error(e);
			return Response.ok(e.getLocalizedMessage()).build();
		}
	}
	
	@GET
	@Path("/election/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getElection(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {			
			Response preResponse = WebServiceAuthentication.authenticate(request);
			if (preResponse != null) {
				System.out.println("repsondo null");
				return preResponse;
			};	
			System.out.println("repsondo elect?");
			ElectionReportTable electionReport = AppContext.getInstance().getMonitorBeanRemote().getElectionTableReport(id);
			Response response = Response.ok(electionReport).build();
			
			return response;			
			
		} catch (Exception e) {
			appLogger.error(e);
			return Response.ok(e.getLocalizedMessage()).build();
		}
	}
	
	@GET
	@Path("/activity")
	@Produces("application/json; charset=UTF-8")
	public Response getActivities(@Context final HttpServletRequest request) {
		try {			
			Response preResponse = WebServiceAuthentication.authenticate(request);
			if (preResponse != null) {
				System.out.println("repsondo null");
				return preResponse;
			};	
			System.out.println("repsondo list?");
			List<TablesReportData> listTablesReportData = AppContext.getInstance().getMonitorBeanRemote().getActivitiesData();
			Response response = Response.ok(listTablesReportData).build();
			
			return response;			
			
		} catch (Exception e) {
			appLogger.error(e);
			return Response.ok(e.getLocalizedMessage()).build();
		}
	}
	
	@GET
	@Path("/activity/{id}")
	@Produces("application/json; charset=UTF-8")
	public Response getActivity(@Context final HttpServletRequest request, @PathParam("id") Long id) {
		try {			
			Response preResponse = WebServiceAuthentication.authenticate(request);
			if (preResponse != null) {
				System.out.println("repsondo null");
				return preResponse;
			};	
			System.out.println("repsondo elect?");
			ActivityReportTable activityReport = AppContext.getInstance().getMonitorBeanRemote().getActivityTableReport(id);
			Response response = Response.ok(activityReport).build();
			
			return response;			
			
		} catch (Exception e) {
			appLogger.error(e);
			return Response.ok(e.getLocalizedMessage()).build();
		}
	}
}
