package net.lacnic.elections.adminweb.ui.admin.election.detail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionCategoryChoiceRenderer;
import net.lacnic.elections.adminweb.validators.AuthorizedUserEmailsValidator;
import net.lacnic.elections.campus.CampusClient;
import net.lacnic.elections.campus.Course;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.ElectionLinkRecoveryMode;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.utils.CountryUtils;

public class ElectionDetailPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	public ElectionDetailPanel(String id, Election election) {
		super(id);
		try {
			setOutputMarkupId(true);

			WebMarkupContainer electionDescriptions = new WebMarkupContainer("electionDescriptions");
			electionDescriptions.setOutputMarkupPlaceholderTag(true);
			electionDescriptions.setVisible(!election.isOnlySp());
			add(electionDescriptions);

			WebMarkupContainer electionTitles = new WebMarkupContainer("electionTitles");
			electionTitles.setOutputMarkupPlaceholderTag(true);
			electionTitles.setVisible(!election.isOnlySp());
			add(electionTitles);

			WebMarkupContainer electionUrls = new WebMarkupContainer("electionUrls");
			electionUrls.setOutputMarkupPlaceholderTag(true);
			electionUrls.setVisible(!election.isOnlySp());
			add(electionUrls);

			AjaxCheckBox onlySp = new AjaxCheckBox("onlySpCheckbox", new PropertyModel<>(election, "onlySp")) {
				private static final long serialVersionUID = 7324854310046050015L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					electionDescriptions.setVisible(!election.isOnlySp());
					electionTitles.setVisible(!election.isOnlySp());
					electionUrls.setVisible(!election.isOnlySp());
					target.add(electionDescriptions);
					target.add(electionTitles);
					target.add(electionUrls);
				}
			};
			add(onlySp);

			DropDownChoice<ElectionCategory> selectCategory = new DropDownChoice<>("selectCategory", new PropertyModel<>(election, "category"),
					SecurityUtils.getVisibleElectionCategories(election), new ElectionCategoryChoiceRenderer(this));
			selectCategory.setRequired(true);
			add(selectCategory);

			CheckBox manageVotersManual = new CheckBox("manageVotersManual", new PropertyModel<>(election, "manageVotersManual"));
			add(manageVotersManual);

			CheckBox manageOrganizationsManual = new CheckBox("manageOrganizationsManual", new PropertyModel<>(election, "manageOrganizationsManual"));
			add(manageOrganizationsManual);

			boolean campusConfigured = CampusClient.isCampusIntegrationEnabled();
			List<Course> courses = initCourse();
			Map<Long, String> campusCourseIdToName = buildCampusCourseLabelMap(courses);
			addCampusCourseChoice("campusCourse", election, "campusCourse", campusCourseIdToName).setVisible(campusConfigured);
			addCampusCourseChoice("campusCourseEnglish", election, "campusCourseEnglish", campusCourseIdToName).setVisible(campusConfigured);
			addCampusCourseChoice("campusCoursePortuguese", election, "campusCoursePortuguese", campusCourseIdToName).setVisible(campusConfigured);

			String campusUrl = CampusClient.getCampusUrl();
			campusUrl = campusUrl == null ? "" : campusUrl.trim();
			if (campusUrl.isEmpty()) {
				campusUrl = "-";
			}
			add(new Label("campusCourseHelp", new StringResourceModel("electionManagementCampusCourseHelp", this, null).setParameters(campusUrl).getString()).setVisible(campusConfigured));

