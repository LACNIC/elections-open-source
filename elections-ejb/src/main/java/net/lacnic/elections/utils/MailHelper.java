package net.lacnic.elections.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Util class for email handling (sending, authentication, etc)
 *
 */
public class MailHelper {

	private static String smtpHost;
	private static String user;
	private static String pass;

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

	private MailHelper() {
	}

	public static String getSmtpHost() {
		return smtpHost;
	}

	public static void setSmtpHost(String smtpHost) {
		MailHelper.smtpHost = smtpHost;
	}

	public static String getUser() {
		return user;
	}

	public static void setUser(String user) {
		MailHelper.user = user;
	}

	public static String getPass() {
		return pass;
	}

	public static void setPass(String pass) {
		MailHelper.pass = pass;
	}

	public static Session initSession() {

		setSmtpHost(EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.EMAIL_HOST));
		setUser(EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.EMAIL_USER));
		setPass(EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.EMAIL_PASSWORD));

		Properties props = new Properties(System.getProperties());
		String configDir = System.getProperty("jboss.server.config.dir");
		if (configDir != null && !configDir.trim().isEmpty()) {
			Path emailPropsPath = Paths.get(configDir, "email.properties");
			if (Files.isRegularFile(emailPropsPath)) {
				try (InputStream input = new FileInputStream(emailPropsPath.toFile())) {
					props.load(input);
				} catch (Exception e) {
					appLogger.warn("Could not load email.properties from {}. Using system properties only.", configDir);
				}
			}
		}
		return initSession(props);
	}

	private static Session initSession(Properties props) {
		props.put("mail.smtp.host", getSmtpHost());
		props.put("mail.smtp.auth", "true");
		return Session.getInstance(props, new jakarta.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(getUser(), getPass());
			}
		});

	}

	public static boolean sendMail(Session session, String fromString, String to, String cc, String bcc, String replyTo, String subject, String body) {
		try {
			if (body.contains("$user.") || body.contains("$election.") || body.contains("$auditor."))
				return false;

			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(getEmailAddress(fromString));
			if (replyTo != null && !replyTo.trim().isEmpty()) {
				msg.setReplyTo(new Address[] { getEmailAddress(replyTo) });
			}

			int toLen = (to != null) ? 1 : 0;
			int ccLen = (cc != null) ? 1 : 0;
			int bccLen = (bcc != null) ? 1 : 0;

			if (toLen + ccLen + bccLen == 0)
				throw new MessagingException("no recipients");
			if (to != null && !"".equals(to))
				msg.addRecipient(jakarta.mail.Message.RecipientType.TO, getEmailAddress(to));
			if (cc != null && !"".equals(cc))
				msg.addRecipient(jakarta.mail.Message.RecipientType.CC, getEmailAddress(cc));
			if (bcc != null && !"".equals(bcc))
				msg.addRecipient(jakarta.mail.Message.RecipientType.BCC, getEmailAddress(bcc));

			msg.setSubject(subject);
			msg.setContent(body, "text/plain; charset=UTF-8");
			msg.setSentDate(new Date());
			msg.setHeader("X-ELECCIONES", getHeaderMailer(subject));

			Transport.send(msg);

			appLogger.info("Message sent OK to " + to);
			return true;

		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	private static String getHeaderMailer(String subject) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return StringUtils.sha256(subject.concat(sdf.format(new Date())));
	}

	private static Address getEmailAddress(String fromString) throws MessagingException, UnsupportedEncodingException {
		String[] from = fromString.split(",");
		if (from.length == 2)
			return new InternetAddress(from[0], from[1]);
		else if (from.length == 1)
			return new InternetAddress(from[0]);
		return null;
	}

}
