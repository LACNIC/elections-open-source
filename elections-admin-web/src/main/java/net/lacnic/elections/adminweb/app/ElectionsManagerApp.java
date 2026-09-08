package net.lacnic.elections.adminweb.app;

import java.time.Duration;

import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.core.request.handler.BookmarkableListenerRequestHandler;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.settings.ExceptionSettings;
import org.apache.wicket.settings.RequestCycleSettings;

import net.lacnic.elections.adminweb.ui.admin.activity.ActivitiesDashboard;
import net.lacnic.elections.adminweb.ui.admin.commissioner.CommissionersDashboard;
import net.lacnic.elections.adminweb.ui.admin.commissioner.EditCommissionerDashboard;
import net.lacnic.elections.adminweb.ui.admin.customization.CustomizationDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.EditAuditorDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.calendarmodule.ElectionCalendarModuleDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.call.ElectionCallDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ManageCandidateBiographyDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ManageCandidatePhotoDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ManageCandidateStatusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ManageCandidateSupportsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ManageCandidateTrainingDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ManageCandidateTranslationsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ViewCandidateAnswersDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.EditUserVoterDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.configuration.ElectionConfigurationDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.create.ElectionCreateDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.email.SendEmailStep1Dashboard;
import net.lacnic.elections.adminweb.ui.admin.election.email.SendEmailStep2Dashboard;
import net.lacnic.elections.adminweb.ui.admin.election.email.SendEmailStep3Dashboard;
import net.lacnic.elections.adminweb.ui.admin.election.joint.JointElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.organizations.ElectionOrganizationsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.prereports.PreElectionReportsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.results.ElectionResultsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.questions.ElectionQuestionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.charts.StatsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.tasks.ElectionTasksDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.view.ViewElectionDashboard;
import net.lacnic.elections.adminweb.ui.admin.email.EmailsDashboard;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EditEmailTemplateDashboard;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EmailTemplatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.ipaccess.IpAccessDashboard;
import net.lacnic.elections.adminweb.ui.admin.organizationsync.OrganizationsSyncRunsDashboard;
import net.lacnic.elections.adminweb.ui.admin.parameter.EditParameterDashboard;
import net.lacnic.elections.adminweb.ui.admin.parameter.ParametersDashboard;
import net.lacnic.elections.adminweb.ui.admin.useradmin.EditUserAdminDashboard;
import net.lacnic.elections.adminweb.ui.admin.useradmin.EditUserAdminPasswordDashboard;
import net.lacnic.elections.adminweb.ui.admin.useradmin.UserAdminsDashboard;
import net.lacnic.elections.adminweb.ui.error.Error401;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.error.Error410;
import net.lacnic.elections.adminweb.ui.error.Error500;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.ui.home.PublicElectionsDashboard;
import net.lacnic.elections.adminweb.ui.home.PublicHomeDashboard;
import net.lacnic.elections.adminweb.ui.home.PublicPhotoResizeDashboard;
import net.lacnic.elections.adminweb.ui.login.LoginDashboard;
import net.lacnic.elections.adminweb.ui.publicresource.CandidatePublicPhotoResourceReference;
import net.lacnic.elections.adminweb.ui.publicresource.ElectionOfficialResultLetterResourceReference;
import net.lacnic.elections.adminweb.ui.results.review.ReviewDashboard;
import net.lacnic.elections.adminweb.ui.token.page.AuditPublicCandidateDetailPage;
import net.lacnic.elections.adminweb.ui.token.page.AuditPublicDashboardPage;
import net.lacnic.elections.adminweb.ui.token.page.AuditPublicResultsPage;
import net.lacnic.elections.adminweb.ui.token.page.DoNominationPage;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.ui.token.page.PublicAccessDeniedPage;
import net.lacnic.elections.adminweb.ui.token.page.PublicElectionPage;
import net.lacnic.elections.adminweb.ui.token.page.PublicElectionPageV2;
import net.lacnic.elections.adminweb.ui.token.page.ResultPublicPage;
import net.lacnic.elections.adminweb.ui.token.page.SupportNominationPage;
import net.lacnic.elections.adminweb.ui.token.page.VotePublicPage;

public class ElectionsManagerApp extends AuthenticatedWebApplication {

	private static final String GOOGLE_URL = "https://www.google.com";
	private static final String GOOGLE_GSTATIC_URL = "https://www.gstatic.com";
	private static final String GOOGLE_RECAPTCHA_URL = "https://www.google.com/recaptcha/";
	private static final String GOOGLE_RECAPTCHA_GSTATIC_URL = "https://www.gstatic.com/recaptcha/";

