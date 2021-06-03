package net.lacnic.elections.adminweb.ui.admin.election;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;

@AuthorizeInstantiation("elections-only-one")
public class ViewElectionDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -3122456234867022677L;

	public ViewElectionDashboard(PageParameters params) {
		super(params);
		add(new ViewElectionPanel("detalleEleccion", UtilsParameters.getIdAsLong(params), UtilsParameters.isAll(params)));
	}
}
