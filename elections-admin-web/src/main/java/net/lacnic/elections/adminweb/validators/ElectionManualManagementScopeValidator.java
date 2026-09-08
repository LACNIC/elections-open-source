package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

import net.lacnic.elections.domain.ElectionType;

public class ElectionManualManagementScopeValidator extends AbstractFormValidator {

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE_KEY = "electionManagementManualManagementRequiredForIanaAso";

	private final FormComponent<?>[] components;

	public ElectionManualManagementScopeValidator(FormComponent<ElectionType> electionTypeComponent,
			FormComponent<Boolean> manageOrganizationsManualComponent,
			FormComponent<Boolean> manageVotersManualComponent) {
		this.components = new FormComponent[] { electionTypeComponent, manageOrganizationsManualComponent, manageVotersManualComponent };
	}

	@Override
	public FormComponent<?>[] getDependentFormComponents() {
		return components;
	}

	@Override
	public void validate(Form form) {
		@SuppressWarnings("unchecked")
		FormComponent<ElectionType> electionTypeComponent = (FormComponent<ElectionType>) components[0];
		@SuppressWarnings("unchecked")
		FormComponent<Boolean> manageOrganizationsManualComponent = (FormComponent<Boolean>) components[1];
		@SuppressWarnings("unchecked")
		FormComponent<Boolean> manageVotersManualComponent = (FormComponent<Boolean>) components[2];

		ElectionType electionType = electionTypeComponent.getConvertedInput();
		if (electionType == null) {
			electionType = electionTypeComponent.getModelObject();
		}
		if (!requiresManualManagement(electionType)) {
			return;
		}

		Boolean manageOrganizationsManual = manageOrganizationsManualComponent.getConvertedInput();
		if (manageOrganizationsManual == null) {
			manageOrganizationsManual = manageOrganizationsManualComponent.getModelObject();
		}
		Boolean manageVotersManual = manageVotersManualComponent.getConvertedInput();
		if (manageVotersManual == null) {
			manageVotersManual = manageVotersManualComponent.getModelObject();
		}

		if (!Boolean.TRUE.equals(manageOrganizationsManual) || !Boolean.TRUE.equals(manageVotersManual)) {
			electionTypeComponent.error(electionTypeComponent.getString(MESSAGE_KEY));
		}
	}

	private boolean requiresManualManagement(ElectionType electionType) {
		return ElectionType.IANA == electionType || ElectionType.ASO == electionType;
	}
}
