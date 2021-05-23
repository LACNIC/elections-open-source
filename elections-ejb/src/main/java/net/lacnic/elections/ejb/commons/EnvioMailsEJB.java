package net.lacnic.elections.ejb.commons;

import java.util.List;

import javax.ejb.Remote;

import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Eleccion;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.TemplateEleccion;
import net.lacnic.elections.domain.UsuarioPadron;
import net.lacnic.elections.domain.Voto;

@Remote
public interface EnvioMailsEJB {

	void encolarEnvioMasivo(List usuarios, TemplateEleccion templateEleccion);

	List<Email> obtenerEmailsParaEnviar();

	void reagendar(List<Email> emailsProblematicos);

	void encolarEnvioIndividual(TemplateEleccion templateEleccion, UsuarioPadron us, Auditor au, Eleccion e, List<Voto> votos);

	void marcarEmailsComoEnviados();

	void moverEmailsaHistoricos();

	void purgarTablas();

}
