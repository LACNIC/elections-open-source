package net.lacnic.elections.adminweb.dashboard.user;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.web.bases.DashboardPublicBasePage;
import net.lacnic.elections.domain.Customization;


public class PublicHomeDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1392182581021963077L;

	private Customization customization;


	public PublicHomeDashboard(PageParameters params) {
		super(params);

		Boolean showHome = true;
		//		String homeHtml;

		customization = AppContext.getInstance().getManagerBeanRemote().getCustomization();
		showHome = customization.isShowHome();
		//		homeHtml = customization.getHomeHtml();

		WebMarkupContainer homeContainer = new WebMarkupContainer("homeContainer");
		add(homeContainer);

		Label homeHtmlLabel = new Label("homeHtml", new PropertyModel<>(PublicHomeDashboard.this, "homeHtml"));
		homeHtmlLabel.setEscapeModelStrings(false);

		WebMarkupContainer homeCustom = new WebMarkupContainer("homeCustom");
		homeCustom.add(homeHtmlLabel).setEscapeModelStrings(false);
		add(homeCustom);

		if (showHome) {
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