				DropDownChoice<ElectionType> electionType = new DropDownChoice<>("electionType", new PropertyModel<>(election, "electionType"), Arrays.asList(ElectionType.values()), new IChoiceRenderer<ElectionType>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Object getDisplayValue(ElectionType object) {
					if (object == null) {
						return "";
					}
					return getString("electionDeclarationsScope." + object.name());
				}

				@Override
				public String getIdValue(ElectionType object, int index) {
					return object == null ? null : object.name();
				}
			});
				electionType.setNullValid(true);
				electionType.setRequired(false);
				add(electionType);

				DropDownChoice<ElectionLinkRecoveryMode> publicLinkRecoveryMode = new DropDownChoice<>("publicLinkRecoveryMode",
						new PropertyModel<>(election, "publicLinkRecoveryMode"),
						Arrays.asList(ElectionLinkRecoveryMode.values()),
						new IChoiceRenderer<ElectionLinkRecoveryMode>() {
							private static final long serialVersionUID = 1L;

							@Override
							public Object getDisplayValue(ElectionLinkRecoveryMode object) {
								return object == null ? "" : getString("electionLinkRecoveryMode." + object.name());
							}

							@Override
							public String getIdValue(ElectionLinkRecoveryMode object, int index) {
								return object == null ? null : object.name();
							}
						});
				publicLinkRecoveryMode.setNullValid(false);
				publicLinkRecoveryMode.setRequired(true);
				add(publicLinkRecoveryMode);

			WebMarkupContainer restrictedCountryContainer = new WebMarkupContainer("restrictedCountryContainer");
			restrictedCountryContainer.setOutputMarkupId(true);
			add(restrictedCountryContainer);

			CountryUtils countryUtils = new CountryUtils();
			DropDownChoice<String> restrictedCountry = new DropDownChoice<>("restrictedCountry", new PropertyModel<>(election, "restrictedCountrySelection"), countryUtils.getIdsListLacnicFirst(false), new IChoiceRenderer<String>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Object getDisplayValue(String object) {
					if (object == null) {
						return "";
					}
					return resolveCountryLabel(object, countryUtils);
				}

				@Override
				public String getIdValue(String object, int index) {
					return object;
				}
			});
			restrictedCountry.setNullValid(true);
			restrictedCountry.setRequired(false);

			restrictedCountry.add(new AjaxFormComponentUpdatingBehavior("change") {
				private static final long serialVersionUID = 4302080897730187800L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					String selection = election.getRestrictedCountrySelection();

					if (selection != null && !selection.isEmpty()) {
						List<String> currentCodes = election.getRestrictedCountryCodes();
						if (!currentCodes.contains(selection)) {
							currentCodes.add(selection);
						}
					}
					election.setRestrictedCountrySelection(null);
					target.add(restrictedCountryContainer);
				}
			});
			restrictedCountryContainer.add(restrictedCountry);

			ListView<String> restrictedCountries = new ListView<String>("restrictedCountries", new PropertyModel<>(election, "restrictedCountryCodes")) {
				private static final long serialVersionUID = -8307756509962801824L;

				@Override
				protected void populateItem(ListItem<String> item) {
					String code = item.getModelObject();

					AjaxLink<Void> removeLink = new AjaxLink<Void>("removeRestrictedCountry") {
						private static final long serialVersionUID = 7295392860607396471L;

						@Override
						public void onClick(AjaxRequestTarget target) {
							election.getRestrictedCountryCodes().remove(code);
							target.add(restrictedCountryContainer);
						}
					};
					item.add(removeLink);
					removeLink.add(new Label("restrictedCountryLabel", resolveCountryLabel(code, countryUtils)));
				}
			};
			restrictedCountryContainer.add(restrictedCountries);

			TextField<String> titleSpanish = new TextField<>("titleSpanish", new PropertyModel<>(election, "titleSpanish"));
			titleSpanish.add(StringValidator.maximumLength(1000));
			titleSpanish.setRequired(true);
			add(titleSpanish);

			TextField<String> titleEnglish = new TextField<>("titleEnglish", new PropertyModel<>(election, "titleEnglish"));
			titleEnglish.add(StringValidator.maximumLength(1000));
			titleEnglish.setRequired(true);
			electionTitles.add(titleEnglish);

			TextField<String> titlePortuguese = new TextField<>("titlePortuguese", new PropertyModel<>(election, "titlePortuguese"));
			titlePortuguese.add(StringValidator.maximumLength(1000));
			titlePortuguese.setRequired(true);
			electionTitles.add(titlePortuguese);

			TextArea<String> descriptionSpanish = new TextArea<>("descriptionSpanish", new PropertyModel<>(election, "descriptionSpanish"));
			descriptionSpanish.add(StringValidator.maximumLength(2000));
			descriptionSpanish.setRequired(true);
			add(descriptionSpanish);

			TextArea<String> descriptionEnglish = new TextArea<>("descriptionEnglish", new PropertyModel<>(election, "descriptionEnglish"));
			descriptionEnglish.add(StringValidator.maximumLength(2000));
			descriptionEnglish.setRequired(true);
			electionDescriptions.add(descriptionEnglish);

			TextArea<String> descriptionPortuguese = new TextArea<>("descriptionPortuguese", new PropertyModel<>(election, "descriptionPortuguese"));
			descriptionPortuguese.add(StringValidator.maximumLength(2000));
			descriptionPortuguese.setRequired(true);
			electionDescriptions.add(descriptionPortuguese);

			TextField<String> linkSpanish = new TextField<>("linkSpanish", new PropertyModel<>(election, "linkSpanish"));
			linkSpanish.add(StringValidator.maximumLength(1000));
			linkSpanish.add(new UrlValidator());
			linkSpanish.setRequired(true);
			add(linkSpanish);

			TextField<String> linkEnglish = new TextField<>("linkEnglish", new PropertyModel<>(election, "linkEnglish"));
			linkEnglish.add(StringValidator.maximumLength(1000));
			linkEnglish.add(new UrlValidator());
			linkEnglish.setRequired(true);
			electionUrls.add(linkEnglish);

			TextField<String> linkPortuguese = new TextField<>("linkPortuguese", new PropertyModel<>(election, "linkPortuguese"));
			linkPortuguese.add(StringValidator.maximumLength(1000));
			linkPortuguese.add(new UrlValidator());
			linkPortuguese.setRequired(true);
			electionUrls.add(linkPortuguese);

			TextField<Integer> maxCandidates = new TextField<>("maxCandidates", new PropertyModel<>(election, "maxCandidates"));
			maxCandidates.setRequired(true);
			maxCandidates.add(RangeValidator.range(1, 100));
			add(maxCandidates);

			TextField<Integer> diffUTC = new TextField<>("diffUTC", new PropertyModel<>(election, "diffUTC"));
			diffUTC.setRequired(true);
			diffUTC.add(RangeValidator.range(0, 12));
			add(diffUTC);

			EmailTextField defaultSender = new EmailTextField("defaultSender", new PropertyModel<>(election, "defaultSender"));
			defaultSender.setRequired(true);
			add(defaultSender);

			EmailTextField defaultRecipient = new EmailTextField("defaultRecipient", new PropertyModel<>(election, "defaultRecipient"));
			defaultRecipient.setRequired(true);
			add(defaultRecipient);

			TextArea<String> authorizedUserEmails = new TextArea<>("authorizedUserEmails", new PropertyModel<>(election, "authorizedUserEmails"));
			authorizedUserEmails.add(new AuthorizedUserEmailsValidator());
			authorizedUserEmails.setRequired(false);
			add(authorizedUserEmails);

			TextArea<String> authorizedSupportEmails = new TextArea<>("authorizedSupportEmails", new PropertyModel<>(election, "authorizedSupportEmails"));
			authorizedSupportEmails.add(new AuthorizedUserEmailsValidator());
			authorizedSupportEmails.setRequired(false);
			add(authorizedSupportEmails);

			TextArea<String> authorizedNominateEmails = new TextArea<>("authorizedNominateEmails", new PropertyModel<>(election, "authorizedNominateEmails"));
			authorizedNominateEmails.add(new AuthorizedUserEmailsValidator());
			authorizedNominateEmails.setRequired(false);
			add(authorizedNominateEmails);

			TextField<String> publicElectionToken = new TextField<>("publicElectionToken", new PropertyModel<>(election, "publicElectionToken"));
			publicElectionToken.setRequired(false);
			publicElectionToken.add(StringValidator.maximumLength(1000));
			add(publicElectionToken);

		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

	private String resolveCountryLabel(String countryCode, CountryUtils countryUtils) {
		String normalizedCode = countryUtils.normalizeCountryCode(countryCode);
		if (normalizedCode == null) {
			return "";
		}
		return countryUtils.getDisplayLabel(normalizedCode, getLocale(), true);
	}

	private DropDownChoice<Long> addCampusCourseChoice(String componentId, Election election, String propertyName, Map<Long, String> campusCourseIdToName) {
		DropDownChoice<Long> campusCourse = new DropDownChoice<>(componentId, new PropertyModel<>(election, propertyName), new ArrayList<>(campusCourseIdToName.keySet()), new IChoiceRenderer<Long>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(Long object) {
				if (object == null) {
					return "";
				}
				return campusCourseIdToName.getOrDefault(object, String.valueOf(object));
			}

			@Override
			public String getIdValue(Long object, int index) {
				return object == null ? null : String.valueOf(object);
			}
		});
		campusCourse.setNullValid(true);
		campusCourse.setRequired(false);
		add(campusCourse);
		return campusCourse;
	}

	private Map<Long, String> buildCampusCourseLabelMap(List<Course> courses) {
		Map<Long, String> labels = new LinkedHashMap<>();
		for (Course course : courses) {
			if (course == null || course.getId() == null) {
				continue;
			}
			labels.put(course.getId(), course.getFullDescription());
		}
		return labels;
	}

	private List<Course> initCourse() {
		List<Course> cursos = CampusClient.getCourses();

		if (cursos == null) {
			return new ArrayList<>();
		}
		return cursos;
	}

}
