package net.lacnic.elections.adminweb.ui.home;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;
import net.lacnic.elections.adminweb.ui.token.page.PublicElectionPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.ElectionLight;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.utils.Constants;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class PublicElectionsDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_LEGACY_MAX_ELECTION_ID = 81;

	private final List<ElectionLight> elections = new ArrayList<>();
	private int legacyMaxElectionId = DEFAULT_LEGACY_MAX_ELECTION_ID;

	public PublicElectionsDashboard(PageParameters params) {
		super(params);

		legacyMaxElectionId = resolveLegacyMaxElectionId();
		loadElections();

		WebMarkupContainer emptyState = new WebMarkupContainer("emptyState");
		emptyState.setVisible(elections.isEmpty());
		add(emptyState);

		add(new ListView<ElectionLight>("electionCards", elections) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<ElectionLight> item) {
				ElectionLight election = item.getModelObject();
				ElectionStatusView status = resolveStatus(election);
				boolean hasPublicToken = hasText(election != null ? election.getPublicElectionToken() : null);
				boolean isLegacy = election != null && election.getElectionId() <= legacyMaxElectionId;
				boolean canOpenDetail = hasPublicToken && !isLegacy;

				item.add(new Label("electionTitle", valueOrDash(resolveTitle(election))));
				item.add(new Label("electionCategory", valueOrDash(resolveCategoryLabel(election))));
				item.add(new Label("electionWindow", valueOrDash(resolveWindowLabel(election))));

				Label statusLabel = new Label("electionStatus", getString(status.labelKey));
				statusLabel.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge " + status.badgeClass));
				item.add(statusLabel);

				PageParameters detailParameters = canOpenDetail
						? UtilsParameters.getToken(election.getPublicElectionToken())
						: new PageParameters();
				BookmarkablePageLink<Void> detailLink = new BookmarkablePageLink<>("electionLink",
						PublicElectionPage.class, detailParameters);
				detailLink.add(AttributeModifier.replace("data-testid", "viewElection-" + election.getElectionId()));
				detailLink.setVisible(canOpenDetail);
				item.add(detailLink);

				boolean showDisabledReason = !canOpenDetail && !isLegacy;
				String disabledReason = showDisabledReason ? getString("publicElectionsNoPublicLink") : "";
				item.add(new Label("electionLinkDisabled", disabledReason).setVisible(showDisabledReason));
			}
		});
	}

	@Override
	protected Class validateToken(PageParameters params) {
		return null;
	}

	@Override
	protected boolean isPublicFailedAccessRateLimitEnabled() {
		return false;
	}

	private void loadElections() {
		List<ElectionLight> availableElections = AppContext.getInstance().getMonitorBeanRemote()
				.getElectionsLightAllOrderStartDateDesc();
		if (availableElections == null) {
			return;
		}

		for (ElectionLight election : availableElections) {
			if (!shouldIncludeElection(election)) {
				continue;
			}
			elections.add(election);
		}
	}

	private boolean shouldIncludeElection(ElectionLight election) {
		return election != null && election.getCategory() != ElectionCategory.TEST;
	}

	private int resolveLegacyMaxElectionId() {
		try {
			Parameter parameter = AppContext.getInstance().getManagerBeanRemote()
					.getParameter(Constants.PUBLIC_ELECTION_LEGACY_MAX_ELECTION_ID);
			if (parameter == null || !hasText(parameter.getValue())) {
				return DEFAULT_LEGACY_MAX_ELECTION_ID;
			}
			return Integer.parseInt(parameter.getValue().trim());
		} catch (Exception e) {
			return DEFAULT_LEGACY_MAX_ELECTION_ID;
		}
	}

	private String resolveTitle(ElectionLight election) {
		if (election == null) {
			return null;
		}

		String language = getLanguage();
		String title = language != null ? election.getTitle(language) : null;
		if (hasText(title)) {
			return title;
		}
		return election.getTitleSpanish();
	}

	private String resolveCategoryLabel(ElectionLight election) {
		if (election == null || election.getCategory() == null) {
			return "-";
		}

		ElectionCategory category = election.getCategory();
		String key = "publicElectionsCategory." + category.name();
		String label = getString(key, null, null);
		return hasText(label) ? label : category.name();
	}

	private String resolveWindowLabel(ElectionLight election) {
		if (election == null) {
			return getString("publicElectionsDatePending");
		}

		String start = election.getVotingPeriodStartDateString();
		String end = election.getVotingPeriodEndDateString();
		if (hasText(start) && hasText(end)) {
			return start + " - " + end;
		}
		if (hasText(start)) {
			return start;
		}
		return getString("publicElectionsDatePending");
	}

	private ElectionStatusView resolveStatus(ElectionLight election) {
		if (election == null) {
			return new ElectionStatusView("publicElectionsStatusDraft", "text-bg-secondary");
		}
		if (!hasText(election.getPublicElectionToken())) {
			return new ElectionStatusView("publicElectionsStatusDraft", "text-bg-secondary");
		}
		if (election.isClosed()) {
			return new ElectionStatusView("publicElectionsStatusClosed", "text-bg-dark");
		}
		if (election.isEnabledToVote()) {
			return new ElectionStatusView("publicElectionsStatusOpen", "text-bg-success");
		}
		if (election.isFinished()) {
			return new ElectionStatusView("publicElectionsStatusFinished", "text-bg-primary");
		}
		if (election.isStarted()) {
			return new ElectionStatusView("publicElectionsStatusInProgress", "text-bg-info");
		}
		return new ElectionStatusView("publicElectionsStatusUpcoming", "text-bg-warning");
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private static class ElectionStatusView implements java.io.Serializable {
		private static final long serialVersionUID = 1L;

		private final String labelKey;
		private final String badgeClass;

		private ElectionStatusView(String labelKey, String badgeClass) {
			this.labelKey = labelKey;
			this.badgeClass = badgeClass;
		}
	}
}
