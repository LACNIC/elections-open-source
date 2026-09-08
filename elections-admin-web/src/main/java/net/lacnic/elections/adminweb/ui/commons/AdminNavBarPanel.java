package net.lacnic.elections.adminweb.ui.commons;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.activity.ActivitiesDashboard;
import net.lacnic.elections.adminweb.ui.admin.commissioner.CommissionersDashboard;
import net.lacnic.elections.adminweb.ui.admin.customization.CustomizationDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.calendarmodule.ElectionCalendarModuleDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.call.ElectionCallDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ManageCandidateBiographyDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ManageCandidatePhotoDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ManageCandidateStatusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ManageCandidateTrainingDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ManageCandidateTranslationsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.configuration.ElectionConfigurationDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.create.ElectionCreateDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.joint.JointElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.organizations.ElectionOrganizationsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.prereports.PreElectionReportsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.questions.ElectionQuestionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.results.ElectionResultsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.charts.StatsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.tasks.ElectionTasksDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.view.ViewElectionDashboard;
import net.lacnic.elections.adminweb.ui.admin.email.EmailsDashboard;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EmailTemplatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.ipaccess.IpAccessDashboard;
import net.lacnic.elections.adminweb.ui.admin.organizationsync.OrganizationsSyncRunsDashboard;
import net.lacnic.elections.adminweb.ui.admin.parameter.ParametersDashboard;
import net.lacnic.elections.adminweb.ui.admin.useradmin.UserAdminsDashboard;
import net.lacnic.elections.adminweb.ui.results.review.ReviewDashboard;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.data.CandidateBioMigrationBatchResult;
import net.lacnic.elections.data.CandidatePhotoBatchResult;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.utils.ElectionsRoles;


public class AdminNavBarPanel extends Panel {

	private static final long serialVersionUID = -4713777134889050424L;
	private static final int CANDIDATE_OPTIONS_NAME_MAX_LENGTH = 32;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private boolean userIsAdmin = true;


	public AdminNavBarPanel(String id) {
		this(id, 0L);
	}

