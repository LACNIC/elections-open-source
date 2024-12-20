package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.admin.election.email.SendEmailStep1Dashboard;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.utils.Constants;


public class EmailTemplatesListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public EmailTemplatesListPanel(String id, long electionId) {
		super(id);
		List<ElectionEmailTemplate> emailTemplatesList = AppContext.getInstance().getManagerBeanRemote().getElectionEmailTemplates(electionId);
		Collections.sort(emailTemplatesList, new Comparator<ElectionEmailTemplate>() {

			@Override
			public int compare(ElectionEmailTemplate template1, ElectionEmailTemplate template2) {
				return template1.getTemplateType().equals(Constants.TEMPLATE_TYPE_NEW) ? -1 : template2.getTemplateType().equals(Constants.TEMPLATE_TYPE_NEW) ? 1 : template1.getTemplateType().compareTo(template2.getTemplateType());
			}
		});
		init(emailTemplatesList, electionId);
	}

	private void init(List<ElectionEmailTemplate> emailTemplatesList, long electionId) {
		try {
			final ListView<ElectionEmailTemplate> emailTemplatesListView = new ListView<ElectionEmailTemplate>("emailTemplatesList", emailTemplatesList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<ElectionEmailTemplate> item) {
					final ElectionEmailTemplate currentTemplate = item.getModelObject();
					try {
						item.add(new Label("subjectEN", currentTemplate.getSubjectEN()));
						item.add(new MultiLineLabel("bodyEN", currentTemplate.getBodyEN()));
						item.add(new Label("subjectSP", currentTemplate.getSubjectSP()));
						item.add(new MultiLineLabel("bodySP", currentTemplate.getBodySP()));
						item.add(new Label("subjectPT", currentTemplate.getSubjectPT()));
						item.add(new MultiLineLabel("bodyPT", currentTemplate.getBodyPT()));
						item.add(new Label("templateType", currentTemplate.getTemplateType()));

						Link<Void> enviarEmail = new Link<Void>("sendEmail") {
							private static final long serialVersionUID = 7217163464200407226L;

							@Override
							public void onClick() {
								try {
									setResponsePage(new SendEmailStep1Dashboard(currentTemplate.getTemplateType(), UtilsParameters.getId(electionId)));
								} catch (Exception e) {
									appLogger.error(e);
								}
							}
						};
						enviarEmail.setVisible(sendButtonVisible(currentTemplate) && electionId != 0);
						item.add(enviarEmail);

						Link<Void> editarPlantilla = new Link<Void>("editTemplate") {
							private static final long serialVersionUID = -995928488655867689L;

							@Override
							public void onClick() {
								try {
									setResponsePage(new EditEmailTemplateDashboard(currentTemplate.getTemplateType(), UtilsParameters.getId(electionId)));
								} catch (Exception e) {
									appLogger.error(e);
								}
							}
						};
						item.add(editarPlantilla);

					} catch (Exception e) {
						error(e.getMessage());
					}
				}

				private boolean sendButtonVisible(ElectionEmailTemplate currentTemplate) {
					return (!(currentTemplate.getTemplateType().equals(Constants.TEMPLATE_TYPE_AUDITOR_AGREEMENT) || currentTemplate.getTemplateType().equals(Constants.TEMPLATE_TYPE_VOTE_CODES) || currentTemplate.getTemplateType().equals(Constants.TEMPLATE_TYPE_AUDITOR_REVISION)));
				}
			};
			add(emailTemplatesListView);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}

}
