package net.lacnic.elections.ejb.commons;

import java.util.List;

import javax.ejb.Remote;

import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.TemplateElection;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;

@Remote
public interface EnvioMailsEJB {

	void encolarEnvioMasivo(List usuarios, TemplateElection templateEleccion);

	List<Email> obtenerEmailsParaEnviar();

	void reagendar(List<Email> emailsProblematicos);

	void encolarEnvioIndividual(TemplateElection templateEleccion, UserVoter us, Auditor au, Election e, List<Vote> votos);

	void marcarEmailsComoEnviados();

	void moverEmailsaHistoricos();

	void purgarTablas();

}
