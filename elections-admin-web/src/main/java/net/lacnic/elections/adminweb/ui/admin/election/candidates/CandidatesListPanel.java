package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.ui.token.CandidateBiographyUtils;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.utils.Constants;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class CandidatesListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String NOT_APPLICABLE_LABEL = "No aplica";
	private static final String KEY_CANDIDATE_MANAGEMENT_LIST_NO_LINK = "candidateManagemenListNoLink";

	public CandidatesListPanel(String id, Election election) {
		super(id);

		try {
			List<Nomination> electionNominations = AppContext.getInstance().getManagerBeanRemote().getElectionNominations(election.getElectionId());
			List<Candidate> orderedCandidates = AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(election.getElectionId());
			final Map<Long, Nomination> nominationByCandidate = new HashMap<>();
			for (Nomination nomination : electionNominations) {
				if (nomination == null) {
					continue;
				}
				if (nomination.getCandidate() != null) {
					long candidateId = nomination.getCandidate().getCandidateId();
					nominationByCandidate.put(candidateId, nomination);
				}
			}
			final List<Candidate> regularCandidates = new ArrayList<>();
			Candidate loadedAbstentionCandidate = null;
			for (Candidate candidate : orderedCandidates) {
				if (candidate == null) {
					continue;
				}
				if (candidate.isAbstention()) {
					loadedAbstentionCandidate = candidate;
					continue;
				}
				regularCandidates.add(candidate);
			}
			final Candidate abstentionCandidate = loadedAbstentionCandidate;

			WebMarkupContainer abstentionContainer = new WebMarkupContainer("abstentionContainer");
			abstentionContainer.setVisible(abstentionCandidate != null);
			add(abstentionContainer);

			if (abstentionCandidate != null) {
				ButtonDeleteWithConfirmation removeAbstention = new ButtonDeleteWithConfirmation("removeAbstention", abstentionCandidate.getCandidateId()) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onConfirm() {
						AppContext.getInstance().getManagerBeanRemote().removeCandidate(abstentionCandidate.getCandidateId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						getSession().info(getString("candidateManagemenListSuccessDel"));
						setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
					}
				};
				removeAbstention.setMarkupId("removeAbstention");
				abstentionContainer.add(removeAbstention);

				NonCachingImage abstentionPicture = new NonCachingImage("abstentionPicture", new ImageResource(abstentionCandidate.getPictureInfo(), abstentionCandidate.getPictureExtension()));
				abstentionPicture.add(new AttributeModifier("style", "width:72px;height:72px;object-fit:cover;"));
				abstentionContainer.add(abstentionPicture);
				abstentionContainer.add(new Label("abstentionName", abstentionCandidate.getName()));
				abstentionContainer.add(new Label("abstentionCandidateId", String.valueOf(abstentionCandidate.getCandidateId())));
				abstentionContainer.add(buildBiographyLabel("abstentionBioSpanish", abstentionCandidate != null ? abstentionCandidate.getBioSpanish() : null));
				abstentionContainer.add(new Label("abstentionCandidateStatus", resolveCandidateStatus(abstentionCandidate)));
				abstentionContainer.add(new Label("abstentionPosition", resolveSortStatusLabel(abstentionCandidate, election)));

				PageParameters abstentionParameters = UtilsParameters.getId(election.getElectionId());
				abstentionParameters.add(UtilsParameters.getCandidateText(), abstentionCandidate.getCandidateId());
				abstentionContainer.add(new BookmarkablePageLink<>("styleAbstentionBiography", ManageCandidateBiographyDashboard.class, abstentionParameters));
				abstentionContainer.add(new BookmarkablePageLink<>("changeAbstentionPhoto", ManageCandidatePhotoDashboard.class, abstentionParameters));

				abstentionContainer.add(new Link<Void>("fixAbstentionTop") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						AppContext.getInstance().getManagerBeanRemote().fixCandidateToTop(abstentionCandidate.getCandidateId());
						setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
					}
				});

				abstentionContainer.add(new Link<Void>("fixAbstentionBottom") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						AppContext.getInstance().getManagerBeanRemote().fixCandidateToBottom(abstentionCandidate.getCandidateId());
						setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
					}
				});
			}

			ListView<Candidate> candidatesDataView = new ListView<Candidate>("candidatesList", regularCandidates) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(final ListItem<Candidate> item) {
					final Candidate currentCandidate = item.getModelObject();

					ButtonDeleteWithConfirmation buttonDeleteWithConfirmation = new ButtonDeleteWithConfirmation("removeCandidate", currentCandidate.getCandidateId()) {
						private static final long serialVersionUID = 542913566518615150L;

						@Override
						public void onConfirm() {
							AppContext.getInstance().getManagerBeanRemote().removeCandidate(currentCandidate.getCandidateId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
							getSession().info(getString("candidateManagemenListSuccessDel"));
							setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					buttonDeleteWithConfirmation.setMarkupId("removeCandidate" + item.getIndex());
					item.add(buttonDeleteWithConfirmation);

					WebMarkupContainer pictureCell = new WebMarkupContainer("pictureCell");
					if (currentCandidate.isWinner()) {
						pictureCell.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "table-primary"));
					}
					item.add(pictureCell);

					NonCachingImage picture = new NonCachingImage("picture", new ImageResource(currentCandidate.getPictureInfo(), currentCandidate.getPictureExtension()));
					picture.add(new AttributeModifier("style", "width:72px;height:72px;object-fit:cover;"));
					pictureCell.add(picture);

					Nomination candidateNomination = nominationByCandidate.get(currentCandidate.getCandidateId());
					Label nominationSummary = new Label("nominationSummary", buildNominationSummary(candidateNomination));
					nominationSummary.setEscapeModelStrings(false);
					item.add(nominationSummary);

					item.add(new Label("name", currentCandidate.getName()));
					item.add(new WebMarkupContainer("winnerBadge").setVisible(currentCandidate.isWinner()));
					item.add(new Label("candidateId", String.valueOf(currentCandidate.getCandidateId())));
					item.add(new Label("mail", currentCandidate.getMail()));
					item.add(buildBiographyLabel("bioSpanish", currentCandidate.getBioSpanish()));

					String linkedinUrl = currentCandidate.getLinkedinUrl();
					ExternalLink linkedinLink = new ExternalLink("linkedinLink", linkedinUrl == null ? "#" : linkedinUrl);
					linkedinLink.setVisible(hasText(linkedinUrl));
					linkedinLink.add(new Label("linkedinLinkText", linkedinUrl));
					item.add(linkedinLink);
					item.add(new Label("linkedinLinkEmpty", getString(KEY_CANDIDATE_MANAGEMENT_LIST_NO_LINK)).setVisible(!hasText(linkedinUrl)));

					PageParameters candidateParameters = UtilsParameters.getId(election.getElectionId());
					candidateParameters.add(UtilsParameters.getCandidateText(), currentCandidate.getCandidateId());
					item.add(new BookmarkablePageLink<>("manageTraining", ManageCandidateTrainingDashboard.class, candidateParameters));
					item.add(new BookmarkablePageLink<>("manageStatus", ManageCandidateStatusDashboard.class, candidateParameters));
					item.add(new BookmarkablePageLink<>("manageSupports", ManageCandidateSupportsDashboard.class, candidateParameters));

					item.add(new Label(
							"campusStatus",
							currentCandidate.getCampusCourseStatus() != null
									? currentCandidate.getCampusCourseStatus().name()
									: getString("candidateManagemenListCampusNoStatus")));
					item.add(new Label("candidateStatus", resolveCandidateStatus(currentCandidate)));

					item.add(new BookmarkablePageLink<>("viewAnswers", ViewCandidateAnswersDashboard.class, candidateParameters));
					item.add(new BookmarkablePageLink<>("styleBiography", ManageCandidateBiographyDashboard.class, candidateParameters));
					item.add(new BookmarkablePageLink<>("changePhoto", ManageCandidatePhotoDashboard.class, candidateParameters));
					item.add(new BookmarkablePageLink<>("manageTranslations", ManageCandidateTranslationsDashboard.class, candidateParameters));
					Link<Void> markWinner = new Link<Void>("markWinner") {
						private static final long serialVersionUID = 1L;

						@Override
						public void onClick() {
							boolean updated = AppContext.getInstance().getManagerBeanRemote().updateCandidateWinner(currentCandidate.getCandidateId(), true, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
							if (updated) {
								getSession().info(getString("candidateManagementWinnerMarked"));
							} else {
								getSession().warn(getString("candidateManagementWinnerUpdateError"));
							}
							setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					markWinner.setVisible(!currentCandidate.isWinner());
					item.add(markWinner);

					Link<Void> unmarkWinner = new Link<Void>("unmarkWinner") {
						private static final long serialVersionUID = 1L;

						@Override
						public void onClick() {
							boolean updated = AppContext.getInstance().getManagerBeanRemote().updateCandidateWinner(currentCandidate.getCandidateId(), false, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
							if (updated) {
								getSession().info(getString("candidateManagementWinnerUnmarked"));
							} else {
								getSession().warn(getString("candidateManagementWinnerUpdateError"));
							}
							setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					unmarkWinner.setVisible(currentCandidate.isWinner());
					item.add(unmarkWinner);

					item.add(new Link<Void>("fixTop") {
						private static final long serialVersionUID = 1827814978899855817L;

						@Override
						public void onClick() {
							AppContext.getInstance().getManagerBeanRemote().fixCandidateToTop(currentCandidate.getCandidateId());
							setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					});

					Candidate nextAboveCandidate = AppContext.getInstance().getManagerBeanRemote().getNextAboveCandidate(currentCandidate);
					Link<Void> moveUpCandidate = new Link<Void>("moveUp") {
						private static final long serialVersionUID = 2854501115609501257L;

						@Override
						public void onClick() {
							AppContext.getInstance().getManagerBeanRemote().moveCandidateUp(currentCandidate.getCandidateId());
							setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					moveUpCandidate.setVisible(!election.isRandomOrderCandidates() && !currentCandidate.isFixed() && nextAboveCandidate != null && nextAboveCandidate.getCandidateOrder() != Constants.MAX_ORDER);
					item.add(moveUpCandidate);

					Candidate nextBelowCandidate = AppContext.getInstance().getManagerBeanRemote().getNextBelowCandidate(currentCandidate);
					Link<Void> moveDownCandidate = new Link<Void>("moveDown") {
						private static final long serialVersionUID = 86645897213313910L;

						@Override
						public void onClick() {
							AppContext.getInstance().getManagerBeanRemote().moveCandidateDown(currentCandidate.getCandidateId());
							setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					moveDownCandidate.setVisible(!election.isRandomOrderCandidates() && !currentCandidate.isFixed() && nextBelowCandidate != null && nextBelowCandidate.getCandidateOrder() != Constants.MIN_ORDER);
					item.add(moveDownCandidate);

					item.add(new Link<Void>("fixBottom") {
						private static final long serialVersionUID = -4002690678342538186L;

						@Override
						public void onClick() {
							AppContext.getInstance().getManagerBeanRemote().fixCandidateToBottom(currentCandidate.getCandidateId());
							setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					});

					Label label = buildSortStatusLabel("sortStatus", currentCandidate, election, item.getIndex());
					pictureCell.add(label);

					Link<Void> eliminarFijar = new Link<Void>("removeFixed") {
						private static final long serialVersionUID = 3876001444790583642L;

						@Override
						public void onClick() {
							AppContext.getInstance().getManagerBeanRemote().fixCandidateToFirstNonFixed(currentCandidate.getCandidateId());
							setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					eliminarFijar.setMarkupId("removeFixed" + item.getIndex());
					eliminarFijar.setVisible(currentCandidate.getCandidateOrder() == Constants.MAX_ORDER || currentCandidate.getCandidateOrder() == Constants.MIN_ORDER);
					pictureCell.add(eliminarFijar);

				}
			};
			add(candidatesDataView);

		} catch (Exception e) {
			appLogger.error("Error building candidates list panel. electionId={}", election.getElectionId(), e);
			throw new IllegalStateException("Could not build candidates list panel", e);
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private Label buildBiographyLabel(String id, String biography) {
		Label label = new Label(id, valueOrDash(CandidateBiographyUtils.toRenderableMarkup(biography)));
		label.setEscapeModelStrings(false);
		return label;
	}

	private String buildNominationSummary(Nomination nomination) {
		if (nomination == null) {
			return escaped(valueOrDash(getString("candidateManagemenListNoNomination")));
		}
		StringBuilder summary = new StringBuilder(512);
		appendNominationSummaryLine(summary, getString("candidateManagemenListNominationSummaryId"), String.valueOf(nomination.getId()));
		appendNominationSummaryLine(summary, getString("candidateManagemenListNominationSummaryStatus"), nomination.getStatus() != null ? nomination.getStatus().name() : "-");
		appendNominationSummaryLine(summary, getString("candidateManagemenListNominationSummaryDate"), valueOrDash(nomination.getNominationDate()));
		appendNominationSummaryLine(summary, getString("candidateManagemenListNominationSummaryOrg"), buildOrganizationSummary(nomination));
		appendNominationSummaryLine(summary, getString("candidateManagemenListNominationSummaryName"), valueOrDash(nomination.getNominationName()));
		appendNominationSummaryLine(summary, getString("candidateManagemenListNominationSummaryEmail"), valueOrDash(nomination.getNominationEmail()));
		appendNominationSummaryLine(summary, getString("candidateManagemenListNominationSummaryReason"), valueOrDash(nomination.getNominationReason(SecurityUtils.getLocale().getLanguage())));
		return summary.toString();
	}

	private String buildOrganizationSummary(Nomination nomination) {
		if (nomination == null || nomination.getOrganization() == null) {
			return "-";
		}
		String orgId = valueOrDash(nomination.getOrganization().getOrgId());
		String orgName = valueOrDash(nomination.getOrganization().getName());
		return orgId + " - " + orgName;
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private String resolveCandidateStatus(Candidate candidate) {
		if (candidate == null || candidate.getStatus() == null) {
			return "-";
		}
		if (candidate.getStatus() == CandidateStatus.PRECOMPLETE) {
			return "PRECOMPLETE";
		}
		return candidate.getStatus().name();
	}

	private Label buildSortStatusLabel(String id, Candidate candidate, Election election, int itemIndex) {
		String labelText = resolveSortStatusLabel(candidate, election, itemIndex);
		Label label = new Label(id, labelText);
		if (candidate != null && candidate.getCandidateOrder() == Constants.MAX_ORDER) {
			label.add(new AttributeModifier(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "label label-success"));
		} else if (candidate != null && candidate.getCandidateOrder() == Constants.MIN_ORDER) {
			label.add(new AttributeModifier(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "label label-danger"));
		}
		return label;
	}

	private String resolveSortStatusLabel(Candidate candidate, Election election) {
		return resolveSortStatusLabel(candidate, election, 0);
	}

	private String resolveSortStatusLabel(Candidate candidate, Election election, int itemIndex) {
		if (candidate == null) {
			return "-";
		}
		int candidateOrder = candidate.getCandidateOrder();
		if (candidateOrder == Constants.MAX_ORDER) {
			return getString("candidateManagemenListPosFixedFirst");
		}
		if (candidateOrder == Constants.MIN_ORDER) {
			return getString("candidateManagemenListPosFixedLast");
		}
		if (election != null && election.isRandomOrderCandidates()) {
			return getString("candidateManagemenListPosRandom");
		}
		return getString("candidateManagemenListPosFixedPos") + (itemIndex + 1);
	}

	private void appendNominationSummaryLine(StringBuilder out, String label, String value) {
		out.append("<div><strong>")
				.append(escaped(label))
				.append(":</strong> ")
				.append(escaped(value))
				.append("</div>");
	}

	private String escaped(String value) {
		return Strings.escapeMarkup(valueOrDash(value)).toString();
	}

}
