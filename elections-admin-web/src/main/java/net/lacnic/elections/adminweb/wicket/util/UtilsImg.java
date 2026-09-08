package net.lacnic.elections.adminweb.wicket.util;

import org.apache.commons.lang3.StringUtils;

public final class UtilsImg {

	private UtilsImg() {
		throw new IllegalStateException("Utility class");
	}

	public static String extractExtension(String filePath) {
		if (StringUtils.isBlank(filePath)) {
			return "jpg";
		}
		int lastSeparator = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
		String fileName = lastSeparator >= 0 && lastSeparator < filePath.length() - 1 ? filePath.substring(lastSeparator + 1) : filePath;
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
			return fileName.substring(dotIndex + 1).toLowerCase();
		}
		return "jpg";
	}

}
