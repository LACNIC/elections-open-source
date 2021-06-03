package net.lacnic.elections.adminweb.ui.admin.election.census;

import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.ui.components.DropDownCountry;
import net.lacnic.elections.adminweb.ui.components.DropDownLanguage;
import net.lacnic.elections.domain.UserVoter;


public class AddUserVoterPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;


	public AddUserVoterPanel(String id, UserVoter userVoter) {
		super(id);

		TextField<String> name = new TextField<>("name", new PropertyModel<>(userVoter, "name"));
		name.setRequired(true);
		name.add(StringValidator.maximumLength(1000));
		add(name);

		EmailTextField mail = new EmailTextField("mail", new PropertyModel<>(userVoter, "mail"));
		mail.setRequired(true);
		mail.add(StringValidator.maximumLength(250));
		add(mail);

		TextField<Integer> voteAmount = new TextField<>("voteAmount", new PropertyModel<>(userVoter, "voteAmount"));
		voteAmount.add(RangeValidator.range(1, 100));
		voteAmount.setRequired(true);
		add(voteAmount);

		TextField<String> orgID = new TextField<>("orgID", new PropertyModel<>(userVoter, "orgID"));
		orgID.add(StringValidator.maximumLength(1000));
		add(orgID);

		add(new DropDownCountry(new PropertyModel<>(userVoter, "country")));
		add(new DropDownLanguage(new PropertyModel<>(userVoter, "language")));

	}

}