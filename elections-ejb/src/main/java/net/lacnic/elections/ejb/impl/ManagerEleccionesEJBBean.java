package net.lacnic.elections.ejb.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.dao.DaoFactoryElecciones;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Comissioner;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.Customization;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.TemplateElection;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.RecipientType;
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.ejb.ManagerEleccionesEJB;
import net.lacnic.elections.exception.CensusValidationException;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.StringUtils;
import net.lacnic.elections.utils.UtilsExcel;
import net.lacnic.elections.utils.UtilsFiles;
import net.lacnic.elections.utils.UtilsLinks;

@Stateless
@Remote(ManagerEleccionesEJB.class)
public class ManagerEleccionesEJBBean implements ManagerEleccionesEJB {

	private static final String TEXT_ELECCION = " para la elección ";

	private Random rand = new SecureRandom();

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	@PersistenceContext(unitName = "elecciones-pu")
	private EntityManager em;

	@Override
	public UserAdmin loginAdmin(String adminId, String password, String ip) {
		UserAdmin a = DaoFactoryElecciones.createUsuarioAdminDao(em).comprobarUsuarioAdmin(adminId, password);
		try {
			if(a != null) {
				String descripcion = adminId.toUpperCase() + " se ha logueado exitosamente";
				EJBFactory.getInstance().getManagerEleccionesEJB().persistirActividad(adminId, ActivityType.LOGIN_SUCCESSFUL, descripcion, ip, null);
			} else {
				String descripcion = "Intento fallido de login de usuario " + adminId.toUpperCase();
				EJBFactory.getInstance().getManagerEleccionesEJB().persistirActividad(adminId, ActivityType.LOGIN_FAILED, descripcion, ip, null);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return a;
	}

	@Override
	public boolean isValidCaptchaResponse(String reCaptchaResponse) {

		try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
			appLogger.info("start verifying ...");
			appLogger.info("reCAPTCHA response value : " + reCaptchaResponse);

			String skGoogleApiReCaptcha = EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constants.SkGoogleApiReCaptcha);
			String checkURL = "https://www.google.com/recaptcha/api/siteverify" + "?secret=" + skGoogleApiReCaptcha + "&response=" + reCaptchaResponse;

			appLogger.info("check URL : " + checkURL);

			HttpPost post = new HttpPost("https://www.google.com/recaptcha/api/siteverify");

			// add headerype
			post.setHeader("User-Agent", "Mozilla/5.0");

			List<NameValuePair> urlParameters = new ArrayList<>();
			urlParameters.add(new BasicNameValuePair("secret", skGoogleApiReCaptcha));
			urlParameters.add(new BasicNameValuePair("response", reCaptchaResponse));

			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			HttpResponse httpClientResponse = httpClient.execute(post);

			appLogger.info("\nSending 'POST' request to URL : https://www.google.com/recaptcha/api/siteverify");
			appLogger.info("Post parameters : " + post.getEntity());
			appLogger.info("Response Code : " + httpClientResponse.getStatusLine().getStatusCode());

			BufferedReader rd = new BufferedReader(new InputStreamReader(httpClientResponse.getEntity().getContent()));

			StringBuilder result = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			JsonReader jsonReader = Json.createReader(new StringReader(result.toString()));
			try {
				JsonObject jsonObject = jsonReader.readObject();
				appLogger.info(jsonObject.getBoolean("success"));
				return jsonObject.getBoolean("success");
			} catch (Exception e) {
				appLogger.error(e);
				return false;
			} finally {
				jsonReader.close();
			}	

		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	public boolean isProd() {
		try {
			return EJBFactory.getInstance().getParametrosEleccionesEJB().isProd();
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	public List<Election> obtenerEleccionesLight() {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerElecciones();
	}

	@Override
	public List<Election> obtenerEleccionesLightEsteAnio() {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerEleccionesLightEsteAnio();
	}

	@Override
	public void eliminarCandidato(long idCandidato, String userId, String ip) {
		Candidate candidato = em.find(Candidate.class, idCandidato);
		em.remove(candidato);
		String descripcion = userId.toUpperCase() + " eliminó al candidato " + candidato.getName() + TEXT_ELECCION + candidato.getElection().getTitleSpanish();
		persistirActividad(userId, ActivityType.ELIMINAR_CANDIDATO, descripcion, ip, candidato.getElection().getIdElection());

		Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(candidato.getElection().getIdElection());
		eleccion.setCandidatesSet(eleccion.getCandidates().size() > 1);
		em.persist(eleccion);
	}

	@Override
	public void eliminarAuditor(long idAuditor, String userId, String ip) {
		Auditor auditor = em.find(Auditor.class, idAuditor);
		em.remove(auditor);
		String descripcion = userId.toUpperCase() + " eliminó al auditor " + auditor.getName() + TEXT_ELECCION + auditor.getElection().getTitleSpanish();
		persistirActividad(userId, ActivityType.ELIMINAR_AUDITOR, descripcion, ip, auditor.getElection().getIdElection());
	}

	@Override
	public void agregarCandidato(long idEleccion, Candidate candidato, String userId, String ip) {
		Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
		candidato.setElection(eleccion);
		int orden = DaoFactoryElecciones.createCandidatoDao(em).obtenerOrdenDelUltimoCandidatoNofijado(idEleccion);
		candidato.setCandidateOrder(orden + 1);
		em.persist(candidato);
		eleccion.setCandidatesSet(true);
		em.persist(eleccion);
		String descripcion = userId.toUpperCase() + " agregó un candidato para la elección  " + eleccion.getTitleSpanish();
		persistirActividad(userId, ActivityType.AGREGAR_CANDIDATO, descripcion, ip, eleccion.getIdElection());
	}

	@Override
	public void editarCandidato(Candidate candidato, String userId, String ip) {
		try {
			em.merge(candidato);
			String descripcion = userId.toUpperCase() + " actualizó los datos de un candidato para la elección " + candidato.getElection().getTitleSpanish();
			persistirActividad(userId, ActivityType.EDITAR_CANDIDATOS, descripcion, ip, candidato.getElection().getIdElection());
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void agregarAuditor(long idEleccion, Auditor auditor, String tituloEspaniol, String userId, String ip) {
		Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
		auditor.setElection(eleccion);
		em.persist(auditor);
		String descripcion = userId.toUpperCase() + " agregó un auditor para la elección " + tituloEspaniol;
		persistirActividad(userId, ActivityType.AGREGAR_AUDITOR, descripcion, ip, idEleccion);
	}

	@Override
	public void persistirAuditoresSeteados(long idEleccion, String tituloEspaniol, String userId, String ip) {
		try {
			Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
			eleccion.setAuditorsSet(true);
			em.persist(eleccion);
			String descripcion = userId.toUpperCase() + " agregó auditores para la elección " + tituloEspaniol;
			persistirActividad(userId, ActivityType.AGREGAR_AUDITORES, descripcion, ip, idEleccion);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public Election obtenerEleccion(long idEleccion) {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
	}

	@Override
	public String obtenerLinkresultado(Election eleccion) throws Exception {
		return UtilsLinks.calcularLinkResultado(eleccion.getResultToken());
	}

	@Override
	public List<Candidate> obtenerCandidatosEleccionOrdenados(long idEleccion) {
		List<Candidate> candidatos = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatosEleccion(idEleccion);
		Election e = obtenerEleccion(idEleccion);
		Collections.sort(candidatos, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate c1, Candidate c2) {
				int r;

				if (c1.getCandidateOrder() == Constants.MIN_ORDER)
					r = Constants.MIN_ORDER;
				else if (c2.getCandidateOrder() == Constants.MAX_ORDER)
					r = Constants.MAX_ORDER;
				else if (e.isRandomOrderCandidates()) {
					r = Integer.valueOf(rand.nextInt(30)).compareTo(Integer.valueOf(rand.nextInt(30)));
				} else
					r = (Integer.valueOf(c2.getCandidateOrder()).compareTo(Integer.valueOf(c1.getCandidateOrder())));
				return r;
			}
		});
		return candidatos;
	}

	@Override
	public Candidate obtenerCandidato(long idCandidato) {
		return DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
	}

	@Override
	public void actualizarUsuariosPadron(long idEleccion, byte[] contenido, String admin, String ip) throws CensusValidationException, Exception {
		try {
			List<UserVoter> usuariosPadron = UtilsExcel.procesarExcelPadronNuevo(contenido);
			Election eleccion = em.find(Election.class, idEleccion);
			eleccion.setElectorsSet(true);
			DaoFactoryElecciones.createVotoDao(em).borrarVotos(idEleccion);
			DaoFactoryElecciones.createUsuarioPadronDao(em).borrarUsuariosPadron(idEleccion);

			for (UserVoter usuarioPadron : usuariosPadron) {
				usuarioPadron.setElection(eleccion);
				usuarioPadron.setVoteToken(StringUtils.createSecureToken());
				em.persist(usuarioPadron);
			}
			em.persist(eleccion);

			String descripcion = admin.toUpperCase() + " agregó el padrón para la elección  " + "(" + eleccion.getTitleSpanish() + ")";
			persistirActividad(admin, ActivityType.AGREGAR_PADRON, descripcion, ip, idEleccion);

		} catch (CensusValidationException e) {
			appLogger.error(e);
			throw e;
		} catch (Exception e1) {
			appLogger.error(e1);
			throw e1;
		}
	}

	@Override
	public boolean agregarUsuarioPadron(long idEleccion, UserVoter usuarioPadron, String userId, String ip) throws CensusValidationException {
		try {
			if(!DaoFactoryElecciones.createUsuarioPadronDao(em).existeUsuarioPadronEleccionEmail(idEleccion, usuarioPadron.getMail())) {
				Election eleccion = em.find(Election.class, idEleccion);
				usuarioPadron.setElection(eleccion);
				usuarioPadron.setVoteToken(StringUtils.createSecureToken());
				em.persist(usuarioPadron);
				eleccion.setElectorsSet(true);
				String descripcion = userId.toUpperCase() + " agregó a " + usuarioPadron.getName() + " como usuario padrón para la elección " + eleccion.getTitleSpanish();
				persistirActividad(userId, ActivityType.AGREGAR_USUARIO_PADRON, descripcion, ip, idEleccion);
				return true;
			} else {
				throw new CensusValidationException("duplicateEmailException");
			}
		} catch (CensusValidationException vpe) {
			appLogger.error(vpe);
			throw vpe;
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	public void editarUsuarioPadron(UserVoter usuarioPadron, String userId, String ip) throws CensusValidationException {
		try {
			if(!DaoFactoryElecciones.createUsuarioPadronDao(em).existeUsuarioPadronEleccionEmail(usuarioPadron.getElection().getIdElection(), usuarioPadron.getMail())) {
				em.merge(usuarioPadron);
				String descripcion = userId.toUpperCase() + " actualizó los datos de un usuario padrón para la elección  " + usuarioPadron.getElection().getTitleSpanish();
				persistirActividad(userId, ActivityType.EDITAR_USUARIO_PADRON, descripcion, ip, usuarioPadron.getElection().getIdElection());
			} else {
				throw new CensusValidationException("duplicateEmailException");
			}

		} catch (CensusValidationException vpe) {
			appLogger.error(vpe);
			throw vpe;
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void actualizarTokenUsuarioPadron(long idUsp, String nombre, String titulo, String userId, String ip) {
		UserVoter u = em.find(UserVoter.class, idUsp);
		u.setVoteToken(StringUtils.createSecureToken());
		em.merge(u);
		String descripcion = userId.toUpperCase() + " actualizó el link de votación para el usuario " + nombre + " en la elección " + titulo;
		persistirActividad(userId, ActivityType.ACTUALIZAR_TOKEN_USUPADRON, descripcion, ip, u.getElection().getIdElection());
	}

	@Override
	public File exportarPadronElectoral(long idEleccion) {
		String nombreArchivo = "/padron_electoral_" + idEleccion + ".xls";
		return UtilsExcel.exportarAExcel(obtenerUsuariosPadron(idEleccion), nombreArchivo);
	}

	@Override
	public File exportarEjemploPadronElectoral() {
		return UtilsFiles.getPadronElectoralEjemplo();
	}

	@Override
	public File exportarEjemploPadronElectoral(String filePath) {
		return UtilsFiles.getPadronElectoralEjemplo(filePath);
	}

	@Override
	public void darDeBajaEleccion(long idEleccion, String titulo, String userId, String ip) throws Exception {
		try {
			List<UserAdmin> usuAdmin = DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerUsuariosAdmin(idEleccion);
			List<Auditor> auditores = DaoFactoryElecciones.createAuditorDao(em).obtenerAuditoresEleccion(idEleccion);
			List<Candidate> candidatos = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatosEleccion(idEleccion);
			List<Email> emails = DaoFactoryElecciones.createEmailDao(em).obtenerMailsDeEleccion(idEleccion);
			List<TemplateElection> templates = DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplatesEleccion(idEleccion);
			List<UserVoter> usuariosP = DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccion(idEleccion);
			List<Vote> votos = DaoFactoryElecciones.createVotoDao(em).obtenerVotosEleccion(idEleccion);
			Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
			for (UserAdmin ad : usuAdmin)
				em.remove(ad);
			for (Auditor a : auditores)
				em.remove(a);
			for (Candidate c : candidatos)
				em.remove(c);
			for (Email e : emails)
				em.remove(e);
			for (TemplateElection t : templates)
				em.remove(t);
			for (UserVoter u : usuariosP)
				em.remove(u);
			for (Vote v : votos)
				em.remove(v);
			em.remove(eleccion);
			String descripcion = userId.toUpperCase() + " eliminó la elección " + titulo;
			persistirActividad(userId, ActivityType.ELIMINAR_ELECCION, descripcion, ip, idEleccion);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List<Comissioner> obtenerComisionados() {
		return DaoFactoryElecciones.createComisionadoDao(em).obtenerComisionados();
	}

	@Override
	public List<Auditor> obtenerAuditoresEleccion(long idEleccion) throws Exception {
		return DaoFactoryElecciones.createAuditorDao(em).obtenerAuditoresEleccion(idEleccion);
	}

	@Override
	public Auditor obtenerAuditor(long idAuditor) {
		return DaoFactoryElecciones.createAuditorDao(em).obtenerAuditor(idAuditor);
	}

	@Override
	public void editarAuditor(Auditor auditor, String userId, String ip) {
		try {
			em.merge(auditor);
			String descripcion = userId.toUpperCase() + " actualizó los datos de un auditor para la elección " + auditor.getElection().getTitleSpanish();
			persistirActividad(userId, ActivityType.EDITAR_AUDITOR, descripcion, ip, auditor.getElection().getIdElection());
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public UserVoter obtenerUsuarioPadron(long idUsuarioPadron) {
		return em.find(UserVoter.class, idUsuarioPadron);

	}

	@Override
	public void eliminarUsuarioPadron(UserVoter actual, String titulo, String userId, String ip) {
		UserVoter usuario = em.find(UserVoter.class, actual.getIdUserVoter());
		Long idEleccion = usuario.getElection().getIdElection();
		em.remove(usuario);
		String descripcion = userId.toUpperCase() + " eliminó a " + usuario.getName() + " del listado de usuario padrón en la elección " + titulo;
		persistirActividad(userId, ActivityType.ELIMINAR_USUPADRON, descripcion, ip, idEleccion);
		long cantidadCandidatos = DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerCantidadUsuariosPadron(usuario.getElection().getIdElection());
		if (cantidadCandidatos < 1) {
			Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(usuario.getElection().getIdElection());
			eleccion.setElectorsSet(false);
			em.persist(eleccion);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Election actualizarEleccion(Election eleccion, String userId, String ip) throws Exception {
		try {
			String descripcion;
			if (eleccion.getIdElection() == 0) {
				em.persist(eleccion);
				descripcion = userId.toUpperCase() + " creó la elección" + " (" + eleccion.getTitleSpanish() + ")" + " correctamente";
				persistirActividad(userId, ActivityType.CREAR_ELECCION, descripcion, ip, eleccion.getIdElection());
				crearPlantillasEleccion(eleccion); // nuevo
				List<Comissioner> comisionados = DaoFactoryElecciones.createComisionadoDao(em).obtenerComisionados();
				for (Comissioner comisionado : comisionados) {
					em.persist(new Auditor(eleccion, comisionado));
				}
			} else
				em.merge(eleccion);
			descripcion = userId.toUpperCase() + " actualizó la elección " + " (" + eleccion.getTitleSpanish() + ")" + " correctamente";
			persistirActividad(userId, ActivityType.EDITAR_ELECCION, descripcion, ip, eleccion.getIdElection());
		} catch (Exception e) {
			appLogger.error(e);
		}

		return eleccion;
	}

	@Override
	public void crearPlantillasEleccion(Election eleccion) {
		List<TemplateElection> bases = DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplatesBase();
		for (int i = 0; i < bases.size(); i++) {
			TemplateElection templateI = bases.get(i);
			if (obtenerTemplate(templateI.getTemplateType(), eleccion.getIdElection()) == null) {
				em.persist(new TemplateElection(eleccion, templateI));
			}
		}

	}

	@Override
	public List<UserAdmin> obtenerUsuariosAdmin() {
		return DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerUsuariosAdmin();
	}

	@Override
	public void habilitarLinkVotacion(Long id, Boolean valor, String admin, String ip) {
		try {
			Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(id);
			eleccion.setVotingLinkAvailable(valor);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de votación para la elección ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = "habilitó el link de votación para la elección ";
			}
			String descripcion = admin.toUpperCase() + " " + strValor + "(" + eleccion.getTitleSpanish() + ")";
			persistirActividad(admin, ActivityType.HABILITACION_LINK_VOTACION, descripcion, ip, eleccion.getIdElection());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void ordenarCandidatosAleatoriamente(Long id, Boolean valor) {
		try {
			Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(id);
			eleccion.setRandomOrderCandidates(valor);
			em.persist(eleccion);
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void habilitarLinkResultado(Long id, Boolean valor, String admin, String ip) {
		try {
			Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(id);
			eleccion.setResultLinkAvailable(valor);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de resultado para la elección ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = "habilitó el link de resultado para la elección ";
			}
			String descripcion = admin.toUpperCase() + " " + strValor + "(" + eleccion.getTitleSpanish() + ")";
			persistirActividad(admin, ActivityType.HABILITACION_LINK_RESULTADOS, descripcion, ip, eleccion.getIdElection());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void habilitarLinkAuditoria(Long id, Boolean valor, String admin, String ip) {
		try {
			Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(id);
			eleccion.setAuditorLinkAvailable(valor);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de auditoria para la elección ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = "habilitó el link de auditoria para la elección ";
			};
			String descripcion = admin.toUpperCase() + " " + strValor + "(" + eleccion.getTitleSpanish() + ")";
			persistirActividad(admin, ActivityType.HABILITACION_LINK_AUDITORIA, descripcion, ip, eleccion.getIdElection());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void solicitarRevision(Long idEleccion, Boolean valor, String admin, String ip) {
		try {
			Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);

			habilitarLinkAuditoria(idEleccion, valor, admin, ip);

			if (!(Boolean.TRUE.equals(valor))) {
				List<Auditor> auditores = eleccion.getAuditors();
				for (Auditor auditor : auditores) {
					auditor.setRevisionAvailable(false);
					em.persist(auditor);
					String descripcion = admin.toUpperCase() + " cerró el proceso de revisión y se revocó la autorizacion de la revisión del auditor: " + auditor.getIdAuditor() + " - " + auditor.getName() + TEXT_ELECCION + "(" + eleccion.getTitleSpanish() + ")";
					persistirActividad(admin, ActivityType.REVISION_DE_ELECCION_NO, descripcion, ip, eleccion.getIdElection());
				}
			}
			eleccion.setRevisionRequest(valor);
			em.persist(eleccion);
			String strValor =  " revocó la solicitud de ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = " solicitó la ";
			};
			String descripcion = admin.toUpperCase() + strValor + "revisión para la elección " + "(" + eleccion.getTitleSpanish() + ")";
			persistirActividad(admin, ActivityType.REVISION_DE_ELECCION, descripcion, ip, eleccion.getIdElection());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public boolean isRevisionActiva(long idEleccion, String admin, String ip) {
		Election eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
		List<Auditor> auditores = eleccion.getAuditors();
		if (!eleccion.isRevisionRequest())
			return false;

		for (Auditor auditor : auditores) {
			if (auditor.isCommissioner() && !auditor.isRevisionAvailable())
				return false;
		}

		String descripcion = admin.toUpperCase() + " ingresó a la revisión de votos para la elección " + "(" + eleccion.getTitleSpanish() + ")";
		persistirActividad(admin, ActivityType.INGRESO_A_REVISION, descripcion, ip, eleccion.getIdElection());
		return true;
	}

	@Override
	public List<UserVoter> obtenerUsuariosPadronEleccion(long idEleccion) {
		return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccion(idEleccion);
	}

	@Override
	public List<Activity> obtenerTodasLasActividades() {
		return DaoFactoryElecciones.createActividadDao(em).obtenerTodasLasActividades();
	}

	@Override
	public List<Activity> obtenerTodasLasActividades(long idEleccion) {
		return DaoFactoryElecciones.createActividadDao(em).obtenerTodasLasActividades(idEleccion);
	}

	@Override
	public void persistirActividad(String nomAdmin, ActivityType tipoActividad, String descripcion, String ip, Long idEleccion) {
		Activity a = new Activity();
		a.setIp(ip);
		a.setElectionId(idEleccion);
		a.setTimestamp(new Date());
		a.setUserName(nomAdmin);
		a.setActivityType(tipoActividad);
		a.setDescription(descripcion);
		em.persist(a);
	}

	@Override
	public List<UserVoter> obtenerUsuariosPadron(long idEleccion) {
		return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccion(idEleccion);
	}

	@Override
	public void eliminarUsuarioAdmin(String userAdminId, String userId, String ip) {
		UserAdmin a = DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerUsuarioAdmin(userAdminId);
		em.remove(a);
		String descripcion = userId.toUpperCase() + " eliminó a " + userAdminId.toUpperCase() + " de listado de admin";
		persistirActividad(userId, ActivityType.ELIMINAR_ADMIN, descripcion, ip, null);
	}

	@Override
	public UserAdmin obtenerUsuarioAdmin(String idUsuarioAdmin) {
		return em.find(UserAdmin.class, idUsuarioAdmin);		
	}

	@Override
	public boolean agregarUsuarioAdmin(UserAdmin adm, String adminId, String ip) {
		try {

			if (DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerUsuarioAdmin(adm.getUserAdminId()) == null) {
				UserAdmin a = new UserAdmin();
				a.setUserAdminId(adm.getUserAdminId().toLowerCase());
				a.setPassword(adm.getPassword().toUpperCase());
				a.setEmail(adm.getEmail().toLowerCase());
				a.setIdElectionAuthorized(adm.getIdElectionAuthorized());
				em.persist(a);
				String descripcion = adminId.toUpperCase() + " agregó a " + adm.getUserAdminId().toUpperCase() + " como admin";
				persistirActividad(adminId, ActivityType.AGREGAR_ADMIN, descripcion, ip, adm.getIdElectionAuthorized());
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	public void editarUsuarioAdmin(UserAdmin usuarioAdmin, String email, Long idEleccionAutorizado, String userId, String ipClient) {
		try {
			usuarioAdmin.setIdElectionAuthorized(idEleccionAutorizado);
			em.merge(usuarioAdmin);
			String descripcion;
			if (userId.equalsIgnoreCase(usuarioAdmin.getUserAdminId())) {
				descripcion = userId.toUpperCase() + " editó  su email de " + email + " a " + usuarioAdmin.getEmail() + " y su elección autorizada a la de id " + idEleccionAutorizado;
			} else {
				descripcion = userId.toUpperCase() + " editó el email del usuario " + usuarioAdmin.getUserAdminId().toUpperCase() + " de " + email + " a " + usuarioAdmin.getEmail() + " y la elección autorizada a la de id " + idEleccionAutorizado;
			}
			persistirActividad(userId, ActivityType.EDITAR_ADMIN, descripcion, ipClient, idEleccionAutorizado);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void editarPassAdmin(String adminUserId, String password, String userId, String ip) {
		try {
			UserAdmin a = DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerUsuarioAdmin(adminUserId);
			a.setPassword(password);

			em.persist(a);
			String descripcion;
			if (userId.equalsIgnoreCase(adminUserId)) {
				descripcion = userId.toUpperCase() + " cambió su contraseña";
			} else {
				descripcion = userId.toUpperCase() + " cambió la contraseña de " + adminUserId.toUpperCase();
			}
			persistirActividad(userId, ActivityType.EDITAR_ADMIN, descripcion, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List<IpAccess> obtenerAccesosIps() {
		return DaoFactoryElecciones.createAccesoIpsDao(em).obtenerIpsInhabilitadasTodas();
	}

	@Override
	public List<Parameter> obtenerListadoParamteros() {
		return EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerListadoParametro();
	}

	@Override
	public boolean agregarParametro(String clave, String valor, String userId, String ip) {
		if (EJBFactory.getInstance().getParametrosEleccionesEJB().agregarParametro(clave, valor)) {
			String descripcion = userId.toUpperCase() + " creó el parámetro " + clave;
			persistirActividad(userId, ActivityType.AGREGAR_PARAMETRO, descripcion, ip, null);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void editarParametro(Parameter p, String userId, String ip) {
		try {
			EJBFactory.getInstance().getParametrosEleccionesEJB().editarParametro(p);
			String descripcion = userId.toUpperCase() + " actualizó el parámetro " + p.getKey();
			persistirActividad(userId, ActivityType.EDITAR_PARAMETRO, descripcion, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void borrarParametro(String clave, String userId, String ip) {
		try {
			String descripcion;
			EJBFactory.getInstance().getParametrosEleccionesEJB().borrarParametro(clave);
			descripcion = userId.toUpperCase() + " eliminó el parámetro " + clave + " del sistema";
			persistirActividad(userId, ActivityType.ELIMINAR_PARAMETRO, descripcion, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List<TemplateElection> obtenerTemplatesEleccion(long idEleccion) {
		if (idEleccion == 0)
			return DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplatesBase();
		else
			return DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplatesEleccion(idEleccion);

	}

	@Override
	public void modificarTemplateEleccion(TemplateElection t) {
		Election eleccion = t.getElection();
		if (eleccion != null)
			em.merge(eleccion);
		em.merge(t);
	}

	@Override
	public List<TemplateElection> obtenerTemplatesBase() {
		return DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplatesBase();
	}

	@Override
	public TemplateElection obtenerTemplate(String tipo, long idEleccion) {
		if (idEleccion == 0)
			return DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplateBase(tipo);
		else
			return DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplate(tipo, idEleccion);
	}

	@Override
	public List obtenerDestinatariosTipoDestinatario(TemplateElection t) throws Exception {
		RecipientType tipo = t.getRecipientType();
		if (tipo.equals(RecipientType.VOTANTES)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccion(t.getElection().getIdElection());
		} else if (tipo.equals(RecipientType.VOTANTES_BR)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccion(t.getElection().getIdElection(), "BR");
		} else if (tipo.equals(RecipientType.VOTANTES_MX)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccion(t.getElection().getIdElection(), "MX");
		}

		else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccionQueAunNoVotaron(t.getElection().getIdElection());
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON_BR)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccionQueAunNoVotaron(t.getElection().getIdElection(), "BR");
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON_MX)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccionQueAunNoVotaron(t.getElection().getIdElection(), "MX");
		}

		else if (tipo.equals(RecipientType.VOTANTES_QUE_YA_VOTARON)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccionQueYaVotaron(t.getElection().getIdElection());
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_YA_VOTARON_BR)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccionQueYaVotaron(t.getElection().getIdElection(), "BR");
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_YA_VOTARON_MX)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccionQueYaVotaron(t.getElection().getIdElection(), "MX");
		}

		else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccionQueNoVotaronEleccionDual(t.getElection().getIdElection());
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES_BR)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccionQueNoVotaronEleccionDual(t.getElection().getIdElection(), "BR");
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES_MX)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccionQueNoVotaronEleccionDual(t.getElection().getIdElection(), "MX");
		}

		else if (tipo.equals(RecipientType.AUDITORES)) {
			return DaoFactoryElecciones.createAuditorDao(em).obtenerAuditoresEleccion(t.getElection().getIdElection());
		} else if (tipo.equals(RecipientType.AUDITORES_QUE_AUN_NO_CONFORMARON)) {
			return DaoFactoryElecciones.createAuditorDao(em).obtenerAuditoresEleccionQueNoConformaron(t.getElection().getIdElection());
		} else if (tipo.equals(RecipientType.AUDITORES_QUE_YA_CONFORMARON)) {
			return DaoFactoryElecciones.createAuditorDao(em).obtenerAuditoresEleccionQueYaConformaron(t.getElection().getIdElection());
		} else
			throw new Exception("Nunca debería haber llegado aquí");
	}

	@Override
	public boolean agregarPlantillaBase(TemplateElection template, String userId, String ip) {
		try {
			template.setTemplateType(template.getTemplateType().toUpperCase());
			em.persist(template);
			String descripcion = userId.toUpperCase() + " creó un nuevo template base " + template.getTemplateType();
			persistirActividad(userId, ActivityType.AGREGAR_TEMPLATE_BASE, descripcion, ip, null);
			return true;
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Integer crearPlantillasEleccionesQueLeFalten() {
		int cuenta = 0;
		List<TemplateElection> bases = obtenerTemplatesBase();
		List<Election> eleccionesSinTemplate = obtenerEleccionesLight();
		for (int j = 0; j < eleccionesSinTemplate.size(); j++) {
			Election eleccionJ = eleccionesSinTemplate.get(j);
			for (int i = 0; i < bases.size(); i++) {
				TemplateElection templateI = bases.get(i);
				if (obtenerTemplate(templateI.getTemplateType(), eleccionJ.getIdElection()) == null) {
					em.persist(new TemplateElection(eleccionJ, templateI));
					cuenta++;
				}
			}
		}
		return cuenta;
	}

	@Override
	public void encolarEnvioMasivo(List usuarios, TemplateElection templateEleccion) {
		EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioMasivo(usuarios, templateEleccion);

	}

	@Override
	public Candidate obtenerCandidatoDEArriba(Candidate candidato) {
		return DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatoDeArriba(candidato.getElection().getIdElection(), candidato.getCandidateOrder());

	}

	@Override
	public Candidate obtenerCandidatoDEAbajo(Candidate candidato) {
		return DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatoDeAbajo(candidato.getElection().getIdElection(), candidato.getCandidateOrder());

	}

	@Override
	public void fijarCandidatoAlPrincipio(long idCandidato) {
		Candidate candidato = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
		Candidate candidatoViejo = DaoFactoryElecciones.createCandidatoDao(em).obtenerPrimerCandidato(candidato.getElection().getIdElection());
		if (candidatoViejo != null) {
			candidatoViejo.setCandidateOrder(candidato.getCandidateOrder());
		}
		candidato.setCandidateOrder(Constants.MAX_ORDER);
		em.persist(candidato);
		em.persist(candidatoViejo);
	}

	@Override
	public void nofijarCandidatoAlPrincipio(long idCandidato) {
		Candidate candidato = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
		int orden = DaoFactoryElecciones.createCandidatoDao(em).obtenerOrdenDelUltimoCandidatoNofijado(candidato.getElection().getIdElection());
		candidato.setCandidateOrder(orden + 1);
		em.persist(candidato);
	}

	@Override
	public void subirCandidato(long idCandidato) {
		Candidate candidato = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
		Candidate candidatoCercano = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatoDeArriba(candidato.getElection().getIdElection(), candidato.getCandidateOrder());
		if (candidatoCercano != null && candidatoCercano.getCandidateOrder() != Constants.MAX_ORDER) {
			appLogger.info(candidato.getCandidateOrder() + " - " + candidatoCercano.getCandidateOrder());

			int aux = candidato.getCandidateOrder();
			candidato.setCandidateOrder(candidatoCercano.getCandidateOrder());
			candidatoCercano.setCandidateOrder(aux);
			em.persist(candidato);
			em.persist(candidatoCercano);
		}

	}

	@Override
	public void bajarCandidato(long idCandidato) {
		Candidate candidato = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
		Candidate candidatoCercano = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatoDeAbajo(candidato.getElection().getIdElection(), candidato.getCandidateOrder());
		if (candidatoCercano != null && candidatoCercano.getCandidateOrder() != Constants.MIN_ORDER) {
			int aux = candidato.getCandidateOrder();
			appLogger.info(candidato.getCandidateOrder() + " - " + candidatoCercano.getCandidateOrder());

			candidato.setCandidateOrder(candidatoCercano.getCandidateOrder());
			candidatoCercano.setCandidateOrder(aux);
			em.persist(candidato);
			em.persist(candidatoCercano);
		}
	}

	@Override
	public void fijarCandidatoAlFinal(long idCandidato) {
		Candidate candidato = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
		Candidate candidatoViejo = DaoFactoryElecciones.createCandidatoDao(em).obtenerUltimoCandidato(candidato.getElection().getIdElection());
		if (candidatoViejo != null) {
			candidatoViejo.setCandidateOrder(candidato.getCandidateOrder());
		}
		candidato.setCandidateOrder(Constants.MIN_ORDER);
		em.persist(candidato);
		em.persist(candidatoViejo);
	}

	@Override
	public List<Email> obtenerEmailsAll() {
		return DaoFactoryElecciones.createEmailDao(em).obtenerEmailsAll();
	}

	@Override
	public List<Email> obtenerMailsPorEnviar() {
		return DaoFactoryElecciones.createEmailDao(em).obtenerEmailsParaEnviarElecciones();
	}

	@Override
	public List<Email> obtenerMailsDeEleccion(Long idEleccion) {
		return DaoFactoryElecciones.createEmailDao(em).obtenerMailsDeEleccion(idEleccion);
	}

	@Override
	public List<Email> obtenerMailsPorEnviarDeEleccion(Long idEleccion) {
		return DaoFactoryElecciones.createEmailDao(em).obtenerMailsPorEnviarDeEleccion(idEleccion);
	}

	@Override
	public List<Vote> obtenerVotos(Long idEleccion) {
		return DaoFactoryElecciones.createVotoDao(em).obtenerVotosEleccion(idEleccion);
	}

	@Override
	public boolean agregarComisionado(String nombre, String mail, String userId, String ip) {
		try {
			if (DaoFactoryElecciones.createComisionadoDao(em).obtenerComisionado(mail) == null) {
				Comissioner a = new Comissioner();
				a.setName(nombre);
				a.setMail(mail);
				em.persist(a);
				String descripcion = userId.toUpperCase() + " agregó al comisionado " + nombre;
				persistirActividad(userId, ActivityType.AGREGAR_COMISIONADO, descripcion, ip, null);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	public void eliminarComisionado(long idComisionado, String nombre, String userId, String ip) {
		Comissioner a = DaoFactoryElecciones.createComisionadoDao(em).obtenerComisionado(idComisionado);
		em.remove(a);
		String descripcion = userId.toUpperCase() + " eliminó al comisionado: " + nombre + " del sistema";
		persistirActividad(userId, ActivityType.ELIMINAR_COMISIONADO, descripcion, ip, null);
	}

	@Override
	public void editarComisionado(Comissioner comisionado, String userId, String ip) {
		try {
			em.merge(comisionado);
			persistirActividad(userId, ActivityType.EDITAR_COMISIONADO, userId + " ha editado el comidionado " + comisionado.getName(), ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public Comissioner obtenerComisionado(long idComisionado) {
		return DaoFactoryElecciones.createComisionadoDao(em).obtenerComisionado(idComisionado);

	}

	@Override
	public void reenviarEmailPadron(UserVoter us, Election e, String adminId, String ipClient) {
		TemplateElection tComienzo = DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplate("AVISO_ELECCION", e.getIdElection());
		TemplateElection tEmpezo = DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplate("COMIENZO_ELECCION", e.getIdElection());
		if (tComienzo != null || tEmpezo != null) {
			Date now = new Date();
			if (((now.after(e.getCreationDate())) && (now.before(e.getStartDate()))))
				EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioIndividual(tComienzo, us, null, e, new ArrayList<>());
			if ((e.isVotingLinkAvailable()) && ((now.after(e.getStartDate())) && (now.before(e.getEndDate()))))
				EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioIndividual(tEmpezo, us, null, e, new ArrayList<>());
			String descripcion = adminId.toUpperCase() + " reénvio el email al usuario padrón " + us.getName().toUpperCase() + " de la elección " + "(" + e.getTitleSpanish() + ")";
			persistirActividad(adminId, ActivityType.REENVIAR_EMAIL_ELECCION_USUPADRON, descripcion, ipClient, e.getIdElection());
		}
	}

	@Override
	public long obtenerIdEleccionUsuAdmin(String adminId) {
		return DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerIdEleccionUsuAdmin(adminId);
	}

	@Override
	public Parameter getParametro(String claveId) {
		return DaoFactoryElecciones.createParametroDao(em).getParametro(claveId);
	}

	@Override
	public boolean existeComisionado(String nombre, String email) {
		return DaoFactoryElecciones.createComisionadoDao(em).existeComisionado(nombre, email);
	}

	@Override
	public boolean existeAuditor(long idEleccion, String nombre, String email) {
		return DaoFactoryElecciones.createAuditorDao(em).existeAuditor(idEleccion, nombre, email);
	}

	@Override
	public String obtenerRemitentePorDefecto() {
		return EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constants.DEFAULT_SENDER);
	}

	@Override
	public String obtenerWebsitePorDefecto() {
		return EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constants.WEBSITE_DEFAULT);
	}

	@Override
	public void actualizarSupraEleccion(JointElection supraEleccion) {
		em.merge(supraEleccion);
	}

	@Override
	public List<JointElection> obtenerSupraElecciones() {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerSupraElecciones();
	}


	@Override
	public boolean isSupraEleccion(long idEleccion) {
		return !DaoFactoryElecciones.createEleccionDao(em).isEleccionSimple(idEleccion);
	}

	@Override
	public JointElection obtenerSupraEleccion(long idEleccion) {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerSupraEleccion(idEleccion);
	}

	@Override
	public void eliminarSupraEleccion(JointElection supraEleccion) {
		em.remove(em.contains(supraEleccion) ? supraEleccion : em.merge(supraEleccion));
	}

	@Override
	public boolean isPadronesIguales(JointElection supraEleccion) {
		return DaoFactoryElecciones.createUsuarioPadronDao(em).padronesIgualesEleccionDual(supraEleccion.getIdElectionA(), supraEleccion.getIdElectionB());
	}

	@Override
	public List<String> obtenerEleccionesIdDesc() {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerEleccionesIdDesc();
	}

	@Override
	public Customization getPersonalizacion() {
		return DaoFactoryElecciones.createPersonalizacionDao(em).getPersonalizacion();
	}

	@Override
	public boolean actualizarPersonalizacion(Customization personalizacion) {
		try {
			em.merge(personalizacion);
			return true;
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}
}