package net.lacnic.elections.adminweb.ui.admin.election.configuration;

import java.io.File;

import jakarta.servlet.ServletContext;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.components.ButtonCloseElection;
import net.lacnic.elections.adminweb.ui.components.OnOffSwitch;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;


public class ElectionConfigurationDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1392182581021963077L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String KEY_CONF_ELEC_TOGGLE_UPDATED = "confElecToggleUpdated";
	private static final String KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR = "confElecToggleUpdateError";


	public ElectionConfigurationDashboard(PageParameters params) {
		super(params);
		try {
			ServletContext context = ((WebApplication) WebApplication.get()).getServletContext();
			String filePath = context.getRealPath("/");

			FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
			feedbackPanel.setOutputMarkupId(true);
			feedbackPanel.setOutputMarkupPlaceholderTag(true);
			add(feedbackPanel);

			long electionId = UtilsParameters.getIdAsLong(params);
			Election election = AppContext.getInstance().getManagerBeanRemote().getElection(electionId);

			add(new Label("electionTitle", election.getTitle(getLanguage())));

			OnOffSwitch votingLinkAvailable = new OnOffSwitch("votingLinkAvailable", new PropertyModel<>(election, "votingLinkAvailable")) {
				private static final long serialVersionUID = -8757419944224081522L;

				@Override
				protected void action() {
					boolean requestedStatus = election.isVotingLinkAvailable();
					try {
						AppContext.getInstance().getManagerBeanRemote().setVoteLinkStatus(electionId, requestedStatus, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						Election updatedElection = reloadElection(electionId, election);
						if (updatedElection != null && updatedElection.isVotingLinkAvailable() == requestedStatus) {
							getSession().success(getString(KEY_CONF_ELEC_TOGGLE_UPDATED));
						} else {
							getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
						}
					} catch (Exception e) {
						appLogger.error("Error updating vote link availability. electionId={}", electionId, e);
						if (reloadElection(electionId, election) == null) {
							election.setVotingLinkAvailable(!requestedStatus);
						}
						getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
					}
					refreshToggleFeedback(feedbackPanel);
				}
			};
			votingLinkAvailable.setOutputMarkupId(true);
			add(votingLinkAvailable);

			OnOffSwitch resultLinkAvailable = new OnOffSwitch("resultLinkAvailable", new PropertyModel<>(election, "resultLinkAvailable")) {
				private static final long serialVersionUID = -3214185498258791153L;

				@Override
				protected void action() {
					boolean requestedStatus = election.isResultLinkAvailable();
					try {
						AppContext.getInstance().getManagerBeanRemote().setResultsLinkStatus(electionId, requestedStatus, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						Election updatedElection = reloadElection(electionId, election);
						if (updatedElection != null && updatedElection.isResultLinkAvailable() == requestedStatus) {
							getSession().success(getString(KEY_CONF_ELEC_TOGGLE_UPDATED));
						} else {
							getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
						}
					} catch (Exception e) {
						appLogger.error("Error updating results link availability. electionId={}", electionId, e);
						if (reloadElection(electionId, election) == null) {
							election.setResultLinkAvailable(!requestedStatus);
						}
						getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
					}
					refreshToggleFeedback(feedbackPanel);
				}
			};
			resultLinkAvailable.setOutputMarkupId(true);
			add(resultLinkAvailable);

			OnOffSwitch auditorLinkAvailable = new OnOffSwitch("auditorLinkAvailable", new PropertyModel<>(election, "auditorLinkAvailable")) {
				private static final long serialVersionUID = -2218718810528752527L;

				@Override
				protected void action() {
					boolean requestedStatus = election.isAuditorLinkAvailable();
					try {
						AppContext.getInstance().getManagerBeanRemote().setAuditLinkStatus(electionId, requestedStatus, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						Election updatedElection = reloadElection(electionId, election);
						if (updatedElection != null && updatedElection.isAuditorLinkAvailable() == requestedStatus) {
							getSession().success(getString(KEY_CONF_ELEC_TOGGLE_UPDATED));
						} else {
							getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
						}
					} catch (Exception e) {
						appLogger.error("Error updating audit link availability. electionId={}", electionId, e);
						if (reloadElection(electionId, election) == null) {
							election.setAuditorLinkAvailable(!requestedStatus);
						}
						getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
					}
					refreshToggleFeedback(feedbackPanel);
				}
			};
			auditorLinkAvailable.setOutputMarkupId(true);
			add(auditorLinkAvailable);

			OnOffSwitch doNominationLinkAvailable = new OnOffSwitch("doNominationLinkAvailable", new PropertyModel<>(election, "doNominationLinkAvailable")) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void action() {
					boolean requestedStatus = election.isDoNominationLinkAvailable();
					try {
						AppContext.getInstance().getManagerBeanRemote().setDoNominationLinkStatus(electionId, requestedStatus, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						Election updatedElection = reloadElection(electionId, election);
						if (updatedElection != null && updatedElection.isDoNominationLinkAvailable() == requestedStatus) {
							getSession().success(getString(KEY_CONF_ELEC_TOGGLE_UPDATED));
						} else {
							getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
						}
					} catch (Exception e) {
						appLogger.error("Error updating nomination link availability. electionId={}", electionId, e);
						if (reloadElection(electionId, election) == null) {
							election.setDoNominationLinkAvailable(!requestedStatus);
						}
						getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
					}
					refreshToggleFeedback(feedbackPanel);
				}
			};
			doNominationLinkAvailable.setOutputMarkupId(true);
			add(doNominationLinkAvailable);

			OnOffSwitch nominationTasksLinkAvailable = new OnOffSwitch("nominationTasksLinkAvailable", new PropertyModel<>(election, "nominationTasksLinkAvailable")) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void action() {
					boolean requestedStatus = election.isNominationTasksLinkAvailable();
					try {
						AppContext.getInstance().getManagerBeanRemote().setNominationTasksLinkStatus(electionId, requestedStatus, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						Election updatedElection = reloadElection(electionId, election);
						if (updatedElection != null && updatedElection.isNominationTasksLinkAvailable() == requestedStatus) {
							getSession().success(getString(KEY_CONF_ELEC_TOGGLE_UPDATED));
						} else {
							getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
						}
					} catch (Exception e) {
						appLogger.error("Error updating nomination tasks link availability. electionId={}", electionId, e);
						if (reloadElection(electionId, election) == null) {
							election.setNominationTasksLinkAvailable(!requestedStatus);
						}
						getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
					}
					refreshToggleFeedback(feedbackPanel);
				}
			};
			nominationTasksLinkAvailable.setOutputMarkupId(true);
			add(nominationTasksLinkAvailable);

			OnOffSwitch nominationSupportLinkAvailable = new OnOffSwitch("nominationSupportLinkAvailable", new PropertyModel<>(election, "nominationSupportLinkAvailable")) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void action() {
					boolean requestedStatus = election.isNominationSupportLinkAvailable();
					try {
						AppContext.getInstance().getManagerBeanRemote().setNominationSupportLinkStatus(electionId, requestedStatus, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						Election updatedElection = reloadElection(electionId, election);
						if (updatedElection != null && updatedElection.isNominationSupportLinkAvailable() == requestedStatus) {
							getSession().success(getString(KEY_CONF_ELEC_TOGGLE_UPDATED));
						} else {
							getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
						}
					} catch (Exception e) {
						appLogger.error("Error updating nomination support link availability. electionId={}", electionId, e);
						if (reloadElection(electionId, election) == null) {
							election.setNominationSupportLinkAvailable(!requestedStatus);
						}
						getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
					}
					refreshToggleFeedback(feedbackPanel);
				}
			};
			nominationSupportLinkAvailable.setOutputMarkupId(true);
			add(nominationSupportLinkAvailable);

			OnOffSwitch publicElectionLinkAvailable = new OnOffSwitch("publicElectionLinkAvailable", new PropertyModel<>(election, "publicElectionLinkAvailable")) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void action() {
					boolean requestedStatus = election.isPublicElectionLinkAvailable();
					try {
						AppContext.getInstance().getManagerBeanRemote().setPublicElectionLinkStatus(electionId, requestedStatus, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						Election updatedElection = reloadElection(electionId, election);
						if (updatedElection != null && updatedElection.isPublicElectionLinkAvailable() == requestedStatus) {
							getSession().success(getString(KEY_CONF_ELEC_TOGGLE_UPDATED));
						} else {
							getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
						}
					} catch (Exception e) {
						appLogger.error("Error updating public election link availability. electionId={}", electionId, e);
						if (reloadElection(electionId, election) == null) {
							election.setPublicElectionLinkAvailable(!requestedStatus);
						}
						getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
					}
					refreshToggleFeedback(feedbackPanel);
				}
			};
			publicElectionLinkAvailable.setOutputMarkupId(true);
			add(publicElectionLinkAvailable);

			OnOffSwitch revisionRequest = new OnOffSwitch("revisionRequest", new PropertyModel<>(election, "revisionRequest")) {
				private static final long serialVersionUID = 476112946371106638L;

				@Override
				protected void action() {
					boolean requestedStatus = election.isRevisionRequest();
					try {
						AppContext.getInstance().getManagerBeanRemote().requestElectionRevision(electionId, requestedStatus, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						Election updatedElection = reloadElection(electionId, election);
						if (updatedElection != null && updatedElection.isRevisionRequest() == requestedStatus) {
							getSession().success(getString(KEY_CONF_ELEC_TOGGLE_UPDATED));
						} else {
							getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
						}
					} catch (Exception e) {
						appLogger.error("Error updating revision request status. electionId={}", electionId, e);
						if (reloadElection(electionId, election) == null) {
							election.setRevisionRequest(!requestedStatus);
						}
						getSession().error(getString(KEY_CONF_ELEC_TOGGLE_UPDATE_ERROR));
					}
					refreshToggleFeedback(feedbackPanel);
				}
			};
			revisionRequest.setOutputMarkupId(true);
			add(revisionRequest);

			WebMarkupContainer closeElectionNotAvailable = new WebMarkupContainer("closeElectionNotAvailable");
			add(closeElectionNotAvailable);
			WebMarkupContainer electionAlreadyClosed = new WebMarkupContainer("electionAlreadyClosed");
			add(electionAlreadyClosed);
			ButtonCloseElection closeElectionButton = new ButtonCloseElection("closeElectionButton") {
				private static final long serialVersionUID = -2239965047961905998L;

				@Override
				public void onConfirm() {
					boolean closedOk = AppContext.getInstance().getManagerBeanRemote().closeElection(electionId, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					if(closedOk) {
						getSession().info(getString("confElecClosingOk"));
					} else {
						getSession().info(getString("confElecClosingFailed"));
					}
					setResponsePage(ElectionConfigurationDashboard.class, UtilsParameters.getId(electionId));
				}
			};
			add(closeElectionButton);
			if(election.isClosed()) {
				electionAlreadyClosed.setVisible(true);
				closeElectionButton.setVisible(false);
				closeElectionNotAvailable.setVisible(false);
			} else {
				electionAlreadyClosed.setVisible(false);
				if(AppContext.getInstance().getManagerBeanRemote().electionCanBeClosed(electionId)) {
					closeElectionButton.setVisible(true);
					closeElectionNotAvailable.setVisible(false);
				} else {
					closeElectionButton.setVisible(false);
					closeElectionNotAvailable.setVisible(true);
				}
			}

			Link<Void> back = new Link<Void>("back") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						setResponsePage(SecurityUtils.getHomePage());
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(back);

			File rolesDocument = AppContext.getInstance().getVoterBeanRemote().getElectionRolesRevisionDocument(filePath);
			add(new DownloadLink("rolesDocument", rolesDocument));

		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

	private Election reloadElection(long electionId, Election election) {
		Election updatedElection = AppContext.getInstance().getManagerBeanRemote().getElection(electionId);
		if (updatedElection == null) {
			return null;
		}
		election.setVotingLinkAvailable(updatedElection.isVotingLinkAvailable());
		election.setResultLinkAvailable(updatedElection.isResultLinkAvailable());
		election.setAuditorLinkAvailable(updatedElection.isAuditorLinkAvailable());
		election.setDoNominationLinkAvailable(updatedElection.isDoNominationLinkAvailable());
		election.setNominationTasksLinkAvailable(updatedElection.isNominationTasksLinkAvailable());
		election.setNominationSupportLinkAvailable(updatedElection.isNominationSupportLinkAvailable());
		election.setPublicElectionLinkAvailable(updatedElection.isPublicElectionLinkAvailable());
		election.setRevisionRequest(updatedElection.isRevisionRequest());
		return updatedElection;
	}

	private void refreshToggleFeedback(FeedbackPanel feedbackPanel) {
		AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class).orElse(null);
		if (target == null) {
			return;
		}
		target.add(feedbackPanel);
		target.addChildren(this, OnOffSwitch.class);
	}

}
