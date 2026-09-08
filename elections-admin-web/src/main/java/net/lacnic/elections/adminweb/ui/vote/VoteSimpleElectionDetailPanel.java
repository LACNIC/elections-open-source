package net.lacnic.elections.adminweb.ui.vote;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.WebMarkupContainer;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.UserVoter;


public class VoteSimpleElectionDetailPanel extends Panel {

	private static final long serialVersionUID = -316823064515653219L;


	public VoteSimpleElectionDetailPanel(String id, Election election, UserVoter userVoter) {
		super(id);

		add(new Label("title", election.getTitle(getLanguage(userVoter))));
		add(new Label("voter", userVoter != null ? userVoter.getName() : ""));
		String complementaryInfo = buildComplementaryInfo(userVoter);
		WebMarkupContainer complementaryInfoContainer = new WebMarkupContainer("complementaryInfoContainer");
		complementaryInfoContainer.setVisible(hasText(complementaryInfo));
		complementaryInfoContainer.add(new Label("complementaryInfo", complementaryInfo));
		add(complementaryInfoContainer);
		add(new Label("voteAmount", userVoter.getVoteAmount()));

		add(new Label("maxCandidates", String.valueOf(election.getMaxCandidates())));
		Label desc = new Label("description", election.getDescription(getLanguage(userVoter)));
		desc.setEscapeModelStrings(false);
		add(desc);
	}

	private String buildComplementaryInfo(UserVoter userVoter) {
		if (userVoter == null) {
			return "";
		}
		String orgId = hasText(userVoter.getOrgID()) ? userVoter.getOrgID().trim() : "";
		String orgName = hasText(userVoter.getOrgName()) ? userVoter.getOrgName().trim() : "";
		if (hasText(orgId) && hasText(orgName)) {
			return orgId + " - " + orgName;
		}
		if (hasText(orgId)) {
			return orgId;
		}
		return orgName;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	public String getLanguage(UserVoter userVoter) {
		if (userVoter != null && userVoter.getLanguageEnum() != null) {
			return userVoter.getLanguageEnum().getCode();
		}
		return LanguageCode.fromValueOrDefault(SecurityUtils.getLocale() != null ? SecurityUtils.getLocale().getLanguage() : null, LanguageCode.SP).getCode();
	}

}
