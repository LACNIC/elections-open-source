package net.lacnic.elections.adminweb.ui.token.page;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.vote.AlreadyVotedPanel;
import net.lacnic.elections.adminweb.ui.vote.VoteJointElectionPanel;
import net.lacnic.elections.adminweb.ui.vote.VoteSimpleElectionPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.ElectionCalendar;

public class VotePublicPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;

	private UserVoter userVoter;
	private UserVoter[] userVoters;
	private boolean jointElectionView;

	public VotePublicPage() {
		this(new PageParameters());
	}

	public VotePublicPage(PageParameters params) {
		super(params);
		setLocaleFromSelectedVoterIfNeeded();
		add(buildVotePanel("votePanel", params));
	}

	private void setLocaleFromSelectedVoterIfNeeded() {
		if (hasLocaleRequestParameter() || hasManualTopbarLocaleSelection()) {
			return;
		}
		if (userVoter != null && userVoter.getLanguageEnum() != null) {
			SecurityUtils.setLocale(userVoter.getLanguageEnum().getLocaleCode());
		}
	}

	private String resolveSelectedVoteToken(PageParameters params) {
		String token1 = UtilsParameters.getParameters("token1", params);
		String token2 = UtilsParameters.getParameters("token2", params);
		if (hasText(token1)) {
			return token1;
		}
		if (hasText(token2)) {
			return token2;
		}
		return getToken();
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	public String getLanguage() {
		return resolveLanguageCode().getCode();
	}

	private LanguageCode resolveLanguageCode() {
		if (!hasLocaleRequestParameter() && !hasManualTopbarLocaleSelection() && userVoter != null && userVoter.getLanguageEnum() != null) {
			return userVoter.getLanguageEnum();
		}
		return LanguageCode.fromValueOrDefault(SecurityUtils.getLocale() != null ? SecurityUtils.getLocale().getLanguage() : null, LanguageCode.SP);
	}

	private Component buildVotePanel(String id, PageParameters params) {
		if (jointElectionView && userVoters != null && userVoters.length >= 2) {
			return new VoteJointElectionPanel(id, params, userVoters, VotePublicPage.class);
		}
		if (userVoter != null && userVoter.isVoted()) {
			return new AlreadyVotedPanel(id, userVoter);
		}
		return new VoteSimpleElectionPanel(id, userVoter, getClientIP(), VotePublicPage.class);
	}

	@Override
	protected Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params) {
		String selectedVoteToken = resolveSelectedVoteToken(params);
		userVoter = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccess(selectedVoteToken);
		if (userVoter == null) {
			return Error404.class;
		}

		setElection(userVoter.getElection());
		setWhereAmI("Pantalla /token/vote usando UserVoter.voteToken");
		setContextClass(UserVoter.class.getName());
		setContextData("userVoterId: " + userVoter.getUserVoterId() + "\n" + "nombre: " + userVoter.getName() + "\n" + "email: " + userVoter.getMail() + "\n" + "orgId: " + userVoter.getOrgID() + "\n"
				+ "voted: " + userVoter.isVoted() + "\n" + "voteAmount: " + userVoter.getVoteAmount());
		setHeaderUserDisplay(userVoter.getName());

		boolean explicitJointSubtoken = hasText(UtilsParameters.getParameters("token1", params)) || hasText(UtilsParameters.getParameters("token2", params));
		boolean simpleElection = AppContext.getInstance().getVoterBeanRemote().electionIsSimple(userVoter.getElection().getElectionId());
		jointElectionView = !simpleElection && !explicitJointSubtoken;
		if (jointElectionView) {
			userVoters = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccessJointElection(getToken());
			if (userVoters == null || userVoters.length < 2 || userVoters[0] == null || userVoters[1] == null) {
				return Error404.class;
			}
			setHeaderUserDisplay(userVoters[0].getName());
		}

		return null;
	}

	@Override
	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		if (getElection() != null && !getElection().isVotingLinkAvailable()) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.ACCESS_NOT_AVAILABLE,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_16_PERIODO_VOTING,
					null,
					null));
		}
		ElectionCalendar calendar = findPublicCalendar(ElectionCalendarKey.N_16_PERIODO_VOTING);
		if (calendar == null || calendar.getStartDate() == null) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.VOTE_NOT_OPEN,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_16_PERIODO_VOTING,
					null,
					null));
		}
		TokenAccessGate.AccessBlock before = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.VOTE_NOT_OPEN,
				PublicAccessDeniedPage.CountdownTargetDate.START,
				ElectionCalendarKey.N_16_PERIODO_VOTING,
				null,
				null);
		TokenAccessGate.AccessBlock after = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.VOTE_CLOSED,
				PublicAccessDeniedPage.CountdownTargetDate.END,
				ElectionCalendarKey.N_16_PERIODO_VOTING,
				null,
				null);
		return TokenAccessGate.of(calendar.getStartDate(), calendar.getEndDate(), before, after);
	}

	@Override
	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionVoting");
	}
}
