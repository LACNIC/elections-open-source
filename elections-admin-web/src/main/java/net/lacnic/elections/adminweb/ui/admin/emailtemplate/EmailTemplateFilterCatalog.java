package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.utils.EmailTemplateType;

public final class EmailTemplateFilterCatalog {

	private static final EnumSet<ElectionScopeFilter> ALL_ELECTION_SCOPES = EnumSet.of(ElectionScopeFilter.STATUTORY, ElectionScopeFilter.NON_STATUTORY);
	private static final EnumSet<ElectionScopeFilter> STATUTORY_ONLY = EnumSet.of(ElectionScopeFilter.STATUTORY);
	private static final EnumSet<ElectionScopeFilter> NON_STATUTORY_ONLY = EnumSet.of(ElectionScopeFilter.NON_STATUTORY);
	private static final Map<String, TemplateMetadata> TEMPLATE_METADATA = buildTemplateMetadata();

	private EmailTemplateFilterCatalog() {
	}

	public interface LocalizedFilterOption {
		String getResourceKey();
	}

	public enum RecipientFilter implements LocalizedFilterOption {
		ALL("mailTemplFilterRecipient.ALL"),
		AUDITORS("mailTemplFilterRecipient.AUDITORS"),
		CANDIDATES("mailTemplFilterRecipient.CANDIDATES"),
		VOTERS("mailTemplFilterRecipient.VOTERS"),
		ORGANIZATIONS("mailTemplFilterRecipient.ORGANIZATIONS"),
		ADMINISTRATION("mailTemplFilterRecipient.ADMINISTRATION"),
		MIXED("mailTemplFilterRecipient.MIXED");

		private final String resourceKey;

		RecipientFilter(String resourceKey) {
			this.resourceKey = resourceKey;
		}

		@Override
		public String getResourceKey() {
			return resourceKey;
		}
	}

	public enum ElectionScopeFilter implements LocalizedFilterOption {
		ALL("mailTemplFilterElectionScope.ALL"),
		STATUTORY("mailTemplFilterElectionScope.STATUTORY"),
		NON_STATUTORY("mailTemplFilterElectionScope.NON_STATUTORY");

		private final String resourceKey;

		ElectionScopeFilter(String resourceKey) {
			this.resourceKey = resourceKey;
		}

		@Override
		public String getResourceKey() {
			return resourceKey;
		}
	}

	public enum LanguageModeFilter implements LocalizedFilterOption {
		ALL("mailTemplFilterLanguageMode.ALL"),
		STANDARD("mailTemplFilterLanguageMode.STANDARD"),
		TRILINGUAL("mailTemplFilterLanguageMode.TRILINGUAL");

		private final String resourceKey;

		LanguageModeFilter(String resourceKey) {
			this.resourceKey = resourceKey;
		}

		@Override
		public String getResourceKey() {
			return resourceKey;
		}
	}

	public enum SendModeFilter implements LocalizedFilterOption {
		ALL("mailTemplFilterSendMode.ALL"),
		MANUAL("mailTemplFilterSendMode.MANUAL"),
		AUTOMATIC("mailTemplFilterSendMode.AUTOMATIC");

		private final String resourceKey;

		SendModeFilter(String resourceKey) {
			this.resourceKey = resourceKey;
		}

		@Override
		public String getResourceKey() {
			return resourceKey;
		}
	}

	public static final class TemplateMetadata {
		private final RecipientFilter recipientFilter;
		private final EnumSet<ElectionScopeFilter> electionScopes;
		private final LanguageModeFilter languageModeFilter;
		private final SendModeFilter sendModeFilter;

		private TemplateMetadata(RecipientFilter recipientFilter, EnumSet<ElectionScopeFilter> electionScopes, LanguageModeFilter languageModeFilter, SendModeFilter sendModeFilter) {
			this.recipientFilter = recipientFilter;
			this.electionScopes = electionScopes != null && !electionScopes.isEmpty() ? EnumSet.copyOf(electionScopes) : EnumSet.copyOf(ALL_ELECTION_SCOPES);
			this.languageModeFilter = languageModeFilter != null ? languageModeFilter : LanguageModeFilter.STANDARD;
			this.sendModeFilter = sendModeFilter != null ? sendModeFilter : SendModeFilter.MANUAL;
		}

		public RecipientFilter getRecipientFilter() {
			return recipientFilter;
		}

		public EnumSet<ElectionScopeFilter> getElectionScopes() {
			return EnumSet.copyOf(electionScopes);
		}

		public LanguageModeFilter getLanguageModeFilter() {
			return languageModeFilter;
		}

