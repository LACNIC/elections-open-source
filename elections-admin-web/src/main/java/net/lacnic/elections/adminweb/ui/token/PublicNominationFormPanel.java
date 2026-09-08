package net.lacnic.elections.adminweb.ui.token;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import java.util.regex.Pattern;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.ElectionsWebAdminSession;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.ui.components.DropDownCountry;
import net.lacnic.elections.adminweb.wicket.util.RestrictedCountriesMessageResolver;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.domain.pre.PublicNominationSubmission;
import net.lacnic.elections.domain.pre.PublicNominationSubmissionResult;
import net.lacnic.elections.utils.AuthorizedEmailListUtils;
import net.lacnic.elections.utils.CountryUtils;

public class PublicNominationFormPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();
	private static final int MAX_COUNTRY_CODE_LENGTH = 5;
	private static final int MIN_PHONE_LENGTH = 5;
	private static final int MAX_PHONE_LENGTH = 50;
	private static final int MIN_REASON_LENGTH = 20;
	private static final int MAX_REASON_LENGTH = 2000;
	private static final Pattern FULL_NAME_PATTERN = Pattern.compile("^[\\p{L}][\\p{L}\\p{M} .,'-]*$");
	private static final String RESTRICTED_COUNTRIES_MODAL_ID = "public-restricted-countries-modal";

	private final String publicElectionToken;
	private final ElectionType electionType;
	private final List<String> restrictedCountryCodes = new ArrayList<>();
	private final List<RestrictedCountryItem> restrictedCountries = new ArrayList<>();
	private String nominatorName;
	private String nominatorEmail;
	private String organizationName;
	private String organizationCountry;
	private Boolean selfNomination;
	private String nomineeName;
	private String nomineeEmail;
	private String nomineePhoneCountryCode;
	private String nomineePhoneLocal;
	private String nominationReason;

	public PublicNominationFormPanel(String id, Election election, String publicElectionToken) {
		super(id);
		this.publicElectionToken = publicElectionToken;
		this.electionType = election != null ? election.getEffectiveElectionType() : null;
		loadRestrictedCountries(election != null ? election.getRestrictedCountryCodes() : null);
		final String dataSiteKey = AppContext.getInstance().getManagerBeanRemote().getDataSiteKey();
		final boolean captchaEnabled = AppContext.getInstance().getManagerBeanRemote().isShowCaptcha() && StringUtils.isNotBlank(dataSiteKey);

		Form<Void> nominationForm = new Form<>("nominationForm");
		add(nominationForm);
		final String nominationRecipient = resolveNominationRecipient(election);
		final String authorizedNominateEmails = election != null ? election.getAuthorizedNominateEmails() : null;

		TextField<String> nominatorNameField = new TextField<>("nominatorName", new PropertyModel<>(this, "nominatorName"));
		nominatorNameField.setRequired(true);
		nominatorNameField.setConvertEmptyInputStringToNull(true);
		nominatorNameField.setLabel(new ResourceModel("publicNominationNominatorNameLabel"));
		nominatorNameField.add(StringValidator.maximumLength(1000));
		nominatorNameField.add(minLengthValidator(2, "publicNominationNominatorNameTooShort"));
		nominatorNameField.add(regexValidator(FULL_NAME_PATTERN, "publicNominationNominatorNameInvalid"));
		nominationForm.add(nominatorNameField);

		EmailTextField nominatorEmailField = new EmailTextField("nominatorEmail", new PropertyModel<>(this, "nominatorEmail"));
		nominatorEmailField.setRequired(true);
		nominatorEmailField.setConvertEmptyInputStringToNull(true);
		nominatorEmailField.setLabel(new ResourceModel("publicNominationNominatorEmailLabel"));
		nominatorEmailField.add(StringValidator.maximumLength(255));
		nominationForm.add(nominatorEmailField);

		TextField<String> organizationNameField = new TextField<>("organizationName", new PropertyModel<>(this, "organizationName"));
		organizationNameField.setRequired(true);
		organizationNameField.setConvertEmptyInputStringToNull(true);
		organizationNameField.setLabel(new ResourceModel("publicNominationOrganizationNameLabel"));
		organizationNameField.add(StringValidator.maximumLength(1000));
		organizationNameField.add(minLengthValidator(2, "publicNominationOrganizationNameTooShort"));
		nominationForm.add(organizationNameField);

		DropDownCountry organizationCountryField = new DropDownCountry("organizationCountry", new PropertyModel<>(this, "organizationCountry"), true);
		organizationCountryField.setRequired(true);
		organizationCountryField.setLabel(new ResourceModel("publicNominationOrganizationCountryLabel"));
		organizationCountryField.add(countryValidator("publicNominationOrganizationCountryInvalid"));
		nominationForm.add(organizationCountryField);

		RadioGroup<Boolean> nominationTargetGroup = new RadioGroup<>("nominationTargetGroup", new PropertyModel<>(this, "selfNomination"));
		nominationTargetGroup.setRequired(true);
		nominationTargetGroup.setLabel(new ResourceModel("publicNominationTargetQuestionLabel"));
		nominationTargetGroup.add(new Radio<>("selfNominationOption", Model.of(Boolean.TRUE)));
		nominationTargetGroup.add(new Radio<>("thirdPartyNominationOption", Model.of(Boolean.FALSE)));
		nominationForm.add(nominationTargetGroup);

		WebMarkupContainer nominationDetailsContainer = new WebMarkupContainer("nominationDetailsContainer");
		nominationDetailsContainer.setOutputMarkupId(true);
		nominationDetailsContainer.setOutputMarkupPlaceholderTag(true);
		nominationDetailsContainer.setVisible(hasNominationTargetSelection());
		nominationForm.add(nominationDetailsContainer);

		WebMarkupContainer nominationThirdPartyHeader = new WebMarkupContainer("nominationThirdPartyHeader");
		nominationThirdPartyHeader.setOutputMarkupPlaceholderTag(true);
		nominationThirdPartyHeader.setVisible(isThirdPartyNominationSelected());
		nominationDetailsContainer.add(nominationThirdPartyHeader);

		WebMarkupContainer nomineeIdentityContainer = new WebMarkupContainer("nomineeIdentityContainer");
		nomineeIdentityContainer.setOutputMarkupPlaceholderTag(true);
		nomineeIdentityContainer.setVisible(isThirdPartyNominationSelected());
		nominationDetailsContainer.add(nomineeIdentityContainer);

		TextField<String> nomineeNameField = new TextField<>("nomineeName", new PropertyModel<>(this, "nomineeName"));
		nomineeNameField.setRequired(true);
		nomineeNameField.setConvertEmptyInputStringToNull(true);
		nomineeNameField.setLabel(new ResourceModel("nominationCandidateFullNameLabel"));
		nomineeNameField.add(StringValidator.maximumLength(255));
		nomineeNameField.add(minLengthValidator(3, "nominationCandidateFullNameTooShort"));
		nomineeNameField.add(regexValidator(FULL_NAME_PATTERN, "nominationCandidateFullNameInvalid"));
		nomineeIdentityContainer.add(nomineeNameField);

		EmailTextField nomineeEmailField = new EmailTextField("nomineeEmail", new PropertyModel<>(this, "nomineeEmail"));
		nomineeEmailField.setRequired(true);
		nomineeEmailField.setConvertEmptyInputStringToNull(true);
		nomineeEmailField.setLabel(new ResourceModel("nominationCandidateEmailLabel"));
		nomineeEmailField.add(StringValidator.maximumLength(255));
		nomineeIdentityContainer.add(nomineeEmailField);

		nominationDetailsContainer.add(new Label("nomineePhoneLabel", new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return getString(isThirdPartyNominationSelected() ? "publicNominationNomineePhoneLabel" : "publicNominationSelfPhoneLabel");
			}
		}));

		TextField<String> nomineePhoneCountryField = new TextField<>("nomineePhoneCountry", new PropertyModel<>(this, "nomineePhoneCountryCode"));
		nomineePhoneCountryField.setRequired(true);
		nomineePhoneCountryField.setConvertEmptyInputStringToNull(true);
		nomineePhoneCountryField.setLabel(new ResourceModel("nominationCandidatePhoneLabel"));
		nomineePhoneCountryField.add(maxLengthValidator(MAX_COUNTRY_CODE_LENGTH, "nominationCandidatePhoneCountryInvalid"));
		nominationDetailsContainer.add(nomineePhoneCountryField);

		TextField<String> nomineePhoneField = new TextField<>("nomineePhone", new PropertyModel<>(this, "nomineePhoneLocal"));
		nomineePhoneField.setRequired(true);
		nomineePhoneField.setConvertEmptyInputStringToNull(true);
		nomineePhoneField.setLabel(new ResourceModel("nominationCandidatePhoneLabel"));
		nomineePhoneField.add(minLengthValidator(MIN_PHONE_LENGTH, "nominationCandidatePhoneInvalid"));
		nomineePhoneField.add(maxLengthValidator(MAX_PHONE_LENGTH, "nominationCandidatePhoneInvalid"));
		nominationDetailsContainer.add(nomineePhoneField);

		AiAssistTextAreaPanel nominationReasonEditor = new AiAssistTextAreaPanel(
				"nominationReasonEditor",
				new PropertyModel<>(this, "nominationReason"),
				null,
				null,
				(originalText, instruction, styleContext) -> null,
				false,
				true,
				false,
				false,
				false,
				MAX_REASON_LENGTH,
				5,
				textArea -> {
					textArea.setRequired(true);
					textArea.setConvertEmptyInputStringToNull(true);
					textArea.setLabel(new ResourceModel("nominationCandidateReasonLabel"));
					textArea.add(minLengthValidator(MIN_REASON_LENGTH, "nominationCandidateReasonTooShort"));
					textArea.add(AttributeModifier.replace("placeholder", new ResourceModel("nominationCandidateReasonPlaceholder")));
				});
		nominationDetailsContainer.add(nominationReasonEditor);

		nominationDetailsContainer.add(new Label("nominationReasonLabel", new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return getString(isThirdPartyNominationSelected() ? "nominationCandidateReasonLabel" : "publicNominationSelfReasonLabel");
			}
		}));

		nominationTargetGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				nominationDetailsContainer.setVisible(hasNominationTargetSelection());
				nominationThirdPartyHeader.setVisible(isThirdPartyNominationSelected());
				nomineeIdentityContainer.setVisible(isThirdPartyNominationSelected());
				target.add(nominationDetailsContainer);
				target.appendJavaScript("if (window.initPublicDynamicUi) { window.initPublicDynamicUi(); }");
			}
		});

		WebMarkupContainer reCaptcha = new WebMarkupContainer("reCaptcha");
		reCaptcha.add(AttributeModifier.replace("data-sitekey", dataSiteKey));
		reCaptcha.setVisibilityAllowed(captchaEnabled);
		nominationForm.add(reCaptcha);

		WebMarkupContainer restrictedCountriesLinkContainer = new WebMarkupContainer("restrictedCountriesLinkContainer");
		restrictedCountriesLinkContainer.setOutputMarkupPlaceholderTag(true);
		restrictedCountriesLinkContainer.setVisible(!restrictedCountries.isEmpty());
		nominationForm.add(restrictedCountriesLinkContainer);

		nominationForm.add(new IFormValidator() {
			private static final long serialVersionUID = 1L;

			@Override
			public FormComponent<?>[] getDependentFormComponents() {
				return new FormComponent<?>[] { nominatorEmailField, nomineeEmailField };
			}

			@Override
			public void validate(Form<?> form) {
				if (captchaEnabled) {
					HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
					String captchaResponse = request.getParameter("g-recaptcha-response");
					boolean validCaptcha = AppContext.getInstance().getManagerBeanRemote().isValidCaptchaResponse(captchaResponse);
					if (!validCaptcha) {
						error(getString("areYouRobot"));
					}
				}

				if (!hasNominationTargetSelection()) {
					return;
				}

				FormComponent<String> emailComponent = isSelfNominationSelected() ? nominatorEmailField : nomineeEmailField;
				String normalizedEmail = normalizeEmail((String) emailComponent.getConvertedInput());
				if (normalizedEmail == null || AuthorizedEmailListUtils.isEmailAuthorized(normalizedEmail, authorizedNominateEmails)) {
					return;
				}

				ValidationError error = new ValidationError().addKey("nominationCandidateEmailUnauthorized");
				error.setVariable("nominationRecipient", valueOrDefault(nominationRecipient, "-"));
				emailComponent.error(error);
			}
		});

		nominationForm.add(new Button("submitNomination") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				PublicNominationSubmission submission = buildSubmission();
				PublicNominationSubmissionResult result = AppContext.getInstance().getPreNominationBeanRemote().submitPublicNomination(
						PublicNominationFormPanel.this.publicElectionToken,
						submission,
						SecurityUtils.getClientIp());
				if (result != null && result.isSuccess()) {
					getSession().info(getString(valueOrDefault(result.getMessageKey(), "publicNominationFormSubmitSuccess")));
					setResponsePage(getPage().getClass(), getPage().getPageParameters());
					return;
				}
				String errorKey = result != null ? result.getMessageKey() : "publicNominationFormSubmitError";
				getSession().error(getString(valueOrDefault(errorKey, "publicNominationFormSubmitError")));
			}
		});

		buildRestrictedCountriesModal();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		if (shouldAutoShowRestrictedCountriesModal()) {
			response.render(OnDomReadyHeaderItem.forScript(
					"var restrictedCountriesModalElement = document.getElementById('" + RESTRICTED_COUNTRIES_MODAL_ID + "');"
							+ "if (restrictedCountriesModalElement && window.bootstrap && bootstrap.Modal) {"
							+ "bootstrap.Modal.getOrCreateInstance(restrictedCountriesModalElement).show();"
							+ "}"));
			markRestrictedCountriesModalAsAcknowledged();
		}
	}

	private void buildRestrictedCountriesModal() {
		WebMarkupContainer modalContainer = new WebMarkupContainer("restrictedCountriesModalContainer");
		modalContainer.setOutputMarkupPlaceholderTag(true);
		modalContainer.setVisible(!restrictedCountries.isEmpty());
		add(modalContainer);

		modalContainer.add(new Label("restrictedCountriesSummary", new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return RestrictedCountriesMessageResolver.resolve(PublicNominationFormPanel.this, electionType, restrictedCountryCodes);
			}
		}));

		modalContainer.add(new ListView<RestrictedCountryItem>("restrictedCountries", restrictedCountries) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<RestrictedCountryItem> item) {
				RestrictedCountryItem countryItem = item.getModelObject();
				ContextImage countryFlagImage = new ContextImage("countryFlag", countryItem.getFlagImagePath());
				countryFlagImage.add(AttributeModifier.replace("alt", countryItem.getCountryName()));
				countryFlagImage.add(AttributeModifier.replace("onerror",
						"this.classList.add('d-none');"
								+ "var fallback=this.nextElementSibling;"
								+ "if(fallback){fallback.classList.remove('d-none');}"
								+ "var label=this.closest('.text-center').querySelector('[data-country-label]');"
								+ "if(label){label.classList.add('d-none');}"));
				item.add(countryFlagImage);
				item.add(new Label("countryFallbackName", countryItem.getCountryName()));
				item.add(new Label("countryName", countryItem.getCountryName()));
			}
		});
	}

	private void loadRestrictedCountries(List<String> restrictedCountryCodes) {
		this.restrictedCountryCodes.clear();
		restrictedCountries.clear();

		if (restrictedCountryCodes == null || restrictedCountryCodes.isEmpty()) {
			return;
		}

		Set<String> addedCountryCodes = new LinkedHashSet<>();
		for (String countryCode : restrictedCountryCodes) {
			String normalizedCountryCode = COUNTRY_UTILS.normalizeCountryCode(countryCode);
			if (StringUtils.isBlank(normalizedCountryCode) || normalizedCountryCode.length() != 2 || !addedCountryCodes.add(normalizedCountryCode)) {
				continue;
			}

			this.restrictedCountryCodes.add(normalizedCountryCode);
			String countryName = COUNTRY_UTILS.getDisplayLabel(normalizedCountryCode, getLocale(), true);
			restrictedCountries.add(new RestrictedCountryItem(
					StringUtils.isNotBlank(countryName) ? countryName.trim() : normalizedCountryCode,
					"v2/images/flags/" + normalizedCountryCode.toLowerCase(Locale.ROOT) + ".svg"));
		}
	}

	private boolean shouldAutoShowRestrictedCountriesModal() {
		return NominationOrganizationSummaryPanel.shouldAutoShowRestrictedCountriesModal(!restrictedCountries.isEmpty(), hasRestrictedCountriesModalBeenShown());
	}

	private boolean hasRestrictedCountriesModalBeenShown() {
		ElectionsWebAdminSession session = getElectionsWebAdminSession();
		return session != null && session.hasDoNominationRestrictedCountriesModalBeenShown(publicElectionToken);
	}

	private void markRestrictedCountriesModalAsAcknowledged() {
		ElectionsWebAdminSession session = getElectionsWebAdminSession();
		if (session != null) {
			session.markDoNominationRestrictedCountriesModalAsShown(publicElectionToken);
		}
	}

	private ElectionsWebAdminSession getElectionsWebAdminSession() {
		return getSession() instanceof ElectionsWebAdminSession ? (ElectionsWebAdminSession) getSession() : null;
	}

	private PublicNominationSubmission buildSubmission() {
		PublicNominationSubmission submission = new PublicNominationSubmission();
		String normalizedNominatorName = normalize(nominatorName);
		String normalizedNominatorEmail = normalizeEmail(nominatorEmail);
		submission.setOrganizationName(normalize(organizationName));
		submission.setOrganizationCountry(normalize(organizationCountry));
		submission.setNominatorName(normalizedNominatorName);
		submission.setNominatorEmail(normalizedNominatorEmail);
		submission.setNomineeName(isSelfNominationSelected() ? normalizedNominatorName : normalize(nomineeName));
		submission.setNomineeEmail(isSelfNominationSelected() ? normalizedNominatorEmail : normalizeEmail(nomineeEmail));
		submission.setNomineePhoneNumber(buildNominationPhoneNumber(normalize(nomineePhoneCountryCode), normalize(nomineePhoneLocal)));
		submission.setNominationReason(normalize(nominationReason));
		submission.setUiLanguage(SecurityUtils.getLanguageCode().getCode());
		return submission;
	}

	private String resolveNominationRecipient(Election election) {
		if (election == null) {
			return "-";
		}

		String recipient = normalize(election.getDefaultRecipient());
		if (recipient != null) {
			return recipient;
		}

		String sender = normalize(election.getDefaultSender());
		return sender != null ? sender : "-";
	}

	private String normalize(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim().replaceAll("\\s+", " ");
		return normalized.isEmpty() ? null : normalized;
	}

	private String normalizeEmail(String value) {
		String normalized = normalize(value);
		return normalized != null ? normalized.toLowerCase(java.util.Locale.ROOT) : null;
	}

	private String valueOrDefault(String value, String fallback) {
		return value != null && !value.trim().isEmpty() ? value : fallback;
	}

	private IValidator<String> minLengthValidator(int minimum, String messageKey) {
		return validatable -> {
			String value = normalize(validatable.getValue());
			if (value != null && value.length() < minimum) {
				validatable.error(new ValidationError().addKey(messageKey));
			}
		};
	}

	private IValidator<String> maxLengthValidator(int maximum, String messageKey) {
		return validatable -> {
			String value = normalize(validatable.getValue());
			if (value != null && value.length() > maximum) {
				validatable.error(new ValidationError().addKey(messageKey));
			}
		};
	}

	private IValidator<String> regexValidator(Pattern pattern, String messageKey) {
		return validatable -> {
			String value = normalize(validatable.getValue());
			if (value != null && !pattern.matcher(value).matches()) {
				validatable.error(new ValidationError().addKey(messageKey));
			}
		};
	}

	private IValidator<String> countryValidator(String messageKey) {
		return validatable -> {
			String value = normalize(validatable.getValue());
			String normalizedCode = COUNTRY_UTILS.normalizeCountryCode(value);
			if (normalizedCode == null || "AA".equals(normalizedCode) || COUNTRY_UTILS.getNameById(normalizedCode) == null) {
				validatable.error(new ValidationError().addKey(messageKey));
			}
		};
	}

	private String buildNominationPhoneNumber(String countryCode, String phone) {
		if (countryCode == null) {
			return phone;
		}
		if (phone == null) {
			return countryCode;
		}
		return countryCode + " " + phone;
	}

	private boolean hasNominationTargetSelection() {
		return selfNomination != null;
	}

	private boolean isSelfNominationSelected() {
		return Boolean.TRUE.equals(selfNomination);
	}

	private boolean isThirdPartyNominationSelected() {
		return Boolean.FALSE.equals(selfNomination);
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getOrganizationCountry() {
		return organizationCountry;
	}

	public void setOrganizationCountry(String organizationCountry) {
		this.organizationCountry = organizationCountry;
	}

	public Boolean getSelfNomination() {
		return selfNomination;
	}

	public void setSelfNomination(Boolean selfNomination) {
		this.selfNomination = selfNomination;
	}

	public String getNominatorName() {
		return nominatorName;
	}

	public void setNominatorName(String nominatorName) {
		this.nominatorName = nominatorName;
	}

	public String getNominatorEmail() {
		return nominatorEmail;
	}

	public void setNominatorEmail(String nominatorEmail) {
		this.nominatorEmail = nominatorEmail;
	}

	public String getNomineeName() {
		return nomineeName;
	}

	public void setNomineeName(String nomineeName) {
		this.nomineeName = nomineeName;
	}

	public String getNomineeEmail() {
		return nomineeEmail;
	}

	public void setNomineeEmail(String nomineeEmail) {
		this.nomineeEmail = nomineeEmail;
	}

	public String getNomineePhoneCountryCode() {
		return nomineePhoneCountryCode;
	}

	public void setNomineePhoneCountryCode(String nomineePhoneCountryCode) {
		this.nomineePhoneCountryCode = nomineePhoneCountryCode;
	}

	public String getNomineePhoneLocal() {
		return nomineePhoneLocal;
	}

	public void setNomineePhoneLocal(String nomineePhoneLocal) {
		this.nomineePhoneLocal = nomineePhoneLocal;
	}

	public String getNominationReason() {
		return nominationReason;
	}

	public void setNominationReason(String nominationReason) {
		this.nominationReason = nominationReason;
	}

	private static final class RestrictedCountryItem implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String countryName;
		private final String flagImagePath;

		private RestrictedCountryItem(String countryName, String flagImagePath) {
			this.countryName = countryName;
			this.flagImagePath = flagImagePath;
		}

		public String getCountryName() {
			return countryName;
		}

		public String getFlagImagePath() {
			return flagImagePath;
		}
	}
}
