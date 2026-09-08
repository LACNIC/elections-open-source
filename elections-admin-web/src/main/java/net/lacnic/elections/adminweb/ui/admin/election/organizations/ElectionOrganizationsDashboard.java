package net.lacnic.elections.adminweb.ui.admin.election.organizations;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.admin.election.call.ElectionCallDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.data.AsyncProcessingError;
import net.lacnic.elections.data.AsyncProcessingProgress;
import net.lacnic.elections.data.AsyncProcessingType;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SyncAudit;
import net.lacnic.elections.domain.pre.SyncRun;
import net.lacnic.elections.exception.CensusValidationException;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class ElectionOrganizationsDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 4966871009674600158L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final int SYNC_RUNS_PAGE_SIZE = 10;
	private static final String KEY_ORGANIZATIONS_MANAGEMENT_ASYNC_PROCESSING_ERROR = "organizationsManagementAsyncProcessingError";

	private Election election;
	private Organization organization = new Organization();
	private String expandedSyncRunKey;

	private static final class OrganizationsProcessingFeedbackMessage implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String text;

		private OrganizationsProcessingFeedbackMessage(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public ElectionOrganizationsDashboard(PageParameters params) {
		super(params);

		Election election = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));
		if (election.isClosed()) {
			setResponsePage(ErrorElectionClosed.class);
			return;
		}

		setElection(election);
		add(new FeedbackPanel("feedback"));
		long electionId = election.getElectionId();
		AsyncProcessingError asyncError = AppContext.getInstance().getManagerBeanRemote().getElectionOrganizationsProcessingError(electionId);
		AsyncProcessingType processingType = AppContext.getInstance().getManagerBeanRemote().getElectionProcessingType(electionId);
		AsyncProcessingProgress organizationsProgress = AppContext.getInstance().getManagerBeanRemote().getElectionOrganizationsProcessingProgress(electionId);
		AsyncProcessingProgress censusProgress = AppContext.getInstance().getManagerBeanRemote().getElectionCensusProcessingProgress(electionId);
		AsyncProcessingProgress activeProgress = resolveProgressByType(processingType, censusProgress, organizationsProgress);
		boolean organizationsProcessing = AppContext.getInstance().getManagerBeanRemote().isElectionOrganizationsProcessing(electionId);
		appLogger.info("Organizations dashboard status. electionId={}, processing={}, processingType={}, progressProcessedRows={}, progressTotalRows={}, asyncErrorKey={}",
				electionId,
				organizationsProcessing,
				processingType,
				activeProgress != null ? activeProgress.getProcessedRows() : null,
				activeProgress != null ? activeProgress.getTotalRows() : null,
				asyncError != null ? asyncError.getMessageKey() : null);
		clearOrganizationsProcessingFeedbackMessages();
		if (organizationsProcessing) {
			info(new OrganizationsProcessingFeedbackMessage(buildProcessingInfoMessage(processingType, activeProgress)));
		} else if (asyncError != null) {
			SecurityUtils.clearAsyncCompletionFeedbackPending(electionId, AsyncProcessingType.ORGANIZATIONS);
			error(resolveOrganizationsAsyncErrorMessage(asyncError));
			appLogger.warn("Organizations async processing error shown in dashboard. electionId={}, messageKey={}, errorRow={}, errorInfo={}, technicalDetail={}, createdAt={}",
					electionId, asyncError.getMessageKey(), asyncError.getErrorRow(), asyncError.getErrorInfo(), asyncError.getTechnicalDetail(), asyncError.getCreatedAt());
		} else if (SecurityUtils.consumeAsyncCompletionFeedbackPending(electionId, AsyncProcessingType.ORGANIZATIONS)) {
			info(getString("organizationsManagementAsyncProcessingSuccess"));
		}
		add(new ManageElectionTabsPanel("tabsPanel", election, "tabOrganizations"));
		boolean manageOrganizationsFromElectionConfig = !election.isManageOrganizationsManual();
		boolean automaticSyncControlsAllowed = manageOrganizationsFromElectionConfig;
		Map<Long, Date[]> syncWindowByElectionId = AppContext.getInstance().getManagerBeanRemote().getMilacnicSyncCalendarWindowByElectionIds(Collections.singletonList(election.getElectionId()));
		Date[] syncWindow = syncWindowByElectionId == null ? null : syncWindowByElectionId.get(election.getElectionId());
		boolean syncWindowActive = isSyncWindowActive(syncWindow, new Date());
		SyncRun latestSyncRun = AppContext.getInstance().getManagerBeanRemote().getElectionLatestSyncRun(election.getElectionId());
		WebMarkupContainer autoOrganizationsInfo = new WebMarkupContainer("autoOrganizationsInfo");
		autoOrganizationsInfo.setVisible(manageOrganizationsFromElectionConfig);
		WebMarkupContainer autoOrganizationsAlert = new WebMarkupContainer("autoOrganizationsAlert");
		autoOrganizationsAlert.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  buildSyncInfoAlertCssClass(latestSyncRun)));
		autoOrganizationsInfo.add(autoOrganizationsAlert);
		autoOrganizationsAlert.add(new Label("autoOrganizationsInfoText",
				getSyncInfoText(latestSyncRun, "organizationsManagementAutoFromConfigInfo", "organizationsSyncInfoErrorText", "organizationsSyncInfoFirstRunPendingText")));
		autoOrganizationsAlert.add(new Label("syncInfoLastSyncAt", formatSyncDate(latestSyncRun != null ? latestSyncRun.getSyncAt() : null, election.getDiffUTC())));
		autoOrganizationsAlert.add(new Label("syncInfoStatus", resolveSyncInfoStatusLabel(latestSyncRun)));
		autoOrganizationsAlert.add(new Label("syncInfoMetrics", buildLatestSyncMetricsText(latestSyncRun)));
		autoOrganizationsAlert.add(new Label("syncInfoMessage", resolveSyncInfoMessage(latestSyncRun)));
		add(autoOrganizationsInfo);

		List<SyncRun> syncRunsResult = AppContext.getInstance().getManagerBeanRemote().getElectionSyncRuns(election.getElectionId(), 200);
		List<SyncRun> syncRuns = syncRunsResult == null ? new ArrayList<>() : new ArrayList<>(syncRunsResult);
		Collections.sort(syncRuns, Comparator.comparing(SyncRun::getSyncAt, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(SyncRun::getId, Comparator.reverseOrder()));
		List<Organization> advancedOrganizationsResult = AppContext.getInstance().getManagerBeanRemote().getOrganizations(electionId);
		List<Organization> advancedOrganizations = advancedOrganizationsResult == null ? Collections.emptyList() : advancedOrganizationsResult;
		List<Nomination> advancedNominationsResult = AppContext.getInstance().getManagerBeanRemote().getElectionNominations(electionId);
		List<Nomination> advancedNominations = advancedNominationsResult == null ? Collections.emptyList() : advancedNominationsResult;
		List<SupportNomination> advancedSupportNominationsResult = AppContext.getInstance().getManagerBeanRemote().getElectionSupportNominations(electionId);
		List<SupportNomination> advancedSupportNominations = advancedSupportNominationsResult == null ? Collections.emptyList() : advancedSupportNominationsResult;
		boolean hasOrganizations = !advancedOrganizations.isEmpty();
		boolean deleteBlockedByNominations = !advancedNominations.isEmpty();
		boolean hasSupportLinks = hasSupportLinks(advancedSupportNominations);

		add(new ElectionOrganizationsForm("electionOrganizationsForm", election, manageOrganizationsFromElectionConfig, organizationsProcessing));

		WebMarkupContainer addOrganizationCard = new WebMarkupContainer("addOrganizationCard");
		addOrganizationCard.setVisible(!manageOrganizationsFromElectionConfig && !organizationsProcessing);
		add(addOrganizationCard);
		Form<Void> organizationForm = new Form<>("addOrganizationsForm");
		organizationForm.add(new AddOrganizationPanel("addOrganizationPanel", election.getElectionId(), organization));
		organizationForm.add(new Button("addOrganization") {
			private static final long serialVersionUID = 7296409026709092027L;

			@Override
				public void onSubmit() {
					try {
						AppContext.getInstance().getManagerBeanRemote().addOrganization(getElection().getElectionId(), organization, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						getSession().info(getString("organizationsManagementAddSuccess"));
						setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(getElection().getElectionId()));
					} catch (CensusValidationException e) {
						error(getString(e.getMessage()));
					}
				}
			});
		addOrganizationCard.add(organizationForm);

		WebMarkupContainer organizationsColumn = new WebMarkupContainer("organizationsColumn");
		organizationsColumn.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "col-lg-12"));
		if (organizationsProcessing) {
			WebMarkupContainer hiddenOrganizationsList = new WebMarkupContainer("organizationsListPanel");
			hiddenOrganizationsList.setVisible(false);
			organizationsColumn.add(hiddenOrganizationsList);
		} else {
			organizationsColumn.add(new AjaxLazyLoadPanel<OrganizationsListPanel>("organizationsListPanel") {
				private static final long serialVersionUID = -8304457490744840262L;

				@Override
				public OrganizationsListPanel getLazyLoadComponent(String markupId) {
					return new OrganizationsListPanel(markupId, election);
				}
			});
		}
		add(organizationsColumn);

		WebMarkupContainer syncRunsColumn = new WebMarkupContainer("syncRunsColumn");
		syncRunsColumn.setVisible(manageOrganizationsFromElectionConfig);
		add(syncRunsColumn);

		WebMarkupContainer syncRunsCard = new WebMarkupContainer("syncRunsCard");
		syncRunsCard.setVisible(manageOrganizationsFromElectionConfig);
		syncRunsCard.setOutputMarkupId(true);
		Label syncWindowStatus = new Label("syncWindowStatus", getString(syncWindowActive ? "organizationsSyncControlWindowStatusIn" : "organizationsSyncControlWindowStatusOut"));
		syncWindowStatus.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  syncWindowActive ? "badge bg-success-subtle text-success border border-success-subtle" : "badge bg-warning-subtle text-warning border border-warning-subtle"));
		syncRunsCard.add(syncWindowStatus);
		AutomaticOrganizationsSyncActionsPanel automaticOrganizationsSyncActionsPanel = new AutomaticOrganizationsSyncActionsPanel(
				"automaticOrganizationsSyncActionsPanel",
				election,
				syncWindowActive && !organizationsProcessing);
		automaticOrganizationsSyncActionsPanel.setVisible(automaticSyncControlsAllowed);
		syncRunsCard.add(automaticOrganizationsSyncActionsPanel);
		syncRunsCard.add(new WebMarkupContainer("syncRunsEmpty").setVisible(syncRuns.isEmpty()));
		PageableListView<SyncRun> syncRunsRows = new PageableListView<SyncRun>("syncRunsRows", syncRuns, SYNC_RUNS_PAGE_SIZE) {
			private static final long serialVersionUID = -2372178362911738276L;

			@Override
			protected void populateItem(ListItem<SyncRun> item) {
				SyncRun run = item.getModelObject();
				String runKey = buildSyncRunKey(run);
				boolean detailVisible = runKey.equals(expandedSyncRunKey);
				List<SyncAudit> auditsForRun = detailVisible ? loadSyncAuditsForRun(electionId, run) : Collections.emptyList();
				item.setOutputMarkupId(true);
				item.add(new Label("syncRunsTimestamp", formatSyncDate(run.getSyncAt(), election.getDiffUTC())));
				item.add(new Label("syncRunsStatus", resolveSyncStatusLabel(run.getStatus())));
				item.add(new Label("syncRunsMetrics", buildSyncRunsMetricsText(run)));
				item.add(new Label("syncRunsDuration", run.getDurationMs() == null ? "-" : run.getDurationMs() + " ms"));
				item.add(new Label("syncRunsWs", buildWsMetricsText(run)));
				item.add(new Label("syncRunsRunId", valueOrDash(run.getSyncRunId())));
				item.add(new Label("syncRunsMessage", valueOrDash(run.getMessage())));
				AjaxLink<Void> toggleDetail = new AjaxLink<Void>("syncRunsToggleDetail") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						expandedSyncRunKey = runKey.equals(expandedSyncRunKey) ? null : runKey;
						target.add(syncRunsCard);
					}
				};
				toggleDetail.add(new Label("syncRunsToggleDetailLabel", runKey.equals(expandedSyncRunKey)
						? getString("organizationsSyncControlRunsDetailHide")
						: getString("organizationsSyncControlRunsDetailShow")));
				item.add(toggleDetail);

				WebMarkupContainer detailRow = new WebMarkupContainer("syncRunsDetailRow");
				detailRow.setVisible(detailVisible);
				detailRow.add(new WebMarkupContainer("syncRunsDetailEmpty").setVisible(detailVisible && auditsForRun.isEmpty()));
				detailRow.add(new ListView<SyncAudit>("syncRunsDetailRows", auditsForRun) {
					private static final long serialVersionUID = -2686764900777534508L;

					@Override
					protected void populateItem(ListItem<SyncAudit> detailItem) {
						SyncAudit audit = detailItem.getModelObject();
						detailItem.add(new Label("syncRunsDetailTimestamp", formatSyncDate(audit.getEventDate(), election.getDiffUTC())));
						detailItem.add(new Label("syncRunsDetailOrgId", valueOrDash(audit.getOrgId())));
						detailItem.add(new Label("syncRunsDetailField", resolveAuditFieldLabel(audit.getFieldName())));
						detailItem.add(new Label("syncRunsDetailOldValue", resolveAuditValue(audit, true)));
						detailItem.add(new Label("syncRunsDetailNewValue", resolveAuditValue(audit, false)));
					}
				}.setVisible(detailVisible && !auditsForRun.isEmpty()));
				item.add(detailRow);
			}
		};
		syncRunsRows.setVisible(!syncRuns.isEmpty());
		syncRunsCard.add(syncRunsRows);
		PagingNavigator syncRunsPager = new PagingNavigator("syncRunsPager", syncRunsRows);
		syncRunsPager.setVisible(syncRuns.size() > SYNC_RUNS_PAGE_SIZE);
		syncRunsCard.add(syncRunsPager);
		syncRunsColumn.add(syncRunsCard);

		add(new OrganizationsAdvancedActionsPanel(
				"organizationsAdvancedActionsPanel",
				election,
				!organizationsProcessing,
				hasOrganizations,
				hasSupportLinks,
				deleteBlockedByNominations,
				manageOrganizationsFromElectionConfig && syncWindowActive));
	}

	private final class ElectionOrganizationsForm extends Form<Void> {
		private static final long serialVersionUID = 554315344484468227L;

		private ElectionOrganizationsForm(String id, Election election, boolean manageOrganizationsFromElectionConfig, boolean organizationsProcessing) {
			super(id);
			WebMarkupContainer uploadSection = new WebMarkupContainer("uploadSection");
			uploadSection.setVisible(!manageOrganizationsFromElectionConfig && !organizationsProcessing);
			add(uploadSection);
			uploadSection.add(new UploadOrganizationsDebtorsFilePanel("uploadOrganizationsDebtorsFilePanel", election));
				add(new Link<Void>("back") {
					private static final long serialVersionUID = 5380596307620592571L;

					@Override
					public void onClick() {
						setResponsePage(ElectionCallDashboard.class, UtilsParameters.getId(election.getElectionId()));
					}
				});
				add(new Link<Void>("skip") {
					private static final long serialVersionUID = 1278073099337809690L;

					@Override
					public void onClick() {
						setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
					}
				});
				add(new Link<Void>("markDoneNext") {
					private static final long serialVersionUID = -4756065994069425659L;

					@Override
					public void onClick() {
						AppContext.getInstance().getManagerBeanRemote().persistElectionOrganizationsSet(election.getElectionId(), election.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
					}
				});
			}
		}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	private String formatSyncDate(Date syncDate, int diffUtc) {
		if (syncDate == null) {
			return valueOrDash(null);
		}
		Date displayDate = new DateTime(syncDate).plusHours(diffUtc).toDate();
		return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(displayDate) + " (UTC)";
	}

	private String resolveSyncStatusLabel(String status) {
		if (status == null || status.trim().isEmpty()) {
			return valueOrDash(null);
		}
		String normalized = status.trim().toUpperCase();
		String key = "organizationsSyncStatus" + normalized;
		return getString(key, null, normalized);
	}

	private String getSyncInfoText(SyncRun run, String successKey, String errorKey, String pendingKey) {
		if (run == null) {
			return getString(pendingKey);
		}
		return isErrorSyncStatus(run) ? getString(errorKey) : getString(successKey);
	}

	private String buildSyncInfoAlertCssClass(SyncRun run) {
		if (run == null) {
			return "alert alert-warning mb-3 fs-5";
		}
		return isErrorSyncStatus(run) ? "alert alert-danger mb-3 fs-5" : "alert alert-success mb-3 fs-5";
	}

	private boolean isErrorSyncStatus(SyncRun run) {
		return run != null && "ERROR".equalsIgnoreCase(valueOrDash(run.getStatus()));
	}

	private String buildLatestSyncMetricsText(SyncRun latestSyncRun) {
		if (latestSyncRun == null) {
			return valueOrDash(null);
		}
		int processed = latestSyncRun.getProcessedRows();
		int created = latestSyncRun.getCreatedRows();
		int updated = latestSyncRun.getUpdatedRows();
		int deleted = latestSyncRun.getDeletedRows();
		return new StringResourceModel("organizationsSyncInfoMetricsValue", this, null).setParameters(processed, created, updated, deleted).getString();
	}

	private String resolveSyncInfoStatusLabel(SyncRun run) {
		if (run == null) {
			return getString("syncInfoFirstRunPendingStatus");
		}
		return resolveSyncStatusLabel(run.getStatus());
	}

	private String resolveSyncInfoMessage(SyncRun run) {
		if (run == null) {
			return getString("syncInfoFirstRunPendingMessage");
		}
		return valueOrDash(run.getMessage());
	}

	private String buildSyncRunsMetricsText(SyncRun run) {
		int processed = run.getProcessedRows();
		int created = run.getCreatedRows();
		int updated = run.getUpdatedRows();
		int deleted = run.getDeletedRows();
		return new StringResourceModel("organizationsSyncRunsMetricsValue", this, null).setParameters(processed, created, updated, deleted).getString();
	}

	private String buildWsMetricsText(SyncRun run) {
		String statusCode = run.getWsStatusCode() == null ? "-" : String.valueOf(run.getWsStatusCode());
		String elapsed = run.getWsElapsedMs() == null ? "-" : run.getWsElapsedMs() + " ms";
		return new StringResourceModel("organizationsSyncControlWsValue", this, null).setParameters(statusCode, elapsed).getString();
	}

	private List<SyncAudit> loadSyncAuditsForRun(long electionId, SyncRun run) {
		if (electionId <= 0 || run == null || !hasText(run.getSyncRunId())) {
			return Collections.emptyList();
		}
		List<SyncAudit> audits = AppContext.getInstance().getManagerBeanRemote().getElectionSyncAuditsByRunId(electionId, run.getSyncRunId(), 0);
		return audits == null ? Collections.emptyList() : audits;
	}

	private String buildSyncRunKey(SyncRun run) {
		if (run != null && hasText(run.getSyncRunId())) {
			return run.getSyncRunId().trim();
		}
		Long fallbackId = run == null ? null : run.getId();
		return fallbackId == null ? "-" : "ID:" + fallbackId;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private boolean isSyncWindowActive(Date[] window, Date referenceDate) {
		if (window == null || window.length < 2 || window[0] == null || window[1] == null || referenceDate == null) {
			return false;
		}
		return !referenceDate.before(window[0]) && referenceDate.before(window[1]);
	}

	private String valueOrDash(String value) {
		if (value == null || value.trim().isEmpty()) {
			return "-";
		}
		return value;
	}

	private String resolveAuditFieldLabel(String fieldName) {
		if (fieldName == null || fieldName.trim().isEmpty()) {
			return "-";
		}
		String normalized = fieldName.trim().toUpperCase();
		if ("DEUDOR".equals(normalized)) {
			return getString("organizationsSyncAuditFieldDebtor");
		}
		if ("MEMBER".equals(normalized)) {
			return getString("organizationsSyncAuditFieldMember");
		}
		if ("ORGANIZATION".equals(normalized)) {
			return getString("organizationsSyncAuditFieldOrganization");
		}
		return normalized;
	}

	private String resolveAuditValue(SyncAudit audit, boolean oldValue) {
		if (audit == null) {
			return "-";
		}
		String textValue = oldValue ? audit.getOldText() : audit.getNewText();
		return hasText(textValue) ? textValue : "-";
	}

	private String resolveOrganizationsAsyncErrorMessage(AsyncProcessingError asyncError) {
		if (asyncError == null) {
			return getString(KEY_ORGANIZATIONS_MANAGEMENT_ASYNC_PROCESSING_ERROR);
		}
		String messageKey = asyncError.getMessageKey();
		if (messageKey == null || messageKey.trim().isEmpty()) {
			return getString(KEY_ORGANIZATIONS_MANAGEMENT_ASYNC_PROCESSING_ERROR);
		}

		switch (messageKey) {
		case "organizationsManagementUploadMissingColumns":
		case "organizationsManagementUploadNoDataRows":
		case "organizationsManagementUploadFileError":
		case "organizationsManagementUploadUnknownFileType":
		case "organizationsManagementUpsertMissingColumns":
		case "organizationsManagementProcessingInProgress":
			return getString(messageKey);
		case "organizationsManagementUploadNullRequiredFields":
			return new StringResourceModel(messageKey).setParameters(asyncError.getErrorRow()).getString();
		case "organizationsManagementMissingOrgIds":
		case "organizationsManagementDeleteBlockedByNominations":
		case "organizationsManagementDeleteBlockedBySupports":
			return new StringResourceModel(messageKey).setParameters(asyncError.getErrorInfo()).getString();
		default:
			return getString(messageKey, null, getString(KEY_ORGANIZATIONS_MANAGEMENT_ASYNC_PROCESSING_ERROR));
		}
	}

	private AsyncProcessingProgress resolveProgressByType(AsyncProcessingType processingType, AsyncProcessingProgress censusProgress, AsyncProcessingProgress organizationsProgress) {
		if (processingType == AsyncProcessingType.ORGANIZATIONS) {
			return organizationsProgress;
		}
		if (processingType == AsyncProcessingType.CENSUS) {
			return censusProgress;
		}
		return organizationsProgress != null ? organizationsProgress : censusProgress;
	}

	private String buildProcessingInfoMessage(AsyncProcessingType processingType, AsyncProcessingProgress progress) {
		String entityLabel = resolveProcessingEntityLabel(processingType);
		if (progress == null || AsyncProcessingProgress.PHASE_PREPARING.equals(progress.getPhase())) {
			return new StringResourceModel("electionManagementAsyncProcessingPreparingTemplate", this, null).setParameters(entityLabel).getString();
		}
		int totalCreatedRows = Math.max(progress.getTotalCreatedRows(), progress.getCreatedRows());
		int totalUpdatedRows = Math.max(progress.getTotalUpdatedRows(), progress.getUpdatedRows());
		int totalDeletedRows = Math.max(progress.getTotalDeletedRows(), progress.getDeletedRows());
		return new StringResourceModel("electionManagementAsyncProcessingStatusTemplate", this, null)
				.setParameters(entityLabel, totalCreatedRows, totalUpdatedRows, totalDeletedRows).getString();
	}

	private String resolveProcessingEntityLabel(AsyncProcessingType processingType) {
		if (processingType == AsyncProcessingType.ORGANIZATIONS) {
			return getString("electionManagementAsyncProcessingEntityOrganizations");
		}
		if (processingType == AsyncProcessingType.CENSUS) {
			return getString("electionManagementAsyncProcessingEntityCensus");
		}
		return getString("electionManagementAsyncProcessingEntityGeneric");
	}

	private boolean hasSupportLinks(List<SupportNomination> supportNominations) {
		if (supportNominations == null || supportNominations.isEmpty()) {
			return false;
		}
		for (SupportNomination supportNomination : supportNominations) {
			if (supportNomination != null && supportNomination.getSupportingOrganization() != null) {
				return true;
			}
		}
		return false;
	}

	private void clearOrganizationsProcessingFeedbackMessages() {
		IFeedbackMessageFilter filter = new IFeedbackMessageFilter() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean accept(FeedbackMessage message) {
				return message != null && message.getMessage() instanceof OrganizationsProcessingFeedbackMessage;
			}
		};
		getSession().getFeedbackMessages().clear(filter);
		getFeedbackMessages().clear(filter);
	}
}
