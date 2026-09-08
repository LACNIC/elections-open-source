package net.lacnic.elections.adminweb.ui.bases;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.utils.ElectionsRoles;

@AuthorizeInstantiation(ElectionsRoles.ELECTIONS_MANAGER)
public abstract class DashboardManagerBasePage extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;

	public DashboardManagerBasePage(PageParameters params) {
		super(params);
	}
}
