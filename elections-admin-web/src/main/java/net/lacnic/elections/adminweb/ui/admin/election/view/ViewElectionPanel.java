package net.lacnic.elections.adminweb.ui.admin.election.view;

import java.io.File;
import java.text.SimpleDateFormat;

import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.admin.activity.ActivitiesListPanel;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.call.ElectionCallDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.calendarmodule.ElectionCalendarModuleDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.organizations.ElectionOrganizationsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.tasks.ElectionTasksDashboard;
import net.lacnic.elections.adminweb.ui.admin.email.EmailsListPanel;
import net.lacnic.elections.adminweb.wicket.util.RestrictedCountriesFormatter;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.ejb.ElectionsManagerEJB;

public class ViewElectionPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String KEY_ELECTIONS_DETAIL_NOT_CONFIGURED = "electionsDetailNotConfigured";
	private static final String KEY_ELECTION_DETAIL_LINK_AVAILABLE_YES = "electionDetailLinkAvailableYes";
	private static final String KEY_ELECTION_DETAIL_LINK_AVAILABLE_NO = "electionDetailLinkAvailableNo";

	private File censusFile;

	public ViewElectionPanel(String id, final long electionId, final boolean sent) {
		super(id);
		setOutputMarkupPlaceholderTag(true);

		final ElectionsManagerEJB managerBeanRemote = AppContext.getInstance().getManagerBeanRemote();

		Election election = loadElectionOrThrow(managerBeanRemote, electionId);

		String restrictedCountriesDisplayText = RestrictedCountriesFormatter.format(election.getRestrictedCountryCodes(), getLocale());
		String resultsUrl = loadResultsUrlOrThrow(managerBeanRemote, election);
		String questionsUrl = election.getTokenQuestionLink();
		ElectionType electionType = election.getEffectiveElectionType();

		add(new ExternalLink("linkSpanish", election.getLinkSpanish(), election.getLinkSpanish()));
		add(new ExternalLink("linkEnglish", election.getLinkEnglish(), election.getLinkEnglish()));
		add(new ExternalLink("linkPortuguese", election.getLinkPortuguese(), election.getLinkPortuguese()));
		add(new Label("titleSpanish", election.getTitleSpanish()));
		add(new Label("titleEnglish", election.getTitleEnglish()));
		add(new Label("titlePortuguese", election.getTitlePortuguese()));

		Label descriptionSpanish = new Label("descriptionSpanish", election.getDescriptionSpanish());
		descriptionSpanish.setEscapeModelStrings(false);
		add(descriptionSpanish);

		Label descriptionEnglish = new Label("descriptionEnglish", election.getDescriptionEnglish());
		descriptionEnglish.setEscapeModelStrings(false);
		add(descriptionEnglish);

		BookmarkablePageLink<Void> editCalendar = new BookmarkablePageLink<Void>("editCalendar", ElectionCalendarModuleDashboard.class, UtilsParameters.getId(election.getElectionId()));
		editCalendar.setVisible(!election.isClosed());
		add(editCalendar);

		Label descriptionPortuguese = new Label("descriptionPortuguese", election.getDescriptionPortuguese());
		descriptionPortuguese.setEscapeModelStrings(false);
		add(descriptionPortuguese);

		Label callSpanish = new Label("callSpanish", election.getCallSpanish());
		callSpanish.setEscapeModelStrings(false);
		add(callSpanish);

		Label callEnglish = new Label("callEnglish", election.getCallEnglish());
		callEnglish.setEscapeModelStrings(false);
		add(callEnglish);

		Label callPortuguese = new Label("callPortuguese", election.getCallPortuguese());
		callPortuguese.setEscapeModelStrings(false);
		add(callPortuguese);

		add(new Label("maxCandidates", String.valueOf(election.getMaxCandidates())));
		add(new Label("category", election.getCategory() == null ? getString(KEY_ELECTIONS_DETAIL_NOT_CONFIGURED) : election.getCategory().name()));
		add(new Label("diffUTC", String.valueOf(election.getDiffUTC())));
		add(new Label("createDate", new SimpleDateFormat("dd/MM/yyyy").format(election.getCreationDate())));
		add(new Label("manageVotersManual", election.isManageVotersManual() ? getString(KEY_ELECTION_DETAIL_LINK_AVAILABLE_YES) : getString(KEY_ELECTION_DETAIL_LINK_AVAILABLE_NO)));
		add(new Label("manageOrganizationsManual", election.isManageOrganizationsManual() ? getString(KEY_ELECTION_DETAIL_LINK_AVAILABLE_YES) : getString(KEY_ELECTION_DETAIL_LINK_AVAILABLE_NO)));
		add(new Label("electionType", electionType == null ? getString(KEY_ELECTIONS_DETAIL_NOT_CONFIGURED) : getString("electionDeclarationsScope." + electionType.name())));
		add(new Label("restrictedCountries", restrictedCountriesDisplayText == null || restrictedCountriesDisplayText.trim().isEmpty() ? getString("electionsDetailRestrictedCountriesNone") : restrictedCountriesDisplayText));
		add(new Label("authorizedUserEmails", election.getAuthorizedUserEmails() == null || election.getAuthorizedUserEmails().trim().isEmpty() ? getString(KEY_ELECTIONS_DETAIL_NOT_CONFIGURED) : election.getAuthorizedUserEmails()));
		add(new Label("authorizedSupportEmails", election.getAuthorizedSupportEmails() == null || election.getAuthorizedSupportEmails().trim().isEmpty() ? getString(KEY_ELECTIONS_DETAIL_NOT_CONFIGURED) : election.getAuthorizedSupportEmails()));
		add(new Label("authorizedNominateEmails", election.getAuthorizedNominateEmails() == null || election.getAuthorizedNominateEmails().trim().isEmpty() ? getString(KEY_ELECTIONS_DETAIL_NOT_CONFIGURED) : election.getAuthorizedNominateEmails()));
		add(new Label("voteLinkAvailable", election.isVotingLinkAvailable() ? getString(KEY_ELECTION_DETAIL_LINK_AVAILABLE_YES) : getString(KEY_ELECTION_DETAIL_LINK_AVAILABLE_NO)));
		add(new Label("resultsLinkAvailable", election.isResultLinkAvailable() ? getString(KEY_ELECTION_DETAIL_LINK_AVAILABLE_YES) : getString(KEY_ELECTION_DETAIL_LINK_AVAILABLE_NO)));
		add(new Label("auditorLinkAvailable", election.isAuditorLinkAvailable() ? getString(KEY_ELECTION_DETAIL_LINK_AVAILABLE_YES) : getString(KEY_ELECTION_DETAIL_LINK_AVAILABLE_NO)));
		add(new ExternalLink("resultsLink", resultsUrl, resultsUrl));
		add(new ExternalLink("questionsLink", questionsUrl, questionsUrl));

		BookmarkablePageLink<Void> editElection = new BookmarkablePageLink<>("editElection", ElectionDetailDashboard.class, UtilsParameters.getId(election.getElectionId()));
		editElection.setVisible(!election.isClosed());
		add(editElection);

		BookmarkablePageLink<Void> editCall = new BookmarkablePageLink<>("editCall", ElectionCallDashboard.class, UtilsParameters.getId(election.getElectionId()));
		editCall.setVisible(!election.isClosed());
		add(editCall);

		BookmarkablePageLink<Void> editOrganizations = new BookmarkablePageLink<>("editOrganizations", ElectionOrganizationsDashboard.class, UtilsParameters.getId(election.getElectionId()));
		editOrganizations.setVisible(!election.isClosed());
		add(editOrganizations);
		/*
		 * BookmarkablePageLink<Void> editCalendar = new BookmarkablePageLink<>("editCalendar", ElectionCalendarDashboard.class, UtilsParameters.getId(election.getElectionId())); editCalendar.setVisible(!election.isClosed()); add(editCalendar);
		 */

		BookmarkablePageLink<Void> editTasks = new BookmarkablePageLink<>("editTasks", ElectionTasksDashboard.class, UtilsParameters.getId(election.getElectionId()));
		editTasks.setVisible(!election.isClosed());
		add(editTasks);

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
				setCensusFile(managerBeanRemote.exportCensus(election.getElectionId()));
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

	}

	public File getCensusFile() {
		return censusFile;
	}

	public void setCensusFile(File censusFile) {
		this.censusFile = censusFile;
	}

	private Election loadElectionOrThrow(ElectionsManagerEJB managerBeanRemote, long electionId) {
		try {
			return managerBeanRemote.getElectionWithRestrictedCountries(electionId);
		} catch (Exception e) {
			appLogger.error("Error loading election detail for electionId={}", electionId, e);
			throw new IllegalStateException("Could not load election detail", e);
		}
	}

	private String loadResultsUrlOrThrow(ElectionsManagerEJB managerBeanRemote, Election election) {
		try {
			return managerBeanRemote.getResultsLink(election);
		} catch (Exception e) {
			appLogger.error("Error loading results URL for electionId={}", election.getElectionId(), e);
			throw new IllegalStateException("Could not load election results URL", e);
		}
	}

}
