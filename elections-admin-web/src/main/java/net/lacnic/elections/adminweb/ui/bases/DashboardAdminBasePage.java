package net.lacnic.elections.adminweb.ui.bases;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.commons.AppTopHeaderPanel;
import net.lacnic.elections.adminweb.ui.commons.AppTopBreadcrumbPanel;
import net.lacnic.elections.adminweb.ui.commons.AppTopNavPrivatePanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;

public abstract class DashboardAdminBasePage extends WebPage {

	private static final long serialVersionUID = 6861984885215804312L;

	public DashboardAdminBasePage(PageParameters params) {
		this(resolveElectionId(params), 0L);
	}

	protected DashboardAdminBasePage(PageParameters params, long electionId) {
		this(electionId, electionId);
	}

	private DashboardAdminBasePage(long navElectionId, long breadcrumbElectionId) {
		add(new AppTopHeaderPanel("appTopHeader", getString("appTopHeaderSectionAdmin")));
		add(new AppTopNavPrivatePanel("appTopNavPrivate", navElectionId));
		add(new AppTopBreadcrumbPanel("appTopPrivateBreadcrumb", breadcrumbElectionId));
	}

	public String getLanguage() {
		return SecurityUtils.getLocale().getLanguage();
	}

	private static long resolveElectionId(PageParameters params) {
		return params != null ? UtilsParameters.getIdAsLong(params) : 0L;
	}

}
