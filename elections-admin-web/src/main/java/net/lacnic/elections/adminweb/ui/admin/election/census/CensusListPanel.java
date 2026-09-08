package net.lacnic.elections.adminweb.ui.admin.election.census;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.ui.components.ButtonResendVoteEmail;
import net.lacnic.elections.adminweb.ui.components.ButtonUpdateToken;
import net.lacnic.elections.adminweb.ui.components.ButtonViewLink;
import net.lacnic.elections.adminweb.ui.components.DropDownCountry;
import net.lacnic.elections.adminweb.validators.CensusDeleteActionValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.UserVoterLite;
import net.lacnic.elections.exception.CensusValidationException;
import net.lacnic.elections.utils.CountryUtils;
import net.lacnic.elections.utils.LinksUtils;

public class CensusListPanel extends Panel {

	private static final long serialVersionUID = -1239455534678268981L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();
	private static final int CENSUS_PAGE_SIZE = 10;
	private static final int VOTES_FILTER_MIN = 0;
	private static final int VOTES_FILTER_MAX = 12;
	private static final String KEY_CENSUS_MANAGEMENT_USER_NOT_FOUND = "censusManagementUserNotFound";
	private final CensusDeleteActionValidator deleteActionValidator = new CensusDeleteActionValidator();

	private List<UserVoterLite> allUserVoters = Collections.emptyList();
	private List<UserVoterLite> filteredUserVoters = Collections.emptyList();
	private PageableListView<UserVoterLite> userVotersDataView;
	private PagingNavigator censusPagerBottom;
	private WebMarkupContainer censusEmpty;
	private File censusFile;
	private String censusFilter;
	private String countryFilter;
	private String votesFilter;
	private String languageFilter;
	private String votedFilter;
	private List<String> votesFilterChoices = Collections.emptyList();
	private List<String> languageFilterChoices = Collections.emptyList();
	private int summaryListedCount;
	private int summaryVotedCount;
	private int summaryPendingCount;
	private int summaryVoteAmountCount;

	public CensusListPanel(String id, Election election) {
		super(id);
		boolean censusProcessing = election.isManageVotersManual() && AppContext.getInstance().getManagerBeanRemote().isElectionCensusProcessing(election.getElectionId());
		if (censusProcessing) {
			setVisible(false);
			return;
		}
		allUserVoters = resolveAllUserVoters(election);
		initializeFilterChoices();
		applyCensusFilter();
		boolean manageVotersManual = election.isManageVotersManual();
		add(new Label("summaryListedCount", new PropertyModel<>(this, "summaryListedCount")));
		add(new Label("summaryVotedCount", new PropertyModel<>(this, "summaryVotedCount")));
		add(new Label("summaryPendingCount", new PropertyModel<>(this, "summaryPendingCount")));
		add(new Label("summaryVoteAmountCount", new PropertyModel<>(this, "summaryVoteAmountCount")));
		WebMarkupContainer deleteHeader = new WebMarkupContainer("deleteHeader");
		deleteHeader.setVisible(manageVotersManual);
		add(deleteHeader);
		WebMarkupContainer editHeader = new WebMarkupContainer("editHeader");
		editHeader.setVisible(manageVotersManual);
		add(editHeader);

		DownloadLink downloadLink = new DownloadLink("exportCensus", new PropertyModel<>(this, "censusFile")) {
			private static final long serialVersionUID = 5415706945162526592L;

			@Override
			public void onClick() {
				setCensusFile(AppContext.getInstance().getManagerBeanRemote().exportCensus(election.getElectionId()));
				super.onClick();
			}
		};
		add(downloadLink);
		downloadLink.setDeleteAfterDownload(true);
		downloadLink.setVisible(election.isElectorsSet());

		Form<Void> filterForm = new Form<>("filterForm");
		filterForm.add(new TextField<>("censusFilterInput", new PropertyModel<>(this, "censusFilter")));
		filterForm.add(new DropDownCountry("countryFilterInput", new PropertyModel<>(this, "countryFilter"), false));

		DropDownChoice<String> votesFilterInput = new DropDownChoice<>("votesFilterInput", new PropertyModel<>(this, "votesFilter"), new PropertyModel<>(this, "votesFilterChoices"),
				createVotesChoiceRenderer());
		votesFilterInput.setNullValid(true);
		filterForm.add(votesFilterInput);

		DropDownChoice<String> languageFilterInput = new DropDownChoice<>("languageFilterInput", new PropertyModel<>(this, "languageFilter"), new PropertyModel<>(this, "languageFilterChoices"),
				createLanguageChoiceRenderer());
		languageFilterInput.setNullValid(true);
		filterForm.add(languageFilterInput);

		DropDownChoice<String> votedFilterInput = new DropDownChoice<>("votedFilterInput", new PropertyModel<>(this, "votedFilter"), Arrays.asList("true", "false"), createVotedChoiceRenderer());
		votedFilterInput.setNullValid(true);
		filterForm.add(votedFilterInput);

		filterForm.add(new org.apache.wicket.markup.html.form.Button("applyFilterButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				applyCensusFilter();
				userVotersDataView.setCurrentPage(0);
			}
		});
		filterForm.add(new org.apache.wicket.markup.html.form.Button("clearFilterButton") {
			private static final long serialVersionUID = 1L;

			{
				setDefaultFormProcessing(false);
			}

			@Override
			public void onSubmit() {
				censusFilter = null;
				countryFilter = null;
				votesFilter = null;
				languageFilter = null;
				votedFilter = null;
				applyCensusFilter();
				userVotersDataView.setCurrentPage(0);
			}
		});
		add(filterForm);

