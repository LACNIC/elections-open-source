package net.lacnic.elections.adminweb.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.security.jacc.PolicyContextException;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.Request;

import net.lacnic.elections.adminweb.wicket.util.UtilsString;
import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.utils.Constants;
import net.lacnic.portal.auth.client.LoginData;

public class ElectionsWebAdminSession extends AuthenticatedWebSession {

	private static final long serialVersionUID = 5650965863312480143L;

	private String userAdminId;
	private String password;
	private Long authorizedElectionId;
	private UserAdmin userAdmin;


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
		
		Parameter parameter =AppContext.getInstance().getManagerBeanRemote().getParameter(Constants.WS_AUTH_METHOD);

		if(parameter.getValue().equals(Constants.WS_AUTH_TYPE_APP)) {
			 userAdmin = AppContext.getInstance().getManagerBeanRemote().userAdminLogin(userAdminId,UtilsString.wantHashMd5(password), getIPClient());

		}else {
			 userAdmin = AppContext.getInstance().getManagerBeanRemote().login(userAdminId, password, getIPClient());		
		}		

		String lang;

		if (userAdmin != null) {
			setUserAdminId(userAdminId);
			setPassword(password);
			setAuthorizedElectionId(userAdmin.getAuthorizedElectionId());

			lang = getLocale().getLanguage();			
			switch(lang) {
			case "pt":
				setLocale(new Locale("PT"));
				break;
			case "en":
				setLocale(new Locale("EN"));
				break;
			case "es":
				setLocale(new Locale("ES"));
				break;				
			default:
				setLocale(new Locale("ES"));
			}			

			return true;
		}
		return false;
	}

	@Override
	public Roles getRoles() {
		Roles roles = null;
		if (isSignedIn()) {
			List<String> rr = new ArrayList<>();
			rr.add("elections-only-one");
			if (getAuthorizedElectionId() == 0)
				rr.add("elections-manager");

			roles = addRolesToSession(rr);
		}
		return roles;
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

	}

	public static String getIPClient() {
		WebClientInfo info = get().getClientInfo();
		return info.getProperties().getRemoteAddress();
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

	public Long getAuthorizedElectionId() {
		return authorizedElectionId;
	}

	public void setAuthorizedElectionId(Long authorizedElectionId) {
		this.authorizedElectionId = authorizedElectionId;
	}

}