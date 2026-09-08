package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import java.io.Serializable;
import java.util.Locale;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;

public class EmailTemplateFilterState implements Serializable {

	private static final long serialVersionUID = -1270857064188885273L;

	static final String PARAM_RECIPIENT = "recipient";
	static final String PARAM_ELECTION_SCOPE = "electionScope";
	static final String PARAM_LANGUAGE_MODE = "languageMode";
	static final String PARAM_SEND_MODE = "sendMode";
	static final String PARAM_TEMPLATE_TYPE = "templateType";

	private EmailTemplateFilterCatalog.RecipientFilter recipientFilter = EmailTemplateFilterCatalog.RecipientFilter.ALL;
	private EmailTemplateFilterCatalog.ElectionScopeFilter electionScopeFilter = EmailTemplateFilterCatalog.ElectionScopeFilter.ALL;
	private EmailTemplateFilterCatalog.LanguageModeFilter languageModeFilter = EmailTemplateFilterCatalog.LanguageModeFilter.ALL;
	private EmailTemplateFilterCatalog.SendModeFilter sendModeFilter = EmailTemplateFilterCatalog.SendModeFilter.ALL;
	private String templateType;

	public static EmailTemplateFilterState from(PageParameters params) {
		EmailTemplateFilterState state = new EmailTemplateFilterState();
		if (params == null) {
			return state;
		}
		state.setRecipientFilter(parseEnum(params.get(PARAM_RECIPIENT).toString(""), EmailTemplateFilterCatalog.RecipientFilter.ALL));
		state.setElectionScopeFilter(parseEnum(params.get(PARAM_ELECTION_SCOPE).toString(""), EmailTemplateFilterCatalog.ElectionScopeFilter.ALL));
		state.setLanguageModeFilter(parseEnum(params.get(PARAM_LANGUAGE_MODE).toString(""), EmailTemplateFilterCatalog.LanguageModeFilter.ALL));
		state.setSendModeFilter(parseEnum(params.get(PARAM_SEND_MODE).toString(""), EmailTemplateFilterCatalog.SendModeFilter.ALL));
		state.setTemplateType(params.get(PARAM_TEMPLATE_TYPE).toString(""));
		return state;
	}

	private static <T extends Enum<T>> T parseEnum(String value, T defaultValue) {
		if (defaultValue == null || value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			return Enum.valueOf(defaultValue.getDeclaringClass(), value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}

	public PageParameters toPageParameters(long electionId) {
		PageParameters params = UtilsParameters.getId(electionId);
		if (recipientFilter != null && recipientFilter != EmailTemplateFilterCatalog.RecipientFilter.ALL) {
			params.add(PARAM_RECIPIENT, recipientFilter.name());
		}
		if (electionScopeFilter != null && electionScopeFilter != EmailTemplateFilterCatalog.ElectionScopeFilter.ALL) {
			params.add(PARAM_ELECTION_SCOPE, electionScopeFilter.name());
		}
		if (languageModeFilter != null && languageModeFilter != EmailTemplateFilterCatalog.LanguageModeFilter.ALL) {
			params.add(PARAM_LANGUAGE_MODE, languageModeFilter.name());
		}
		if (sendModeFilter != null && sendModeFilter != EmailTemplateFilterCatalog.SendModeFilter.ALL) {
			params.add(PARAM_SEND_MODE, sendModeFilter.name());
		}
		if (templateType != null) {
			params.add(PARAM_TEMPLATE_TYPE, templateType);
		}
		return params;
	}

	public boolean matches(ElectionEmailTemplate template) {
		if (template == null) {
			return false;
		}
		if (!matchesTemplateType(template.getTemplateType())) {
			return false;
		}
		return true;
	}

	public boolean matchesTemplateType(String value) {
		if (templateType == null) {
			return true;
		}
		String normalizedValue = normalize(value);
		return normalizedValue != null && normalizedValue.contains(templateType.toLowerCase(Locale.ROOT));
	}

	private static String normalize(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return value.trim().toLowerCase(Locale.ROOT);
	}

	public EmailTemplateFilterCatalog.RecipientFilter getRecipientFilter() {
		return recipientFilter;
	}

	public void setRecipientFilter(EmailTemplateFilterCatalog.RecipientFilter recipientFilter) {
		this.recipientFilter = recipientFilter != null ? recipientFilter : EmailTemplateFilterCatalog.RecipientFilter.ALL;
	}

	public EmailTemplateFilterCatalog.ElectionScopeFilter getElectionScopeFilter() {
		return electionScopeFilter;
	}

	public void setElectionScopeFilter(EmailTemplateFilterCatalog.ElectionScopeFilter electionScopeFilter) {
		this.electionScopeFilter = electionScopeFilter != null ? electionScopeFilter : EmailTemplateFilterCatalog.ElectionScopeFilter.ALL;
	}

	public EmailTemplateFilterCatalog.LanguageModeFilter getLanguageModeFilter() {
		return languageModeFilter;
	}

	public void setLanguageModeFilter(EmailTemplateFilterCatalog.LanguageModeFilter languageModeFilter) {
		this.languageModeFilter = languageModeFilter != null ? languageModeFilter : EmailTemplateFilterCatalog.LanguageModeFilter.ALL;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = normalize(templateType);
	}

	public EmailTemplateFilterCatalog.SendModeFilter getSendModeFilter() {
		return sendModeFilter;
	}

	public void setSendModeFilter(EmailTemplateFilterCatalog.SendModeFilter sendModeFilter) {
		this.sendModeFilter = sendModeFilter != null ? sendModeFilter : EmailTemplateFilterCatalog.SendModeFilter.ALL;
	}
}
