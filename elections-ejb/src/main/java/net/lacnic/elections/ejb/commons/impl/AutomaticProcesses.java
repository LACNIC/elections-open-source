package net.lacnic.elections.ejb.commons.impl;

import java.util.List;

import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.TransactionTimeout;

import net.lacnic.elections.domain.Email;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.MailHelper;


@Stateless
public class AutomaticProcesses {

	private static boolean running = false;
	private static int attempts = 0;

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");


	/**
	 * Get the list of emails to send and tries to send them. Adds the ones who fail to the email wit problemas list which are re-scheduled to be sent later.
	 */
	@TransactionTimeout(35000)
	@Schedules({ @Schedule(second = "0", minute = "*/10", hour = "*", persistent = false) })
	public void sendEmail() {
		try {
			if (!running) {
				running = true;
				appLogger.info("START Execute Elections Mails Sending");
				List<Email> emails = EJBFactory.getInstance().getMailsSendingEJB().getEmailsToSend();
				String host = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.EMAIL_HOST);
				String user = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.EMAIL_USER);
				String password = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.EMAIL_PASSWORD);
				MailHelper.setSmtpHost(host);
				MailHelper.setUser(user);
				MailHelper.setPass(password);
				Session session = MailHelper.initSession();

				for (int i = 0; i < emails.size(); i++) {
					Email email = emails.get(i);
					try {
						appLogger.info("SENDING EMAIL " + email.getSubject() + " to " + email.getRecipients());
						if(MailHelper.sendMail(session, email.getSender(), email.getRecipients(), email.getCc(), email.getBcc(), email.getSubject(), email.getBody())) {
							// Sent OK, mark email as sent
							EJBFactory.getInstance().getMailsSendingEJB().markEmailAsSent(email);
						} else {
							appLogger.error("ERROR sending mail to " + email.getRecipients());
						}
						if (i % 500 == 0)
							Thread.sleep(5000);

					} catch (MessagingException e) {
						appLogger.error("ERROR sending mail to " + email.getRecipients());
						appLogger.error(e);
					} catch (Exception e) {
						appLogger.error("ERROR sending mail to " + email.getRecipients());
						appLogger.error(e);
					}
				}
				appLogger.info("END Execute Elections Mails Sending");
				running = false;
			} else {
				if (attempts < 8) {
					attempts = attempts + 1;
					appLogger.info("WARM Currently running, sending rescheduled for " + attempts + " time.");
				} else {
					appLogger.info("WARM Last time this job is rescheduled, " + attempts + " attempt(s)");
					attempts = 0;
					running = false;
				}
			}
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	/**
	 * Calculates and updates the health check information
	 */
	@Schedules({ @Schedule(second = "0", minute = "*/10", hour = "*", persistent = false) })
	public void updateHealthCheckData() {
		EJBFactory.getInstance().getElectionsMonitorEJB().updateHealthCheckData();
	}

	
	/**
	 * Moves all the email to the history tables 
	 */
	@TransactionTimeout(35000)
	@Schedules({ @Schedule(second = "0", minute = "15", hour = "4", persistent = false) })
	public void moveEmailsToHistory() {
		EJBFactory.getInstance().getMailsSendingEJB().moveEmailsToHistory();
	}

	
	/**
	 * Purge the email tables.
	 */
	@TransactionTimeout(35000)
	@Schedules({ @Schedule(second = "0", minute = "15", hour = "5", persistent = false) })
	public void purgeTables() {
		EJBFactory.getInstance().getMailsSendingEJB().purgeTables();
	}


	/**
	 * Get the amount of attempts
	 * 
	 * @return returns an integer with the amount of attempts
	 */
	public static int getAttempts() {
		return attempts;
	}

	/**
	 * Updates the amount of attempts of automatic proceses
	 * 
	 * @param attempts
	 */
	public static void setAttempts(int attempts) {
		AutomaticProcesses.attempts = attempts;
	}

}
