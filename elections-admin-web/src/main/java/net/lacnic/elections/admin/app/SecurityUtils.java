package net.lacnic.elections.admin.app;

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.request.WebClientInfo;

public class SecurityUtils {

	private SecurityUtils() { }

	public static SisEleccionesManagerSession getEleccionesSesion() {
		return ((SisEleccionesManagerSession) Session.get());
	}

	public static String getIPClient() {
		WebClientInfo info = getEleccionesSesion().getClientInfo();
		return info.getProperties().getRemoteAddress();
	}

	public static String getAdminId() {
		if (getEleccionesSesion().isSignedIn())
			return getEleccionesSesion().getAdminId();
		else
			return "";
	}

	public static void signOut() {
		getEleccionesSesion().signOut();
	}

	public static Class<? extends Page> getHomePage() {
		return Application.get().getHomePage();
	}

	public static Locale getLocale() {
		return ((SisEleccionesManagerSession) Session.get()).getLocale();
	}

	public static void info(String string) {
		getEleccionesSesion().info(string);
	}

	public static void error(String string) {
		getEleccionesSesion().error(string);
	}

	public static Long getIdEleccionAutorizado() {
		return getEleccionesSesion().getIdEleccionAutorizado();
	}

	public static void logOut() {
		getEleccionesSesion().logOut();
	}

	public static void setLocale(String idioma) {
		getEleccionesSesion().setLocale(new Locale(idioma.toUpperCase(), idioma.toUpperCase()));
	}

	public static boolean isSignedIn() {
		return getEleccionesSesion().isSignedIn();
	}

}
