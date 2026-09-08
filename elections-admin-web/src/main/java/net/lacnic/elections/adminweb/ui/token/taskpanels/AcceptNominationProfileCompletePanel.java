package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.io.Serializable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.validators.CandidateProfileLinkedinUrlValidator;
import net.lacnic.elections.adminweb.validators.CandidateProfileNameValidator;
import net.lacnic.elections.adminweb.validators.CandidatePictureUploadValidator;
import net.lacnic.elections.adminweb.validators.NonBlankStringValidator;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsImg;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.adminweb.ui.token.TokenResourceKeys;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.utils.Constants;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class AcceptNominationProfileCompletePanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private static final int MIN_NAME_LENGTH = 5;
	private static final int MAX_NAME_LENGTH = 255;
	private static final int MAX_LINKEDIN_LENGTH = 1000;
	private static final int MAX_BIOGRAPHY_LENGTH = 2000;
	private static final long MAX_PICTURE_SIZE_BYTES = CandidatePictureUploadValidator.MAX_CANDIDATE_INPUT_SIZE_BYTES;
	private static final Set<String> DEFAULT_PICTURE_NAME_MARKERS = new HashSet<>(
			Arrays.asList("default_candidate_photo", "default-profile-picture", "foto_candidato_default"));
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";

	private Candidate candidate;
	private final String token;

	private String fullName;
	private String linkedinUrl;
	private String biographySpanish;

	private byte[] uploadedPictureInfo;
	private String uploadedPictureName;
	private String uploadedPictureExtension;

	public AcceptNominationProfileCompletePanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(buildTaskTitle("cardTitle"));
		this.candidate = resolution.getCandidate();
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";

		loadFormState();

		Label restrictionMessage = new Label("restrictionMessage", getTaskResolution().hasModeRestriction() ? getString(getTaskResolution().getRestrictionMessageKey()) : "");
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);

		Form<Void> candidateProfileForm = new Form<>("candidateProfileForm");
		candidateProfileForm.setMultiPart(true);
		candidateProfileForm.setMaxSize(Bytes.bytes(MAX_PICTURE_SIZE_BYTES));
		add(candidateProfileForm);
		add(buildPhotoGuideReferenceImage("photoGuideReferenceImage"));

		WebMarkupContainer candidateImageContainer = new WebMarkupContainer("candidateImageContainer");
		candidateImageContainer.setOutputMarkupId(true);
		candidateImageContainer.add(buildImageComponent("candidateImage"));
		candidateProfileForm.add(candidateImageContainer);

		FileUploadField candidatePictureUploadField = new FileUploadField("candidatePicture");
		Label defaultPhotoRecommendation = new Label("defaultPhotoRecommendation", getString("candidateProfileDefaultPhotoWarning"));
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

		TextField<String> fullNameField = new TextField<>("name", new org.apache.wicket.model.PropertyModel<>(this, "fullName"));
		fullNameField.setConvertEmptyInputStringToNull(true);
		fullNameField.add(AttributeModifier.replace("maxlength", String.valueOf(MAX_NAME_LENGTH)));
		fullNameField.add(new NonBlankStringValidator("candidateProfileNameRequired"));
		fullNameField.add(new CandidateProfileNameValidator(MIN_NAME_LENGTH, MAX_NAME_LENGTH));
		candidateProfileForm.add(fullNameField);
		TextField<String> linkedinUrlField = new TextField<>("linkedinUrl", new org.apache.wicket.model.PropertyModel<>(this, "linkedinUrl"));
		linkedinUrlField.setConvertEmptyInputStringToNull(true);
		linkedinUrlField.add(new CandidateProfileLinkedinUrlValidator(MAX_LINKEDIN_LENGTH));
		candidateProfileForm.add(linkedinUrlField);

		AiAssistTextAreaPanel biographyEditor = new AiAssistTextAreaPanel(
				"biographyEditor",
				new org.apache.wicket.model.PropertyModel<>(this, "biography"),
				Model.of(getString("candidateProfileBiographyTitle")),
				this::addExternalFeedback,
				(originalText, instruction, styleContext) -> AppContext.getInstance().getPreNominationBeanRemote().improveCandidateTextWithStatus(
						token,
						originalText,
						instruction,
						styleContext,
						SecurityUtils.getClientIp()),
				isReadOnlyMode(),
				true,
				true,
				true,
				true,
				MAX_BIOGRAPHY_LENGTH,
				7);
		candidateProfileForm.add(biographyEditor);

		Button continueLaterButton = new Button("continueLaterButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (!ensureCanContinueLater()) {
					return;
				}
				if (!persistCandidateProfile(candidatePictureUploadField, false)) {
					return;
				}
				if (!updateTaskStatus(CandidateElectionTaskStatus.STARTED, false)) {
					return;
				}
				SecurityUtils.info(getString("candidateProfileContinueLaterSuccess"));
				setResponsePage(GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token));
			}
		};
		continueLaterButton.setVisible(canContinueLater());
		candidateProfileForm.add(continueLaterButton);

		candidateProfileForm.add(new BookmarkablePageLink<Void>("backButton", GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token)));

		candidateProfileForm.add(new Button("finishAndSendButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (!persistCandidateProfile(candidatePictureUploadField, true)) {
					return;
				}

				if (!updateTaskStatus(CandidateElectionTaskStatus.COMPLETED, true)) {
					return;
				}

				setResponsePage(GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token));
			}
		});
	}

	private void addExternalFeedback(AjaxRequestTarget target) {
		addPageFeedback(target);
	}

	private boolean persistCandidateProfile(FileUploadField candidatePictureUploadField, boolean completeSubmission) {
		FileUpload fileUpload = candidatePictureUploadField.getFileUpload();
		if (fileUpload != null && !processPictureUpload(fileUpload)) {
			return false;
		}

		if (!validateProfileData(completeSubmission)) {
			return false;
		}

		if (!hasProfileChanges()) {
			SecurityUtils.info(getString("candidateProfileNoChanges"));
			return true;
		}

		try {
			applyProfileChanges();
			Candidate updatedCandidate = AppContext.getInstance().getPreNominationBeanRemote().saveCandidateProfile(token, getCandidate(), CANDIDATE_LINK_ACTIVITY_ACTOR, SecurityUtils.getClientIp());
			if (updatedCandidate == null) {
				SecurityUtils.error(getString(TokenResourceKeys.CANDIDATE_PROFILE_PROCESSING_ERROR));
				return false;
			}

			AcceptNominationProfileCompletePanel.this.candidate = updatedCandidate;
			resetUploadedPicture();
			if (completeSubmission) {
				SecurityUtils.info(getString("candidateProfileEditSuccess"));
			}
			return true;
		} catch (Exception ex) {
			appLogger.error("Error updating candidate profile", ex);
			SecurityUtils.error(getString(TokenResourceKeys.CANDIDATE_PROFILE_PROCESSING_ERROR));
			return false;
		}
	}

	private boolean updateTaskStatus(CandidateElectionTaskStatus status, boolean showSuccessMessage) {
		if (status == CandidateElectionTaskStatus.STARTED && isSelectedTaskCompleted()) {
			getSession().warn(getString("acceptNominationTaskRestrictionAlreadyCompleted"));
			return false;
		}
		ElectionTaskKey selectedTaskKey = getSelectedTask() != null && getSelectedTask().getTaskKey() != null ? getSelectedTask().getTaskKey() : ElectionTaskKey.PROFILE;
		boolean updated = AppContext.getInstance().getPreNominationBeanRemote().updateCandidateTaskStatus(token, selectedTaskKey, status, SecurityUtils.getClientIp());
		if (!updated) {
			SecurityUtils.error(getString("acceptNominationTaskStatusChangeError"));
			return false;
		}

		if (showSuccessMessage) {
			SecurityUtils.info(resolveTaskStatusChangeMessage(status));
		}
		return true;
	}

	private void loadFormState() {
		if (getCandidate() == null) {
			fullName = "";
			linkedinUrl = "";
			biographySpanish = "";
			return;
		}
		fullName = StringUtils.defaultString(getCandidate().getName());
		linkedinUrl = StringUtils.defaultString(getCandidate().getLinkedinUrl());
		biographySpanish = StringUtils.defaultString(getCandidate().getBioSpanish());
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

	private boolean validateProfileData(boolean requireMandatoryFields) {
		boolean valid = true;

		String biographyValue = StringUtils.trimToEmpty(getBiography());
		if (requireMandatoryFields && StringUtils.isBlank(biographyValue)) {
			SecurityUtils.error(getString("candidateProfileBiographyRequired"));
			valid = false;
		}

		if (requireMandatoryFields && !hasPictureInformationAvailable()) {
			SecurityUtils.error(getString("candidateProfilePhotoRequired"));
			valid = false;
		}

		return valid;
	}

	private boolean hasPictureInformationAvailable() {
		if (uploadedPictureInfo != null && uploadedPictureInfo.length > 0) {
			return true;
		}
		if (getCandidate().getPictureInfo() != null && getCandidate().getPictureInfo().length > 0) {
			return true;
		}
		return loadDefaultImage() != null;
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

	private boolean hasProfileChanges() {
		Candidate currentCandidate = getCandidate();
		String selectedBiography = StringUtils.trimToNull(getBiography());

		if (!StringUtils.equals(StringUtils.trimToEmpty(currentCandidate.getName()), StringUtils.trimToEmpty(fullName))) {
			return true;
		}
		if (!StringUtils.equals(StringUtils.trimToNull(currentCandidate.getLinkedinUrl()), StringUtils.trimToNull(linkedinUrl))) {
			return true;
		}
		if (!StringUtils.equals(StringUtils.trimToNull(currentCandidate.getBioSpanish()), selectedBiography)) {
			return true;
		}
		if (StringUtils.trimToNull(currentCandidate.getBioEnglish()) != null) {
			return true;
		}
		if (StringUtils.trimToNull(currentCandidate.getBioPortuguese()) != null) {
			return true;
		}
		if (!currentCandidate.isOnlySp()) {
			return true;
		}
		if (uploadedPictureInfo != null && uploadedPictureInfo.length > 0) {
			return true;
		}

		return currentCandidate.getCandidateId() <= 0;
	}

	private void applyProfileChanges() {
		Candidate currentCandidate = getCandidate();
		String selectedBiography = StringUtils.trimToNull(getBiography());

		currentCandidate.setName(StringUtils.trimToEmpty(fullName));
		currentCandidate.setLinkedinUrl(StringUtils.trimToNull(linkedinUrl));
		currentCandidate.setBioSpanish(selectedBiography);
		currentCandidate.setBioEnglish(null);
		currentCandidate.setBioPortuguese(null);
		currentCandidate.setOnlySp(true);

		if (uploadedPictureInfo != null && uploadedPictureInfo.length > 0) {
			currentCandidate.setPictureInfo(uploadedPictureInfo);
			currentCandidate.setPictureName(uploadedPictureName);
			currentCandidate.setPictureExtension(uploadedPictureExtension);
			return;
		}

		if (currentCandidate.getPictureInfo() == null || currentCandidate.getPictureInfo().length == 0) {
			DefaultImage defaultImage = loadDefaultImage();
			if (defaultImage != null) {
				currentCandidate.setPictureInfo(defaultImage.content);
				currentCandidate.setPictureName("default-profile-picture." + defaultImage.extension);
				currentCandidate.setPictureExtension(defaultImage.extension);
			}
		}
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

	private void resetUploadedPicture() {
		uploadedPictureInfo = null;
		uploadedPictureName = null;
		uploadedPictureExtension = null;
	}

	private Candidate getCandidate() {
		return candidate;
	}

	public String getBiography() {
		return biographySpanish;
	}

	public void setBiography(String biography) {
		biographySpanish = biography;
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
