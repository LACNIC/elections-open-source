package net.lacnic.elections.adminweb.web.bases;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.error.Error401;
import net.lacnic.elections.adminweb.web.commons.NavBarAdmin;
import net.lacnic.elections.adminweb.web.commons.TopBar;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;


@AuthorizeInstantiation("elections-manager")
public class DashboardAdminBasePage extends WebPage {

	private static final long serialVersionUID = 6861984885215804314L;

	public DashboardAdminBasePage(PageParameters params) {
		if (SecurityUtils.getAuthorizedElectionId() != 0 && UtilsParameters.isId(params) && (UtilsParameters.getIdAsLong(params) != SecurityUtils.getAuthorizedElectionId()))
			setResponsePage(Error401.class);
		add(new NavBarAdmin("navBar"));
		add(new TopBar("topBar"));
	}

}
