package net.lacnic.siselecciones.admin.web.commons;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class UtilsString {

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public static String wantHashMd5(String pass) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] buffer = pass.getBytes();
			md.update(buffer);
			byte[] digest = md.digest();
			HexBinaryAdapter hex = new HexBinaryAdapter();
			return hex.marshal(digest).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			appLogger.error(e);
		}
		return pass;
	}

}
