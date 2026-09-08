package net.lacnic.elections.adminweb.ui.admin.election.organizations;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.evra.registro.CategoriasEnum;
import net.lacnic.elections.adminweb.ui.components.DropDownIdioma;
import net.lacnic.elections.adminweb.ui.components.DropDownCountry;
import net.lacnic.elections.adminweb.validators.NonBlankStringValidator;
import net.lacnic.elections.adminweb.validators.OptionalOrganizationOrgIdValidator;
import net.lacnic.elections.adminweb.validators.OrganizationCountryValidator;
import net.lacnic.elections.adminweb.validators.OrganizationOrgIdUniqueValidator;
import net.lacnic.elections.adminweb.validators.OrganizationVotesValidator;
import net.lacnic.elections.domain.pre.Organization;

public class AddOrganizationPanel extends Panel {

	private static final long serialVersionUID = 1893365330065619014L;

	public AddOrganizationPanel(String id, long electionId, Organization organization) {
		super(id);

		TextField<String> orgId = new TextField<>("orgId", new PropertyModel<>(organization, "orgId"));
		orgId.setRequired(false);
		orgId.add(StringValidator.maximumLength(255));
		orgId.add(new OptionalOrganizationOrgIdValidator("organizationsManagementValidationOrgIdInvalid"));
		orgId.add(new OrganizationOrgIdUniqueValidator(electionId));
		withTitle(orgId, "organizationsManagementOrgIdLabel");
		add(orgId);

		TextField<String> name = new TextField<>("name", new PropertyModel<>(organization, "name"));
		name.setRequired(true);
		name.add(StringValidator.maximumLength(255));
		name.add(new NonBlankStringValidator("organizationsManagementValidationNameRequired"));
		withTitle(name, "organizationsManagementNameLabel");
		add(name);

		TextField<Integer> votes = new TextField<>("votes", new PropertyModel<>(organization, "votes"));
		votes.setRequired(true);
		votes.add(new OrganizationVotesValidator());
		withTitle(votes, "organizationsManagementVotesLabel");
		add(votes);

		List<CategoriasEnum> categories = CategoriasEnum.obtenerVigentes();
		DropDownChoice<CategoriasEnum> category = new DropDownChoice<>("category", new PropertyModel<>(organization, "categoryEnum"), categories, new ChoiceRenderer<CategoriasEnum>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(CategoriasEnum categoria) {
				if (categoria == null) {
					return "";
				}
				return categoria.getNombreTabla();
			}

			@Override
			public String getIdValue(CategoriasEnum categoria, int index) {
				return categoria == null ? null : categoria.name();
			}
		});
		category.setRequired(true);
		category.setNullValid(true);
		withTitle(category, "organizationsManagementCategoryLabel");
		add(category);

		DropDownCountry country = new DropDownCountry("country", new PropertyModel<>(organization, "country"), false);
		country.setRequired(true);
		country.add(new OrganizationCountryValidator());
		withTitle(country, "organizationsManagementCountryLabel");
		add(country);

		TextField<String> cnpj = new TextField<>("cnpj", new PropertyModel<>(organization, "cnpj"));
		cnpj.setRequired(false);
		cnpj.add(StringValidator.maximumLength(255));
		withTitle(cnpj, "organizationsManagementCnpjLabel");
		add(cnpj);

		TextField<String> asn = new TextField<>("asn", new PropertyModel<>(organization, "asn"));
		asn.setRequired(false);
		asn.add(StringValidator.maximumLength(255));
		withTitle(asn, "organizationsManagementAsnLabel");
		add(asn);

		TextField<String> membershipContactId = new TextField<>("membershipContactId", new PropertyModel<>(organization, "membershipContactId"));
		membershipContactId.setRequired(true);
		membershipContactId.add(StringValidator.maximumLength(255));
		membershipContactId.add(new NonBlankStringValidator("organizationsManagementValidationMembershipContactIdRequired"));
		withTitle(membershipContactId, "organizationsManagementMembershipContactIdLabel");
		add(membershipContactId);

		TextField<String> membershipContactName = new TextField<>("membershipContactName", new PropertyModel<>(organization, "membershipContactName"));
		membershipContactName.setRequired(true);
		membershipContactName.add(StringValidator.maximumLength(255));
		membershipContactName.add(new NonBlankStringValidator("organizationsManagementValidationMembershipContactNameRequired"));
		withTitle(membershipContactName, "organizationsManagementMembershipContactNameLabel");
		add(membershipContactName);

		EmailTextField membershipContactEmail = new EmailTextField("membershipContactEmail", new PropertyModel<>(organization, "membershipContactEmail"));
		membershipContactEmail.setRequired(true);
		membershipContactEmail.add(StringValidator.maximumLength(255));
		membershipContactEmail.add(new NonBlankStringValidator("organizationsManagementValidationMembershipContactEmailRequired"));
		withTitle(membershipContactEmail, "organizationsManagementMembershipContactEmailLabel");
		add(membershipContactEmail);

		DropDownIdioma membershipContactLanguage = new DropDownIdioma("membershipContactLanguage", new PropertyModel<>(organization, "membershipContactLanguageEnum"));
		membershipContactLanguage.setRequired(true);
		withTitle(membershipContactLanguage, "organizationsManagementMembershipContactLanguageLabel");
		add(membershipContactLanguage);
	}

	private <T extends FormComponent<?>> T withTitle(T component, String key) {
		component.add(AttributeModifier.replace("title", new StringResourceModel(key, this, null)));
		component.setLabel(new ResourceModel(key));
		return component;
	}
}
