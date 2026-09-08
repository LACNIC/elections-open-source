package net.lacnic.elections.adminweb.validators;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.form.upload.FileUpload;

import net.lacnic.elections.utils.PublicPhotoResizeProcessor;

public final class CandidatePictureUploadValidator {

	private static final long STRICT_MAX_PICTURE_SIZE_BYTES = 512L * 1024L;
	private static final int STRICT_MAX_PICTURE_DIMENSION_PIXELS = 400;
	private static final Set<String> VALID_PICTURE_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png"));
	private static final String CUSTOM_PICTURE_NAME_PREFIX = "candidate_profile_custom_";
	private static final String CANDIDATE_PROCESSED_EXTENSION = "jpg";

	public static final long MAX_CANDIDATE_INPUT_SIZE_BYTES = PublicPhotoResizeProcessor.MAX_INPUT_SIZE_BYTES;

	private CandidatePictureUploadValidator() {
	}

	public static PictureUploadResult validateAndBuild(FileUpload fileUpload) {
		if (fileUpload == null) {
			return PictureUploadResult.invalid(FailureReason.PROCESSING_ERROR);
		}

		if (fileUpload.getSize() > STRICT_MAX_PICTURE_SIZE_BYTES) {
			return PictureUploadResult.invalid(FailureReason.INVALID_SIZE);
		}

		String fileName = StringUtils.defaultString(fileUpload.getClientFileName());
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		if (!VALID_PICTURE_EXTENSIONS.contains(extension)) {
			return PictureUploadResult.invalid(FailureReason.INVALID_FORMAT);
		}

		try {
			byte[] fileBytes = fileUpload.getBytes();
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
			if (image == null) {
				return PictureUploadResult.invalid(FailureReason.INVALID_FORMAT);
			}
			if (!isValidStrictImageDimensions(image)) {
				return PictureUploadResult.invalid(FailureReason.INVALID_SIZE);
			}

			String detectedFormat = detectImageFormat(fileBytes);
			if (StringUtils.isBlank(detectedFormat) || !isExtensionCompatibleWithDetectedFormat(extension, detectedFormat)) {
				return PictureUploadResult.invalid(FailureReason.INVALID_FORMAT);
			}

			String pictureName = buildCustomPictureName(detectedFormat);
			return PictureUploadResult.valid(fileBytes, pictureName, detectedFormat);
		} catch (Exception ex) {
			return PictureUploadResult.invalid(FailureReason.PROCESSING_ERROR);
		}
	}

	public static PictureUploadResult validateAndBuildForCandidate(FileUpload fileUpload) {
		CandidateUploadValidationResult candidateUploadValidationResult = validateAndExtractCandidateUpload(fileUpload);
		if (!candidateUploadValidationResult.isValid()) {
			return PictureUploadResult.invalid(candidateUploadValidationResult.getFailureReason());
		}

		try {
			PublicPhotoResizeProcessor.ProcessingResult processingResult = PublicPhotoResizeProcessor.process(
					candidateUploadValidationResult.getPictureInfo(),
					candidateUploadValidationResult.getFileName());
			if (!processingResult.isSuccess()) {
				return PictureUploadResult.invalid(mapResizeFailureReason(processingResult.getFailureReason()));
			}

			String pictureName = buildCustomPictureName(CANDIDATE_PROCESSED_EXTENSION);
			return PictureUploadResult.valid(processingResult.getOutputBytes(), pictureName, CANDIDATE_PROCESSED_EXTENSION);
		} catch (Exception ex) {
			return PictureUploadResult.invalid(FailureReason.PROCESSING_ERROR);
		}
	}

	private static FailureReason mapResizeFailureReason(PublicPhotoResizeProcessor.FailureReason failureReason) {
		if (failureReason == null) {
			return FailureReason.PROCESSING_ERROR;
		}
		switch (failureReason) {
		case INPUT_TOO_LARGE:
			return FailureReason.INVALID_SIZE;
		case INVALID_IMAGE_FORMAT:
			return FailureReason.INVALID_FORMAT;
		case EMPTY_UPLOAD:
		case OUTPUT_TOO_LARGE:
		case PROCESSING_ERROR:
		default:
			return FailureReason.PROCESSING_ERROR;
		}
	}

	private static boolean isValidStrictImageDimensions(BufferedImage image) {
		return image.getWidth() <= STRICT_MAX_PICTURE_DIMENSION_PIXELS && image.getHeight() <= STRICT_MAX_PICTURE_DIMENSION_PIXELS;
	}

