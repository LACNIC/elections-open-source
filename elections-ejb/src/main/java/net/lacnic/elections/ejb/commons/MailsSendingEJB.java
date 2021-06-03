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

	void queueMassiveSending(List users, ElectionEmailTemplate templateEleccion);

	List<Email> getEmailsToSend();

	void reschedule(List<Email> problemEmails);

	void queueSingleSending(ElectionEmailTemplate tamplateElection, UserVoter userVoter, Auditor auditor, Election election, List<Vote> votes);

	void markEmailsAsSent();

	void moveEmailsToHistory();

	void purgeTables();

}
