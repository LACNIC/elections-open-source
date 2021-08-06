package net.lacnic.elections.ejb.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.dao.ReportDao;
import net.lacnic.elections.data.ElectionReport;
import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participation;
import net.lacnic.elections.domain.ActivityReportTable;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionLight;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.ejb.ElectionsMonitorEJB;
import net.lacnic.elections.ejb.commons.impl.AutomaticProcesses;
import net.lacnic.elections.utils.Constants;
import net.ripe.ipresource.IpResourceSet;

import net.lacnic.elections.data.TablesReportData;
import net.lacnic.elections.domain.ElectionReportTable;
import net.lacnic.elections.dao.ActivityDao;



@Stateless
@Remote(ElectionsMonitorEJB.class)
public class ElectionsMonitorEJBBean implements ElectionsMonitorEJB {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	private static HealthCheck healthCheck;


	public ElectionsMonitorEJBBean() { }

	/**
	 * Get the health check data.
	 * 
	 * @return returns a health check entity containing the system's health information.
	 */
	@Override
	public HealthCheck getHealthCheckData() {
		if (healthCheck == null)
			healthCheck = updateHealthCheckData();
		return healthCheck;
	}

	/**
	 * Creates a updated health check report.
	 * 
	 * @return returns a health check entity containing the health check information from the system. 
	 */
	@Override
	public HealthCheck updateHealthCheckData() {
		ReportDao reportDao = ElectionsDaoFactory.createReportDao(em);

		int sendAttempts = AutomaticProcesses.getAttempts();
		long failedAccessIps = reportDao.getFailedIpAccesesAmount();
		long failedAccessSum = reportDao.getFailedIpAccesesSum();
		long mailsTotal = reportDao.getEmailsAmount();
		long mailsPending = reportDao.getPendingSendEmailsAmount();
		long mailsSent = reportDao.getSentEmailsAmount();

		List<ElectionReport> elections = electionReport();

		return new HealthCheck(sendAttempts, failedAccessIps, failedAccessSum, mailsTotal, mailsPending, mailsSent, elections);
	}

	/**
	 * Gets a list of reports from all the elections on the system
	 * 
	 * @return returns a collection of election report entity containing the information
	 */
	private List<ElectionReport> electionReport() {
		ReportDao reportDao = ElectionsDaoFactory.createReportDao(em);
		List<ElectionReport> election = new ArrayList<>();

		for (Object[] nameAndId : reportDao.getElectionsAllIdName()) {
			Long id = (Long) nameAndId[0];
			String electionName = (String) nameAndId[1];
			long usersVoted = reportDao.getElectionAlreadyVotedAmount(id);
			long usersNotVoted = reportDao.getElectionNotVotedYetAmount(id);
			long usersTotal = reportDao.getElectionCensusSize(id);
			long pendingMails = reportDao.getElectionPendingSendEmailsAmount(id);
			ElectionReport eleccionReport = new ElectionReport(electionName, usersVoted, usersNotVoted, usersTotal, pendingMails);
			election.add(eleccionReport);
		}

		return election;
	}

	/**
	 * Get a list of all the participations of an organization in all the elections.
	 * 
	 * @param org
	 * 			String containing the organization looked for.
	 * 
	 * @return returns a collection of participation entity with the information.
	 */
	@Override
	public List<Participation> getOrganizationParticipations(String org) {
		List<Participation> participations = new ArrayList<>();
		List<Election> elections = ElectionsDaoFactory.createElectionDao(em).getElectionsAllOrderStartDateDesc();
		for (Election election : elections) {
			UserVoter userVoter = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVotersByOrganization(org, election.getElectionId());
			Participation participation = new Participation();
			participation.setCategory(election.getCategory().toString());
			if (userVoter != null) {
				participation.setEmail(userVoter.getMail());
				participation.setName(userVoter.getName());
				participation.setCountry(userVoter.getCountry());
				participation.setVoted(userVoter.isVoted());
				if (election.isEnabledToVote()) {
					participation.setVoteLink(userVoter.getVoteLink());
				} else {
					participation.setVoteLink("");
				}
			} else {
				participation.setEmail("");
				participation.setName("");
				participation.setCountry("");
				participation.setVoted(false);
				participation.setVoteLink("");
			};			
			participation.setElectionEndDate(election.getEndDate());
			participation.setElectionStartDate(election.getStartDate());
			participation.setOrgId(org);			
			participation.setElectionTitleEN(election.getTitleEnglish());
			participation.setElectionTitleSP(election.getTitleSpanish());
			participation.setElectionTitlePT(election.getTitlePortuguese());			
			participation.setElectionLinkSP(election.getLinkSpanish());
			participation.setElectionLinkEN(election.getLinkEnglish());
			participation.setElectionLinkPT(election.getLinkPortuguese());

			participations.add(participation);
		}
		return participations;
	}

	/**
	 * Gets a list with the basic information of all the elections on the system orderer by date in descending order.
	 * 
	 * @return returns a collection of election light entity with the information.
	 */
	@Override
	public List<ElectionLight> getElectionsLightAllOrderStartDateDesc() {
		return ElectionsDaoFactory.createElectionDao(em).getElectionsLightAllOrderStartDateDesc();
	}

	/**
	 * Gets the authentication token to validate services invocation. It is withdrawn from the parameter WS_AUTH_TOKEN
	 * 
	 * @return returns a string with the token.
	 */
	@Override
	public String getWsAuthToken() {
		try {
			return ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_AUTH_TOKEN).getValue();
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Gets the authentication method used in web services. It is specified with the WS_AUTH_METHOD parameter
	 * 
	 * @return returns a string with the auth method.
	 */
	public String getWsAuthMethod() {
		try {
			return ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_AUTH_METHOD).getValue();
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Gets the authentication method used in web services. It is specified with the WS_AUTH_METHOD parameter
	 * 
	 * @return returns a string with the auth method.
	 */
	public String getWsLacnicAuthUrl() {
		try {
			return ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_LACNIC_AUTH_URL).getValue();
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Gets and parses the list of authorized ips from which the web services can be invoked, the are withdrawn from the parameter WS_AUTHORIZED_IPS.
	 *  
	 * @return returns a ip resource set entity with the information.
	 */
	@Override
	public IpResourceSet getWsAuthorizedIps() {
		try {
			return IpResourceSet.parse(ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_AUTHORIZED_IPS).getValue());
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	/**
	 * Gets all the rows from the elections table
	 * 
	 * @return a colection of election id y and spanish title
	 */
	@Override
	public List<TablesReportData> getElectionsData() {
		try {
			return ElectionsDaoFactory.createElectionDao(em).getElectionsData();
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	/**
	 * Get an election table row identify by id
	 * 
	 * @param id
	 * 			Identifier of the election
	 * 
	 * @return an election table report entity containing the information
	 */
	@Override
	public ElectionReportTable getElectionTableReport(Long id) {
		try {
			return ElectionsDaoFactory.createElectionDao(em).getElectionTableReport(id);
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	/**
	 * Gets all the rows from the elections table
	 * 
	 * @return a colection of activity id y and description
	 */
	@Override
	public List<TablesReportData> getActivitiesData() {
		try {
			ActivityDao activityDao = new ActivityDao(em);
			return activityDao.getActivitiesData();
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	/**
	 * Get an election table row identify by id
	 * 
	 * @param id
	 * 			Identifier of the election
	 * 
	 * @return an election table report entity containing the information
	 */
	@Override
	public ActivityReportTable getActivityTableReport(Long id) {
		try {
			ActivityDao activityDao = new ActivityDao(em);
			return activityDao.getActivityTableReport(id);
			
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

}
