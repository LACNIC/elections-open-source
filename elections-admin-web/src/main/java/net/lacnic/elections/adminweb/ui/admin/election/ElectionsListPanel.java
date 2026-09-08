package net.lacnic.elections.adminweb.ui.admin.election;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
import net.lacnic.elections.adminweb.ui.admin.election.call.ElectionCallDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.calendarmodule.ElectionCalendarModuleDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.configuration.ElectionConfigurationDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.organizations.ElectionOrganizationsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.prereports.PreElectionReportsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.questions.ElectionQuestionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.results.ElectionResultsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.charts.StatsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.tasks.ElectionTasksDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.view.ViewElectionDashboard;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EmailTemplatesDashboard;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.ui.results.review.ReviewDashboard;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.utils.ElectionsRoles;

public class ElectionsListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String ELECTION_ID_ORDER_COLUMN = "1";
	private static final String CREATION_DATE_ORDER_COLUMN = "3";
	private static final String HTML_ATTRIBUTE_VALUE = "value";

	private long userAdminId;

	public ElectionsListPanel(String id, PageParameters pars) {
		super(id);
		boolean allElectionsFilter = UtilsParameters.isAll(pars);
		List<Election> electionsList = new ArrayList<>();
		if (allElectionsFilter) {
			electionsList = AppContext.getInstance().getManagerBeanRemote().getElectionsAllOrderCreationDate();
		} else {
			electionsList = AppContext.getInstance().getManagerBeanRemote().getElectionsLightThisYear();
		}
		if (!SecurityUtils.isManager()) {
			electionsList.removeIf(election -> !SecurityUtils.canAccessElection(election));
		}
		if (allElectionsFilter) {
			sortElectionsByIdDescending(electionsList);
		}
		init(electionsList, allElectionsFilter);
	}

	private void init(List<Election> electionsList, boolean allElectionsFilter) {
		try {

			setOutputMarkupPlaceholderTag(true);
			addDefaultOrderColumn(allElectionsFilter);

			final ListView<Election> electionsListView = new ListView<Election>("electionsList", electionsList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Election> item) {
					final Election currentElection = item.getModelObject();
					try {
						Label titles = new Label("titles", currentElection.getTitle(SecurityUtils.getLocale().getLanguage()));
						titles.setEscapeModelStrings(false);

						Label closedTag = new Label("closedTag", getString("closedText"));
						closedTag.setVisible(currentElection.isClosed());
						item.add(closedTag);

						item.add(new Label("electionId", currentElection.getElectionId()));
						item.add(new Label("creationDate", new SimpleDateFormat("dd/MM/yyyy").format(currentElection.getCreationDate())));
						item.add(new Label("startDate", currentElection.getVotingPeriodStartDateString()));
						item.add(new Label("endDate", currentElection.getVotingPeriodEndDateString()));

						item.add(new BookmarkablePageLink<Void>("electionDetail", ViewElectionDashboard.class, UtilsParameters.getId(currentElection.getElectionId())).add(titles));

						BookmarkablePageLink<Void> editElection = new BookmarkablePageLink<>("editElection", ElectionDetailDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						editElection.setMarkupId("editElection" + currentElection.getElectionId());
						editElection.setVisible(!currentElection.isClosed());
						item.add(editElection);

						BookmarkablePageLink<Void> census = new BookmarkablePageLink<>("manageCensus", ElectionCensusDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						census.setMarkupId("electionCensus" + currentElection.getElectionId());
						census.setEnabled(!currentElection.isClosed());
						census.add(new AttributeModifier(MarkupLiterals.HTML_ATTRIBUTE_CLASS, statusButtonClass(currentElection.isElectorsSet())));
						item.add(census);

						BookmarkablePageLink<Void> call = new BookmarkablePageLink<>("call", ElectionCallDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						call.setMarkupId("electionCall" + currentElection.getElectionId());
						call.setEnabled(!currentElection.isClosed());
						call.add(new AttributeModifier(MarkupLiterals.HTML_ATTRIBUTE_CLASS, statusButtonClass(currentElection.isCallSet())));
						item.add(call);

						BookmarkablePageLink<Void> organizations = new BookmarkablePageLink<>("organizations", ElectionOrganizationsDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						organizations.setMarkupId("Organizations" + currentElection.getElectionId());
						organizations.setEnabled(!currentElection.isClosed());
						organizations.add(new AttributeModifier(MarkupLiterals.HTML_ATTRIBUTE_CLASS, statusButtonClass(currentElection.isOrganizationsSet())));
						item.add(organizations);

						BookmarkablePageLink<Void> calendar = new BookmarkablePageLink<>("calendar", ElectionCalendarModuleDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						calendar.setMarkupId("electionCalendar" + currentElection.getElectionId());
						calendar.setEnabled(!currentElection.isClosed());
						calendar.add(new AttributeModifier(MarkupLiterals.HTML_ATTRIBUTE_CLASS, statusButtonClass(currentElection.isCalendarSet())));
						item.add(calendar);

						BookmarkablePageLink<Void> tasks = new BookmarkablePageLink<>("tasks", ElectionTasksDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						tasks.setMarkupId("electionTasks" + currentElection.getElectionId());
						tasks.setEnabled(!currentElection.isClosed());
						tasks.add(new AttributeModifier(MarkupLiterals.HTML_ATTRIBUTE_CLASS, statusButtonClass(currentElection.isTasksSet())));
						item.add(tasks);

						BookmarkablePageLink<Void> candidates = new BookmarkablePageLink<>("candidates", ElectionCandidatesDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						candidates.setMarkupId("electionCandidates" + currentElection.getElectionId());
						candidates.setEnabled(!currentElection.isClosed());
						candidates.add(new AttributeModifier(MarkupLiterals.HTML_ATTRIBUTE_CLASS, statusButtonClass(currentElection.isCandidatesSet())));
						item.add(candidates);

						BookmarkablePageLink<Void> auditors = new BookmarkablePageLink<>("auditors", ElectionAuditorsDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						auditors.setMarkupId("electionAuditors" + currentElection.getElectionId());
						auditors.setEnabled(!currentElection.isClosed());
						auditors.add(new AttributeModifier(MarkupLiterals.HTML_ATTRIBUTE_CLASS, statusButtonClass(currentElection.isAuditorsSet())));
						item.add(auditors);

						ButtonDeleteWithConfirmation buttonDeleteWithConfirmation = new ButtonDeleteWithConfirmation("removeElection", currentElection.getElectionId()) {
							private static final long serialVersionUID = -2068256428165604654L;

							@Override
							public void onConfirm() {
								try {
									if (!SecurityUtils.hasRole(ElectionsRoles.ELECTIONS_DELETER)) {
										SecurityUtils.error(getString("deleteAccessDenied"));
										setResponsePage(ElectionsDashboard.class);
										return;
									}
									boolean isNew = true;
									boolean isJoint = false;
									// Check if election is joint with another
									if (currentElection.getElectionId() == 0) {
										isNew = true;
									} else {
										isNew = false;
										isJoint = AppContext.getInstance().getManagerBeanRemote().isJointElection(currentElection.getElectionId());
									}
									;

									if ((!isNew) && (isJoint)) {
										SecurityUtils.error(getString("deleteElectionErrorJoint"));
										setResponsePage(ElectionsDashboard.class);
									} else {
										AppContext.getInstance().getManagerBeanRemote().removeElection(currentElection.getElectionId(), currentElection.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
										SecurityUtils.info(getString("deleteElectionSuccess"));
										setResponsePage(ElectionsDashboard.class);
									}
								} catch (Exception e) {
									appLogger.error(e.getMessage(), e);
								}
							}
						};
						item.add(buttonDeleteWithConfirmation);
						buttonDeleteWithConfirmation.setVisible(SecurityUtils.hasRole(ElectionsRoles.ELECTIONS_DELETER));

						BookmarkablePageLink<Void> revisionLink = new BookmarkablePageLink<>("revision", ReviewDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						revisionLink.setMarkupId("revision" + currentElection.getElectionId());
						revisionLink.setVisible(currentElection.isRevisionRequest());
						item.add(revisionLink);

						BookmarkablePageLink<Void> manageEmailsLink = new BookmarkablePageLink<>("manageEmails", EmailTemplatesDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						manageEmailsLink.setMarkupId("manageEmails" + currentElection.getElectionId());
						manageEmailsLink.setVisible(!currentElection.isClosed() && SecurityUtils.isManager());
						item.add(manageEmailsLink);

						BookmarkablePageLink<Void> statsLink = new BookmarkablePageLink<>("seeStats", StatsDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						statsLink.setMarkupId("seeStats" + currentElection.getElectionId());
						item.add(statsLink);

						BookmarkablePageLink<Void> officialResultsLink = new BookmarkablePageLink<>("seeOfficialResults", ElectionResultsDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						officialResultsLink.setMarkupId("seeOfficialResults" + currentElection.getElectionId());
						officialResultsLink.setVisible(!currentElection.isClosed());
						item.add(officialResultsLink);

						BookmarkablePageLink<Void> configurationLink = new BookmarkablePageLink<>("configuration", ElectionConfigurationDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						configurationLink.setMarkupId("configuration" + currentElection.getElectionId());
						item.add(configurationLink);

						BookmarkablePageLink<Void> questionsLink = new BookmarkablePageLink<>("questions", ElectionQuestionsDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						questionsLink.setMarkupId("questions" + currentElection.getElectionId());
						questionsLink.setVisible(!currentElection.isClosed());
						item.add(questionsLink);

						BookmarkablePageLink<Void> preReportsLink = new BookmarkablePageLink<>("preReports", PreElectionReportsDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						preReportsLink.setMarkupId("preReports" + currentElection.getElectionId());
						preReportsLink.setVisible(!currentElection.isClosed());
						item.add(preReportsLink);
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(electionsListView);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}

	public long getUserAdminId() {
		return userAdminId;
	}

	private static void sortElectionsByIdDescending(List<Election> electionsList) {
		electionsList.sort(Comparator.comparingLong(Election::getElectionId).reversed());
	}

	private void addDefaultOrderColumn(boolean allElectionsFilter) {
		WebMarkupContainer defaultOrderColumn = new WebMarkupContainer("defaultOrderColumn");
		defaultOrderColumn.add(new AttributeModifier(HTML_ATTRIBUTE_VALUE, allElectionsFilter ? ELECTION_ID_ORDER_COLUMN : CREATION_DATE_ORDER_COLUMN));
		add(defaultOrderColumn);
	}

	private String statusButtonClass(boolean completed) {
		if (completed) {
			return "btn btn-soft-success btn-sm rounded-pill text-body opacity-100";
		}
		return "btn btn-soft-warning btn-sm rounded-pill text-body opacity-100";
	}

}
