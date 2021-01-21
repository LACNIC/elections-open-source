package net.lacnic.siselecciones.ejb.commons.impl;

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

import net.lacnic.siselecciones.dao.DaoFactoryElecciones;
import net.lacnic.siselecciones.dominio.Auditor;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.dominio.Email;
import net.lacnic.siselecciones.dominio.EmailHistorico;
import net.lacnic.siselecciones.dominio.TemplateEleccion;
import net.lacnic.siselecciones.dominio.UsuarioPadron;
import net.lacnic.siselecciones.dominio.Voto;
import net.lacnic.siselecciones.ejb.commons.EnvioMailsEJB;
import net.lacnic.siselecciones.utils.Constantes;
import net.lacnic.siselecciones.utils.EJBFactory;

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
	public void encolarEnvioMasivo(List usuarios, TemplateEleccion templateEleccion) {
		try {
			Eleccion e = templateEleccion.getEleccion();
			List<UsuarioPadron> usuariosPadron = new ArrayList<>();
			List<Auditor> usuariosAuditor = new ArrayList<>();

			if (usuarios != null && !usuarios.isEmpty()) {
				if (usuarios.get(0) instanceof UsuarioPadron) {
					usuariosPadron = usuarios;
				} else if (usuarios.get(0) instanceof Auditor) {
					usuariosAuditor = usuarios;
				}
			}
			for (int i = 0; i < usuariosPadron.size(); i++) {
				UsuarioPadron usp = usuariosPadron.get(i);
				Email email = new Email();
				String templateAsunto;
				String templateCuerpo;

				if (usp.getIdioma().equals("SP")) {
					templateAsunto = templateEleccion.getAsuntoES();  
				} else if (usp.getIdioma().equals("EN")) {
					templateAsunto = templateEleccion.getAsuntoEN(); 
				} else {
					templateAsunto = templateEleccion.getAsuntoPT();
				};

				if (usp.getIdioma().equals("SP")) {
					templateCuerpo = templateEleccion.getCuerpoES(); 
				} else if (usp.getIdioma().equals("EN")) {
					templateCuerpo = templateEleccion.getCuerpoEN();
				} else {
					templateCuerpo = templateEleccion.getCuerpoPT();
				};

				if (templateAsunto.contains(RESUMEN_CODIGO) || templateCuerpo.contains(RESUMEN_CODIGO))
					usp.setResumenCodigos(agregarVotos(DaoFactoryElecciones.createVotoDao(em).obtenerVotos(usp.getIdUsuarioPadron(), e.getIdEleccion())));

				Map<String, Object> mapa = new HashMap<>();
				mapa.put("usuario", usp);
				mapa.put("eleccion", e);
				String asuntoProcesado = processTemplate(templateAsunto, mapa);
				String cuerpoProcesado = processTemplate(templateCuerpo, mapa);
				email.setAsunto(asuntoProcesado);
				email.setCuerpo(cuerpoProcesado);
				email.setDesde(e.getRemitentePorDefecto());
				email.setDestinatarios(usp.getMail());
				email.setEleccion(e);
				email.setEnviado(false);
				email.setTipoTemplate(templateEleccion.getTipoTemplate());
				em.persist(email);
			}
			for (int i = 0; i < usuariosAuditor.size(); i++) {
				Auditor a = usuariosAuditor.get(i);
				Email email = new Email();
				String templateAsunto = templateEleccion.getAsuntoES();
				String templateCuerpo = templateEleccion.getCuerpoES();
				Map<String, Object> mapa = new HashMap<>();
				mapa.put("auditor", a);
				mapa.put("eleccion", e);
				String asuntoProcesado = processTemplate(templateAsunto, mapa);
				String cuerpoProcesado = processTemplate(templateCuerpo, mapa);
				email.setAsunto(asuntoProcesado);
				email.setCuerpo(cuerpoProcesado);
				email.setDesde(e.getRemitentePorDefecto());
				email.setDestinatarios(a.getMail());
				email.setEleccion(e);
				email.setEnviado(false);
				email.setTipoTemplate(templateEleccion.getTipoTemplate());
				em.persist(email);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void encolarEnvioIndividual(TemplateEleccion templateEleccion, UsuarioPadron us, Auditor au, Eleccion e, List<Voto> votos) {
		try {
			if (templateEleccion.getCuerpoES().contains(RESUMEN_CODIGO) || templateEleccion.getCuerpoEN().contains(RESUMEN_CODIGO) || templateEleccion.getCuerpoPT().contains(RESUMEN_CODIGO))
				us.setResumenCodigos(agregarVotos(votos));

			if (templateEleccion.getTipoTemplate().contains(Constantes.TipoTemplateAUDITOR)) {
				Email email = new Email();
				String templateAsunto = templateEleccion.getAsuntoES();
				String templateCuerpo = templateEleccion.getCuerpoES();
				Map<String, Object> mapa = new HashMap<>();
				mapa.put("auditor", au);
				mapa.put("eleccion", e);
				String asuntoProcesado = processTemplate(templateAsunto, mapa);
				String cuerpoProcesado = processTemplate(templateCuerpo, mapa);
				email.setAsunto(asuntoProcesado);
				email.setCuerpo(cuerpoProcesado);
				email.setDesde(e.getRemitentePorDefecto());
				email.setDestinatarios(EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constantes.REMITENTE_ESTANDAR));
				email.setEleccion(e);
				email.setEnviado(false);
				email.setTipoTemplate(templateEleccion.getTipoTemplate());
				em.persist(email);
			} else {
				Email email = new Email();
				String templateAsunto; 
				String templateCuerpo; 

				if (us.getIdioma().equals("SP")) {
					templateAsunto = templateEleccion.getAsuntoES();
				} else if (us.getIdioma().equals("EN")) {
					templateAsunto = templateEleccion.getAsuntoEN(); 
				} else {
					templateAsunto = templateEleccion.getAsuntoPT();
				};

				if (us.getIdioma().equals("SP") ) {
					templateCuerpo = templateEleccion.getCuerpoES();
				} else if (us.getIdioma().equals("EN")) {
					templateCuerpo = templateEleccion.getCuerpoEN();
				} else {
					templateCuerpo = templateEleccion.getCuerpoPT();
				};

				Map<String, Object> mapa = new HashMap<>();
				mapa.put("usuario", us);
				mapa.put("eleccion", e);
				String asuntoProcesado = processTemplate(templateAsunto, mapa);
				String cuerpoProcesado = processTemplate(templateCuerpo, mapa);
				email.setAsunto(asuntoProcesado);
				email.setCuerpo(cuerpoProcesado);
				email.setDesde(e.getRemitentePorDefecto());
				email.setDestinatarios(us.getMail());
				email.setEleccion(e);
				email.setEnviado(false);
				email.setTipoTemplate(templateEleccion.getTipoTemplate());
				em.persist(email);
			}
		} catch (Exception ex) {
			appLogger.error(ex);
		}
	}

	private String agregarVotos(List<Voto> votos) {
		String aux = "";
		for (Voto v : votos) {
			aux = aux.concat(v.getCodigo() + " / " + v.getCandidato().getNombre() + "\n");
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
				email.setEnviado(false);
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
			em.persist(new EmailHistorico(email));
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
