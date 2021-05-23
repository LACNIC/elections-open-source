package net.lacnic.elections.admin.web.bases;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.error.Error401;
import net.lacnic.elections.admin.web.commons.NavBarAdmin;
import net.lacnic.elections.admin.web.commons.TopBar;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;

@AuthorizeInstantiation("siselecciones-manager")
public class DashboardAdminBasePage extends WebPage {

	private static final long serialVersionUID = 6861984885215804314L;

	public DashboardAdminBasePage(PageParameters params) {
		if (SecurityUtils.getIdEleccionAutorizado() != 0 && UtilsParameters.isId(params) && (UtilsParameters.getIdAsLong(params) != SecurityUtils.getIdEleccionAutorizado()))
			setResponsePage(Error401.class);
		add(new NavBarAdmin("navBar"));
		add(new TopBar("topBar"));
	}

}
