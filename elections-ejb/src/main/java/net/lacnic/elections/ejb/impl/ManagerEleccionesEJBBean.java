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

import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Commissioner;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.Customization;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.ElectionEmailTemplate;
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

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	@Override
	public UserAdmin loginAdmin(String adminId, String password, String ip) {
		UserAdmin a = ElectionsDaoFactory.createUsuarioAdminDao(em).verifyUserLogin(adminId, password);
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
		return ElectionsDaoFactory.createEleccionDao(em).getElectionsAllOrderCreationDate();
	}

	@Override
	public List<Election> obtenerEleccionesLightEsteAnio() {
		return ElectionsDaoFactory.createEleccionDao(em).getElectionsLightThisYear();
	}

	@Override
	public void eliminarCandidato(long idCandidato, String userId, String ip) {
		Candidate candidato = em.find(Candidate.class, idCandidato);
		em.remove(candidato);
		String descripcion = userId.toUpperCase() + " eliminó al candidato " + candidato.getName() + TEXT_ELECCION + candidato.getElection().getTitleSpanish();
		persistirActividad(userId, ActivityType.REMOVE_CANDIDATE, descripcion, ip, candidato.getElection().getElectionId());

		Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(candidato.getElection().getElectionId());
		eleccion.setCandidatesSet(eleccion.getCandidates().size() > 1);
		em.persist(eleccion);
	}

	@Override
	public void eliminarAuditor(long idAuditor, String userId, String ip) {
		Auditor auditor = em.find(Auditor.class, idAuditor);
		em.remove(auditor);
		String descripcion = userId.toUpperCase() + " eliminó al auditor " + auditor.getName() + TEXT_ELECCION + auditor.getElection().getTitleSpanish();
		persistirActividad(userId, ActivityType.REMOVE_AUDITOR, descripcion, ip, auditor.getElection().getElectionId());
	}

	@Override
	public void agregarCandidato(long idEleccion, Candidate candidato, String userId, String ip) {
		Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(idEleccion);
		candidato.setElection(eleccion);
		int orden = ElectionsDaoFactory.createCandidateDao(em).getLastNonFixedCandidateOrder(idEleccion);
		candidato.setCandidateOrder(orden + 1);
		em.persist(candidato);
		eleccion.setCandidatesSet(true);
		em.persist(eleccion);
		String descripcion = userId.toUpperCase() + " agregó un candidato para la elección  " + eleccion.getTitleSpanish();
		persistirActividad(userId, ActivityType.ADD_CANDIDATE, descripcion, ip, eleccion.getElectionId());
	}

	@Override
	public void editarCandidato(Candidate candidato, String userId, String ip) {
		try {
			em.merge(candidato);
			String descripcion = userId.toUpperCase() + " actualizó los datos de un candidato para la elección " + candidato.getElection().getTitleSpanish();
			persistirActividad(userId, ActivityType.EDIT_CANDIDATES, descripcion, ip, candidato.getElection().getElectionId());
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void agregarAuditor(long idEleccion, Auditor auditor, String tituloEspaniol, String userId, String ip) {
		Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(idEleccion);
		auditor.setElection(eleccion);
		em.persist(auditor);
		String descripcion = userId.toUpperCase() + " agregó un auditor para la elección " + tituloEspaniol;
		persistirActividad(userId, ActivityType.ADD_AUDITOR, descripcion, ip, idEleccion);
	}

	@Override
	public void persistirAuditoresSeteados(long idEleccion, String tituloEspaniol, String userId, String ip) {
		try {
			Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(idEleccion);
			eleccion.setAuditorsSet(true);
			em.persist(eleccion);
			String descripcion = userId.toUpperCase() + " agregó auditores para la elección " + tituloEspaniol;
			persistirActividad(userId, ActivityType.ADD_AUDITORS, descripcion, ip, idEleccion);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public Election obtenerEleccion(long idEleccion) {
		return ElectionsDaoFactory.createEleccionDao(em).getElection(idEleccion);
	}

	@Override
	public String obtenerLinkresultado(Election eleccion) throws Exception {
		return UtilsLinks.buildResultsLink(eleccion.getResultToken());
	}

	@Override
	public List<Candidate> obtenerCandidatosEleccionOrdenados(long idEleccion) {
		List<Candidate> candidatos = ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(idEleccion);
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
		return ElectionsDaoFactory.createCandidateDao(em).getCandidate(idCandidato);
	}

	@Override
	public void actualizarUsuariosPadron(long idEleccion, byte[] contenido, String admin, String ip) throws CensusValidationException, Exception {
		try {
			List<UserVoter> usuariosPadron = UtilsExcel.procesarExcelPadronNuevo(contenido);
			Election eleccion = em.find(Election.class, idEleccion);
			eleccion.setElectorsSet(true);
			ElectionsDaoFactory.createVotoDao(em).deleteElectionVotes(idEleccion);
			ElectionsDaoFactory.createUsuarioPadronDao(em).deleteElectionCensus(idEleccion);

			for (UserVoter usuarioPadron : usuariosPadron) {
				usuarioPadron.setElection(eleccion);
				usuarioPadron.setVoteToken(StringUtils.createSecureToken());
				em.persist(usuarioPadron);
			}
			em.persist(eleccion);

			String descripcion = admin.toUpperCase() + " agregó el padrón para la elección  " + "(" + eleccion.getTitleSpanish() + ")";
			persistirActividad(admin, ActivityType.ADD_CENSUS, descripcion, ip, idEleccion);

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
			if(!ElectionsDaoFactory.createUsuarioPadronDao(em).userVoterExistsByMailElection(idEleccion, usuarioPadron.getMail())) {
				Election eleccion = em.find(Election.class, idEleccion);
				usuarioPadron.setElection(eleccion);
				usuarioPadron.setVoteToken(StringUtils.createSecureToken());
				em.persist(usuarioPadron);
				eleccion.setElectorsSet(true);
				String descripcion = userId.toUpperCase() + " agregó a " + usuarioPadron.getName() + " como usuario padrón para la elección " + eleccion.getTitleSpanish();
				persistirActividad(userId, ActivityType.ADD_VOTE_USER, descripcion, ip, idEleccion);
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
			if(!ElectionsDaoFactory.createUsuarioPadronDao(em).userVoterExistsByMailElection(usuarioPadron.getElection().getElectionId(), usuarioPadron.getMail())) {
				em.merge(usuarioPadron);
				String descripcion = userId.toUpperCase() + " actualizó los datos de un usuario padrón para la elección  " + usuarioPadron.getElection().getTitleSpanish();
				persistirActividad(userId, ActivityType.EDIT_VOTE_USER, descripcion, ip, usuarioPadron.getElection().getElectionId());
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
		persistirActividad(userId, ActivityType.UPDATE_TOKEN_USER_CENSUS, descripcion, ip, u.getElection().getElectionId());
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
			List<UserAdmin> usuAdmin = ElectionsDaoFactory.createUsuarioAdminDao(em).getElectionUserAdmins(idEleccion);
			List<Auditor> auditores = ElectionsDaoFactory.createAuditorDao(em).getElectionAuditors(idEleccion);
			List<Candidate> candidatos = ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(idEleccion);
			List<Email> emails = ElectionsDaoFactory.createEmailDao(em).getElectionEmails(idEleccion);
			List<ElectionEmailTemplate> templates = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplates(idEleccion);
			List<UserVoter> usuariosP = ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionUserVoters(idEleccion);
			List<Vote> votos = ElectionsDaoFactory.createVotoDao(em).getElectionVotes(idEleccion);
			Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(idEleccion);
			for (UserAdmin ad : usuAdmin)
				em.remove(ad);
			for (Auditor a : auditores)
				em.remove(a);
			for (Candidate c : candidatos)
				em.remove(c);
			for (Email e : emails)
				em.remove(e);
			for (ElectionEmailTemplate t : templates)
				em.remove(t);
			for (UserVoter u : usuariosP)
				em.remove(u);
			for (Vote v : votos)
				em.remove(v);
			em.remove(eleccion);
			String descripcion = userId.toUpperCase() + " eliminó la elección " + titulo;
			persistirActividad(userId, ActivityType.DELETE_ELECTION, descripcion, ip, idEleccion);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List<Commissioner> obtenerComisionados() {
		return ElectionsDaoFactory.createCommissionerDao(em).getCommissionerAll();
	}

	@Override
	public List<Auditor> obtenerAuditoresEleccion(long idEleccion) throws Exception {
		return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditors(idEleccion);
	}

	@Override
	public Auditor obtenerAuditor(long idAuditor) {
		return ElectionsDaoFactory.createAuditorDao(em).getAuditor(idAuditor);
	}

	@Override
	public void editarAuditor(Auditor auditor, String userId, String ip) {
		try {
			em.merge(auditor);
			String descripcion = userId.toUpperCase() + " actualizó los datos de un auditor para la elección " + auditor.getElection().getTitleSpanish();
			persistirActividad(userId, ActivityType.EDIT_AUDITOR, descripcion, ip, auditor.getElection().getElectionId());
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
		UserVoter usuario = em.find(UserVoter.class, actual.getUserVoterId());
		Long idEleccion = usuario.getElection().getElectionId();
		em.remove(usuario);
		String descripcion = userId.toUpperCase() + " eliminó a " + usuario.getName() + " del listado de usuario padrón en la elección " + titulo;
		persistirActividad(userId, ActivityType.REMOVE_USER_CENSUS, descripcion, ip, idEleccion);
		long cantidadCandidatos = ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionCensusSize(usuario.getElection().getElectionId());
		if (cantidadCandidatos < 1) {
			Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(usuario.getElection().getElectionId());
			eleccion.setElectorsSet(false);
			em.persist(eleccion);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Election actualizarEleccion(Election eleccion, String userId, String ip) throws Exception {
		try {
			String descripcion;
			if (eleccion.getElectionId() == 0) {
				em.persist(eleccion);
				descripcion = userId.toUpperCase() + " creó la elección" + " (" + eleccion.getTitleSpanish() + ")" + " correctamente";
				persistirActividad(userId, ActivityType.CREATE_ELECTION, descripcion, ip, eleccion.getElectionId());
				crearPlantillasEleccion(eleccion); // nuevo
				List<Commissioner> comisionados = ElectionsDaoFactory.createCommissionerDao(em).getCommissionerAll();
				for (Commissioner comisionado : comisionados) {
					em.persist(new Auditor(eleccion, comisionado));
				}
			} else
				em.merge(eleccion);
			descripcion = userId.toUpperCase() + " actualizó la elección " + " (" + eleccion.getTitleSpanish() + ")" + " correctamente";
			persistirActividad(userId, ActivityType.EDIT_ELECTION, descripcion, ip, eleccion.getElectionId());
		} catch (Exception e) {
			appLogger.error(e);
		}

		return eleccion;
	}

	@Override
	public void crearPlantillasEleccion(Election eleccion) {
		List<ElectionEmailTemplate> bases = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplates();
		for (int i = 0; i < bases.size(); i++) {
			ElectionEmailTemplate templateI = bases.get(i);
			if (obtenerTemplate(templateI.getTemplateType(), eleccion.getElectionId()) == null) {
				em.persist(new ElectionEmailTemplate(eleccion, templateI));
			}
		}

	}

	@Override
	public List<UserAdmin> obtenerUsuariosAdmin() {
		return ElectionsDaoFactory.createUsuarioAdminDao(em).getUserAdminsAll();
	}

	@Override
	public void habilitarLinkVotacion(Long id, Boolean valor, String admin, String ip) {
		try {
			Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(id);
			eleccion.setVotingLinkAvailable(valor);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de votación para la elección ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = "habilitó el link de votación para la elección ";
			}
			String descripcion = admin.toUpperCase() + " " + strValor + "(" + eleccion.getTitleSpanish() + ")";
			persistirActividad(admin, ActivityType.ENABLE_VOTE_LINK, descripcion, ip, eleccion.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void ordenarCandidatosAleatoriamente(Long id, Boolean valor) {
		try {
			Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(id);
			eleccion.setRandomOrderCandidates(valor);
			em.persist(eleccion);
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void habilitarLinkResultado(Long id, Boolean valor, String admin, String ip) {
		try {
			Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(id);
			eleccion.setResultLinkAvailable(valor);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de resultado para la elección ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = "habilitó el link de resultado para la elección ";
			}
			String descripcion = admin.toUpperCase() + " " + strValor + "(" + eleccion.getTitleSpanish() + ")";
			persistirActividad(admin, ActivityType.ENABLE_RESULTS_LINK, descripcion, ip, eleccion.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void habilitarLinkAuditoria(Long id, Boolean valor, String admin, String ip) {
		try {
			Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(id);
			eleccion.setAuditorLinkAvailable(valor);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de auditoria para la elección ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = "habilitó el link de auditoria para la elección ";
			};
			String descripcion = admin.toUpperCase() + " " + strValor + "(" + eleccion.getTitleSpanish() + ")";
			persistirActividad(admin, ActivityType.ENABLE_AUDIT_LINK, descripcion, ip, eleccion.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void solicitarRevision(Long idEleccion, Boolean valor, String admin, String ip) {
		try {
			Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(idEleccion);

			habilitarLinkAuditoria(idEleccion, valor, admin, ip);

			if (!(Boolean.TRUE.equals(valor))) {
				List<Auditor> auditores = eleccion.getAuditors();
				for (Auditor auditor : auditores) {
					auditor.setRevisionAvailable(false);
					em.persist(auditor);
					String descripcion = admin.toUpperCase() + " cerró el proceso de revisión y se revocó la autorizacion de la revisión del auditor: " + auditor.getAuditorId() + " - " + auditor.getName() + TEXT_ELECCION + "(" + eleccion.getTitleSpanish() + ")";
					persistirActividad(admin, ActivityType.ELECTION_REVISION_NO, descripcion, ip, eleccion.getElectionId());
				}
			}
			eleccion.setRevisionRequest(valor);
			em.persist(eleccion);
			String strValor =  " revocó la solicitud de ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = " solicitó la ";
			};
			String descripcion = admin.toUpperCase() + strValor + "revisión para la elección " + "(" + eleccion.getTitleSpanish() + ")";
			persistirActividad(admin, ActivityType.ELECTION_REVISION, descripcion, ip, eleccion.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public boolean isRevisionActiva(long idEleccion, String admin, String ip) {
		Election eleccion = ElectionsDaoFactory.createEleccionDao(em).getElection(idEleccion);
		List<Auditor> auditores = eleccion.getAuditors();
		if (!eleccion.isRevisionRequest())
			return false;

		for (Auditor auditor : auditores) {
			if (auditor.isCommissioner() && !auditor.isRevisionAvailable())
				return false;
		}

		String descripcion = admin.toUpperCase() + " ingresó a la revisión de votos para la elección " + "(" + eleccion.getTitleSpanish() + ")";
		persistirActividad(admin, ActivityType.ENTER_TO_REVISION, descripcion, ip, eleccion.getElectionId());
		return true;
	}

	@Override
	public List<UserVoter> obtenerUsuariosPadronEleccion(long idEleccion) {
		return ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionUserVoters(idEleccion);
	}

	@Override
	public List<Activity> obtenerTodasLasActividades() {
		return ElectionsDaoFactory.createActivityDao(em).getActivitiesAll();
	}

	@Override
	public List<Activity> obtenerTodasLasActividades(long idEleccion) {
		return ElectionsDaoFactory.createActivityDao(em).getElectionActivities(idEleccion);
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
		return ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionUserVoters(idEleccion);
	}

	@Override
	public void eliminarUsuarioAdmin(String userAdminId, String userId, String ip) {
		UserAdmin a = ElectionsDaoFactory.createUsuarioAdminDao(em).getUserAdmin(userAdminId);
		em.remove(a);
		String descripcion = userId.toUpperCase() + " eliminó a " + userAdminId.toUpperCase() + " de listado de admin";
		persistirActividad(userId, ActivityType.REMOVE_ADMIN, descripcion, ip, null);
	}

	@Override
	public UserAdmin obtenerUsuarioAdmin(String idUsuarioAdmin) {
		return em.find(UserAdmin.class, idUsuarioAdmin);		
	}

	@Override
	public boolean agregarUsuarioAdmin(UserAdmin adm, String adminId, String ip) {
		try {

			if (ElectionsDaoFactory.createUsuarioAdminDao(em).getUserAdmin(adm.getUserAdminId()) == null) {
				UserAdmin a = new UserAdmin();
				a.setUserAdminId(adm.getUserAdminId().toLowerCase());
				a.setPassword(adm.getPassword().toUpperCase());
				a.setEmail(adm.getEmail().toLowerCase());
				a.setAuthorizedElectionId(adm.getAuthorizedElectionId());
				em.persist(a);
				String descripcion = adminId.toUpperCase() + " agregó a " + adm.getUserAdminId().toUpperCase() + " como admin";
				persistirActividad(adminId, ActivityType.ADD_ADMIN, descripcion, ip, adm.getAuthorizedElectionId());
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
			usuarioAdmin.setAuthorizedElectionId(idEleccionAutorizado);
			em.merge(usuarioAdmin);
			String descripcion;
			if (userId.equalsIgnoreCase(usuarioAdmin.getUserAdminId())) {
				descripcion = userId.toUpperCase() + " editó  su email de " + email + " a " + usuarioAdmin.getEmail() + " y su elección autorizada a la de id " + idEleccionAutorizado;
			} else {
				descripcion = userId.toUpperCase() + " editó el email del usuario " + usuarioAdmin.getUserAdminId().toUpperCase() + " de " + email + " a " + usuarioAdmin.getEmail() + " y la elección autorizada a la de id " + idEleccionAutorizado;
			}
			persistirActividad(userId, ActivityType.EDIT_ADMIN, descripcion, ipClient, idEleccionAutorizado);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void editarPassAdmin(String adminUserId, String password, String userId, String ip) {
		try {
			UserAdmin a = ElectionsDaoFactory.createUsuarioAdminDao(em).getUserAdmin(adminUserId);
			a.setPassword(password);

			em.persist(a);
			String descripcion;
			if (userId.equalsIgnoreCase(adminUserId)) {
				descripcion = userId.toUpperCase() + " cambió su contraseña";
			} else {
				descripcion = userId.toUpperCase() + " cambió la contraseña de " + adminUserId.toUpperCase();
			}
			persistirActividad(userId, ActivityType.EDIT_ADMIN, descripcion, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List<IpAccess> obtenerAccesosIps() {
		return ElectionsDaoFactory.createIpAccessDao(em).getAllDisabledIPs();
	}

	@Override
	public List<Parameter> obtenerListadoParamteros() {
		return EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerListadoParametro();
	}

	@Override
	public boolean agregarParametro(String clave, String valor, String userId, String ip) {
		if (EJBFactory.getInstance().getParametrosEleccionesEJB().agregarParametro(clave, valor)) {
			String descripcion = userId.toUpperCase() + " creó el parámetro " + clave;
			persistirActividad(userId, ActivityType.ADD_PARAMETER, descripcion, ip, null);
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
			persistirActividad(userId, ActivityType.EDIT_PARAMETER, descripcion, ip, null);
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
			persistirActividad(userId, ActivityType.DELET_PARAMETER, descripcion, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List<ElectionEmailTemplate> obtenerTemplatesEleccion(long idEleccion) {
		if (idEleccion == 0)
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplates();
		else
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplates(idEleccion);

	}

	@Override
	public void modificarTemplateEleccion(ElectionEmailTemplate t) {
		Election eleccion = t.getElection();
		if (eleccion != null)
			em.merge(eleccion);
		em.merge(t);
	}

	@Override
	public List<ElectionEmailTemplate> obtenerTemplatesBase() {
		return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplates();
	}

	@Override
	public ElectionEmailTemplate obtenerTemplate(String tipo, long idEleccion) {
		if (idEleccion == 0)
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplate(tipo);
		else
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(tipo, idEleccion);
	}

	@Override
	public List obtenerDestinatariosTipoDestinatario(ElectionEmailTemplate t) throws Exception {
		RecipientType tipo = t.getRecipientType();
		if (tipo.equals(RecipientType.VOTANTES)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionUserVoters(t.getElection().getElectionId());
		} else if (tipo.equals(RecipientType.VOTANTES_BR)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionCensusByCountry(t.getElection().getElectionId(), "BR");
		} else if (tipo.equals(RecipientType.VOTANTES_MX)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionCensusByCountry(t.getElection().getElectionId(), "MX");
		}

		else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionsUserVotersNotVotedYet(t.getElection().getElectionId());
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON_BR)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionsUserVotersNotVotedYetByCountry(t.getElection().getElectionId(), "BR");
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON_MX)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionsUserVotersNotVotedYetByCountry(t.getElection().getElectionId(), "MX");
		}

		else if (tipo.equals(RecipientType.VOTANTES_QUE_YA_VOTARON)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionsUserVotersVoted(t.getElection().getElectionId());
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_YA_VOTARON_BR)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionsUserVotersVotedByCountry(t.getElection().getElectionId(), "BR");
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_YA_VOTARON_MX)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getElectionsUserVotersVotedByCountry(t.getElection().getElectionId(), "MX");
		}

		else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getJointElectionUserVotersNotVotedYet(t.getElection().getElectionId());
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES_BR)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getJointElectionUserVotersNotVotedYetByCountry(t.getElection().getElectionId(), "BR");
		} else if (tipo.equals(RecipientType.VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES_MX)) {
			return ElectionsDaoFactory.createUsuarioPadronDao(em).getJointElectionUserVotersNotVotedYetByCountry(t.getElection().getElectionId(), "MX");
		}

		else if (tipo.equals(RecipientType.AUDITORES)) {
			return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditors(t.getElection().getElectionId());
		} else if (tipo.equals(RecipientType.AUDITORES_QUE_AUN_NO_CONFORMARON)) {
			return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditorsNotAgreedConformity(t.getElection().getElectionId());
		} else if (tipo.equals(RecipientType.AUDITORES_QUE_YA_CONFORMARON)) {
			return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditorsAgreedConformity(t.getElection().getElectionId());
		} else
			throw new Exception("Nunca debería haber llegado aquí");
	}

	@Override
	public boolean agregarPlantillaBase(ElectionEmailTemplate template, String userId, String ip) {
		try {
			template.setTemplateType(template.getTemplateType().toUpperCase());
			em.persist(template);
			String descripcion = userId.toUpperCase() + " creó un nuevo template base " + template.getTemplateType();
			persistirActividad(userId, ActivityType.ADD_BASE_TEMPLATE, descripcion, ip, null);
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
		List<ElectionEmailTemplate> bases = obtenerTemplatesBase();
		List<Election> eleccionesSinTemplate = obtenerEleccionesLight();
		for (int j = 0; j < eleccionesSinTemplate.size(); j++) {
			Election eleccionJ = eleccionesSinTemplate.get(j);
			for (int i = 0; i < bases.size(); i++) {
				ElectionEmailTemplate templateI = bases.get(i);
				if (obtenerTemplate(templateI.getTemplateType(), eleccionJ.getElectionId()) == null) {
					em.persist(new ElectionEmailTemplate(eleccionJ, templateI));
					cuenta++;
				}
			}
		}
		return cuenta;
	}

	@Override
	public void encolarEnvioMasivo(List usuarios, ElectionEmailTemplate templateEleccion) {
		EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioMasivo(usuarios, templateEleccion);

	}

	@Override
	public Candidate obtenerCandidatoDEArriba(Candidate candidato) {
		return ElectionsDaoFactory.createCandidateDao(em).getNextAboveCandidate(candidato.getElection().getElectionId(), candidato.getCandidateOrder());

	}

	@Override
	public Candidate obtenerCandidatoDEAbajo(Candidate candidato) {
		return ElectionsDaoFactory.createCandidateDao(em).getNextBelowCandidate(candidato.getElection().getElectionId(), candidato.getCandidateOrder());

	}

	@Override
	public void fijarCandidatoAlPrincipio(long idCandidato) {
		Candidate candidato = ElectionsDaoFactory.createCandidateDao(em).getCandidate(idCandidato);
		Candidate candidatoViejo = ElectionsDaoFactory.createCandidateDao(em).getElectionFirstCandidate(candidato.getElection().getElectionId());
		if (candidatoViejo != null) {
			candidatoViejo.setCandidateOrder(candidato.getCandidateOrder());
		}
		candidato.setCandidateOrder(Constants.MAX_ORDER);
		em.persist(candidato);
		em.persist(candidatoViejo);
	}

	@Override
	public void nofijarCandidatoAlPrincipio(long idCandidato) {
		Candidate candidato = ElectionsDaoFactory.createCandidateDao(em).getCandidate(idCandidato);
		int orden = ElectionsDaoFactory.createCandidateDao(em).getLastNonFixedCandidateOrder(candidato.getElection().getElectionId());
		candidato.setCandidateOrder(orden + 1);
		em.persist(candidato);
	}

	@Override
	public void subirCandidato(long idCandidato) {
		Candidate candidato = ElectionsDaoFactory.createCandidateDao(em).getCandidate(idCandidato);
		Candidate candidatoCercano = ElectionsDaoFactory.createCandidateDao(em).getNextAboveCandidate(candidato.getElection().getElectionId(), candidato.getCandidateOrder());
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
		Candidate candidato = ElectionsDaoFactory.createCandidateDao(em).getCandidate(idCandidato);
		Candidate candidatoCercano = ElectionsDaoFactory.createCandidateDao(em).getNextBelowCandidate(candidato.getElection().getElectionId(), candidato.getCandidateOrder());
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
		Candidate candidato = ElectionsDaoFactory.createCandidateDao(em).getCandidate(idCandidato);
		Candidate candidatoViejo = ElectionsDaoFactory.createCandidateDao(em).getElectionLastCandidate(candidato.getElection().getElectionId());
		if (candidatoViejo != null) {
			candidatoViejo.setCandidateOrder(candidato.getCandidateOrder());
		}
		candidato.setCandidateOrder(Constants.MIN_ORDER);
		em.persist(candidato);
		em.persist(candidatoViejo);
	}

	@Override
	public List<Email> obtenerEmailsAll() {
		return ElectionsDaoFactory.createEmailDao(em).getEmailsAll();
	}

	@Override
	public List<Email> obtenerMailsPorEnviar() {
		return ElectionsDaoFactory.createEmailDao(em).getPendingSendEmails();
	}

	@Override
	public List<Email> obtenerMailsDeEleccion(Long idEleccion) {
		return ElectionsDaoFactory.createEmailDao(em).getElectionEmails(idEleccion);
	}

	@Override
	public List<Email> obtenerMailsPorEnviarDeEleccion(Long idEleccion) {
		return ElectionsDaoFactory.createEmailDao(em).getElectionPendingSendEmails(idEleccion);
	}

	@Override
	public List<Vote> obtenerVotos(Long idEleccion) {
		return ElectionsDaoFactory.createVotoDao(em).getElectionVotes(idEleccion);
	}

	@Override
	public boolean agregarComisionado(String nombre, String mail, String userId, String ip) {
		try {
			if (ElectionsDaoFactory.createCommissionerDao(em).getCommissionerByMail(mail) == null) {
				Commissioner a = new Commissioner();
				a.setName(nombre);
				a.setMail(mail);
				em.persist(a);
				String descripcion = userId.toUpperCase() + " agregó al comisionado " + nombre;
				persistirActividad(userId, ActivityType.ADD_COMMISSIONER, descripcion, ip, null);
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
		Commissioner a = ElectionsDaoFactory.createCommissionerDao(em).getCommissioner(idComisionado);
		em.remove(a);
		String descripcion = userId.toUpperCase() + " eliminó al comisionado: " + nombre + " del sistema";
		persistirActividad(userId, ActivityType.REMOVE_COMMISSIONER, descripcion, ip, null);
	}

	@Override
	public void editarComisionado(Commissioner comisionado, String userId, String ip) {
		try {
			em.merge(comisionado);
			persistirActividad(userId, ActivityType.EDIT_COMMISSIONER, userId + " ha editado el comidionado " + comisionado.getName(), ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public Commissioner obtenerComisionado(long idComisionado) {
		return ElectionsDaoFactory.createCommissionerDao(em).getCommissioner(idComisionado);

	}

	@Override
	public void reenviarEmailPadron(UserVoter us, Election e, String adminId, String ipClient) {
		ElectionEmailTemplate tComienzo = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(Constants.TemplateTypeELECTION_NOTICE, e.getElectionId());
		ElectionEmailTemplate tEmpezo = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(Constants.TemplateTypeELECTION_START, e.getElectionId());
		if (tComienzo != null || tEmpezo != null) {
			Date now = new Date();
			if (((now.after(e.getCreationDate())) && (now.before(e.getStartDate()))))
				EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioIndividual(tComienzo, us, null, e, new ArrayList<>());
			if ((e.isVotingLinkAvailable()) && ((now.after(e.getStartDate())) && (now.before(e.getEndDate()))))
				EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioIndividual(tEmpezo, us, null, e, new ArrayList<>());
			String descripcion = adminId.toUpperCase() + " reénvio el email al usuario padrón " + us.getName().toUpperCase() + " de la elección " + "(" + e.getTitleSpanish() + ")";
			persistirActividad(adminId, ActivityType.RESEND_EMAIL_ELECTION_USER_CENSUS, descripcion, ipClient, e.getElectionId());
		}
	}

	@Override
	public long obtenerIdEleccionUsuAdmin(String adminId) {
		return ElectionsDaoFactory.createUsuarioAdminDao(em).getUserAuthorizedElectionId(adminId);
	}

	@Override
	public Parameter getParametro(String claveId) {
		return ElectionsDaoFactory.createParameterDao(em).getParameter(claveId);
	}

	@Override
	public boolean existeComisionado(String nombre, String email) {
		return ElectionsDaoFactory.createCommissionerDao(em).commissionerExists(nombre, email);
	}

	@Override
	public boolean existeAuditor(long idEleccion, String nombre, String email) {
		return ElectionsDaoFactory.createAuditorDao(em).auditorExists(idEleccion, nombre, email);
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
		return ElectionsDaoFactory.createEleccionDao(em).getJointElectionsAll();
	}


	@Override
	public boolean isSupraEleccion(long idEleccion) {
		return !ElectionsDaoFactory.createEleccionDao(em).isEleccionSimple(idEleccion);
	}

	@Override
	public JointElection obtenerSupraEleccion(long idEleccion) {
		return ElectionsDaoFactory.createEleccionDao(em).getJointElectionForElection(idEleccion);
	}

	@Override
	public void eliminarSupraEleccion(JointElection supraEleccion) {
		em.remove(em.contains(supraEleccion) ? supraEleccion : em.merge(supraEleccion));
	}

	@Override
	public boolean isPadronesIguales(JointElection supraEleccion) {
		return ElectionsDaoFactory.createUsuarioPadronDao(em).electionsCensusEqual(supraEleccion.getIdElectionA(), supraEleccion.getIdElectionB());
	}

	@Override
	public List<String> obtenerEleccionesIdDesc() {
		return ElectionsDaoFactory.createEleccionDao(em).getElectionsAllIdAndTitle();
	}

	@Override
	public Customization getPersonalizacion() {
		return ElectionsDaoFactory.createCustomizationDao(em).getCustomization();
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