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

	private final String CODE_SUMMARY = "$usuario.resumenCodigos";

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	@Override
	public void queueMassiveSending(List users, ElectionEmailTemplate electionTemplate) {
		try {
			Election election = electionTemplate.getElection();
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
					templateSubject = electionTemplate.getSubjectSP();  
				} else if (userVoter.getLanguage().equals("EN")) {
					templateSubject = electionTemplate.getSubjectEN(); 
				} else {
					templateSubject = electionTemplate.getSubjectPT();
				};

				if (userVoter.getLanguage().equals("SP")) {
					templateBody = electionTemplate.getBodySP(); 
				} else if (userVoter.getLanguage().equals("EN")) {
					templateBody = electionTemplate.getBodyEN();
				} else {
					templateBody = electionTemplate.getBodyPT();
				};

				if (templateSubject.contains(CODE_SUMMARY) || templateBody.contains(CODE_SUMMARY))
					userVoter.setCodeSummary(addVotes(ElectionsDaoFactory.createVoteDao(em).getElectionUserVoterVotes(userVoter.getUserVoterId(), election.getElectionId())));

				Map<String, Object> map = new HashMap<>();
				map.put("usuario", userVoter);
				map.put("eleccion", election);
				String processSubject = processTemplate(templateSubject, map);
				String processBody = processTemplate(templateBody, map);
				email.setSubject(processSubject);
				email.setBody(processBody);
				email.setFrom(election.getDefaultSender());
				email.setRecipients(userVoter.getMail());
				email.setElection(election);
				email.setSent(false);
				email.setTemplateType(electionTemplate.getTemplateType());
				em.persist(email);
			}
			for (int i = 0; i < userAuditors.size(); i++) {
				Auditor auditor = userAuditors.get(i);
				Email email = new Email();
				String templateSubject = electionTemplate.getSubjectSP();
				String templateBody = electionTemplate.getBodySP();
				Map<String, Object> map = new HashMap<>();
				map.put("auditor", auditor);
				map.put("eleccion", election);
				String processSubject = processTemplate(templateSubject, map);
				String processBody = processTemplate(templateBody, map);
				email.setSubject(processSubject);
				email.setBody(processBody);
				email.setFrom(election.getDefaultSender());
				email.setRecipients(auditor.getMail());
				email.setElection(election);
				email.setSent(false);
				email.setTemplateType(electionTemplate.getTemplateType());
				em.persist(email);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void queueSingleSending(ElectionEmailTemplate templateEleccion, UserVoter userVoter, Auditor auditor, Election election, List<Vote> votes) {
		try {
			if (templateEleccion.getBodySP().contains(CODE_SUMMARY) || templateEleccion.getBodyEN().contains(CODE_SUMMARY) || templateEleccion.getBodyPT().contains(CODE_SUMMARY))
				userVoter.setCodeSummary(addVotes(votes));

			if (templateEleccion.getTemplateType().contains(Constants.TemplateTypeAUDITOR)) {
				Email email = new Email();
				String templateSubject = templateEleccion.getSubjectSP();
				String templateBody = templateEleccion.getBodySP();
				Map<String, Object> map = new HashMap<>();
				map.put("auditor", auditor);
				map.put("eleccion", election);
				String processSubject = processTemplate(templateSubject, map);
				String processBody = processTemplate(templateBody, map);
				email.setSubject(processSubject);
				email.setBody(processBody);
				email.setFrom(election.getDefaultSender());
				email.setRecipients(EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.DEFAULT_SENDER));
				email.setElection(election);
				email.setSent(false);
				email.setTemplateType(templateEleccion.getTemplateType());
				em.persist(email);
			} else {
				Email email = new Email();
				String templateSubject; 
				String templateBody; 

				if (userVoter.getLanguage().equals("SP")) {
					templateSubject = templateEleccion.getSubjectSP();
				} else if (userVoter.getLanguage().equals("EN")) {
					templateSubject = templateEleccion.getSubjectEN(); 
				} else {
					templateSubject = templateEleccion.getSubjectPT();
				};

				if (userVoter.getLanguage().equals("SP") ) {
					templateBody = templateEleccion.getBodySP();
				} else if (userVoter.getLanguage().equals("EN")) {
					templateBody = templateEleccion.getBodyEN();
				} else {
					templateBody = templateEleccion.getBodyPT();
				};

				Map<String, Object> map = new HashMap<>();
				map.put("usuario", userVoter);
				map.put("eleccion", election);
				String processSubject = processTemplate(templateSubject, map);
				String processBody = processTemplate(templateBody, map);
				email.setSubject(processSubject);
				email.setBody(processBody);
				email.setFrom(election.getDefaultSender());
				email.setRecipients(userVoter.getMail());
				email.setElection(election);
				email.setSent(false);
				email.setTemplateType(templateEleccion.getTemplateType());
				em.persist(email);
			}
		} catch (Exception ex) {
			appLogger.error(ex);
		}
	}

	private String addVotes(List<Vote> votes) {
		String aux = "";
		for (Vote v : votes) {
			aux = aux.concat(v.getCode() + " / " + v.getCandidate().getName() + "\n");
		}
		return aux;
	}

	private String processTemplate(String template, Map<String, Object> variables) throws IOException {
		VelocityEngine velocityEngine = new VelocityEngine();
		velocityEngine.init();
		VelocityContext context = new VelocityContext();

		for (Map.Entry<String,Object> entry : variables.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}

		StringWriter stringWriter = new StringWriter();
		Velocity.evaluate(context, stringWriter, "email-template", template);
		return stringWriter.toString();
	}

	@Override
	public List<Email> getEmailsToSend() {
		return ElectionsDaoFactory.createEmailDao(em).getPendingSendEmails();
	}

	@Override
	public void markEmailsAsSent() {
		ElectionsDaoFactory.createEmailDao(em).markAllEmailsAsSent();
	}

	@Override
	public void reschedule(List<Email> problemEmails) {
		for (Email email : problemEmails) {
			try {
				email.setSent(false);
				em.merge(email);
			} catch (Exception e) {
				appLogger.error(e);
			}
		}
	}

	@Override
	public void moveEmailsToHistory() {
		List<Email> emails = ElectionsDaoFactory.createEmailDao(em).getEmailsOlderOneMonth();
		for (Email email : emails) {
			em.persist(new EmailHistory(email));
			em.remove(email);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void purgeTables() {

		org.hibernate.Session session = em.unwrap(Session.class);
		org.hibernate.internal.SessionImpl sessionImpl = (SessionImpl) session;

		try ( 
			java.sql.Connection connection = sessionImpl.connection();
			java.sql.PreparedStatement s1 = connection.prepareStatement("VACUUM email");
			java.sql.PreparedStatement s2 = connection.prepareStatement("VACUUM FULL email");
			java.sql.PreparedStatement s3 = connection.prepareStatement("VACUUM usuariopadron");
			java.sql.PreparedStatement s4 = connection.prepareStatement("VACUUM FULL usuariopadron");
			java.sql.PreparedStatement s5 = connection.prepareStatement("VACUUM candidato");
			java.sql.PreparedStatement s6 = connection.prepareStatement("VACUUM FULL candidato");
			java.sql.PreparedStatement s7 = connection.prepareStatement("VACUUM eleccion");
			java.sql.PreparedStatement s8 = connection.prepareStatement("VACUUM FULL eleccion");
			java.sql.PreparedStatement s9 = connection.prepareStatement("VACUUM");
			java.sql.PreparedStatement s10 = connection.prepareStatement("VACUUM FULL");
		){
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
