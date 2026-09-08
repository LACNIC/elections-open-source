package net.lacnic.elections.adminweb.ui.admin.election.census;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.exception.CensusValidationException;

public class EditUserVoterDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = -4584362258132685785L;
	private UserVoter userVoter;
	private String name;
	private String mail;
	private Integer voteAmount;
	private String orgID;
	private String orgName;
	private String country;
	private String language;

	public EditUserVoterDashboard(PageParameters params) {
		super(params);

			long userVoterId = UtilsParameters.getUserAsLong(params);
			userVoter = AppContext.getInstance().getManagerBeanRemote().getUserVoter(userVoterId);
		name = userVoter.getName();
		mail = userVoter.getMail();
		voteAmount = userVoter.getVoteAmount();
		orgID = userVoter.getOrgID();
		orgName = userVoter.getOrgName();
		country = userVoter.getCountry();
		language = userVoter.getLanguage();

		add(new FeedbackPanel("feedback"));
		Form<Void> userVoterForm = new Form<>("userVoterForm");
		boolean orgIdEnabled = true;
		boolean orgIdRequired = true;
		userVoterForm.add(new AddUserVoterPanel("addUserVoterPanel", userVoter, userVoter.getElection().getElectionId(), true, orgIdEnabled, orgIdRequired));
		add(userVoterForm);

		userVoterForm.add(new Button("save") {
			private static final long serialVersionUID = -5373277597924225186L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				// Save only if something changed
				// BUG-FIX: Now redirects to the Election Dashboard in case of non-changes
				if (hasUserVoterChanges()) {
					try {
						AppContext.getInstance().getManagerBeanRemote().editUserVoter(getUserVoter(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						getSession().info(getString("censusManagementUserEditSuccess"));
						setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(userVoter.getElection().getElectionId()));
					} catch (CensusValidationException e) {
						error(getString(e.getMessage()));
					}
				} else {
					setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(userVoter.getElection().getElectionId()));
				}
			}
		});

		userVoterForm.add(new Link<Void>("cancel") {
			private static final long serialVersionUID = 8213865900998891288L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(userVoter.getElection().getElectionId()));
			}
		});
	}
	public UserVoter getUserVoter() {
		return userVoter;
	}

	public void setUserVoter(UserVoter userVoter) {
		this.userVoter = userVoter;
	}

	private boolean hasUserVoterChanges() {
		return !equalsNullableIgnoreCase(mail, userVoter.getMail())
				|| !equalsNullableIgnoreCase(name, userVoter.getName())
				|| !java.util.Objects.equals(voteAmount, userVoter.getVoteAmount())
				|| !equalsNullableIgnoreCase(orgID, userVoter.getOrgID())
				|| !equalsNullableIgnoreCase(orgName, userVoter.getOrgName())
				|| !equalsNullableIgnoreCase(country, userVoter.getCountry())
				|| !equalsNullableIgnoreCase(language, userVoter.getLanguage());
	}

	private boolean equalsNullableIgnoreCase(String left, String right) {
		String normalizedLeft = normalizeOptionalText(left);
		String normalizedRight = normalizeOptionalText(right);
		if (normalizedLeft == null || normalizedRight == null) {
			return normalizedLeft == normalizedRight;
		}
		return normalizedLeft.equalsIgnoreCase(normalizedRight);
	}

	private String normalizeOptionalText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

}
