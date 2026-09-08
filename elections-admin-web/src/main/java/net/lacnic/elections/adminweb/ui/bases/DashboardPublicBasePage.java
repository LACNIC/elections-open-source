package net.lacnic.elections.adminweb.ui.bases;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.ElectionsWebAdminSession;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.error.Error429;
import net.lacnic.elections.adminweb.ui.commons.AppTopHeaderPanel;
import net.lacnic.elections.adminweb.ui.commons.AppTopNavPublicPanel;
import net.lacnic.elections.adminweb.ui.commons.AppTopBreadcrumbPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

public abstract class DashboardPublicBasePage extends WebPage {

	private static final long serialVersionUID = 6861984885215804314L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private Election election;
	private Election[] elections;
	private String token;

	public DashboardPublicBasePage(PageParameters params) {
		setToken(UtilsParameters.getToken(params));
		Class classError;
		if (isPublicFailedAccessRateLimitEnabled() && isPublicFailedAccessRateLimited()) {
			classError = Error429.class;
		} else {
			classError = validateToken(params);
			if (isPublicFailedAccessRateLimitEnabled() && Error404.class.equals(classError)) {
				classError = registerPublicFailedAccessAttemptAndResolveError();
			}
		}
		if (classError != null) {
			setResponsePage(classError);
		}
//		add(new PublicTopBarPanel("topBarPublic"));
		add(new AppTopHeaderPanel("appTopHeader", getString("appTopHeaderSectionPublic")));
		add(new AppTopNavPublicPanel("appTopNavPublic"));
		add(new AppTopBreadcrumbPanel("appTopPrivateBreadcrumb"));

	}

	public String getIP() {
		return ElectionsWebAdminSession.getIPClient();
	}

	protected abstract Class validateToken(PageParameters params);

	protected boolean isPublicFailedAccessRateLimitEnabled() {
		return true;
	}

	protected boolean isPublicFailedAccessRateLimited() {
		try {
			return AppContext.getInstance().getVoterBeanRemote().isPublicFailedAccessRateLimited(getIP());
		} catch (Exception e) {
			appLogger.error("Error checking public failed access rate limit", e);
			return false;
		}
	}

	protected Class registerPublicFailedAccessAttemptAndResolveError() {
		try {
			boolean blocked = AppContext.getInstance().getVoterBeanRemote().registerPublicFailedAccessAttempt(getIP());
			return blocked ? Error429.class : Error404.class;
		} catch (Exception e) {
			appLogger.error("Error registering public failed access attempt", e);
			return Error404.class;
		}
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Election[] getElections() {
		return elections;
	}

	public void setElections(Election[] elections) {
		this.elections = elections;
	}

	public String getLanguage() {
		return SecurityUtils.getLocale().getLanguage();
	}

}
