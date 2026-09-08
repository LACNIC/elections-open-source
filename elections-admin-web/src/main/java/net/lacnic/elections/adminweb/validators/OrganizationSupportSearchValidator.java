package net.lacnic.elections.adminweb.validators;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.model.StringResourceModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.token.taskpanels.GenericNominationOrgSupportsManagementPanel.SupportIdentifierType;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.ejb.ElectionsPreNominationEJB;

public class OrganizationSupportSearchValidator extends AbstractFormValidator {

	private static final long serialVersionUID = 1L;

	private final FormComponent[] components;
	private final String token;

	private Organization foundOrganization;
	private SupportIdentifierType selectedIdentifierType;
	private String normalizedIdentifierValue;
	private final String internalRecipient;

	public OrganizationSupportSearchValidator(FormComponent<SupportIdentifierType> identifierTypeComponent,
			FormComponent<String> identifierValueComponent,
			String token,
			String internalRecipient) {
		this.components = new FormComponent[] { identifierTypeComponent, identifierValueComponent };
		this.token = token;
		this.internalRecipient = internalRecipient;
	}

	@Override
	public FormComponent<?>[] getDependentFormComponents() {
		return components;
	}

	@Override
	public void validate(Form form) {
		foundOrganization = null;
		selectedIdentifierType = null;
		normalizedIdentifierValue = null;

		@SuppressWarnings("unchecked")
		FormComponent<SupportIdentifierType> identifierTypeComponent = (FormComponent<SupportIdentifierType>) components[0];
		@SuppressWarnings("unchecked")
		FormComponent<String> identifierValueComponent = (FormComponent<String>) components[1];

		SupportIdentifierType resolvedIdentifierType = identifierTypeComponent.getConvertedInput();
		if (resolvedIdentifierType == null) {
			resolvedIdentifierType = identifierTypeComponent.getModelObject();
		}
		if (resolvedIdentifierType == null) {
			identifierTypeComponent.error(identifierTypeComponent.getString("acceptNominationOrgSupportsIdentifierTypeRequired"));
			return;
		}

		String identifierValueInput = identifierValueComponent.getConvertedInput();
		if (identifierValueInput == null) {
			identifierValueInput = identifierValueComponent.getInput();
		}
		if (identifierValueInput == null) {
			identifierValueInput = identifierValueComponent.getModelObject();
		}

		String resolvedIdentifierValue = normalizeIdentifierValue(identifierValueInput);
		if (StringUtils.isBlank(resolvedIdentifierValue)) {
			identifierValueComponent.error(identifierValueComponent.getString("acceptNominationOrgSupportsIdentifierRequired"));
			return;
		}

		ElectionsPreNominationEJB preNominationBean = getPreNominationBean();
		String validationMessageKey = preNominationBean
				.getOrganizationSupportSearchValidationMessageKey(token, resolvedIdentifierType.name(), resolvedIdentifierValue);
		if (StringUtils.isNotBlank(validationMessageKey)) {
			if ("acceptNominationOrgSupportsMembershipContactMatchesCandidate".equals(validationMessageKey)) {
				identifierValueComponent.error(new StringResourceModel(validationMessageKey, identifierValueComponent, null)
						.setParameters(StringUtils.defaultIfBlank(internalRecipient, "-"))
						.getString());
			} else {
				identifierValueComponent.error(identifierValueComponent.getString(validationMessageKey));
			}
			return;
		}

		Organization organization = preNominationBean
				.findOrganizationForSupportRequest(token, resolvedIdentifierType.name(), resolvedIdentifierValue);
		if (organization == null) {
			identifierValueComponent.error(identifierValueComponent.getString("acceptNominationOrgSupportsSearchNotFound"));
			return;
		}

		selectedIdentifierType = resolvedIdentifierType;
		normalizedIdentifierValue = resolvedIdentifierValue;
		foundOrganization = organization;
	}

	private ElectionsPreNominationEJB getPreNominationBean() {
		return AppContext.getInstance().getPreNominationBeanRemote();
	}

	private String normalizeIdentifierValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return value.trim().toUpperCase(Locale.ROOT);
	}

	public Organization getFoundOrganization() {
		return foundOrganization;
	}

	public SupportIdentifierType getSelectedIdentifierType() {
		return selectedIdentifierType;
	}

	public String getNormalizedIdentifierValue() {
		return normalizedIdentifierValue;
	}
}
