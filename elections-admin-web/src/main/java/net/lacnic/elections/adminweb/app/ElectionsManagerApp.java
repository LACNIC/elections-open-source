package net.lacnic.elections.adminweb.app;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.core.request.handler.BookmarkableListenerRequestHandler;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.settings.ExceptionSettings;

import net.lacnic.elections.adminweb.ui.admin.activity.ActivitiesDashboard;
import net.lacnic.elections.adminweb.ui.admin.commissioner.CommissionersDashboard;
import net.lacnic.elections.adminweb.ui.admin.commissioner.EditCommissionerDashboard;
import net.lacnic.elections.adminweb.ui.admin.customization.CustomizationDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.EditAuditorDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.EditCandidateDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.EditUserVoterDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.configuration.ElectionConfigurationDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.email.SendEmailStep1Dashboard;
import net.lacnic.elections.adminweb.ui.admin.election.email.SendEmailStep2Dashboard;
import net.lacnic.elections.adminweb.ui.admin.election.email.SendEmailStep3Dashboard;
import net.lacnic.elections.adminweb.ui.admin.election.joint.JointElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.stats.StatsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.view.ViewElectionDashboard;
import net.lacnic.elections.adminweb.ui.admin.email.EmailsDashboard;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EditEmailTemplateDashboard;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EmailTemplatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.ipaccess.IpAccessDashboard;
import net.lacnic.elections.adminweb.ui.admin.parameter.EditParameterDashboard;
import net.lacnic.elections.adminweb.ui.admin.parameter.ParametersDashboard;
import net.lacnic.elections.adminweb.ui.admin.useradmin.EditUserAdminDashboard;
import net.lacnic.elections.adminweb.ui.admin.useradmin.EditUserAdminPasswordDashboard;
import net.lacnic.elections.adminweb.ui.admin.useradmin.UserAdminsDashboard;
import net.lacnic.elections.adminweb.ui.error.Error401;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.error.Error410;
import net.lacnic.elections.adminweb.ui.error.Error500;
import net.lacnic.elections.adminweb.ui.error.ErrorAuditNotPublic;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.ui.error.ErrorResultsNotPublic;
import net.lacnic.elections.adminweb.ui.error.ErrorVoteNotPublic;
import net.lacnic.elections.adminweb.ui.error.ErrorVoteNotStarted;
import net.lacnic.elections.adminweb.ui.home.PublicHomeDashboard;
import net.lacnic.elections.adminweb.ui.login.LoginDashboard;
import net.lacnic.elections.adminweb.ui.results.ResultsDashboard;
import net.lacnic.elections.adminweb.ui.results.audit.AuditDashboard;
import net.lacnic.elections.adminweb.ui.results.review.ReviewDashboard;
import net.lacnic.elections.adminweb.ui.vote.AlreadyVotedDashboard;
import net.lacnic.elections.adminweb.ui.vote.VoteDashboard;
import net.lacnic.elections.adminweb.ui.vote.VoteJointElectionDashboard;
import net.lacnic.elections.adminweb.ui.vote.VoteSimpleElectionDashboard;


public class ElectionsManagerApp extends AuthenticatedWebApplication {

