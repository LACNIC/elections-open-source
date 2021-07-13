package net.lacnic.elections.adminweb.ui.admin.email;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.ui.components.DropDownElection;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;


@AuthorizeInstantiation("elections-only-one")
public class EmailsDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -9012477457154755011L;

	private Election electionFilter;


	public EmailsDashboard(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));

		Form<Void> form = new Form<>("electionFilterForm");
		add(form);
		DropDownElection electionsDropDown = new DropDownElection("election", new PropertyModel<>(EmailsDashboard.this, "electionFilter"));
		form.add(electionsDropDown);

		form.add(new Button("filter") {
			private static final long serialVersionUID = -5319374745120063191L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				params.remove(UtilsParameters.getIdText());
				params.add(UtilsParameters.getIdText(), getElectionFilter().getElectionId());
				setResponsePage(EmailsDashboard.class, params);
			}
		});

		add(new EmailsListPanel("emailsList", UtilsParameters.getIdAsLong(params), UtilsParameters.isAll(params)));
	}


	public Election getElectionFilter() {
		return electionFilter;
	}

	public void setElectionFilter(Election electionFilter) {
		this.electionFilter = electionFilter;
	}

}