	public AdminNavBarPanel(String id, long electionId) {
		super(id);

		boolean managerVisible = SecurityUtils.hasRole(ElectionsRoles.ELECTIONS_MANAGER);
		boolean localLogin = SecurityUtils.isLocalAuthentication();
		boolean electionVisible = SecurityUtils.hasAnyRole(
				ElectionsRoles.ELECTIONS_MANAGER,
				ElectionsRoles.ELECTIONS_STATUTARY_ONLY,
				ElectionsRoles.ELECTIONS_NON_STATUTARY_ONLY);
		setUserIsAdmin(managerVisible);

		WebMarkupContainer electionsContainer = new WebMarkupContainer("electionsContainer");
		electionsContainer.setVisible(electionVisible);
		add(electionsContainer);
		electionsContainer.add(new BookmarkablePageLink<>("allElectionsLink", ElectionsDashboard.class, UtilsParameters.getFilterAll()).setVisibilityAllowed(electionVisible));
		electionsContainer.add(new BookmarkablePageLink<>("recentElectionsLink", ElectionsDashboard.class).setVisibilityAllowed(electionVisible));

		Election currentElection = loadElection(electionId);
		boolean electionOptionsEnabled = electionVisible && SecurityUtils.canAccessElection(currentElection);
		boolean electionOpen = currentElection != null && !currentElection.isClosed();

		WebMarkupContainer electionOptionsDisabledContainer = new WebMarkupContainer("electionOptionsDisabledContainer");
		electionOptionsDisabledContainer.setVisible(electionVisible && !electionOptionsEnabled);
		add(electionOptionsDisabledContainer);

		WebMarkupContainer electionOptionsContainer = new WebMarkupContainer("electionOptionsContainer");
		electionOptionsContainer.setVisible(electionOptionsEnabled);
		add(electionOptionsContainer);

		WebMarkupContainer electionOptionsToggle = new WebMarkupContainer("electionOptionsToggle");
		electionOptionsToggle.add(new AttributeAppender("class", " text-primary fw-semibold"));
		electionOptionsContainer.add(electionOptionsToggle);

		electionOptionsContainer.add(createElectionOptionLink("electionOptionDetailsLink", ViewElectionDashboard.class, electionId));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionEditLink", ElectionDetailDashboard.class, electionId).setEnabled(electionOpen));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionCallLink", ElectionCallDashboard.class, electionId).setEnabled(electionOpen));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionOrganizationsLink", ElectionOrganizationsDashboard.class, electionId).setEnabled(electionOpen));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionCensusLink", ElectionCensusDashboard.class, electionId).setEnabled(electionOpen));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionCalendarLink", ElectionCalendarModuleDashboard.class, electionId).setEnabled(electionOpen));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionTasksLink", ElectionTasksDashboard.class, electionId).setEnabled(electionOpen));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionCandidatesLink", ElectionCandidatesDashboard.class, electionId).setEnabled(electionOpen));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionAuditorsLink", ElectionAuditorsDashboard.class, electionId).setEnabled(electionOpen));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionEmailsLink", EmailTemplatesDashboard.class, electionId).setVisibilityAllowed(electionOpen && managerVisible));
		electionOptionsContainer.add(new WebMarkupContainer("electionOptionResultsToggle"));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionAuditorResultsLink", StatsDashboard.class, electionId));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionOfficialResultsLink", ElectionResultsDashboard.class, electionId).setVisibilityAllowed(electionOpen));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionQuestionsLink", ElectionQuestionsDashboard.class, electionId).setVisibilityAllowed(electionOpen));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionReportsLink", PreElectionReportsDashboard.class, electionId).setVisibilityAllowed(electionOpen));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionConfigurationLink", ElectionConfigurationDashboard.class, electionId));
		electionOptionsContainer.add(createElectionOptionLink("electionOptionReviewLink", ReviewDashboard.class, electionId).setVisibilityAllowed(currentElection != null && currentElection.isRevisionRequest()));

		List<Candidate> candidateOptions = loadCandidateOptions(electionOptionsEnabled, electionId);
		boolean candidateOptionsEnabled = electionOptionsEnabled && !candidateOptions.isEmpty();

		WebMarkupContainer candidateOptionsDisabledContainer = new WebMarkupContainer("candidateOptionsDisabledContainer");
		candidateOptionsDisabledContainer.setVisible(electionVisible && !candidateOptionsEnabled);
		add(candidateOptionsDisabledContainer);

		WebMarkupContainer candidateOptionsContainer = new WebMarkupContainer("candidateOptionsContainer");
		candidateOptionsContainer.setVisible(candidateOptionsEnabled);
		add(candidateOptionsContainer);

		WebMarkupContainer candidateOptionsToggle = new WebMarkupContainer("candidateOptionsToggle");
		candidateOptionsToggle.add(new AttributeAppender("class", " text-primary fw-semibold"));
		candidateOptionsContainer.add(candidateOptionsToggle);

		candidateOptionsContainer.add(new ListView<Candidate>("candidateOptionsList", candidateOptions) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Candidate> item) {
				Candidate candidate = item.getModelObject();
				long candidateId = candidate.getCandidateId();
				String candidateName = resolveCandidateName(candidate);

				WebMarkupContainer candidateOptionToggle = new WebMarkupContainer("candidateOptionToggle");
				candidateOptionToggle.add(AttributeModifier.replace("title", candidateName));
				candidateOptionToggle.add(new Label("candidateOptionName", truncateCandidateName(candidateName)));
				item.add(candidateOptionToggle);

				item.add(createCandidateOptionLink("candidateOptionBiographyLink", ManageCandidateBiographyDashboard.class, electionId, candidateId));
				item.add(createCandidateOptionLink("candidateOptionPhotoLink", ManageCandidatePhotoDashboard.class, electionId, candidateId));
				item.add(createCandidateOptionLink("candidateOptionStatusLink", ManageCandidateStatusDashboard.class, electionId, candidateId));
				item.add(createCandidateOptionLink("candidateOptionTrainingLink", ManageCandidateTrainingDashboard.class, electionId, candidateId));
				item.add(createCandidateOptionLink("candidateOptionTranslationsLink", ManageCandidateTranslationsDashboard.class, electionId, candidateId));
			}
		});

		add(new BookmarkablePageLink<>("newElectionLink", ElectionCreateDashboard.class).setVisibilityAllowed(electionVisible));
		add(new BookmarkablePageLink<>("commissionersLink", CommissionersDashboard.class).setVisibilityAllowed(managerVisible));
		add(new BookmarkablePageLink<>("userAdminsLink", UserAdminsDashboard.class).setVisibilityAllowed(managerVisible && localLogin));

		WebMarkupContainer advancedContainer = new WebMarkupContainer("advancedContainer");
		advancedContainer.setVisible(managerVisible);
		add(advancedContainer);
		advancedContainer.add(new BookmarkablePageLink<>("invalidIpsLink", IpAccessDashboard.class).setVisibilityAllowed(managerVisible));
		advancedContainer.add(new BookmarkablePageLink<>("activitiesLink", ActivitiesDashboard.class).setVisibilityAllowed(managerVisible));
		advancedContainer.add(new BookmarkablePageLink<>("parametersLink", ParametersDashboard.class).setVisibilityAllowed(managerVisible));
		advancedContainer.add(new BookmarkablePageLink<>("pendingEmailsLink", EmailsDashboard.class).setVisibilityAllowed(managerVisible));
		advancedContainer.add(new BookmarkablePageLink<>("allEmailsLink", EmailsDashboard.class, UtilsParameters.getFilterAll()).setVisibilityAllowed(managerVisible));
		advancedContainer.add(new BookmarkablePageLink<>("emailTemplatesLink", EmailTemplatesDashboard.class, UtilsParameters.getId(0L)).setVisibilityAllowed(managerVisible));
			advancedContainer.add(new Link<Void>("updateTemplatesLink") {
				private static final long serialVersionUID = -5210149681203217889L;

			@Override
				public void onClick() {
					Integer cuenta = AppContext.getInstance().getManagerBeanRemote().createMissingEmailTemplates();
					getSession().info("Se ha ejecutado el proceso de creación de templates para elecciones, creando " + cuenta + " templates");
					setResponsePage(ElectionsDashboard.class);
				}
			}.setVisibilityAllowed(managerVisible));
			advancedContainer.add(new Link<Void>("forceAllRemindersLink") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick() {
					try {
						AppContext.getInstance().getManagerBeanRemote().processDailyReminderFrequency();
						getSession().success(getString("forceAllRemindersSuccess"));
					} catch (Exception e) {
						getSession().error(getString("forceAllRemindersError"));
					}
					setResponsePage(ElectionsDashboard.class);
				}
			}.setVisibilityAllowed(managerVisible));
			advancedContainer.add(new Link<Void>("optimizeCandidatePhotosLink") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick() {
					try {
						CandidatePhotoBatchResult result = AppContext.getInstance().getManagerBeanRemote()
								.optimizeAllCandidatePhotos(SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						getSession().success(new StringResourceModel("optimizeCandidatePhotosSuccess", AdminNavBarPanel.this, null)
								.setParameters(result.getTotalCandidates(), result.getUpdatedCandidates(), result.getSkippedCandidates(), result.getFailedCandidates(),
										formatBytes(result.getOriginalTotalBytes()), formatBytes(result.getResultingTotalBytes()))
								.getString());
					} catch (Exception e) {
						getSession().error(getString("optimizeCandidatePhotosError"));
					}
					setResponsePage(ElectionsDashboard.class);
				}
			}.setVisibilityAllowed(managerVisible));
			advancedContainer.add(new Link<Void>("migrateCandidateBiosLink") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick() {
					try {
						CandidateBioMigrationBatchResult result = AppContext.getInstance().getManagerBeanRemote()
								.migrateCandidateBiosHtmlToPlainText(SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						getSession().success(new StringResourceModel("migrateCandidateBiosSuccess", AdminNavBarPanel.this, null)
								.setParameters(result.getTotalCandidates(), result.getUpdatedCandidates(), result.getSkippedCandidates(), result.getFailedCandidates())
								.getString());
					} catch (Exception e) {
						getSession().error(getString("migrateCandidateBiosError"));
					}
					setResponsePage(ElectionsDashboard.class, UtilsParameters.getFilterAll());
				}
			}.setVisibilityAllowed(managerVisible));
			advancedContainer.add(new BookmarkablePageLink<>("organizationsSyncRunsLink", OrganizationsSyncRunsDashboard.class).setVisibilityAllowed(managerVisible));
			advancedContainer.add(new BookmarkablePageLink<>("jointElectionsLink", JointElectionsDashboard.class, UtilsParameters.getId(0L)).setVisibilityAllowed(managerVisible));
			advancedContainer.add(new BookmarkablePageLink<>("customizationLink", CustomizationDashboard.class, UtilsParameters.getId(0L)).setVisibilityAllowed(managerVisible));
			}

	private String formatBytes(long bytes) {
		return FileUtils.byteCountToDisplaySize(Math.max(bytes, 0L));
	}

	private Election loadElection(long electionId) {
		if (electionId <= 0) {
			return null;
		}
		return AppContext.getInstance().getManagerBeanRemote().getElection(electionId);
	}

	private BookmarkablePageLink<Void> createElectionOptionLink(String id, Class<? extends Page> pageClass, long electionId) {
		return new BookmarkablePageLink<>(id, pageClass, UtilsParameters.getId(electionId));
	}

	private BookmarkablePageLink<Void> createCandidateOptionLink(String id, Class<? extends Page> pageClass, long electionId, long candidateId) {
		return new BookmarkablePageLink<>(id, pageClass, candidatePageParameters(electionId, candidateId));
	}

	private PageParameters candidatePageParameters(long electionId, long candidateId) {
		PageParameters parameters = UtilsParameters.getId(electionId);
		parameters.add(UtilsParameters.getCandidateText(), candidateId);
		return parameters;
	}

	private List<Candidate> loadCandidateOptions(boolean electionOptionsEnabled, long electionId) {
		if (!electionOptionsEnabled) {
			return Collections.emptyList();
		}
		try {
			List<Candidate> loadedCandidates = AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(electionId);
			if (loadedCandidates == null) {
				return Collections.emptyList();
			}
			List<Candidate> regularCandidates = new ArrayList<>();
			for (Candidate candidate : loadedCandidates) {
				if (candidate != null && !candidate.isAbstention()) {
					regularCandidates.add(candidate);
				}
			}
			sortCandidatesAlphabetically(regularCandidates);
			return regularCandidates;
		} catch (Exception e) {
			appLogger.error("Error loading candidate options for navbar. electionId={}", electionId, e);
			return Collections.emptyList();
		}
	}

	private void sortCandidatesAlphabetically(List<Candidate> candidates) {
		final Collator collator = Collator.getInstance(getLocale());
		collator.setStrength(Collator.PRIMARY);
		Collections.sort(candidates, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate firstCandidate, Candidate secondCandidate) {
				String firstCandidateName = resolveCandidateName(firstCandidate);
				String secondCandidateName = resolveCandidateName(secondCandidate);
				int result = collator.compare(firstCandidateName, secondCandidateName);
				if (result != 0) {
					return result;
				}
				result = firstCandidateName.compareToIgnoreCase(secondCandidateName);
				if (result != 0) {
					return result;
				}
				return Long.compare(firstCandidate.getCandidateId(), secondCandidate.getCandidateId());
			}
		});
	}

	private String resolveCandidateName(Candidate candidate) {
		if (candidate == null || candidate.getName() == null || candidate.getName().trim().isEmpty()) {
			return "-";
		}
		return candidate.getName().trim();
	}

	private String truncateCandidateName(String candidateName) {
		if (candidateName == null || candidateName.length() <= CANDIDATE_OPTIONS_NAME_MAX_LENGTH) {
			return candidateName;
		}
		return candidateName.substring(0, CANDIDATE_OPTIONS_NAME_MAX_LENGTH - 3) + "...";
	}

	public boolean isUserIsAdmin() {
		return userIsAdmin;
	}

	public void setUserIsAdmin(boolean userIsAdmin) {
		this.userIsAdmin = userIsAdmin;
	}

}