	@Override
	protected void init() {
		super.init();

		// Remove Wicket markup when in Development mode (Deployment mode removes it by
		// default)
		getMarkupSettings().setStripWicketTags(true);

		getCspSettings().blocking().clear()
				.add(CSPDirective.SCRIPT_SRC, "'self'", "'unsafe-eval'", "'unsafe-inline'", GOOGLE_URL, GOOGLE_GSTATIC_URL, GOOGLE_RECAPTCHA_URL, GOOGLE_RECAPTCHA_GSTATIC_URL)
				.add(CSPDirective.FRAME_SRC, GOOGLE_RECAPTCHA_URL, GOOGLE_URL)
				.add(CSPDirective.CHILD_SRC, "'self'", GOOGLE_RECAPTCHA_URL, GOOGLE_URL);

//		.add(CSPDirective.FRAME_ANCESTORS, CSPDirectiveSrcValue.NONE).add(CSPDirective.FRAME_SRC, "https://www.google.com").add(CSPDirective.FRAME_SRC, "https://www.googletagmanager.com").add(CSPDirective.CHILD_SRC, CSPDirectiveSrcValue.SELF);
		getRequestCycleSettings().setRenderStrategy(RequestCycleSettings.RenderStrategy.REDIRECT_TO_BUFFER); // para util cuando se usa AJAX en pestañas paralelas.
		getRequestCycleSettings().setTimeout(Duration.ofMinutes(10)); // evita timeout de lock en cargas pesadas (p. ej. padrón grande)

		getRequestCycleSettings().setResponseRequestEncoding("UTF-8");
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
		getDebugSettings().setAjaxDebugModeEnabled(false);

		getApplicationSettings().setPageExpiredErrorPage(Error404.class);
		getApplicationSettings().setInternalErrorPage(Error500.class);
		getApplicationSettings().setAccessDeniedPage(Error401.class);
		getExceptionSettings().setUnexpectedExceptionDisplay(ExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);

		// General public pages
		mountAppPage("/home", PublicHomeDashboard.class);
		mountAppPage("/public-elections", PublicElectionsDashboard.class);
		mountAppPage("/login", LoginDashboard.class);
		mountAppPage("/public/photo/resize", PublicPhotoResizeDashboard.class);
		mountResource("/public/candidate/photo", new CandidatePublicPhotoResourceReference());
		mountResource("/public/election/result-letter", new ElectionOfficialResultLetterResourceReference());

		// Election management pages
		mountAppPage("/admin/election/list", ElectionsDashboard.class);
		mountAppPage("/admin/election/view", ViewElectionDashboard.class);
		mountAppPage("/admin/election/create", ElectionCreateDashboard.class);
		mountAppPage("/admin/election/detail", ElectionDetailDashboard.class);
		mountAppPage("/admin/election/call", ElectionCallDashboard.class);
		mountAppPage("/admin/election/organizations", ElectionOrganizationsDashboard.class);
		mountAppPage("/admin/election/voters", ElectionCensusDashboard.class);
		mountAppPage("/admin/election/voters/edit", EditUserVoterDashboard.class);
		mountAppPage("/admin/election/calendar", ElectionCalendarModuleDashboard.class);
		mountAppPage("/admin/election/tasks", ElectionTasksDashboard.class);
		mountAppPage("/admin/election/candidates", ElectionCandidatesDashboard.class);
		mountAppPage("/admin/election/candidates/biography", ManageCandidateBiographyDashboard.class);
		mountAppPage("/admin/election/candidates/photo", ManageCandidatePhotoDashboard.class);
		mountAppPage("/admin/election/candidates/status", ManageCandidateStatusDashboard.class);
		mountAppPage("/admin/election/candidates/supports", ManageCandidateSupportsDashboard.class);
		mountAppPage("/admin/election/candidates/training", ManageCandidateTrainingDashboard.class);
		mountAppPage("/admin/election/candidates/translations", ManageCandidateTranslationsDashboard.class);
		mountAppPage("/admin/election/candidates/answers", ViewCandidateAnswersDashboard.class);
		mountAppPage("/admin/election/auditors", ElectionAuditorsDashboard.class);
		mountAppPage("/admin/election/auditors/edit", EditAuditorDashboard.class);
		mountAppPage("/admin/election/questions", ElectionQuestionsDashboard.class);
		mountAppPage("/admin/election/pre-reports", PreElectionReportsDashboard.class);
		mountAppPage("/admin/election/results", ElectionResultsDashboard.class);
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
		mountAppPage("/admin/organizations-sync-runs", OrganizationsSyncRunsDashboard.class);
		mountAppPage("/admin/sendmailstep1", SendEmailStep1Dashboard.class);
		mountAppPage("/admin/sendmailstep2", SendEmailStep2Dashboard.class);
		mountAppPage("/admin/sendmailstep3", SendEmailStep3Dashboard.class);
		// Error pages
		mountAppPage("/error401", Error401.class);
		mountAppPage("/error404", Error404.class);
		mountAppPage("/error410", Error410.class);
		mountAppPage("/error500", Error500.class);
		mountAppPage("/election/closed", ErrorElectionClosed.class);

		// vote
		// legacy vote/result/audit removed; users must hit /token/*
		mountAppPage("/token/vote", VotePublicPage.class);
		mountAppPage("/token/result", ResultPublicPage.class);
		mountAppPage("/token/audit", AuditPublicDashboardPage.class);
		mountAppPage("/token/audit/result", AuditPublicResultsPage.class);
		mountAppPage("/token/audit/candidate", AuditPublicCandidateDetailPage.class);
		mountAppPage("/token/nomination/tasks", GenericAcceptNominationTasksPage.class);
		mountAppPage("/token/organization/do-nomination", DoNominationPage.class);
		mountAppPage("/token/nomination/support", SupportNominationPage.class);
		mountAppPage("/token/public-election", PublicElectionPage.class);
		mountAppPage("/token/public-election-v2", PublicElectionPageV2.class);
		mountAppPage("/token/access-denied", PublicAccessDeniedPage.class);

		// results and audit
		mountAppPage("/review", ReviewDashboard.class);
		mountAppPage("/stats", StatsDashboard.class);

	}

	@Override
	public RuntimeConfigurationType getConfigurationType() {
		return RuntimeConfigurationType.DEPLOYMENT;
	}

	@Override
	protected Class<? extends WebPage> getSignInPageClass() {
		return LoginDashboard.class;
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return PublicHomeDashboard.class;
	}

	private void mountAppPage(String url, Class<? extends Page> pageClass) {
		mount(new MountedMapperWithoutPageComponentInfo(url, pageClass));
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
