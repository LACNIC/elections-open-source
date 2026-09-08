package net.lacnic.elections.adminweb.ui.admin.election.email;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.admin.email.EmailsDashboard;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EmailTemplatesDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardManagerBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.utils.CountryUtils;


public class SendEmailStep3Dashboard extends DashboardManagerBasePage {

	private static final long serialVersionUID = 9205049748099839214L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();
	private static final String COMPONENT_ID_QUANTITY = "quantity";
	private static final String COMPONENT_ID_EMAIL = "email";
	private static final String COMPONENT_ID_REMOVE = "remove";

	private List<Auditor> auditorsList = new ArrayList<>();
	private List<UserVoter> votersList = new ArrayList<>();
	private List<Organization> organizationsList = new ArrayList<>();
	int quantity;


	public SendEmailStep3Dashboard(final ElectionEmailTemplate template, PageParameters params) {
		super(params);

		setOutputMarkupPlaceholderTag(true);
		add(new FeedbackPanel("feedback"));

		List recipientsList;
		try {
			recipientsList = AppContext.getInstance().getManagerBeanRemote().getRecipientsByRecipientType(template);

			quantity = 0;
			if (recipientsList != null && !recipientsList.isEmpty()) {
				quantity = recipientsList.size();
				if (recipientsList.get(0) instanceof UserVoter) {
					votersList = recipientsList;
				} else if (recipientsList.get(0) instanceof Auditor) {
					auditorsList = recipientsList;
				} else if (recipientsList.get(0) instanceof Organization) {
					organizationsList = recipientsList;
				}
			}

			WebMarkupContainer auditorsContainer = new WebMarkupContainer("auditorsContainer");
			auditorsContainer.add(new Label(COMPONENT_ID_QUANTITY, new PropertyModel<>(SendEmailStep3Dashboard.this, COMPONENT_ID_QUANTITY)));

			ListView<Auditor> auditorsListView = new ListView<Auditor>("auditorsRecipientList", auditorsList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Auditor> item) {
					final Auditor auditor = item.getModelObject();
					try {
						item.add(new Label("name", auditor.getName()));
						item.add(new Label(COMPONENT_ID_EMAIL, auditor.getMail()));

						item.add(new AjaxLink<Void>(COMPONENT_ID_REMOVE) {
							private static final long serialVersionUID = 6264547412680468966L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								item.remove();
								auditorsList.remove(auditor);
								quantity = (auditorsList.size());
								target.add(auditorsContainer);
							}
						});
					} catch (Exception e) {
						appLogger.error(e.getMessage(), e);
						error(e.getMessage());
					}
				}
			};

			auditorsContainer.setOutputMarkupPlaceholderTag(true);
			auditorsContainer.add(auditorsListView);
			add(auditorsContainer);
			auditorsContainer.setVisible(!auditorsList.isEmpty());

			WebMarkupContainer votersContainer = new WebMarkupContainer("votersContainer");
			votersContainer.add(new Label(COMPONENT_ID_QUANTITY, new PropertyModel<>(SendEmailStep3Dashboard.this, COMPONENT_ID_QUANTITY)));

			ListView<UserVoter> votersListView = new ListView<UserVoter>("votersRecipientList", votersList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<UserVoter> item) {
					final UserVoter voter = item.getModelObject();
					try {
						item.add(new Label("name", voter.getName()));
						item.add(new Label(COMPONENT_ID_EMAIL, voter.getMail()));
						item.add(new Label("country", resolveCountryLabel(voter.getCountry())));
						item.add(new Label("language", voter.getLanguage()));

						item.add(new AjaxLink<Void>(COMPONENT_ID_REMOVE) {
							private static final long serialVersionUID = -3818107825435540166L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								item.remove();
								votersList.remove(voter);
								quantity = (votersList.size());
								target.add(votersContainer);
							}
						});
					} catch (Exception e) {
						appLogger.error(e.getMessage(), e);
						error(e.getMessage());
					}
				}
			};

			votersContainer.setOutputMarkupPlaceholderTag(true);
			votersContainer.add(votersListView);
			add(votersContainer);
			votersContainer.setVisible(!votersList.isEmpty());

			WebMarkupContainer organizationsContainer = new WebMarkupContainer("organizationsContainer");
			organizationsContainer.add(new Label(COMPONENT_ID_QUANTITY, new PropertyModel<>(SendEmailStep3Dashboard.this, COMPONENT_ID_QUANTITY)));

			ListView<Organization> organizationsListView = new ListView<Organization>("organizationsRecipientList", organizationsList) {
				private static final long serialVersionUID = 4811146975752925323L;

				@Override
				protected void populateItem(ListItem<Organization> item) {
					final Organization organization = item.getModelObject();
					try {
						item.add(new Label("name", safe(organization.getMembershipContactName())));
						item.add(new Label("orgId", safe(organization.getOrgId())));
						item.add(new Label(COMPONENT_ID_EMAIL, safe(organization.getMembershipContactEmail())));
						item.add(new Label("country", resolveCountryLabel(organization.getCountry())));
						item.add(new Label("language", safe(organization.getMembershipContactLanguage())));

						item.add(new AjaxLink<Void>(COMPONENT_ID_REMOVE) {
							private static final long serialVersionUID = -5623934521853353537L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								item.remove();
								organizationsList.remove(organization);
								quantity = organizationsList.size();
								target.add(organizationsContainer);
							}
						});
					} catch (Exception e) {
						appLogger.error(e.getMessage(), e);
						error(e.getMessage());
					}
				}
			};

			organizationsContainer.setOutputMarkupPlaceholderTag(true);
			organizationsContainer.add(organizationsListView);
			add(organizationsContainer);
			organizationsContainer.setVisible(!organizationsList.isEmpty());

			add(new Link<Void>("send") {
				private static final long serialVersionUID = -5702257874045515363L;

				@Override
				public void onClick() {
					try {
						if (!votersList.isEmpty()) {
							AppContext.getInstance().getManagerBeanRemote().queueMassiveSending(votersList, template);
						} else if (!auditorsList.isEmpty()) {
							AppContext.getInstance().getManagerBeanRemote().queueMassiveSending(auditorsList, template);
						} else {
							AppContext.getInstance().getManagerBeanRemote().queueMassiveSending(organizationsList, template);
						}
						getSession().info(getString("prevDestSuccess"));
						setResponsePage(EmailsDashboard.class, UtilsParameters.getId(template.getElection().getElectionId()));
					} catch (Exception e) {
						appLogger.error(e.getMessage(), e);
					}
				}
			});

			add(new BookmarkablePageLink<>("cancel", EmailTemplatesDashboard.class, new PageParameters(params)));

		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}


	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	private String safe(String value) {
		return value == null ? "" : value;
	}

	private String resolveCountryLabel(String countryCode) {
		String normalizedCode = COUNTRY_UTILS.normalizeCountryCode(countryCode);
		if (normalizedCode == null) {
			return "";
		}
		return COUNTRY_UTILS.getDisplayLabel(normalizedCode, getLocale(), true);
	}

}
