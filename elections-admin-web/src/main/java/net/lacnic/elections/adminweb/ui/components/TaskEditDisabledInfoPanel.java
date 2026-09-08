package net.lacnic.elections.adminweb.ui.components;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.utils.Constants;

public class TaskEditDisabledInfoPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private final String contactEmail;
	private final String candidateEmail;
	private final String candidateName;
	private final String electionTitle;
	private final String token;

	public TaskEditDisabledInfoPanel(String id, String contactEmail, String candidateEmail, String candidateName, String electionTitle, String token) {
		super(id);
		this.contactEmail = StringUtils.defaultIfBlank(contactEmail, getString("taskEditDisabledFallbackSender"));
		this.candidateEmail = StringUtils.defaultIfBlank(candidateEmail, getString("taskEditDisabledFallbackCandidateEmail"));
		this.candidateName = StringUtils.defaultIfBlank(candidateName, getString("taskEditDisabledFallbackCandidateName"));
		this.electionTitle = StringUtils.defaultIfBlank(electionTitle, getString("taskEditDisabledFallbackElectionTitle"));
		this.token = StringUtils.defaultString(token);
		initializeComponents();
	}

	public TaskEditDisabledInfoPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id);
		this.contactEmail = StringUtils.defaultIfBlank(resolveDefaultSender(resolution), getString("taskEditDisabledFallbackSender"));
		this.candidateEmail = StringUtils.defaultIfBlank(resolveCandidateEmail(resolution), getString("taskEditDisabledFallbackCandidateEmail"));
		this.candidateName = StringUtils.defaultIfBlank(resolveCandidateName(resolution), getString("taskEditDisabledFallbackCandidateName"));
		this.electionTitle = StringUtils.defaultIfBlank(resolveElectionTitle(resolution), getString("taskEditDisabledFallbackElectionTitle"));
		this.token = resolveToken(resolution);
		initializeComponents();
	}

        private void initializeComponents() {
            add(new Label("cardHeaderTitle", getString("taskEditDisabledCardHeader")));
            add(new Label("messageTitle", getString("taskEditDisabledTitle")));
            add(new Label("messageBody", getString("taskEditDisabledBody")));
            add(new Label("contactInstructions", MessageFormat.format(getString("taskEditDisabledContactInstructions"), this.candidateEmail)));
            ExternalLink contactEmailLink = new ExternalLink("contactEmailLink", buildContactEmailHref(), this.contactEmail);
            contactEmailLink.add(new AttributeModifier("aria-label", this.contactEmail));
            add(contactEmailLink);
        add(new BookmarkablePageLink<Void>("backToTasks", GenericAcceptNominationTasksPage.class, buildDefaultPageParameters()));
        }

	private String resolveCandidateEmail(AcceptNominationTaskResolution resolution) {
		if (resolution != null && resolution.getCandidate() != null && StringUtils.isNotBlank(resolution.getCandidate().getMail())) {
			return resolution.getCandidate().getMail();
		}
		return null;
	}

	private String resolveCandidateName(AcceptNominationTaskResolution resolution) {
		if (resolution != null && resolution.getCandidate() != null && StringUtils.isNotBlank(resolution.getCandidate().getName())) {
			return resolution.getCandidate().getName();
		}
		return null;
	}

	private String resolveElectionTitle(AcceptNominationTaskResolution resolution) {
		if (resolution != null && resolution.getNomination() != null && resolution.getNomination().getElection() != null) {
			String title = resolution.getNomination().getElection().getTitle(SecurityUtils.getLocale().getLanguage());
			if (StringUtils.isNotBlank(title)) {
				return title;
			}
		}
		return null;
	}

	private String resolveDefaultSender(AcceptNominationTaskResolution resolution) {
		if (resolution == null) {
			return null;
		}
		try {
			Parameter parameter = AppContext.getInstance().getManagerBeanRemote().getParameter(Constants.DEFAULT_SENDER);
			if (parameter != null && StringUtils.isNotBlank(parameter.getValue())) {
				return parameter.getValue();
			}
		} catch (Exception ex) {
			appLogger.error("Unable to resolve default sender for disabled edit task panel", ex);
		}
		return null;
	}

	private String resolveToken(AcceptNominationTaskResolution resolution) {
		if (resolution != null && resolution.getNomination() != null) {
			return StringUtils.defaultString(resolution.getNomination().getAcceptNominationToken());
		}
		return "";
	}

	private String buildContactEmailHref() {
		String subject = MessageFormat.format(getString("taskEditDisabledMailSubject"), candidateName, electionTitle);
		return "mailto:" + contactEmail + "?subject=" + encode(subject);
	}

	private String encode(String value) {
		return URLEncoder.encode(StringUtils.defaultString(value), StandardCharsets.UTF_8);
	}

	private PageParameters buildDefaultPageParameters() {
		return UtilsParameters.getToken(token);
	}
}
