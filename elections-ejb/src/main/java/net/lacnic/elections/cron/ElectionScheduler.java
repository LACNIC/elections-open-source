package net.lacnic.elections.cron;

import java.util.List;
import java.util.UUID;

import org.jboss.ejb3.annotation.TransactionTimeout;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.mail.Session;
import net.lacnic.elections.campus.CampusClient;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.ElectionsCaches;
import net.lacnic.elections.utils.MailHelper;
import net.lacnic.elections.utils.OpenAiClient;

@Stateless
public class ElectionScheduler {

	private static int attempts = 0;
	private static final String CAMPUS_CHECK_BUTTON_COURSE = "COURSE";
	private static final String CAMPUS_CHECK_BUTTON_EVALUATION = "EVALUATION";

	private static final int EMAIL_BATCH_SIZE = 3000;
	private static final int DEFAULT_CAMPUS_PROGRESS_CHECK_COURSE_RATE_LIMIT_CACHE_RESET_HOURS = 6;
	private static final int DEFAULT_CAMPUS_PROGRESS_CHECK_EVALUATION_RATE_LIMIT_CACHE_RESET_HOURS = 6;
	private static final int DEFAULT_AI_TEXT_IMPROVEMENT_RATE_LIMIT_CACHE_RESET_HOURS = 1;
	private static final int DEFAULT_PUBLIC_FAILED_ACCESS_RATE_LIMIT_CACHE_RESET_HOURS = 1;
	private static final int DEFAULT_LOGIN_CAPTCHA_RATE_LIMIT_CACHE_RESET_HOURS = 6;

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

	/**
	 * Get the list of emails to send and tries to send them.
	 */
	@TransactionTimeout(35000)
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	public void sendEmail() {
		try {
			List<Email> emails = EJBFactory.getInstance().getMailsSendingEJB().getEmailsToSend(EMAIL_BATCH_SIZE);
			if (emails == null || emails.isEmpty()) {
				return;
			}

			int prioritizedCount = 0;
			int regularCount = 0;
			Session session = MailHelper.initSession();

			for (Email email : emails) {
				if (email == null) {
					continue;
				}
				try {
					if (Boolean.TRUE.equals(email.getPrioritized())) {
						prioritizedCount++;
					} else {
						regularCount++;
					}
					appLogger.info("SENDING EMAIL " + email.getSubject() + " to " + email.getRecipients());
					String replyTo = resolveReplyTo(email);
					if (MailHelper.sendMail(session, email.getSender(), email.getRecipients(), email.getCc(), email.getBcc(), replyTo, email.getSubject(), email.getBody())) {
						// Sent OK, mark email as sent
						EJBFactory.getInstance().getMailsSendingEJB().markEmailAsSent(email);
					} else {
						appLogger.error("ERROR sending mail to " + email.getRecipients());
					}
				} catch (Exception e) {
					appLogger.error("ERROR sending mail to " + email.getRecipients());
					appLogger.error(e.getMessage());
				}
			}
			appLogger.info("Email batch processed: total={}, prioritized={}, regular={}", emails.size(), prioritizedCount, regularCount);
		} catch (Exception e1) {
			appLogger.error(e1.getMessage());
		}
	}

	/**
	 * Calculates and updates the health check information
	 */
	@Schedule(second = "0", minute = "*/5", hour = "*", persistent = false)
	public void updateHealthCheckData() {
		EJBFactory.getInstance().getElectionsMonitorEJB().updateHealthCheckData();
	}

	@Schedule(second = "0", minute = "*/5", hour = "*", persistent = false)
	@TransactionTimeout(35000)
	public void refreshOpenPublicElectionSnapshotCache() {
		EJBFactory.getInstance().getElectionsMonitorEJB().refreshOpenPublicElectionSnapshotCache();
	}

	@Schedule(second = "0", minute = "*/5", hour = "*", persistent = false)
	@TransactionTimeout(35000)
	public void refreshPublicElectionsSnapshotCache() {
		EJBFactory.getInstance().getElectionsMonitorEJB().refreshPublicElectionsSnapshotCache();
	}

	/**
	 * Moves all the email to the history tables
	 */
	@TransactionTimeout(35000)
	@Schedule(second = "0", minute = "15", hour = "4", persistent = false)
	public void moveEmailsToHistory() {
		EJBFactory.getInstance().getMailsSendingEJB().moveEmailsToHistory();
	}

	/**
	 * Purge the email tables.
	 */
	@TransactionTimeout(35000)
	@Schedule(second = "0", minute = "15", hour = "5", persistent = false)
	public void purgeTables() {
		EJBFactory.getInstance().getMailsSendingEJB().purgeTables();
	}

	@TransactionTimeout(9000)
	@Schedule(second = "0", minute = "*/5", hour = "*", persistent = false, info = "updateCampusCandidateProgress")
	public void updateCampusCandidateProgress() {
		if (!CampusClient.isCampusIntegrationEnabled()) {
			return;
		}
		appLogger.info("** START updateCampusCandidateProgress");
		EJBFactory.getInstance().getElectionsManagerEJB().verifyCampusCourseAccessForCandidates();
		EJBFactory.getInstance().getElectionsManagerEJB().verifyCampusEvaluationGradesForCandidates();
		appLogger.info("** END updateCampusCandidateProgress");
	}

