package net.lacnic.elections.adminweb.ui.token;

import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.validators.AuthorizedNominationEmailValidator;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.Nomination;

public class NominationFormPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final int MAX_COUNTRY_CODE_LENGTH = 5;
	private static final int MIN_PHONE_LENGTH = 5;
	private static final int MAX_PHONE_LENGTH = 50;
	private static final int MIN_REASON_LENGTH = 20;
	private static final int MAX_REASON_LENGTH = 2000;
	private static final Pattern FULL_NAME_PATTERN = Pattern.compile("^[\\p{L}][\\p{L}\\p{M} .,'-]*$");

	private final String token;
	private final Nomination nominationData = new Nomination();
	private String nominationPhoneCountryCode;
	private String nominationPhoneLocal;

	public NominationFormPanel(String id, Election election, String token) {
		super(id);
		this.token = token;
		add(new Label("electionTitle", valueOrDash(resolveElectionTitle(election))));

		Form<Void> nominationForm = new Form<>("nominationForm");
		add(nominationForm);
		String nominationRecipient = resolveNominationRecipient(election);

		TextField<String> candidateFullName = new TextField<>("candidateFullName", new PropertyModel<>(nominationData, "nominationName"));
		candidateFullName.setRequired(true);
		candidateFullName.setConvertEmptyInputStringToNull(true);
		candidateFullName.setLabel(new ResourceModel("nominationCandidateFullNameLabel"));
		candidateFullName.add(StringValidator.maximumLength(255));
		candidateFullName.add(minLengthValidator(3, "nominationCandidateFullNameTooShort"));
		candidateFullName.add(regexValidator(FULL_NAME_PATTERN, "nominationCandidateFullNameInvalid"));
		nominationForm.add(candidateFullName);

		EmailTextField candidateEmail = new EmailTextField("candidateEmail", new PropertyModel<>(nominationData, "nominationEmail"));
		candidateEmail.setRequired(true);
		candidateEmail.setConvertEmptyInputStringToNull(true);
		candidateEmail.setLabel(new ResourceModel("nominationCandidateEmailLabel"));
		candidateEmail.add(StringValidator.maximumLength(255));
		candidateEmail.add(new AuthorizedNominationEmailValidator(election != null ? election.getAuthorizedNominateEmails() : null, nominationRecipient));
		nominationForm.add(candidateEmail);

		TextField<String> candidatePhoneCountry = new TextField<>("candidatePhoneCountry", new PropertyModel<>(this, "nominationPhoneCountryCode"));
		candidatePhoneCountry.setRequired(true);
		candidatePhoneCountry.setConvertEmptyInputStringToNull(true);
		candidatePhoneCountry.setLabel(new ResourceModel("nominationCandidatePhoneLabel"));
		candidatePhoneCountry.add(maxLengthValidator(MAX_COUNTRY_CODE_LENGTH, "nominationCandidatePhoneCountryInvalid"));
		nominationForm.add(candidatePhoneCountry);

		TextField<String> candidatePhone = new TextField<>("candidatePhone", new PropertyModel<>(this, "nominationPhoneLocal"));
		candidatePhone.setRequired(true);
		candidatePhone.setConvertEmptyInputStringToNull(true);
		candidatePhone.setLabel(new ResourceModel("nominationCandidatePhoneLabel"));
		candidatePhone.add(minLengthValidator(MIN_PHONE_LENGTH, "nominationCandidatePhoneInvalid"));
		candidatePhone.add(maxLengthValidator(MAX_PHONE_LENGTH, "nominationCandidatePhoneInvalid"));
		nominationForm.add(candidatePhone);

		AiAssistTextAreaPanel candidateReasonEditor = new AiAssistTextAreaPanel(
				"candidateReasonEditor",
				new PropertyModel<>(nominationData, "nominationReason"),
				null,
				null,
				(originalText, instruction, styleContext) -> AppContext.getInstance().getPreNominationBeanRemote().improveCandidateTextWithStatus(
						token,
						originalText,
						instruction,
						styleContext,
						SecurityUtils.getClientIp()),
				false,
				true,
				true,
				false,
				false,
				MAX_REASON_LENGTH,
				4,
				textArea -> {
					textArea.setRequired(true);
					textArea.setConvertEmptyInputStringToNull(true);
					textArea.setLabel(new ResourceModel("nominationCandidateReasonLabel"));
					textArea.add(minLengthValidator(MIN_REASON_LENGTH, "nominationCandidateReasonTooShort"));
					textArea.add(AttributeModifier.replace("placeholder", new ResourceModel("nominationCandidateReasonPlaceholder")));
				});
		nominationForm.add(candidateReasonEditor);

		nominationForm.add(new Button("submitNomination") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				normalizeFormData();
				boolean ok = AppContext.getInstance().getPreNominationBeanRemote().doNomination(
						token,
						nominationData,
						SecurityUtils.getClientIp());
				if (ok) {
					getSession().info(
							MessageFormat.format(getString("nominationFormSubmitSuccess"), valueOrDash(nominationData.getNominationName()), valueOrDash(nominationData.getNominationEmail())));
					setResponsePage(getPage().getClass(), getPage().getPageParameters());
				} else {
					getSession().error(getString("nominationFormSubmitError"));
				}
			}
		});
	}

	private String resolveElectionTitle(Election election) {
		if (election == null) {
			return null;
		}
		return election.getTitle(SecurityUtils.getLocale().getLanguage());
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

	private void normalizeFormData() {
		nominationData.setNominationName(normalize(nominationData.getNominationName()));
		nominationData.setNominationEmail(normalize(nominationData.getNominationEmail()));
		nominationData.setNominationReason(normalize(nominationData.getNominationReason()));
		nominationPhoneCountryCode = normalize(nominationPhoneCountryCode);
		nominationPhoneLocal = normalize(nominationPhoneLocal);
		nominationData.setNominationPhoneNumber(buildNominationPhoneNumber(nominationPhoneCountryCode, nominationPhoneLocal));
	}

	private String normalize(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim().replaceAll("\\s+", " ");
		return normalized.isEmpty() ? null : normalized;
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

	private String valueOrDash(String value) {
		if (value == null || value.isEmpty()) {
			return "-";
		}
		return value;
	}

	public String getNominationPhoneCountryCode() {
		return nominationPhoneCountryCode;
	}

	public void setNominationPhoneCountryCode(String nominationPhoneCountryCode) {
		this.nominationPhoneCountryCode = nominationPhoneCountryCode;
	}

	public String getNominationPhoneLocal() {
		return nominationPhoneLocal;
	}

	public void setNominationPhoneLocal(String nominationPhoneLocal) {
		this.nominationPhoneLocal = nominationPhoneLocal;
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
}
