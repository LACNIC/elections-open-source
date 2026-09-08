package net.lacnic.elections.adminweb.ui.admin.election.organizations;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.ui.components.ButtonUpdateToken;
import net.lacnic.elections.adminweb.ui.components.ButtonViewLink;
import net.lacnic.elections.adminweb.ui.components.ButtonViewLinksList;
import net.lacnic.elections.adminweb.ui.components.DropDownCountry;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.adminweb.validators.OrganizationDeleteActionValidator;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.exception.CensusValidationException;
import net.lacnic.elections.utils.CountryUtils;
import net.lacnic.elections.utils.LinksUtils;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class OrganizationsListPanel extends Panel {

	private static final long serialVersionUID = -8763783352910159177L;
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();
	private static final int ORGANIZATIONS_PAGE_SIZE = 10;
	private static final int VOTES_FILTER_MIN = 0;
	private static final int VOTES_FILTER_MAX = 12;
	private static final String ORGANIZATIONS_DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm";
	private final OrganizationDeleteActionValidator deleteActionValidator = new OrganizationDeleteActionValidator();
	private List<Organization> allOrganizations = Collections.emptyList();
	private List<Organization> filteredOrganizations = Collections.emptyList();
	private PageableListView<Organization> organizationsListView;
	private PagingNavigator organizationsPagerBottom;
	private WebMarkupContainer organizationsEmpty;
	private String organizationsFilter;
	private String countryFilter;
	private String votesFilter;
	private String memberFilter;
	private String debtorFilter;
	private String languageFilter;
	private List<String> votesFilterChoices = Collections.emptyList();
	private List<String> languageFilterChoices = Collections.emptyList();
	private int summaryListedCount;
	private int summaryNonDebtorCount;
	private int summaryMemberCount;
	private int summaryMemberNonDebtorCount;
	private File organizationsUpsertFile;

	public OrganizationsListPanel(String id, Election election) {
		super(id);
		boolean organizationsProcessing = election.isManageOrganizationsManual() && AppContext.getInstance().getManagerBeanRemote().isElectionOrganizationsProcessing(election.getElectionId());
		if (organizationsProcessing) {
			setVisible(false);
			return;
		}
		List<Organization> organizations = AppContext.getInstance().getManagerBeanRemote().getOrganizations(election.getElectionId());
		allOrganizations = organizations == null ? Collections.emptyList() : organizations;
		initializeFilterChoices();
		applyOrganizationsFilter();

		Map<Long, List<SupportNomination>> supportsBySupportingOrganizationId = indexSupportNominationsBySupportingOrganization(
				AppContext.getInstance().getManagerBeanRemote().getElectionSupportNominations(election.getElectionId()));
		boolean manageOrganizationsManual = election.isManageOrganizationsManual();

		add(new Label("summaryListedCount", new PropertyModel<>(this, "summaryListedCount")));
		add(new Label("summaryNonDebtorCount", new PropertyModel<>(this, "summaryNonDebtorCount")));
		add(new Label("summaryMemberCount", new PropertyModel<>(this, "summaryMemberCount")));
		add(new Label("summaryMemberNonDebtorCount", new PropertyModel<>(this, "summaryMemberNonDebtorCount")));
		WebMarkupContainer deleteHeader = new WebMarkupContainer("deleteHeader");
		deleteHeader.setVisible(manageOrganizationsManual);
		add(deleteHeader);

		DownloadLink downloadOrganizationsUploadExcel = new DownloadLink("downloadOrganizationsUploadExcel", new PropertyModel<>(this, "organizationsUpsertFile")) {
			private static final long serialVersionUID = -6327037166991825724L;

			@Override
			public void onClick() {
				setOrganizationsUpsertFile(AppContext.getInstance().getManagerBeanRemote().exportOrganizationsUpsert(election.getElectionId()));
				super.onClick();
			}
		};
		downloadOrganizationsUploadExcel.setDeleteAfterDownload(true);
		downloadOrganizationsUploadExcel.setVisible(!allOrganizations.isEmpty());
		add(downloadOrganizationsUploadExcel);

		Form<Void> filterForm = new Form<>("filterForm");
		filterForm.add(new TextField<>("organizationsFilterInput", new PropertyModel<>(this, "organizationsFilter")));
		filterForm.add(new DropDownCountry("countryFilterInput", new PropertyModel<>(this, "countryFilter"), false));

		DropDownChoice<String> votesFilterInput = new DropDownChoice<>("votesFilterInput", new PropertyModel<>(this, "votesFilter"), new PropertyModel<>(this, "votesFilterChoices"),
				createVotesChoiceRenderer());
		votesFilterInput.setNullValid(true);
		filterForm.add(votesFilterInput);

		DropDownChoice<String> memberFilterInput = new DropDownChoice<>("memberFilterInput", new PropertyModel<>(this, "memberFilter"), Arrays.asList("true", "false"), createMemberChoiceRenderer());
		memberFilterInput.setNullValid(true);
		filterForm.add(memberFilterInput);

		DropDownChoice<String> debtorFilterInput = new DropDownChoice<>("debtorFilterInput", new PropertyModel<>(this, "debtorFilter"), Arrays.asList("true", "false"), createDebtorChoiceRenderer());
		debtorFilterInput.setNullValid(true);
		filterForm.add(debtorFilterInput);

		DropDownChoice<String> languageFilterInput = new DropDownChoice<>("languageFilterInput", new PropertyModel<>(this, "languageFilter"), new PropertyModel<>(this, "languageFilterChoices"),
				createLanguageChoiceRenderer());
		languageFilterInput.setNullValid(true);
		filterForm.add(languageFilterInput);
		filterForm.add(new org.apache.wicket.markup.html.form.Button("applyFilterButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				applyOrganizationsFilter();
				organizationsListView.setCurrentPage(0);
			}
		});
		filterForm.add(new org.apache.wicket.markup.html.form.Button("clearFilterButton") {
			private static final long serialVersionUID = 1L;

			{
				setDefaultFormProcessing(false);
			}

			@Override
			public void onSubmit() {
				organizationsFilter = null;
				countryFilter = null;
				votesFilter = null;
				memberFilter = null;
				debtorFilter = null;
				languageFilter = null;
				applyOrganizationsFilter();
				organizationsListView.setCurrentPage(0);
			}
		});
		add(filterForm);

		organizationsEmpty = new WebMarkupContainer("organizationsEmpty");
		organizationsEmpty.setVisible(filteredOrganizations.isEmpty());
		add(organizationsEmpty);

		organizationsListView = new PageableListView<Organization>("organizationsList", filteredOrganizations, ORGANIZATIONS_PAGE_SIZE) {
			private static final long serialVersionUID = -3775313896314522003L;

			@Override
			protected void populateItem(ListItem<Organization> item) {
				Organization org = item.getModelObject();
				WebMarkupContainer removeOrganizationCell = new WebMarkupContainer("removeOrganizationCell");
				removeOrganizationCell.setVisible(manageOrganizationsManual);
				item.add(new Label("orgId", org.getOrgId()));
				item.add(new Label("name", org.getName()));
				item.add(new Label("votes", org.getVotes() == null ? "" : String.valueOf(org.getVotes())));
				item.add(new Label("category", org.getCategory()));
				item.add(new Label("country", resolveCountryCode(org.getCountry())));
				item.add(new Label("cnpj", org.getCnpj()));
				item.add(new ListView<String>("asnRows", buildAsnRows(org.getAsn())) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(ListItem<String> asnRowItem) {
						asnRowItem.add(new Label("asnRow", asnRowItem.getModelObject()));
					}
				});
				item.add(new Label("membershipContactName", org.getMembershipContactName()));
				item.add(new Label("membershipContactEmail", org.getMembershipContactEmail()));
				item.add(new Label("deudor", org.isDeudor() ? getString("organizationsManagementDebtorYes") : getString("organizationsManagementDebtorNo")));
				item.add(new Label("member", org.isMember() ? getString("organizationsManagementMemberYes") : getString("organizationsManagementMemberNo")));
				item.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  org.isDeudor() ? "table-danger" : ""));
				item.add(new Label("membershipContactId", org.getMembershipContactId()));
				item.add(new Label("membershipContactLanguage", resolveLanguageLabel(org.getMembershipContactLanguage())));
				String nominationLink = LinksUtils.buildDoNominationLink(org.getDoNominationToken());
				String viewActivity = SecurityUtils.getUserAdminId().toUpperCase() + " vió el link de do nomination de la organización " + org.getOrgId();
				item.add(new Label("createdAt", formatDateTime(org.getCreatedAt())));
				item.add(new Label("updatedAt", formatDateTime(org.getUpdatedAt())));
				item.add(new ButtonViewLink("viewLinkButton", item.getIndex(), nominationLink) {
					private static final long serialVersionUID = -6186365304149829346L;

					@Override
					public void registerActivity() {
						AppContext.getInstance().getManagerBeanRemote().persistActivity(SecurityUtils.getUserAdminId(), ActivityType.VIEW_LINK, viewActivity, SecurityUtils.getClientIp(), election.getElectionId());
					}
				});

				List<ButtonViewLinksList.ViewLinkItem> supportLinkItems = buildSupportLinkItems(supportsBySupportingOrganizationId.get(org.getId()));
				String viewSupportLinksActivity = SecurityUtils.getUserAdminId().toUpperCase() + " vió los links de soporte recibidos por la organización " + org.getOrgId() + ". Cantidad de links: "
						+ supportLinkItems.size();
				item.add(new ButtonViewLinksList("viewSupportLinksButton", item.getIndex(), supportLinkItems) {
					private static final long serialVersionUID = 1L;

					@Override
					public void registerActivity() {
						AppContext.getInstance().getManagerBeanRemote().persistActivity(SecurityUtils.getUserAdminId(), ActivityType.VIEW_LINK, viewSupportLinksActivity, SecurityUtils.getClientIp(),
								election.getElectionId());
					}
				});

				item.add(new ButtonUpdateToken("renewLinkButton", item.getIndex()) {
					private static final long serialVersionUID = 7073407309121152929L;

					@Override
					public void onConfirm() {
						try {
							AppContext.getInstance().getManagerBeanRemote().renewOrganizationNominationLink(election.getElectionId(), org.getOrgId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
							getSession().info(getString("organizationsManagementRenewLinkOk"));
							setResponsePage(ElectionOrganizationsDashboard.class, getPage().getPageParameters());
						} catch (CensusValidationException e) {
							getSession().error(getString("organizationsManagementOrgNotFound"));
						}
					}
				});

				removeOrganizationCell.add(new ButtonDeleteWithConfirmation("removeOrganizationButton", item.getIndex()) {
					private static final long serialVersionUID = 4708407474594461454L;

					@Override
					public void onConfirm() {
						try {
							deleteActionValidator.validateBeforeSubmit(election.getElectionId(), org.getOrgId());
							AppContext.getInstance().getManagerBeanRemote().removeOrganization(election.getElectionId(), org.getOrgId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
							getSession().info(getString("organizationsManagementDeleteOrgOk"));
							setResponsePage(ElectionOrganizationsDashboard.class, getPage().getPageParameters());
						} catch (CensusValidationException e) {
							if ("organizationsManagementDeleteOrgHasNominations".equals(e.getMessage()) || "organizationsManagementDeleteOrgHasSupports".equals(e.getMessage()) || "organizationsManagementOrgNotFound".equals(e.getMessage())) {
								getSession().error(new StringResourceModel(e.getMessage()).setParameters(org.getOrgId()).getString());
							} else {
								getSession().error(getString("organizationsManagementErrBif"));
							}
						}
					}
				});
				item.add(removeOrganizationCell);
			}
		};
		add(organizationsListView);

		organizationsPagerBottom = new PagingNavigator("organizationsPagerBottom", organizationsListView);
		organizationsPagerBottom.setVisible(filteredOrganizations.size() > ORGANIZATIONS_PAGE_SIZE);
		add(organizationsPagerBottom);
	}

	private void applyOrganizationsFilter() {
		boolean hasTextFilter = hasText(organizationsFilter);
		boolean hasCountryFilter = hasText(countryFilter);
		boolean hasVotesFilter = hasText(votesFilter);
		boolean hasMemberFilter = hasText(memberFilter);
		boolean hasDebtorFilter = hasText(debtorFilter);
		boolean hasLanguageFilter = hasText(languageFilter);
		if (!hasTextFilter && !hasCountryFilter && !hasVotesFilter && !hasMemberFilter && !hasDebtorFilter && !hasLanguageFilter) {
			filteredOrganizations = new ArrayList<>(allOrganizations);
		} else {
			String textFilterValue = hasTextFilter ? organizationsFilter.trim().toLowerCase(Locale.ROOT) : null;
			String normalizedCountryFilter = normalizeCodeFilter(countryFilter);
			String normalizedVotesFilter = hasVotesFilter ? votesFilter.trim() : null;
			String normalizedLanguageFilter = normalizeCodeFilter(languageFilter);
			List<Organization> matches = new ArrayList<>();
			for (Organization organization : allOrganizations) {
				if (organization == null) {
					continue;
				}
				if (!matchesTextFilter(organization, textFilterValue)) {
					continue;
				}
				if (!matchesCountryFilter(organization, normalizedCountryFilter)) {
					continue;
				}
				if (!matchesVotesFilter(organization, normalizedVotesFilter)) {
					continue;
				}
				if (!matchesBooleanFilter(organization.isMember(), memberFilter)) {
					continue;
				}
				if (!matchesBooleanFilter(organization.isDeudor(), debtorFilter)) {
					continue;
				}
				if (!matchesLanguageFilter(organization, normalizedLanguageFilter)) {
					continue;
				}
				{
					matches.add(organization);
				}
			}
			filteredOrganizations = matches;
		}
		if (organizationsListView != null) {
			organizationsListView.setList(filteredOrganizations);
		}
		if (organizationsPagerBottom != null) {
			organizationsPagerBottom.setVisible(filteredOrganizations.size() > ORGANIZATIONS_PAGE_SIZE);
		}
		if (organizationsEmpty != null) {
			organizationsEmpty.setVisible(filteredOrganizations.isEmpty());
		}
		updateSummaryCounters();
	}

	private void updateSummaryCounters() {
		int listed = 0;
		int nonDebtor = 0;
		int member = 0;
		int memberNonDebtor = 0;
		for (Organization organization : filteredOrganizations) {
			if (organization == null) {
				continue;
			}
			listed++;
			if (!organization.isDeudor()) {
				nonDebtor++;
			}
			if (organization.isMember()) {
				member++;
				if (!organization.isDeudor()) {
					memberNonDebtor++;
				}
			}
		}
		summaryListedCount = listed;
		summaryNonDebtorCount = nonDebtor;
		summaryMemberCount = member;
		summaryMemberNonDebtorCount = memberNonDebtor;
	}

	private void initializeFilterChoices() {
		Set<String> distinctLanguages = new LinkedHashSet<>();
		distinctLanguages.add("SP");
		distinctLanguages.add("EN");
		distinctLanguages.add("PT");
		for (Organization organization : allOrganizations) {
			if (organization == null) {
				continue;
			}
			String languageCode = normalizeCodeFilter(organization.getMembershipContactLanguage());
			if (hasText(languageCode)) {
				distinctLanguages.add(languageCode);
			}
		}

		List<String> votes = new ArrayList<>();
		for (int votesValue = VOTES_FILTER_MIN; votesValue <= VOTES_FILTER_MAX; votesValue++) {
			votes.add(String.valueOf(votesValue));
		}
		votesFilterChoices = votes;

		List<String> languages = new ArrayList<>();
		languages.addAll(distinctLanguages);
		languageFilterChoices = languages;
	}

	private boolean matchesTextFilter(Organization organization, String filterValue) {
		if (!hasText(filterValue)) {
			return true;
		}
		return containsIgnoreCase(organization.getOrgId(), filterValue) || containsIgnoreCase(organization.getName(), filterValue) || containsIgnoreCase(organization.getCnpj(), filterValue)
				|| containsIgnoreCase(organization.getAsn(), filterValue) || containsIgnoreCase(organization.getMembershipContactId(), filterValue)
				|| containsIgnoreCase(organization.getMembershipContactName(), filterValue);
	}

	private boolean matchesCountryFilter(Organization organization, String normalizedCountryFilter) {
		if (!hasText(normalizedCountryFilter)) {
			return true;
		}
		String normalizedOrgCountry = normalizeCodeFilter(COUNTRY_UTILS.normalizeCountryCode(organization.getCountry()));
		return normalizedCountryFilter.equals(normalizedOrgCountry);
	}

	private boolean matchesVotesFilter(Organization organization, String normalizedVotesFilter) {
		if (!hasText(normalizedVotesFilter)) {
			return true;
		}
		return normalizedVotesFilter.equals(organization.getVotes() == null ? null : String.valueOf(organization.getVotes()));
	}

	private boolean matchesLanguageFilter(Organization organization, String normalizedLanguageFilter) {
		if (!hasText(normalizedLanguageFilter)) {
			return true;
		}
		String normalizedOrgLanguage = normalizeCodeFilter(organization.getMembershipContactLanguage());
		return normalizedLanguageFilter.equals(normalizedOrgLanguage);
	}

	private boolean matchesBooleanFilter(boolean value, String filterValue) {
		if (!hasText(filterValue)) {
			return true;
		}
		return value == Boolean.parseBoolean(filterValue.trim());
	}

	private String normalizeCodeFilter(String value) {
		if (!hasText(value)) {
			return null;
		}
		String normalized = value.trim().toUpperCase(Locale.ROOT);
		return "AA".equals(normalized) ? null : normalized;
	}

	private boolean containsIgnoreCase(String source, String filterValue) {
		return source != null && source.toLowerCase(Locale.ROOT).contains(filterValue);
	}

	private IChoiceRenderer<String> createVotesChoiceRenderer() {
		return new IChoiceRenderer<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(String object) {
				return hasText(object) ? object : "";
			}

			@Override
			public String getIdValue(String object, int index) {
				return String.valueOf(index);
			}
		};
	}

	private IChoiceRenderer<String> createMemberChoiceRenderer() {
		return new IChoiceRenderer<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(String object) {
				if (!hasText(object)) {
					return "";
				}
				return Boolean.parseBoolean(object) ? getString("organizationsManagementMemberYes") : getString("organizationsManagementMemberNo");
			}

			@Override
			public String getIdValue(String object, int index) {
				return String.valueOf(index);
			}
		};
	}

	private IChoiceRenderer<String> createDebtorChoiceRenderer() {
		return new IChoiceRenderer<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(String object) {
				if (!hasText(object)) {
					return "";
				}
				return Boolean.parseBoolean(object) ? getString("organizationsManagementDebtorYes") : getString("organizationsManagementDebtorNo");
			}

			@Override
			public String getIdValue(String object, int index) {
				return String.valueOf(index);
			}
		};
	}

	private IChoiceRenderer<String> createLanguageChoiceRenderer() {
		return new IChoiceRenderer<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(String object) {
				if (!hasText(object)) {
					return "";
				}
				String label = resolveLanguageLabel(object);
				return hasText(label) ? label : object;
			}

			@Override
			public String getIdValue(String object, int index) {
				return String.valueOf(index);
			}
		};
	}

	public int getSummaryListedCount() {
		return summaryListedCount;
	}

	public int getSummaryNonDebtorCount() {
		return summaryNonDebtorCount;
	}

	public int getSummaryMemberCount() {
		return summaryMemberCount;
	}

	public int getSummaryMemberNonDebtorCount() {
		return summaryMemberNonDebtorCount;
	}

	private Map<Long, List<SupportNomination>> indexSupportNominationsBySupportingOrganization(List<SupportNomination> supportNominations) {
		if (supportNominations == null || supportNominations.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Long, List<SupportNomination>> index = new HashMap<>();
		for (SupportNomination supportNomination : supportNominations) {
			if (supportNomination == null || supportNomination.getSupportingOrganization() == null) {
				continue;
			}
			long supportingOrganizationId = supportNomination.getSupportingOrganization().getId();
			index.computeIfAbsent(supportingOrganizationId, key -> new ArrayList<>()).add(supportNomination);
		}
		return index;
	}

	private List<ButtonViewLinksList.ViewLinkItem> buildSupportLinkItems(List<SupportNomination> supportNominations) {
		if (supportNominations == null || supportNominations.isEmpty()) {
			return Collections.emptyList();
		}
		List<ButtonViewLinksList.ViewLinkItem> linkItems = new ArrayList<>();
		for (SupportNomination supportNomination : supportNominations) {
			if (supportNomination == null || !hasText(supportNomination.getSupportNominationLink())) {
				continue;
			}
			String label = "#" + supportNomination.getId() + " | " + buildRequestingOrganizationSummary(supportNomination.getNomination()) + " | " + buildRequestingCandidateSummary(supportNomination.getNomination());
			linkItems.add(new ButtonViewLinksList.ViewLinkItem(supportNomination.getSupportNominationLink(), label));
		}
		return linkItems;
	}

	private String buildRequestingOrganizationSummary(Nomination nomination) {
		if (nomination == null || nomination.getOrganization() == null) {
			return "-";
		}
		return valueOrDash(nomination.getOrganization().getOrgId()) + " - " + valueOrDash(nomination.getOrganization().getName());
	}

	private String buildRequestingCandidateSummary(Nomination nomination) {
		if (nomination == null) {
			return "-";
		}
		Candidate candidate = nomination.getCandidate();
		if (candidate != null) {
			return valueOrDash(candidate.getName()) + " (#" + candidate.getCandidateId() + ")";
		}
		return valueOrDash(nomination.getNominationName());
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private List<String> buildAsnRows(String asn) {
		if (!hasText(asn)) {
			return Collections.emptyList();
		}
		List<String> asnValues = new ArrayList<>();
		for (String token : asn.split("[,;\\s]+")) {
			if (hasText(token)) {
				asnValues.add(token.trim());
			}
		}
		if (asnValues.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> rows = new ArrayList<>();
		for (int i = 0; i < asnValues.size(); i += 3) {
			int end = Math.min(i + 3, asnValues.size());
			rows.add(String.join(", ", asnValues.subList(i, end)));
		}
		return rows;
	}

	private String resolveLanguageLabel(String languageCode) {
		if (languageCode == null || languageCode.trim().isEmpty()) {
			return "";
		}
		String normalized = languageCode.trim().toUpperCase(Locale.ROOT);
		String key = "organizationsManagementLanguageOption" + normalized;
		return getString(key, null, normalized);
	}

	private String resolveCountryCode(String code) {
		String normalizedCode = COUNTRY_UTILS.normalizeCountryCode(code);
		if (normalizedCode == null) {
			return "";
		}
		return COUNTRY_UTILS.getDisplayLabel(normalizedCode, getLocale(), true);
	}

	private String formatDateTime(Date date) {
		if (date == null) {
			return "-";
		}
		return new SimpleDateFormat(ORGANIZATIONS_DATE_TIME_PATTERN).format(date);
	}

	public File getOrganizationsUpsertFile() {
		return organizationsUpsertFile;
	}

	public void setOrganizationsUpsertFile(File organizationsUpsertFile) {
		this.organizationsUpsertFile = organizationsUpsertFile;
	}
}