	@Schedule(second = "0", minute = "0", hour = "*", persistent = false, info = "clearAiTextImprovementRateLimitCache")
	public void clearAiTextImprovementRateLimitCache() {
		if (!OpenAiClient.isTextImprovementEnabled()) {
			return;
		}
		int intervalHours = resolveAiTextImprovementRateLimitCacheResetHours();
		int currentHourUtc = DateTime.now(DateTimeZone.UTC).getHourOfDay();
		if (currentHourUtc % intervalHours != 0) {
			return;
		}

		ElectionsCaches.clearAiTextImprovementRateLimitCache();
		appLogger.info("AI text improvement rate limit cache cleared. intervalHours={}, currentHourUtc={}", intervalHours, currentHourUtc);
	}

	@Schedule(second = "0", minute = "0", hour = "*", persistent = false, info = "clearCampusProgressCheckRateLimitCache")
	public void clearCampusProgressCheckRateLimitCache() {
		if (!CampusClient.isCampusIntegrationEnabled()) {
			return;
		}
		int currentHourUtc = DateTime.now(DateTimeZone.UTC).getHourOfDay();
		clearCampusProgressCheckRateLimitCacheByButton(CAMPUS_CHECK_BUTTON_COURSE, resolveCampusCourseProgressCheckRateLimitCacheResetHours(), currentHourUtc);
		clearCampusProgressCheckRateLimitCacheByButton(CAMPUS_CHECK_BUTTON_EVALUATION, resolveCampusEvaluationProgressCheckRateLimitCacheResetHours(), currentHourUtc);
	}

	@Schedule(second = "0", minute = "0", hour = "*", persistent = false, info = "clearPublicFailedAccessRateLimitCache")
	public void clearPublicFailedAccessRateLimitCache() {
		int intervalHours = resolvePublicFailedAccessRateLimitCacheResetHours();
		int currentHourUtc = DateTime.now(DateTimeZone.UTC).getHourOfDay();
		if (currentHourUtc % intervalHours != 0) {
			return;
		}

		ElectionsCaches.clearPublicFailedAccessRateLimitCache();
		appLogger.info("Public failed access rate limit cache cleared. intervalHours={}, currentHourUtc={}", intervalHours, currentHourUtc);
	}

	@Schedule(second = "0", minute = "0", hour = "*", persistent = false, info = "clearLoginCaptchaRateLimitCache")
	public void clearLoginCaptchaRateLimitCache() {
		int intervalHours = resolveLoginCaptchaRateLimitCacheResetHours();
		int currentHourUtc = DateTime.now(DateTimeZone.UTC).getHourOfDay();
		if (currentHourUtc % intervalHours != 0) {
			return;
		}

		ElectionsCaches.clearLoginCaptchaRateLimitCache();
		appLogger.info("Login captcha rate limit cache cleared. intervalHours={}, currentHourUtc={}", intervalHours, currentHourUtc);
	}

	/**
	 * Runs daily reminder processing for candidates and auditors based on
	 * configured reminder frequency.
	 */
	@Schedule(second = "0", minute = "0", hour = "10", persistent = false)
	public void processReminderFrequencyDaily() {
		try {
			appLogger.info("START Execute Daily Reminder Frequency Process");
			EJBFactory.getInstance().getElectionsManagerEJB().processDailyReminderFrequency();
			appLogger.info("END Execute Daily Reminder Frequency Process");
		} catch (Exception e) {
			appLogger.error("ERROR Execute Daily Reminder Frequency Process", e);
		}
	}

	/**
	 * Runs automatic organizations sync first and automatic census sync immediately
	 * after, keeping both processes independent.
	 */
	@TransactionTimeout(9000)
	@Schedule(second = "0", minute = "0", hour = "*/2", persistent = false)
	public void processAutomaticOrganizationsAndCensusSync() {
		String schedulerRunId = "combined-sync-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		long startedAt = System.currentTimeMillis();
		try {
			appLogger.info("Combined organizations/census sync job started. source=CRON, schedulerRunId={}", schedulerRunId);
			EJBFactory.getInstance().getElectionsManagerEJB().processAutomaticOrganizationsAndCensusSync();
		} catch (Exception e) {
			appLogger.error("ERROR Execute combined organizations/census sync process. schedulerRunId={}", schedulerRunId, e);
		} finally {
			long durationMs = System.currentTimeMillis() - startedAt;
			appLogger.info("Combined organizations/census sync job finished. source=CRON, schedulerRunId={}, durationMs={}", schedulerRunId, durationMs);
		}
	}

	/**
	 * Get the amount of attempts.
	 * 
	 * @return returns an integer with the amount of attempts
	 */
	public static int getAttempts() {
		return attempts;
	}

	/**
	 * Updates the amount of attempts of automatic processes.
	 * 
	 * @param attempts amount of attempts to store
	 */
	public static void setAttempts(int attempts) {
		ElectionScheduler.attempts = attempts;
	}

