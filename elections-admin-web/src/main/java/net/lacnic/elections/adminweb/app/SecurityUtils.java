package net.lacnic.elections.adminweb.app;

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.request.WebClientInfo;


public class SecurityUtils {

	private SecurityUtils() { }


	public static ElectionsWebAdminSession getElectionsSession() {
		return ((ElectionsWebAdminSession) Session.get());
	}

	public static String getClientIp() {
		WebClientInfo info = getElectionsSession().getClientInfo();
		return info.getProperties().getRemoteAddress();
	}

	public static String getUserAdminId() {
		if (getElectionsSession().isSignedIn())
			return getElectionsSession().getUserAdminId();
		else
			return "";
	}

	public static void signOut() {
		getElectionsSession().signOut();
	}

	public static Class<? extends Page> getHomePage() {
		return Application.get().getHomePage();
	}

	public static Locale getLocale() {
		return ((ElectionsWebAdminSession) Session.get()).getLocale();
	}

	public static void info(String string) {
		getElectionsSession().info(string);
	}

	public static void error(String string) {
		getElectionsSession().error(string);
	}

	public static Long getAuthorizedElectionId() {
		return getElectionsSession().getAuthorizedElectionId();
	}

	public static void logOut() {
		getElectionsSession().logOut();
	}

	public static void setLocale(String language) {
		getElectionsSession().setLocale(new Locale(language.toUpperCase(), language.toUpperCase()));
	}

	public static boolean isSignedIn() {
		return getElectionsSession().isSignedIn();
	}

}
