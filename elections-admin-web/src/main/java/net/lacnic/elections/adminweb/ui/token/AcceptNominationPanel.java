package net.lacnic.elections.adminweb.ui.token;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonRejectNomination;
import net.lacnic.elections.adminweb.ui.token.page.AcceptNominationConditionsPage;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.Organization;

public class AcceptNominationPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final String token;
	private String electionTitle;
	private String organizationName;
	private boolean acceptedNominationConditions;
	private boolean nominationRejected;
	private boolean alreadyAcceptedOtherNomination;
	private String acceptedNominationToken;

	public AcceptNominationPanel(String id, String token) {
		super(id);
		this.token = token;

		loadNominationData();

		WebMarkupContainer conditionsCard = new WebMarkupContainer("conditionsCard");
		conditionsCard.setVisible(!nominationRejected && !alreadyAcceptedOtherNomination);
		add(conditionsCard);

		WebMarkupContainer rejectedCard = new WebMarkupContainer("rejectedCard");
		rejectedCard.setVisible(nominationRejected);
		add(rejectedCard);

		WebMarkupContainer alreadyAcceptedCard = new WebMarkupContainer("alreadyAcceptedCard");
		alreadyAcceptedCard.setVisible(!nominationRejected && alreadyAcceptedOtherNomination);
		add(alreadyAcceptedCard);

		alreadyAcceptedCard.add(new Link<Void>("goAcceptedNominationButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				if (hasText(acceptedNominationToken)) {
					setResponsePage(GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(acceptedNominationToken));
				}
			}
		}.setVisible(hasText(acceptedNominationToken)));

		conditionsCard.add(new Label("electionTitle", valueOrDash(electionTitle)));

		Form<Void> acceptNominationForm = new Form<>("acceptNominationForm");
		conditionsCard.add(acceptNominationForm);

		CheckBox acceptNominationCheck = new CheckBox("acceptNominationCheck", new PropertyModel<>(this, "acceptedNominationConditions"));
		acceptNominationCheck.setRequired(true);
		acceptNominationCheck.setLabel(new ResourceModel("acceptNominationCheckRequiredLabel"));
		acceptNominationForm.add(acceptNominationCheck);

		Label conditionsLabel = new Label("acceptNominationConditionsHtml", AppContext.getInstance().getPreNominationBeanRemote().getAcceptNominationConditions(SecurityUtils.getLocale().getLanguage()));
		conditionsLabel.setEscapeModelStrings(false);
		acceptNominationForm.add(conditionsLabel);

		acceptNominationForm.add(new Label("acceptNominationClauseText", new StringResourceModel("acceptNominationClauseText", this, null).setParameters(valueOrDash(electionTitle), valueOrDash(organizationName))));

		Button confirmAcceptNominationButton = new Button("confirmAcceptNomination") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (!acceptedNominationConditions) {
					getSession().error(getString("acceptNominationCheckRequiredError"));
					return;
				}
				boolean ok = AppContext.getInstance().getPreNominationBeanRemote().acceptNomination(token, SecurityUtils.getClientIp());
				if (ok) {
					getSession().info(MessageFormat.format(getString("acceptNominationConfirmSuccess"), valueOrDash(electionTitle)));
				}
				setResponsePage(GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token));
			}
		};
		acceptNominationForm.add(confirmAcceptNominationButton);

		acceptNominationForm.add(new ButtonRejectNomination("rejectNominationButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onConfirm() {
				boolean ok = AppContext.getInstance().getPreNominationBeanRemote().rejectNomination(token, SecurityUtils.getClientIp());
				if (ok) {
					getSession().info(MessageFormat.format(getString("acceptNominationRejectSuccess"), valueOrDash(organizationName)));
				}
				setResponsePage(AcceptNominationConditionsPage.class, UtilsParameters.getToken(token));
			}
		});
	}

	private void loadNominationData() {
		Nomination nomination = AppContext.getInstance().getPreNominationBeanRemote().verifyAcceptNominationAccess(token);
		if (nomination == null) {
			electionTitle = null;
			organizationName = null;
			return;
		}

		electionTitle = nomination.getElection() != null ? nomination.getElection().getTitle(SecurityUtils.getLocale().getLanguage()) : null;
		Organization organization = nomination.getOrganization();
		organizationName = organization != null ? organization.getName() : null;
		nominationRejected = nomination.getStatus() == NominationStatus.REJECTED_BY_CANDIDATE;
		acceptedNominationToken = findAcceptedNominationTokenForSameEmail(nomination);
		alreadyAcceptedOtherNomination = hasText(acceptedNominationToken);
	}

	private String valueOrDash(String value) {
		if (value == null || value.isEmpty()) {
			return "-";
		}
		return value;
	}

	private String findAcceptedNominationTokenForSameEmail(Nomination currentNomination) {
		if (currentNomination == null || currentNomination.getElection() == null) {
			return null;
		}
		String currentEmail = normalizeEmail(currentNomination.getNominationEmail());
		if (!hasText(currentEmail)) {
			return null;
		}
		List<Nomination> nominations = AppContext.getInstance().getManagerBeanRemote().getElectionNominations(currentNomination.getElection().getElectionId());
		if (nominations == null || nominations.isEmpty()) {
			return null;
		}
		for (Nomination nomination : nominations) {
			if (nomination == null || nomination.getId() == currentNomination.getId() || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE) {
				continue;
			}
			if (currentEmail.equals(normalizeEmail(nomination.getNominationEmail())) && hasText(nomination.getAcceptNominationToken())) {
				return nomination.getAcceptNominationToken();
			}
		}
		return null;
	}

	private String normalizeEmail(String email) {
		if (!hasText(email)) {
			return null;
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	public boolean isAcceptedNominationConditions() {
		return acceptedNominationConditions;
	}

	public void setAcceptedNominationConditions(boolean acceptedNominationConditions) {
		this.acceptedNominationConditions = acceptedNominationConditions;
	}
}
