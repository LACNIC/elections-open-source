package net.lacnic.elections.adminweb.wicket.util;

import org.apache.wicket.request.mapper.parameter.PageParameters;


public class UtilsParameters {

	public static String getIdText() {
		return "id";
	}

	public static String getUserText() {
		return "user";
	}

	public static String getCandidateText() {
		return "candidate";
	}

	public static String getAuditText() {
		return "audit";
	}

	public static String getAdminText() {
		return "admin";
	}

	public static String getClaveText() {
		return "clave";
	}

	public static String getPlatilla() {
		return "plantilla";
	}

	public static String getFilterText() {
		return "filter";
	}

	public static String getFilterValueAll() {
		return "all";
	}

	public static String getOrgIdText() {
		return "orgId";
	}

	private static String getTokenText() {
		return "token";
	}

	public static String getFilter(PageParameters params) {
		return params.get(getFilterText()).toString("");
	}

	public static PageParameters getOrgId(String orgId) {
		return getParams(getOrgIdText(), orgId);
	}

	public static PageParameters getFilter(String filter) {
		return getParams(getFilterText(), filter);
	}

	public static boolean isAll(PageParameters params) {
		return getFilter(params).compareToIgnoreCase(getFilterValueAll()) == 0;
	}

	public static boolean isId(PageParameters params) {
		return !getId(params).isEmpty();
	}

	public static PageParameters getFilterAll() {
		return getParams(getFilterText(), getFilterValueAll());
	}

	public static PageParameters getId(String id) {
		return getParams(getIdText(), id);
	}

	public static PageParameters getUser(String id) {
		return getParams(getUserText(), id);
	}

	public static PageParameters getCandidate(String id) {
		return getParams(getCandidateText(), id);
	}

	public static PageParameters getAudit(String id) {
		return getParams(getAuditText(), id);
	}

	public static PageParameters getAdminId(String id) {
		return getParams(getAdminText(), id);
	}

	public static PageParameters getClaveId(String id) {
		return getParams(getClaveText(), id);
	}

	public static PageParameters getToken(String token) {
		return getParams(getTokenText(), token);
	}

	public static PageParameters getId(Long id) {
		return getId(String.valueOf(id));
	}

	public static PageParameters getUser(Long id) {
		return getUser(String.valueOf(id));
	}

	public static PageParameters getCandidate(Long id) {
		return getCandidate(String.valueOf(id));
	}

	public static PageParameters getAudit(Long id) {
		return getAudit(String.valueOf(id));
	}

	public static String getId(PageParameters params) {
		return params.get(getIdText()).toString("");
	}

	public static String getAdminId(PageParameters params) {
		return params.get(getAdminText()).toString("");
	}

	public static String getClaveId(PageParameters params) {
		return params.get(getClaveText()).toString("");
	}

	public static String getOrgId(PageParameters params) {
		return params.get(getOrgIdText()).toString("");
	}

	public static PageParameters getParams(String key, String value) {
		PageParameters pars = new PageParameters();
		pars.add(key, value);

		return pars;
	}

	public static PageParameters getParams(String key, Object value) {
		PageParameters pars = new PageParameters();
		pars.add(key, value);

		return pars;
	}

	public static String getParameters(String key, PageParameters pars) {
		return pars.get(key).toString("");
	}

	public static long getIdAsLong(PageParameters params) {
		return params.get(getIdText()).toLong(0);
	}

	public static long getUserAsLong(PageParameters params) {
		return params.get(getUserText()).toLong(0);
	}

	public static long getAuditAsLong(PageParameters params) {
		return params.get(getAuditText()).toLong(0);
	}

	public static long getCandidateAsLong(PageParameters params) {
		return params.get(getCandidateText()).toLong(0);
	}

	public static String getToken(PageParameters pars) {
		return pars.get(getTokenText()).toString("");
	}
}
