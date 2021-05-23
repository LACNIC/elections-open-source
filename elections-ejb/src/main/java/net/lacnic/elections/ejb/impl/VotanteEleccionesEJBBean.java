package net.lacnic.elections.ejb.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.dao.DaoFactoryElecciones;
import net.lacnic.elections.dao.UsuarioPadronDao;
import net.lacnic.elections.data.DetalleResultadoData;
import net.lacnic.elections.data.ResultadoEleccionesData;
import net.lacnic.elections.domain.AccesosIps;
import net.lacnic.elections.domain.Actividad;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidato;
import net.lacnic.elections.domain.Eleccion;
import net.lacnic.elections.domain.SupraEleccion;
import net.lacnic.elections.domain.TemplateEleccion;
import net.lacnic.elections.domain.TipoActividad;
import net.lacnic.elections.domain.UsuarioPadron;
import net.lacnic.elections.domain.Voto;
import net.lacnic.elections.ejb.VotanteEleccionesEJB;
import net.lacnic.elections.exception.OperationNotPermittedException;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.FileUtils;
import net.lacnic.elections.utils.StringUtils;
import net.lacnic.elections.utils.UtilsFiles;

@Stateless
@Remote(VotanteEleccionesEJB.class)
public class VotanteEleccionesEJBBean implements VotanteEleccionesEJB {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	
	@PersistenceContext(unitName = "elecciones-pu")
	private EntityManager em;

	String templatesPath = System.getProperty("jboss.server.config.dir").concat("/templates/");

	@Resource
	private SessionContext context;

	public VotanteEleccionesEJBBean() {
		//Constructor vacío
	}

	@Override
	public List<Object[]> obtenerCodigosdeVotacion(long idEleccion) {
		return DaoFactoryElecciones.createVotoDao(em).obtenerCodigosdeVotacion(idEleccion);
	}

	@Override
	public void votar(List<Candidato> candidatos, UsuarioPadron up, String ip) throws OperationNotPermittedException {
		UsuarioPadron usP = DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuarioPadron(up.getIdUsuarioPadron());
		if (usP.isYaVoto()) {
			throw new OperationNotPermittedException("Operación no permitida");
		}
		if (candidatos.size() > usP.getEleccion().getMaxCandidatos() || usP.isYaVoto() || !usP.getEleccion().isHabilitadaParaVotar()) {
			throw new OperationNotPermittedException("Operación no permitida");
		}

		ArrayList<Voto> votos = new ArrayList<>();
		Date fechaVoto = new Date();
		for (Candidato c : candidatos) {
			for (int i = 0; i < usP.getCantVotos(); i++) {
				Voto v = new Voto();
				v.setCodigo(StringUtils.createSmallToken());
				v.setIp(ip);
				v.setCandidato(c);
				v.setFechaVoto(fechaVoto);
				v.setUsuarioPadron(usP);
				v.setEleccion(usP.getEleccion());
				em.persist(v);
				votos.add(v);
			}
		}
		usP.setYaVoto(true);
		usP.setFechaVoto(fechaVoto);
		TemplateEleccion t = EJBFactory.getInstance().getManagerEleccionesEJB().obtenerTemplate(Constants.TemplateTypeVOTE_CODES, usP.getEleccion().getIdEleccion());
		EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioIndividual(t, usP, null, usP.getEleccion(), votos);
		em.persist(usP);
	}

