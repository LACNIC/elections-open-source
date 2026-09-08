package net.lacnic.elections.adminweb.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;

import net.lacnic.elections.data.AsyncProcessingType;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.utils.ElectionsRoles;


public class SecurityUtils {

	private SecurityUtils() { }
	private static final MetaDataKey<HashSet<String>> PENDING_ASYNC_COMPLETION_FEEDBACKS = new MetaDataKey<>() { };


	public static ElectionsWebAdminSession getElectionsSession() {
		return ((ElectionsWebAdminSession) Session.get());
	}

	public static String getClientIp() {
		ServletWebRequest servletWebRequest = (ServletWebRequest) RequestCycle.get().getRequest();
		HttpServletRequest request = servletWebRequest.getContainerRequest();
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		String clientIp = extractClientIp(xForwardedFor);
		if (clientIp == null || clientIp.trim().isEmpty()) {
			clientIp = request.getHeader("X-Real-IP");
		}
		if (clientIp == null || clientIp.trim().isEmpty()) {
			clientIp = request.getRemoteAddr();
		}
		return clientIp;
	}

	private static String extractClientIp(String headerValue) {
		if (headerValue == null || headerValue.trim().isEmpty()) {
			return null;
		}
		String[] parts = headerValue.split(",");
		for (String part : parts) {
			if (part != null) {
				String candidate = part.trim();
				if (!candidate.isEmpty()) {
					return candidate;
				}
			}
		}
		return null;
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

	public static LanguageCode getLanguageCode() {
		return LanguageCode.fromValueOrDefault(getLocale() != null ? getLocale().getLanguage() : null, LanguageCode.SP);
	}

	public static void info(String string) {
		getElectionsSession().info(string);
	}

	public static void error(String string) {
		getElectionsSession().error(string);
	}

	private static final List<ElectionCategory> ALL_ELECTION_CATEGORIES = Collections.unmodifiableList(Arrays.asList(ElectionCategory.values()));
	private static final List<ElectionCategory> STATUTORY_ELECTION_CATEGORIES = Collections.unmodifiableList(Arrays.asList(ElectionCategory.STATUTORY));
	private static final List<ElectionCategory> NON_STATUTORY_ELECTION_CATEGORIES = Collections.unmodifiableList(Arrays.asList(ElectionCategory.MODERATORS, ElectionCategory.OTHER, ElectionCategory.TEST));

	public static List<ElectionCategory> getVisibleElectionCategories() {
		if (isManager()) {
			return ALL_ELECTION_CATEGORIES;
		}
		if (hasStatutoryAndNonStatutoryRoles()) {
			return ALL_ELECTION_CATEGORIES;
		}
		if (isStatutoryOnlyUser()) {
			return STATUTORY_ELECTION_CATEGORIES;
		}
		if (isNonStatutoryOnlyUser()) {
			return NON_STATUTORY_ELECTION_CATEGORIES;
		}
		return ALL_ELECTION_CATEGORIES;
	}

	public static List<ElectionCategory> getVisibleElectionCategories(Election election) {
		if (isElectionListedUser(election)) {
			return ALL_ELECTION_CATEGORIES;
		}
		return getVisibleElectionCategories();
	}

	public static void logOut() {
		getElectionsSession().logOut();
	}

	public static void setLocale(LanguageCode language) {
		LanguageCode resolvedLanguage = language != null ? language : LanguageCode.SP;
		getElectionsSession().setLocale(resolvedLanguage.toLocale());
	}

	public static void setLocale(String language) {
		setLocale(LanguageCode.fromValueOrDefault(language, LanguageCode.SP));
	}

	public static boolean isSignedIn() {
		return getElectionsSession().isSignedIn();
	}

	public static boolean isLocalAuthentication() {
		return getElectionsSession().isLocalAuthentication();
	}

	public static boolean hasRole(String role) {
		if (!isSignedIn() || role == null || role.trim().isEmpty()) {
			return false;
		}
		Roles roles = getElectionsSession().getRoles();
		return roles != null && roles.hasRole(role);
	}

	public static boolean hasAnyRole(String... roleNames) {
		if (roleNames == null || roleNames.length == 0) {
			return false;
		}
		for (String roleName : roleNames) {
			if (hasRole(roleName)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isManager() {
		return hasRole(ElectionsRoles.ELECTIONS_MANAGER);
	}

	public static boolean isElectionDeleter() {
		return hasRole(ElectionsRoles.ELECTIONS_DELETER);
	}

	public static boolean isStatutoryOnlyUser() {
		return hasRole(ElectionsRoles.ELECTIONS_STATUTARY_ONLY) && !hasRole(ElectionsRoles.ELECTIONS_NON_STATUTARY_ONLY);
	}

	public static boolean isNonStatutoryOnlyUser() {
		return hasRole(ElectionsRoles.ELECTIONS_NON_STATUTARY_ONLY) && !hasRole(ElectionsRoles.ELECTIONS_STATUTARY_ONLY);
	}

	public static boolean hasStatutoryAndNonStatutoryRoles() {
		return hasRole(ElectionsRoles.ELECTIONS_STATUTARY_ONLY) && hasRole(ElectionsRoles.ELECTIONS_NON_STATUTARY_ONLY);
	}

	public static boolean canAccessElection(Election election) {
		if (election == null) {
			return false;
		}
		if (isManager() || isElectionListedUser(election)) {
			return true;
		}
		if (hasStatutoryAndNonStatutoryRoles()) {
			return true;
		}
		if (isStatutoryOnlyUser()) {
			return election.getCategory() == ElectionCategory.STATUTORY;
		}
		if (isNonStatutoryOnlyUser()) {
			return election.getCategory() != ElectionCategory.STATUTORY;
		}
		return false;
	}

	public static boolean isElectionListedUser(Election election) {
		if (election == null) {
			return false;
		}
		String currentEmail = normalizeUserEmail(getCurrentUserEmail());
		if (currentEmail == null) {
			return false;
		}
		String configuredEmails = election.getAuthorizedUserEmails();
		if (configuredEmails == null || configuredEmails.trim().isEmpty()) {
			return false;
		}
		for (String value : configuredEmails.split(",")) {
			String normalizedValue = normalizeUserEmail(value);
			if (normalizedValue != null && normalizedValue.equals(currentEmail)) {
				return true;
			}
		}
		return false;
	}

	private static String getCurrentUserEmail() {
		if (!isSignedIn() || getElectionsSession().getUserAdmin() == null) {
			return null;
		}
		String email = getElectionsSession().getUserAdmin().getEmail();
		return email == null || email.trim().isEmpty() ? getUserAdminId() : email;
	}

	private static String normalizeUserEmail(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim().toLowerCase(Locale.ROOT);
		return normalized.isEmpty() ? null : normalized;
	}

	public static void markAsyncCompletionFeedbackPending(long electionId, AsyncProcessingType processingType) {
		if (electionId <= 0 || processingType == null) {
			return;
		}
		ElectionsWebAdminSession session = getElectionsSession();
		HashSet<String> pendingFeedbacks = session.getMetaData(PENDING_ASYNC_COMPLETION_FEEDBACKS);
		HashSet<String> updatedFeedbacks = pendingFeedbacks == null ? new HashSet<>() : new HashSet<>(pendingFeedbacks);
		updatedFeedbacks.add(buildAsyncCompletionFeedbackKey(electionId, processingType));
		session.setMetaData(PENDING_ASYNC_COMPLETION_FEEDBACKS, updatedFeedbacks);
	}

	public static boolean consumeAsyncCompletionFeedbackPending(long electionId, AsyncProcessingType processingType) {
		if (electionId <= 0 || processingType == null) {
			return false;
		}
		ElectionsWebAdminSession session = getElectionsSession();
		HashSet<String> pendingFeedbacks = session.getMetaData(PENDING_ASYNC_COMPLETION_FEEDBACKS);
		if (pendingFeedbacks == null || pendingFeedbacks.isEmpty()) {
			return false;
		}
		HashSet<String> updatedFeedbacks = new HashSet<>(pendingFeedbacks);
		boolean removed = updatedFeedbacks.remove(buildAsyncCompletionFeedbackKey(electionId, processingType));
		if (removed) {
			session.setMetaData(PENDING_ASYNC_COMPLETION_FEEDBACKS, updatedFeedbacks.isEmpty() ? null : updatedFeedbacks);
		}
		return removed;
	}

	public static void clearAsyncCompletionFeedbackPending(long electionId, AsyncProcessingType processingType) {
		consumeAsyncCompletionFeedbackPending(electionId, processingType);
	}

	private static String buildAsyncCompletionFeedbackKey(long electionId, AsyncProcessingType processingType) {
		return electionId + ":" + processingType.name();
	}

}
