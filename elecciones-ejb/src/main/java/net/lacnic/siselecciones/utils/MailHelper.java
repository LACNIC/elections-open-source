package net.lacnic.siselecciones.utils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Clase utilizada para el manejo de e-mails, envíos, autenticación en el
 * servidor de mails, etc
 * 
 * @author Antonymous
 *
 */
public class MailHelper {

	
	private static String smtpHost;
	private static String user;
	private static String pass;

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	
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

	
	public static Session initSession(Properties props) {
		props.put("mail.smtp.host", getSmtpHost());
		props.put("mail.smtp.auth", "true");
		return Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(getUser(), getPass());
			}
		});
		 
	}

	public static boolean sendMail(Properties props, String fromString, String to, String cc, String bcc, String subject, String body) throws Exception {
		return sendMail(initSession(props), fromString, to, cc, bcc, subject, body);
	}

	public static boolean sendMail(Session session, String fromString, String to, String cc, String bcc, String subject, String body) throws Exception {
		try {
			if (body.contains("$usuario.") || body.contains("$eleccion.") || body.contains("$auditor."))
				return false;
			
			Message msg = new MimeMessage(session);
			msg.setFrom(getEmailAddress(fromString));

			int toLen = (to != null) ? 1 : 0;
			int ccLen = (cc != null) ? 1 : 0;
			int bccLen = (bcc != null) ? 1 : 0;

			if (toLen + ccLen + bccLen == 0)
				throw new MessagingException("no recipients");
			if (to != null && !"".equals(to))
				msg.addRecipient(javax.mail.Message.RecipientType.TO, getEmailAddress(to));
			if (cc != null && !"".equals(cc))
				msg.addRecipient(javax.mail.Message.RecipientType.CC, getEmailAddress(cc));
			if (bcc != null && !"".equals(bcc))
				msg.addRecipient(javax.mail.Message.RecipientType.BCC, getEmailAddress(bcc));

			msg.setSubject(subject);
			msg.setContent(body, "text/plain; charset=UTF-8");
			msg.setSentDate(new Date());
			msg.setHeader("X-ELECCIONES", getHeaderMailer(subject));

			Transport.send(msg);

			appLogger.info("Message sent OK." + to);
			return true;

		} catch (Exception e) {
			return false;
		}

	}

	private static String getHeaderMailer(String subject) {
		SimpleDateFormat formatDateFecha = new SimpleDateFormat("dd/MM/yyyy");
		return StringUtils.md5(subject.concat(formatDateFecha.format(new Date())));
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