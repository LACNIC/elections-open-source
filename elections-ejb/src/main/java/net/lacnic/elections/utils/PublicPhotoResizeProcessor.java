package net.lacnic.elections.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public final class PublicPhotoResizeProcessor {

	public static final int TARGET_SIZE_PIXELS = 200;
	public static final long MAX_INPUT_SIZE_BYTES = 9_900_000L;
	public static final long MAX_OUTPUT_SIZE_BYTES = 400L * 1024L;

	private static final float INITIAL_JPEG_QUALITY = 0.95f;
	private static final float MIN_JPEG_QUALITY = 0.35f;
	private static final float JPEG_QUALITY_STEP = 0.05f;

	private PublicPhotoResizeProcessor() {
	}

	public static ProcessingResult process(byte[] inputBytes, String originalFileName) {
		if (inputBytes == null || inputBytes.length == 0) {
			return ProcessingResult.failure(FailureReason.EMPTY_UPLOAD);
		}
		if (inputBytes.length > MAX_INPUT_SIZE_BYTES) {
			return ProcessingResult.failure(FailureReason.INPUT_TOO_LARGE);
		}
		try {
			BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(inputBytes));
			if (sourceImage == null) {
				return ProcessingResult.failure(FailureReason.INVALID_IMAGE_FORMAT);
			}

			BufferedImage squaredImage = cropToCenteredSquare(sourceImage);
			BufferedImage resizedImage = resizeToTargetSize(squaredImage);
			byte[] outputBytes = compressAsJpegWithinLimit(resizedImage);
			if (outputBytes == null || outputBytes.length == 0) {
				return ProcessingResult.failure(FailureReason.PROCESSING_ERROR);
			}
			if (outputBytes.length > MAX_OUTPUT_SIZE_BYTES) {
				return ProcessingResult.failure(FailureReason.OUTPUT_TOO_LARGE);
			}

			return ProcessingResult.success(outputBytes, buildOutputFileName(originalFileName));
		} catch (Exception ex) {
			return ProcessingResult.failure(FailureReason.PROCESSING_ERROR);
		}
	}

	private static BufferedImage cropToCenteredSquare(BufferedImage source) {
		int size = Math.min(source.getWidth(), source.getHeight());
		int x = (source.getWidth() - size) / 2;
		int y = (source.getHeight() - size) / 2;
		return source.getSubimage(x, y, size, size);
	}

	private static BufferedImage resizeToTargetSize(BufferedImage source) {
		BufferedImage resized = new BufferedImage(TARGET_SIZE_PIXELS, TARGET_SIZE_PIXELS, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = resized.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, TARGET_SIZE_PIXELS, TARGET_SIZE_PIXELS);
			graphics.drawImage(source, 0, 0, TARGET_SIZE_PIXELS, TARGET_SIZE_PIXELS, null);
		} finally {
			graphics.dispose();
		}
		return resized;
	}

	private static byte[] compressAsJpegWithinLimit(BufferedImage image) throws Exception {
		byte[] lastAttempt = null;
		for (float quality = INITIAL_JPEG_QUALITY; quality >= MIN_JPEG_QUALITY; quality -= JPEG_QUALITY_STEP) {
			lastAttempt = writeJpeg(image, quality);
			if (lastAttempt.length <= MAX_OUTPUT_SIZE_BYTES) {
				return lastAttempt;
			}
		}
		return lastAttempt;
	}

	private static byte[] writeJpeg(BufferedImage image, float quality) throws Exception {
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
		if (!writers.hasNext()) {
			throw new IllegalStateException("No JPEG writer available");
		}
		ImageWriter writer = writers.next();
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
			writer.setOutput(imageOutputStream);
			ImageWriteParam writeParam = writer.getDefaultWriteParam();
			if (writeParam.canWriteCompressed()) {
				writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				writeParam.setCompressionQuality(quality);
			}
			writer.write(null, new IIOImage(image, null, null), writeParam);
			return outputStream.toByteArray();
		} finally {
			writer.dispose();
		}
	}

	private static String buildOutputFileName(String originalFileName) {
		String baseName = "photo";
		if (originalFileName != null && !originalFileName.trim().isEmpty()) {
			String fileName = originalFileName.trim();
			int slashIdx = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
			if (slashIdx >= 0 && slashIdx + 1 < fileName.length()) {
				fileName = fileName.substring(slashIdx + 1);
			}
			int dotIdx = fileName.lastIndexOf('.');
			baseName = dotIdx > 0 ? fileName.substring(0, dotIdx) : fileName;
			if (baseName.trim().isEmpty()) {
				baseName = "photo";
			}
		}
		return baseName + "_200x200.jpg";
	}

	public enum FailureReason {
		EMPTY_UPLOAD,
		INPUT_TOO_LARGE,
		INVALID_IMAGE_FORMAT,
		OUTPUT_TOO_LARGE,
		PROCESSING_ERROR
	}

	public static final class ProcessingResult {
		private final boolean success;
		private final FailureReason failureReason;
		private final byte[] outputBytes;
		private final String outputFileName;
		private final String contentType;
		private final int width;
		private final int height;

		private ProcessingResult(boolean success, FailureReason failureReason, byte[] outputBytes, String outputFileName, String contentType, int width, int height) {
			this.success = success;
			this.failureReason = failureReason;
			this.outputBytes = outputBytes;
			this.outputFileName = outputFileName;
			this.contentType = contentType;
			this.width = width;
			this.height = height;
		}

		static ProcessingResult success(byte[] outputBytes, String outputFileName) {
			return new ProcessingResult(true, null, outputBytes, outputFileName, "image/jpeg", TARGET_SIZE_PIXELS, TARGET_SIZE_PIXELS);
		}

		static ProcessingResult failure(FailureReason failureReason) {
			return new ProcessingResult(false, failureReason, null, null, null, 0, 0);
		}

		public boolean isSuccess() {
			return success;
		}

		public FailureReason getFailureReason() {
			return failureReason;
		}

		public byte[] getOutputBytes() {
			return outputBytes;
		}

		public String getOutputFileName() {
			return outputFileName;
		}

		public String getContentType() {
			return contentType;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
	}
}
