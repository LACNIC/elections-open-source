package net.lacnic.elections.adminweb.ui.admin.election.census;

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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PageableListView;
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
import net.lacnic.elections.adminweb.ui.admin.election.calendarmodule.ElectionCalendarModuleDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.organizations.ElectionOrganizationsDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.data.AsyncProcessingError;
import net.lacnic.elections.data.AsyncProcessingProgress;
import net.lacnic.elections.data.AsyncProcessingType;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.UserVoterLite;
import net.lacnic.elections.domain.pre.SyncAudit;
import net.lacnic.elections.domain.pre.SyncRun;
import net.lacnic.elections.exception.CensusValidationException;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class ElectionCensusDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1538700499093907394L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final int CENSUS_SYNC_RUNS_PAGE_SIZE = 10;
	private static final String KEY_CENSUS_MANAGEMENT_ASYNC_PROCESSING_ERROR = "censusManagementAsyncProcessingError";

	private Election election;
	private UserVoter userVoter = new UserVoter();
	private String expandedSyncRunKey;

	private static final class CensusProcessingFeedbackMessage implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String text;

		private CensusProcessingFeedbackMessage(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public ElectionCensusDashboard(PageParameters params) {
		super(params);

		// Check if election is closed (user might be using a direct link to get to this
		// page)
		Election election = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));
		if (election.isClosed()) {
			setResponsePage(ErrorElectionClosed.class);
		} else {
			setElection(election);
			add(new FeedbackPanel("feedback"));
			long electionId = election.getElectionId();
			boolean manageVotersManual = election.isManageVotersManual();
			AsyncProcessingError asyncError = AppContext.getInstance().getManagerBeanRemote().getElectionCensusProcessingError(electionId);
			AsyncProcessingType processingType = AppContext.getInstance().getManagerBeanRemote().getElectionProcessingType(electionId);
			AsyncProcessingProgress censusProgress = AppContext.getInstance().getManagerBeanRemote().getElectionCensusProcessingProgress(electionId);
			AsyncProcessingProgress organizationsProgress = AppContext.getInstance().getManagerBeanRemote().getElectionOrganizationsProcessingProgress(electionId);
			AsyncProcessingProgress activeProgress = resolveProgressByType(processingType, censusProgress, organizationsProgress);
			boolean censusProcessing = AppContext.getInstance().getManagerBeanRemote().isElectionCensusProcessing(electionId);
			appLogger.info("Census dashboard status. electionId={}, processing={}, processingType={}, progressProcessedRows={}, progressTotalRows={}, asyncErrorKey={}",
					electionId,
					censusProcessing,
					processingType,
					activeProgress != null ? activeProgress.getProcessedRows() : null,
					activeProgress != null ? activeProgress.getTotalRows() : null,
					asyncError != null ? asyncError.getMessageKey() : null);
			clearCensusProcessingFeedbackMessages();
			if (censusProcessing) {
				info(new CensusProcessingFeedbackMessage(buildProcessingInfoMessage(processingType, activeProgress)));
			} else if (asyncError != null) {
				SecurityUtils.clearAsyncCompletionFeedbackPending(electionId, AsyncProcessingType.CENSUS);
				error(resolveCensusAsyncErrorMessage(asyncError));
				appLogger.warn("Census async processing error shown in dashboard. electionId={}, messageKey={}, errorRow={}, errorInfo={}, technicalDetail={}, createdAt={}", electionId, asyncError.getMessageKey(),
						asyncError.getErrorRow(), asyncError.getErrorInfo(), asyncError.getTechnicalDetail(), asyncError.getCreatedAt());
			} else if (SecurityUtils.consumeAsyncCompletionFeedbackPending(electionId, AsyncProcessingType.CENSUS)) {
				info(getString("censusManagementAsyncProcessingSuccess"));
			}
			add(new ManageElectionTabsPanel("tabsPanel", election, "tabCensus"));
			boolean manageVotersFromOrganizations = !election.isManageVotersManual();
			boolean automaticSyncControlsAllowed = manageVotersFromOrganizations;
			Map<Long, Date[]> syncWindowByElectionId = AppContext.getInstance().getManagerBeanRemote().getMilacnicSyncCalendarWindowByElectionIds(Collections.singletonList(election.getElectionId()));
			Date[] syncWindow = syncWindowByElectionId == null ? null : syncWindowByElectionId.get(election.getElectionId());
			boolean syncWindowActive = isSyncWindowActive(syncWindow, new Date());
			List<SyncRun> censusSyncRunsResult = AppContext.getInstance().getManagerBeanRemote().getElectionAutomaticCensusSyncRuns(election.getElectionId(), 200);
			List<SyncRun> censusSyncRuns = censusSyncRunsResult == null ? new ArrayList<>() : new ArrayList<>(censusSyncRunsResult);
			Collections.sort(censusSyncRuns, Comparator.comparing(SyncRun::getSyncAt, Comparator.nullsLast(Comparator.reverseOrder()))
					.thenComparing(SyncRun::getId, Comparator.reverseOrder()));
			SyncRun latestCensusSyncRun = censusSyncRuns.isEmpty() ? null : censusSyncRuns.get(0);
			WebMarkupContainer autoVotersInfo = new WebMarkupContainer("autoVotersInfo");
			autoVotersInfo.setVisible(manageVotersFromOrganizations);
			WebMarkupContainer autoVotersAlert = new WebMarkupContainer("autoVotersAlert");
			autoVotersAlert.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  buildSyncInfoAlertCssClass(latestCensusSyncRun)));
			autoVotersInfo.add(autoVotersAlert);
			autoVotersAlert.add(new Label("autoVotersInfoText", getSyncInfoText(latestCensusSyncRun, "censusManagementAutoFromOrganizationsInfo", "censusSyncInfoErrorText",
					"censusSyncInfoFirstRunPendingText")));
			autoVotersAlert.add(new Label("syncInfoLastSyncAt", formatSyncDate(latestCensusSyncRun != null ? latestCensusSyncRun.getSyncAt() : null, election.getDiffUTC())));
			autoVotersAlert.add(new Label("syncInfoStatus", resolveSyncInfoStatusLabel(latestCensusSyncRun)));
			autoVotersAlert.add(new Label("syncInfoMetrics", buildLatestCensusSyncMetricsText(latestCensusSyncRun)));
			autoVotersAlert.add(new Label("syncInfoMessage", resolveSyncInfoMessage(latestCensusSyncRun)));
			add(autoVotersInfo);

			WebMarkupContainer electionCensusForm = new WebMarkupContainer("electionCensusForm");
			add(electionCensusForm);

			WebMarkupContainer uploadSection = new WebMarkupContainer("uploadSection");
			uploadSection.setVisible(!manageVotersFromOrganizations && !censusProcessing);
			electionCensusForm.add(uploadSection);

			Form<Void> uploadCensusForm = new Form<>("uploadCensusForm");
			uploadSection.add(uploadCensusForm);

			UploadCensusFilePanel uploadCensusFilePanel = new UploadCensusFilePanel("uploadCensusFilePanel", election);
			uploadCensusFilePanel.setOutputMarkupId(true);
			uploadCensusForm.add(uploadCensusFilePanel);

			WebMarkupContainer userVoterCard = new WebMarkupContainer("userVoterCard");
			userVoterCard.setVisible(!manageVotersFromOrganizations && !censusProcessing);
			electionCensusForm.add(userVoterCard);

			Form<Void> addUserVoterForm = new Form<>("addUserVoterForm");
			userVoterCard.add(addUserVoterForm);

			boolean orgIdEnabled = true;
			boolean orgIdRequired = true;
			addUserVoterForm.add(new AddUserVoterPanel("addUserVoterPanel", userVoter, election.getElectionId(), false, orgIdEnabled, orgIdRequired));
			addUserVoterForm.add(new Button("addUserVoter") {
				private static final long serialVersionUID = -5396612472447055621L;

				@Override
				public void onSubmit() {
					super.onSubmit();
					try {
						AppContext.getInstance().getManagerBeanRemote().addUserVoter(getElection().getElectionId(), getUserVoter(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						getSession().info(getString("censusManagementSuccess"));
						setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(getElection().getElectionId()));
					} catch (CensusValidationException e) {
						error(getString(e.getMessage()));
					}
				}
			});

			Link<Void> markDoneNext = new Link<Void>("markDoneNext") {
				private static final long serialVersionUID = 1401899220065440256L;

				@Override
				public void onClick() {
					AppContext.getInstance().getManagerBeanRemote().persistElectionElectorsSet(election.getElectionId(), election.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					setResponsePage(ElectionCalendarModuleDashboard.class, UtilsParameters.getId(election.getElectionId()));
				}
			};
			electionCensusForm.add(markDoneNext);

			Link<Void> skip = new Link<Void>("skip") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						setResponsePage(ElectionCalendarModuleDashboard.class, UtilsParameters.getId(election.getElectionId()));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			electionCensusForm.add(skip);

			Link<Void> back = new Link<Void>("back") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(election.getElectionId()));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			electionCensusForm.add(back);

			if (censusProcessing) {
				WebMarkupContainer hiddenCensusList = new WebMarkupContainer("censusListPanel");
				hiddenCensusList.setVisible(false);
				add(hiddenCensusList);
			} else {
				add(new AjaxLazyLoadPanel<CensusListPanel>("censusListPanel") {
					private static final long serialVersionUID = 8165854022863553760L;

					@Override
					public CensusListPanel getLazyLoadComponent(String markupId) {
						return new CensusListPanel(markupId, election);
					}
				});
			}

			WebMarkupContainer censusSyncRunsSection = new WebMarkupContainer("censusSyncRunsSection");
			censusSyncRunsSection.setVisible(manageVotersFromOrganizations);
			censusSyncRunsSection.setOutputMarkupId(true);
			add(censusSyncRunsSection);
			Label syncWindowStatus = new Label("syncWindowStatus", getString(syncWindowActive ? "organizationsSyncControlWindowStatusIn" : "organizationsSyncControlWindowStatusOut"));
			syncWindowStatus.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  syncWindowActive ? "badge bg-success-subtle text-success border border-success-subtle" : "badge bg-warning-subtle text-warning border border-warning-subtle"));
			censusSyncRunsSection.add(syncWindowStatus);
			AutomaticCensusSyncActionsPanel automaticCensusSyncActionsPanel = new AutomaticCensusSyncActionsPanel("automaticCensusSyncActionsPanel", election, syncWindowActive && !censusProcessing);
			automaticCensusSyncActionsPanel.setVisible(automaticSyncControlsAllowed);
			censusSyncRunsSection.add(automaticCensusSyncActionsPanel);
			censusSyncRunsSection.add(new WebMarkupContainer("censusSyncRunsEmpty").setVisible(censusSyncRuns.isEmpty()));
			PageableListView<SyncRun> censusSyncRunsRows = new PageableListView<SyncRun>("censusSyncRunsRows", censusSyncRuns, CENSUS_SYNC_RUNS_PAGE_SIZE) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<SyncRun> item) {
					SyncRun run = item.getModelObject();
					String runKey = buildSyncRunKey(run);
					boolean detailVisible = runKey.equals(expandedSyncRunKey);
					List<SyncAudit> auditsForRun = detailVisible ? loadSyncAuditsForRun(electionId, run) : Collections.emptyList();
					item.setOutputMarkupId(true);
					item.add(new Label("censusSyncRunsTimestamp", formatSyncDate(run.getSyncAt(), election.getDiffUTC())));
					item.add(new Label("censusSyncRunsStatus", resolveSyncStatusLabel(run.getStatus())));
					item.add(new Label("censusSyncRunsMetrics", buildCensusSyncRunsMetricsText(run)));
					item.add(new Label("censusSyncRunsDuration", run.getDurationMs() == null ? "-" : run.getDurationMs() + " ms"));
					item.add(new Label("censusSyncRunsRunId", valueOrDash(run.getSyncRunId())));
					item.add(new Label("censusSyncRunsMessage", resolveCensusSyncMessage(run.getMessage())));
					AjaxLink<Void> toggleDetail = new AjaxLink<Void>("censusSyncRunsToggleDetail") {
						private static final long serialVersionUID = 1L;

						@Override
						public void onClick(AjaxRequestTarget target) {
							expandedSyncRunKey = runKey.equals(expandedSyncRunKey) ? null : runKey;
							target.add(censusSyncRunsSection);
						}
					};
					toggleDetail.add(new Label("censusSyncRunsToggleDetailLabel", runKey.equals(expandedSyncRunKey)
							? getString("censusSyncControlRunsDetailHide")
							: getString("censusSyncControlRunsDetailShow")));
					item.add(toggleDetail);

					WebMarkupContainer detailRow = new WebMarkupContainer("censusSyncRunsDetailRow");
					detailRow.setVisible(detailVisible);
					detailRow.add(new WebMarkupContainer("censusSyncRunsDetailEmpty").setVisible(detailVisible && auditsForRun.isEmpty()));
					detailRow.add(new ListView<SyncAudit>("censusSyncRunsDetailRows", auditsForRun) {
						private static final long serialVersionUID = 1L;

						@Override
						protected void populateItem(ListItem<SyncAudit> detailItem) {
							SyncAudit audit = detailItem.getModelObject();
							detailItem.add(new Label("censusSyncRunsDetailTimestamp", formatSyncDate(audit.getEventDate(), election.getDiffUTC())));
							detailItem.add(new Label("censusSyncRunsDetailOrgId", valueOrDash(audit.getOrgId())));
							detailItem.add(new Label("censusSyncRunsDetailField", resolveAuditFieldLabel(audit.getFieldName())));
							detailItem.add(new Label("censusSyncRunsDetailOldValue", resolveAuditValue(audit, true)));
							detailItem.add(new Label("censusSyncRunsDetailNewValue", resolveAuditValue(audit, false)));
						}
					}.setVisible(detailVisible && !auditsForRun.isEmpty()));
					item.add(detailRow);
				}
			};
			censusSyncRunsRows.setVisible(!censusSyncRuns.isEmpty());
			censusSyncRunsSection.add(censusSyncRunsRows);
			PagingNavigator censusSyncRunsPager = new PagingNavigator("censusSyncRunsPager", censusSyncRunsRows);
			censusSyncRunsPager.setVisible(censusSyncRuns.size() > CENSUS_SYNC_RUNS_PAGE_SIZE);
			censusSyncRunsSection.add(censusSyncRunsPager);

			List<UserVoterLite> advancedUserVotersResult = AppContext.getInstance().getManagerBeanRemote().getElectionUserVotersLite(electionId);
			List<UserVoterLite> advancedUserVoters = advancedUserVotersResult == null ? Collections.emptyList() : advancedUserVotersResult;
			boolean hasUserVoters = !advancedUserVoters.isEmpty();
			boolean deleteBlockedByVotes = hasVotedUsers(advancedUserVoters);
			add(new CensusAdvancedActionsPanel("censusAdvancedActionsPanel", election, !censusProcessing, hasUserVoters, deleteBlockedByVotes));
		}

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

	private String formatSyncDate(Date syncDate, int diffUtc) {
		if (syncDate == null) {
			return valueOrDash(null);
		}
		Date displayDate = new DateTime(syncDate).plusHours(diffUtc).toDate();
		return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(displayDate) + " (UTC)";
	}

	private boolean isSyncWindowActive(Date[] syncWindow, Date referenceDate) {
		if (syncWindow == null || syncWindow.length < 2 || syncWindow[0] == null || syncWindow[1] == null || referenceDate == null) {
			return false;
		}
		return !referenceDate.before(syncWindow[0]) && referenceDate.before(syncWindow[1]);
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

	private String buildCensusSyncRunsMetricsText(SyncRun run) {
		int processedRows = run.getProcessedRows();
		int createdRows = run.getCreatedRows();
		int updatedRows = run.getUpdatedRows();
		int deletedRows = run.getDeletedRows();
		return new StringResourceModel("censusSyncRunsMetricsValue", this, null).setParameters(processedRows, createdRows, updatedRows, deletedRows).getString();
	}

	private String buildLatestCensusSyncMetricsText(SyncRun run) {
		if (run == null) {
			return valueOrDash(null);
		}
		int processedRows = run.getProcessedRows();
		int createdRows = run.getCreatedRows();
		int updatedRows = run.getUpdatedRows();
		int deletedRows = run.getDeletedRows();
		return new StringResourceModel("censusSyncInfoMetricsValue", this, null).setParameters(processedRows, createdRows, updatedRows, deletedRows).getString();
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

	private String resolveCensusSyncMessage(String rawMessage) {
		return valueOrDash(rawMessage);
	}

	private String resolveAuditFieldLabel(String fieldName) {
		if (!hasText(fieldName)) {
			return "-";
		}
		String normalized = fieldName.trim().toUpperCase();
		if ("CENSUS".equals(normalized)) {
			return getString("censusSyncAuditFieldVoter");
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

	private String valueOrDash(String value) {
		if (value == null || value.trim().isEmpty()) {
			return "-";
		}
		return value;
	}

	private String resolveCensusAsyncErrorMessage(AsyncProcessingError asyncError) {
		if (asyncError == null) {
			return getString(KEY_CENSUS_MANAGEMENT_ASYNC_PROCESSING_ERROR);
		}
		String messageKey = asyncError.getMessageKey();
		if (messageKey == null || messageKey.trim().isEmpty()) {
			return getString(KEY_CENSUS_MANAGEMENT_ASYNC_PROCESSING_ERROR);
		}

		switch (messageKey) {
		case "censusManagementUploadMissingColumns":
		case "censusManagementUploadNoDataRows":
		case "censusManagementUploadFileError":
		case "censusManagementUploadUnknownFileType":
		case "censusManagementUploadOrgIdRequired":
		case "censusManagementProcessingInProgress":
			return getString(messageKey);
		case "censusManagementUploadNullRequiredFields":
			return new StringResourceModel(messageKey).setParameters(asyncError.getErrorRow(), asyncError.getErrorInfo()).getString();
		case "censusManagementUploadWrongVoteAmount":
			return new StringResourceModel(messageKey).setParameters(asyncError.getErrorRow()).getString();
		case "censusManagementUploadWrongLanguage":
		case "censusManagementUploadWrongEmail":
		case "censusManagementUploadDuplicateOrgId":
		case "censusManagementUploadWrongCountry":
			return new StringResourceModel(messageKey).setParameters(asyncError.getErrorInfo(), asyncError.getErrorRow()).getString();
		case "censusManagementOverwriteBlockedByVotes":
			return new StringResourceModel(messageKey).setParameters(asyncError.getErrorInfo()).getString();
		default:
			return getString(messageKey, null, getString(KEY_CENSUS_MANAGEMENT_ASYNC_PROCESSING_ERROR));
		}
	}

	private AsyncProcessingProgress resolveProgressByType(AsyncProcessingType processingType, AsyncProcessingProgress censusProgress, AsyncProcessingProgress organizationsProgress) {
		if (processingType == AsyncProcessingType.ORGANIZATIONS) {
			return organizationsProgress;
		}
		if (processingType == AsyncProcessingType.CENSUS) {
			return censusProgress;
		}
		return censusProgress != null ? censusProgress : organizationsProgress;
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

	private boolean hasVotedUsers(List<UserVoterLite> userVoters) {
		if (userVoters == null || userVoters.isEmpty()) {
			return false;
		}
		for (UserVoterLite userVoter : userVoters) {
			if (userVoter != null && userVoter.isVoted()) {
				return true;
			}
		}
		return false;
	}

	private void clearCensusProcessingFeedbackMessages() {
		IFeedbackMessageFilter filter = new IFeedbackMessageFilter() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean accept(FeedbackMessage message) {
				return message != null && message.getMessage() instanceof CensusProcessingFeedbackMessage;
			}
		};
		getSession().getFeedbackMessages().clear(filter);
		getFeedbackMessages().clear(filter);
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public UserVoter getUserVoter() {
		return userVoter;
	}

	public void setUserVoter(UserVoter userVoter) {
		this.userVoter = userVoter;
	}

}
