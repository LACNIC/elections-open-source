package net.lacnic.elections.adminweb.ui.admin.election.auditors;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;


@AuthorizeInstantiation("elections-only-one")
public class EditAuditorDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;

	private Auditor auditor;
	private String name;
	private String mail;
	private boolean commissioner;


	public EditAuditorDashboard(PageParameters params) {
		super(params);

		long auditorId = UtilsParameters.getAuditAsLong(params);
		auditor = AppContext.getInstance().getManagerBeanRemote().getAuditor(auditorId);
		mail = auditor.getMail();
		name = auditor.getName();
		commissioner = auditor.isCommissioner();

		add(new FeedbackPanel("feedback"));
		Form<Void> auditorForm = new Form<>("auditorForm");
		add(auditorForm);

		TextField<String> nameTextField = new TextField<>("name", new PropertyModel<>(auditor, "name"));
		nameTextField.setRequired(true);
		nameTextField.add(StringValidator.maximumLength(255));
		auditorForm.add(nameTextField);

		final EmailTextField mailTextField = new EmailTextField("mail", new PropertyModel<>(auditor, "mail"));
		mailTextField.setRequired(true);
		mailTextField.add(StringValidator.maximumLength(40));
		auditorForm.add(mailTextField);

		auditorForm.add(new CheckBox("commissionerCheckbox", new PropertyModel<>(auditor, "commissioner")));

		auditorForm.add(new Button("save") {
			private static final long serialVersionUID = 5845761991376797119L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				// Save only if something changed
				if (!(mail.equalsIgnoreCase(auditor.getMail())) || !(name.equalsIgnoreCase(auditor.getName())) || !(commissioner == auditor.isCommissioner())) {
					AppContext.getInstance().getManagerBeanRemote().editAuditor(getAuditor(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					getSession().info(getString("auditorEditSuccess"));
				}
				setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(auditor.getElection().getElectionId()));
			}
		});

		auditorForm.add(new Link<Void>("cancel") {
			private static final long serialVersionUID = -8699657879088465106L;

			@Override
			public void onClick() {
				setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(auditor.getElection().getElectionId()));
			}
		});
	}


	public Auditor getAuditor() {
		return auditor;
	}

	public void setAuditor(Auditor auditor) {
		this.auditor = auditor;
	}

}
