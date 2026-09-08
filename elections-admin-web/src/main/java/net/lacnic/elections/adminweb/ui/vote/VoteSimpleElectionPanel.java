package net.lacnic.elections.adminweb.ui.vote;

import java.util.ArrayList;
import java.util.List;

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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.error.Error500;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.LinksUtils;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class VoteSimpleElectionPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String CANDIDATE_CARD_CLASS = "card mb-3 vote-candidate-card";
	private static final String CANDIDATE_CARD_SELECTED_CLASS = "card mb-3 vote-candidate-card border-primary bg-primary-subtle shadow-sm";

	private final UserVoter userVoter;
	private final String clientIp;
	private final Class<? extends IRequestablePage> votePageClass;
	private final List<Candidate> chosenCandidates = new ArrayList<>();
	private final List<Candidate> ballotCandidates;

	private WebMarkupContainer voteButtonContainer;
	private WebMarkupContainer confirmVoteContainer;
	private WebMarkupContainer candidatesContainer;
	private FeedbackPanel feedbackPanel;

	public VoteSimpleElectionPanel(String id, UserVoter userVoter, String clientIp, Class<? extends IRequestablePage> votePageClass) {
		super(id);
		this.userVoter = userVoter;
		this.clientIp = clientIp;
		this.votePageClass = votePageClass;

		add(new VoteSimpleElectionDetailPanel("voteElectionDetail", getElection(), userVoter));

		feedbackPanel = new FeedbackPanel("feedbackPanel");
		feedbackPanel.setOutputMarkupPlaceholderTag(true);
		add(feedbackPanel);

		boolean randomOrderCandidates = getElection().isRandomOrderCandidates();
		String candidatesOrderKey = randomOrderCandidates ? "randomCandidates" : "alphabeticalCandidates";
		add(new Label("candidatesOrder", getString(candidatesOrderKey)));
		add(new Label("maxCandidates", String.valueOf(getElection().getMaxCandidates())));

		ballotCandidates = AppContext.getInstance().getManagerBeanRemote().getElectionBallotCandidates(getElection().getElectionId());

		candidatesContainer = new WebMarkupContainer("candidatesContainer");
		candidatesContainer.setOutputMarkupPlaceholderTag(true);
		add(candidatesContainer);

		ListView<Candidate> candidatesListView = new ListView<Candidate>("candidatesList", ballotCandidates) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<Candidate> item) {
				final Candidate candidate = item.getModelObject();
				WebMarkupContainer markupContainer = new WebMarkupContainer("markupContainer");
				markupContainer.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  getCandidateCardCssClass(chosenCandidates.contains(candidate))));
				markupContainer.setMarkupId("electionCandidate" + item.getIndex());
				item.add(markupContainer);

				AjaxLink<Void> candidateLink = new AjaxLink<Void>("candidateLink") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						if (chosenCandidates.contains(candidate)) {
							chosenCandidates.remove(candidate);
							markupContainer.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  getCandidateCardCssClass(false)));
						} else {
							chosenCandidates.add(candidate);
							markupContainer.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  getCandidateCardCssClass(true)));
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

				String linkText = resolveCandidateMoreInfoLink(candidate);
				ExternalLink externalLink = new ExternalLink("link", hasText(linkText) ? linkText : "#");
				externalLink.setVisible(hasText(linkText));
				markupContainer.add(externalLink);
			}
		};
		candidatesContainer.add(candidatesListView);

		initVoteActions();
	}

	private void initVoteActions() {
		voteButtonContainer = new WebMarkupContainer("voteButtonContainer");
		voteButtonContainer.setOutputMarkupPlaceholderTag(true);
		add(voteButtonContainer);

		AjaxLink<Void> vote = new AjaxLink<Void>("vote") {
			private static final long serialVersionUID = 1L;

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
		voteButtonContainer.add(vote);

		confirmVoteContainer = new WebMarkupContainer("confirmVoteContainer");
		confirmVoteContainer.setOutputMarkupPlaceholderTag(true);
		confirmVoteContainer.setVisible(false);
		add(confirmVoteContainer);

		Link<Void> confirm = new Link<Void>("confirm") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				if (isOkForVote()) {
					try {
						AppContext.getInstance().getVoterBeanRemote().vote(chosenCandidates, userVoter, clientIp);
						setResponsePage(votePageClass, UtilsParameters.getToken(userVoter.getVoteToken()));
					} catch (Exception e) {
						appLogger.error(e.getMessage(), e);
						setResponsePage(Error500.class);
					}
				}
			}
		};
		confirmVoteContainer.add(confirm);

		AjaxLink<Void> cancel = new AjaxLink<Void>("cancel") {
			private static final long serialVersionUID = 1L;

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
		confirmVoteContainer.add(cancel);
	}

	private boolean isOkForVote() {
		try {
			int configuredMaxCandidates = getElection().getMaxCandidates();
			int selectableCandidates = Math.min(configuredMaxCandidates, ballotCandidates.size());

			if (AppContext.getInstance().getVoterBeanRemote().userAlreadyVoted(userVoter.getUserVoterId())) {
				setResponsePage(votePageClass, UtilsParameters.getToken(userVoter.getVoteToken()));
				return false;
			}
			if (chosenCandidates.isEmpty()) {
				error(getString("noCandidateChosen"));
				return false;
			}
			if (chosenCandidates.size() > configuredMaxCandidates) {
				error(getString("tooManyCandidatesChosen") + configuredMaxCandidates);
				return false;
			}
			if (chosenCandidates.size() < selectableCandidates) {
				info(getString("tooLittleCandidatesChosen"));
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			error(e.getMessage());
		}
		return true;
	}

	private Election getElection() {
		return userVoter.getElection();
	}

	private String getCandidateCardCssClass(boolean selected) {
		return selected ? CANDIDATE_CARD_SELECTED_CLASS : CANDIDATE_CARD_CLASS;
	}

	private String resolveCandidateMoreInfoLink(Candidate candidate) {
		String configuredLink = candidate != null ? candidate.getLink(getLanguage()) : null;
		if (hasText(configuredLink)) {
			return configuredLink.trim();
		}
		if (shouldHideVotePagePublicProfileLink(candidate)) {
			return "";
		}
		return buildPublicCandidateProfileLink(candidate);
	}

	static boolean shouldHideVotePagePublicProfileLink(Candidate candidate) {
		return candidate != null && candidate.isAbstention();
	}

	private String buildPublicCandidateProfileLink(Candidate candidate) {
		if (candidate == null || candidate.getElection() == null || candidate.getCandidateId() <= 0) {
			return "";
		}
		String publicElectionToken = candidate.getElection().getPublicElectionToken();
		if (!hasText(publicElectionToken)) {
			return "";
		}
		return LinksUtils.buildPublicCandidateProfileLink(publicElectionToken, candidate.getCandidateId());
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String getLanguage() {
		if (userVoter != null && userVoter.getLanguageEnum() != null) {
			return userVoter.getLanguageEnum().getCode();
		}
		return LanguageCode.fromValueOrDefault(SecurityUtils.getLocale() != null ? SecurityUtils.getLocale().getLanguage() : null, LanguageCode.SP).getCode();
	}
}
