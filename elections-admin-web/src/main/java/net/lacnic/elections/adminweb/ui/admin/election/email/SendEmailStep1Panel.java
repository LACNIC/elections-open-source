package net.lacnic.elections.adminweb.ui.admin.election.email;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EmailTemplatesDashboard;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;


public class SendEmailStep1Panel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");


	public SendEmailStep1Panel(String id, ElectionEmailTemplate template, PageParameters params) {
		super(id);
		final PageParameters returnParameters = params != null ? new PageParameters(params) : new PageParameters();

		try {
			Form<Void> form = new Form<>("form");
			add(form);

			TextField<String> sender = new TextField<>("sender", new PropertyModel<>(template, "election.defaultSender"));
			form.add(sender);
			setOutputMarkupId(true);

			TextField<String> subjectSP = new TextField<>("subjectSP", new PropertyModel<>(template, "subjectSP"));
			subjectSP.add(StringValidator.maximumLength(1000));
			subjectSP.setRequired(true);
			form.add(subjectSP);

			TextField<String> subjectEN = new TextField<>("subjectEN", new PropertyModel<>(template, "subjectEN"));
			subjectEN.add(StringValidator.maximumLength(1000));
			subjectEN.setRequired(true);
			form.add(subjectEN);

			TextField<String> subjectPT = new TextField<>("subjectPT", new PropertyModel<>(template, "subjectPT"));
			subjectPT.add(StringValidator.maximumLength(1000));
			subjectPT.setRequired(true);
			form.add(subjectPT);

			TextArea<String> bodySP = new TextArea<>("bodySP", new PropertyModel<>(template, "bodySP"));
			bodySP.add(StringValidator.maximumLength(2000));
			bodySP.setRequired(true);
			form.add(bodySP);

			TextArea<String> bodyEN = new TextArea<>("bodyEN", new PropertyModel<>(template, "bodyEN"));
			bodyEN.add(StringValidator.maximumLength(2000));
			bodyEN.setRequired(true);
			form.add(bodyEN);

			TextArea<String> bodyPT = new TextArea<>("bodyPT", new PropertyModel<>(template, "bodyPT"));
			bodyPT.add(StringValidator.maximumLength(2000));
			bodyPT.setRequired(true);
			form.add(bodyPT);

			form.add(new Button("next") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onSubmit() {
					try {
						AppContext.getInstance().getManagerBeanRemote().modifyElectionEmailTemplate(template);
						AppContext.getInstance().getManagerBeanRemote().modifyElectionEmailTemplate(template);
						setResponsePage(new SendEmailStep2Dashboard(template, new PageParameters(returnParameters)));
					} catch (Exception e) {
						appLogger.error(e.getMessage(), e);
					}
				}
			});

			form.add(new Link<Void>("back") {
				private static final long serialVersionUID = -1139916268825131175L;

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
