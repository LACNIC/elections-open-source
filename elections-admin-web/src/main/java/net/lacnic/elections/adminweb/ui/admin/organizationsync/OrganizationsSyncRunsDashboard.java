package net.lacnic.elections.adminweb.ui.admin.organizationsync;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardManagerBasePage;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.SyncRun;

public class OrganizationsSyncRunsDashboard extends DashboardManagerBasePage {

	private static final long serialVersionUID = -6513654928032421764L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	public OrganizationsSyncRunsDashboard(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));

		List<Election> elections = AppContext.getInstance().getManagerBeanRemote().getElectionsAllOrderCreationDate();
		Collections.sort(elections, Comparator.comparing(Election::getCreationDate, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(Election::getElectionId, Comparator.reverseOrder()));
		List<Long> electionIds = new ArrayList<>();
		for (Election election : elections) {
			if (election != null) {
				electionIds.add(election.getElectionId());
			}
		}

		Map<Long, Date[]> syncWindowsByElectionId = AppContext.getInstance().getManagerBeanRemote().getMilacnicSyncCalendarWindowByElectionIds(electionIds);
		Map<Long, SyncRun> latestSyncByElectionId = AppContext.getInstance().getManagerBeanRemote().getLatestSyncRunsByElectionIds(electionIds);
		List<SyncRun> runs = AppContext.getInstance().getManagerBeanRemote().getSyncRuns(500);
		Collections.sort(runs, Comparator.comparing(SyncRun::getSyncAt, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(SyncRun::getId, Comparator.reverseOrder()));

		add(new Link<Void>("forceAllSyncLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				try {
					AppContext.getInstance().getManagerBeanRemote().processMilacnicOrganizationsSync();
					getSession().success(getString("organizationsSyncControlForceAllSuccess"));
				} catch (Exception e) {
					appLogger.error("Error forcing global MiLACNIC sync from organizations sync control dashboard", e);
					getSession().error(getString("organizationsSyncControlForceAllError"));
				}
				setResponsePage(OrganizationsSyncRunsDashboard.class);
			}
		});

		List<SyncByElectionRow> byElectionRows = new ArrayList<>();
		Date now = new Date();
		for (Election election : elections) {
			if (election == null) {
				continue;
			}
			Date[] window = syncWindowsByElectionId.get(election.getElectionId());
			SyncRun latestRun = latestSyncByElectionId.get(election.getElectionId());
			byElectionRows.add(new SyncByElectionRow(election, window, latestRun, now));
		}

		add(new WebMarkupContainer("byElectionEmpty").setVisible(byElectionRows.isEmpty()));
		add(new ListView<SyncByElectionRow>("byElectionRows", byElectionRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<SyncByElectionRow> item) {
				SyncByElectionRow row = item.getModelObject();
				item.add(new Label("colElectionId", row.election.getElectionId()));
				item.add(new Label("colElectionTitle", valueOrDash(row.election.getTitleSpanish())));
				item.add(new Label("colMode", row.election.isManageOrganizationsManual() ? getString("organizationsSyncControlModeManual") : getString("organizationsSyncControlModeAuto")));
				item.add(new Label("colSyncWindow", resolveWindowText(row.windowStart, row.windowEnd, row.election.getDiffUTC())));
				item.add(new Label("colWindowState", row.windowActive ? getString("organizationsSyncControlWindowActive") : getString("organizationsSyncControlWindowInactive")));
				item.add(new Label("colLatestSyncAt", formatSyncDate(row.latestRun != null ? row.latestRun.getSyncAt() : null, row.election.getDiffUTC())));
				item.add(new Label("colLatestStatus", resolveSyncStatusLabel(row.latestRun != null ? row.latestRun.getStatus() : null)));
				item.add(new Label("colLatestMetrics", buildSyncMetricsText(row.latestRun)));
				String runInWindowDisabledMessage = !row.forceAvailable ? getString("organizationsSyncControlForceElectionNotAvailable")
						: getString("organizationsSyncControlRunInWindowOutOfWindow");
				item.add(new Label("colActionInWindowDisabled", runInWindowDisabledMessage).setVisible(!(row.forceAvailable && row.windowActive)));
				item.add(new Label("colActionOutOfWindowDisabled", row.forceAvailable ? getString("organizationsSyncControlRunOutOfWindowInWindow")
						: getString("organizationsSyncControlForceElectionNotAvailable")).setVisible(!(row.forceAvailable && !row.windowActive)));
				item.add(new Link<Void>("runSyncElectionInWindowLink") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						try {
							boolean queued = AppContext.getInstance().getManagerBeanRemote().queueMilacnicOrganizationsSyncForElectionInWindow(row.election.getElectionId(), SecurityUtils.getUserAdminId(),
									SecurityUtils.getClientIp());
							if (queued) {
								getSession().success(new StringResourceModel("organizationsSyncControlRunInWindowSuccess", OrganizationsSyncRunsDashboard.this, null)
										.setParameters(row.election.getElectionId()).getString());
							} else {
								getSession().error(getString("organizationsManagementProcessingInProgress"));
							}
						} catch (Exception e) {
							appLogger.error("Error running MiLACNIC sync in window for election {}", row.election.getElectionId(), e);
							getSession().error(new StringResourceModel("organizationsSyncControlRunInWindowError", OrganizationsSyncRunsDashboard.this, null)
									.setParameters(row.election.getElectionId()).getString());
						}
						setResponsePage(OrganizationsSyncRunsDashboard.class);
					}
				}.setVisible(row.forceAvailable && row.windowActive));
				item.add(new Link<Void>("runSyncElectionOutOfWindowLink") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						try {
							boolean queued = AppContext.getInstance().getManagerBeanRemote().queueMilacnicOrganizationsSyncForElection(row.election.getElectionId(), SecurityUtils.getUserAdminId(),
									SecurityUtils.getClientIp());
							if (queued) {
								getSession().success(new StringResourceModel("organizationsSyncControlRunOutOfWindowSuccess", OrganizationsSyncRunsDashboard.this, null)
										.setParameters(row.election.getElectionId()).getString());
							} else {
								getSession().error(getString("organizationsManagementProcessingInProgress"));
							}
						} catch (Exception e) {
							appLogger.error("Error forcing MiLACNIC sync for election {}", row.election.getElectionId(), e);
							getSession().error(new StringResourceModel("organizationsSyncControlRunOutOfWindowError", OrganizationsSyncRunsDashboard.this, null)
									.setParameters(row.election.getElectionId()).getString());
						}
						setResponsePage(OrganizationsSyncRunsDashboard.class);
					}
				}.setVisible(row.forceAvailable && !row.windowActive));
			}
		}.setVisible(!byElectionRows.isEmpty()));

		add(new WebMarkupContainer("runsEmpty").setVisible(runs.isEmpty()));
		add(new ListView<SyncRun>("runsRows", runs) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<SyncRun> item) {
				SyncRun run = item.getModelObject();
				Election election = run.getElection();
				int diffUtc = election != null ? election.getDiffUTC() : 0;
				item.add(new Label("runSyncAt", formatSyncDate(run.getSyncAt(), diffUtc)));
					item.add(new Label("runElection", buildRunElectionLabel(election)));
					item.add(new Label("runStatus", resolveSyncStatusLabel(run.getStatus())));
				item.add(new Label("runMetrics", buildSyncMetricsText(run)));
					item.add(new Label("runDuration", run.getDurationMs() == null ? "-" : run.getDurationMs() + " ms"));
					item.add(new Label("runWs", buildWsMetricsText(run)));
				item.add(new Label("runHealth", valueOrDash(run.getHealthIndicators())));
				item.add(new Label("runMessage", valueOrDash(run.getMessage())));
				item.add(new Label("runSyncRunId", valueOrDash(run.getSyncRunId())));
			}
		}.setVisible(!runs.isEmpty()));
	}

	private String buildRunElectionLabel(Election election) {
		if (election == null) {
			return "-";
		}
		return election.getElectionId() + " - " + valueOrDash(election.getTitleSpanish());
	}

	private String buildWsMetricsText(SyncRun run) {
		String statusCode = run.getWsStatusCode() == null ? "-" : String.valueOf(run.getWsStatusCode());
		String elapsed = run.getWsElapsedMs() == null ? "-" : run.getWsElapsedMs() + " ms";
		return new StringResourceModel("organizationsSyncControlWsValue", this, null).setParameters(statusCode, elapsed).getString();
	}

	private String resolveWindowText(Date startDate, Date endDate, int diffUtc) {
		if (startDate == null && endDate == null) {
			return getString("organizationsSyncControlWindowNotConfigured");
		}
		return formatSyncDate(startDate, diffUtc) + " -> " + formatSyncDate(endDate, diffUtc);
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

	private String buildSyncMetricsText(SyncRun run) {
		int processed = run == null ? 0 : run.getProcessedRows();
		int created = run == null ? 0 : run.getCreatedRows();
		int updated = run == null ? 0 : run.getUpdatedRows();
		int deleted = run == null ? 0 : run.getDeletedRows();
		return new StringResourceModel("organizationsSyncRunsMetricsValue", this, null).setParameters(processed, created, updated, deleted).getString();
	}

	private String valueOrDash(String value) {
		if (value == null || value.trim().isEmpty()) {
			return "-";
		}
		return value;
	}

	private static final class SyncByElectionRow implements Serializable {
		private static final long serialVersionUID = 1L;
		private final Election election;
		private final Date windowStart;
		private final Date windowEnd;
		private final boolean windowActive;
		private final SyncRun latestRun;
		private final boolean forceAvailable;

		private SyncByElectionRow(Election election, Date[] window, SyncRun latestRun, Date now) {
			this.election = election;
			this.windowStart = window != null && window.length > 0 ? window[0] : null;
			this.windowEnd = window != null && window.length > 1 ? window[1] : null;
			boolean insideWindow = false;
			if (now != null && windowStart != null && windowEnd != null) {
				insideWindow = !now.before(windowStart) && now.before(windowEnd);
			}
			this.windowActive = insideWindow;
			this.latestRun = latestRun;
			this.forceAvailable = election != null && !election.isClosed() && !election.isManageOrganizationsManual();
		}
	}
}