		public SendModeFilter getSendModeFilter() {
			return sendModeFilter;
		}
	}

	public static List<ElectionEmailTemplate> applyFilters(List<ElectionEmailTemplate> templates, EmailTemplateFilterState filterState) {
		if (templates == null || templates.isEmpty()) {
			return Collections.emptyList();
		}
		List<ElectionEmailTemplate> filteredTemplates = new ArrayList<>();
		for (ElectionEmailTemplate template : templates) {
			if (matches(template, filterState)) {
				filteredTemplates.add(template);
			}
		}
		return filteredTemplates;
	}

	public static boolean matches(ElectionEmailTemplate template, EmailTemplateFilterState filterState) {
		if (template == null) {
			return false;
		}
		if (filterState == null || !filterState.matches(template)) {
			return filterState == null;
		}
		TemplateMetadata metadata = resolveMetadata(template.getTemplateType());
		if (filterState.getRecipientFilter() != RecipientFilter.ALL && metadata.getRecipientFilter() != filterState.getRecipientFilter()) {
			return false;
		}
		if (filterState.getElectionScopeFilter() != ElectionScopeFilter.ALL && !metadata.getElectionScopes().contains(filterState.getElectionScopeFilter())) {
			return false;
		}
		if (filterState.getLanguageModeFilter() != LanguageModeFilter.ALL && metadata.getLanguageModeFilter() != filterState.getLanguageModeFilter()) {
			return false;
		}
		return filterState.getSendModeFilter() == SendModeFilter.ALL || metadata.getSendModeFilter() == filterState.getSendModeFilter();
	}

	public static TemplateMetadata resolveMetadata(String templateType) {
		String normalizedType = normalizeTemplateType(templateType);
		if (normalizedType == null) {
			return fallbackMetadata(templateType);
		}
		TemplateMetadata metadata = TEMPLATE_METADATA.get(normalizedType);
		return metadata != null ? metadata : fallbackMetadata(normalizedType);
	}

