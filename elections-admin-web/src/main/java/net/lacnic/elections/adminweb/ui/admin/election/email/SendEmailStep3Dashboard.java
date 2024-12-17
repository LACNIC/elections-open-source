package net.lacnic.elections.adminweb.ui.admin.election.email;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
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
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.UserVoter;

@AuthorizeInstantiation("elections-only-one")
public class SendEmailStep3Dashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = 9205049748099839214L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private List<Auditor> auditorsList = new ArrayList<>();
	private List<UserVoter> votersList = new ArrayList<>();
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
				}
			}
			String stringQuantity = "quantity";

			WebMarkupContainer auditorsContainer = new WebMarkupContainer("auditorsContainer");
			auditorsContainer.add(new Label(stringQuantity, new PropertyModel<>(SendEmailStep3Dashboard.this, stringQuantity)));

			ListView<Auditor> auditorsListView = new ListView<Auditor>("auditorsRecipientList", auditorsList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Auditor> item) {
					final Auditor auditor = item.getModelObject();
					try {
						item.add(new Label("name", auditor.getName()));
						item.add(new Label("email", auditor.getMail()));

						item.add(new AjaxLink<Void>("remove") {
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
						appLogger.error(e);
						error(e.getMessage());
					}
				}
			};

			auditorsContainer.setOutputMarkupPlaceholderTag(true);
			auditorsContainer.add(auditorsListView);
			add(auditorsContainer);
			auditorsContainer.setVisible(!auditorsList.isEmpty());

			WebMarkupContainer votersContainer = new WebMarkupContainer("votersContainer");
			votersContainer.add(new Label(stringQuantity, new PropertyModel<>(SendEmailStep3Dashboard.this, stringQuantity)));

			ListView<UserVoter> votersListView = new ListView<UserVoter>("votersRecipientList", votersList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<UserVoter> item) {
					final UserVoter voter = item.getModelObject();
					try {
						item.add(new Label("name", voter.getName()));
						item.add(new Label("email", voter.getMail()));
						item.add(new Label("country", voter.getCountry()));
						item.add(new Label("language", voter.getLanguage()));

						item.add(new AjaxLink<Void>("remove") {
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
						appLogger.error(e);
						error(e.getMessage());
					}
				}
			};

			votersContainer.setOutputMarkupPlaceholderTag(true);
			votersContainer.add(votersListView);
			add(votersContainer);
			votersContainer.setVisible(!votersList.isEmpty());

			add(new Link<Void>("send") {
				private static final long serialVersionUID = -5702257874045515363L;

				@Override
				public void onClick() {
					try {
						if (votersList.isEmpty())
							AppContext.getInstance().getManagerBeanRemote().queueMassiveSending(auditorsList, template);
						else
							AppContext.getInstance().getManagerBeanRemote().queueMassiveSending(votersList, template);
						getSession().info(getString("prevDestSuccess"));
						setResponsePage(EmailsDashboard.class, UtilsParameters.getId(template.getElection().getElectionId()));
					} catch (Exception e) {
						appLogger.error(e);
					}
				}
			});

			add(new BookmarkablePageLink<>("cancel", EmailTemplatesDashboard.class, UtilsParameters.getId(template.getElection().getElectionId())));

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

}
