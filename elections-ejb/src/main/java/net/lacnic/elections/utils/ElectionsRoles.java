package net.lacnic.elections.utils;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ElectionsRoles {

	public static final String ELECTIONS_MANAGER = "elections-manager";
	public static final String ELECTIONS_DELETER = "elections-deleter";
	public static final String ELECTIONS_STATUTARY_ONLY = "elections-statutary-only";
	public static final String ELECTIONS_NON_STATUTARY_ONLY = "elections-non-statutary-only";

	private ElectionsRoles() {
	}

	public static Set<String> supportedPortalRoles() {
		LinkedHashSet<String> roles = new LinkedHashSet<>();
		roles.add(ELECTIONS_MANAGER);
		roles.add(ELECTIONS_DELETER);
		roles.add(ELECTIONS_STATUTARY_ONLY);
		roles.add(ELECTIONS_NON_STATUTARY_ONLY);
		return roles;
	}
}
