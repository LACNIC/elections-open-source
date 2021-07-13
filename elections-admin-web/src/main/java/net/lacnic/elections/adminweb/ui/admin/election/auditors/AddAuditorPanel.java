package net.lacnic.elections.adminweb.ui.admin.election.auditors;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.validators.AuditorValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Election;


public class AddAuditorPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private Auditor auditor;


	public AddAuditorPanel(String id, Election election) {
		super(id);
		try {
			Form<Void> form = new Form<>("electionAuditorForm");
			add(form);

			auditor = new Auditor();
			TextField<String> name = new TextField<>("name", new PropertyModel<>(auditor, "name"));
			name.setRequired(true);
			form.add(name);

			TextField<String> mail = new TextField<>("mail", new PropertyModel<>(auditor, "mail"));
			mail.setRequired(true);
			mail.add(EmailAddressValidator.getInstance());
			form.add(mail);

			form.add(new AuditorValidator(election.getElectionId(), name, mail));

			form.add(new CheckBox("commissionerCheckbox", new PropertyModel<>(auditor, "commissioner")));

			SubmitLink addAuditorButton = new SubmitLink("addAuditor") {
				private static final long serialVersionUID = -6152250993469094986L;

				@Override
				public void onSubmit() {
					super.onSubmit();
					AppContext.getInstance().getManagerBeanRemote().addAuditor(election.getElectionId(), auditor, election.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(election.getElectionId()));
					getSession().info(getString("auditorManagementSuccessAdd"));
				}
			};
			form.add(addAuditorButton);

			Link<Void> finish = new Link<Void>("finish") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						AppContext.getInstance().getManagerBeanRemote().persistElectionAuditorsSet(election.getElectionId(), election.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						setResponsePage(ElectionsDashboard.class);
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			form.add(finish);

			Link<Void> skip = new Link<Void>("skip") {
				private static final long serialVersionUID = -5077147466274097615L;

				@Override
				public void onClick() {
					setResponsePage(ElectionsDashboard.class);
				}
			};
			form.add(skip);

			Link<Void> back = new Link<Void>("back") {
				private static final long serialVersionUID = 4841898170457659991L;

				@Override
				public void onClick() {
					setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
				}
			};
			form.add(back);

		} catch (Exception e) {
			error(getString("auditorManagementErrorAdd"));
		}
	}

}
