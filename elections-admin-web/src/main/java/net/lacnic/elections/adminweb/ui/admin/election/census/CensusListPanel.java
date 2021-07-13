package net.lacnic.elections.adminweb.ui.admin.election.census;

import java.io.File;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.ui.components.ButtonResendVoteEmail;
import net.lacnic.elections.adminweb.ui.components.ButtonUpdateToken;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.LinksUtils;


public class CensusListPanel extends Panel {

	private static final long serialVersionUID = -1239455534678268981L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private List<UserVoter> userVoters;
	private File censusFile;


	public CensusListPanel(String id, Election election) {
		super(id);
		userVoters = AppContext.getInstance().getManagerBeanRemote().getElectionUserVoters(election.getElectionId());

		add(new Label("censusSize", String.valueOf(userVoters.size())));

		DownloadLink downloadLink = new DownloadLink("exportCensus", new PropertyModel<>(CensusListPanel.this, "censusFile")) {
			private static final long serialVersionUID = 5415706945162526592L;

			@Override
			public void onClick() {
				setCensusFile(AppContext.getInstance().getManagerBeanRemote().exportCensus(election.getElectionId()));
				super.onClick();
			}
		};
		add(downloadLink);
		downloadLink.setVisible(election.isElectorsSet());

		final ListView<UserVoter> userVotersDataView = new ListView<UserVoter>("userVotersList", userVoters) {
			private static final long serialVersionUID = 1786359392545666490L;

			@Override
			protected void populateItem(final ListItem<UserVoter> item) {
				try {
					final UserVoter currentUser = item.getModelObject();

					ButtonDeleteWithConfirmation buttonDeleteWithConfirmation = new ButtonDeleteWithConfirmation("removeUser", item.getIndex()) {
						private static final long serialVersionUID = -6583106894827434879L;

						@Override
						public void onConfirm() {
							try {
								AppContext.getInstance().getManagerBeanRemote().removeUserVoter(currentUser, election.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
								getSession().info(getString("censusManagementUserListDeleteSuccess"));
								setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
							} catch (Exception e) {
								appLogger.error(e);
							}
						}
					};
					buttonDeleteWithConfirmation.setMarkupId("removeUser" + item.getIndex());
					item.add(buttonDeleteWithConfirmation);

					item.add(new Label("language", currentUser.getLanguage()));
					item.add(new Label("name", currentUser.getName()));
					item.add(new Label("mail", currentUser.getMail()));
					item.add(new Label("voteAmount", String.valueOf(currentUser.getVoteAmount())));
					item.add(new Label("country", currentUser.getCountry()));
					item.add(new Label("orgId", currentUser.getOrgID()));
					item.add(new Label("voted", (currentUser.isVoted() ? getString("censusManagementUserListColVotedYes") : getString("censusManagementUserListColVotedNo"))));
					String voteLinkText = LinksUtils.buildVoteLink(currentUser.getVoteToken());
					Label voteLinkTextLabel = new Label("voteLinkText", voteLinkText);
					ExternalLink voteLink = new ExternalLink("voteLink", voteLinkText);
					voteLink.add(voteLinkTextLabel);
					item.add(voteLink);

					ButtonUpdateToken buttonUpdateToken = new ButtonUpdateToken("updateToken") {
						private static final long serialVersionUID = 3609140813722818708L;

						@Override
						public void onConfirm() {
							try {
								AppContext.getInstance().getManagerBeanRemote().updateUserVoterToken(currentUser.getUserVoterId(), currentUser.getName(), election.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
								getSession().info(getString("censusManagementUserListTokenSuccess"));
								setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
							} catch (Exception e) {
								appLogger.error(e);
							}
						}
					};
					item.add(buttonUpdateToken);

					ButtonResendVoteEmail buttonResendVoteEmail = new ButtonResendVoteEmail("resendLink") {
						private static final long serialVersionUID = -4628772989608517427L;

						@Override
						public void onConfirm() {
							try {
								AppContext.getInstance().getManagerBeanRemote().resendUserVoterElectionMail(currentUser, election, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
								getSession().info(getString("censusManagementUserListLinkSuccess"));
								setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
							} catch (Exception e) {
								appLogger.error(e);
							}
						}
					};
					item.add(buttonResendVoteEmail);

					Link<Void> editUserVoterLink = new Link<Void>("editUserVoter") {
						private static final long serialVersionUID = 8268292966477896858L;

						@Override
						public void onClick() {
							setResponsePage(EditUserVoterDashboard.class, UtilsParameters.getUser(currentUser.getUserVoterId()));
						}
					};
					editUserVoterLink.setMarkupId("editUser" + item.getIndex());
					item.add(editUserVoterLink);

				} catch (Exception e) {
					appLogger.error(e);
				}
			}
		};
		add(userVotersDataView);
	}

	public File getCensusFile() {
		return censusFile;
	}

	public void setCensusFile(File censusFile) {
		this.censusFile = censusFile;
	}

}
