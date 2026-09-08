package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.io.Serializable;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.validators.CandidateProfileLinkedinUrlValidator;
import net.lacnic.elections.adminweb.validators.CandidateProfileNameValidator;
import net.lacnic.elections.adminweb.validators.CandidatePictureUploadValidator;
import net.lacnic.elections.adminweb.validators.NonBlankStringValidator;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.TokenResourceKeys;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsImg;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.utils.Constants;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class AcceptNominationProfileEditPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private static final int MIN_NAME_LENGTH = 5;
	private static final int MAX_NAME_LENGTH = 255;
	private static final int MAX_LINKEDIN_LENGTH = 1000;
	private static final long MAX_PICTURE_SIZE_BYTES = CandidatePictureUploadValidator.MAX_CANDIDATE_INPUT_SIZE_BYTES;
	private static final Set<String> DEFAULT_PICTURE_NAME_MARKERS = new HashSet<>(
			Arrays.asList("default_candidate_photo", "default-profile-picture", "foto_candidato_default"));
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";

	private Candidate candidate;
	private final String token;

	private String candidateName;
	private String candidateEmail;
	private String contactEmail;
	private String electionTitle;

	private String linkedinUrl;

	private byte[] uploadedPictureInfo;
	private String uploadedPictureName;
	private String uploadedPictureExtension;

	public AcceptNominationProfileEditPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(buildTaskTitle("cardTitle"));
		this.candidate = resolution.getCandidate();
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";

		loadFormState();
		loadContactData();

		Form<Void> candidateProfileForm = new Form<>("candidateProfileForm");
		candidateProfileForm.setMultiPart(true);
		candidateProfileForm.setMaxSize(Bytes.bytes(MAX_PICTURE_SIZE_BYTES));
		add(candidateProfileForm);
		add(buildPhotoGuideReferenceImage("photoGuideReferenceImage"));
		candidateProfileForm.add(new Label("translationNoticeTitle", new ResourceModel("candidateProfileEditLimitedTitle")));
		candidateProfileForm.add(new Label("translationNoticeBody", new ResourceModel("candidateProfileEditLimitedBody")));
		candidateProfileForm.add(new Label("translationNoticeContact", new StringResourceModel("candidateProfileEditLimitedContact", this, null).setParameters(candidateEmail)));
            ExternalLink contactEmailLink = new ExternalLink("contactEmailLink", new LoadableDetachableModel<String>() {
                private static final long serialVersionUID = 1L;

                @Override
                protected String load() {
                    return buildContactEmailHref();
                }
            }, Model.of(contactEmail));
            contactEmailLink.add(new AttributeModifier("aria-label", Model.of(contactEmail)));
            candidateProfileForm.add(contactEmailLink);

		WebMarkupContainer candidateImageContainer = new WebMarkupContainer("candidateImageContainer");
		candidateImageContainer.setOutputMarkupId(true);
		candidateImageContainer.add(buildImageComponent("candidateImage"));
		candidateProfileForm.add(candidateImageContainer);

		FileUploadField candidatePictureUploadField = new FileUploadField("candidatePicture");
		Label defaultPhotoRecommendation = new Label("defaultPhotoRecommendation", new ResourceModel("candidateProfileDefaultPhotoWarning"));
		defaultPhotoRecommendation.setOutputMarkupPlaceholderTag(true);
		defaultPhotoRecommendation.setOutputMarkupId(true);
		defaultPhotoRecommendation.setVisible(isDefaultPictureInUse());
		defaultPhotoRecommendation.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "alert alert-warning mt-3 mb-3"));

		AjaxFormSubmitBehavior pictureChangeBehavior = new AjaxFormSubmitBehavior(candidateProfileForm, "change") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				FileUpload fileUpload = candidatePictureUploadField.getFileUpload();
				if (processPictureUpload(fileUpload)) {
					candidateImageContainer.addOrReplace(buildImageComponent("candidateImage"));
					target.add(candidateImageContainer);
				}
				defaultPhotoRecommendation.setVisible(isDefaultPictureInUse());
				target.add(defaultPhotoRecommendation);
				addExternalFeedback(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				addExternalFeedback(target);
			}
		};
		pictureChangeBehavior.setDefaultProcessing(false);
		candidatePictureUploadField.add(pictureChangeBehavior);
		candidateProfileForm.add(candidatePictureUploadField);
		candidateProfileForm.add(defaultPhotoRecommendation);

		TextField<String> candidateNameField = new TextField<>("candidateName", new PropertyModel<>(this, "candidateName"));
		candidateNameField.setConvertEmptyInputStringToNull(true);
		candidateNameField.add(AttributeModifier.replace("maxlength", String.valueOf(MAX_NAME_LENGTH)));
		candidateNameField.add(new NonBlankStringValidator("candidateProfileNameRequired"));
		candidateNameField.add(new CandidateProfileNameValidator(MIN_NAME_LENGTH, MAX_NAME_LENGTH));
		candidateProfileForm.add(candidateNameField);

		TextField<String> linkedinUrlField = new TextField<>("linkedinUrl", new PropertyModel<>(this, "linkedinUrl"));
		linkedinUrlField.setConvertEmptyInputStringToNull(true);
		linkedinUrlField.add(new CandidateProfileLinkedinUrlValidator(MAX_LINKEDIN_LENGTH));
		candidateProfileForm.add(linkedinUrlField);

		candidateProfileForm.add(new BookmarkablePageLink<Void>("backButton", GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token)));

		candidateProfileForm.add(new Button("finishAndSendButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (!persistEditableProfileChanges(candidatePictureUploadField)) {
					return;
				}
				SecurityUtils.info(getString("candidateProfileEditSuccess"));
				setResponsePage(GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token));
			}
		});
	}

	private void addExternalFeedback(AjaxRequestTarget target) {
		addPageFeedback(target);
	}

	private boolean persistEditableProfileChanges(FileUploadField candidatePictureUploadField) {
		FileUpload fileUpload = candidatePictureUploadField.getFileUpload();
		if (fileUpload != null && !processPictureUpload(fileUpload)) {
			return false;
		}

		if (!hasEditableProfileChanges()) {
			SecurityUtils.info(getString("candidateProfileNoChanges"));
			return true;
		}

		try {
			applyEditableProfileChanges();
			Candidate updatedCandidate = AppContext.getInstance().getPreNominationBeanRemote().saveCandidateProfile(token, getCandidate(), CANDIDATE_LINK_ACTIVITY_ACTOR,
					SecurityUtils.getClientIp());
			if (updatedCandidate == null) {
				SecurityUtils.error(getString(TokenResourceKeys.CANDIDATE_PROFILE_PROCESSING_ERROR));
				return false;
			}

				AcceptNominationProfileEditPanel.this.candidate = updatedCandidate;
				resetUploadedPicture();
				return true;
			} catch (Exception ex) {
				appLogger.error("Error updating candidate profile in edit mode", ex);
			SecurityUtils.error(getString(TokenResourceKeys.CANDIDATE_PROFILE_PROCESSING_ERROR));
			return false;
		}
	}

	private void loadFormState() {
		candidateName = StringUtils.defaultString(getCandidate().getName());
		candidateEmail = StringUtils.defaultIfBlank(getCandidate().getMail(), getString("taskEditDisabledFallbackCandidateEmail"));
		linkedinUrl = StringUtils.defaultString(getCandidate().getLinkedinUrl());
	}

	private void loadContactData() {
		contactEmail = resolveSupportRecipient();
		electionTitle = resolveElectionTitle();
	}

	private boolean hasEditableProfileChanges() {
		if (!StringUtils.equals(StringUtils.trimToEmpty(getCandidate().getName()), StringUtils.trimToEmpty(candidateName))) {
			return true;
		}
		if (!StringUtils.equals(StringUtils.trimToNull(getCandidate().getLinkedinUrl()), StringUtils.trimToNull(linkedinUrl))) {
			return true;
		}
		return uploadedPictureInfo != null && uploadedPictureInfo.length > 0;
	}

	private void applyEditableProfileChanges() {
		Candidate currentCandidate = getCandidate();
		currentCandidate.setName(StringUtils.trimToEmpty(candidateName));
		currentCandidate.setLinkedinUrl(StringUtils.trimToNull(linkedinUrl));

		if (uploadedPictureInfo != null && uploadedPictureInfo.length > 0) {
			currentCandidate.setPictureInfo(uploadedPictureInfo);
			currentCandidate.setPictureName(uploadedPictureName);
			currentCandidate.setPictureExtension(uploadedPictureExtension);
		}
	}

	private boolean processPictureUpload(FileUpload fileUpload) {
		CandidatePictureUploadValidator.PictureUploadResult pictureUploadResult = CandidatePictureUploadValidator.validateAndBuildForCandidate(fileUpload);
		if (!pictureUploadResult.isValid()) {
			handlePictureUploadError(pictureUploadResult.getFailureReason());
			return false;
		}

		uploadedPictureInfo = pictureUploadResult.getPictureInfo();
		uploadedPictureName = pictureUploadResult.getPictureName();
		uploadedPictureExtension = pictureUploadResult.getPictureExtension();
		return true;
	}

	private void handlePictureUploadError(CandidatePictureUploadValidator.FailureReason failureReason) {
		if (failureReason == CandidatePictureUploadValidator.FailureReason.INVALID_SIZE) {
			SecurityUtils.error(getString("candidateProfilePhotoSizeError"));
			return;
		}
		if (failureReason == CandidatePictureUploadValidator.FailureReason.INVALID_FORMAT) {
			SecurityUtils.error(getString("candidateManagementErrorForm"));
			return;
		}
		if (failureReason == CandidatePictureUploadValidator.FailureReason.PROCESSING_ERROR) {
			SecurityUtils.error(getString(TokenResourceKeys.CANDIDATE_PROFILE_PROCESSING_ERROR));
			return;
		}
		SecurityUtils.error(getString("candidateProfilePhotoUploadEmptyError"));
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

	private boolean isDefaultPictureInUse() {
		if (uploadedPictureInfo != null && uploadedPictureInfo.length > 0) {
			return false;
		}

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

	private Component buildImageComponent(String componentId) {
		if (uploadedPictureInfo != null && uploadedPictureInfo.length > 0) {
			return new NonCachingImage(componentId, new ImageResource(uploadedPictureInfo, uploadedPictureExtension));
		}

		byte[] pictureInfo = getCandidate().getPictureInfo();
		if (pictureInfo != null && pictureInfo.length > 0) {
			String extension = StringUtils.defaultIfBlank(getCandidate().getPictureExtension(), "jpg");
			return new NonCachingImage(componentId, new ImageResource(pictureInfo, extension));
		}

		DefaultImage defaultImage = loadDefaultImage();
		if (defaultImage != null) {
			return new NonCachingImage(componentId, new ImageResource(defaultImage.content, defaultImage.extension));
		}

		WebComponent candidateImage = new WebComponent(componentId);
		candidateImage.setVisible(false);
		return candidateImage;
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

	private String resolveSupportRecipient() {
		if (getTaskResolution() != null && getTaskResolution().getNomination() != null && getTaskResolution().getNomination().getElection() != null) {
			String electionRecipient = StringUtils.trimToNull(getTaskResolution().getNomination().getElection().getDefaultRecipient());
			if (electionRecipient != null) {
				return electionRecipient;
			}
			String electionSender = StringUtils.trimToNull(getTaskResolution().getNomination().getElection().getDefaultSender());
			if (electionSender != null) {
				return electionSender;
			}
		}

		try {
			Parameter parameter = AppContext.getInstance().getManagerBeanRemote().getParameter(Constants.DEFAULT_RECIPIENT);
			if (parameter != null && StringUtils.isNotBlank(parameter.getValue())) {
				return parameter.getValue();
			}
		} catch (Exception ex) {
			appLogger.error("Unable to resolve default recipient for profile edit limited mode", ex);
		}

		try {
			Parameter parameter = AppContext.getInstance().getManagerBeanRemote().getParameter(Constants.DEFAULT_SENDER);
			if (parameter != null && StringUtils.isNotBlank(parameter.getValue())) {
				return parameter.getValue();
			}
		} catch (Exception ex) {
			appLogger.error("Unable to resolve default sender fallback for profile edit limited mode", ex);
		}
		return getString("taskEditDisabledFallbackSender");
	}

	private String resolveElectionTitle() {
		if (getTaskResolution() != null && getTaskResolution().getNomination() != null && getTaskResolution().getNomination().getElection() != null) {
			String title = getTaskResolution().getNomination().getElection().getTitle(SecurityUtils.getLocale().getLanguage());
			if (StringUtils.isNotBlank(title)) {
				return title;
			}
		}
		return getString("taskEditDisabledFallbackElectionTitle");
	}

	private String buildContactEmailHref() {
		String subject = MessageFormat.format(getString("candidateProfileEditLimitedMailSubject"), StringUtils.defaultString(candidateName), StringUtils.defaultString(electionTitle));
		return "mailto:" + contactEmail + "?subject=" + encode(subject);
	}

	private String encode(String value) {
		return URLEncoder.encode(StringUtils.defaultString(value), StandardCharsets.UTF_8);
	}

	private void resetUploadedPicture() {
		uploadedPictureInfo = null;
		uploadedPictureName = null;
		uploadedPictureExtension = null;
	}

	private Candidate getCandidate() {
		return candidate;
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
