package net.lacnic.elections.ejb.commons;

import java.util.List;

import javax.ejb.Remote;

import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;


@Remote
public interface MailsSendingEJB {

	void queueMassiveSending(List usuarios, ElectionEmailTemplate templateEleccion);

	List<Email> getEmailsToSend();

	void reschedule(List<Email> emailsProblematicos);

	void queueSingleSending(ElectionEmailTemplate templateEleccion, UserVoter us, Auditor au, Election e, List<Vote> votos);

	void markEmailsAsSent();

	void moveEmailsToHistory();

	void purgeTables();

}
