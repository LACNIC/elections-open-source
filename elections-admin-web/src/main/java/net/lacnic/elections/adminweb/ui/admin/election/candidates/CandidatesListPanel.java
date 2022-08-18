package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.ui.components.OnOffSwitch;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.utils.Constants;


public class CandidatesListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public CandidatesListPanel(String id, Election election) {
		super(id);

		try {
			add(new OnOffSwitch("randomOrder", new PropertyModel<>(election, "randomOrderCandidates")) {
				private static final long serialVersionUID = -4511634522624852162L;

				@Override
				protected void action() {
					AppContext.getInstance().getManagerBeanRemote().setSortCandidatesRandomly(election.getElectionId(), election.isRandomOrderCandidates());
					setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
				}
			});

			ListView<Candidate> candidatesDataView = new ListView<Candidate>("candidatesList", AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(election.getElectionId())) {
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

					item.add(new NonCachingImage("picture", new ImageResource(currentCandidate.getPictureInfo(), currentCandidate.getPictureExtension())));

					item.add(new Label("name", currentCandidate.getName()));

					item.add(new Label("mail", currentCandidate.getMail()));

					Label bio = new Label("bio", currentCandidate.getBioSpanish());
					bio.setEscapeModelStrings(false);
					item.add(bio);

					String linkText = currentCandidate.getLinkSpanish();
					ExternalLink externalLink = new ExternalLink("externalLink", linkText);
					externalLink.setVisible(linkText != null && !linkText.isEmpty());
					externalLink.add(new Label("externalLinkText", linkText));
					item.add(externalLink);

					Link<Void> editar = new Link<Void>("edit") {
						private static final long serialVersionUID = 8751663222625085852L;

						@Override
						public void onClick() {
							setResponsePage(EditCandidateDashboard.class, UtilsParameters.getCandidate(currentCandidate.getCandidateId()));
						}
					};
					editar.setMarkupId("editCandidate" + item.getIndex());
					item.add(editar);

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

					int candidateOrder = currentCandidate.getCandidateOrder();
					Label label;
					if (candidateOrder == Constants.MAX_ORDER) {
						label = new Label("sortStatus", getString("candidateManagemenListPosFixedFirst"));
						label.add(new AttributeModifier("class", "label label-success"));
					} else if (candidateOrder == Constants.MIN_ORDER) {
						label = new Label("sortStatus", getString("candidateManagemenListPosFixedLast"));
						label.add(new AttributeModifier("class", "label label-danger"));
					} else {
						if (election.isRandomOrderCandidates()) {
							label = new Label("sortStatus", getString("candidateManagemenListPosRandom"));
						} else {
							label = new Label("sortStatus", getString("candidateManagemenListPosFixedPos") + (item.getIndex() + 1));
						}
					}
					item.add(label);

					Link<Void> eliminarFijar = new Link<Void>("removeFixed") {
						private static final long serialVersionUID = 3876001444790583642L;

						@Override
						public void onClick() {
							AppContext.getInstance().getManagerBeanRemote().fixCandidateToFirstNonFixed(currentCandidate.getCandidateId());
							setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					eliminarFijar.setMarkupId("removeFixed" + item.getIndex());
					eliminarFijar.setVisible(candidateOrder == Constants.MAX_ORDER || candidateOrder == Constants.MIN_ORDER);
					item.add(eliminarFijar);

				}
			};
			add(candidatesDataView);

		} catch (Exception e) {
			appLogger.error(e);
		}
	}
}
