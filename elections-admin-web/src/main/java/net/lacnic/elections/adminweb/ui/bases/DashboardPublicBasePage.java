package net.lacnic.elections.adminweb.ui.bases;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.commons.PublicTopBarPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;


public abstract class DashboardPublicBasePage extends WebPage {

	private static final long serialVersionUID = 6861984885215804314L;

	private Election election;
	private Election[] elections;
	private String token;


	public DashboardPublicBasePage(PageParameters params) {
		setToken(UtilsParameters.getToken(params));
		Class classError = validateToken(params);
		if (classError != null) {
			setResponsePage(classError);
		}
		add(new PublicTopBarPanel("topBarPublic"));
	}

	public String getIP() {
		WebClientInfo info = (WebClientInfo) getSession().getClientInfo();
		return info.getProperties().getRemoteAddress();
	}


	protected abstract Class validateToken(PageParameters params);


	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Election[] getElections() {
		return elections;
	}

	public void setElections(Election[] elections) {
		this.elections = elections;
	}

	public String getLanguage() {
		return SecurityUtils.getLocale().getLanguage();
	}

}
