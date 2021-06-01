package net.lacnic.elections.admin.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.Request;

import net.lacnic.elections.domain.UserAdmin;

public class SisEleccionesManagerSession extends AuthenticatedWebSession {

	private static final long serialVersionUID = 5650965863312480143L;

	private String adminId;
	private Long idEleccionAutorizado;
	private String signoPass;

	public SisEleccionesManagerSession(Request request) {
		super(request);
	}

	@Override
	public boolean authenticate(String adminId, String password) {
		UserAdmin a = AppContext.getInstance().getManagerBeanRemote().userAdminLogin(adminId, password, getIPClient());
		
		String lang;
		
		if (a != null) {
			setAdminId(adminId);
			setSignoPass(password);
			setIdEleccionAutorizado(a.getAuthorizedElectionId());
			
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
			//setLocale(new Locale("ES"));
			
			return true;
		}
		return false;
	}

	@Override
	public Roles getRoles() {
		Roles roles = null;
		if (isSignedIn()) {
			List<String> rr = new ArrayList<>();
			rr.add("siselecciones-only-one");
			if (getIdEleccionAutorizado() == 0)
				rr.add("siselecciones-manager");

			roles = addRollesToSession(rr);
		}
		return roles;
	}

	private Roles addRollesToSession(List<String> rolesList) {
		Roles roles = new Roles();
		if (rolesList != null) {
			roles.addAll(rolesList);
		}
		return roles;
	}

	public void logOut() {
		signOut();

	}

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public String getSignoPass() {
		return signoPass;
	}

	public void setSignoPass(String signoPass) {
		this.signoPass = signoPass;
	}

	public static String getIPClient() {
		WebClientInfo info = get().getClientInfo();
		return info.getProperties().getRemoteAddress();
	}

	public Long getIdEleccionAutorizado() {
		return idEleccionAutorizado;
	}

	public void setIdEleccionAutorizado(Long idEleccionAutorizada) {
		this.idEleccionAutorizado = idEleccionAutorizada;
	}
}
