package net.lacnic.elections.adminweb.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

import net.lacnic.elections.adminweb.wicket.util.UtilsString;
import net.lacnic.elections.data.AdminLoginResult;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.ElectionsProperties;
import net.lacnic.elections.utils.ElectionsRoles;

public class ElectionsWebAdminSession extends AuthenticatedWebSession {

	private static final long serialVersionUID = 5650965863312480143L;
	static final int MAX_TRACKED_DO_NOMINATION_RESTRICTED_COUNTRIES_TOKENS = 64;

	private String userAdminId;
	private String password;
	private String totpAux = "";
	private String loginError;
	private UserAdmin userAdmin;
	private Set<String> authenticationRoles = new LinkedHashSet<>();
	private boolean localAuthentication;
	private Set<String> doNominationRestrictedCountriesModalShownTokens = new LinkedHashSet<>();

	public UserAdmin getUserAdmin() {
		return userAdmin;
	}

	public void setUserAdmin(UserAdmin userAdmin) {
		this.userAdmin = userAdmin;
	}

	public ElectionsWebAdminSession(Request request) {
		super(request);
	}

	@Override
	public boolean authenticate(String userAdminId, String password) {

		setAuthenticationRoles(null);
		setLoginError(null);
		userAdmin = null;
		String loginTotp = getTotpAux();
		setTotpAux("");

		boolean localAuth = Constants.WS_AUTH_TYPE_APP.equals(ElectionsProperties.get(Constants.WS_AUTH_METHOD));
		setLocalAuthentication(localAuth);

		if (localAuth) {
			userAdmin = AppContext.getInstance().getManagerBeanRemote().userAdminLogin(userAdminId, UtilsString.wantHashMd5(password), getIPClient());
		} else {
			AdminLoginResult loginResult = AppContext.getInstance().getManagerBeanRemote().login(userAdminId, password, getIPClient(), loginTotp);
			if (loginResult != null) {
				setLoginError(loginResult.getError());
				userAdmin = loginResult.getUserAdmin();
				setAuthenticationRoles(loginResult.getRoles());
			}
		}

		if (userAdmin != null) {
			setLoginError(null);
			setUserAdminId(userAdminId);
			setPassword(password);
			clearDoNominationRestrictedCountriesModalShownTokens();

			LanguageCode languageCode = LanguageCode.fromValueOrDefault(getLocale() != null ? getLocale().getLanguage() : null, LanguageCode.SP);
			setLocale(languageCode.toLocale());

			return true;
		}
		return false;
	}

	@Override
	public Roles getRoles() {
		if (!isSignedIn()) {
			return null;
		}

		List<String> effectiveRoles = new ArrayList<>(getAuthenticationRoles());
		if (isLocalAuthentication() && !effectiveRoles.contains(Constants.elections_manager)) {
			effectiveRoles.add(Constants.elections_manager);
		}
		if (isLocalAuthentication() && !effectiveRoles.contains(ElectionsRoles.ELECTIONS_DELETER)) {
			effectiveRoles.add(ElectionsRoles.ELECTIONS_DELETER);
		}
		if (effectiveRoles.isEmpty()) {
			effectiveRoles.add(Constants.elections_manager);
		}

		return addRolesToSession(effectiveRoles);
	}

	private Roles addRolesToSession(List<String> rolesList) {
		Roles roles = new Roles();
		if (rolesList != null) {
			roles.addAll(rolesList);
		}
		return roles;
	}

	public void logOut() {
		signOut();
		setAuthenticationRoles(null);
		setLocalAuthentication(false);
		setLoginError(null);
		setTotpAux("");
		clearDoNominationRestrictedCountriesModalShownTokens();

	}

