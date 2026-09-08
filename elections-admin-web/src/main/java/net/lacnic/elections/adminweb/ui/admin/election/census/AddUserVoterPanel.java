package net.lacnic.elections.adminweb.ui.admin.election.census;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.ui.components.DropDownCountry;
import net.lacnic.elections.adminweb.ui.components.DropDownLanguage;
import net.lacnic.elections.adminweb.validators.CensusOrgIdIdentityValidator;
import net.lacnic.elections.adminweb.validators.CensusVotesValidator;
import net.lacnic.elections.adminweb.validators.NonBlankStringValidator;
import net.lacnic.elections.adminweb.validators.OptionalOrganizationCountryValidator;
import net.lacnic.elections.adminweb.validators.OptionalOrganizationOrgIdValidator;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.UserVoter;


public class AddUserVoterPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;


	public AddUserVoterPanel(String id, UserVoter userVoter, long electionId, boolean editingMode, boolean orgIdEnabled, boolean orgIdRequired) {
		super(id);

		TextField<String> name = new TextField<>("name", new PropertyModel<>(userVoter, "name"));
		name.setRequired(true);
		name.add(StringValidator.maximumLength(1000));
		name.add(new NonBlankStringValidator("censusManagementValidationNameRequired"));
		withTitle(name, "censusManagementPhName");
		add(name);

		TextField<String> orgName = new TextField<>("orgName", new PropertyModel<>(userVoter, "orgName"));
		orgName.setRequired(false);
		orgName.add(StringValidator.maximumLength(1000));
		withTitle(orgName, "censusManagementPhOrgName");
		add(orgName);

		IModel<String> mailModel = new PropertyModel<>(userVoter, "mail");
		IModel<String> orgIdModel = new PropertyModel<>(userVoter, "orgID");
		IModel<Long> userVoterIdModel = new PropertyModel<>(userVoter, "userVoterId");

		TextField<String> orgID = new TextField<>("orgID", orgIdModel);
		orgID.setEnabled(orgIdEnabled);
		orgID.setRequired(orgIdRequired);
		orgID.add(StringValidator.maximumLength(255));
		orgID.add(new OptionalOrganizationOrgIdValidator("censusManagementValidationOrgIdInvalid"));
			if (orgIdRequired) {
				orgID.add(new NonBlankStringValidator("censusManagementValidationOrgIdRequired"));
			}
			if (orgIdEnabled) {
				orgID.add(new CensusOrgIdIdentityValidator(electionId, editingMode, mailModel, userVoterIdModel));
			}
		withTitle(orgID, "censusManagementPhOrgId");

		EmailTextField mail = new EmailTextField("mail", mailModel);
		mail.setRequired(true);
		mail.add(StringValidator.maximumLength(255));
		mail.add(new NonBlankStringValidator("censusManagementValidationEmailRequired"));
		withTitle(mail, "censusManagementPhEmail");
		add(mail);

		TextField<Integer> voteAmount = new TextField<>("voteAmount", new PropertyModel<>(userVoter, "voteAmount"));
		voteAmount.setRequired(true);
		voteAmount.add(new CensusVotesValidator());
		withTitle(voteAmount, "censusManagementPhVotos");
		add(voteAmount);

		add(orgID);

		DropDownCountry country = new DropDownCountry(new PropertyModel<>(userVoter, "country"));
		country.setRequired(false);
		country.add(new OptionalOrganizationCountryValidator("censusManagementValidationCountryInvalid"));
		withTitle(country, "censusManagementUserColPais");
		add(country);

		DropDownLanguage language = new DropDownLanguage(new PropertyModel<LanguageCode>(userVoter, "languageEnum"));
		withTitle(language, "censusManagementUserColLang");
		add(language);

	}

	private <T extends FormComponent<?>> T withTitle(T component, String key) {
		component.add(AttributeModifier.replace("title", getString(key)));
		component.setLabel(new ResourceModel(key));
		return component;
	}

}
