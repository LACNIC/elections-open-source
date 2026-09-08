package net.lacnic.elections.adminweb.ui.commons;

import org.apache.wicket.request.cycle.RequestCycle;

public final class ProjectUrls {

	public static final String SOURCE_CODE = "https://github.com/LACNIC/elections-open-source-v3";
	public static final String CONTACT = "mailto:desarrollo@lacnic.net";

	private static final String DOCUMENTATION_ROOT = "documentacion/";

	private ProjectUrls() {
	}

	public static String documentation() {
		return contextRelative(DOCUMENTATION_ROOT);
	}

	public static String installationGuide() {
		return contextRelative(DOCUMENTATION_ROOT + "manual.html");
	}

	public static String servicesDocumentation() {
		return contextRelative(DOCUMENTATION_ROOT + "services.html");
	}

	public static String releaseNotes() {
		return contextRelative(DOCUMENTATION_ROOT + "release-notes.html");
	}

	public static String license() {
		return contextRelative(DOCUMENTATION_ROOT + "LICENSE.txt");
	}

	private static String contextRelative(String path) {
		return RequestCycle.get().getUrlRenderer().renderContextRelativeUrl(path);
	}
}
