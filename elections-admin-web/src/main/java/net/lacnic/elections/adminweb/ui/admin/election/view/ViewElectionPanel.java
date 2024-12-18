package net.lacnic.elections.adminweb.ui.admin.election.view;

import java.io.File;
import java.text.SimpleDateFormat;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.admin.activity.ActivitiesListPanel;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.admin.email.EmailsListPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

public class ViewElectionPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private File censusFile;

	public ViewElectionPanel(String id, final long electionId, final boolean sent) {
		super(id);

		try {
			setOutputMarkupPlaceholderTag(true);
			Election election = AppContext.getInstance().getManagerBeanRemote().getElection(electionId);
			election.initStringsStartEndDates();

			add(new Label("linkSpanish", election.getLinkSpanish()));
			add(new Label("linkEnglish", election.getLinkEnglish()));
			add(new Label("linkPortuguese", election.getLinkPortuguese()));
			add(new Label("titleSpanish", election.getTitleSpanish()));
			add(new Label("titleEnglish", election.getTitleEnglish()));
			add(new Label("titlePortuguese", election.getTitlePortuguese()));

			Label descriptionSpanish = new Label("descriptionSpanish", election.getDescriptionSpanish());
			descriptionSpanish.setEscapeModelStrings(false);
			add(descriptionSpanish);
			Label descriptionEnglish = new Label("descriptionEnglish", election.getDescriptionEnglish());
			descriptionEnglish.setEscapeModelStrings(false);
			add(descriptionEnglish);
			Label descriptionPortuguese = new Label("descriptionPortuguese", election.getDescriptionPortuguese());
			descriptionPortuguese.setEscapeModelStrings(false);
			add(descriptionPortuguese);

			add(new Label("maxCandidates", String.valueOf(election.getMaxCandidates())));
			add(new Label("diffUTC", String.valueOf(election.getDiffUTC())));
			add(new Label("createDate", new SimpleDateFormat("dd/MM/yyyy").format(election.getCreationDate())));
			add(new Label("startDate", election.getAuxStartDate() + " " + election.getAuxStartHour() + " (UTC)"));
			add(new Label("endDate", election.getAuxEndDate() + " " + election.getAuxEndHour() + " (UTC)"));
			add(new Label("voteLinkAvailable", election.isVotingLinkAvailable() ? getString("electionDetailLinkAvailableYes") : getString("electionDetailLinkAvailableNo")));
			add(new Label("resultsLinkAvailable", election.isResultLinkAvailable() ? getString("electionDetailLinkAvailableYes") : getString("electionDetailLinkAvailableNo")));
			add(new Label("resultsLink", AppContext.getInstance().getManagerBeanRemote().getResultsLink(election)));

			BookmarkablePageLink<Void> editElection = new BookmarkablePageLink<>("editElection", ElectionDetailDashboard.class, UtilsParameters.getId(election.getElectionId()));
			editElection.setVisible(!election.isClosed());
			add(editElection);

			add(new AjaxLazyLoadPanel<ViewCandidatesListPanel>("candidatesListPanel") {
				private static final long serialVersionUID = 6513156554118602169L;

				@Override
				public ViewCandidatesListPanel getLazyLoadComponent(String markupId) {
					return new ViewCandidatesListPanel(markupId, election.getElectionId());
				}
			});

			BookmarkablePageLink<Void> editCandidates = new BookmarkablePageLink<>("editCandidates", ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
			editCandidates.setVisible(!election.isClosed());
			add(editCandidates);

			add(new AjaxLazyLoadPanel<ViewAuditorsListPanel>("auditorsListPanel") {
				private static final long serialVersionUID = -8684993569281131596L;

				@Override
				public ViewAuditorsListPanel getLazyLoadComponent(String markupId) {
					return new ViewAuditorsListPanel(markupId, election.getElectionId());
				}
			});

			BookmarkablePageLink<Void> editAuditors = new BookmarkablePageLink<>("editAuditors", ElectionAuditorsDashboard.class, UtilsParameters.getId(election.getElectionId()));
			editAuditors.setVisible(!election.isClosed());
			add(editAuditors);

			add(new AjaxLazyLoadPanel<ViewUserVotersListPanel>("votersList") {
				private static final long serialVersionUID = -5066564828514741892L;

				@Override
				public ViewUserVotersListPanel getLazyLoadComponent(String markupId) {
					return new ViewUserVotersListPanel(markupId, election);
				}
			});

			BookmarkablePageLink<Void> editCensus = new BookmarkablePageLink<>("editCensus", ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
			editCensus.setVisible(!election.isClosed());
			add(editCensus);

			DownloadLink downloadLink = new DownloadLink("exportCensus", new PropertyModel<>(ViewElectionPanel.this, "censusFile")) {
				private static final long serialVersionUID = 4098839411736270253L;

				@Override
				public void onClick() {
					setCensusFile(AppContext.getInstance().getManagerBeanRemote().exportCensus(election.getElectionId()));
					super.onClick();
				}
			};
			downloadLink.setVisible(election.isElectorsSet() && election.isClosed());
			add(downloadLink);

			add(new AjaxLazyLoadPanel<EmailsListPanel>("emailsList") {
				private static final long serialVersionUID = -6326434661632018604L;

				@Override
				public EmailsListPanel getLazyLoadComponent(String markupId) {
					return new EmailsListPanel(markupId, election.getElectionId(), sent);
				}
			});

			add(new AjaxLazyLoadPanel<ActivitiesListPanel>("activitiesList") {
				private static final long serialVersionUID = 5350609383247662704L;

				@Override
				public ActivitiesListPanel getLazyLoadComponent(String markupId) {
					return new ActivitiesListPanel(markupId, election.getElectionId());
				}
			});

			Link<Void> back = new Link<Void>("back") {
				private static final long serialVersionUID = -5761950650383408715L;

				@Override
				public void onClick() {
					try {
						setResponsePage(ElectionsDashboard.class);
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(back);

		} catch (Exception e) {
			appLogger.error(e);
		}

	}

	public File getCensusFile() {
		return censusFile;
	}

	public void setCensusFile(File censusFile) {
		this.censusFile = censusFile;
	}

}
