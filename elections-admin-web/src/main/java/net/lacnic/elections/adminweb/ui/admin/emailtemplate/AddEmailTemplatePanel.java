package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.validators.TemplateTypeValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;

public class AddEmailTemplatePanel extends Panel {

	private static final long serialVersionUID = -6525820489640825347L;

	private ElectionEmailTemplate emailTemplate;


	public AddEmailTemplatePanel(String id) {
		super(id);
		emailTemplate = new ElectionEmailTemplate();

		Form<Void> form = new Form<>("form");
		add(form);

		final TextField<String> templateType = new TextField<>("templateType", new PropertyModel<>(emailTemplate, "templateType"));
		templateType.setRequired(true);
		templateType.add(new TemplateTypeValidator());
		form.add(templateType);

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
				boolean result = AppContext.getInstance().getManagerBeanRemote().createBaseEmailTemplate(emailTemplate, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
				if (result) {
					SecurityUtils.info(getString("baseMailTemplAddSuccessAdd"));
					setResponsePage(EmailTemplatesDashboard.class, UtilsParameters.getId(0L));
				} else {
					SecurityUtils.error(getString("baseMailTemplAddErrorAdd"));
				}
			}
		});

		form.add(new Link<Void>("back") {
			private static final long serialVersionUID = 3552524670657820059L;

			@Override
			public void onClick() {
				setResponsePage(EmailTemplatesDashboard.class, UtilsParameters.getId(0L));
			}
		});

	}

}