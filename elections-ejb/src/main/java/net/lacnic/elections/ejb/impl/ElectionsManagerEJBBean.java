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
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Commissioner;
import net.lacnic.elections.domain.Customization;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.RecipientType;
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.ejb.ElectionsManagerEJB;
import net.lacnic.elections.exception.CensusValidationException;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.StringUtils;
import net.lacnic.elections.utils.ExcelUtils;
import net.lacnic.elections.utils.FilesUtils;
import net.lacnic.elections.utils.LinksUtils;


@Stateless
@Remote(ElectionsManagerEJB.class)
public class ElectionsManagerEJBBean implements ElectionsManagerEJB {

	private static final String TEXT_ELECCION = " para la elección ";

	private Random rand = new SecureRandom();

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;


	public ElectionsManagerEJBBean() { }


	@Override
	public UserAdmin userAdminLogin(String userAdminId, String password, String ip) {
		UserAdmin a = ElectionsDaoFactory.createUserAdminDao(em).verifyUserLogin(userAdminId, password);
		try {
			if(a != null) {
				String descripcion = userAdminId.toUpperCase() + " se ha logueado exitosamente";
				EJBFactory.getInstance().getElectionsManagerEJB().persistActivity(userAdminId, ActivityType.LOGIN_SUCCESSFUL, descripcion, ip, null);
			} else {
				String descripcion = "Intento fallido de login de usuario " + userAdminId.toUpperCase();
				EJBFactory.getInstance().getElectionsManagerEJB().persistActivity(userAdminId, ActivityType.LOGIN_FAILED, descripcion, ip, null);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return a;
	}

	@Override
	public List<UserAdmin> getUserAdminsAll() {
		return ElectionsDaoFactory.createUserAdminDao(em).getUserAdminsAll();
	}

	@Override
	public UserAdmin getUserAdmin(String userAdminId) {
		return em.find(UserAdmin.class, userAdminId);		
	}

	@Override
	public boolean isValidCaptchaResponse(String reCaptchaResponse) {

		try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
			appLogger.info("start verifying ...");
			appLogger.info("reCAPTCHA response value : " + reCaptchaResponse);

			String skGoogleApiReCaptcha = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.SkGoogleApiReCaptcha);
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
			return EJBFactory.getInstance().getElectionsParametersEJB().isProd();
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	public Election getElection(long electionId) {
		return ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
	}

	@Override
	public List<Candidate> getElectionCandidatesOrdered(long electionId) {
		List<Candidate> candidates = ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(electionId);
		Election election = getElection(electionId);
		Collections.sort(candidates, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate candidate1, Candidate candidate2) {
				int result;

				if (candidate1.getCandidateOrder() == Constants.MIN_ORDER)
					result = Constants.MIN_ORDER;
				else if (candidate2.getCandidateOrder() == Constants.MAX_ORDER)
					result = Constants.MAX_ORDER;
				else if (election.isRandomOrderCandidates()) {
					result = Integer.valueOf(rand.nextInt(30)).compareTo(Integer.valueOf(rand.nextInt(30)));
				} else
					result = (Integer.valueOf(candidate2.getCandidateOrder()).compareTo(Integer.valueOf(candidate1.getCandidateOrder())));
				return result;
			}
		});
		return candidates;
	}

