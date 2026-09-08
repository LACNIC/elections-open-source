package net.lacnic.elections.adminweb.wicket.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.annotation.adapters.HexBinaryAdapter;

public class UtilsString {

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

		public static String wantHashMd5(String pass) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] buffer = pass.getBytes(StandardCharsets.UTF_8);
			md.update(buffer);
			byte[] digest = md.digest();
			HexBinaryAdapter hex = new HexBinaryAdapter();
			return hex.marshal(digest).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			appLogger.error(e.getMessage(), e);
			throw new IllegalStateException("Cannot hash password with SHA-256", e);
		}
	}

}