	@Override
	public UsuarioPadron verificarAccesoUP(String token) {
		try {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuarioPadronToken(token);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public UsuarioPadron[] verificarAccesoUPEleccionJunta(String token) {
		try {
			UsuarioPadron up1 = DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuarioPadronToken(token);
			UsuarioPadron up2 = null;
			SupraEleccion supraEleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerSupraEleccion(up1.getEleccion().getIdEleccion());
			long eleccionOtra = 0;

			if (up1.getEleccion().getIdEleccion() == supraEleccion.getIdEleccionB())
				eleccionOtra = supraEleccion.getIdEleccionA();
			else
				eleccionOtra = supraEleccion.getIdEleccionB();

			for (UsuarioPadron usu : DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(eleccionOtra).getUsuariosPadron()) {
				if (usu.getMail().toUpperCase().equalsIgnoreCase(up1.getMail())) {
					up2 = usu;
					break;
				}
			}

			return new UsuarioPadron[] { up1, up2 };
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Eleccion verificarAccesoResultado(String token) {
		try {
			return DaoFactoryElecciones.createEleccionDao(em).obtenerEleccionConTokenResultado(token);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Auditor verificarAccesoResultadoAuditor(String token) {
		try {
			return DaoFactoryElecciones.createAuditorDao(em).obtenerAuditorTokenResultado(token);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void intentoFallidoIp(String remoteAddress) {
		AccesosIps ip = DaoFactoryElecciones.createAccesoIpsDao(em).obteneriP(remoteAddress);
		if (ip == null) {
			ip = new AccesosIps();
			ip.setFechaUltimoIntento(new Date());
			ip.setFechaPrimerIntento(new Date());
			ip.setIp(remoteAddress);
			ip.setIntentos(1);
			em.persist(ip);
		} else {
			ip.setFechaUltimoIntento(new Date());
			ip.setIntentos(ip.getIntentos() + 1);
			em.merge(ip);
		}

	}

	@Override
	public List<Candidato> obtenerCandidatosEleccionOrdenados(long idEleccion) throws Exception {
		return EJBFactory.getInstance().getManagerEleccionesEJB().obtenerCandidatosEleccionOrdenados(idEleccion);
	}

	@Override
	public List<Candidato> obtenerCandidatosEleccionConVotos(long idEleccion) throws Exception {
		return DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatosEleccion(idEleccion);
	}

	@Override
	public long obtenerVotosCandidato(long idCandidato) throws Exception {
		return DaoFactoryElecciones.createCandidatoDao(em).obtenerCantVotosCandidato(idCandidato);
	}

	@Override
	public long obtenerCantidadVotantesQueVotaronEleccion(long idEleccion) throws Exception {
		UsuarioPadronDao padronDao = DaoFactoryElecciones.createUsuarioPadronDao(em);
		return padronDao.obtenerCantidadVotantesQueVotaronEleccion(idEleccion);
	}

	@Override
	public void confirmarEleccionAuditor(long idAuditor) {
		try {
			Auditor a = em.find(Auditor.class, idAuditor);
			a.setExpresoConformidad(true);
			em.persist(a);
			TemplateEleccion t = EJBFactory.getInstance().getManagerEleccionesEJB().obtenerTemplate(Constants.TemplateTypeAUDITOR_AGREEMENT, a.getEleccion().getIdEleccion());
			EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioIndividual(t, null, a, a.getEleccion(), new ArrayList<>());
		} catch (Exception e) {
			appLogger.error(e);
		}

	}

	@Override
	public void habilitarRevisionEleccionAuditor(long idAuditor, String ip) {
		try {
			Auditor a = em.find(Auditor.class, idAuditor);
			a.setHabilitaRevision(true);
			em.persist(a);
			TemplateEleccion t = EJBFactory.getInstance().getManagerEleccionesEJB().obtenerTemplate(Constants.TemplateTypeAUDITOR_REVISION, a.getEleccion().getIdEleccion());
			EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioIndividual(t, null, a, a.getEleccion(), new ArrayList<>());

			String descripcion = a.getIdAuditor() + " - " + a.getNombre() + " autorizó la revisión de la elección: " + a.getEleccion().getTituloEspanol();
			persistirActividad(a.getNombre(), TipoActividad.REVISION_DE_ELECCION_SI, descripcion, ip, a.getEleccion().getIdEleccion());
		} catch (Exception e) {
			appLogger.error(e);
		}

	}

	@Override
	public boolean yaVoto(long idUsuarioPadron) throws Exception {
		UsuarioPadron u = DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuarioPadron(idUsuarioPadron);
		return u.isYaVoto();
	}

	@Override
	public long obtenerTotalVotosEleccion(long idEleccion) throws Exception {
		return DaoFactoryElecciones.createVotoDao(em).obtenerTotalVotosEleccion(idEleccion);
	}

	@Override
	public byte[] obtenerAuditoriaAquaIt() {
		try {
			return FileUtils.getBytesFromFile(templatesPath + "AQUAIT-LACNIC-sistema-votacion.pdf");
		} catch (Exception e) {
			appLogger.error(e);
		}
		return "".getBytes();
	}

	@Override
	public byte[] obtenerRequerimientosV1() {
		try {

			return FileUtils.getBytesFromFile(templatesPath + "RequerimientosSistemaEleccionesV10.pdf");
		} catch (Exception e) {
			appLogger.error(e);
		}
		return "".getBytes();
	}

	@Override
	public byte[] obtenerEleccionesRolesFuncionamientoRevision() {
		try {

			return FileUtils.getBytesFromFile(templatesPath + "EleccionesRolesFuncionamientoRevision.pdf");
		} catch (Exception e) {
			appLogger.error(e);
		}
		return "".getBytes();
	}
	
	@Override
	public byte[] obtenerEleccionesRolesFuncionamientoRevision(String filePath) {
		try {			
			return FileUtils.getBytesFromFile(filePath + "/static/EleccionesRolesFuncionamientoRevision.pdf");
		} catch (Exception e) {
			appLogger.error(e);
		}
		return "".getBytes();
	}
	
	@Override
	public File getEleccionesRolesFuncionamientoRevision(String filePath) {					
			return UtilsFiles.getEleccionesRolesFuncionamientoRevision(filePath);			
	}

	@Override
	public ResultadoEleccionesData obtenerResultadoEleccionesData(long idEleccion) throws Exception {
		Eleccion eleccion = em.find(Eleccion.class, idEleccion);
		int max = eleccion.getMaxCandidatos();

		ResultadoEleccionesData resultadoData = new ResultadoEleccionesData(max);
		ArrayList<DetalleResultadoData> listaDetalleResultadoData = new ArrayList<>();
		List<Integer> pesosDistintos = DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerTodosLosPesosDistintosDeLosUP(idEleccion);

		for (Integer pesos : pesosDistintos) {
			Long habilidado = obtenerCantidadVotantesPorPesoPorEleccion(idEleccion, pesos);
			Long participantes = obtenerCantidadVotantesQueVotaronEleccionPorPeso(idEleccion, pesos);
			DetalleResultadoData detalle = new DetalleResultadoData(habilidado, participantes, pesos);
			listaDetalleResultadoData.add(detalle);
		}
		resultadoData.setDetalleResultadoData(listaDetalleResultadoData);
		resultadoData.calcularTotales();
		return resultadoData;
	}

	private Long obtenerCantidadVotantesPorPesoPorEleccion(long idEleccion, Integer pesos) {
		UsuarioPadronDao padronDao = DaoFactoryElecciones.createUsuarioPadronDao(em);
		return padronDao.obtenerCantidadVotantesPorPesoPorEleccion(idEleccion, pesos);
	}

	private Long obtenerCantidadVotantesQueVotaronEleccionPorPeso(long idEleccion, Integer pesos) {
		UsuarioPadronDao padronDao = DaoFactoryElecciones.createUsuarioPadronDao(em);
		return padronDao.obtenerCantidadVotantesQueVotaronEleccionPorPeso(idEleccion, pesos);
	}

	@Override
	public List<Object[]> obtenerCandidatosVotacion(long idUsuarioPadron, long idEleccion) {
		return DaoFactoryElecciones.createVotoDao(em).obtenerCandidatosVotacion(idUsuarioPadron, idEleccion);
	}

	@Override
	public List<Object> obtenerDataEvolucionVotosEleccion(long idEleccion) {
		List<UsuarioPadron> usuPadVotaronEleccion = DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccionQueYaVotaron(idEleccion);

		List<String> dias = new ArrayList<>();
		List<Integer> votosPorDia = new ArrayList<>();
		List<Integer> votosAcumulados = new ArrayList<>();
		List<Object> data = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		try {

			Date dia1 = usuPadVotaronEleccion.get(0).getFechaVoto();
			Date ultimoDia = usuPadVotaronEleccion.get(usuPadVotaronEleccion.size() - 1).getFechaVoto();

			List<String> allDates = getDatesBetween(dia1, ultimoDia);
			List<String> votosDates = new ArrayList<>();

			for (UsuarioPadron usuPad : usuPadVotaronEleccion) {
				votosDates.add(sdf.format(usuPad.getFechaVoto()));
			}

			for (String date : allDates) {
				dias.add(date);
				votosPorDia.add(Collections.frequency(votosDates, date));
			}

			int sum = 0;
			for (int votosDia : votosPorDia) {
				sum = sum + votosDia;
				votosAcumulados.add(sum);
			}

			data.add(dias);
			data.add(votosPorDia);
			data.add(votosAcumulados);

			return data;
		} catch (Exception e) {

			return null;
		}
	}

	public static List<String> getDatesBetween(Date startDate, Date endDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		List<String> datesInRange = new ArrayList<>();
		
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(startDate);

		Calendar endCalendar = new GregorianCalendar();
		endCalendar.setTime(endDate);

		while (calendar.before(endCalendar)) {
			Date result = calendar.getTime();
			String dia = sdf.format(result);
			if (!datesInRange.contains(dia))
				datesInRange.add(dia);
			calendar.add(Calendar.DATE, 1);
		}
		String dia = sdf.format(endDate);
		if (!datesInRange.contains(dia))
			datesInRange.add(dia);

		return datesInRange;
	}

	private void persistirActividad(String nomAdmin, TipoActividad tipoActividad, String descripcion, String ip, Long idEleccion) {
		Actividad a = new Actividad();
		a.setIp(ip);
		a.setIdEleccion(idEleccion);
		a.setTiempo(new Date());
		a.setNomUser(nomAdmin);
		a.setTipoActividad(tipoActividad);
		a.setDescripcion(descripcion);
		em.persist(a);
	}

	@Override
	public Eleccion obtenerEleccion(long idEleccion) {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
	}

	@Override
	public boolean isEleccionSimple(long idEleccion) {
		return DaoFactoryElecciones.createEleccionDao(em).isEleccionSimple(idEleccion);
	}

	@Override
	public SupraEleccion obtenerSupraEleccion(long idEleccion) {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerSupraEleccion(idEleccion);
	}

}