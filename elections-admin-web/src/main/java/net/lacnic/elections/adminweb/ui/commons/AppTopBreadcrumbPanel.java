package net.lacnic.elections.adminweb.ui.commons;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.extensions.breadcrumb.BreadCrumbBar;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.extensions.breadcrumb.panel.BreadCrumbParticipantDelegate;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.domain.Election;

public class AppTopBreadcrumbPanel extends Panel {

	private static final long serialVersionUID = 1680967959752642982L;
	private static final String DEFAULT_PAGE_HEADING_KEY = "pageHeading";
	private static final String PAGE_HEADING_KEY_PREFIX = "pageHeading.";
	private static final String DEFAULT_PAGE_TITLE_KEY = "pageTitle";
	private static final String PAGE_TITLE_KEY_PREFIX = "pageTitle.";
	private static final String OPTIONAL_RESOURCE_NOT_FOUND = "__optional_resource_not_found__";
	private static final String TITLE_SEPARATOR = " - ";

	private final long electionId;
	private final boolean contextualTitleEnabled;
	private transient boolean electionTitleResolved;
	private transient String electionTitle;

	public AppTopBreadcrumbPanel(String id) {
		this(id, 0L, false);
	}

	public AppTopBreadcrumbPanel(String id, long electionId) {
		this(id, electionId, true);
	}

	private AppTopBreadcrumbPanel(String id, long electionId, boolean contextualTitleEnabled) {
		super(id);
		this.electionId = electionId;
		this.contextualTitleEnabled = contextualTitleEnabled;

		BreadCrumbBar breadCrumbBar = new BreadCrumbBar("breadcrumb");
		add(breadCrumbBar);
		add(new Label("pageHeading", createHeadingTitleModel()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(contextualTitleEnabled || hasExplicitHeadingTitle());
			}
		});

		WebMarkupContainer homeCrumb = new WebMarkupContainer("homeCrumb");
		homeCrumb.setVisible(false);
		add(homeCrumb);

		WebMarkupContainer currentCrumb = new WebMarkupContainer("currentCrumb");
		currentCrumb.setVisible(false);
		add(currentCrumb);

		IBreadCrumbParticipant home = new BreadCrumbParticipantDelegate(homeCrumb) {
			private static final long serialVersionUID = 3983244852888846201L;

			@Override
			public IModel<String> getTitle() {
				return Model.of("Home");
			}
		};

		IBreadCrumbParticipant current = new BreadCrumbParticipantDelegate(currentCrumb) {
			private static final long serialVersionUID = 7471491883826580928L;

			@Override
			public IModel<String> getTitle() {
				return createPageTitleModel();
			}
		};

		breadCrumbBar.setActive(home);
		breadCrumbBar.setActive(current);
	}

	private IModel<String> createHeadingTitleModel() {
		return new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return resolveHeadingTitle();
			}
		};
	}

	private IModel<String> createPageTitleModel() {
		return new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return resolvePageTitle();
			}
		};
	}

	private String resolveHeadingTitle() {
		String explicitHeadingTitle = resolveExplicitHeadingTitle();
		if (hasText(explicitHeadingTitle)) {
			return explicitHeadingTitle;
		}

		String pageTitle = resolvePageTitle();
		String resolvedElectionTitle = resolveElectionTitle();
		if (hasText(resolvedElectionTitle)) {
			return resolvedElectionTitle + TITLE_SEPARATOR + pageTitle;
		}
		return pageTitle;
	}

	private boolean hasExplicitHeadingTitle() {
		return hasText(resolveExplicitHeadingTitle());
	}

	private String resolveExplicitHeadingTitle() {
		String classHeading = getOptionalString(PAGE_HEADING_KEY_PREFIX + getPage().getClass().getSimpleName());
		if (hasText(classHeading)) {
			return classHeading;
		}
		String defaultHeading = getOptionalString(DEFAULT_PAGE_HEADING_KEY);
		if (hasText(defaultHeading)) {
			return defaultHeading;
		}
		return null;
	}

	private String resolvePageTitle() {
		String classTitle = getOptionalString(PAGE_TITLE_KEY_PREFIX + getPage().getClass().getSimpleName());
		if (hasText(classTitle)) {
			return classTitle;
		}
		String defaultTitle = getOptionalString(DEFAULT_PAGE_TITLE_KEY);
		if (hasText(defaultTitle)) {
			return defaultTitle;
		}
		return humanizeClassName(getPage().getClass().getSimpleName());
	}

	private String resolveElectionTitle() {
		if (electionId <= 0) {
			return null;
		}
		if (!electionTitleResolved) {
			electionTitleResolved = true;
			Election election = AppContext.getInstance().getManagerBeanRemote().getElection(electionId);
			if (election != null) {
				String language = SecurityUtils.getLocale() != null ? SecurityUtils.getLocale().getLanguage() : null;
				electionTitle = language != null ? election.getTitle(language) : null;
				if (!hasText(electionTitle)) {
					electionTitle = election.getTitleSpanish();
				}
			}
		}
		return electionTitle;
	}

	private String humanizeClassName(String className) {
		if (!hasText(className)) {
			return DEFAULT_PAGE_TITLE_KEY;
		}
		String title = className.replace("Dashboard", "").replace("Page", "");
		title = title.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
		return hasText(title) ? title : className;
	}

	private String getOptionalString(String key) {
		String value = getString(key, null, OPTIONAL_RESOURCE_NOT_FOUND);
		return OPTIONAL_RESOURCE_NOT_FOUND.equals(value) ? null : value;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
