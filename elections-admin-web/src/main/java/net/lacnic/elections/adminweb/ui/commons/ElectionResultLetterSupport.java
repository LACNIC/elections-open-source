package net.lacnic.elections.adminweb.ui.commons;

import net.lacnic.elections.domain.LanguageCode;

public final class ElectionResultLetterSupport {

	private static final String CONTENT_TYPE_PDF = "application/pdf";

	private ElectionResultLetterSupport() {
	}

	public static String resolveContentType(byte[] content) {
		return CONTENT_TYPE_PDF;
	}

	public static String resolveFileName(long electionId, LanguageCode languageCode, byte[] content) {
		LanguageCode resolvedLanguageCode = languageCode != null ? languageCode : LanguageCode.SP;
		String languageSuffix;
		switch (resolvedLanguageCode) {
		case EN:
			languageSuffix = "EN";
			break;
		case PT:
			languageSuffix = "PT";
			break;
		case SP:
		default:
			languageSuffix = "ES";
			break;
		}
		return "CartaComisionElectoral" + languageSuffix + "-eleccion-" + electionId + ".pdf";
	}

	public static String appendUniqueSuffix(String fileName, long uniqueValue) {
		return appendUniqueSuffix(fileName, String.valueOf(uniqueValue));
	}

	public static String appendUniqueSuffix(String fileName, String uniqueValue) {
		if (fileName == null || fileName.trim().isEmpty() || uniqueValue == null || uniqueValue.trim().isEmpty()) {
			return fileName;
		}
		int extensionIndex = fileName.toLowerCase().lastIndexOf(".pdf");
		if (extensionIndex < 0) {
			return fileName + "_" + uniqueValue;
		}
		return fileName.substring(0, extensionIndex) + "_" + uniqueValue + fileName.substring(extensionIndex);
	}

	public static boolean isPdf(byte[] content) {
		return startsWith(content, (byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46);
	}

	private static boolean startsWith(byte[] content, byte... signature) {
		if (content == null || signature == null || content.length < signature.length) {
			return false;
		}
		for (int i = 0; i < signature.length; i++) {
			if (content[i] != signature[i]) {
				return false;
			}
		}
		return true;
	}
}
