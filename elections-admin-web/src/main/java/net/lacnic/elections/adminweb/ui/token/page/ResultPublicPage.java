package net.lacnic.elections.adminweb.ui.token.page;

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.results.PublicResultsPanel;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;

public class ResultPublicPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;

	private Election election;

	public ResultPublicPage() {
		this(new PageParameters());
	}

	public ResultPublicPage(PageParameters params) {
		super(params);
		add(buildResultsPanel("resultsContent"));
	}

	private Component buildResultsPanel(String id) {
		return new PublicResultsPanel(id, election, getLanguage());
	}

	@Override
	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		if (election != null && !election.isResultLinkAvailable()) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.ACCESS_NOT_AVAILABLE,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED,
					null,
					null));
		}
		ElectionCalendar calendar = findPublicCalendar(ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED);
		if (calendar == null || calendar.getStartDate() == null) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.RESULTS_NOT_PUBLIC,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED,
					null,
					null));
		}
		TokenAccessGate.AccessBlock before = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.RESULTS_NOT_PUBLIC,
				PublicAccessDeniedPage.CountdownTargetDate.START,
				ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED,
				null,
				null);
		return TokenAccessGate.of(calendar.getStartDate(), null, before, null);
	}

	public String getLanguage() {
		if (SecurityUtils.getLocale() != null && SecurityUtils.getLocale().getLanguage() != null) {
			return SecurityUtils.getLocale().getLanguage();
		}
		return "es";
	}

	@Override
	protected Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params) {
		election = AppContext.getInstance().getVoterBeanRemote().verifyResultAccess(getToken());
		if (election == null) {
			return Error404.class;
		}

		setElection(election);
		setWhereAmI("Pantalla /token/result usando Election.resultToken");
		setContextClass(Election.class.getName());
		setContextData("electionId: " + election.getElectionId() + "\n" + "titleSpanish: " + election.getTitleSpanish() + "\n" + "resultLinkAvailable: " + election.isResultLinkAvailable());
		setHeaderUserDisplay("Resultados publicos");
		return null;
	}

	@Override
	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionResults");
	}
}
