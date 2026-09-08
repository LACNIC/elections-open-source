package net.lacnic.elections.adminweb.ui.token.panel;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionLinkRecoveryMode;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.utils.PublicLinkRecoveryType;

public class PublicElectionRecoverLinkPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final int MAX_LINKS_BY_TYPE = 10;
	private static final String BRAZIL_COUNTRY_CODE = "BR";

	private final Election election;
	private String recoverLinkEmail;

	public PublicElectionRecoverLinkPanel(String id, Election election) {
		super(id);
		this.election = election;
		add(new FeedbackPanel("feedback"));
		add(new Label("recoverLinkPanelIntro", new ResourceModel("recoverLinkPanelIntro")));
		add(new Label("recoverLinkPanelHelp", new ResourceModel("recoverLinkPanelHelp")));

		final boolean recoveryEnabled = isRecoveryEnabled();
		boolean captchaAllowed = recoveryEnabled && AppContext.getInstance().getManagerBeanRemote().isShowCaptcha();
		final String dataSiteKey = captchaAllowed ? AppContext.getInstance().getManagerBeanRemote().getDataSiteKey() : "";
		final boolean captchaEnabled = captchaAllowed && hasText(dataSiteKey);

		Label onlyBrMilacnicNotice = new Label("recoverLinkOnlyBrMilacnicNotice", new ResourceModel("recoverLinkOnlyBrMilacnicNotice"));
		onlyBrMilacnicNotice.setVisible(resolveRecoveryMode().isOnlyBr());
		add(onlyBrMilacnicNotice);

		Form<Void> recoverLinkForm = new Form<Void>("recoverLinkForm") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onValidate() {
				super.onValidate();
				if (!captchaEnabled) {
					return;
				}
				HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
				String captchaResponse = request.getParameter("g-recaptcha-response");
				boolean valid = AppContext.getInstance().getManagerBeanRemote().isValidCaptchaResponse(captchaResponse);
				if (!valid) {
					error(getString("areYouRobot"));
				}
			}

			@Override
			protected void onSubmit() {
				processPublicLinkRecovery();
			}
		};
		recoverLinkForm.setOutputMarkupPlaceholderTag(true);
		recoverLinkForm.setVisible(recoveryEnabled);
		add(recoverLinkForm);

		EmailTextField recoverEmailField = new EmailTextField("recoverLinkEmail", new PropertyModel<>(this, "recoverLinkEmail"));
		recoverEmailField.setRequired(true);
		recoverEmailField.setLabel(new ResourceModel("recoverLinkEmailLabel"));
		recoverLinkForm.add(new Label("recoverLinkEmailLabel", new ResourceModel("recoverLinkEmailLabel")));
		recoverLinkForm.add(recoverEmailField);

		WebMarkupContainer recoverCaptcha = new WebMarkupContainer("recoverReCaptcha");
		recoverCaptcha.add(AttributeModifier.replace("data-sitekey", dataSiteKey));
		recoverCaptcha.setVisibilityAllowed(captchaEnabled);
		recoverLinkForm.add(recoverCaptcha);
		recoverLinkForm.add(new Button("recoverLinkSend", new ResourceModel("recoverLinkButton")));
	}

	private void processPublicLinkRecovery() {
		if (!isRecoveryEnabled()) {
			error(getString("recoverLinkNotEnabled"));
			return;
		}

		String normalizedEmail = normalizeEmail(recoverLinkEmail);
		if (!hasText(normalizedEmail)) {
			error(getString("recoverLinkInvalidEmail"));
			return;
		}

		try {
			RecoveryResult result = recoverPublicLinks(normalizedEmail);
			if (result.hasOperationalFailure()) {
				appLogger.error("Failed to queue one or more public link recovery emails. electionId={}, matches={}, queued={}",
						election != null ? election.getElectionId() : null,
						result.getMatches(),
						result.getQueued());
				error(getString("recoverLinkSendError"));
				return;
			}
		} catch (Exception e) {
			appLogger.error("Error processing public link recovery. electionId={}, email={}",
					election != null ? election.getElectionId() : null,
					normalizedEmail,
					e);
			error(getString("recoverLinkSendError"));
			return;
		}
		info(getString("recoverLinkRequestAccepted"));
		recoverLinkEmail = null;
	}

	private RecoveryResult recoverPublicLinks(String normalizedEmail) throws Exception {
		switch (resolveRecoveryMode()) {
		case NONE:
			return new RecoveryResult();
		case ONLY_BR:
			return recoverPublicLinksOnlyBrazil(normalizedEmail);
		case ALL:
		default:
			return recoverPublicLinksAllCountries(normalizedEmail);
		}
	}

	private RecoveryResult recoverPublicLinksAllCountries(String normalizedEmail) throws Exception {
		return recoverPublicLinksByCountry(normalizedEmail, null);
	}

	private RecoveryResult recoverPublicLinksOnlyBrazil(String normalizedEmail) throws Exception {
		return recoverPublicLinksByCountry(normalizedEmail, BRAZIL_COUNTRY_CODE);
	}

	private RecoveryResult recoverPublicLinksByCountry(String normalizedEmail, String countryCodeFilter) throws Exception {
		RecoveryResult summary = new RecoveryResult();
		summary.merge(recoverVoterVoteLinks(normalizedEmail, countryCodeFilter, MAX_LINKS_BY_TYPE));
		summary.merge(recoverDoNominationLinks(normalizedEmail, countryCodeFilter, MAX_LINKS_BY_TYPE));
		summary.merge(recoverSupportLinks(normalizedEmail, countryCodeFilter, MAX_LINKS_BY_TYPE));
		return summary;
	}

	private RecoveryResult recoverVoterVoteLinks(String normalizedEmail, String countryCodeFilter, int maxResults) {
		RecoveryResult result = new RecoveryResult();
		long electionId = election != null ? election.getElectionId() : 0L;
		if (electionId <= 0) {
			return result;
		}
		int safeMaxResults = maxResults > 0 ? maxResults : MAX_LINKS_BY_TYPE;
		List<UserVoter> userVoters = AppContext.getInstance().getManagerBeanRemote()
				.getElectionUserVotersByEmail(electionId, normalizedEmail, safeMaxResults, countryCodeFilter);
		for (UserVoter userVoter : safeList(userVoters)) {
			result.record(queuePublicRecoveryEmail(
					PublicLinkRecoveryType.VOTER_VOTE_LINK,
					userVoter != null ? userVoter.getMail() : null,
					userVoter != null ? userVoter.getTokenVoteLink() : null));
		}
		return result;
	}

	private RecoveryResult recoverDoNominationLinks(String normalizedEmail, String countryCodeFilter, int maxResults) {
		RecoveryResult result = new RecoveryResult();
		long electionId = election != null ? election.getElectionId() : 0L;
		if (electionId <= 0) {
			return result;
		}
		int safeMaxResults = maxResults > 0 ? maxResults : MAX_LINKS_BY_TYPE;
		List<Organization> organizations = AppContext.getInstance().getManagerBeanRemote()
				.getElectionOrganizationsByMembershipContactEmail(electionId, normalizedEmail, safeMaxResults, countryCodeFilter);
		for (Organization organization : safeList(organizations)) {
			String doNominationLink = organization != null && hasText(organization.getDoNominationToken())
					? organization.getDoNominationLink()
					: null;
			result.record(queuePublicRecoveryEmail(
					PublicLinkRecoveryType.NOMINATION_LINK,
					organization != null ? organization.getMembershipContactEmail() : null,
					doNominationLink));
		}
		return result;
	}

	private RecoveryResult recoverSupportLinks(String normalizedEmail, String countryCodeFilter, int maxResults) {
		RecoveryResult result = new RecoveryResult();
		long electionId = election != null ? election.getElectionId() : 0L;
		if (electionId <= 0) {
			return result;
		}
		int safeMaxResults = maxResults > 0 ? maxResults : MAX_LINKS_BY_TYPE;
		List<SupportNomination> supportNominations = AppContext.getInstance().getManagerBeanRemote()
				.getElectionSupportNominationsByEmail(electionId, normalizedEmail, safeMaxResults, countryCodeFilter);
		for (SupportNomination supportNomination : safeList(supportNominations)) {
			result.record(queuePublicRecoveryEmail(
					PublicLinkRecoveryType.SUPPORT_LINK,
					supportNomination != null ? supportNomination.getSupportingContactEmail() : null,
					supportNomination != null ? supportNomination.getSupportNominationLink() : null));
		}
		return result;
	}

	private boolean queuePublicRecoveryEmail(PublicLinkRecoveryType linkType, String recipientEmail, String linkUrl) {
		if (election == null || election.getElectionId() <= 0) {
			return false;
		}
		return AppContext.getInstance().getManagerBeanRemote().sendLinkRecoveryEmail(
				election.getElectionId(),
				recipientEmail,
				linkType.getKey(),
				linkUrl,
				"PUBLIC_ELECTION_PAGE",
				SecurityUtils.getClientIp());
	}

	private ElectionLinkRecoveryMode resolveRecoveryMode() {
		return election != null ? election.getPublicLinkRecoveryMode() : ElectionLinkRecoveryMode.NONE;
	}

	private boolean isRecoveryEnabled() {
		return election != null && !election.isClosed() && !resolveRecoveryMode().isNone();
	}

	private String normalizeEmail(String emailValue) {
		if (!hasText(emailValue)) {
			return null;
		}
		String normalized = emailValue.trim().toLowerCase(Locale.ROOT);
		return hasText(normalized) ? normalized : null;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private <T> List<T> safeList(List<T> values) {
		return values != null ? values : Collections.emptyList();
	}

	private static class RecoveryResult implements Serializable {
		private static final long serialVersionUID = 1L;
		private int matches;
		private int queued;
		private boolean operationalFailure;

		private void record(boolean queuedSuccessfully) {
			matches++;
			if (queuedSuccessfully) {
				queued++;
			}
		}

		private void merge(RecoveryResult anotherResult) {
			if (anotherResult == null) {
				return;
			}
			matches += anotherResult.matches;
			queued += anotherResult.queued;
			if (anotherResult.matches > 0 && anotherResult.queued <= 0) {
				operationalFailure = true;
			}
			operationalFailure = operationalFailure || anotherResult.operationalFailure;
		}

		private boolean hasOperationalFailure() {
			return operationalFailure;
		}

		private int getMatches() {
			return matches;
		}

		private int getQueued() {
			return queued;
		}
	}
}
