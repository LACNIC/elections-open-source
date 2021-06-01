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
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionLight;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.ejb.ElectionsMonitorEJB;
import net.lacnic.elections.ejb.commons.impl.AutomaticProcesses;
import net.lacnic.elections.utils.Constants;
import net.ripe.ipresource.IpResourceSet;


@Stateless
@Remote(ElectionsMonitorEJB.class)
public class ElectionsMonitorEJBBean implements ElectionsMonitorEJB {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	
	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	private static HealthCheck healthCheck;


	public ElectionsMonitorEJBBean() { }


	@Override
	public HealthCheck getHealthCheckData() {
		if (healthCheck == null)
			healthCheck = updateHealthCheckData();
		return healthCheck;
	}

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


	@Override
	public List<ElectionLight> getElectionsLightAllOrderStartDateDesc() {
		return ElectionsDaoFactory.createElectionDao(em).getElectionsLightAllOrderStartDateDesc();
	}
	
	@Override
	public String getWsAuthToken() {
		try {
			return ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_AUTH_TOKEN).getValue();
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	@Override
	public IpResourceSet getWsAuthorizedIps() {
		try {
			return IpResourceSet.parse(ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_AUTHORIZED_IPS).getValue());
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

}
