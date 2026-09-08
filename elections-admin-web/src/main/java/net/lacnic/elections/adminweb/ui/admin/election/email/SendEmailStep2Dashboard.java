package net.lacnic.elections.adminweb.ui.admin.election.email;

import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EmailTemplatesDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardManagerBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.RecipientType;


public class SendEmailStep2Dashboard extends DashboardManagerBasePage {

	private static final long serialVersionUID = -5648589978016911231L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private RecipientType recipientType;


	public SendEmailStep2Dashboard(final ElectionEmailTemplate template, PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));

		final List<RecipientType> recipientTypes = Arrays.asList(RecipientType.values());

		RadioGroup<RecipientType> recipients = new RadioGroup<>("recipients", new PropertyModel<>(this, "recipientType"));

		recipients.add(new ListView<RecipientType>("recipientTypes", recipientTypes) {
				private static final long serialVersionUID = -1145609116531304514L;

				protected void populateItem(ListItem<RecipientType> listItem) {
					RecipientType type = listItem.getModelObject();
					listItem.add(new Radio<RecipientType>("recipientTypesRadio", listItem.getModel()));
					listItem.add(new Label("label", getString("sendMail2RecipientType." + type.name() + ".label")));
					listItem.add(new Label("description", getString("sendMail2RecipientType." + type.name() + ".description")));
				}
			});

		Form<Void> form = new Form<>("form");
		add(form);
		
		form.add(recipients);
		
		form.add(new BookmarkablePageLink<>("cancel", EmailTemplatesDashboard.class, new PageParameters(params)));

		form.add(new Button("next") {
			private static final long serialVersionUID = -4455872975571708869L;

			@Override
			public void onSubmit() {
				try {
					boolean isJoint = false;
					boolean toVoters = false;
					boolean votersEqual = true;				
					JointElection jointElection;

					if ((getRecipientType() != null) && (getRecipientType().compareTo(RecipientType.VOTERS) == 0)) {
						toVoters = true;
						isJoint = AppContext.getInstance().getManagerBeanRemote().isJointElection(template.getElection().getElectionId());
						// If it is a joint election and sending mail to voters, validate if census are equal 
						if (isJoint) {
							jointElection = AppContext.getInstance().getManagerBeanRemote().getJointElectionForElection(template.getElection().getElectionId());
							votersEqual = AppContext.getInstance().getManagerBeanRemote().electionsCensusEqual(jointElection); 
						};				
					};

					if (getRecipientType() == null) {
						error(getString("sendMail2Error"));
					} else if (toVoters && isJoint && !votersEqual) { 
						error(getString("sendMailJointCensusDiffError"));
					} else {
						template.setRecipientType(recipientType);
						setResponsePage(new SendEmailStep3Dashboard(template, new PageParameters(params)));
					}
				} catch (Exception e) {
					appLogger.error(e.getMessage(), e);
				}
			}
		});

	}

	public RecipientType getRecipientType() {
		return recipientType;
	}

	public void setRecipientType(RecipientType recipientType) {
		this.recipientType = recipientType;
	}

}