	@Override
	public void removeElection(long electionId, String electionTitle, String userAdminId, String ip) throws Exception {
		try {
			List<UserAdmin> userAdmins = ElectionsDaoFactory.createUserAdminDao(em).getElectionUserAdmins(electionId);
			List<Auditor> auditors = ElectionsDaoFactory.createAuditorDao(em).getElectionAuditors(electionId);
			List<Candidate> candidates = ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(electionId);
			List<Email> emails = ElectionsDaoFactory.createEmailDao(em).getElectionEmails(electionId);
			List<ElectionEmailTemplate> emailTemplates = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplates(electionId);
			List<UserVoter> userVoters = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionId);
			List<Vote> votes = ElectionsDaoFactory.createVoteDao(em).getElectionVotes(electionId);
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			for (UserAdmin userAdmin : userAdmins)
				em.remove(userAdmin);
			for (Auditor auditor : auditors)
				em.remove(auditor);
			for (Candidate candidate : candidates)
				em.remove(candidate);
			for (Email email : emails)
				em.remove(email);
			for (ElectionEmailTemplate emailTemplate : emailTemplates)
				em.remove(emailTemplate);
			for (UserVoter userVoter : userVoters)
				em.remove(userVoter);
			for (Vote vote : votes)
				em.remove(vote);
			em.remove(election);
			String description = userAdminId.toUpperCase() + " removed election " + electionTitle;
			persistActivity(userAdminId, ActivityType.DELETE_ELECTION, description, ip, electionId);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List<Auditor> getElectionAuditors(long electionId) throws Exception {
		return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditors(electionId);
	}

	@Override
	public List<Election> getElectionsAllOrderCreationDate() {
		return ElectionsDaoFactory.createElectionDao(em).getElectionsAllOrderCreationDate();
	}

	@Override
	public UserVoter getUserVoter(long userVoterId) {
		return em.find(UserVoter.class, userVoterId);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Election updateElection(Election election, String userAdminId, String ip) throws Exception {
		try {
			String descripcion;
			if (election.getElectionId() == 0) {
				em.persist(election);
				descripcion = userAdminId.toUpperCase() + " creó la elección" + " (" + election.getTitleSpanish() + ")" + " correctamente";
				persistActivity(userAdminId, ActivityType.CREATE_ELECTION, descripcion, ip, election.getElectionId());
				createElectionEmailTemplates(election); // nuevo
				List<Commissioner> comisionados = ElectionsDaoFactory.createCommissionerDao(em).getCommissionersAll();
				for (Commissioner comisionado : comisionados) {
					em.persist(new Auditor(election, comisionado));
				}
			} else
				em.merge(election);
			descripcion = userAdminId.toUpperCase() + " actualizó la elección " + " (" + election.getTitleSpanish() + ")" + " correctamente";
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, descripcion, ip, election.getElectionId());
		} catch (Exception e) {
			appLogger.error(e);
		}

		return election;
	}

	@Override
	public String getResultsLink(Election election) throws Exception {
		return LinksUtils.buildResultsLink(election.getResultToken());
	}

	@Override
	public void setResultsLinkStatus(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election eleccion = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			eleccion.setResultLinkAvailable(status);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de resultado para la elección ";
			if (Boolean.TRUE.equals(status)) {
				strValor = "habilitó el link de resultado para la elección ";
			}
			String descripcion = userAdminId.toUpperCase() + " " + strValor + "(" + eleccion.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.ENABLE_RESULTS_LINK, descripcion, ip, eleccion.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void setAuditLinkStatus(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election eleccion = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			eleccion.setAuditorLinkAvailable(status);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de auditoria para la elección ";
			if (Boolean.TRUE.equals(status)) {
				strValor = "habilitó el link de auditoria para la elección ";
			}
			String descripcion = userAdminId.toUpperCase() + " " + strValor + "(" + eleccion.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.ENABLE_AUDIT_LINK, descripcion, ip, eleccion.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public List<Commissioner> getCommissionersAll() {
		return ElectionsDaoFactory.createCommissionerDao(em).getCommissionersAll();
	}

	@Override
	public void removeUserVoter(UserVoter userVoter, String electionTitle, String userAdminId, String ip) {
		UserVoter userVoterDB = em.find(UserVoter.class, userVoter.getUserVoterId());
		Long idEleccion = userVoterDB.getElection().getElectionId();
		em.remove(userVoterDB);
		String descripcion = userAdminId.toUpperCase() + " eliminó a " + userVoterDB.getName() + " del listado de usuario padrón en la elección " + electionTitle;
		persistActivity(userAdminId, ActivityType.REMOVE_USER_CENSUS, descripcion, ip, idEleccion);
		long cantidadCandidatos = ElectionsDaoFactory.createUserVoterDao(em).getElectionCensusSize(userVoterDB.getElection().getElectionId());
		if (cantidadCandidatos < 1) {
			Election eleccion = ElectionsDaoFactory.createElectionDao(em).getElection(userVoterDB.getElection().getElectionId());
			eleccion.setElectorsSet(false);
			em.persist(eleccion);
		}
	}

	@Override
	public List<Activity> getActivitiesAll() {
		return ElectionsDaoFactory.createActivityDao(em).getActivitiesAll();
	}

	@Override
	public List<Activity> getElectionActivities(long electionId) {
		return ElectionsDaoFactory.createActivityDao(em).getElectionActivities(electionId);
	}

	@Override
	public List<UserVoter> getElectionUserVoters(long electionId) {
		return ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionId);
	}

	@Override
	public void updateUserVoterToken(long userVoterId, String name, String electionTitle, String userAdminId, String ip) {
		UserVoter u = em.find(UserVoter.class, userVoterId);
		u.setVoteToken(StringUtils.createSecureToken());
		em.merge(u);
		String descripcion = userAdminId.toUpperCase() + " actualizó el link de votación para el usuario " + name + " en la elección " + electionTitle;
		persistActivity(userAdminId, ActivityType.UPDATE_TOKEN_USER_CENSUS, descripcion, ip, u.getElection().getElectionId());
	}

	@Override
	public void removeUserAdmin(String userAdminToDeleteId, String userAdminId, String ip) {
		UserAdmin a = ElectionsDaoFactory.createUserAdminDao(em).getUserAdmin(userAdminToDeleteId);
		em.remove(a);
		String descripcion = userAdminId.toUpperCase() + " eliminó a " + userAdminToDeleteId.toUpperCase() + " de listado de admin";
		persistActivity(userAdminId, ActivityType.REMOVE_ADMIN, descripcion, ip, null);
	}

	@Override
	public void updateElectionCensus(long electionId, byte[] content, String userAdminId, String ip) throws CensusValidationException, Exception {
		try {
			List<UserVoter> usuariosPadron = ExcelUtils.processCensusExcel(content);
			Election eleccion = em.find(Election.class, electionId);
			eleccion.setElectorsSet(true);
			ElectionsDaoFactory.createVoteDao(em).deleteElectionVotes(electionId);
			ElectionsDaoFactory.createUserVoterDao(em).deleteElectionCensus(electionId);

			for (UserVoter usuarioPadron : usuariosPadron) {
				usuarioPadron.setElection(eleccion);
				usuarioPadron.setVoteToken(StringUtils.createSecureToken());
				em.persist(usuarioPadron);
			}
			em.persist(eleccion);

			String descripcion = userAdminId.toUpperCase() + " agregó el padrón para la elección  " + "(" + eleccion.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.ADD_CENSUS, descripcion, ip, electionId);

		} catch (CensusValidationException e) {
			appLogger.error(e);
			throw e;
		} catch (Exception e1) {
			appLogger.error(e1);
			throw e1;
		}
	}

	@Override
	public List<Election> getElectionsLightThisYear() {
		return ElectionsDaoFactory.createElectionDao(em).getElectionsLightThisYear();
	}

	@Override
	public boolean addUserVoter(long electionId, UserVoter userVoter, String userAdminId, String ip) throws CensusValidationException {
		try {
			if(!ElectionsDaoFactory.createUserVoterDao(em).userVoterExistsByMailElection(electionId, userVoter.getMail())) {
				Election eleccion = em.find(Election.class, electionId);
				userVoter.setElection(eleccion);
				userVoter.setVoteToken(StringUtils.createSecureToken());
				em.persist(userVoter);
				eleccion.setElectorsSet(true);
				String descripcion = userAdminId.toUpperCase() + " agregó a " + userVoter.getName() + " como usuario padrón para la elección " + eleccion.getTitleSpanish();
				persistActivity(userAdminId, ActivityType.ADD_VOTE_USER, descripcion, ip, electionId);
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
	public void editUserVoter(UserVoter userVoter, String userAdminId, String ip) throws CensusValidationException {
		try {
			if(!ElectionsDaoFactory.createUserVoterDao(em).userVoterExistsByMailElection(userVoter.getElection().getElectionId(), userVoter.getMail())) {
				em.merge(userVoter);
				String descripcion = userAdminId.toUpperCase() + " actualizó los datos de un usuario padrón para la elección  " + userVoter.getElection().getTitleSpanish();
				persistActivity(userAdminId, ActivityType.EDIT_VOTE_USER, descripcion, ip, userVoter.getElection().getElectionId());
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
	public List<ElectionEmailTemplate> getElectionEmailTemplates(long electionId) {
		if (electionId == 0)
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplates();
		else
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplates(electionId);
	}

	@Override
	public void modifyElectionEmailTemplate(ElectionEmailTemplate electionEmailTemplate) {
		Election election = electionEmailTemplate.getElection();
		if (election != null)
			em.merge(election);
		em.merge(electionEmailTemplate);
	}

	@Override
	public void setVoteLinkStatus(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election eleccion = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			eleccion.setVotingLinkAvailable(status);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de votación para la elección ";
			if (Boolean.TRUE.equals(status)) {
				strValor = "habilitó el link de votación para la elección ";
			}
			String descripcion = userAdminId.toUpperCase() + " " + strValor + "(" + eleccion.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.ENABLE_VOTE_LINK, descripcion, ip, eleccion.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public List<ElectionEmailTemplate> getBaseEmailTemplates() {
		return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplates();
	}

	@Override
	public ElectionEmailTemplate getEmailTemplate(String templateType, long electionId) {
		if (electionId == 0)
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplate(templateType);
		else
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(templateType, electionId);
	}

	@Override
	public void editUserAdmin(UserAdmin userAdmin, String email, Long authorizedElectionId, String userAdminId, String ip) {
		try {
			userAdmin.setAuthorizedElectionId(authorizedElectionId);
			em.merge(userAdmin);
			String descripcion;
			if (userAdminId.equalsIgnoreCase(userAdmin.getUserAdminId())) {
				descripcion = userAdminId.toUpperCase() + " editó  su email de " + email + " a " + userAdmin.getEmail() + " y su elección autorizada a la de id " + authorizedElectionId;
			} else {
				descripcion = userAdminId.toUpperCase() + " editó el email del usuario " + userAdmin.getUserAdminId().toUpperCase() + " de " + email + " a " + userAdmin.getEmail() + " y la elección autorizada a la de id " + authorizedElectionId;
			}
			persistActivity(userAdminId, ActivityType.EDIT_ADMIN, descripcion, ip, authorizedElectionId);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public File exportCensus(long electionId) {
		String fileName = "/padron_electoral_" + electionId + ".xls";
		return ExcelUtils.exportToExcel(getElectionUserVoters(electionId), fileName);
	}

	@Override
	public void editAdminUserPassword(String userAdminToUpdateId, String password, String userAdminId, String ip) {
		try {
			UserAdmin a = ElectionsDaoFactory.createUserAdminDao(em).getUserAdmin(userAdminToUpdateId);
			a.setPassword(password);

			em.persist(a);
			String descripcion;
			if (userAdminId.equalsIgnoreCase(userAdminToUpdateId)) {
				descripcion = userAdminId.toUpperCase() + " cambió su contraseña";
			} else {
				descripcion = userAdminId.toUpperCase() + " cambió la contraseña de " + userAdminToUpdateId.toUpperCase();
			}
			persistActivity(userAdminId, ActivityType.EDIT_ADMIN, descripcion, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public boolean addUserAdmin(UserAdmin userAdmin, String userAdminId, String ip) {
		try {
			if (ElectionsDaoFactory.createUserAdminDao(em).getUserAdmin(userAdmin.getUserAdminId()) == null) {
				UserAdmin a = new UserAdmin();
				a.setUserAdminId(userAdmin.getUserAdminId().toLowerCase());
				a.setPassword(userAdmin.getPassword().toUpperCase());
				a.setEmail(userAdmin.getEmail().toLowerCase());
				a.setAuthorizedElectionId(userAdmin.getAuthorizedElectionId());
				em.persist(a);
				String descripcion = userAdminId.toUpperCase() + " agregó a " + userAdmin.getUserAdminId().toUpperCase() + " como admin";
				persistActivity(userAdminId, ActivityType.ADD_ADMIN, descripcion, ip, userAdmin.getAuthorizedElectionId());
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
	public void addCandidate(long electionId, Candidate candidate, String userAdminId, String ip) {
		Election eleccion = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		candidate.setElection(eleccion);
		int orden = ElectionsDaoFactory.createCandidateDao(em).getLastNonFixedCandidateOrder(electionId);
		candidate.setCandidateOrder(orden + 1);
		em.persist(candidate);
		eleccion.setCandidatesSet(true);
		em.persist(eleccion);
		String descripcion = userAdminId.toUpperCase() + " agregó un candidato para la elección  " + eleccion.getTitleSpanish();
		persistActivity(userAdminId, ActivityType.ADD_CANDIDATE, descripcion, ip, eleccion.getElectionId());
	}

	@Override
	public void removeCandidate(long candidateId, String userAdminId, String ip) {
		Candidate candidato = em.find(Candidate.class, candidateId);
		em.remove(candidato);
		String descripcion = userAdminId.toUpperCase() + " eliminó al candidato " + candidato.getName() + TEXT_ELECCION + candidato.getElection().getTitleSpanish();
		persistActivity(userAdminId, ActivityType.REMOVE_CANDIDATE, descripcion, ip, candidato.getElection().getElectionId());

		Election eleccion = ElectionsDaoFactory.createElectionDao(em).getElection(candidato.getElection().getElectionId());
		eleccion.setCandidatesSet(eleccion.getCandidates().size() > 1);
		em.persist(eleccion);
	}

	@Override
	public Candidate getCandidate(long candidateId) {
		return ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
	}

	@Override
	public void editCandidate(Candidate candidate, String userAdminId, String ip) {
		try {
			em.merge(candidate);
			String descripcion = userAdminId.toUpperCase() + " actualizó los datos de un candidato para la elección " + candidate.getElection().getTitleSpanish();
			persistActivity(userAdminId, ActivityType.EDIT_CANDIDATES, descripcion, ip, candidate.getElection().getElectionId());
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void addAuditor(long electionId, Auditor auditor, String electionTitle, String userAdminId, String ip) {
		Election eleccion = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		auditor.setElection(eleccion);
		em.persist(auditor);
		String descripcion = userAdminId.toUpperCase() + " agregó un auditor para la elección " + electionTitle;
		persistActivity(userAdminId, ActivityType.ADD_AUDITOR, descripcion, ip, electionId);
	}

	@Override
	public void removeAuditor(long auditorId, String userAdminId, String ip) {
		Auditor auditor = em.find(Auditor.class, auditorId);
		em.remove(auditor);
		String descripcion = userAdminId.toUpperCase() + " eliminó al auditor " + auditor.getName() + TEXT_ELECCION + auditor.getElection().getTitleSpanish();
		persistActivity(userAdminId, ActivityType.REMOVE_AUDITOR, descripcion, ip, auditor.getElection().getElectionId());
	}

	@Override
	public Auditor getAuditor(long auditorId) {
		return ElectionsDaoFactory.createAuditorDao(em).getAuditor(auditorId);
	}

	@Override
	public void editAuditor(Auditor auditor, String userAdminId, String ip) {
		try {
			em.merge(auditor);
			String descripcion = userAdminId.toUpperCase() + " actualizó los datos de un auditor para la elección " + auditor.getElection().getTitleSpanish();
			persistActivity(userAdminId, ActivityType.EDIT_AUDITOR, descripcion, ip, auditor.getElection().getElectionId());
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void persistElectionAuditorsSet(long electionId, String electionTitle, String userAdminId, String ip) {
		try {
			Election eleccion = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			eleccion.setAuditorsSet(true);
			em.persist(eleccion);
			String descripcion = userAdminId.toUpperCase() + " agregó auditores para la elección " + electionTitle;
			persistActivity(userAdminId, ActivityType.ADD_AUDITORS, descripcion, ip, electionId);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List<Parameter> getParametersAll() {
		return EJBFactory.getInstance().getElectionsParametersEJB().getParametersAll();
	}

	@Override
	public List<IpAccess> getAllDisabledIPs() {
		return ElectionsDaoFactory.createIpAccessDao(em).getAllDisabledIPs();
	}

	@Override
	public boolean addParameter(String key, String valor, String userAdminId, String ip) {
		if (EJBFactory.getInstance().getElectionsParametersEJB().addParameter(key, valor)) {
			String descripcion = userAdminId.toUpperCase() + " creó el parámetro " + key;
			persistActivity(userAdminId, ActivityType.ADD_PARAMETER, descripcion, ip, null);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void editParameter(Parameter parameter, String userAdminId, String ip) {
		try {
			EJBFactory.getInstance().getElectionsParametersEJB().editParameter(parameter);
			String descripcion = userAdminId.toUpperCase() + " actualizó el parámetro " + parameter.getKey();
			persistActivity(userAdminId, ActivityType.EDIT_PARAMETER, descripcion, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void removeParameter(String key, String userAdminId, String ip) {
		try {
			String descripcion;
			EJBFactory.getInstance().getElectionsParametersEJB().deleteParameter(key);
			descripcion = userAdminId.toUpperCase() + " eliminó el parámetro " + key + " del sistema";
			persistActivity(userAdminId, ActivityType.DELET_PARAMETER, descripcion, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List getRecipientsByRecipientType(ElectionEmailTemplate electionEmailTemplate) throws Exception {
		RecipientType recipientType = electionEmailTemplate.getRecipientType();
		if (recipientType.equals(RecipientType.VOTERS)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.VOTERS_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionCensusByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_MX)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionCensusByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersNotVotedYet(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_MX)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
		} else if (recipientType.equals(RecipientType.VOTERS_ALREADY_VOTED)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersVoted(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.VOTERS_ALREADY_VOTED_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersVotedByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_ALREADY_VOTED_MX)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersVotedByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_TWO_ELECTIONS)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getJointElectionUserVotersNotVotedYet(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_TWO_ELECTIONS_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getJointElectionUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_TWO_ELECTIONS_MX)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getJointElectionUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
		} else if (recipientType.equals(RecipientType.AUDITORS)) {
			return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditors(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.AUDITORS_NOT_AGREED_CONFORMITY_YET)) {
			return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditorsNotAgreedConformity(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.AUDITORS_ALREADY_AGREED_CONFORMITY)) {
			return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditorsAgreedConformity(electionEmailTemplate.getElection().getElectionId());
		} else {
			throw new Exception("Should have never got here.");
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Integer createMissingEmailTemplates() {
		int cuenta = 0;
		List<ElectionEmailTemplate> bases = getBaseEmailTemplates();
		List<Election> eleccionesSinTemplate = getElectionsAllOrderCreationDate();
		for (int j = 0; j < eleccionesSinTemplate.size(); j++) {
			Election eleccionJ = eleccionesSinTemplate.get(j);
			for (int i = 0; i < bases.size(); i++) {
				ElectionEmailTemplate templateI = bases.get(i);
				if (getEmailTemplate(templateI.getTemplateType(), eleccionJ.getElectionId()) == null) {
					em.persist(new ElectionEmailTemplate(eleccionJ, templateI));
					cuenta++;
				}
			}
		}
		return cuenta;
	}

	@Override
	public void queueMassiveSending(List users, ElectionEmailTemplate electionEmailTemplate) {
		EJBFactory.getInstance().getMailsSendingEJB().queueMassiveSending(users, electionEmailTemplate);
	}

	@Override
	public void fixCandidateToTop(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		Candidate currentFirstCandidate = ElectionsDaoFactory.createCandidateDao(em).getElectionFirstCandidate(candidate.getElection().getElectionId());
		if (currentFirstCandidate != null) {
			currentFirstCandidate.setCandidateOrder(candidate.getCandidateOrder());
		}
		candidate.setCandidateOrder(Constants.MAX_ORDER);
		em.persist(candidate);
		em.persist(currentFirstCandidate);
	}

	@Override
	public void fixCandidateToFirstNonFixed(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		int lastNonFixedOrder = ElectionsDaoFactory.createCandidateDao(em).getLastNonFixedCandidateOrder(candidate.getElection().getElectionId());
		candidate.setCandidateOrder(lastNonFixedOrder + 1);
		em.persist(candidate);
	}

	@Override
	public void fixCandidateToBottom(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		Candidate currentLastCandidate = ElectionsDaoFactory.createCandidateDao(em).getElectionLastCandidate(candidate.getElection().getElectionId());
		if (currentLastCandidate != null) {
			currentLastCandidate.setCandidateOrder(candidate.getCandidateOrder());
		}
		candidate.setCandidateOrder(Constants.MIN_ORDER);
		em.persist(candidate);
		em.persist(currentLastCandidate);
	}

	@Override
	public void subirCandidato(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		Candidate nextAboveCandidate = ElectionsDaoFactory.createCandidateDao(em).getNextAboveCandidate(candidate.getElection().getElectionId(), candidate.getCandidateOrder());
		if (nextAboveCandidate != null && nextAboveCandidate.getCandidateOrder() != Constants.MAX_ORDER) {
			appLogger.info(candidate.getCandidateOrder() + " - " + nextAboveCandidate.getCandidateOrder());

			int aux = candidate.getCandidateOrder();
			candidate.setCandidateOrder(nextAboveCandidate.getCandidateOrder());
			nextAboveCandidate.setCandidateOrder(aux);
			em.persist(candidate);
			em.persist(nextAboveCandidate);
		}
	}

	@Override
	public void bajarCandidato(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		Candidate nextBelowCandidate = ElectionsDaoFactory.createCandidateDao(em).getNextBelowCandidate(candidate.getElection().getElectionId(), candidate.getCandidateOrder());
		if (nextBelowCandidate != null && nextBelowCandidate.getCandidateOrder() != Constants.MIN_ORDER) {
			int aux = candidate.getCandidateOrder();
			appLogger.info(candidate.getCandidateOrder() + " - " + nextBelowCandidate.getCandidateOrder());

			candidate.setCandidateOrder(nextBelowCandidate.getCandidateOrder());
			nextBelowCandidate.setCandidateOrder(aux);
			em.persist(candidate);
			em.persist(nextBelowCandidate);
		}
	}

	@Override
	public void setSortCandidatesRandomly(Long electionId, Boolean valor) {
		try {
			Election eleccion = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			eleccion.setRandomOrderCandidates(valor);
			em.persist(eleccion);
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public List<Email> getPendingSendEmails() {
		return ElectionsDaoFactory.createEmailDao(em).getPendingSendEmails();
	}

	@Override
	public List<Email> getEmailsAll() {
		return ElectionsDaoFactory.createEmailDao(em).getEmailsAll();
	}

	@Override
	public List<Email> getElectionEmails(Long electionId) {
		return ElectionsDaoFactory.createEmailDao(em).getElectionEmails(electionId);
	}

	@Override
	public List<Email> getElectionPendingSendEmails(Long electionId) {
		return ElectionsDaoFactory.createEmailDao(em).getElectionPendingSendEmails(electionId);
	}

	@Override
	public Commissioner getCommissioner(long commissionerId) {
		return ElectionsDaoFactory.createCommissionerDao(em).getCommissioner(commissionerId);
	}

	@Override
	public boolean addCommissioner(String name, String mail, String userAdminId, String ip) {
		try {
			if (ElectionsDaoFactory.createCommissionerDao(em).getCommissionerByMail(mail) == null) {
				Commissioner a = new Commissioner();
				a.setName(name);
				a.setMail(mail);
				em.persist(a);
				String descripcion = userAdminId.toUpperCase() + " agregó al comisionado " + name;
				persistActivity(userAdminId, ActivityType.ADD_COMMISSIONER, descripcion, ip, null);
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
	public void removeCommissioner(long commissionerId, String name, String userAdminId, String ip) {
		Commissioner a = ElectionsDaoFactory.createCommissionerDao(em).getCommissioner(commissionerId);
		em.remove(a);
		String descripcion = userAdminId.toUpperCase() + " eliminó al comisionado " + name + " del sistema";
		persistActivity(userAdminId, ActivityType.REMOVE_COMMISSIONER, descripcion, ip, null);
	}

	@Override
	public void editCommissioner(Commissioner commissioner, String userAdminId, String ip) {
		try {
			em.merge(commissioner);
			persistActivity(userAdminId, ActivityType.EDIT_COMMISSIONER, userAdminId + " ha editado el comidionado " + commissioner.getName(), ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public File exportCensusExample(String filePath) {
		return FilesUtils.getCensusExample(filePath);
	}

	@Override
	public void createElectionEmailTemplates(Election election) {
		List<ElectionEmailTemplate> bases = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplates();
		for (int i = 0; i < bases.size(); i++) {
			ElectionEmailTemplate templateI = bases.get(i);
			if (getEmailTemplate(templateI.getTemplateType(), election.getElectionId()) == null) {
				em.persist(new ElectionEmailTemplate(election, templateI));
			}
		}
	}

	@Override
	public void resendUserVoterElectionMail(UserVoter userVoter, Election election, String userAdminId, String ip) {
		ElectionEmailTemplate tComienzo = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(Constants.TemplateTypeELECTION_NOTICE, election.getElectionId());
		ElectionEmailTemplate tEmpezo = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(Constants.TemplateTypeELECTION_START, election.getElectionId());
		if (tComienzo != null || tEmpezo != null) {
			Date now = new Date();
			if (((now.after(election.getCreationDate())) && (now.before(election.getStartDate()))))
				EJBFactory.getInstance().getMailsSendingEJB().queueSingleSending(tComienzo, userVoter, null, election, new ArrayList<>());
			if ((election.isVotingLinkAvailable()) && ((now.after(election.getStartDate())) && (now.before(election.getEndDate()))))
				EJBFactory.getInstance().getMailsSendingEJB().queueSingleSending(tEmpezo, userVoter, null, election, new ArrayList<>());
			String descripcion = userAdminId.toUpperCase() + " reénvio el email al usuario padrón " + userVoter.getName().toUpperCase() + " de la elección " + "(" + election.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.RESEND_EMAIL_ELECTION_USER_CENSUS, descripcion, ip, election.getElectionId());
		}
	}

	@Override
	public long getUserAuthorizedElectionId(String userAdminId) {
		return ElectionsDaoFactory.createUserAdminDao(em).getUserAuthorizedElectionId(userAdminId);
	}

	@Override
	public Parameter getParameter(String key) {
		return ElectionsDaoFactory.createParameterDao(em).getParameter(key);
	}

	@Override
	public void requestElectionRevision(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election eleccion = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			setAuditLinkStatus(electionId, status, userAdminId, ip);

			if (!(Boolean.TRUE.equals(status))) {
				List<Auditor> auditores = eleccion.getAuditors();
				for (Auditor auditor : auditores) {
					auditor.setRevisionAvailable(false);
					em.persist(auditor);
					String descripcion = userAdminId.toUpperCase() + " cerró el proceso de revisión y se revocó la autorizacion de la revisión del auditor: " + auditor.getAuditorId() + " - " + auditor.getName() + TEXT_ELECCION + "(" + eleccion.getTitleSpanish() + ")";
					persistActivity(userAdminId, ActivityType.ELECTION_REVISION_NO, descripcion, ip, eleccion.getElectionId());
				}
			}
			eleccion.setRevisionRequest(status);
			em.persist(eleccion);
			String strValor =  " revocó la solicitud de ";
			if (Boolean.TRUE.equals(status)) {
				strValor = " solicitó la ";
			}
			String descripcion = userAdminId.toUpperCase() + strValor + "revisión para la elección " + "(" + eleccion.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.ELECTION_REVISION, descripcion, ip, eleccion.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public List<Vote> getElectionVotes(Long electionId) {
		return ElectionsDaoFactory.createVoteDao(em).getElectionVotes(electionId);
	}

	@Override
	public boolean isRevisionActive(long electionId, String userAdminId, String ip) {
		Election eleccion = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		List<Auditor> auditores = eleccion.getAuditors();
		if (!eleccion.isRevisionRequest())
			return false;

		for (Auditor auditor : auditores) {
			if (auditor.isCommissioner() && !auditor.isRevisionAvailable())
				return false;
		}

		String descripcion = userAdminId.toUpperCase() + " ingresó a la revisión de votos para la elección " + "(" + eleccion.getTitleSpanish() + ")";
		persistActivity(userAdminId, ActivityType.ENTER_TO_REVISION, descripcion, ip, eleccion.getElectionId());
		return true;
	}

	@Override
	public Candidate getNextAboveCandidate(Candidate candidate) {
		return ElectionsDaoFactory.createCandidateDao(em).getNextAboveCandidate(candidate.getElection().getElectionId(), candidate.getCandidateOrder());
	}

	@Override
	public Candidate getNextBelowCandidate(Candidate candidate) {
		return ElectionsDaoFactory.createCandidateDao(em).getNextBelowCandidate(candidate.getElection().getElectionId(), candidate.getCandidateOrder());
	}

	@Override
	public boolean commissionerExists(String nombre, String mail) {
		return ElectionsDaoFactory.createCommissionerDao(em).commissionerExists(nombre, mail);
	}

	@Override
	public boolean auditorExists(long electionId, String name, String mail) {
		return ElectionsDaoFactory.createAuditorDao(em).auditorExists(electionId, name, mail);
	}

	@Override
	public String getDefaultSender() {
		return EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.DEFAULT_SENDER);
	}

	@Override
	public String getDefaultWebsite() {
		return EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.WEBSITE_DEFAULT);
	}

	@Override
	public boolean createBaseEmailTemplate(ElectionEmailTemplate electionEmailTemplate, String userAdminId, String ip) {
		try {
			electionEmailTemplate.setTemplateType(electionEmailTemplate.getTemplateType().toUpperCase());
			em.persist(electionEmailTemplate);
			String descripcion = userAdminId.toUpperCase() + " creó un nuevo template base " + electionEmailTemplate.getTemplateType();
			persistActivity(userAdminId, ActivityType.ADD_BASE_TEMPLATE, descripcion, ip, null);
			return true;
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	public List<JointElection> getJointElectionsAll() {
		return ElectionsDaoFactory.createElectionDao(em).getJointElectionsAll();
	}

	@Override
	public JointElection getJointElectionForElection(long electionId) {
		return ElectionsDaoFactory.createElectionDao(em).getJointElectionForElection(electionId);
	}

	@Override
	public void updateJointElection(JointElection jointElection) {
		em.merge(jointElection);
	}

	@Override
	public void removeJointElection(JointElection jointElection) {
		em.remove(em.contains(jointElection) ? jointElection : em.merge(jointElection));
	}

	@Override
	public boolean isJointElection(long electionId) {
		return !ElectionsDaoFactory.createElectionDao(em).electionIsSimple(electionId);
	}

	@Override
	public boolean electionsCensusEqual(JointElection jointElection) {
		return ElectionsDaoFactory.createUserVoterDao(em).electionsCensusEqual(jointElection.getIdElectionA(), jointElection.getIdElectionB());
	}

	@Override
	public List<String> getElectionsAllIdAndTitle() {
		return ElectionsDaoFactory.createElectionDao(em).getElectionsAllIdAndTitle();
	}

	@Override
	public Customization getCustomization() {
		return ElectionsDaoFactory.createCustomizationDao(em).getCustomization();
	}

	@Override
	public boolean updateCustomization(Customization customization) {
		try {
			em.merge(customization);
			return true;
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	public void persistActivity(String userAdminId, ActivityType activityType, String description, String ip, Long electionId) {
		Activity a = new Activity();
		a.setIp(ip);
		a.setElectionId(electionId);
		a.setTimestamp(new Date());
		a.setUserName(userAdminId);
		a.setActivityType(activityType);
		a.setDescription(description);
		em.persist(a);
	}

}
