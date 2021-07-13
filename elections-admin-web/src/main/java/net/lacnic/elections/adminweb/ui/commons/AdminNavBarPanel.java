package net.lacnic.elections.adminweb.ui.commons;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebApplication;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.activity.ActivitiesDashboard;
import net.lacnic.elections.adminweb.ui.admin.commissioner.CommissionersDashboard;
import net.lacnic.elections.adminweb.ui.admin.customization.CustomizationDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.joint.JointElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.email.EmailsDashboard;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EmailTemplatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.ipaccess.IpAccessDashboard;
import net.lacnic.elections.adminweb.ui.admin.parameter.ParametersDashboard;
import net.lacnic.elections.adminweb.ui.admin.useradmin.EditUserAdminPasswordDashboard;
import net.lacnic.elections.adminweb.ui.admin.useradmin.UserAdminsDashboard;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Customization;


public class AdminNavBarPanel extends Panel {

	private static final long serialVersionUID = -4713777134889050424L;

	private boolean userIsAdmin = true;
	private Customization customization;


	public AdminNavBarPanel(String id) {
		super(id);

		Long authorizedElectionId = AppContext.getInstance().getManagerBeanRemote().getUserAuthorizedElectionId(SecurityUtils.getUserAdminId());
		if (authorizedElectionId != 0)
			setUserIsAdmin(false);

		String pictureExtension = "";
		byte[] pictureFile;
		String pictureFileName;
		customization = AppContext.getInstance().getManagerBeanRemote().getCustomization();
		pictureFileName = customization.getPicSymbol();
		pictureFile = customization.getContPicSymbol();
		if (pictureFile == null) {
			add(new ContextImage("symbolPicture","image/" + pictureFileName));
		} else {
			pictureExtension = FilenameUtils.getExtension(pictureFileName);
			add(new NonCachingImage("symbolPicture", new ImageResource(pictureFile, pictureExtension)));
		}

		add(new Label("username", SecurityUtils.getUserAdminId()));
		add(new BookmarkablePageLink<>("changePasswordLink", EditUserAdminPasswordDashboard.class, UtilsParameters.getAdminId(SecurityUtils.getUserAdminId())));
		add(new Link<Void>("logoutLink") {
			private static final long serialVersionUID = 2525351923512004923L;

			@Override
			public void onClick() {
				SecurityUtils.signOut();
				setResponsePage(WebApplication.get().getHomePage());
			}
		});

		WebMarkupContainer electionsContainer = new WebMarkupContainer("electionsContainer");
		electionsContainer.setVisible(isUserIsAdmin());
		add(electionsContainer);
		electionsContainer.add(new BookmarkablePageLink<>("allElectionsLink", ElectionsDashboard.class, UtilsParameters.getFilterAll()).setVisibilityAllowed(isUserIsAdmin()));
		electionsContainer.add(new BookmarkablePageLink<>("recentElectionsLink", ElectionsDashboard.class).setVisibilityAllowed(isUserIsAdmin()));

		add(new BookmarkablePageLink<>("newElectionLink", ElectionDetailDashboard.class).setVisibilityAllowed(isUserIsAdmin()));
		add(new BookmarkablePageLink<>("commissionersLink", CommissionersDashboard.class).setVisibilityAllowed(isUserIsAdmin()));
		add(new BookmarkablePageLink<>("userAdminsLink", UserAdminsDashboard.class).setVisibilityAllowed(isUserIsAdmin()));

		WebMarkupContainer advancedContainer = new WebMarkupContainer("advancedContainer");
		advancedContainer.setVisible(isUserIsAdmin());
		add(advancedContainer);
		advancedContainer.add(new BookmarkablePageLink<>("invalidIpsLink", IpAccessDashboard.class).setVisibilityAllowed(isUserIsAdmin()));
		advancedContainer.add(new BookmarkablePageLink<>("activitiesLink", ActivitiesDashboard.class).setVisibilityAllowed(isUserIsAdmin()));
		advancedContainer.add(new BookmarkablePageLink<>("parametersLink", ParametersDashboard.class).setVisibilityAllowed(isUserIsAdmin()));
		advancedContainer.add(new BookmarkablePageLink<>("pendingEmailsLink", EmailsDashboard.class).setVisibilityAllowed(isUserIsAdmin()));
		advancedContainer.add(new BookmarkablePageLink<>("allEmailsLink", EmailsDashboard.class, UtilsParameters.getFilterAll()).setVisibilityAllowed(isUserIsAdmin()));
		advancedContainer.add(new BookmarkablePageLink<>("emailTemplatesLink", EmailTemplatesDashboard.class, UtilsParameters.getId(0L)));
		advancedContainer.add(new Link<Void>("updateTemplatesLink") {
			private static final long serialVersionUID = -5210149681203217889L;

			@Override
			public void onClick() {
				Integer cuenta = AppContext.getInstance().getManagerBeanRemote().createMissingEmailTemplates();
				getSession().info("Se ha ejecutado el proceso de creación de templates para elecciones, creando " + cuenta + " templates");
				setResponsePage(ElectionsDashboard.class);
			}
		}.setVisibilityAllowed(isUserIsAdmin()));
		advancedContainer.add(new BookmarkablePageLink<>("jointElectionsLink", JointElectionsDashboard.class, UtilsParameters.getId(0L)));
		advancedContainer.add(new BookmarkablePageLink<>("customizationLink", CustomizationDashboard.class, UtilsParameters.getId(0L)));
	}


	public boolean isUserIsAdmin() {
		return userIsAdmin;
	}

	public void setUserIsAdmin(boolean userIsAdmin) {
		this.userIsAdmin = userIsAdmin;
	}

}