	private static Map<String, TemplateMetadata> buildTemplateMetadata() {
		Map<String, TemplateMetadata> metadata = new LinkedHashMap<>();

		register(metadata, EmailTemplateType.NEW, RecipientFilter.MIXED, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.STANDARD_DISPATCH_NOTICE, RecipientFilter.ADMINISTRATION, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.SIGNATURE, RecipientFilter.ADMINISTRATION, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.AUDITOR, RecipientFilter.AUDITORS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.AUDITOR_SP_PT_EN, RecipientFilter.AUDITORS, ALL_ELECTION_SCOPES, LanguageModeFilter.TRILINGUAL);
		register(metadata, EmailTemplateType.AUDITOR_NOMINATION_SP_PT_EN, RecipientFilter.AUDITORS, ALL_ELECTION_SCOPES, LanguageModeFilter.TRILINGUAL);
		register(metadata, EmailTemplateType.AUDITOR_AGREEMENT, RecipientFilter.AUDITORS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.AUDITOR_REVISION, RecipientFilter.AUDITORS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.AUDITOR_CANDIDATE_APPROVED, RecipientFilter.ADMINISTRATION, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.AUDITOR_CANDIDATE_REJECTED, RecipientFilter.ADMINISTRATION, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.ELECTION_NOTICE, RecipientFilter.VOTERS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.ELECTION_NOTICE_SP_PT_EN, RecipientFilter.VOTERS, ALL_ELECTION_SCOPES, LanguageModeFilter.TRILINGUAL);
		register(metadata, EmailTemplateType.SYNC_ERROR_ALERT, RecipientFilter.ADMINISTRATION, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.ELECTION_START, RecipientFilter.VOTERS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.ELECTION_START_SP_PT_EN, RecipientFilter.VOTERS, ALL_ELECTION_SCOPES, LanguageModeFilter.TRILINGUAL);
		register(metadata, EmailTemplateType.ELECTION_ABOUT_TO_END, RecipientFilter.VOTERS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.ELECTION_ABOUT_TO_END_SP_PT_EN, RecipientFilter.VOTERS, ALL_ELECTION_SCOPES, LanguageModeFilter.TRILINGUAL);
		register(metadata, EmailTemplateType.VOTE_RESULT, RecipientFilter.VOTERS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.VOTE_RESULT_SP_PT_EN, RecipientFilter.VOTERS, ALL_ELECTION_SCOPES, LanguageModeFilter.TRILINGUAL);
		register(metadata, EmailTemplateType.VOTE_CODES, RecipientFilter.VOTERS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.NOMINATION_SUBMITTED_NOMINEE, RecipientFilter.CANDIDATES, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.NOMINATION_ORG_CANDIDATES_INVITATION, RecipientFilter.ORGANIZATIONS, STATUTORY_ONLY, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.NOMINATION_ORG_CANDIDATES_INVITATION_NOTA_NO_VOTA, RecipientFilter.ORGANIZATIONS, NON_STATUTORY_ONLY, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.NOMINATION_ORG_SUPPORT_REQUEST, RecipientFilter.ORGANIZATIONS, STATUTORY_ONLY, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.NOMINATION_USER_SUPPORT_REQUEST, RecipientFilter.MIXED, NON_STATUTORY_ONLY, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.NOMINATION_SUPPORT_STATUS_APPROVED_CANDIDATE, RecipientFilter.CANDIDATES, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.NOMINATION_SUPPORT_STATUS_REJECTED_CANDIDATE, RecipientFilter.CANDIDATES, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.NOMINATION_ACCEPTED_REPRESENTATIVE, RecipientFilter.ORGANIZATIONS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.NOMINATION_REJECTED_REPRESENTATIVE, RecipientFilter.ORGANIZATIONS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.SUPPORT_REMINDER, RecipientFilter.MIXED, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.SUPPORT_REQUESTER_REMINDER, RecipientFilter.CANDIDATES, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.NOMINATION_REMINDER, RecipientFilter.CANDIDATES, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.CANDIDATE_REMINDER, RecipientFilter.CANDIDATES, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.AUDITOR_REMINDER, RecipientFilter.AUDITORS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.AUDITOR_REMINDER_REVISION, RecipientFilter.AUDITORS, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.LINK_RECOVERY, RecipientFilter.MIXED, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.CANDIDATE_CONFIRMED_AND_PUBLISHED, RecipientFilter.CANDIDATES, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.CANDIDATE_QUESTION_STATUS_NOTIFICATION, RecipientFilter.MIXED, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.CANDIDATE_TRAINING_ACCESS_REQUESTED_ADMIN, RecipientFilter.CANDIDATES, STATUTORY_ONLY, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.CANDIDATE_EVALUATION_ACCESS_REQUESTED_ADMIN, RecipientFilter.ADMINISTRATION, STATUTORY_ONLY, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.CANDIDATE_TASK_COMPLETED_ADMIN, RecipientFilter.ADMINISTRATION, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);
		register(metadata, EmailTemplateType.CANDIDATE_ALL_TASKS_COMPLETED_ADMIN, RecipientFilter.ADMINISTRATION, ALL_ELECTION_SCOPES, LanguageModeFilter.STANDARD);

		return Collections.unmodifiableMap(metadata);
	}

	private static void register(Map<String, TemplateMetadata> metadata, EmailTemplateType templateType, RecipientFilter recipientFilter, Collection<ElectionScopeFilter> electionScopes,
			LanguageModeFilter languageModeFilter) {
		if (metadata == null || templateType == null) {
			return;
		}
		EnumSet<ElectionScopeFilter> scopes = electionScopes == null || electionScopes.isEmpty() ? EnumSet.copyOf(ALL_ELECTION_SCOPES) : EnumSet.copyOf(electionScopes);
		metadata.put(templateType.getKey(), new TemplateMetadata(recipientFilter, scopes, languageModeFilter, resolveSendMode(templateType)));
	}

	private static TemplateMetadata fallbackMetadata(String templateType) {
		return new TemplateMetadata(RecipientFilter.MIXED, ALL_ELECTION_SCOPES, isTrilingualTemplate(templateType) ? LanguageModeFilter.TRILINGUAL : LanguageModeFilter.STANDARD, resolveSendMode(templateType));
	}

	private static SendModeFilter resolveSendMode(EmailTemplateType templateType) {
		if (templateType == null) {
			return SendModeFilter.MANUAL;
		}
		return templateType.shouldShowSendButton() ? SendModeFilter.MANUAL : SendModeFilter.AUTOMATIC;
	}

	private static SendModeFilter resolveSendMode(String templateType) {
		return resolveSendMode(EmailTemplateType.fromKey(normalizeTemplateType(templateType)));
	}

	private static boolean isTrilingualTemplate(String templateType) {
		String normalizedType = normalizeTemplateType(templateType);
		return normalizedType != null && normalizedType.endsWith("_SP_PT_EN");
	}

	private static String normalizeTemplateType(String templateType) {
		if (templateType == null || templateType.trim().isEmpty()) {
			return null;
		}
		return templateType.trim().toUpperCase(Locale.ROOT);
	}
}
