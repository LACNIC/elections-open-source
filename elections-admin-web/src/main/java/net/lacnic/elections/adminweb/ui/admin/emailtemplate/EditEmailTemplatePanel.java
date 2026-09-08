package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionEmailTemplate;

public class EditEmailTemplatePanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	public EditEmailTemplatePanel(String id, ElectionEmailTemplate emailTemplate, PageParameters params) {
		super(id);
		final PageParameters returnParameters = params != null ? new PageParameters(params) : new PageParameters();
		try {
			Form<Void> form = new Form<>("form");
			add(form);

			TextField<String> subjectSP = new TextField<>("subjectSP", new PropertyModel<>(emailTemplate, "subjectSP"));
			subjectSP.add(StringValidator.maximumLength(1000));
			subjectSP.setRequired(true);
			form.add(subjectSP);

			TextField<String> subjectEN = new TextField<>("subjectEN", new PropertyModel<>(emailTemplate, "subjectEN"));
			subjectEN.add(StringValidator.maximumLength(1000));
			subjectEN.setRequired(true);
			form.add(subjectEN);

			TextField<String> subjectPT = new TextField<>("subjectPT", new PropertyModel<>(emailTemplate, "subjectPT"));
			subjectPT.add(StringValidator.maximumLength(1000));
			subjectPT.setRequired(true);
			form.add(subjectPT);

			TextArea<String> bodySP = new TextArea<>("bodySP", new PropertyModel<>(emailTemplate, "bodySP"));
			bodySP.add(StringValidator.maximumLength(2000));
			bodySP.setRequired(true);
			form.add(bodySP);

			TextArea<String> bodyEN = new TextArea<>("bodyEN", new PropertyModel<>(emailTemplate, "bodyEN"));
			bodyEN.add(StringValidator.maximumLength(2000));
			bodyEN.setRequired(true);
			form.add(bodyEN);

			TextArea<String> bodyPT = new TextArea<>("bodyPT", new PropertyModel<>(emailTemplate, "bodyPT"));
			bodyPT.add(StringValidator.maximumLength(2000));
			bodyPT.setRequired(true);
			form.add(bodyPT);

			form.add(new Button("save") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onSubmit() {
					try {
						long electionId = 0L;
						AppContext.getInstance().getManagerBeanRemote().modifyElectionEmailTemplate(emailTemplate);
						Election election = emailTemplate.getElection();
						String templateType = emailTemplate.getTemplateType();

						if (election != null) {
							String info = getString("mailTemplEditSuccess1") + templateType + getString("mailTemplEditSuccess3") + election.getTitleSpanish() + getString("mailTemplEditSuccess4");
							getSession().info(info);
							electionId = election.getElectionId();
						} else {
							String info = getString("mailTemplEditSuccess2") + templateType + getString("mailTemplEditSuccess4");
							getSession().info(info);
						}
						PageParameters responseParameters = new PageParameters(returnParameters);
						if (!responseParameters.get(UtilsParameters.getIdText()).isEmpty()) {
							electionId = UtilsParameters.getIdAsLong(responseParameters);
						}
						if (responseParameters.get(UtilsParameters.getIdText()).isEmpty()) {
							responseParameters = UtilsParameters.getId(electionId);
						}
						setResponsePage(new EditEmailTemplateDashboard(templateType, responseParameters));
					} catch (Exception e) {
						appLogger.error(e.getMessage(), e);
					}
				}
			});

			form.add(new Link<Void>("back") {
				private static final long serialVersionUID = 1283686909541851699L;

				@Override
				public void onClick() {
					setResponsePage(EmailTemplatesDashboard.class, new PageParameters(returnParameters));
				}
			});

		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}
}
