package net.lacnic.elections.ejb.commons.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;

import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.EmailHistory;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.ejb.commons.MailsSendingEJB;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;

/**
 * Session Bean implementation class EmailerBean
 */
@Stateless
@Remote(MailsSendingEJB.class)
public class MailsSendingEJBBean implements MailsSendingEJB {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private static final String CODE_SUMMARY = "$user.codesSummary";
	private static final String ELECTION = "election";

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	/**
	 * Creates mails with the information required depending on the template type, from a list of voters or auditors (depending on the template type). If the templates contains VOTE_SuMMARY on the body,
	 * it adds the vote summary if the template is for auditor, it creates a mail with the auditor information; if not, it creates a mail for the voter.
	 * 
	 * @param templateEleccion The email template from which to create the mail
	 * @param users            A collection of either user voter entity of auditor entity with the information for each mail
	 * 
	 */
	@Override
	public void queueMassiveSending(List users, ElectionEmailTemplate emailTemplate) {
		try {
			Election election = emailTemplate.getElection();
			List<UserVoter> userVoters = new ArrayList<>();
			List<Auditor> userAuditors = new ArrayList<>();

			if (users != null && !users.isEmpty()) {
				if (users.get(0) instanceof UserVoter) {
					userVoters = users;
				} else if (users.get(0) instanceof Auditor) {
					userAuditors = users;
				}
			}
			for (int i = 0; i < userVoters.size(); i++) {
				UserVoter userVoter = userVoters.get(i);
				Email email = new Email();
				String templateSubject;
				String templateBody;

				if (userVoter.getLanguage().equals("SP")) {
					templateSubject = emailTemplate.getSubjectSP();
				} else if (userVoter.getLanguage().equals("EN")) {
					templateSubject = emailTemplate.getSubjectEN();
				} else {
					templateSubject = emailTemplate.getSubjectPT();
				}
				;

				if (userVoter.getLanguage().equals("SP")) {
					templateBody = emailTemplate.getBodySP();
				} else if (userVoter.getLanguage().equals("EN")) {
					templateBody = emailTemplate.getBodyEN();
				} else {
					templateBody = emailTemplate.getBodyPT();
				}
				;

				if (templateSubject.contains(CODE_SUMMARY) || templateBody.contains(CODE_SUMMARY))
					userVoter.setCodesSummary(addVotes(ElectionsDaoFactory.createVoteDao(em).getElectionUserVoterVotes(userVoter.getUserVoterId(), election.getElectionId())));

				Map<String, Object> map = new HashMap<>();
				map.put("user", userVoter);
				map.put(ELECTION, election);
				String processSubject = processTemplate(templateSubject, map);
				String processBody = processTemplate(templateBody, map);
				email.setSubject(processSubject);
				email.setBody(processBody);
				email.setSender(election.getDefaultSender());
				email.setRecipients(userVoter.getMail());
				email.setElection(election);
				email.setSent(false);
				email.setTemplateType(emailTemplate.getTemplateType());
				em.persist(email);
			}
			for (int i = 0; i < userAuditors.size(); i++) {
				Auditor auditor = userAuditors.get(i);
				Email email = new Email();
				String templateSubject = emailTemplate.getSubjectSP();
				String templateBody = emailTemplate.getBodySP();
				Map<String, Object> map = new HashMap<>();
				map.put("auditor", auditor);
				map.put(ELECTION, election);
				String processSubject = processTemplate(templateSubject, map);
				String processBody = processTemplate(templateBody, map);
				email.setSubject(processSubject);
				email.setBody(processBody);
				email.setSender(election.getDefaultSender());
				email.setRecipients(auditor.getMail());
				email.setElection(election);
				email.setSent(false);
				email.setTemplateType(emailTemplate.getTemplateType());
				em.persist(email);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Creates a mail with the information required depending on the template type. If the templates contains VOTE_SuMMARY on the body, it adds the vote summary if the template is fopr auditor, it creates
	 * a mail with the auditor information; if not, it creates a mail for the voter.
	 * 
	 * @param templateEleccion The email template from which to create the mail
	 * @param userVoter        The voter of the election
	 * @param auditor          The auditor of the election
	 * @param election         The election
	 * @param votes            A collection of vote entity containing the votes
	 * 
	 */
	@Override
	public void queueSingleSending(ElectionEmailTemplate emailTemplate, UserVoter userVoter, Auditor auditor, Election election, List<Vote> votes) {
		try {
			if (emailTemplate.getBodySP().contains(CODE_SUMMARY) || emailTemplate.getBodyEN().contains(CODE_SUMMARY) || emailTemplate.getBodyPT().contains(CODE_SUMMARY))
				userVoter.setCodesSummary(addVotes(votes));

			if (emailTemplate.getTemplateType().contains(Constants.TEMPLATE_TYPE_AUDITOR)) {
				Email email = new Email();
				String templateSubject = emailTemplate.getSubjectSP();
				String templateBody = emailTemplate.getBodySP();
				Map<String, Object> map = new HashMap<>();
				map.put("auditor", auditor);
				map.put(ELECTION, election);
				String processSubject = processTemplate(templateSubject, map);
				String processBody = processTemplate(templateBody, map);
				email.setSubject(processSubject);
				email.setBody(processBody);
				email.setSender(election.getDefaultSender());
				email.setRecipients(EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.DEFAULT_SENDER));
				email.setElection(election);
				email.setSent(false);
				email.setTemplateType(emailTemplate.getTemplateType());
				em.persist(email);
			} else {
				Email email = new Email();
				String templateSubject;
				String templateBody;

				if (userVoter.getLanguage().equals("SP")) {
					templateSubject = emailTemplate.getSubjectSP();
				} else if (userVoter.getLanguage().equals("EN")) {
					templateSubject = emailTemplate.getSubjectEN();
				} else {
					templateSubject = emailTemplate.getSubjectPT();
				}
				;

				if (userVoter.getLanguage().equals("SP")) {
					templateBody = emailTemplate.getBodySP();
				} else if (userVoter.getLanguage().equals("EN")) {
					templateBody = emailTemplate.getBodyEN();
				} else {
					templateBody = emailTemplate.getBodyPT();
				}
				;

				Map<String, Object> map = new HashMap<>();
				map.put("user", userVoter);
				map.put(ELECTION, election);
				String processSubject = processTemplate(templateSubject, map);
				String processBody = processTemplate(templateBody, map);
				email.setSubject(processSubject);
				email.setBody(processBody);
				email.setSender(election.getDefaultSender());
				email.setRecipients(userVoter.getMail());
				email.setElection(election);
				email.setSent(false);
				email.setTemplateType(emailTemplate.getTemplateType());
				em.persist(email);
			}
		} catch (Exception ex) {
			appLogger.error(ex);
		}
	}

	/**
	 * Gets a list of votes in the form of vote code / candidate name list
	 * 
	 * @param votes A collection of vote entity containing the votes
	 * 
	 * @return returns a string with all the vote codes and candidate names in the form of "code / name"
	 */
	private String addVotes(List<Vote> votes) {
		String aux = "";
		for (Vote v : votes) {
			aux = aux.concat(v.getCode() + " / " + v.getCandidate().getName() + "\n");
		}
		return aux;
	}

	/**
	 * Maps the variables to the template fields using velocity
	 * 
	 * @param template  Template text with variables
	 * @param variables A map of string - object with the information.
	 * @return returns a string with the template filled.
	 * 
	 */
	private String processTemplate(String template, Map<String, Object> variables) throws IOException {
		VelocityEngine velocityEngine = new VelocityEngine();
		velocityEngine.init();
		VelocityContext context = new VelocityContext();

		for (Map.Entry<String, Object> entry : variables.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}

		StringWriter stringWriter = new StringWriter();
		Velocity.evaluate(context, stringWriter, "email-template", template);
		return stringWriter.toString();
	}

	/**
	 * Gets a list of all the email pending to be sent.
	 * 
	 * @return returns a collection of email entity containing the information
	 */
	@Override
	public List<Email> getEmailsToSend() {
		return ElectionsDaoFactory.createEmailDao(em).getPendingSendEmails();
	}

	/**
	 * Changes the status of all the emails on Email table as sent
	 */
	@Override
	public void markEmailsAsSent() {
		ElectionsDaoFactory.createEmailDao(em).markAllEmailsAsSent();
	}

	/**
	 * Takes a list of unsent emails and re sends them
	 * 
	 * @param problemEmails A collection of email entity containing the mails that need to be re-sent.
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void markEmailAsSent(Email email) {
		email.setSent(true);
		em.merge(email);
	}

	/**
	 * Moves emails with creation date older than 30 days to the table email history
	 */
	@Override
	public void moveEmailsToHistory() {
		List<Email> emails = ElectionsDaoFactory.createEmailDao(em).getEmailsOlderOneMonth();
		for (Email email : emails) {
			em.persist(new EmailHistory(email));
			em.remove(email);
		}
	}

	/**
	 * Deletes all the transactional information from the database (email, uservoter, candidate, election )
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void purgeTables() {

		org.hibernate.Session session = em.unwrap(Session.class);
		org.hibernate.internal.SessionImpl sessionImpl = (SessionImpl) session;

		try (java.sql.Connection connection = sessionImpl.connection(); java.sql.PreparedStatement s1 = connection.prepareStatement("VACUUM email"); java.sql.PreparedStatement s2 = connection.prepareStatement("VACUUM FULL email"); java.sql.PreparedStatement s3 = connection.prepareStatement("VACUUM uservoter"); java.sql.PreparedStatement s4 = connection.prepareStatement("VACUUM FULL uservoter"); java.sql.PreparedStatement s5 = connection.prepareStatement("VACUUM candidate"); java.sql.PreparedStatement s6 = connection.prepareStatement("VACUUM FULL candidate"); java.sql.PreparedStatement s7 = connection.prepareStatement("VACUUM election"); java.sql.PreparedStatement s8 = connection.prepareStatement("VACUUM FULL election"); java.sql.PreparedStatement s9 = connection.prepareStatement("VACUUM"); java.sql.PreparedStatement s10 = connection.prepareStatement("VACUUM FULL");) {
			s1.execute();
			s2.execute();

			s3.execute();
			s4.execute();

			s5.execute();
			s6.execute();

			s7.execute();
			s8.execute();

			s9.execute();
			s10.execute();
		} catch (SQLException e) {
			appLogger.error(e);
		}

	}

}