	private static CandidateUploadValidationResult validateAndExtractCandidateUpload(FileUpload fileUpload) {
		if (fileUpload == null) {
			return CandidateUploadValidationResult.invalid(FailureReason.PROCESSING_ERROR);
		}

		if (fileUpload.getSize() > MAX_CANDIDATE_INPUT_SIZE_BYTES) {
			return CandidateUploadValidationResult.invalid(FailureReason.INVALID_SIZE);
		}

		String fileName = StringUtils.defaultString(fileUpload.getClientFileName());
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		if (!VALID_PICTURE_EXTENSIONS.contains(extension)) {
			return CandidateUploadValidationResult.invalid(FailureReason.INVALID_FORMAT);
		}

		try {
			byte[] fileBytes = fileUpload.getBytes();
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
			if (image == null) {
				return CandidateUploadValidationResult.invalid(FailureReason.INVALID_FORMAT);
			}

			String detectedFormat = detectImageFormat(fileBytes);
			if (StringUtils.isBlank(detectedFormat) || !isExtensionCompatibleWithDetectedFormat(extension, detectedFormat)) {
				return CandidateUploadValidationResult.invalid(FailureReason.INVALID_FORMAT);
			}
			return CandidateUploadValidationResult.valid(fileBytes, fileName);
		} catch (Exception ex) {
			return CandidateUploadValidationResult.invalid(FailureReason.PROCESSING_ERROR);
		}
	}

	private static String detectImageFormat(byte[] imageBytes) {
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes))) {
			if (imageInputStream == null) {
				return null;
			}
			Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
			if (!readers.hasNext()) {
				return null;
			}
			String format = StringUtils.lowerCase(readers.next().getFormatName());
			if ("jpeg".equals(format)) {
				return "jpg";
			}
			return format;
		} catch (IOException ex) {
			return null;
		}
	}

	private static boolean isExtensionCompatibleWithDetectedFormat(String extension, String detectedFormat) {
		if (StringUtils.isBlank(extension) || StringUtils.isBlank(detectedFormat)) {
			return false;
		}
		if ("jpg".equals(detectedFormat)) {
			return "jpg".equals(extension) || "jpeg".equals(extension);
		}
		return detectedFormat.equals(extension);
	}

	private static String buildCustomPictureName(String extension) {
		return CUSTOM_PICTURE_NAME_PREFIX + System.currentTimeMillis() + "." + StringUtils.defaultIfBlank(extension, "jpg");
	}

	public enum FailureReason {
		INVALID_FORMAT,
		INVALID_SIZE,
		PROCESSING_ERROR
	}

	public static final class PictureUploadResult {
		private final boolean valid;
		private final FailureReason failureReason;
		private final byte[] pictureInfo;
		private final String pictureName;
		private final String pictureExtension;

		private PictureUploadResult(boolean valid, FailureReason failureReason, byte[] pictureInfo, String pictureName, String pictureExtension) {
			this.valid = valid;
			this.failureReason = failureReason;
			this.pictureInfo = pictureInfo;
			this.pictureName = pictureName;
			this.pictureExtension = pictureExtension;
		}

		static PictureUploadResult valid(byte[] pictureInfo, String pictureName, String pictureExtension) {
			return new PictureUploadResult(true, null, pictureInfo, pictureName, pictureExtension);
		}

		static PictureUploadResult invalid(FailureReason reason) {
			return new PictureUploadResult(false, reason, null, null, null);
		}

		public boolean isValid() {
			return valid;
		}

		public FailureReason getFailureReason() {
			return failureReason;
		}

		public byte[] getPictureInfo() {
			return pictureInfo;
		}

		public String getPictureName() {
			return pictureName;
		}

		public String getPictureExtension() {
			return pictureExtension;
		}
	}

	private static final class CandidateUploadValidationResult {
		private final boolean valid;
		private final FailureReason failureReason;
		private final byte[] pictureInfo;
		private final String fileName;

		private CandidateUploadValidationResult(boolean valid, FailureReason failureReason, byte[] pictureInfo, String fileName) {
			this.valid = valid;
			this.failureReason = failureReason;
			this.pictureInfo = pictureInfo;
			this.fileName = fileName;
		}

		static CandidateUploadValidationResult valid(byte[] pictureInfo, String fileName) {
			return new CandidateUploadValidationResult(true, null, pictureInfo, fileName);
		}

		static CandidateUploadValidationResult invalid(FailureReason failureReason) {
			return new CandidateUploadValidationResult(false, failureReason, null, null);
		}

		boolean isValid() {
			return valid;
		}

		FailureReason getFailureReason() {
			return failureReason;
		}

		byte[] getPictureInfo() {
			return pictureInfo;
		}

		String getFileName() {
			return fileName;
		}
	}
}
