package net.lacnic.elections.adminweb.ui.bases;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import net.lacnic.elections.utils.ElectionsRoles;

@AuthorizeInstantiation({ElectionsRoles.ELECTIONS_MANAGER, ElectionsRoles.ELECTIONS_STATUTARY_ONLY, ElectionsRoles.ELECTIONS_NON_STATUTARY_ONLY})
public abstract class DashboardElectionBasePage extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;

	public DashboardElectionBasePage(PageParameters params) {
		super(params, resolveElectionId(params));
		enforceElectionAccess(params);
	}

	protected void enforceElectionAccess(Election election) {
		if (election == null || SecurityUtils.canAccessElection(election)) {
			return;
		}
		String messageKey = SecurityUtils.isStatutoryOnlyUser() ? "electionAccessDeniedStatutoryOnly" : "electionAccessDeniedNonStatutaryOnly";
		SecurityUtils.error(getString(messageKey));
		throw new RestartResponseException(SecurityUtils.getHomePage());
	}

	private void enforceElectionAccess(PageParameters params) {
		if (!hasElectionContext(params)) {
			return;
		}
		Election election = resolveElection(params);
		if (election == null) {
			throw new RestartResponseException(Error404.class);
		}
		enforceElectionAccess(election);
	}

	protected Election reloadAndEnforceElectionAccess(long electionId) {
		if (electionId <= 0) {
			throw new RestartResponseException(Error404.class);
		}
		Election election = AppContext.getInstance().getManagerBeanRemote().getElectionWithRestrictedCountries(electionId);
		if (election == null) {
			throw new RestartResponseException(Error404.class);
		}
		enforceElectionAccess(election);
		return election;
	}

	private static long resolveElectionId(PageParameters params) {
		if (params == null) {
			return 0L;
		}
		Election election = resolveElection(params);
		return election != null ? election.getElectionId() : 0L;
	}

	private static boolean hasElectionContext(PageParameters params) {
		if (params == null) {
			return false;
		}
		return UtilsParameters.isId(params)
			|| UtilsParameters.getCandidateAsLong(params) > 0
			|| UtilsParameters.getUserAsLong(params) > 0
			|| UtilsParameters.getAuditAsLong(params) > 0
			|| UtilsParameters.getQuestionAsLong(params) > 0;
	}

	private static Election resolveElection(PageParameters params) {
		if (params == null) {
			return null;
		}
		if (UtilsParameters.isId(params)) {
			long electionId = UtilsParameters.getIdAsLong(params);
			if (electionId > 0) {
				return AppContext.getInstance().getManagerBeanRemote().getElection(electionId);
			}
		}

		long candidateId = UtilsParameters.getCandidateAsLong(params);
		if (candidateId > 0) {
			Candidate candidate = AppContext.getInstance().getManagerBeanRemote().getCandidate(candidateId);
			return candidate != null ? candidate.getElection() : null;
		}

		long voterId = UtilsParameters.getUserAsLong(params);
		if (voterId > 0) {
			UserVoter userVoter = AppContext.getInstance().getManagerBeanRemote().getUserVoter(voterId);
			return userVoter != null ? userVoter.getElection() : null;
		}

		long auditorId = UtilsParameters.getAuditAsLong(params);
		if (auditorId > 0) {
			Auditor auditor = AppContext.getInstance().getManagerBeanRemote().getAuditor(auditorId);
			return auditor != null ? auditor.getElection() : null;
		}

		long questionId = UtilsParameters.getQuestionAsLong(params);
		if (questionId > 0) {
			CandidateQuestion question = AppContext.getInstance().getManagerBeanRemote().getCandidateQuestion(questionId);
			return question != null ? question.getElection() : null;
		}

		return null;
	}
}
