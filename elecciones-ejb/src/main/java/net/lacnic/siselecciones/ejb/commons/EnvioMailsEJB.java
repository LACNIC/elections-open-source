package net.lacnic.siselecciones.ejb.commons;

import java.util.List;

import javax.ejb.Remote;

import net.lacnic.siselecciones.dominio.Auditor;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.dominio.Email;
import net.lacnic.siselecciones.dominio.TemplateEleccion;
import net.lacnic.siselecciones.dominio.UsuarioPadron;
import net.lacnic.siselecciones.dominio.Voto;

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
