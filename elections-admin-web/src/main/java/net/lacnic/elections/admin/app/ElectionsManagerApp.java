package net.lacnic.elections.admin.app;

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

import net.lacnic.elections.admin.dashboard.admin.DashboardActividades;
import net.lacnic.elections.admin.dashboard.admin.DashboardAdministradores;
import net.lacnic.elections.admin.dashboard.admin.DashboardComisionados;
import net.lacnic.elections.admin.dashboard.admin.DashboardConfiguracion;
import net.lacnic.elections.admin.dashboard.admin.DashboardDetalleEleccion;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarAdministrador;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarAuditor;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarCandidato;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarComisionado;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarParametrosPage;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarPasswordAdministrador;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarUsuarioPadron;
import net.lacnic.elections.admin.dashboard.admin.DashboardEleccionesJuntas;
import net.lacnic.elections.admin.dashboard.admin.DashboardEnviarEmailPaso1;
import net.lacnic.elections.admin.dashboard.admin.DashboardEstadisticas;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionAuditores;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionCandidatos;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionEleccion;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionPadron;
import net.lacnic.elections.admin.dashboard.admin.DashboardHomePage;
import net.lacnic.elections.admin.dashboard.admin.DashboardIp;
import net.lacnic.elections.admin.dashboard.admin.DashboardLoginPage;
import net.lacnic.elections.admin.dashboard.admin.DashboardMensajes;
import net.lacnic.elections.admin.dashboard.admin.DashboardParametros;
import net.lacnic.elections.admin.dashboard.admin.DashboardPlantillasVer;
import net.lacnic.elections.admin.dashboard.admin.DashboardReview;
import net.lacnic.elections.admin.dashboard.error.Error401;
import net.lacnic.elections.admin.dashboard.error.Error404;
import net.lacnic.elections.admin.dashboard.error.Error410;
import net.lacnic.elections.admin.dashboard.error.Error500;
import net.lacnic.elections.admin.dashboard.error.ErrorAuditoriaNoPublica;
import net.lacnic.elections.admin.dashboard.error.ErrorResultadosNoPublicos;
import net.lacnic.elections.admin.dashboard.error.ErrorVotacionNoComenzada;
import net.lacnic.elections.admin.dashboard.error.ErrorVotacionNoPublica;
import net.lacnic.elections.admin.dashboard.user.DashboardAuditores;
import net.lacnic.elections.admin.dashboard.user.DashboardHomePublico;
import net.lacnic.elections.admin.dashboard.user.DashboardResultado;
import net.lacnic.elections.admin.dashboard.user.DashboardVotar;
import net.lacnic.elections.admin.dashboard.user.DashboardVotarEleccionSimple;
import net.lacnic.elections.admin.dashboard.user.DashboardVotarEleccionesJuntas;
import net.lacnic.elections.admin.dashboard.user.DashboardYaVoto;


public class ElectionsManagerApp extends AuthenticatedWebApplication {

	@Override
	protected void init() {
		super.init();

		getRequestCycleSettings().setResponseRequestEncoding("UTF-8");
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
		getDebugSettings().setAjaxDebugModeEnabled(false);
		// Remove Wicket markup when in Development mode (Deployment mode removes it by default)
		getMarkupSettings().setStripWicketTags(true);

		// Paginas generales
		montarPagina("/login", DashboardLoginPage.class);
		montarPagina("/home", DashboardHomePublico.class);

		montarPagina("/list", DashboardHomePage.class);
		montarPagina("/vote", DashboardVotar.class);
		montarPagina("/s/vote", DashboardVotarEleccionSimple.class);
		montarPagina("/j/vote", DashboardVotarEleccionesJuntas.class);
		montarPagina("/ready", DashboardYaVoto.class);
		montarPagina("/result", DashboardResultado.class);
		montarPagina("/audit", DashboardAuditores.class);
		montarPagina("/view", DashboardDetalleEleccion.class);
		montarPagina("/ip", DashboardIp.class);
		montarPagina("/activities", DashboardActividades.class);
		montarPagina("/messages", DashboardMensajes.class);
		montarPagina("/review", DashboardReview.class);

		// paginas de una elección
		montarPagina("/g/detail", DashboardGestionEleccion.class);
		montarPagina("/g/voters", DashboardGestionPadron.class);
		montarPagina("/g/voters/edit", DashboardEditarUsuarioPadron.class);
		montarPagina("/g/candidates", DashboardGestionCandidatos.class);
		montarPagina("/g/candidates/edit", DashboardEditarCandidato.class);
		montarPagina("/g/audits", DashboardGestionAuditores.class);
		montarPagina("/g/audits/edit", DashboardEditarAuditor.class);
		montarPagina("/g/stats", DashboardEstadisticas.class);
		montarPagina("/g/config", DashboardConfiguracion.class);
		montarPagina("/g/union", DashboardEleccionesJuntas.class);

		// Paginas generales con edición
		montarPagina("/commissioners", DashboardComisionados.class);
		montarPagina("/commissioners/edit", DashboardEditarComisionado.class);

		montarPagina("/templates", DashboardPlantillasVer.class);
		montarPagina("/templates/edit", DashboardEnviarEmailPaso1.class);

		montarPagina("/parameters", DashboardParametros.class);
		montarPagina("/parameters/edit", DashboardEditarParametrosPage.class);

		montarPagina("/admin", DashboardAdministradores.class);
		montarPagina("/admin/edit", DashboardEditarAdministrador.class);
		montarPagina("/admin/edit/pass", DashboardEditarPasswordAdministrador.class);

		// Paginas de errores
		montarPagina("/error401", Error401.class);
		montarPagina("/error404", Error404.class);
		montarPagina("/error410", Error410.class);
		montarPagina("/error500", Error500.class);
		montarPagina("/a/nopublic", ErrorAuditoriaNoPublica.class);
		montarPagina("/r/nopublic", ErrorResultadosNoPublicos.class);
		montarPagina("/v/nopublic", ErrorVotacionNoPublica.class);
		montarPagina("/v/nostart", ErrorVotacionNoComenzada.class);

		getApplicationSettings().setAccessDeniedPage(Error401.class);
		getApplicationSettings().setPageExpiredErrorPage(Error410.class);
		getApplicationSettings().setInternalErrorPage(Error500.class);

		getExceptionSettings().setUnexpectedExceptionDisplay(ExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return DashboardHomePublico.class;
	}

	@Override
	protected Class<? extends WebPage> getSignInPageClass() {
		return DashboardLoginPage.class;
	}

	private void montarPagina(String url, Class<? extends WebPage> pageClass) {
		mount(new MountedMapperWithoutPageComponentInfo(url, pageClass));
	}

	@Override
	protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
		return SisEleccionesManagerSession.class;
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
