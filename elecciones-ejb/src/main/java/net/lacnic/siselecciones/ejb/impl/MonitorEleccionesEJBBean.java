package net.lacnic.siselecciones.ejb.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.siselecciones.dao.DaoFactoryElecciones;
import net.lacnic.siselecciones.dao.ReportDao;
import net.lacnic.siselecciones.data.EleccionReporte;
import net.lacnic.siselecciones.data.HealthCheck;
import net.lacnic.siselecciones.data.Participacion;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.dominio.EleccionLight;
import net.lacnic.siselecciones.dominio.UsuarioPadron;
import net.lacnic.siselecciones.ejb.MonitorEleccionesEJB;
import net.lacnic.siselecciones.ejb.commons.impl.ProcesosAutomaticos;
import net.lacnic.siselecciones.utils.Constantes;
import net.ripe.ipresource.IpResourceSet;

/**
 * Session Bean implementation class Login
 */
@Stateless
@Remote(MonitorEleccionesEJB.class)
public class MonitorEleccionesEJBBean implements MonitorEleccionesEJB {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	
	@PersistenceContext(unitName = "elecciones-pu")
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

		ReportDao reportDao = DaoFactoryElecciones.createReportDao(em);

		int intentosDeEnvio = ProcesosAutomaticos.getIntentos();
		long ipsAccesosFallidos = reportDao.obtenerCantIpsAccesosFallidos();
		long sumaAccesosFallidos = reportDao.sumaIntentosAccesosFallidos();
		long correosTotales = reportDao.obtenerCantidadCorreos();
		long correosPendientes = reportDao.obtenerCorreosPendientes();
		long correosEnviados = reportDao.obtenerCorreosEnviados();

		List<EleccionReporte> elecciones = reporteEleccion();

		return new HealthCheck(intentosDeEnvio, ipsAccesosFallidos, sumaAccesosFallidos, correosTotales, correosPendientes, correosEnviados, elecciones);
		
	}

	public List<EleccionReporte> reporteEleccion() {

		ReportDao reportDao = DaoFactoryElecciones.createReportDao(em);
		List<EleccionReporte> eleccion = new ArrayList<>();

		for (Object[] iDyNombre : reportDao.idElecciones()) {
			Long id = (Long) iDyNombre[0];
			String nombreEleccion = (String) iDyNombre[1];
			long usuariosVotaron = reportDao.obtenerCantidadVotantesVanVotando(id);
			long usuariosNoVotaron = reportDao.obtenerCantidadVotantesNoHanVotado(id);
			long usuariosTotales = reportDao.obtenerCantUsuariosPadronEleccion(id);
			long correosPendientes = reportDao.obtenerCorreosSinEnviarElec(id);
			EleccionReporte eleccionReport = new EleccionReporte(nombreEleccion, usuariosVotaron, usuariosNoVotaron, usuariosTotales, correosPendientes);
			eleccion.add(eleccionReport);
		}

		return eleccion;
	}

	@Override
	public List<Participacion> obtenerParticipacionesOrgId(String org) {
		List<Participacion> participaciones = new ArrayList<>();
		List<Eleccion> elecciones = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccionesDesc();
		for (Eleccion eleccion : elecciones) {
			UsuarioPadron up = DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronOrgId(org, eleccion.getIdEleccion());
			Participacion p = new Participacion();
			p.setCategoria(eleccion.getCategoria().toString());
			if (up != null) {
				p.setEmail(up.getMail());
				p.setNombre(up.getNombre());
				p.setPais(up.getPais());
				p.setYaVoto(up.isYaVoto());
				if (eleccion.isHabilitadaParaVotar()) {
					p.setLinkVotar(up.getLinkVotar());
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
			p.setFechaFinEleccion(eleccion.getFechaFin());
			p.setFechaInicioEleccion(eleccion.getFechaInicio());
			p.setOrgId(org);			
			p.setTituloEleccionEN(eleccion.getTituloIngles());
			p.setTituloEleccionSP(eleccion.getTituloEspanol());
			p.setTituloEleccionPT(eleccion.getTituloPortugues());			
			p.setLinkEleccionSP(eleccion.getLinkEspanol());
			p.setLinkEleccionEN(eleccion.getLinkIngles());
			p.setLinkEleccionPT(eleccion.getLinkPortugues());
			
			participaciones.add(p);
		}
		return participaciones;
	}


	@Override
	public List<EleccionLight> obtenerEleccionesLightDesc() {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerEleccionesLightDesc();
	}
	
	@Override
	public String getWsAuthToken() {
		try {
			return DaoFactoryElecciones.createParametroDao(em).getParametro(Constantes.WS_AUTH_TOKEN).getValor();
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	@Override
	public IpResourceSet getWsIPsHabilitadas() {
		try {
			return IpResourceSet.parse(DaoFactoryElecciones.createParametroDao(em).getParametro(Constantes.WS_IPS_HABILITADAS).getValor());
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

}
