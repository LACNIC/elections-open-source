package net.lacnic.elections.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase utilizada para la generación de UUIDs para los códigos de votación
 * 
 */
public class StringUtils {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

	private StringUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String createSecureToken() {
		return UUID.randomUUID().toString().concat(UUID.randomUUID().toString());
	}

	public static String createSmallToken() {
		return UUID.randomUUID().toString();
	}

	public static String arrayToCommaSeparated(String[] input) {
		if (input != null) {
			StringBuilder buff = new StringBuilder();
			String sep = "";
			for (String str : input) {
				buff.append(sep);
				buff.append(str);
				sep = ",";
			}
			return buff.toString();
		}
		return "";
	}

	public static String[] stringsToArray(String... input) {
		List<String> list = new ArrayList<>();
		for (String value : input) {
			list.add(value);
		}
		return list.toArray(new String[list.size()]);
	}

	public static String[] commaSeparatedToArray(String input) {
		if (input != null) {
			return input.split(",");
		}
		return null;
	}

	public static String sha256(String input) {
		String res = "";
		try {
			MessageDigest algorithm = MessageDigest.getInstance("SHA-256");
			algorithm.reset();
			algorithm.update(input.getBytes(StandardCharsets.UTF_8));
			byte[] digest = algorithm.digest();
			String tmp = "";
			StringBuilder tmpBld = new StringBuilder();
			for (int i = 0; i < digest.length; i++) {
				tmp = (Integer.toHexString(0xFF & digest[i]));
				if (tmp.length() == 1) {
					tmpBld.append("0" + tmp);
				} else {
					tmpBld.append(tmp);
				}
			}
			res = tmpBld.toString();
		} catch (NoSuchAlgorithmException ex) {
			appLogger.error(ex.getMessage(), ex);
		}
		return res;
	}

}
