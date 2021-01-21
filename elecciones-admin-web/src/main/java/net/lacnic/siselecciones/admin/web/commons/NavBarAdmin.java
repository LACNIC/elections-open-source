package net.lacnic.siselecciones.admin.web.commons;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebApplication;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardActividades;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardAdministradores;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardComisionados;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardEditarPasswordAdministrador;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardEleccionesJuntas;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionEleccion;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardHomePage;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardIp;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardMensajes;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardParametros;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardPlantillasVer;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;

import net.lacnic.siselecciones.admin.wicket.util.ImageResource;
import net.lacnic.siselecciones.dominio.Personalizacion;

public class NavBarAdmin extends Panel {

	private static final long serialVersionUID = -4713777134889050424L;

	private boolean ver = true;

	private Personalizacion personalizacion;

	public NavBarAdmin(String id) {
		super(id);
		Long userIdEleccion = Contexto.getInstance().getManagerBeanRemote().obtenerIdEleccionUsuAdmin(SecurityUtils.getAdminId());
		if (userIdEleccion != 0)
			setVer(false);
		add(new Label("username", SecurityUtils.getAdminId()));
		add(new BookmarkablePageLink<>("nav.pass", DashboardEditarPasswordAdministrador.class, UtilsParameters.getAdminId(SecurityUtils.getAdminId())));

		/* Cargo el simbolo */
		String ext = "";
		byte[] archivoPicSymbol;
		String nombreArchivoSymbol;

		personalizacion = Contexto.getInstance().getManagerBeanRemote().getPersonalizacion();
		nombreArchivoSymbol = personalizacion.getPicSimbolo();
		archivoPicSymbol = personalizacion.getContPicSimbolo();
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

		seleccionarEleccion.add(new BookmarkablePageLink<>("listadoElecciones", DashboardHomePage.class).setVisibilityAllowed(isVer()));
		add(new BookmarkablePageLink<>("nueva", DashboardGestionEleccion.class).setVisibilityAllowed(isVer()));

		seleccionarEleccion.add(new BookmarkablePageLink<>("todas", DashboardHomePage.class, UtilsParameters.getFilterAll()).setVisibilityAllowed(isVer()));
		add(new BookmarkablePageLink<>("admins", DashboardAdministradores.class).setVisibilityAllowed(isVer()));
		avanzadas.add(new BookmarkablePageLink<>("actividades", DashboardActividades.class).setVisibilityAllowed(isVer()));
		avanzadas.add(new Link<Void>("actualizarPlantillas") {

			private static final long serialVersionUID = -5210149681203217889L;

			@Override
			public void onClick() {
				Integer cuenta = Contexto.getInstance().getManagerBeanRemote().crearPlantillasEleccionesQueLeFalten();
				getSession().info("Se ha ejecutado el proceso de creación de templates para elecciones, creando " + cuenta + " templates");
				setResponsePage(DashboardHomePage.class);
			}
		}.setVisibilityAllowed(isVer()));
		avanzadas.add(new BookmarkablePageLink<>("parametros", DashboardParametros.class).setVisibilityAllowed(isVer()));
		avanzadas.add(new BookmarkablePageLink<>("listadoIpInhabilitadas", DashboardIp.class).setVisibilityAllowed(isVer()));
		add(new BookmarkablePageLink<>("comisionados", DashboardComisionados.class).setVisibilityAllowed(isVer()));

		avanzadas.add(new BookmarkablePageLink<>("emailsPendientes", DashboardMensajes.class).setVisibilityAllowed(isVer()));
		avanzadas.add(new BookmarkablePageLink<>("emailsTodos", DashboardMensajes.class, UtilsParameters.getFilterAll()).setVisibilityAllowed(isVer()));
		avanzadas.add(new BookmarkablePageLink<>("gestionEmails", DashboardPlantillasVer.class, UtilsParameters.getId(0L)));
		avanzadas.add(new BookmarkablePageLink<>("eleccionesJuntas", DashboardEleccionesJuntas.class, UtilsParameters.getId(0L)));
	}

	public boolean isVer() {
		return ver;
	}

	public void setVer(boolean ver) {
		this.ver = ver;
	}

}
