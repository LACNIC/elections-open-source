package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ByteArrayResource;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.CandidateCountryLink;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateWorkOrganization;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.utils.LinksUtils;

public class ViewCandidateAnswersDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 7010672582863118973L;
	private static final String KEY_CANDIDATE_ANSWERS_LINK_NOT_AVAILABLE = "candidateAnswersLinkNotAvailable";

	private static final Set<String> CANDIDATE_FIELDS_TO_SKIP = new HashSet<>(Arrays.asList(
			"votes",
			"workOrganizations",
			"countryLinks",
			"taskProgress",
			"auditorDecisions",
			"candidateQuestions"));

	private static final Set<String> NOMINATION_FIELDS_TO_SKIP = new HashSet<>(Arrays.asList(
			"supports"));

	public ViewCandidateAnswersDashboard(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedback"));

		long candidateId = UtilsParameters.getCandidateAsLong(params);
		Candidate candidate = AppContext.getInstance().getManagerBeanRemote().getCandidate(candidateId);
		if (candidate == null) {
			getSession().error(getString("candidateAnswersCandidateNotFound"));
			setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(UtilsParameters.getIdAsLong(params)));
			return;
		}

		long electionId = candidate.getElection() != null ? candidate.getElection().getElectionId() : UtilsParameters.getIdAsLong(params);
		Nomination nomination = getNominationWithDetails(electionId, candidateId);
		Candidate detailedCandidate = nomination != null && nomination.getCandidate() != null ? nomination.getCandidate() : candidate;

		List<CandidateWorkOrganization> organizations = getCandidateOrganizations(electionId, candidateId, detailedCandidate);
		List<CandidateCountryLink> countryLinks = getCandidateCountryLinks(electionId, candidateId, detailedCandidate);
		List<CandidateElectionTaskProgress> taskProgress = getCandidateTaskProgress(detailedCandidate, nomination);
		List<AuditorCandidateDecision> auditorDecisions = getCandidateAuditorDecisions(electionId, candidateId, detailedCandidate);
		List<SupportNomination> supports = nomination != null ? safeList(nomination.getSupports()) : Collections.emptyList();

		add(new NonCachingImage("picture", new ImageResource(detailedCandidate.getPictureInfo(), detailedCandidate.getPictureExtension())));
		add(new Label("candidateName", valueOrDash(detailedCandidate.getName())));
		add(new Label("candidateMail", valueOrDash(detailedCandidate.getMail())));

		String completionLink = nomination != null ? nomination.getAcceptNominationLink() : null;
		addExternalLink("nominationLink", "nominationLinkUnavailable", completionLink, "candidateManagemenListViewCompletionLinkUnavailable");
		addExternalLink("candidateLinkSpanishLink", "candidateLinkSpanishEmpty", resolveCandidateLinkForPresentation(detailedCandidate.getLinkSpanish(), detailedCandidate), KEY_CANDIDATE_ANSWERS_LINK_NOT_AVAILABLE);
		addExternalLink("candidateLinkEnglishLink", "candidateLinkEnglishEmpty", resolveCandidateLinkForPresentation(detailedCandidate.getLinkEnglish(), detailedCandidate), KEY_CANDIDATE_ANSWERS_LINK_NOT_AVAILABLE);
		addExternalLink("candidateLinkPortugueseLink", "candidateLinkPortugueseEmpty", resolveCandidateLinkForPresentation(detailedCandidate.getLinkPortuguese(), detailedCandidate), KEY_CANDIDATE_ANSWERS_LINK_NOT_AVAILABLE);
		addExternalLink("candidateLinkedinLink", "candidateLinkedinEmpty", detailedCandidate.getLinkedinUrl(), KEY_CANDIDATE_ANSWERS_LINK_NOT_AVAILABLE);

		byte[] proctorioResult = detailedCandidate.getProctorioResultFile();
		boolean hasProctorioFile = proctorioResult != null && proctorioResult.length > 0;
		String proctorioContentType = ProctorioFileSupport.resolveContentType(proctorioResult);
		String proctorioFileName = ProctorioFileSupport.resolveFileName(candidateId, proctorioResult);
		ResourceLink<Void> proctorioDownload = new ResourceLink<>(
				"proctorioDownload",
				new ByteArrayResource(proctorioContentType, hasProctorioFile ? proctorioResult : new byte[0], proctorioFileName));
		proctorioDownload.setVisible(hasProctorioFile);
		add(proctorioDownload);
		add(new Label("proctorioDownloadUnavailable", getString(KEY_CANDIDATE_ANSWERS_LINK_NOT_AVAILABLE)).setVisible(!hasProctorioFile));

		add(new MultiLineLabel("candidateDetails", buildObjectDetails(detailedCandidate, CANDIDATE_FIELDS_TO_SKIP)));
		add(new MultiLineLabel("nominationDetails", buildObjectDetails(nomination, NOMINATION_FIELDS_TO_SKIP)));
		add(new MultiLineLabel("workOrganizationsDetails", buildCollectionDetails(organizations)));
		add(new MultiLineLabel("countryLinksDetails", buildCollectionDetails(countryLinks)));
		add(new MultiLineLabel("taskProgressDetails", buildCollectionDetails(taskProgress)));
		add(new MultiLineLabel("auditorDecisionsDetails", buildCollectionDetails(auditorDecisions)));
		add(new MultiLineLabel("supportsDetails", buildCollectionDetails(supports)));

		add(new Link<Void>("back") {
			private static final long serialVersionUID = 3447226517623578286L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			}
		});
	}

	private void addExternalLink(String linkId, String emptyLabelId, String url, String emptyMessageKey) {
		boolean hasUrl = hasText(url);
		ExternalLink link = new ExternalLink(linkId, hasUrl ? url : "#", hasUrl ? url : "");
		link.setVisible(hasUrl);
		add(link);
		add(new Label(emptyLabelId, getString(emptyMessageKey)).setVisible(!hasUrl));
	}

	private String resolveCandidateLinkForPresentation(String explicitLink, Candidate candidate) {
		if (hasText(explicitLink)) {
			return explicitLink.trim();
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

	private Nomination getNominationWithDetails(long electionId, long candidateId) {
		List<Nomination> nominations = AppContext.getInstance().getManagerBeanRemote().getElectionNominations(electionId);
		for (Nomination nomination : nominations) {
			if (nomination == null || nomination.getCandidate() == null || nomination.getCandidate().getCandidateId() != candidateId) {
				continue;
			}
			String token = nomination.getAcceptNominationToken();
			if (hasText(token)) {
				Nomination detailed = AppContext.getInstance().getPreNominationBeanRemote().verifyAcceptNominationAccess(token);
				if (detailed != null) {
					return detailed;
				}
			}
			return nomination;
		}
		return null;
	}

	private List<CandidateWorkOrganization> getCandidateOrganizations(long electionId, long candidateId, Candidate candidate) {
		List<CandidateWorkOrganization> fromManager = filterByCandidateId(
				AppContext.getInstance().getManagerBeanRemote().getElectionCandidateWorkOrganizations(electionId),
				candidateId);
		if (!fromManager.isEmpty()) {
			return fromManager;
		}
		return safeList(candidate != null ? candidate.getWorkOrganizations() : null);
	}

	private List<CandidateCountryLink> getCandidateCountryLinks(long electionId, long candidateId, Candidate candidate) {
		List<CandidateCountryLink> fromManager = filterByCandidateId(
				AppContext.getInstance().getManagerBeanRemote().getElectionCandidateCountryLinks(electionId),
				candidateId);
		if (!fromManager.isEmpty()) {
			return fromManager;
		}
		return safeList(candidate != null ? candidate.getCountryLinks() : null);
	}

	private List<CandidateElectionTaskProgress> getCandidateTaskProgress(Candidate candidate, Nomination nomination) {
		List<CandidateElectionTaskProgress> direct = safeList(candidate != null ? candidate.getTaskProgress() : null);
		if (!direct.isEmpty()) {
			return direct;
		}
		if (nomination != null && nomination.getCandidate() != null) {
			return safeList(nomination.getCandidate().getTaskProgress());
		}
		return Collections.emptyList();
	}

	private List<AuditorCandidateDecision> getCandidateAuditorDecisions(long electionId, long candidateId, Candidate candidate) {
		List<AuditorCandidateDecision> fromManager = filterByCandidateId(
				AppContext.getInstance().getManagerBeanRemote().getElectionAuditorCandidateDecisions(electionId),
				candidateId);
		if (!fromManager.isEmpty()) {
			return fromManager;
		}
		return safeList(candidate != null ? candidate.getAuditorDecisions() : null);
	}

	private <T> List<T> filterByCandidateId(List<T> source, long candidateId) {
		if (source == null || source.isEmpty()) {
			return Collections.emptyList();
		}
		List<T> filtered = new ArrayList<>();
		for (T item : source) {
			if (item == null) {
				continue;
			}
			Object candidate = readFieldValue(item, "candidate");
			if (candidate == null) {
				continue;
			}
			if (String.valueOf(candidateId).equals(resolveEntityId(candidate))) {
				filtered.add(item);
			}
		}
		return filtered;
	}

	private Object readFieldValue(Object instance, String fieldName) {
		if (instance == null || fieldName == null) {
			return null;
		}
		try {
			Field field = instance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(instance);
		} catch (Exception e) {
			return null;
		}
	}

	private <T> List<T> safeList(List<T> source) {
		if (source == null) {
			return Collections.emptyList();
		}
		try {
			source.size();
			return source;
		} catch (RuntimeException e) {
			return Collections.emptyList();
		}
	}

	private String buildCollectionDetails(Collection<?> items) {
		if (items == null || items.isEmpty()) {
			return getString("candidateAnswersNoData");
		}
		StringBuilder sb = new StringBuilder();
		int index = 1;
		for (Object item : items) {
			sb.append('#').append(index++).append('\n');
			sb.append(buildObjectDetails(item, Collections.emptySet())).append("\n\n");
		}
		return sb.toString().trim();
	}

	private String buildObjectDetails(Object object, Set<String> fieldsToSkip) {
		if (object == null) {
			return getString("candidateAnswersNoData");
		}

		StringBuilder sb = new StringBuilder();
		for (Field field : object.getClass().getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if (fieldsToSkip.contains(field.getName())) {
				continue;
			}
			field.setAccessible(true);
			sb.append(field.getName()).append(": ");
			try {
				sb.append(formatValue(field.get(object)));
			} catch (IllegalAccessException e) {
				sb.append("ERROR");
			}
			sb.append('\n');
		}
		return sb.toString().trim();
	}

	private String formatValue(Object value) {
		if (value == null) {
			return "-";
		}
		if (value instanceof Date) {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format((Date) value);
		}
		if (value instanceof Enum<?>) {
			return ((Enum<?>) value).name();
		}
		if (value instanceof byte[]) {
			return "bytes[" + ((byte[]) value).length + "]";
		}
		if (value instanceof Collection<?>) {
			try {
				return "size=" + ((Collection<?>) value).size();
			} catch (RuntimeException e) {
				return "size=-";
			}
		}
		Package valuePackage = value.getClass().getPackage();
		if (valuePackage != null && valuePackage.getName().startsWith("net.lacnic.elections.domain")) {
			return value.getClass().getSimpleName() + "#" + resolveEntityId(value);
		}
		return String.valueOf(value);
	}

	private String resolveEntityId(Object entity) {
		if (entity == null) {
			return "-";
		}
		for (Field field : entity.getClass().getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			String fieldName = field.getName();
			if (!"id".equalsIgnoreCase(fieldName) && !fieldName.endsWith("Id")) {
				continue;
			}
			try {
				field.setAccessible(true);
				Object value = field.get(entity);
				return value == null ? "-" : String.valueOf(value);
			} catch (IllegalAccessException e) {
				return "-";
			}
		}
		return "-";
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