		censusEmpty = new WebMarkupContainer("censusEmpty");
		censusEmpty.setVisible(filteredUserVoters.isEmpty());
		add(censusEmpty);

		userVotersDataView = new PageableListView<UserVoterLite>("userVotersList", filteredUserVoters, CENSUS_PAGE_SIZE) {
			private static final long serialVersionUID = 1786359392545666490L;

			@Override
			protected void populateItem(final ListItem<UserVoterLite> item) {
				try {
					final UserVoterLite currentUser = item.getModelObject();
					WebMarkupContainer removeUserCell = new WebMarkupContainer("removeUserCell");
					removeUserCell.setVisible(manageVotersManual);

					ButtonDeleteWithConfirmation buttonDeleteWithConfirmation = new ButtonDeleteWithConfirmation("removeUser", currentUser.getUserVoterId()) {
						private static final long serialVersionUID = -6583106894827434879L;

						@Override
						public void onConfirm() {
							try {
								deleteActionValidator.validateBeforeSubmit(election.getElectionId(), currentUser.getUserVoterId());
								UserVoter userVoterToRemove = AppContext.getInstance().getManagerBeanRemote().getUserVoter(currentUser.getUserVoterId());
								if (userVoterToRemove == null) {
									getSession().error(getString(KEY_CENSUS_MANAGEMENT_USER_NOT_FOUND));
									return;
								}
								AppContext.getInstance().getManagerBeanRemote().removeUserVoter(userVoterToRemove, election.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
								getSession().info(getString("censusManagementUserListDeleteSuccess"));
								setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
							} catch (CensusValidationException e) {
								if ("censusManagementDeleteBlockedByVotes".equals(e.getMessage())) {
									getSession().error(new StringResourceModel("censusManagementDeleteBlockedByVotes").setParameters(e.getErrorInfo()).getString());
								} else if (KEY_CENSUS_MANAGEMENT_USER_NOT_FOUND.equals(e.getMessage())) {
									getSession().error(getString(KEY_CENSUS_MANAGEMENT_USER_NOT_FOUND));
								} else {
									getSession().error(getString("censusManagementErrBif"));
								}
							} catch (Exception e) {
								appLogger.error(e.getMessage(), e);
							}
						}
					};
					buttonDeleteWithConfirmation.setMarkupId("removeUser" + item.getIndex());
					removeUserCell.add(buttonDeleteWithConfirmation);
					item.add(removeUserCell);

					item.add(new Label("voted", currentUser.isVoted() ? getString("censusManagementUserListColVotedYes") : getString("censusManagementUserListColVotedNo")));
					item.add(new Label("language", resolveLanguageLabel(currentUser.getLanguage())));
					item.add(new Label("name", currentUser.getName()));
					item.add(new Label("mail", currentUser.getMail()));
					item.add(new Label("voteAmount", String.valueOf(currentUser.getVoteAmount())));
					item.add(new Label("country", resolveCountryLabel(currentUser.getCountry())));
					item.add(new Label("orgId", hasText(currentUser.getOrgID()) ? currentUser.getOrgID() : "-"));
					item.add(new Label("orgName", hasText(currentUser.getOrgName()) ? currentUser.getOrgName() : "-"));

					String voteLinkText = LinksUtils.buildVoteLink(currentUser.getVoteToken());
					String userAdminId = SecurityUtils.getUserAdminId();
					String activityDescription = userAdminId.toUpperCase() + " vió el link de votación del votante " + currentUser.getName() + " en la elección " + election.getTitleSpanish();
					ButtonViewLink viewLinkButton = new ButtonViewLink("viewLinkButton", currentUser.getUserVoterId(), voteLinkText) {
						private static final long serialVersionUID = 3666243113529801997L;

						@Override
						public void registerActivity() {
							AppContext.getInstance().getManagerBeanRemote().persistActivity(SecurityUtils.getUserAdminId(), ActivityType.VIEW_LINK, activityDescription, SecurityUtils.getClientIp(), election.getElectionId());
						}
					};
					item.add(viewLinkButton);

					ButtonUpdateToken buttonUpdateToken = new ButtonUpdateToken("updateToken", currentUser.getUserVoterId()) {
						private static final long serialVersionUID = 3609140813722818708L;

						@Override
						public void onConfirm() {
							try {
								AppContext.getInstance().getManagerBeanRemote().updateUserVoterToken(currentUser.getUserVoterId(), currentUser.getName(), election.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
								getSession().info(getString("censusManagementUserListTokenSuccess"));
								setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
							} catch (Exception e) {
								appLogger.error(e.getMessage(), e);
							}
						}
					};
					item.add(buttonUpdateToken);

					ButtonResendVoteEmail buttonResendVoteEmail = new ButtonResendVoteEmail("resendLink", currentUser.getUserVoterId()) {
						private static final long serialVersionUID = -4628772989608517427L;

						@Override
						public void onConfirm() {
							try {
								UserVoter userVoterToResend = AppContext.getInstance().getManagerBeanRemote().getUserVoter(currentUser.getUserVoterId());
								if (userVoterToResend == null) {
									getSession().error(getString(KEY_CENSUS_MANAGEMENT_USER_NOT_FOUND));
									return;
								}
								AppContext.getInstance().getManagerBeanRemote().resendUserVoterElectionMail(userVoterToResend, election, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
								getSession().info(getString("censusManagementUserListLinkSuccess"));
								setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
							} catch (Exception e) {
								appLogger.error(e.getMessage(), e);
							}
						}
					};
					item.add(buttonResendVoteEmail);

					WebMarkupContainer editUserVoterCell = new WebMarkupContainer("editUserVoterCell");
					editUserVoterCell.setVisible(manageVotersManual);
					Link<Void> editUserVoterLink = new Link<Void>("editUserVoter") {
						private static final long serialVersionUID = 8268292966477896858L;

						@Override
						public void onClick() {
							setResponsePage(EditUserVoterDashboard.class, UtilsParameters.getUser(currentUser.getUserVoterId()));
						}
					};
					editUserVoterLink.setMarkupId("editUserVoter." + currentUser.getUserVoterId());
					editUserVoterCell.add(editUserVoterLink);
					item.add(editUserVoterCell);
				} catch (Exception e) {
					appLogger.error(e.getMessage(), e);
				}
			}
		};
		add(userVotersDataView);

		censusPagerBottom = new PagingNavigator("censusPagerBottom", userVotersDataView);
		censusPagerBottom.setVisible(filteredUserVoters.size() > CENSUS_PAGE_SIZE);
		add(censusPagerBottom);
	}

	private List<UserVoterLite> resolveAllUserVoters(Election election) {
		List<UserVoterLite> userVoters = AppContext.getInstance().getManagerBeanRemote().getElectionUserVotersLite(election.getElectionId());
		return userVoters == null ? Collections.emptyList() : userVoters;
	}

	private void initializeFilterChoices() {
		Set<String> distinctLanguages = new LinkedHashSet<>();
		distinctLanguages.add("SP");
		distinctLanguages.add("EN");
		distinctLanguages.add("PT");
		for (UserVoterLite userVoter : allUserVoters) {
			if (userVoter == null) {
				continue;
			}
			String languageCode = normalizeCodeFilter(userVoter.getLanguage());
			if (hasText(languageCode)) {
				distinctLanguages.add(languageCode);
			}
		}

		List<String> votes = new ArrayList<>();
		for (int votesValue = VOTES_FILTER_MIN; votesValue <= VOTES_FILTER_MAX; votesValue++) {
			votes.add(String.valueOf(votesValue));
		}
		votesFilterChoices = votes;
		languageFilterChoices = new ArrayList<>(distinctLanguages);
	}

	private void applyCensusFilter() {
		boolean hasTextFilter = hasText(censusFilter);
		boolean hasCountryFilter = hasText(countryFilter);
		boolean hasVotesFilter = hasText(votesFilter);
		boolean hasLanguageFilter = hasText(languageFilter);
		boolean hasVotedFilter = hasText(votedFilter);

		if (!hasTextFilter && !hasCountryFilter && !hasVotesFilter && !hasLanguageFilter && !hasVotedFilter) {
			filteredUserVoters = new ArrayList<>(allUserVoters);
		} else {
			String textFilterValue = hasTextFilter ? censusFilter.trim().toLowerCase(Locale.ROOT) : null;
			String normalizedCountryFilter = normalizeCodeFilter(countryFilter);
			String normalizedVotesFilter = hasVotesFilter ? votesFilter.trim() : null;
			String normalizedLanguageFilter = normalizeCodeFilter(languageFilter);
			List<UserVoterLite> matches = new ArrayList<>();
			for (UserVoterLite userVoter : allUserVoters) {
				if (userVoter == null) {
					continue;
				}
				if (!matchesTextFilter(userVoter, textFilterValue)) {
					continue;
				}
				if (!matchesCountryFilter(userVoter, normalizedCountryFilter)) {
					continue;
				}
				if (!matchesVotesFilter(userVoter, normalizedVotesFilter)) {
					continue;
				}
				if (!matchesLanguageFilter(userVoter, normalizedLanguageFilter)) {
					continue;
				}
				if (!matchesBooleanFilter(userVoter.isVoted(), votedFilter)) {
					continue;
				}
				matches.add(userVoter);
			}
			filteredUserVoters = matches;
		}

		if (userVotersDataView != null) {
			userVotersDataView.setList(filteredUserVoters);
		}
		if (censusPagerBottom != null) {
			censusPagerBottom.setVisible(filteredUserVoters.size() > CENSUS_PAGE_SIZE);
		}
		if (censusEmpty != null) {
			censusEmpty.setVisible(filteredUserVoters.isEmpty());
		}
		updateSummaryCounters();
	}

	private void updateSummaryCounters() {
		int listed = 0;
		int voted = 0;
		int pending = 0;
		int voteAmount = 0;
		for (UserVoterLite userVoter : filteredUserVoters) {
			if (userVoter == null) {
				continue;
			}
			listed++;
			if (userVoter.isVoted()) {
				voted++;
			} else {
				pending++;
			}
			voteAmount += userVoter.getVoteAmount() == null ? 0 : userVoter.getVoteAmount().intValue();
		}
		summaryListedCount = listed;
		summaryVotedCount = voted;
		summaryPendingCount = pending;
		summaryVoteAmountCount = voteAmount;
	}

	private boolean matchesTextFilter(UserVoterLite userVoter, String filterValue) {
		if (!hasText(filterValue)) {
			return true;
		}
		return containsIgnoreCase(userVoter.getOrgID(), filterValue)
				|| containsIgnoreCase(userVoter.getOrgName(), filterValue)
				|| containsIgnoreCase(userVoter.getName(), filterValue)
				|| containsIgnoreCase(userVoter.getMail(), filterValue);
	}

	private boolean matchesCountryFilter(UserVoterLite userVoter, String normalizedCountryFilter) {
		if (!hasText(normalizedCountryFilter)) {
			return true;
		}
		String normalizedUserCountry = normalizeCodeFilter(COUNTRY_UTILS.normalizeCountryCode(userVoter.getCountry()));
		return normalizedCountryFilter.equals(normalizedUserCountry);
	}

	private boolean matchesVotesFilter(UserVoterLite userVoter, String normalizedVotesFilter) {
		if (!hasText(normalizedVotesFilter)) {
			return true;
		}
		return normalizedVotesFilter.equals(userVoter.getVoteAmount() == null ? null : String.valueOf(userVoter.getVoteAmount()));
	}

	private boolean matchesLanguageFilter(UserVoterLite userVoter, String normalizedLanguageFilter) {
		if (!hasText(normalizedLanguageFilter)) {
			return true;
		}
		String normalizedUserLanguage = normalizeCodeFilter(userVoter.getLanguage());
		return normalizedLanguageFilter.equals(normalizedUserLanguage);
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

	private IChoiceRenderer<String> createVotedChoiceRenderer() {
		return new IChoiceRenderer<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(String object) {
				if (!hasText(object)) {
					return "";
				}
				return Boolean.parseBoolean(object) ? getString("censusManagementUserListColVotedYes") : getString("censusManagementUserListColVotedNo");
			}

			@Override
			public String getIdValue(String object, int index) {
				return String.valueOf(index);
			}
		};
	}

	private String resolveCountryLabel(String countryCode) {
		String normalizedCode = COUNTRY_UTILS.normalizeCountryCode(countryCode);
		if (normalizedCode == null) {
			return "";
		}
		return COUNTRY_UTILS.getDisplayLabel(normalizedCode, getLocale(), true);
	}

	private String resolveLanguageLabel(String languageCode) {
		if (!hasText(languageCode)) {
			return "";
		}
		String normalized = languageCode.trim().toUpperCase(Locale.ROOT);
		switch (normalized) {
		case "SP":
		case "ES":
			return getString("publicMenuLanguageSpanish");
		case "EN":
			return getString("publicMenuLanguageEnglish");
		case "PT":
			return getString("publicMenuLanguagePortuguese");
		default:
			return normalized;
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	public int getSummaryListedCount() {
		return summaryListedCount;
	}

	public int getSummaryVotedCount() {
		return summaryVotedCount;
	}

	public int getSummaryPendingCount() {
		return summaryPendingCount;
	}

	public int getSummaryVoteAmountCount() {
		return summaryVoteAmountCount;
	}

	public File getCensusFile() {
		return censusFile;
	}

	public void setCensusFile(File censusFile) {
		this.censusFile = censusFile;
	}
}
