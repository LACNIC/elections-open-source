package net.lacnic.elections.adminweb.ui.vote;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.error.Error500;
import net.lacnic.elections.adminweb.ui.error.ErrorVoteNotStarted;
import net.lacnic.elections.adminweb.ui.error.ErrorVoteNotPublic;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.UserVoter;

public class VoteSimpleElectionDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = -867241975964848115L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private UserVoter userVoter;
	private List<Candidate> chosenCandidates;
	private WebMarkupContainer voteButtonContainer;
	private WebMarkupContainer confirmVoteContainer;
	private WebMarkupContainer candidatesContainer;
	private FeedbackPanel feedbackPanel;


	public VoteSimpleElectionDashboard(PageParameters params) {
		super(params);
		if (userVoter.isVoted()) {
			setResponsePage(AlreadyVotedDashboard.class, params);
		} else {
			setLocale();
			setChosenCandidates(new ArrayList<>());
			add(new VoteSimpleElectionDetailPanel("voteElectionDetail", getElection(), userVoter));

			feedbackPanel = new FeedbackPanel("feedbackPanel");
			feedbackPanel.setOutputMarkupPlaceholderTag(true);
			add(feedbackPanel);

			add(new Label("randomCandidates", getString("randomCandidates")).setVisible(getElection().isRandomOrderCandidates()));
			add(new Label("maxCandidates", String.valueOf(getElection().getMaxCandidates())));

			candidatesContainer = new WebMarkupContainer("candidatesContainer");
			candidatesContainer.setOutputMarkupPlaceholderTag(true);
			add(candidatesContainer);

			ListView<Candidate> candidatesListView = new ListView<Candidate>("candidatesList", AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(getElection().getElectionId())) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(final ListItem<Candidate> item) {
					final Candidate candidate = item.getModelObject();
					WebMarkupContainer markupContainer = new WebMarkupContainer("markupContainer");
					if (!chosenCandidates.contains(candidate)) {
						markupContainer.add(new AttributeModifier("class", "candidate-box-white"));
					} else {
						markupContainer.add(new AttributeModifier("class", "candidate-box-green"));
					}
					markupContainer.setMarkupId("electionCandidate" + item.getIndex());
					item.add(markupContainer);

					AjaxLink<Void> candidateLink = new AjaxLink<Void>("candidateLink") {
						private static final long serialVersionUID = -3578083844361956035L;

						@Override
						public void onClick(AjaxRequestTarget target) {
							if (chosenCandidates.contains(candidate)) {
								markupContainer.add(new AttributeModifier("class", "candidate-box-white"));
								chosenCandidates.remove(candidate);
							} else {
								markupContainer.add(new AttributeModifier("class", "candidate-box-green"));
								chosenCandidates.add(candidate);
							}
							target.add(item);
						}
					};
					markupContainer.add(candidateLink);

					candidateLink.add(new Label("name", candidate.getName()));
					Label bio = new Label("bio", candidate.getBio(getLanguage()));
					bio.setEscapeModelStrings(false);
					candidateLink.add(bio);
					candidateLink.add(new NonCachingImage("image", new ImageResource(candidate.getPictureInfo(), candidate.getPictureExtension())));
					item.setOutputMarkupId(true);

					String linkText = candidate.getLink(getLanguage());
					ExternalLink externalLink = new ExternalLink("link", linkText);
					externalLink.setVisible(linkText != null && !linkText.isEmpty());
					markupContainer.add(externalLink);
				}
			};
			candidatesContainer.add(candidatesListView);
			initBoton();
		}
	}

	@Override
	public Class validateToken(PageParameters params) {
		String token1 = params.get("token1").toString();
		String token2 = params.get("token2").toString();

		if ((token1 == null || token1.equals("")) && (token2 == null || token2.equals(""))) {
			userVoter = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccess(getToken());
		} else {
			if (token1 == null || token1.equals(""))
				userVoter = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccess(token2);
			else
				userVoter = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccess(token1);
		}

		if (userVoter == null) {
			AppContext.getInstance().getVoterBeanRemote().saveFailedAccessIp(getIP());
			return Error404.class;
		} else {
			setElection(userVoter.getElection());
			if (!getElection().isStarted()) {
				setResponsePage(ErrorVoteNotStarted.class, getPageParameters());
			} else if (!getElection().isEnabledToVote()) {
				return ErrorVoteNotPublic.class;
			}
		}
		return null;
	}

	public void initBoton() {
		voteButtonContainer = new WebMarkupContainer("voteButtonContainer");
		voteButtonContainer.setOutputMarkupPlaceholderTag(true);
		add(voteButtonContainer);

		AjaxLink<Void> preguntar = new AjaxLink<Void>("vote") {
			private static final long serialVersionUID = -1763298278543164951L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (isOkForVote()) {
					confirmVoteContainer.setVisible(true);
					voteButtonContainer.setVisible(false);
					candidatesContainer.setEnabled(false);
					target.add(confirmVoteContainer);
					target.add(voteButtonContainer);
					target.add(candidatesContainer);
				}
				target.add(feedbackPanel);

			}
		};
		voteButtonContainer.add(preguntar);

		confirmVoteContainer = new WebMarkupContainer("confirmVoteContainer");
		confirmVoteContainer.setOutputMarkupPlaceholderTag(true);
		confirmVoteContainer.setVisible(false);
		add(confirmVoteContainer);

		Link<Void> confirmar = new Link<Void>("confirm") {
			private static final long serialVersionUID = 1572281088958125191L;

			@Override
			public void onClick() {
				if (isOkForVote()) {
					try {
						AppContext.getInstance().getVoterBeanRemote().vote(getChosenCandidates(), userVoter, getIP());
						setResponsePage(VoteDashboard.class, UtilsParameters.getToken(userVoter.getVoteToken()));
					} catch (Exception e) {
						appLogger.error(e);
						setResponsePage(Error500.class);
					}
				}
			}
		};
		confirmVoteContainer.add(confirmar);

		AjaxLink<Void> cancelar = new AjaxLink<Void>("cancel") {
			private static final long serialVersionUID = 7304601865499665430L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				confirmVoteContainer.setVisible(false);
				voteButtonContainer.setVisible(true);
				candidatesContainer.setEnabled(true);
				target.add(confirmVoteContainer);
				target.add(voteButtonContainer);
				target.add(candidatesContainer);
			}
		};
		confirmVoteContainer.add(cancelar);
	}

	private boolean isOkForVote() {
		try {
			if (AppContext.getInstance().getVoterBeanRemote().userAlreadyVoted(userVoter.getUserVoterId())) {
				setResponsePage(new AlreadyVotedDashboard(UtilsParameters.getToken(userVoter.getVoteToken())));
				return false;
			} else {
				if (chosenCandidates.isEmpty()) {
					error(getString("noCandidateChosen"));
					return false;
				} else {
					if (chosenCandidates.size() > getElection().getMaxCandidates()) {
						error(getString("tooManyCandidatesChosen") + getElection().getMaxCandidates());
						return false;
					}
				}
			}
		} catch (Exception e) {
			appLogger.error(e);
			error(e.getMessage());
		}
		return true;
	}

	private void setLocale() {
		if (getLanguage().toLowerCase().contains("en") || getLanguage().toLowerCase().contains("english"))
			SecurityUtils.setLocale("EN");
		else if (getLanguage().toLowerCase().contains("pt") || getLanguage().toLowerCase().contains("portuguese"))
			SecurityUtils.setLocale("PT");
		else
			SecurityUtils.setLocale("ES");
	}

	@Override
	public String getLanguage() {
		if (userVoter != null)
			return userVoter.getLanguage();
		else
			return SecurityUtils.getLocale().getDisplayName();
	}

	public List<Candidate> getChosenCandidates() {
		return chosenCandidates;
	}

	public void setChosenCandidates(List<Candidate> chosenCandidates) {
		this.chosenCandidates = chosenCandidates;
	}

}
