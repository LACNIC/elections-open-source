package net.lacnic.elections.adminweb.ui.token.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTasksPanel;
import net.lacnic.elections.adminweb.ui.token.NominationOrganizationSummaryPanel;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.Organization;

public class AcceptNominationConditionsPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;
	private Organization organization;
	private List<String> restrictedCountryCodes = Collections.emptyList();
	private String electionTitle;
	private String organizationName;
	private Nomination nomination;

	public AcceptNominationConditionsPage() {
		this(new PageParameters());
	}

	public AcceptNominationConditionsPage(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedbackPanel"));
		add(new AcceptNominationPanel("acceptNominationPanel", getToken()));
		add(new AcceptNominationTasksPanel("acceptNominationTasksPanel", getToken()));
	}

	@Override
	protected Component buildTopbarContent(String id) {
		return new NominationOrganizationSummaryPanel(id, organization, restrictedCountryCodes, getToken());
	}

	@Override
	protected Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params) {
		nomination = AppContext.getInstance().getPreNominationBeanRemote().verifyAcceptNominationAccess(getToken());
		if (nomination == null) {
			return Error404.class;
		}
		if (nomination.getStatus() == NominationStatus.ACCEPTED_BY_CANDIDATE) {
			throw new RestartResponseException(GenericAcceptNominationTasksPage.class, buildTokenPageParameters());
		}
		String otherAcceptedNominationToken = findAcceptedNominationTokenForSameEmail(nomination);
		if (nomination.getStatus() != NominationStatus.REJECTED_BY_CANDIDATE && hasText(otherAcceptedNominationToken)) {
			throw new RestartResponseException(GenericAcceptNominationTasksPage.class, buildTokenPageParameters(otherAcceptedNominationToken));
		}

		organization = nomination.getOrganization();
		restrictedCountryCodes = loadRestrictedCountryCodes(nomination.getElection() != null ? nomination.getElection().getElectionId() : 0L);
		electionTitle = nomination.getElection() != null ? nomination.getElection().getTitle(SecurityUtils.getLocale().getLanguage()) : null;
		organizationName = organization != null ? organization.getName() : null;

		setElection(nomination.getElection());
		setWhereAmI("Pantalla accept nomination (pagina 2) usando Nomination.acceptNominationToken");
		setContextClass(Nomination.class.getName());
		setContextData(buildContextData(nomination));
		setHeaderUserDisplay(nomination.getNominationName());
		return null;
	}

	@Override
	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		return resolveNominationAcceptanceAccessGate(nomination);
	}

	@Override
	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionNomination");
	}

	private String buildContextData(Nomination nomination) {
		StringBuilder sb = new StringBuilder();
		sb.append("nominationId: ").append(nomination.getId()).append('\n');
		sb.append("status: ").append(nomination.getStatus()).append('\n');
		sb.append("nominationName: ").append(valueOrDash(nomination.getNominationName())).append('\n');
		sb.append("nominationEmail: ").append(valueOrDash(nomination.getNominationEmail())).append('\n');
		sb.append("organizationId: ").append(organization != null ? organization.getId() : "-").append('\n');
		sb.append("organizationName: ").append(valueOrDash(organizationName));

		Candidate candidate = nomination.getCandidate();
		if (candidate != null) {
			sb.append("\n\ncandidateId: ").append(candidate.getCandidateId());
			sb.append("\ncandidateName: ").append(valueOrDash(candidate.getName()));
			sb.append("\ncandidateEmail: ").append(valueOrDash(candidate.getMail()));
			sb.append("\ncandidateStatus: ").append(candidate.getStatus() != null ? candidate.getStatus() : "-");
			sb.append("\ncandidateOrder: ").append(candidate.getCandidateOrder());

			List<CandidateElectionTaskProgress> taskProgressList = candidate.getTaskProgress();
			if (taskProgressList != null && !taskProgressList.isEmpty()) {
				List<CandidateElectionTaskProgress> orderedTaskProgressList = new ArrayList<>(taskProgressList);
				orderedTaskProgressList.sort(Comparator.comparingLong(CandidateElectionTaskProgress::getId));
				sb.append("\n\ncandidateTaskProgress:");
				for (CandidateElectionTaskProgress taskProgress : orderedTaskProgressList) {
					sb.append("\n- progressId: ").append(taskProgress.getId());
					sb.append(", taskId: ").append(taskProgress.getElectionTask() != null ? taskProgress.getElectionTask().getId() : "-");
					sb.append(", taskKey: ").append(taskProgress.getElectionTask() != null && taskProgress.getElectionTask().getTaskKey() != null ? taskProgress.getElectionTask().getTaskKey() : "-");
					sb.append(", status: ").append(taskProgress.getStatus() != null ? taskProgress.getStatus() : "-");
					sb.append(", startDate: ").append(taskProgress.getStartDate() != null ? taskProgress.getStartDate() : "-");
					sb.append(", endDate: ").append(taskProgress.getEndDate() != null ? taskProgress.getEndDate() : "-");
				}
			}
		}

		return sb.toString();
	}

	private String valueOrDash(String value) {
		if (value == null || value.isEmpty()) {
			return "-";
		}
		return value;
	}

	private String findAcceptedNominationTokenForSameEmail(Nomination currentNomination) {
		if (currentNomination == null || currentNomination.getElection() == null) {
			return null;
		}
		String currentEmail = normalizeEmail(currentNomination.getNominationEmail());
		if (!hasText(currentEmail)) {
			return null;
		}
		List<Nomination> nominations = AppContext.getInstance().getManagerBeanRemote().getElectionNominations(currentNomination.getElection().getElectionId());
		if (nominations == null || nominations.isEmpty()) {
			return null;
		}
		for (Nomination otherNomination : nominations) {
			if (otherNomination == null
					|| otherNomination.getId() == currentNomination.getId()
					|| otherNomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE) {
				continue;
			}
			if (currentEmail.equals(normalizeEmail(otherNomination.getNominationEmail())) && hasText(otherNomination.getAcceptNominationToken())) {
				return otherNomination.getAcceptNominationToken();
			}
		}
		return null;
	}

	private PageParameters buildTokenPageParameters(String token) {
		PageParameters pageParameters = new PageParameters();
		pageParameters.add("token", token);
		return pageParameters;
	}

	private String normalizeEmail(String email) {
		if (!hasText(email)) {
			return null;
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private List<String> loadRestrictedCountryCodes(long electionId) {
		if (electionId <= 0) {
			return Collections.emptyList();
		}
		try {
			Election detailedElection = AppContext.getInstance().getManagerBeanRemote().getElectionWithRestrictedCountries(electionId);
			if (detailedElection == null || detailedElection.getRestrictedCountryCodes() == null) {
				return Collections.emptyList();
			}
			return detailedElection.getRestrictedCountryCodes();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
}
