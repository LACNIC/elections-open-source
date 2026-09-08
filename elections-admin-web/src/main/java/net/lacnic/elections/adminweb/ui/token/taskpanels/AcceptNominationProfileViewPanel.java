package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskMode;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolver;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsImg;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.utils.Constants;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class AcceptNominationProfileViewPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String CANDIDATE_IMAGE_ID = "candidateImage";
	private static final String BIO_SECTION_SPANISH = "spanish";
	private static final String BIO_SECTION_ENGLISH = "english";
	private static final String BIO_SECTION_PORTUGUESE = "portuguese";
	private static final Set<String> DEFAULT_PICTURE_NAME_MARKERS = new HashSet<>(
			Arrays.asList("default_candidate_photo", "default-profile-picture", "foto_candidato_default"));

	private final Candidate candidate;

	public AcceptNominationProfileViewPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		this.candidate = resolution.getCandidate();
		add(buildTaskTitle("cardTitle"));

		AjaxLink<Void> editProfile = new AjaxLink<Void>("editProfile") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				setResponsePage(GenericAcceptNominationTasksPage.class, buildPageParameters(AcceptNominationTaskMode.EDIT));
			}
		};
		editProfile.setVisible(!isProfileTaskCompleted());
		add(editProfile);

		add(buildImageComponent());
		Label defaultPhotoRecommendation = new Label("defaultPhotoRecommendation", getString("candidateProfileDefaultPhotoWarning"));
		defaultPhotoRecommendation.setVisible(isDefaultPictureInUse());
		defaultPhotoRecommendation.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "alert alert-warning mt-3 mb-3"));
		add(defaultPhotoRecommendation);
		add(new Label("candidateNameHeading", getCandidateName()));
		add(new Label("candidateMail", StringUtils.defaultString(getCandidate().getMail())));

		String linkedinUrl = StringUtils.trimToNull(getCandidate().getLinkedinUrl());
		String linkedinDisplayValue = linkedinUrl != null ? linkedinUrl : getString("candidateProfileLinkedInNotAvailable");
		ExternalLink linkedinLink = new ExternalLink("linkedinLink", StringUtils.defaultString(linkedinUrl), linkedinDisplayValue);
		linkedinLink.setVisible(linkedinUrl != null);
		add(linkedinLink);

		Label linkedinNotAvailable = new Label("linkedinNotAvailable", linkedinDisplayValue);
		linkedinNotAvailable.setVisible(linkedinUrl == null);
		add(linkedinNotAvailable);

		WebMarkupContainer bioSpanishSection = createBiographySection("bioSpanishSection", "bioSpanishText", getCandidate().getBioSpanish());
		WebMarkupContainer bioEnglishSection = createBiographySection("bioEnglishSection", "bioEnglishText", getCandidate().getBioEnglish());
		WebMarkupContainer bioPortugueseSection = createBiographySection("bioPortugueseSection", "bioPortugueseText", getCandidate().getBioPortuguese());
		applyBiographySectionsVisibility(bioSpanishSection, bioEnglishSection, bioPortugueseSection);
		add(bioSpanishSection);
		add(bioEnglishSection);
		add(bioPortugueseSection);
	}

	private void applyBiographySectionsVisibility(
			WebMarkupContainer bioSpanishSection,
			WebMarkupContainer bioEnglishSection,
			WebMarkupContainer bioPortugueseSection) {
		boolean hasSpanish = StringUtils.isNotBlank(getCandidate().getBioSpanish());
		boolean hasEnglish = StringUtils.isNotBlank(getCandidate().getBioEnglish());
		boolean hasPortuguese = StringUtils.isNotBlank(getCandidate().getBioPortuguese());

		String visibleSection = resolveVisibleBiographySection(hasSpanish, hasEnglish, hasPortuguese);
		bioSpanishSection.setVisible(BIO_SECTION_SPANISH.equals(visibleSection));
		bioEnglishSection.setVisible(BIO_SECTION_ENGLISH.equals(visibleSection));
		bioPortugueseSection.setVisible(BIO_SECTION_PORTUGUESE.equals(visibleSection));
	}

	private String resolveVisibleBiographySection(boolean hasSpanish, boolean hasEnglish, boolean hasPortuguese) {
		String language = resolveCurrentLanguage();
		if ("en".equals(language) && hasEnglish) {
			return BIO_SECTION_ENGLISH;
		}
		if ("pt".equals(language) && hasPortuguese) {
			return BIO_SECTION_PORTUGUESE;
		}
		if (hasSpanish) {
			return BIO_SECTION_SPANISH;
		}
		if (hasEnglish) {
			return BIO_SECTION_ENGLISH;
		}
		if (hasPortuguese) {
			return BIO_SECTION_PORTUGUESE;
		}
		return "none";
	}

	private String resolveCurrentLanguage() {
		Locale locale = getSession() != null ? getSession().getLocale() : null;
		String language = locale != null ? locale.getLanguage() : null;
		return StringUtils.lowerCase(StringUtils.defaultString(language));
	}

	private PageParameters buildPageParameters(AcceptNominationTaskMode mode) {
		String token = getTaskResolution().getNomination() != null ? getTaskResolution().getNomination().getAcceptNominationToken() : "";
		PageParameters params = UtilsParameters.getToken(token);
		ElectionTaskKey selectedTaskKey = getSelectedTask() != null && getSelectedTask().getTaskKey() != null ? getSelectedTask().getTaskKey() : ElectionTaskKey.PROFILE;
		params.add(AcceptNominationTaskResolver.TASK_PARAM, selectedTaskKey.name());
		params.add(AcceptNominationTaskResolver.MODE_PARAM, mode.getParameterValue());
		return params;
	}

	private Component buildImageComponent() {
		byte[] pictureInfo = getCandidate().getPictureInfo();
		if (pictureInfo != null && pictureInfo.length > 0) {
			String extension = StringUtils.defaultIfBlank(getCandidate().getPictureExtension(), "jpg");
			return new NonCachingImage(CANDIDATE_IMAGE_ID, new ImageResource(pictureInfo, extension));
		}

		DefaultImage defaultImage = loadDefaultImage();
		if (defaultImage != null) {
			return new NonCachingImage(CANDIDATE_IMAGE_ID, new ImageResource(defaultImage.content, defaultImage.extension));
		}

		WebComponent candidateImage = new WebComponent(CANDIDATE_IMAGE_ID);
		candidateImage.setVisible(false);
		return candidateImage;
	}

	private boolean isDefaultPictureInUse() {
		String pictureName = StringUtils.lowerCase(StringUtils.defaultString(getCandidate().getPictureName()));
		if (isKnownDefaultPictureName(pictureName)) {
			return true;
		}

		byte[] currentPicture = getCandidate().getPictureInfo();
		if (currentPicture == null || currentPicture.length == 0) {
			return loadDefaultImage() != null;
		}

		DefaultImage defaultImage = loadDefaultImage();
		if (defaultImage == null || defaultImage.content == null || defaultImage.content.length == 0) {
			return false;
		}
		return Arrays.equals(currentPicture, defaultImage.content);
	}

	private boolean isKnownDefaultPictureName(String pictureName) {
		if (StringUtils.isBlank(pictureName)) {
			return false;
		}
		for (String marker : DEFAULT_PICTURE_NAME_MARKERS) {
			if (pictureName.contains(marker)) {
				return true;
			}
		}
		return false;
	}

	private DefaultImage loadDefaultImage() {
		try {
			Parameter parameter = AppContext.getInstance().getManagerBeanRemote().getParameter(Constants.DEFAULT_PHOTO);
			if (parameter == null || StringUtils.isBlank(parameter.getValue())) {
				return null;
			}

			String resolvedPath = resolveHomePlaceholder(parameter.getValue());
			Path imagePath = Paths.get(resolvedPath);
			if (!Files.exists(imagePath)) {
				return null;
			}

			byte[] imageBytes = Files.readAllBytes(imagePath);
			String extension = UtilsImg.extractExtension(resolvedPath);
			return new DefaultImage(imageBytes, extension);
		} catch (IOException ex) {
			appLogger.error("Unable to read default candidate photo file", ex);
		} catch (Exception ex) {
			appLogger.error("Unexpected error while loading default candidate photo", ex);
		}
		return null;
	}

	private String resolveHomePlaceholder(String value) {
		if (StringUtils.isBlank(value)) {
			return value;
		}
		String userHome = System.getProperty("user.home");
		if (StringUtils.isBlank(userHome)) {
			return value;
		}
		return value.replace("$HOME", userHome);
	}

	private WebMarkupContainer createBiographySection(String containerId, String textComponentId, String content) {
		WebMarkupContainer section = new WebMarkupContainer(containerId);
		boolean hasContent = StringUtils.isNotBlank(content);
		section.setVisible(hasContent);
		Label bioText = new Label(textComponentId, StringUtils.defaultString(content));
		bioText.setEscapeModelStrings(false);
		section.add(bioText);
		return section;
	}

	private String getCandidateName() {
		return StringUtils.defaultString(getCandidate().getName());
	}

	private Candidate getCandidate() {
		return candidate;
	}

	private boolean isProfileTaskCompleted() {
		return getSelectedTask() != null && getSelectedTask().getTaskKey() == ElectionTaskKey.PROFILE && getSelectedTask().isCompleted();
	}

	private static final class DefaultImage implements Serializable {
		private static final long serialVersionUID = 1L;
		private final byte[] content;
		private final String extension;

		private DefaultImage(byte[] content, String extension) {
			this.content = content;
			this.extension = extension;
		}
	}
}
