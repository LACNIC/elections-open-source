package net.lacnic.elections.adminweb.ui.token.panel;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.token.CandidateBiographyUtils;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.utils.LinksUtils;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class PublicElectionLegacyPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final Election election;
	private final List<Candidate> candidates = new ArrayList<>();
	private final List<Candidate> publicCandidates = new ArrayList<>();
	private final List<UserVoter> userVoters = new ArrayList<>();
	private final List<Vote> votes = new ArrayList<>();
	private final List<Auditor> auditors = new ArrayList<>();

	public PublicElectionLegacyPanel(String id, Election election, Collection<Candidate> candidates,
			Collection<UserVoter> userVoters, Collection<Vote> votes, Collection<Auditor> auditors) {
		super(id);
		this.election = election;
		if (candidates != null) {
			this.candidates.addAll(candidates);
		}
		if (userVoters != null) {
			this.userVoters.addAll(userVoters);
		}
		if (votes != null) {
			this.votes.addAll(votes);
		}
		if (auditors != null) {
			this.auditors.addAll(auditors);
		}

		sortCandidates();
		loadPublicCandidates();
		addHeader();
		addCandidatesSection();
		addResultsSection();
		addCommissionersSection();
		addRollSection();
	}

	private void addHeader() {
		add(new Label("legacyElectionTitle", valueOrDash(resolveElectionTitle())));
		Label description = new Label("legacyElectionDescription", valueOrDash(resolveElectionDescription()));
		description.setEscapeModelStrings(false);
		add(description);

		String startDate = election != null ? election.getVotingPeriodStartDateString() : null;
		String endDate = election != null ? election.getVotingPeriodEndDateString() : null;

		WebMarkupContainer datesBlock = new WebMarkupContainer("legacyDatesBlock");
		datesBlock.setVisible(hasText(startDate) || hasText(endDate));
		datesBlock.add(new Label("legacyElectionStartDate", valueOrDash(startDate)));
		datesBlock.add(new Label("legacyElectionEndDate", valueOrDash(endDate)));
		add(datesBlock);
	}

	private void addCandidatesSection() {
		boolean hasPublicCandidates = !publicCandidates.isEmpty();
		WebMarkupContainer abstentionNotice = new WebMarkupContainer("legacyAbstentionNotice");
		abstentionNotice.setVisible(hasPublicCandidates && hasPublishedAbstentionCandidate());
		add(abstentionNotice);
		WebMarkupContainer randomOrderNotice = new WebMarkupContainer("legacyRandomOrderNotice");
		randomOrderNotice.setVisible(hasPublicCandidates && election != null && election.isRandomOrderCandidates());
		add(randomOrderNotice);
		add(new WebMarkupContainer("legacyCandidatesEmpty").setVisible(!hasPublicCandidates));
		add(new ListView<Candidate>("legacyCandidateCards", publicCandidates) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Candidate> item) {
				Candidate candidate = item.getModelObject();
				NonCachingImage picture = new NonCachingImage("legacyCandidatePicture",
						new ImageResource(candidate != null ? candidate.getPictureInfo() : null,
								candidate != null ? candidate.getPictureExtension() : null));
				picture.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "rounded-circle candidate-photo-sepia"));
				picture.add(AttributeModifier.replace("style", "width:96px;height:96px;object-fit:cover;object-position:center;"));
				item.add(picture);
				item.add(new Label("legacyCandidateName", valueOrDash(candidate != null ? candidate.getName() : null)));

					item.add(new Label("legacyCandidateBio", valueOrDash(resolveCandidateBioSnippet(candidate))));

				String externalLink = resolveCandidateLink(candidate);
				ExternalLink link = new ExternalLink("legacyCandidateExternalLink",
						hasText(externalLink) ? externalLink : "#",
						hasText(externalLink) ? externalLink : "Sin enlace");
				link.setVisible(hasText(externalLink));
				item.add(link);
			}
		});
	}

	private void addResultsSection() {
		WebMarkupContainer resultsSection = new WebMarkupContainer("legacyResultsSection");
		resultsSection.setVisible(election != null && election.isResultLinkAvailable());
		resultsSection.add(new ListView<LegacyResultRow>("legacyResultRows", buildResultRows()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<LegacyResultRow> item) {
				LegacyResultRow row = item.getModelObject();
				item.add(new Label("legacyResultCandidate", row.getCandidateName()));
				item.add(new Label("legacyResultVotes", row.getVotesLabel()));
				item.add(new Label("legacyResultPercentage", row.getPercentageLabel()));
			}
		});
		resultsSection.add(new Label("legacyResultTotalVotes", String.valueOf(votes.size())));
		resultsSection.add(new Label("legacyResultEnabledVoters", String.valueOf(userVoters.size())));
		resultsSection.add(new Label("legacyResultVotedVoters", String.valueOf(countVotedVoters())));
		resultsSection.add(new Label("legacyResultParticipation", calculateParticipationLabel()));
		add(resultsSection);
	}

	private void addCommissionersSection() {
		List<LegacyCommissionerRow> commissionerRows = buildCommissionerRows();
		add(new WebMarkupContainer("legacyCommissionersEmpty").setVisible(commissionerRows.isEmpty()));
		WebMarkupContainer table = new WebMarkupContainer("legacyCommissionersTable");
		table.setVisible(!commissionerRows.isEmpty());
		table.add(new ListView<LegacyCommissionerRow>("legacyCommissionerRows", commissionerRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<LegacyCommissionerRow> item) {
				LegacyCommissionerRow row = item.getModelObject();
				item.add(new Label("legacyCommissionerName", row.getName()));
				Label approval = new Label("legacyCommissionerApproval", row.getApprovalLabel());
				approval.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getApprovalCssClass()));
				item.add(approval);
			}
		});
		add(table);
	}

	private void addRollSection() {
		List<LegacyRollRow> rollRows = buildRollRows();
		add(new Label("legacyRollOrganizations", String.valueOf(countDistinctOrganizations())));
		add(new Label("legacyRollCountries", String.valueOf(countDistinctCountries())));
		add(new Label("legacyRollVotes", String.valueOf(votes.size())));
		add(new WebMarkupContainer("legacyRollEmpty").setVisible(rollRows.isEmpty()));
		add(new ListView<LegacyRollRow>("legacyRollRows", rollRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<LegacyRollRow> item) {
				LegacyRollRow row = item.getModelObject();
				item.add(new Label("legacyRollCountry", row.getCountryCode()));
				item.add(new Label("legacyRollOrganization", row.getOrganization()));
				item.add(new Label("legacyRollRepresentative", row.getRepresentative()));
			}
		});
	}

	private void sortCandidates() {
		Collections.sort(candidates, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate a, Candidate b) {
				int aOrder = a != null ? a.getCandidateOrder() : 0;
				int bOrder = b != null ? b.getCandidateOrder() : 0;
				if (aOrder != bOrder) {
					return Integer.compare(bOrder, aOrder);
				}
				String aName = a != null ? a.getName() : "";
				String bName = b != null ? b.getName() : "";
				return aName.compareToIgnoreCase(bName);
			}
		});
	}

	private void loadPublicCandidates() {
		for (Candidate candidate : candidates) {
			if (isLegacyPublicCandidate(candidate)) {
				publicCandidates.add(candidate);
			}
		}
		if (election != null && election.isRandomOrderCandidates()) {
			Collections.shuffle(publicCandidates);
		}
	}

	private List<LegacyResultRow> buildResultRows() {
		Map<Long, Long> votesByCandidateId = new HashMap<>();
		for (Vote vote : votes) {
			if (vote == null || vote.getCandidate() == null) {
				continue;
			}
			long candidateId = vote.getCandidate().getCandidateId();
			Long current = votesByCandidateId.get(candidateId);
			votesByCandidateId.put(candidateId, current == null ? 1L : current + 1L);
		}

		List<LegacyResultRow> rows = new ArrayList<>();
		long totalVotes = votes.size();
		for (Candidate candidate : candidates) {
			if (!isLegacyResultCandidate(candidate)) {
				continue;
			}
			long candidateVotes = votesByCandidateId.containsKey(candidate.getCandidateId())
					? votesByCandidateId.get(candidate.getCandidateId())
					: 0L;
			double percentage = totalVotes > 0 ? (100.0d * candidateVotes) / totalVotes : 0.0d;
			rows.add(new LegacyResultRow(candidate.getName(), candidateVotes, percentage));
		}

		Collections.sort(rows, new Comparator<LegacyResultRow>() {
			@Override
			public int compare(LegacyResultRow a, LegacyResultRow b) {
				int votesCompare = Long.compare(b.getVotes(), a.getVotes());
				if (votesCompare != 0) {
					return votesCompare;
				}
				return valueOrDash(a.getCandidateName()).compareToIgnoreCase(valueOrDash(b.getCandidateName()));
			}
		});
		return rows;
	}

	private boolean isLegacyPublicCandidate(Candidate candidate) {
		return candidate != null
				&& !candidate.isAbstention()
				&& candidate.getStatus() == CandidateStatus.CONFIRMED_AND_PUBLISHED;
	}

	private boolean isLegacyResultCandidate(Candidate candidate) {
		return candidate != null && candidate.getStatus() == CandidateStatus.CONFIRMED_AND_PUBLISHED;
	}

	private boolean hasPublishedAbstentionCandidate() {
		for (Candidate candidate : candidates) {
			if (candidate != null && candidate.isAbstention() && candidate.getStatus() == CandidateStatus.CONFIRMED_AND_PUBLISHED) {
				return true;
			}
		}
		return false;
	}

	private List<LegacyCommissionerRow> buildCommissionerRows() {
		List<LegacyCommissionerRow> rows = new ArrayList<>();
		for (Auditor auditor : auditors) {
			if (auditor == null || !auditor.isCommissioner()) {
				continue;
			}
			rows.add(new LegacyCommissionerRow(valueOrDash(auditor.getName()), auditor.isAgreedConformity()));
		}
		Collections.sort(rows, new Comparator<LegacyCommissionerRow>() {
			@Override
			public int compare(LegacyCommissionerRow a, LegacyCommissionerRow b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		return rows;
	}

	private List<LegacyRollRow> buildRollRows() {
		List<LegacyRollRow> rows = new ArrayList<>();
		for (UserVoter userVoter : userVoters) {
			if (userVoter == null) {
				continue;
			}
			rows.add(new LegacyRollRow(
					normalizeCountry(userVoter.getCountry()),
					valueOrDash(userVoter.getOrgID()),
					maskRepresentative(userVoter.getName())));
		}
		Collections.sort(rows, new Comparator<LegacyRollRow>() {
			@Override
			public int compare(LegacyRollRow a, LegacyRollRow b) {
				int orgCompare = a.getOrganization().compareToIgnoreCase(b.getOrganization());
				if (orgCompare != 0) {
					return orgCompare;
				}
				int countryCompare = a.getCountryCode().compareToIgnoreCase(b.getCountryCode());
				if (countryCompare != 0) {
					return countryCompare;
				}
				return a.getRepresentative().compareToIgnoreCase(b.getRepresentative());
			}
		});
		return rows;
	}

	private long countVotedVoters() {
		long count = 0L;
		for (UserVoter userVoter : userVoters) {
			if (userVoter != null && userVoter.isVoted()) {
				count++;
			}
		}
		return count;
	}

	private long countDistinctOrganizations() {
		Set<String> values = new HashSet<>();
		for (UserVoter userVoter : userVoters) {
			if (userVoter != null && hasText(userVoter.getOrgID())) {
				values.add(userVoter.getOrgID().trim().toUpperCase(Locale.ROOT));
			}
		}
		return values.size();
	}

	private long countDistinctCountries() {
		Set<String> values = new HashSet<>();
		for (UserVoter userVoter : userVoters) {
			if (userVoter != null && hasText(userVoter.getCountry())) {
				values.add(userVoter.getCountry().trim().toUpperCase(Locale.ROOT));
			}
		}
		return values.size();
	}

	private String calculateParticipationLabel() {
		if (userVoters.isEmpty()) {
			return "0%";
		}
		double percentage = (100.0d * countVotedVoters()) / userVoters.size();
		NumberFormat format = NumberFormat.getNumberInstance(resolveLocale());
		format.setMinimumFractionDigits(1);
		format.setMaximumFractionDigits(1);
		return format.format(percentage) + "%";
	}

	private String resolveElectionTitle() {
		if (election == null) {
			return null;
		}
		String language = resolveLanguage();
		String title = language != null ? election.getTitle(language) : null;
		return hasText(title) ? title : election.getTitleSpanish();
	}

	private String resolveElectionDescription() {
		if (election == null) {
			return null;
		}
		String language = resolveLanguage();
		String description = language != null ? election.getDescription(language) : null;
		return hasText(description) ? description : election.getDescriptionSpanish();
	}

	private String resolveCandidateBioSnippet(Candidate candidate) {
		if (candidate == null) {
			return "Sin bio pública";
		}
		String language = resolveLanguage();
		String bio = language != null ? candidate.getBio(language) : null;
		if (!hasText(bio)) {
			bio = candidate.getBioSpanish();
		}
		if (!hasText(bio)) {
			return "Sin bio pública";
		}
		return CandidateBiographyUtils.toPlainTextSnippet(bio, 220);
	}

	private String resolveCandidateLink(Candidate candidate) {
		if (candidate == null) {
			return null;
		}
		String language = resolveLanguage();
		String link = language != null ? candidate.getLink(language) : null;
		if (!hasText(link)) {
			link = candidate.getLinkSpanish();
		}
		if (hasText(link)) {
			return link.trim();
		}
		return buildPublicCandidateProfileLink(candidate);
	}

	private String buildPublicCandidateProfileLink(Candidate candidate) {
		if (candidate == null || candidate.getElection() == null || candidate.getCandidateId() <= 0L) {
			return "";
		}
		String publicElectionToken = candidate.getElection().getPublicElectionToken();
		if (!hasText(publicElectionToken)) {
			return "";
		}
		return LinksUtils.buildPublicCandidateProfileLink(publicElectionToken, candidate.getCandidateId());
	}

	private String resolveLanguage() {
		return SecurityUtils.getLocale() != null ? SecurityUtils.getLocale().getLanguage() : "es";
	}

	private Locale resolveLocale() {
		return SecurityUtils.getLocale() != null ? SecurityUtils.getLocale() : new Locale("es");
	}

	private String maskRepresentative(String fullName) {
		if (!hasText(fullName)) {
			return "********";
		}
		String[] parts = fullName.trim().split("\\s+");
		StringBuilder sb = new StringBuilder();
		int maxInitials = Math.min(2, parts.length);
		for (int i = 0; i < maxInitials; i++) {
			if (!hasText(parts[i])) {
				continue;
			}
			sb.append(Character.toUpperCase(parts[i].charAt(0))).append(". ");
		}
		sb.append("********");
		return sb.toString().trim();
	}

	private String normalizeCountry(String country) {
		if (!hasText(country)) {
			return "-";
		}
		return country.trim().toUpperCase(Locale.ROOT);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private static class LegacyResultRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String candidateName;
		private final long votes;
		private final double percentage;

		private LegacyResultRow(String candidateName, long votes, double percentage) {
			this.candidateName = candidateName;
			this.votes = votes;
			this.percentage = percentage;
		}

		public String getCandidateName() {
			return candidateName;
		}

		public long getVotes() {
			return votes;
		}

		public String getVotesLabel() {
			return String.valueOf(votes);
		}

		public String getPercentageLabel() {
			NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
			format.setMinimumFractionDigits(1);
			format.setMaximumFractionDigits(1);
			return format.format(percentage) + "%";
		}
	}

	private static class LegacyCommissionerRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String name;
		private final boolean approved;

		private LegacyCommissionerRow(String name, boolean approved) {
			this.name = name;
			this.approved = approved;
		}

		public String getName() {
			return name;
		}

		public String getApprovalLabel() {
			return approved ? "Aprobado" : "Pendiente";
		}

		public String getApprovalCssClass() {
			return approved ? "badge text-bg-success" : "badge text-bg-warning";
		}
	}

	private static class LegacyRollRow implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String countryCode;
		private final String organization;
		private final String representative;

		private LegacyRollRow(String countryCode, String organization, String representative) {
			this.countryCode = countryCode;
			this.organization = organization;
			this.representative = representative;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public String getOrganization() {
			return organization;
		}

		public String getRepresentative() {
			return representative;
		}
	}
}
