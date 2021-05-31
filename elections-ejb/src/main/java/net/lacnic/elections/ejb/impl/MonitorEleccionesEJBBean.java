package net.lacnic.elections.ejb.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.dao.ReportDao;
import net.lacnic.elections.data.EleccionReporte;
import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participation;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionLight;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.ejb.MonitorEleccionesEJB;
import net.lacnic.elections.ejb.commons.impl.ProcesosAutomaticos;
import net.lacnic.elections.utils.Constants;
import net.ripe.ipresource.IpResourceSet;

/**
 * Session Bean implementation class Login
 */
@Stateless
@Remote(MonitorEleccionesEJB.class)
public class MonitorEleccionesEJBBean implements MonitorEleccionesEJB {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	
	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	private static HealthCheck healthCheck;

	@Override
	public HealthCheck obtenerDatosWS() {
		if (healthCheck == null)
			healthCheck = actualizarHCDatosWS();
		return healthCheck;
	}

	@Override
	public HealthCheck actualizarHCDatosWS() {

		ReportDao reportDao = ElectionsDaoFactory.createReportDao(em);

		int intentosDeEnvio = ProcesosAutomaticos.getIntentos();
		long ipsAccesosFallidos = reportDao.getFailedIpAccesesAmount();
		long sumaAccesosFallidos = reportDao.getFailedIpAccesesSum();
		long correosTotales = reportDao.getEmailsAmount();
		long correosPendientes = reportDao.getPendingSendEmailsAmount();
		long correosEnviados = reportDao.getSentEmailsAmount();

		List<EleccionReporte> elecciones = reporteEleccion();

		return new HealthCheck(intentosDeEnvio, ipsAccesosFallidos, sumaAccesosFallidos, correosTotales, correosPendientes, correosEnviados, elecciones);
		
	}

	public List<EleccionReporte> reporteEleccion() {

		ReportDao reportDao = ElectionsDaoFactory.createReportDao(em);
		List<EleccionReporte> eleccion = new ArrayList<>();

		for (Object[] iDyNombre : reportDao.getElectionsAllIdName()) {
			Long id = (Long) iDyNombre[0];
			String nombreEleccion = (String) iDyNombre[1];
			long usuariosVotaron = reportDao.getElectionAlreadyVotedAmount(id);
			long usuariosNoVotaron = reportDao.getElectionNotVotedYetAmount(id);
			long usuariosTotales = reportDao.getElectionCensusSize(id);
			long correosPendientes = reportDao.getElectionPendingSendEmailsAmount(id);
			EleccionReporte eleccionReport = new EleccionReporte(nombreEleccion, usuariosVotaron, usuariosNoVotaron, usuariosTotales, correosPendientes);
			eleccion.add(eleccionReport);
		}

		return eleccion;
	}

	@Override
	public List<Participation> obtenerParticipacionesOrgId(String org) {
		List<Participation> participaciones = new ArrayList<>();
		List<Election> elecciones = ElectionsDaoFactory.createEleccionDao(em).getElectionsAllOrderStartDateDesc();
		for (Election eleccion : elecciones) {
			UserVoter up = ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionUserVotersByOrganization(org, eleccion.getElectionId());
			Participation p = new Participation();
			p.setCategoria(eleccion.getCategory().toString());
			if (up != null) {
				p.setEmail(up.getMail());
				p.setNombre(up.getName());
				p.setPais(up.getCountry());
				p.setYaVoto(up.isVoted());
				if (eleccion.isEnabledToVote()) {
					p.setLinkVotar(up.getVoteLink());
				} else {
					p.setLinkVotar("");
				}
			} else {
				p.setEmail("");
				p.setNombre("");
				p.setPais("");
				p.setYaVoto(false);
				p.setLinkVotar("");
			};			
			p.setFechaFinEleccion(eleccion.getEndDate());
			p.setFechaInicioEleccion(eleccion.getStartDate());
			p.setOrgId(org);			
			p.setTituloEleccionEN(eleccion.getTitleEnglish());
			p.setTituloEleccionSP(eleccion.getTitleSpanish());
			p.setTituloEleccionPT(eleccion.getTitlePortuguese());			
			p.setLinkEleccionSP(eleccion.getLinkSpanish());
			p.setLinkEleccionEN(eleccion.getLinkEnglish());
			p.setLinkEleccionPT(eleccion.getLinkPortuguese());
			
			participaciones.add(p);
		}
		return participaciones;
	}


	@Override
	public List<ElectionLight> obtenerEleccionesLightDesc() {
		return ElectionsDaoFactory.createEleccionDao(em).getElectionsLightAllOrderStartDateDesc();
	}
	
	@Override
	public String getWsAuthToken() {
		try {
			return ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_AUTH_TOKEN).getValue();
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	@Override
	public IpResourceSet getWsIPsHabilitadas() {
		try {
			return IpResourceSet.parse(ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_AUTHORIZED_IPS).getValue());
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

}