	private String resolveReplyTo(Email email) {
		if (email == null) {
			return null;
		}

		Election election = email.getElection();
		if (election != null && hasText(election.getDefaultRecipient())) {
			return election.getDefaultRecipient().trim();
		}

		try {
			String defaultRecipient = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.DEFAULT_RECIPIENT);
			if (hasText(defaultRecipient)) {
				return defaultRecipient.trim();
			}
		} catch (Exception e) {
			appLogger.error("ERROR resolving DEFAULT_RECIPIENT parameter", e);
		}

		if (election != null && hasText(election.getDefaultSender())) {
			return election.getDefaultSender().trim();
		}

		try {
			String defaultSender = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.DEFAULT_SENDER);
			return hasText(defaultSender) ? defaultSender.trim() : null;
		} catch (Exception e) {
			appLogger.error("ERROR resolving DEFAULT_SENDER parameter", e);
			return null;
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private int resolveAiTextImprovementRateLimitCacheResetHours() {
		return resolveRateLimitCacheResetHours(Constants.AI_TEXT_IMPROVEMENT_RATE_LIMIT_CACHE_RESET_HOURS, DEFAULT_AI_TEXT_IMPROVEMENT_RATE_LIMIT_CACHE_RESET_HOURS);
	}

	private void clearCampusProgressCheckRateLimitCacheByButton(String buttonKey, int intervalHours, int currentHourUtc) {
		if (currentHourUtc % intervalHours != 0) {
			return;
		}
		ElectionsCaches.clearCampusProgressCheckRateLimitCacheByButton(buttonKey);
		appLogger.info("Campus progress check rate limit cache cleared. buttonKey={}, intervalHours={}, currentHourUtc={}", buttonKey, intervalHours, currentHourUtc);
	}

	private int resolveCampusCourseProgressCheckRateLimitCacheResetHours() {
		return resolveRateLimitCacheResetHours(Constants.CAMPUS_PROGRESS_CHECK_COURSE_RATE_LIMIT_CACHE_RESET_HOURS, DEFAULT_CAMPUS_PROGRESS_CHECK_COURSE_RATE_LIMIT_CACHE_RESET_HOURS, Constants.CAMPUS_PROGRESS_CHECK_RATE_LIMIT_CACHE_RESET_HOURS, Constants.CAMPUS_PROGRESS_CHECK_COURSE_WINDOW_HOURS, Constants.CAMPUS_PROGRESS_CHECK_WINDOW_HOURS);
	}

	private int resolveCampusEvaluationProgressCheckRateLimitCacheResetHours() {
		return resolveRateLimitCacheResetHours(Constants.CAMPUS_PROGRESS_CHECK_EVALUATION_RATE_LIMIT_CACHE_RESET_HOURS, DEFAULT_CAMPUS_PROGRESS_CHECK_EVALUATION_RATE_LIMIT_CACHE_RESET_HOURS, Constants.CAMPUS_PROGRESS_CHECK_RATE_LIMIT_CACHE_RESET_HOURS, Constants.CAMPUS_PROGRESS_CHECK_EVALUATION_WINDOW_HOURS, Constants.CAMPUS_PROGRESS_CHECK_WINDOW_HOURS);
	}

	private int resolvePublicFailedAccessRateLimitCacheResetHours() {
		return resolveRateLimitCacheResetHours(Constants.PUBLIC_FAILED_ACCESS_RATE_LIMIT_CACHE_RESET_HOURS, DEFAULT_PUBLIC_FAILED_ACCESS_RATE_LIMIT_CACHE_RESET_HOURS);
	}

	private int resolveLoginCaptchaRateLimitCacheResetHours() {
		return resolveRateLimitCacheResetHours(Constants.LOGIN_CAPTCHA_RATE_LIMIT_CACHE_RESET_HOURS, DEFAULT_LOGIN_CAPTCHA_RATE_LIMIT_CACHE_RESET_HOURS);
	}

	private int resolveRateLimitCacheResetHours(String primaryParameter, int defaultValue, String... fallbackParameters) {
		try {
			String value = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(primaryParameter);
			if (!hasText(value) && fallbackParameters != null) {
				for (String fallbackParameter : fallbackParameters) {
					if (!hasText(fallbackParameter)) {
						continue;
					}
					String fallbackValue = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(fallbackParameter);
					if (hasText(fallbackValue)) {
						value = fallbackValue;
						break;
					}
				}
			}
			if (!hasText(value)) {
				return defaultValue;
			}
			int parsedValue = Integer.parseInt(value.trim());
			if (parsedValue <= 0 || parsedValue > 24) {
				appLogger.warn("Parameter {} must be between 1 and 24. Received value: {}. Using default {}.", primaryParameter, value, defaultValue);
				return defaultValue;
			}
			return parsedValue;
		} catch (Exception e) {
			appLogger.warn("Unable to resolve parameter {}, using default {}", primaryParameter, defaultValue, e);
			return defaultValue;
		}
	}
}
