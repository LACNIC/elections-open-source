package net.lacnic.siselecciones.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Clase utilizada para la generación de UUIDs para los códigos de votación
 * 
 * @author Antonymous
 *
 */
public class StringUtils {
	
	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	
	private StringUtils() {
	    throw new IllegalStateException("Utility class");
	  }

	public static String createSecureToken() {
		return UUID.randomUUID().toString().concat(UUID.randomUUID().toString());
	}

	public static String createSmallToken() {
		return UUID.randomUUID().toString();
	}

	public static String array2commaSeparated(String[] values) {
		if (values != null) {
			StringBuilder buff = new StringBuilder();
			String sep = "";
			for (String str : values) {
				buff.append(sep);
				buff.append(str);
				sep = ",";
			}
			return buff.toString();
		}
		return "";
	}

	public static String[] strings2Array(String... values) {
		List<String> list = new ArrayList<>();
		for (String value : values) {
			list.add(value);
		}
		return list.toArray(new String[list.size()]);
	}

	public static String[] commaSeparated2Array(String value) {
		if (value != null) {
			return value.split(",");
		}
		return null;
	}

	public static String md5(String input) {
		String res = "";
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(input.getBytes());
			byte[] md5 = algorithm.digest();
			String tmp = "";
			StringBuilder tmpBld = new StringBuilder();
			for (int i = 0; i < md5.length; i++) {
				tmp = (Integer.toHexString(0xFF & md5[i]));
				if (tmp.length() == 1) {
					tmpBld.append("0" + tmp);
				} else {
					tmpBld.append(tmp);
				}
			}
			res = tmpBld.toString();
		} catch (NoSuchAlgorithmException ex) {
			appLogger.error(ex);
		}
		return res;
	}

}
