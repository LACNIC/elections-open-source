package net.lacnic.elections.adminweb.ui.token.page;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.token.NominationOrganizationSummaryPanel;
import net.lacnic.elections.adminweb.ui.token.NominationFormPanel;
import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.Organization;

public class DoNominationPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter NOMINATION_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private Organization organization;
	private List<String> restrictedCountryCodes = Collections.emptyList();
	private Nomination latestNomination;
	private boolean allowNewNomination = true;

	public DoNominationPage() {
		this(new PageParameters());
	}

	public DoNominationPage(PageParameters params) {
		super(params);
		resolveNominationState();
		add(new FeedbackPanel("feedbackPanel"));
		NominationFormPanel nominationFormPanel = new NominationFormPanel("nominationFormPanel", getElection(), getToken());
		nominationFormPanel.setVisible(allowNewNomination);
		add(nominationFormPanel);
		buildNominationStateCard();
		buildNominationTableCard();
	}

	@Override
	protected Component buildTopbarContent(String id) {
		return new NominationOrganizationSummaryPanel(id, organization, restrictedCountryCodes, getToken());
	}

	@Override
	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionNomination");
	}

	private void resolveNominationState() {
		latestNomination = findLatestNomination();
		allowNewNomination = !hasBlockingNomination();
	}

	private boolean hasBlockingNomination() {
		if (organization == null || organization.getNominations() == null || organization.getNominations().isEmpty()) {
			return false;
		}

		for (Nomination nomination : organization.getNominations()) {
			if (nomination == null || nomination.getStatus() == null) {
				continue;
			}
			if (nomination.getStatus() == NominationStatus.PROPOSED
					|| nomination.getStatus() == NominationStatus.ACCEPTED_BY_CANDIDATE
					|| nomination.getStatus() == NominationStatus.APPROVED) {
				return true;
			}
		}
		return false;
	}

	private Nomination findLatestNomination() {
		if (organization == null || organization.getNominations() == null || organization.getNominations().isEmpty()) {
			return null;
		}

		Nomination latest = null;
		for (Nomination nomination : organization.getNominations()) {
			if (nomination == null) {
				continue;
			}
			if (latest == null || compareNominationsForRecency(nomination, latest) < 0) {
				latest = nomination;
			}
		}
		return latest;
	}

	private int compareNominationsForRecency(Nomination left, Nomination right) {
		long leftDate = nominationDateToEpochMillis(left != null ? left.getNominationDate() : null);
		long rightDate = nominationDateToEpochMillis(right != null ? right.getNominationDate() : null);
		if (leftDate != rightDate) {
			return Long.compare(rightDate, leftDate);
		}
		long leftId = left != null ? left.getId() : -1;
		long rightId = right != null ? right.getId() : -1;
		return Long.compare(rightId, leftId);
	}

	private long nominationDateToEpochMillis(String nominationDate) {
		if (StringUtils.isBlank(nominationDate)) {
			return Long.MIN_VALUE;
		}
		try {
			LocalDateTime dateTime = LocalDateTime.parse(nominationDate.trim(), NOMINATION_DATE_FORMATTER);
			return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		} catch (DateTimeParseException ignored) {
			return Long.MIN_VALUE;
		}
	}

	private void buildNominationStateCard() {
		WebMarkupContainer nominationStateCard = new WebMarkupContainer("nominationStateCard");
		nominationStateCard.setOutputMarkupPlaceholderTag(true);
		nominationStateCard.setVisible(latestNomination != null);
		nominationStateCard.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveNominationStateCardClass()));
		add(nominationStateCard);

		WebMarkupContainer nominationStateHeader = new WebMarkupContainer("nominationStateHeader");
		nominationStateHeader.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveNominationStateHeaderClass()));
		nominationStateCard.add(nominationStateHeader);

		nominationStateHeader.add(new Label("nominationStateTitle", getString(resolveNominationStateTitleKey())));
		nominationStateCard.add(new Label("nominationStateBody", resolveNominationStateBody()));
	}

	private void buildNominationTableCard() {
		WebMarkupContainer nominationTableCard = new WebMarkupContainer("nominationTableCard");
		nominationTableCard.setOutputMarkupPlaceholderTag(true);
		nominationTableCard.setVisible(latestNomination != null);
		add(nominationTableCard);

		nominationTableCard.add(new Label("nominationTableId", latestNomination != null ? String.valueOf(latestNomination.getId()) : "-"));
		nominationTableCard.add(new Label("nominationTableDate", latestNomination != null ? valueOrDash(latestNomination.getNominationDate()) : "-"));
		nominationTableCard.add(new Label("nominationTableName", latestNomination != null ? valueOrDash(latestNomination.getNominationName()) : "-"));
		nominationTableCard.add(new Label("nominationTableEmail", latestNomination != null ? valueOrDash(latestNomination.getNominationEmail()) : "-"));
		nominationTableCard.add(new Label("nominationTableStatus", latestNomination != null ? resolveNominationStatusLabel(latestNomination.getStatus()) : "-"));
	}

	private String resolveNominationStateCardClass() {
		if (latestNomination == null || latestNomination.getStatus() == null) {
			return "card border-warning mb-3";
		}
		if (latestNomination.getStatus() == NominationStatus.REJECTED_BY_CANDIDATE) {
			return "card border-danger mb-3";
		}
		if (latestNomination.getStatus() == NominationStatus.APPROVED
				|| latestNomination.getStatus() == NominationStatus.ACCEPTED_BY_CANDIDATE) {
			return "card border-success mb-3";
		}
		return "card border-warning mb-3";
	}

	private String resolveNominationStateHeaderClass() {
		if (latestNomination == null || latestNomination.getStatus() == null) {
			return "card-header bg-warning-subtle";
		}
		if (latestNomination.getStatus() == NominationStatus.REJECTED_BY_CANDIDATE) {
			return "card-header bg-danger-subtle";
		}
		if (latestNomination.getStatus() == NominationStatus.APPROVED
				|| latestNomination.getStatus() == NominationStatus.ACCEPTED_BY_CANDIDATE) {
			return "card-header bg-success-subtle";
		}
		return "card-header bg-warning-subtle";
	}

	private String resolveNominationStateTitleKey() {
		if (latestNomination == null || latestNomination.getStatus() == null) {
			return "doNominationRegisteredCardTitle";
		}
		switch (latestNomination.getStatus()) {
		case REJECTED_BY_CANDIDATE:
			return "doNominationRejectedCardTitle";
		case APPROVED:
			return "doNominationApprovedCardTitle";
		case ACCEPTED_BY_CANDIDATE:
			return "doNominationAcceptedByCandidateCardTitle";
		case PROPOSED:
			return "doNominationPendingCardTitle";
		default:
			return "doNominationRegisteredCardTitle";
		}
	}

	private String resolveNominationStateBody() {
		if (latestNomination == null || latestNomination.getStatus() == null) {
			return getString("doNominationRegisteredCardBody");
		}
		switch (latestNomination.getStatus()) {
		case REJECTED_BY_CANDIDATE:
			return getString("doNominationRejectedCardBody");
		case APPROVED:
			return getString("doNominationApprovedCardBody");
		case ACCEPTED_BY_CANDIDATE:
			return getString("doNominationAcceptedByCandidateCardBody");
		case PROPOSED:
			return getString("doNominationPendingCardBody");
		default:
			return MessageFormat.format(getString("doNominationRegisteredCardBodyWithStatus"), resolveNominationStatusLabel(latestNomination.getStatus()));
		}
	}

	private String resolveNominationStatusLabel(NominationStatus nominationStatus) {
		if (nominationStatus == null) {
			return "-";
		}
		switch (nominationStatus) {
		case PROPOSED:
			return getString("doNominationStatusProposed");
		case ACCEPTED_BY_CANDIDATE:
			return getString("doNominationStatusAcceptedByCandidate");
		case REJECTED_BY_CANDIDATE:
			return getString("doNominationStatusRejectedByCandidate");
		case INVALID:
			return getString("doNominationStatusInvalid");
		case APPROVED:
			return getString("doNominationStatusApproved");
		default:
			return nominationStatus.name();
		}
	}

	private String valueOrDash(String value) {
		if (StringUtils.isBlank(value)) {
			return "-";
		}
		return value;
	}

	@Override
	protected Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params) {
		organization = AppContext.getInstance().getPreNominationBeanRemote().verifyDoNominationAccess(getToken());
		if (organization == null) {
			return Error404.class;
		}
		restrictedCountryCodes = loadRestrictedCountryCodes(organization.getElection() != null ? organization.getElection().getElectionId() : 0L);

		setWhereAmI("Pantalla do nomination usando Organization.doNominationToken");
		setContextClass(Organization.class.getName());
		setContextData("organizationId: " + organization.getId() + "\n" + "orgId: " + organization.getOrgId() + "\n" + "name: " + organization.getName() + "\n" + "country: " + organization.getCountry() + "\n" + "membershipContactEmail: " + organization.getMembershipContactEmail() + "\n" + "deudor: " + organization.isDeudor());
		setElection(organization.getElection());
		setHeaderUserDisplay(organization.getMembershipContactName());
		setHeaderNotificationStatus("Estado: activadas");
		return null;
	}

	@Override
	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		if (getElection() != null && !getElection().isDoNominationLinkAvailable()) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.ACCESS_NOT_AVAILABLE,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
					null,
					null));
		}
		ElectionCalendar calendar = findPublicCalendar(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
		if (calendar == null || calendar.getStartDate() == null) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.PUBLIC_ELECTION_NOT_AVAILABLE,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
					null,
					null));
		}
		TokenAccessGate.AccessBlock before = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.PUBLIC_ELECTION_NOT_AVAILABLE,
				PublicAccessDeniedPage.CountdownTargetDate.START,
				ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
				null,
				null);
		TokenAccessGate.AccessBlock after = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.PUBLIC_ELECTION_NOT_AVAILABLE,
				PublicAccessDeniedPage.CountdownTargetDate.END,
				ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
				null,
				null);
			return TokenAccessGate.of(calendar.getStartDate(), calendar.getEndDate(), before, after);
	}

	private List<String> loadRestrictedCountryCodes(long electionId) {
		if (electionId <= 0) {
			return Collections.emptyList();
		}
		try {
			Election detailedElection = AppContext.getInstance().getManagerBeanRemote().getElectionWithRestrictedCountries(electionId);
			if (detailedElection == null || detailedElection.getRestrictedCountryCodes() == null) {
				return Collections.emptyList();
			}
			return detailedElection.getRestrictedCountryCodes();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
}