	@Override
	protected void init() {
		super.init();

		getRequestCycleSettings().setResponseRequestEncoding("UTF-8");
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
		getDebugSettings().setAjaxDebugModeEnabled(false);
		// Remove Wicket markup when in Development mode (Deployment mode removes it by default)
		getMarkupSettings().setStripWicketTags(true);

		// General public pages
		mountAppPage("/home", PublicHomeDashboard.class);
		mountAppPage("/login", LoginDashboard.class);

		// Election management pages
		mountAppPage("/admin/election/list", ElectionsDashboard.class);
		mountAppPage("/admin/election/view", ViewElectionDashboard.class);
		mountAppPage("/admin/election/detail", ElectionDetailDashboard.class);
		mountAppPage("/admin/election/voters", ElectionCensusDashboard.class);
		mountAppPage("/admin/election/voters/edit", EditUserVoterDashboard.class);
		mountAppPage("/admin/election/candidates", ElectionCandidatesDashboard.class);
		mountAppPage("/admin/election/candidates/edit", EditCandidateDashboard.class);
		mountAppPage("/admin/election/auditors", ElectionAuditorsDashboard.class);
		mountAppPage("/admin/election/auditors/edit", EditAuditorDashboard.class);
		mountAppPage("/admin/election/config", ElectionConfigurationDashboard.class);
		mountAppPage("/admin/election/joint", JointElectionsDashboard.class);

		// Administration pages
		mountAppPage("/admin/commissioners", CommissionersDashboard.class);
		mountAppPage("/admin/commissioners/edit", EditCommissionerDashboard.class);
		mountAppPage("/admin/useradmin", UserAdminsDashboard.class);
		mountAppPage("/admin/useradmin/edit", EditUserAdminDashboard.class);
		mountAppPage("/admin/useradmin/edit/pass", EditUserAdminPasswordDashboard.class);
		mountAppPage("/admin/ipaccess", IpAccessDashboard.class);
		mountAppPage("/admin/activities", ActivitiesDashboard.class);
		mountAppPage("/admin/parameters", ParametersDashboard.class);
		mountAppPage("/admin/parameters/edit", EditParameterDashboard.class);
		mountAppPage("/admin/emails", EmailsDashboard.class);
		mountAppPage("/admin/templates", EmailTemplatesDashboard.class);
		mountAppPage("/admin/templates/edit", EditEmailTemplateDashboard.class);
		mountAppPage("/admin/customization", CustomizationDashboard.class);
		mountAppPage("/admin/sendmailstep1", SendEmailStep1Dashboard.class);
		mountAppPage("/admin/sendmailstep2", SendEmailStep2Dashboard.class);
		mountAppPage("/admin/sendmailstep3", SendEmailStep3Dashboard.class);

		// Error pages
		mountAppPage("/error401", Error401.class);
		mountAppPage("/error404", Error404.class);
		mountAppPage("/error410", Error410.class);
		mountAppPage("/error500", Error500.class);
		mountAppPage("/audit/notpublic", ErrorAuditNotPublic.class);
		mountAppPage("/results/notpublic", ErrorResultsNotPublic.class);
		mountAppPage("/vote/notpublic", ErrorVoteNotPublic.class);
		mountAppPage("/vote/notstarted", ErrorVoteNotStarted.class);
		mountAppPage("/election/closed", ErrorElectionClosed.class);
		getApplicationSettings().setAccessDeniedPage(Error401.class);
		getApplicationSettings().setPageExpiredErrorPage(Error410.class);
		getApplicationSettings().setInternalErrorPage(Error500.class);
		getExceptionSettings().setUnexpectedExceptionDisplay(ExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);

		// vote
		mountAppPage("/vote", VoteDashboard.class);
		mountAppPage("/vote/simple", VoteSimpleElectionDashboard.class);
		mountAppPage("/vote/joint", VoteJointElectionDashboard.class);
		mountAppPage("/vote/ready", AlreadyVotedDashboard.class);

		// results and audit
		mountAppPage("/result", ResultsDashboard.class);
		mountAppPage("/audit", AuditDashboard.class);
		mountAppPage("/review", ReviewDashboard.class);
		mountAppPage("/stats", StatsDashboard.class);

	}

	private void mountAppPage(String url, Class<? extends WebPage> pageClass) {
		mount(new MountedMapperWithoutPageComponentInfo(url, pageClass));
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return PublicHomeDashboard.class;
	}

	@Override
	protected Class<? extends WebPage> getSignInPageClass() {
		return LoginDashboard.class;
	}

	@Override
	protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
		return ElectionsWebAdminSession.class;
	}

	private class MountedMapperWithoutPageComponentInfo extends MountedMapper {

		public MountedMapperWithoutPageComponentInfo(String mountPath, Class<? extends IRequestablePage> pageClass) {
			super(mountPath, pageClass, new PageParametersEncoder());
		}

		@Override
		protected void encodePageComponentInfo(Url url, PageComponentInfo info) {
			// do nothing so that component info does not get rendered in url
		}

		@Override
		public Url mapHandler(IRequestHandler requestHandler) {
			if (requestHandler instanceof ListenerRequestHandler || requestHandler instanceof BookmarkableListenerRequestHandler) {
				return null;
			} else {
				return super.mapHandler(requestHandler);
			}
		}
	}

}
