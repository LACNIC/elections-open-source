package net.lacnic.elections.adminweb.ui.admin.election.candidates;

final class ProctorioFileSupport {

	private static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";

	private ProctorioFileSupport() {
	}

	static String resolveContentType(byte[] content) {
		if (startsWith(content, (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47,
				(byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A)) {
			return "image/png";
		}
		if (startsWith(content, (byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46)) {
			return "application/pdf";
		}
		if (startsWith(content, (byte) 0xFF, (byte) 0xD8, (byte) 0xFF)) {
			return "image/jpeg";
		}
		if (startsWith(content, (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38)) {
			return "image/gif";
		}
		if (startsWith(content, (byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04)) {
			return "application/zip";
		}
		return CONTENT_TYPE_OCTET_STREAM;
	}

	static String resolveFileName(long candidateId, byte[] content) {
		return "proctorio-result-" + candidateId + "." + resolveExtension(resolveContentType(content));
	}

	private static String resolveExtension(String contentType) {
		switch (contentType) {
		case "image/png":
			return "png";
		case "application/pdf":
			return "pdf";
		case "image/jpeg":
			return "jpg";
		case "image/gif":
			return "gif";
		case "application/zip":
			return "zip";
		default:
			return "bin";
		}
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
