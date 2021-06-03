package net.lacnic.elections.adminweb.web.commons;

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
import net.lacnic.elections.adminweb.dashboard.admin.DashboardActividades;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardEditarPasswordAdministrador;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardEleccionesJuntas;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardIp;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardMensajes;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardParametros;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardPersonalizacion;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardPlantillasVer;
import net.lacnic.elections.adminweb.ui.admin.commissioner.CommissionersDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.admin.useradmin.UserAdminsDashboard;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Customization;

public class NavBarAdmin extends Panel {

	private static final long serialVersionUID = -4713777134889050424L;

	private boolean ver = true;

	private Customization personalizacion;

	public NavBarAdmin(String id) {
		super(id);
		Long userIdEleccion = AppContext.getInstance().getManagerBeanRemote().getUserAuthorizedElectionId(SecurityUtils.getUserAdminId());
		if (userIdEleccion != 0)
			setVer(false);
		add(new Label("username", SecurityUtils.getUserAdminId()));
		add(new BookmarkablePageLink<>("nav.pass", DashboardEditarPasswordAdministrador.class, UtilsParameters.getAdminId(SecurityUtils.getUserAdminId())));

		/* Cargo el simbolo */
		String ext = "";
		byte[] archivoPicSymbol;
		String nombreArchivoSymbol;

		personalizacion = AppContext.getInstance().getManagerBeanRemote().getCustomization();
		nombreArchivoSymbol = personalizacion.getPicSymbol();
		archivoPicSymbol = personalizacion.getContPicSymbol();
		if (archivoPicSymbol == null) {
			add(new ContextImage("fotoSymbol","image/" + nombreArchivoSymbol));
		} else {
			ext = FilenameUtils.getExtension(nombreArchivoSymbol);
			add(new NonCachingImage("fotoSymbol", new ImageResource(archivoPicSymbol, ext)));
		}

		add(new Link<Void>("nav.logout") {

			private static final long serialVersionUID = 2525351923512004923L;

			@Override
			public void onClick() {
				SecurityUtils.signOut();
				setResponsePage(WebApplication.get().getHomePage());
			}

		});

		WebMarkupContainer seleccionarEleccion = new WebMarkupContainer("seleccionarEleccionContainer");
		seleccionarEleccion.setVisible(isVer());
		add(seleccionarEleccion);

		WebMarkupContainer avanzadas = new WebMarkupContainer("avanzadasContainer");
		avanzadas.setVisible(isVer());
		add(avanzadas);

		seleccionarEleccion.add(new BookmarkablePageLink<>("listadoElecciones", ElectionsDashboard.class).setVisibilityAllowed(isVer()));
		add(new BookmarkablePageLink<>("nueva", ElectionDetailDashboard.class).setVisibilityAllowed(isVer()));

		seleccionarEleccion.add(new BookmarkablePageLink<>("todas", ElectionsDashboard.class, UtilsParameters.getFilterAll()).setVisibilityAllowed(isVer()));
		add(new BookmarkablePageLink<>("admins", UserAdminsDashboard.class).setVisibilityAllowed(isVer()));
		avanzadas.add(new BookmarkablePageLink<>("actividades", DashboardActividades.class).setVisibilityAllowed(isVer()));
		avanzadas.add(new Link<Void>("actualizarPlantillas") {

			private static final long serialVersionUID = -5210149681203217889L;

			@Override
			public void onClick() {
				Integer cuenta = AppContext.getInstance().getManagerBeanRemote().createMissingEmailTemplates();
				getSession().info("Se ha ejecutado el proceso de creación de templates para elecciones, creando " + cuenta + " templates");
				setResponsePage(ElectionsDashboard.class);
			}
		}.setVisibilityAllowed(isVer()));
		avanzadas.add(new BookmarkablePageLink<>("parametros", DashboardParametros.class).setVisibilityAllowed(isVer()));
		avanzadas.add(new BookmarkablePageLink<>("listadoIpInhabilitadas", DashboardIp.class).setVisibilityAllowed(isVer()));
		add(new BookmarkablePageLink<>("comisionados", CommissionersDashboard.class).setVisibilityAllowed(isVer()));

		avanzadas.add(new BookmarkablePageLink<>("emailsPendientes", DashboardMensajes.class).setVisibilityAllowed(isVer()));
		avanzadas.add(new BookmarkablePageLink<>("emailsTodos", DashboardMensajes.class, UtilsParameters.getFilterAll()).setVisibilityAllowed(isVer()));
		avanzadas.add(new BookmarkablePageLink<>("gestionEmails", DashboardPlantillasVer.class, UtilsParameters.getId(0L)));
		avanzadas.add(new BookmarkablePageLink<>("eleccionesJuntas", DashboardEleccionesJuntas.class, UtilsParameters.getId(0L)));
		avanzadas.add(new BookmarkablePageLink<>("personalizacion", DashboardPersonalizacion.class, UtilsParameters.getId(0L)));
	}

	public boolean isVer() {
		return ver;
	}

	public void setVer(boolean ver) {
		this.ver = ver;
	}

}
