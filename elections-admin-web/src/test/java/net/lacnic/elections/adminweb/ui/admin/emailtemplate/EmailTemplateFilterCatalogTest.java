package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.utils.EmailTemplateType;

class EmailTemplateFilterCatalogTest {

	@Test
	void resolveMetadataUsesCentralizedClassification() {
		EmailTemplateFilterCatalog.TemplateMetadata voterMetadata = EmailTemplateFilterCatalog.resolveMetadata(EmailTemplateType.ELECTION_NOTICE_SP_PT_EN.getKey());
		assertEquals(EmailTemplateFilterCatalog.RecipientFilter.VOTERS, voterMetadata.getRecipientFilter());
		assertEquals(EmailTemplateFilterCatalog.LanguageModeFilter.TRILINGUAL, voterMetadata.getLanguageModeFilter());
		assertEquals(EmailTemplateFilterCatalog.SendModeFilter.MANUAL, voterMetadata.getSendModeFilter());
		assertEquals(Arrays.asList(EmailTemplateFilterCatalog.ElectionScopeFilter.STATUTORY, EmailTemplateFilterCatalog.ElectionScopeFilter.NON_STATUTORY),
				voterMetadata.getElectionScopes().stream().sorted().toList());

		EmailTemplateFilterCatalog.TemplateMetadata trainingMetadata = EmailTemplateFilterCatalog.resolveMetadata(EmailTemplateType.CANDIDATE_TRAINING_ACCESS_REQUESTED_ADMIN.getKey());
		assertEquals(EmailTemplateFilterCatalog.RecipientFilter.CANDIDATES, trainingMetadata.getRecipientFilter());
		assertIterableEquals(List.of(EmailTemplateFilterCatalog.ElectionScopeFilter.STATUTORY), trainingMetadata.getElectionScopes());
		assertEquals(EmailTemplateFilterCatalog.SendModeFilter.AUTOMATIC, trainingMetadata.getSendModeFilter());

		EmailTemplateFilterCatalog.TemplateMetadata noVotaMetadata = EmailTemplateFilterCatalog
				.resolveMetadata(EmailTemplateType.NOMINATION_ORG_CANDIDATES_INVITATION_NOTA_NO_VOTA.getKey());
		assertEquals(EmailTemplateFilterCatalog.RecipientFilter.ORGANIZATIONS, noVotaMetadata.getRecipientFilter());
		assertIterableEquals(List.of(EmailTemplateFilterCatalog.ElectionScopeFilter.NON_STATUTORY), noVotaMetadata.getElectionScopes());
		assertEquals(EmailTemplateFilterCatalog.LanguageModeFilter.STANDARD, noVotaMetadata.getLanguageModeFilter());
		assertEquals(EmailTemplateFilterCatalog.SendModeFilter.MANUAL, noVotaMetadata.getSendModeFilter());

		EmailTemplateFilterCatalog.TemplateMetadata auditorRevisionReminderMetadata = EmailTemplateFilterCatalog
				.resolveMetadata(EmailTemplateType.AUDITOR_REMINDER_REVISION.getKey());
		assertEquals(EmailTemplateFilterCatalog.RecipientFilter.AUDITORS, auditorRevisionReminderMetadata.getRecipientFilter());
		assertIterableEquals(Arrays.asList(EmailTemplateFilterCatalog.ElectionScopeFilter.STATUTORY,
				EmailTemplateFilterCatalog.ElectionScopeFilter.NON_STATUTORY), auditorRevisionReminderMetadata.getElectionScopes());
		assertEquals(EmailTemplateFilterCatalog.SendModeFilter.AUTOMATIC, auditorRevisionReminderMetadata.getSendModeFilter());
	}

	@Test
	void applyFiltersSupportsRecipientScopeTypeFilters() {
		ElectionEmailTemplate voterTemplate = buildTemplate(10L, EmailTemplateType.ELECTION_NOTICE.getKey());
		ElectionEmailTemplate candidateTemplate = buildTemplate(20L, EmailTemplateType.CANDIDATE_REMINDER.getKey());
		ElectionEmailTemplate orgTemplate = buildTemplate(30L, EmailTemplateType.NOMINATION_ORG_SUPPORT_REQUEST.getKey());

		EmailTemplateFilterState filters = new EmailTemplateFilterState();
		filters.setRecipientFilter(EmailTemplateFilterCatalog.RecipientFilter.CANDIDATES);
		filters.setElectionScopeFilter(EmailTemplateFilterCatalog.ElectionScopeFilter.NON_STATUTORY);
		filters.setSendModeFilter(EmailTemplateFilterCatalog.SendModeFilter.AUTOMATIC);
		filters.setTemplateType("candidate");

		List<ElectionEmailTemplate> filteredTemplates = EmailTemplateFilterCatalog.applyFilters(Arrays.asList(voterTemplate, candidateTemplate, orgTemplate), filters);

		assertEquals(List.of(candidateTemplate), filteredTemplates);
	}

	private ElectionEmailTemplate buildTemplate(long id, String templateType) {
		ElectionEmailTemplate template = new ElectionEmailTemplate();
		template.setElectionEmailTemplateId(id);
		template.setTemplateType(templateType);
		return template;
	}
}
