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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.dao.DaoFactoryElecciones;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.EmailHistory;
import net.lacnic.elections.domain.TemplateElection;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.ejb.commons.EnvioMailsEJB;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;

/**
 * Session Bean implementation class EmailerBean
 */
@Stateless
@Remote(EnvioMailsEJB.class)
public class EnvioMailsEJBBean implements EnvioMailsEJB {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private final String RESUMEN_CODIGO = "$usuario.resumenCodigos";

	@PersistenceContext(unitName = "elecciones-pu")
	private EntityManager em;

	@Override
	public void encolarEnvioMasivo(List usuarios, TemplateElection templateEleccion) {
		try {
			Election e = templateEleccion.getElection();
			List<UserVoter> usuariosPadron = new ArrayList<>();
			List<Auditor> usuariosAuditor = new ArrayList<>();

			if (usuarios != null && !usuarios.isEmpty()) {
				if (usuarios.get(0) instanceof UserVoter) {
					usuariosPadron = usuarios;
				} else if (usuarios.get(0) instanceof Auditor) {
					usuariosAuditor = usuarios;
				}
			}
			for (int i = 0; i < usuariosPadron.size(); i++) {
				UserVoter usp = usuariosPadron.get(i);
				Email email = new Email();
				String templateAsunto;
				String templateCuerpo;

				if (usp.getLanguage().equals("SP")) {
					templateAsunto = templateEleccion.getSubjectSP();  
				} else if (usp.getLanguage().equals("EN")) {
					templateAsunto = templateEleccion.getSubjectEN(); 
				} else {
					templateAsunto = templateEleccion.getSubjectPT();
				};

				if (usp.getLanguage().equals("SP")) {
					templateCuerpo = templateEleccion.getBodySP(); 
				} else if (usp.getLanguage().equals("EN")) {
					templateCuerpo = templateEleccion.getBodyEN();
				} else {
					templateCuerpo = templateEleccion.getBodyPT();
				};

				if (templateAsunto.contains(RESUMEN_CODIGO) || templateCuerpo.contains(RESUMEN_CODIGO))
					usp.setCodeSummary(agregarVotos(DaoFactoryElecciones.createVotoDao(em).obtenerVotos(usp.getIdUserVoter(), e.getIdElection())));

				Map<String, Object> mapa = new HashMap<>();
				mapa.put("usuario", usp);
				mapa.put("eleccion", e);
				String asuntoProcesado = processTemplate(templateAsunto, mapa);
				String cuerpoProcesado = processTemplate(templateCuerpo, mapa);
				email.setSubject(asuntoProcesado);
				email.setBody(cuerpoProcesado);
				email.setEmailFrom(e.getDefaultSender());
				email.setRecipients(usp.getMail());
				email.setElection(e);
				email.setSent(false);
				email.setTemplateType(templateEleccion.getTemplateType());
				em.persist(email);
			}
			for (int i = 0; i < usuariosAuditor.size(); i++) {
				Auditor a = usuariosAuditor.get(i);
				Email email = new Email();
				String templateAsunto = templateEleccion.getSubjectSP();
				String templateCuerpo = templateEleccion.getBodySP();
				Map<String, Object> mapa = new HashMap<>();
				mapa.put("auditor", a);
				mapa.put("eleccion", e);
				String asuntoProcesado = processTemplate(templateAsunto, mapa);
				String cuerpoProcesado = processTemplate(templateCuerpo, mapa);
				email.setSubject(asuntoProcesado);
				email.setBody(cuerpoProcesado);
				email.setEmailFrom(e.getDefaultSender());
				email.setRecipients(a.getMail());
				email.setElection(e);
				email.setSent(false);
				email.setTemplateType(templateEleccion.getTemplateType());
				em.persist(email);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void encolarEnvioIndividual(TemplateElection templateEleccion, UserVoter us, Auditor au, Election e, List<Vote> votos) {
		try {
			if (templateEleccion.getBodySP().contains(RESUMEN_CODIGO) || templateEleccion.getBodyEN().contains(RESUMEN_CODIGO) || templateEleccion.getBodyPT().contains(RESUMEN_CODIGO))
				us.setCodeSummary(agregarVotos(votos));

			if (templateEleccion.getTemplateType().contains(Constants.TipoTemplateAUDITOR)) {
				Email email = new Email();
				String templateAsunto = templateEleccion.getSubjectSP();
				String templateCuerpo = templateEleccion.getBodySP();
				Map<String, Object> mapa = new HashMap<>();
				mapa.put("auditor", au);
				mapa.put("eleccion", e);
				String asuntoProcesado = processTemplate(templateAsunto, mapa);
				String cuerpoProcesado = processTemplate(templateCuerpo, mapa);
				email.setSubject(asuntoProcesado);
				email.setBody(cuerpoProcesado);
				email.setEmailFrom(e.getDefaultSender());
				email.setRecipients(EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constants.DEFAULT_SENDER));
				email.setElection(e);
				email.setSent(false);
				email.setTemplateType(templateEleccion.getTemplateType());
				em.persist(email);
			} else {
				Email email = new Email();
				String templateAsunto; 
				String templateCuerpo; 

				if (us.getLanguage().equals("SP")) {
					templateAsunto = templateEleccion.getSubjectSP();
				} else if (us.getLanguage().equals("EN")) {
					templateAsunto = templateEleccion.getSubjectEN(); 
				} else {
					templateAsunto = templateEleccion.getSubjectPT();
				};

				if (us.getLanguage().equals("SP") ) {
					templateCuerpo = templateEleccion.getBodySP();
				} else if (us.getLanguage().equals("EN")) {
					templateCuerpo = templateEleccion.getBodyEN();
				} else {
					templateCuerpo = templateEleccion.getBodyPT();
				};

				Map<String, Object> mapa = new HashMap<>();
				mapa.put("usuario", us);
				mapa.put("eleccion", e);
				String asuntoProcesado = processTemplate(templateAsunto, mapa);
				String cuerpoProcesado = processTemplate(templateCuerpo, mapa);
				email.setSubject(asuntoProcesado);
				email.setBody(cuerpoProcesado);
				email.setEmailFrom(e.getDefaultSender());
				email.setRecipients(us.getMail());
				email.setElection(e);
				email.setSent(false);
				email.setTemplateType(templateEleccion.getTemplateType());
				em.persist(email);
			}
		} catch (Exception ex) {
			appLogger.error(ex);
		}
	}

	private String agregarVotos(List<Vote> votos) {
		String aux = "";
		for (Vote v : votos) {
			aux = aux.concat(v.getCode() + " / " + v.getCandidate().getName() + "\n");
		}
		return aux;
	}

	private String processTemplate(String template, Map<String, Object> variables) throws IOException {
		VelocityEngine ve = new VelocityEngine();
		ve.init();
		VelocityContext context = new VelocityContext();

		for (Map.Entry<String,Object> entry : variables.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}

		StringWriter w = new StringWriter();
		Velocity.evaluate(context, w, "email-template", template);
		return w.toString();
	}

	@Override
	public List<Email> obtenerEmailsParaEnviar() {
		return DaoFactoryElecciones.createEmailDao(em).obtenerEmailsParaEnviarElecciones();
	}

	@Override
	public void marcarEmailsComoEnviados() {
		DaoFactoryElecciones.createEmailDao(em).marcarEmailsComoEnviados();
	}

	@Override
	public void reagendar(List<Email> emailsProblematicos) {
		for (Email email : emailsProblematicos) {
			try {
				email.setSent(false);
				em.merge(email);
			} catch (Exception e) {
				appLogger.error(e);
			}
		}
	}

	@Override
	public void moverEmailsaHistoricos() {
		List<Email> emails = DaoFactoryElecciones.createEmailDao(em).obtenerEmailsViejo();
		for (Email email : emails) {
			em.persist(new EmailHistory(email));
			em.remove(email);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void purgarTablas() {

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
