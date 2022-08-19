package net.lacnic.elections.adminweb.ui.home;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;
import net.lacnic.elections.domain.Customization;

public class PublicHomeDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1392182581021963077L;

	private Customization customization;

	public PublicHomeDashboard(PageParameters params) {
		super(params);

		customization = AppContext.getInstance().getManagerBeanRemote().getCustomization();

		Label homeHtmlLabel = new Label("customHomeHtml", new PropertyModel<>(customization, "homeHtml"));
		homeHtmlLabel.setEscapeModelStrings(false);

		WebMarkupContainer homeContainer = new WebMarkupContainer("homeContainer");
		add(homeContainer);

		WebMarkupContainer homeCustom = new WebMarkupContainer("homeCustom");
		homeCustom.add(homeHtmlLabel).setEscapeModelStrings(false);
		add(homeCustom);

		ExternalLink downloadLink = new ExternalLink("exportAuditLink", AppContext.getInstance().getManagerBeanRemote().getAuditReportURL());
		homeContainer.add(downloadLink);

		if (customization.isShowHome()) {
			homeContainer.setVisible(false);
		} else {
			homeCustom.setVisible(false);
		}

		if (SecurityUtils.isSignedIn())
			setResponsePage(ElectionsDashboard.class);
	}

	@Override
	protected Class validateToken(PageParameters params) {
		return null;
	}

}