	public static String getIPClient() {
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null && requestCycle.getRequest() instanceof ServletWebRequest servletWebRequest) {
			HttpServletRequest request = servletWebRequest.getContainerRequest();
			String xForwardedFor = request.getHeader("X-Forwarded-For");
			String clientIp = extractClientIp(xForwardedFor);
			if (clientIp == null || clientIp.isBlank()) {
				clientIp = request.getHeader("X-Real-IP");
			}
			if (clientIp == null || clientIp.isBlank()) {
				clientIp = request.getRemoteAddr();
			}
			return clientIp;
		}
		WebClientInfo info = get().getClientInfo();
		return info.getProperties().getRemoteAddress();
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

	public String getUserAdminId() {
		return userAdminId;
	}

	public void setUserAdminId(String userAdminId) {
		this.userAdminId = userAdminId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTotpAux() {
		return totpAux;
	}

	public void setTotpAux(String totpAux) {
		this.totpAux = totpAux == null ? "" : totpAux.trim();
	}

	public String getLoginError() {
		return loginError;
	}

	public void setLoginError(String loginError) {
		this.loginError = loginError == null || loginError.trim().isEmpty() ? null : loginError.trim();
	}

	public boolean isLocalAuthentication() {
		return localAuthentication;
	}

	public void setLocalAuthentication(boolean localAuthentication) {
		this.localAuthentication = localAuthentication;
	}

	public Set<String> getAuthenticationRoles() {
		return authenticationRoles == null ? new LinkedHashSet<>() : new LinkedHashSet<>(authenticationRoles);
	}

	public void setAuthenticationRoles(Set<String> authenticationRoles) {
		this.authenticationRoles = authenticationRoles == null ? new LinkedHashSet<>() : new LinkedHashSet<>(authenticationRoles);
	}

	public Set<String> getDoNominationRestrictedCountriesModalShownTokens() {
		return doNominationRestrictedCountriesModalShownTokens == null ? new LinkedHashSet<>() : new LinkedHashSet<>(doNominationRestrictedCountriesModalShownTokens);
	}

	public void setDoNominationRestrictedCountriesModalShownTokens(Set<String> doNominationRestrictedCountriesModalShownTokens) {
		this.doNominationRestrictedCountriesModalShownTokens = doNominationRestrictedCountriesModalShownTokens == null ? new LinkedHashSet<>() : new LinkedHashSet<>(doNominationRestrictedCountriesModalShownTokens);
	}

	public boolean hasDoNominationRestrictedCountriesModalBeenShown(String token) {
		String normalizedToken = normalizeTrackedDoNominationToken(token);
		if (normalizedToken == null) {
			return false;
		}
		return doNominationRestrictedCountriesModalShownTokens != null && doNominationRestrictedCountriesModalShownTokens.contains(normalizedToken);
	}

	public void markDoNominationRestrictedCountriesModalAsShown(String token) {
		String normalizedToken = normalizeTrackedDoNominationToken(token);
		if (normalizedToken == null) {
			return;
		}
		if (doNominationRestrictedCountriesModalShownTokens == null) {
			doNominationRestrictedCountriesModalShownTokens = new LinkedHashSet<>();
		}
		doNominationRestrictedCountriesModalShownTokens.add(normalizedToken);
		enforceTrackedDoNominationRestrictedCountriesTokenLimit();
	}

	public void clearDoNominationRestrictedCountriesModalShownTokens() {
		doNominationRestrictedCountriesModalShownTokens = new LinkedHashSet<>();
	}

	private void enforceTrackedDoNominationRestrictedCountriesTokenLimit() {
		if (doNominationRestrictedCountriesModalShownTokens == null) {
			return;
		}
		Iterator<String> iterator = doNominationRestrictedCountriesModalShownTokens.iterator();
		while (doNominationRestrictedCountriesModalShownTokens.size() > MAX_TRACKED_DO_NOMINATION_RESTRICTED_COUNTRIES_TOKENS && iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	private String normalizeTrackedDoNominationToken(String token) {
		if (token == null || token.trim().isEmpty()) {
			return null;
		}
		return token.trim();
	}

}
