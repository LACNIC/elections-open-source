package net.lacnic.elections.adminweb.ui.bases;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.commons.AdminNavBarPanel;
import net.lacnic.elections.adminweb.ui.commons.AdminTopBarPanel;
import net.lacnic.elections.adminweb.ui.error.Error401;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;


@AuthorizeInstantiation("elections-manager")
public class DashboardAdminBasePage extends WebPage {

	private static final long serialVersionUID = 6861984885215804314L;


	public DashboardAdminBasePage(PageParameters params) {
		if (SecurityUtils.getAuthorizedElectionId() != 0 && UtilsParameters.isId(params) && (UtilsParameters.getIdAsLong(params) != SecurityUtils.getAuthorizedElectionId()))
			setResponsePage(Error401.class);
		add(new AdminNavBarPanel("navBar"));
		add(new AdminTopBarPanel("topBar"));
	}

	public String getLanguage() {
		return SecurityUtils.getLocale().getLanguage();
	}

}
