package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.email.SendEmailStep1Dashboard;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EmailTemplateType;

public class EmailTemplatesListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private final EmailTemplateFilterState filterState;
	private final List<EmailTemplateFilterCatalog.RecipientFilter> recipientFilterChoices = Arrays.asList(EmailTemplateFilterCatalog.RecipientFilter.values());
	private final List<EmailTemplateFilterCatalog.ElectionScopeFilter> electionScopeFilterChoices = Arrays.asList(EmailTemplateFilterCatalog.ElectionScopeFilter.values());
	private final List<EmailTemplateFilterCatalog.LanguageModeFilter> languageModeFilterChoices = Arrays.asList(EmailTemplateFilterCatalog.LanguageModeFilter.values());
	private final List<EmailTemplateFilterCatalog.SendModeFilter> sendModeFilterChoices = Arrays.asList(EmailTemplateFilterCatalog.SendModeFilter.values());

	public EmailTemplatesListPanel(String id, long electionId, PageParameters params) {
		super(id);
		filterState = EmailTemplateFilterState.from(params);
		List<ElectionEmailTemplate> emailTemplatesList = AppContext.getInstance().getManagerBeanRemote().getElectionEmailTemplates(electionId);
		Collections.sort(emailTemplatesList, new Comparator<ElectionEmailTemplate>() {

			@Override
			public int compare(ElectionEmailTemplate template1, ElectionEmailTemplate template2) {
				return template1.getTemplateType().equals(Constants.TemplateTypeNEW) ? -1 : template2.getTemplateType().equals(Constants.TemplateTypeNEW) ? 1 : template1.getTemplateType().compareTo(template2.getTemplateType());
			}
		});
		init(EmailTemplateFilterCatalog.applyFilters(emailTemplatesList, filterState), electionId);
	}

	private void init(List<ElectionEmailTemplate> emailTemplatesList, long electionId) {
		try {
			addFilterForm(electionId);
			final PageParameters returnParameters = filterState.toPageParameters(electionId);
			WebMarkupContainer emptyState = new WebMarkupContainer("emailTemplatesEmpty");
			emptyState.setVisible(emailTemplatesList == null || emailTemplatesList.isEmpty());
			add(emptyState);

			final ListView<ElectionEmailTemplate> emailTemplatesListView = new ListView<ElectionEmailTemplate>("emailTemplatesList", emailTemplatesList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<ElectionEmailTemplate> item) {
					final ElectionEmailTemplate currentTemplate = item.getModelObject();
					try {
						item.add(AttributeModifier.replace("id", resolveTemplateAnchorId(currentTemplate.getTemplateType())));
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
									setResponsePage(new SendEmailStep1Dashboard(currentTemplate.getTemplateType(), new PageParameters(returnParameters)));
								} catch (Exception e) {
									appLogger.error(e.getMessage(), e);
								}
							}
						};
						EmailTemplateType templateType = EmailTemplateType.fromKey(currentTemplate.getTemplateType());
						boolean showSendButton = templateType == null || templateType.shouldShowSendButton();
						enviarEmail.setVisible(showSendButton && electionId != 0);
						item.add(enviarEmail);

						Link<Void> editarPlantilla = new Link<Void>("editTemplate") {
							private static final long serialVersionUID = -995928488655867689L;

							@Override
							public void onClick() {
								try {
									setResponsePage(new EditEmailTemplateDashboard(currentTemplate.getTemplateType(), new PageParameters(returnParameters)));
								} catch (Exception e) {
									appLogger.error(e.getMessage(), e);
								}
							}
						};
						item.add(editarPlantilla);

						Link<Void> forzarPlantilla = new Link<Void>("forceTemplate") {
							private static final long serialVersionUID = -4122229927879986489L;

							@Override
							public void onClick() {
								try {
									int affectedElections = AppContext.getInstance().getManagerBeanRemote()
											.forceBaseTemplateToOpenElections(currentTemplate.getTemplateType(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
									String info = getString("mailTemplListForceSuccess1") + currentTemplate.getTemplateType() + getString("mailTemplListForceSuccess2") + affectedElections
											+ getString("mailTemplListForceSuccess3");
									getSession().info(info);
									setResponsePage(EmailTemplatesDashboard.class, new PageParameters(returnParameters));
								} catch (Exception e) {
									appLogger.error(e.getMessage(), e);
									getSession().error(getString("mailTemplListForceError"));
								}
							}
						};
						forzarPlantilla.setVisible(electionId == 0 && !Constants.TemplateTypeNEW.equalsIgnoreCase(currentTemplate.getTemplateType()));
						item.add(forzarPlantilla);

					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(emailTemplatesListView);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}

	private void addFilterForm(final long electionId) {
		Form<Void> filterForm = new Form<>("filterForm");
		add(filterForm);

		DropDownChoice<EmailTemplateFilterCatalog.RecipientFilter> recipientFilterInput = new DropDownChoice<>("recipientFilterInput", new PropertyModel<>(filterState, "recipientFilter"),
				recipientFilterChoices, createChoiceRenderer());
		filterForm.add(recipientFilterInput);

		DropDownChoice<EmailTemplateFilterCatalog.ElectionScopeFilter> electionScopeFilterInput = new DropDownChoice<>("electionScopeFilterInput",
				new PropertyModel<>(filterState, "electionScopeFilter"), electionScopeFilterChoices, createChoiceRenderer());
		filterForm.add(electionScopeFilterInput);

		DropDownChoice<EmailTemplateFilterCatalog.LanguageModeFilter> languageModeFilterInput = new DropDownChoice<>("languageModeFilterInput",
				new PropertyModel<>(filterState, "languageModeFilter"), languageModeFilterChoices, createChoiceRenderer());
		filterForm.add(languageModeFilterInput);

		DropDownChoice<EmailTemplateFilterCatalog.SendModeFilter> sendModeFilterInput = new DropDownChoice<>("sendModeFilterInput",
				new PropertyModel<>(filterState, "sendModeFilter"), sendModeFilterChoices, createChoiceRenderer());
		filterForm.add(sendModeFilterInput);

		filterForm.add(new TextField<>("templateTypeFilterInput", new PropertyModel<>(filterState, "templateType")));

		filterForm.add(new Button("applyFilterButton") {
			private static final long serialVersionUID = -3852893266318298884L;

			@Override
			public void onSubmit() {
				setResponsePage(EmailTemplatesDashboard.class, filterState.toPageParameters(electionId));
			}
		});

		filterForm.add(new Button("clearFilterButton") {
			private static final long serialVersionUID = -8576847573857098799L;

			{
				setDefaultFormProcessing(false);
			}

			@Override
			public void onSubmit() {
				setResponsePage(EmailTemplatesDashboard.class, UtilsParameters.getId(electionId));
			}
		});
	}

	private <T extends Enum<T> & EmailTemplateFilterCatalog.LocalizedFilterOption> IChoiceRenderer<T> createChoiceRenderer() {
		return new IChoiceRenderer<T>() {
			private static final long serialVersionUID = 1977061153147190628L;

			@Override
			public Object getDisplayValue(T object) {
				return object != null ? getString(object.getResourceKey()) : "";
			}

			@Override
			public String getIdValue(T object, int index) {
				return object != null ? object.name() : String.valueOf(index);
			}
		};
	}

	private String resolveTemplateAnchorId(String templateType) {
		if (templateType == null || templateType.trim().isEmpty()) {
			return "template";
		}
		return "template-" + templateType.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
	}

}
