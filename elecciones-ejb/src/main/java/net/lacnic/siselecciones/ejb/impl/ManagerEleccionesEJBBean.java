package net.lacnic.siselecciones.ejb.impl;

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

import net.lacnic.siselecciones.dao.DaoFactoryElecciones;
import net.lacnic.siselecciones.dominio.AccesosIps;
import net.lacnic.siselecciones.dominio.Actividad;
import net.lacnic.siselecciones.dominio.Auditor;
import net.lacnic.siselecciones.dominio.Candidato;
import net.lacnic.siselecciones.dominio.Comisionado;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.dominio.Email;
import net.lacnic.siselecciones.dominio.Parametro;
import net.lacnic.siselecciones.dominio.Personalizacion;
import net.lacnic.siselecciones.dominio.SupraEleccion;
import net.lacnic.siselecciones.dominio.TemplateEleccion;
import net.lacnic.siselecciones.dominio.TipoActividad;
import net.lacnic.siselecciones.dominio.TipoDestinatario;
import net.lacnic.siselecciones.dominio.UsuarioAdmin;
import net.lacnic.siselecciones.dominio.UsuarioPadron;
import net.lacnic.siselecciones.dominio.Voto;
import net.lacnic.siselecciones.ejb.ManagerEleccionesEJB;
import net.lacnic.siselecciones.utils.Constantes;
import net.lacnic.siselecciones.utils.EJBFactory;
import net.lacnic.siselecciones.utils.StringUtils;
import net.lacnic.siselecciones.utils.UtilsExcel;
import net.lacnic.siselecciones.utils.UtilsFiles;
import net.lacnic.siselecciones.utils.UtilsLinks;
import net.lacnic.siselecciones.utils.ValidacionPadronException;

@Stateless
@Remote(ManagerEleccionesEJB.class)
public class ManagerEleccionesEJBBean implements ManagerEleccionesEJB {

	private static final String TEXT_ELECCION = " para la elección ";

	private Random rand = new SecureRandom();

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	@PersistenceContext(unitName = "elecciones-pu")
	private EntityManager em;

	@Override
	public UsuarioAdmin loginAdmin(String adminId, String password, String ip) {
		UsuarioAdmin a = DaoFactoryElecciones.createUsuarioAdminDao(em).comprobarUsuarioAdmin(adminId, password);
		try {
			if(a != null) {
				String descripcion = adminId.toUpperCase() + " se ha logueado exitosamente";
				EJBFactory.getInstance().getManagerEleccionesEJB().persistirActividad(adminId, TipoActividad.LOGIN_SUCCESSFUL, descripcion, ip, null);
			} else {
				String descripcion = "Intento fallido de login de usuario " + adminId.toUpperCase();
				EJBFactory.getInstance().getManagerEleccionesEJB().persistirActividad(adminId, TipoActividad.LOGIN_FAILED, descripcion, ip, null);
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

			String skGoogleApiReCaptcha = EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constantes.SkGoogleApiReCaptcha);
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
	public List<Eleccion> obtenerEleccionesLight() {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerElecciones();
	}

	@Override
	public List<Eleccion> obtenerEleccionesLightEsteAnio() {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerEleccionesLightEsteAnio();
	}

	@Override
	public void eliminarCandidato(long idCandidato, String userId, String ip) {
		Candidato candidato = em.find(Candidato.class, idCandidato);
		em.remove(candidato);
		String descripcion = userId.toUpperCase() + " eliminó al candidato " + candidato.getNombre() + TEXT_ELECCION + candidato.getEleccion().getTituloEspanol();
		persistirActividad(userId, TipoActividad.ELIMINAR_CANDIDATO, descripcion, ip, candidato.getEleccion().getIdEleccion());

		Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(candidato.getEleccion().getIdEleccion());
		eleccion.setCandidatosSeteado(eleccion.getCandidatos().size() > 1);
		em.persist(eleccion);
	}

	@Override
	public void eliminarAuditor(long idAuditor, String userId, String ip) {
		Auditor auditor = em.find(Auditor.class, idAuditor);
		em.remove(auditor);
		String descripcion = userId.toUpperCase() + " eliminó al auditor " + auditor.getNombre() + TEXT_ELECCION + auditor.getEleccion().getTituloEspanol();
		persistirActividad(userId, TipoActividad.ELIMINAR_AUDITOR, descripcion, ip, auditor.getEleccion().getIdEleccion());
	}

	@Override
	public void agregarCandidato(long idEleccion, Candidato candidato, String userId, String ip) {
		Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
		candidato.setEleccion(eleccion);
		int orden = DaoFactoryElecciones.createCandidatoDao(em).obtenerOrdenDelUltimoCandidatoNofijado(idEleccion);
		candidato.setOrden(orden + 1);
		em.persist(candidato);
		eleccion.setCandidatosSeteado(true);
		em.persist(eleccion);
		String descripcion = userId.toUpperCase() + " agregó un candidato para la elección  " + eleccion.getTituloEspanol();
		persistirActividad(userId, TipoActividad.AGREGAR_CANDIDATO, descripcion, ip, eleccion.getIdEleccion());
	}

	@Override
	public void editarCandidato(Candidato candidato, String userId, String ip) {
		try {
			em.merge(candidato);
			String descripcion = userId.toUpperCase() + " actualizó los datos de un candidato para la elección " + candidato.getEleccion().getTituloEspanol();
			persistirActividad(userId, TipoActividad.EDITAR_CANDIDATOS, descripcion, ip, candidato.getEleccion().getIdEleccion());
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void agregarAuditor(long idEleccion, Auditor auditor, String tituloEspaniol, String userId, String ip) {
		Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
		auditor.setEleccion(eleccion);
		em.persist(auditor);
		String descripcion = userId.toUpperCase() + " agregó un auditor para la elección " + tituloEspaniol;
		persistirActividad(userId, TipoActividad.AGREGAR_AUDITOR, descripcion, ip, idEleccion);
	}

	@Override
	public void persistirAuditoresSeteados(long idEleccion, String tituloEspaniol, String userId, String ip) {
		try {
			Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
			eleccion.setAuditoresSeteado(true);
			em.persist(eleccion);
			String descripcion = userId.toUpperCase() + " agregó auditores para la elección " + tituloEspaniol;
			persistirActividad(userId, TipoActividad.AGREGAR_AUDITORES, descripcion, ip, idEleccion);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public Eleccion obtenerEleccion(long idEleccion) {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
	}

	@Override
	public String obtenerLinkresultado(Eleccion eleccion) throws Exception {
		return UtilsLinks.calcularLinkResultado(eleccion.getTokenResultado());
	}

	@Override
	public List<Candidato> obtenerCandidatosEleccionOrdenados(long idEleccion) {
		List<Candidato> candidatos = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatosEleccion(idEleccion);
		Eleccion e = obtenerEleccion(idEleccion);
		Collections.sort(candidatos, new Comparator<Candidato>() {
			@Override
			public int compare(Candidato c1, Candidato c2) {
				int r;

				if (c1.getOrden() == Constantes.ORDEN_MINIMO)
					r = Constantes.ORDEN_MINIMO;
				else if (c2.getOrden() == Constantes.ORDEN_MAXIMO)
					r = Constantes.ORDEN_MAXIMO;
				else if (e.isCandidatosAleatorios()) {
					r = Integer.valueOf(rand.nextInt(30)).compareTo(Integer.valueOf(rand.nextInt(30)));
				} else
					r = (Integer.valueOf(c2.getOrden()).compareTo(Integer.valueOf(c1.getOrden())));
				return r;
			}
		});
		return candidatos;
	}

	@Override
	public Candidato obtenerCandidato(long idCandidato) {
		return DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
	}

	@Override
	public void actualizarUsuariosPadron(long idEleccion, byte[] contenido, String admin, String ip) throws ValidacionPadronException, Exception {
		try {
			List<UsuarioPadron> usuariosPadron = UtilsExcel.procesarExcelPadronNuevo(contenido);
			Eleccion eleccion = em.find(Eleccion.class, idEleccion);
			eleccion.setPadronSeteado(true);
			DaoFactoryElecciones.createVotoDao(em).borrarVotos(idEleccion);
			DaoFactoryElecciones.createUsuarioPadronDao(em).borrarUsuariosPadron(idEleccion);

			for (UsuarioPadron usuarioPadron : usuariosPadron) {
				usuarioPadron.setEleccion(eleccion);
				usuarioPadron.setTokenVotacion(StringUtils.createSecureToken());
				em.persist(usuarioPadron);
			}
			em.persist(eleccion);

			String descripcion = admin.toUpperCase() + " agregó el padrón para la elección  " + "(" + eleccion.getTituloEspanol() + ")";
			persistirActividad(admin, TipoActividad.AGREGAR_PADRON, descripcion, ip, idEleccion);

		} catch (ValidacionPadronException e) {
			appLogger.error(e);
			throw e;
		} catch (Exception e1) {
			appLogger.error(e1);
			throw e1;
		}
	}

	@Override
	public boolean agregarUsuarioPadron(long idEleccion, UsuarioPadron usuarioPadron, String userId, String ip) throws ValidacionPadronException {
		try {
			if(!DaoFactoryElecciones.createUsuarioPadronDao(em).existeUsuarioPadronEleccionEmail(idEleccion, usuarioPadron.getMail())) {
				Eleccion eleccion = em.find(Eleccion.class, idEleccion);
				usuarioPadron.setEleccion(eleccion);
				usuarioPadron.setTokenVotacion(StringUtils.createSecureToken());
				em.persist(usuarioPadron);
				eleccion.setPadronSeteado(true);
				String descripcion = userId.toUpperCase() + " agregó a " + usuarioPadron.getNombre() + " como usuario padrón para la elección " + eleccion.getTituloEspanol();
				persistirActividad(userId, TipoActividad.AGREGAR_USUARIO_PADRON, descripcion, ip, idEleccion);
				return true;
			} else {
				throw new ValidacionPadronException("duplicateEmailException");
			}
		} catch (ValidacionPadronException vpe) {
			appLogger.error(vpe);
			throw vpe;
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	public void editarUsuarioPadron(UsuarioPadron usuarioPadron, String userId, String ip) throws ValidacionPadronException {
		try {
			if(!DaoFactoryElecciones.createUsuarioPadronDao(em).existeUsuarioPadronEleccionEmail(usuarioPadron.getEleccion().getIdEleccion(), usuarioPadron.getMail())) {
				em.merge(usuarioPadron);
				String descripcion = userId.toUpperCase() + " actualizó los datos de un usuario padrón para la elección  " + usuarioPadron.getEleccion().getTituloEspanol();
				persistirActividad(userId, TipoActividad.EDITAR_USUARIO_PADRON, descripcion, ip, usuarioPadron.getEleccion().getIdEleccion());
			} else {
				throw new ValidacionPadronException("duplicateEmailException");
			}

		} catch (ValidacionPadronException vpe) {
			appLogger.error(vpe);
			throw vpe;
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void actualizarTokenUsuarioPadron(long idUsp, String nombre, String titulo, String userId, String ip) {
		UsuarioPadron u = em.find(UsuarioPadron.class, idUsp);
		u.setTokenVotacion(StringUtils.createSecureToken());
		em.merge(u);
		String descripcion = userId.toUpperCase() + " actualizó el link de votación para el usuario " + nombre + " en la elección " + titulo;
		persistirActividad(userId, TipoActividad.ACTUALIZAR_TOKEN_USUPADRON, descripcion, ip, u.getEleccion().getIdEleccion());
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
			List<UsuarioAdmin> usuAdmin = DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerUsuariosAdmin(idEleccion);
			List<Auditor> auditores = DaoFactoryElecciones.createAuditorDao(em).obtenerAuditoresEleccion(idEleccion);
			List<Candidato> candidatos = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatosEleccion(idEleccion);
			List<Email> emails = DaoFactoryElecciones.createEmailDao(em).obtenerMailsDeEleccion(idEleccion);
			List<TemplateEleccion> templates = DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplatesEleccion(idEleccion);
			List<UsuarioPadron> usuariosP = DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccion(idEleccion);
			List<Voto> votos = DaoFactoryElecciones.createVotoDao(em).obtenerVotosEleccion(idEleccion);
			Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
			for (UsuarioAdmin ad : usuAdmin)
				em.remove(ad);
			for (Auditor a : auditores)
				em.remove(a);
			for (Candidato c : candidatos)
				em.remove(c);
			for (Email e : emails)
				em.remove(e);
			for (TemplateEleccion t : templates)
				em.remove(t);
			for (UsuarioPadron u : usuariosP)
				em.remove(u);
			for (Voto v : votos)
				em.remove(v);
			em.remove(eleccion);
			String descripcion = userId.toUpperCase() + " eliminó la elección " + titulo;
			persistirActividad(userId, TipoActividad.ELIMINAR_ELECCION, descripcion, ip, idEleccion);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List<Comisionado> obtenerComisionados() {
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
			String descripcion = userId.toUpperCase() + " actualizó los datos de un auditor para la elección " + auditor.getEleccion().getTituloEspanol();
			persistirActividad(userId, TipoActividad.EDITAR_AUDITOR, descripcion, ip, auditor.getEleccion().getIdEleccion());
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public UsuarioPadron obtenerUsuarioPadron(long idUsuarioPadron) {
		return em.find(UsuarioPadron.class, idUsuarioPadron);

	}

	@Override
	public void eliminarUsuarioPadron(UsuarioPadron actual, String titulo, String userId, String ip) {
		UsuarioPadron usuario = em.find(UsuarioPadron.class, actual.getIdUsuarioPadron());
		Long idEleccion = usuario.getEleccion().getIdEleccion();
		em.remove(usuario);
		String descripcion = userId.toUpperCase() + " eliminó a " + usuario.getNombre() + " del listado de usuario padrón en la elección " + titulo;
		persistirActividad(userId, TipoActividad.ELIMINAR_USUPADRON, descripcion, ip, idEleccion);
		long cantidadCandidatos = DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerCantidadUsuariosPadron(usuario.getEleccion().getIdEleccion());
		if (cantidadCandidatos < 1) {
			Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(usuario.getEleccion().getIdEleccion());
			eleccion.setPadronSeteado(false);
			em.persist(eleccion);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Eleccion actualizarEleccion(Eleccion eleccion, String userId, String ip) throws Exception {
		try {
			String descripcion;
			if (eleccion.getIdEleccion() == 0) {
				em.persist(eleccion);
				descripcion = userId.toUpperCase() + " creó la elección" + " (" + eleccion.getTituloEspanol() + ")" + " correctamente";
				persistirActividad(userId, TipoActividad.CREAR_ELECCION, descripcion, ip, eleccion.getIdEleccion());
				crearPlantillasEleccion(eleccion); // nuevo
				List<Comisionado> comisionados = DaoFactoryElecciones.createComisionadoDao(em).obtenerComisionados();
				for (Comisionado comisionado : comisionados) {
					em.persist(new Auditor(eleccion, comisionado));
				}
			} else
				em.merge(eleccion);
			descripcion = userId.toUpperCase() + " actualizó la elección " + " (" + eleccion.getTituloEspanol() + ")" + " correctamente";
			persistirActividad(userId, TipoActividad.EDITAR_ELECCION, descripcion, ip, eleccion.getIdEleccion());
		} catch (Exception e) {
			appLogger.error(e);
		}

		return eleccion;
	}

	@Override
	public void crearPlantillasEleccion(Eleccion eleccion) {
		List<TemplateEleccion> bases = DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplatesBase();
		for (int i = 0; i < bases.size(); i++) {
			TemplateEleccion templateI = bases.get(i);
			if (obtenerTemplate(templateI.getTipoTemplate(), eleccion.getIdEleccion()) == null) {
				em.persist(new TemplateEleccion(eleccion, templateI));
			}
		}

	}

	@Override
	public List<UsuarioAdmin> obtenerUsuariosAdmin() {
		return DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerUsuariosAdmin();
	}

	@Override
	public void habilitarLinkVotacion(Long id, Boolean valor, String admin, String ip) {
		try {
			Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(id);
			eleccion.setHabilitadoLinkVotacion(valor);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de votación para la elección ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = "habilitó el link de votación para la elección ";
			}
			String descripcion = admin.toUpperCase() + " " + strValor + "(" + eleccion.getTituloEspanol() + ")";
			persistirActividad(admin, TipoActividad.HABILITACION_LINK_VOTACION, descripcion, ip, eleccion.getIdEleccion());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void ordenarCandidatosAleatoriamente(Long id, Boolean valor) {
		try {
			Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(id);
			eleccion.setCandidatosAleatorios(valor);
			em.persist(eleccion);
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void habilitarLinkResultado(Long id, Boolean valor, String admin, String ip) {
		try {
			Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(id);
			eleccion.setHabilitadoLinkResultado(valor);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de resultado para la elección ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = "habilitó el link de resultado para la elección ";
			}
			String descripcion = admin.toUpperCase() + " " + strValor + "(" + eleccion.getTituloEspanol() + ")";
			persistirActividad(admin, TipoActividad.HABILITACION_LINK_RESULTADOS, descripcion, ip, eleccion.getIdEleccion());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void habilitarLinkAuditoria(Long id, Boolean valor, String admin, String ip) {
		try {
			Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(id);
			eleccion.setHabilitadoLinkAuditor(valor);
			em.persist(eleccion);
			String strValor =  "deshabilitó el link de auditoria para la elección ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = "habilitó el link de auditoria para la elección ";
			};
			String descripcion = admin.toUpperCase() + " " + strValor + "(" + eleccion.getTituloEspanol() + ")";
			persistirActividad(admin, TipoActividad.HABILITACION_LINK_AUDITORIA, descripcion, ip, eleccion.getIdEleccion());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public void solicitarRevision(Long idEleccion, Boolean valor, String admin, String ip) {
		try {
			Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);

			habilitarLinkAuditoria(idEleccion, valor, admin, ip);

			if (!(Boolean.TRUE.equals(valor))) {
				List<Auditor> auditores = eleccion.getAuditores();
				for (Auditor auditor : auditores) {
					auditor.setHabilitaRevision(false);
					em.persist(auditor);
					String descripcion = admin.toUpperCase() + " cerró el proceso de revisión y se revocó la autorizacion de la revisión del auditor: " + auditor.getIdAuditor() + " - " + auditor.getNombre() + TEXT_ELECCION + "(" + eleccion.getTituloEspanol() + ")";
					persistirActividad(admin, TipoActividad.REVISION_DE_ELECCION_NO, descripcion, ip, eleccion.getIdEleccion());
				}
			}
			eleccion.setSolicitarRevision(valor);
			em.persist(eleccion);
			String strValor =  " revocó la solicitud de ";
			if (Boolean.TRUE.equals(valor)) {
				strValor = " solicitó la ";
			};
			String descripcion = admin.toUpperCase() + strValor + "revisión para la elección " + "(" + eleccion.getTituloEspanol() + ")";
			persistirActividad(admin, TipoActividad.REVISION_DE_ELECCION, descripcion, ip, eleccion.getIdEleccion());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	@Override
	public boolean isRevisionActiva(long idEleccion, String admin, String ip) {
		Eleccion eleccion = DaoFactoryElecciones.createEleccionDao(em).obtenerEleccion(idEleccion);
		List<Auditor> auditores = eleccion.getAuditores();
		if (!eleccion.isSolicitarRevision())
			return false;

		for (Auditor auditor : auditores) {
			if (auditor.isComisionado() && !auditor.isHabilitaRevision())
				return false;
		}

		String descripcion = admin.toUpperCase() + " ingresó a la revisión de votos para la elección " + "(" + eleccion.getTituloEspanol() + ")";
		persistirActividad(admin, TipoActividad.INGRESO_A_REVISION, descripcion, ip, eleccion.getIdEleccion());
		return true;
	}

	@Override
	public List<UsuarioPadron> obtenerUsuariosPadronEleccion(long idEleccion) {
		return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccion(idEleccion);
	}

	@Override
	public List<Actividad> obtenerTodasLasActividades() {
		return DaoFactoryElecciones.createActividadDao(em).obtenerTodasLasActividades();
	}

	@Override
	public List<Actividad> obtenerTodasLasActividades(long idEleccion) {
		return DaoFactoryElecciones.createActividadDao(em).obtenerTodasLasActividades(idEleccion);
	}

	@Override
	public void persistirActividad(String nomAdmin, TipoActividad tipoActividad, String descripcion, String ip, Long idEleccion) {
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
	public List<UsuarioPadron> obtenerUsuariosPadron(long idEleccion) {
		return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccion(idEleccion);
	}

	@Override
	public void eliminarUsuarioAdmin(String userAdminId, String userId, String ip) {
		UsuarioAdmin a = DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerUsuarioAdmin(userAdminId);
		em.remove(a);
		String descripcion = userId.toUpperCase() + " eliminó a " + userAdminId.toUpperCase() + " de listado de admin";
		persistirActividad(userId, TipoActividad.ELIMINAR_ADMIN, descripcion, ip, null);
	}

	@Override
	public UsuarioAdmin obtenerUsuarioAdmin(String idUsuarioAdmin) {
		return em.find(UsuarioAdmin.class, idUsuarioAdmin);		
	}

	@Override
	public boolean agregarUsuarioAdmin(UsuarioAdmin adm, String adminId, String ip) {
		try {

			if (DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerUsuarioAdmin(adm.getUserAdminId()) == null) {
				UsuarioAdmin a = new UsuarioAdmin();
				a.setUserAdminId(adm.getUserAdminId().toLowerCase());
				a.setPassword(adm.getPassword().toUpperCase());
				a.setEmail(adm.getEmail().toLowerCase());
				a.setIdEleccionAutorizado(adm.getIdEleccionAutorizado());
				em.persist(a);
				String descripcion = adminId.toUpperCase() + " agregó a " + adm.getUserAdminId().toUpperCase() + " como admin";
				persistirActividad(adminId, TipoActividad.AGREGAR_ADMIN, descripcion, ip, adm.getIdEleccionAutorizado());
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
	public void editarUsuarioAdmin(UsuarioAdmin usuarioAdmin, String email, Long idEleccionAutorizado, String userId, String ipClient) {
		try {
			usuarioAdmin.setIdEleccionAutorizado(idEleccionAutorizado);
			em.merge(usuarioAdmin);
			String descripcion;
			if (userId.equalsIgnoreCase(usuarioAdmin.getUserAdminId())) {
				descripcion = userId.toUpperCase() + " editó  su email de " + email + " a " + usuarioAdmin.getEmail() + " y su elección autorizada a la de id " + idEleccionAutorizado;
			} else {
				descripcion = userId.toUpperCase() + " editó el email del usuario " + usuarioAdmin.getUserAdminId().toUpperCase() + " de " + email + " a " + usuarioAdmin.getEmail() + " y la elección autorizada a la de id " + idEleccionAutorizado;
			}
			persistirActividad(userId, TipoActividad.EDITAR_ADMIN, descripcion, ipClient, idEleccionAutorizado);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void editarPassAdmin(String adminUserId, String password, String userId, String ip) {
		try {
			UsuarioAdmin a = DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerUsuarioAdmin(adminUserId);
			a.setPassword(password);

			em.persist(a);
			String descripcion;
			if (userId.equalsIgnoreCase(adminUserId)) {
				descripcion = userId.toUpperCase() + " cambió su contraseña";
			} else {
				descripcion = userId.toUpperCase() + " cambió la contraseña de " + adminUserId.toUpperCase();
			}
			persistirActividad(userId, TipoActividad.EDITAR_ADMIN, descripcion, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List<AccesosIps> obtenerAccesosIps() {
		return DaoFactoryElecciones.createAccesoIpsDao(em).obtenerIpsInhabilitadasTodas();
	}

	@Override
	public List<Parametro> obtenerListadoParamteros() {
		return EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerListadoParametro();
	}

	@Override
	public boolean agregarParametro(String clave, String valor, String userId, String ip) {
		if (EJBFactory.getInstance().getParametrosEleccionesEJB().agregarParametro(clave, valor)) {
			String descripcion = userId.toUpperCase() + " creó el parámetro " + clave;
			persistirActividad(userId, TipoActividad.AGREGAR_PARAMETRO, descripcion, ip, null);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void editarParametro(Parametro p, String userId, String ip) {
		try {
			EJBFactory.getInstance().getParametrosEleccionesEJB().editarParametro(p);
			String descripcion = userId.toUpperCase() + " actualizó el parámetro " + p.getClave();
			persistirActividad(userId, TipoActividad.EDITAR_PARAMETRO, descripcion, ip, null);
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
			persistirActividad(userId, TipoActividad.ELIMINAR_PARAMETRO, descripcion, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public List<TemplateEleccion> obtenerTemplatesEleccion(long idEleccion) {
		if (idEleccion == 0)
			return DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplatesBase();
		else
			return DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplatesEleccion(idEleccion);

	}

	@Override
	public void modificarTemplateEleccion(TemplateEleccion t) {
		Eleccion eleccion = t.getEleccion();
		if (eleccion != null)
			em.merge(eleccion);
		em.merge(t);
	}

	@Override
	public List<TemplateEleccion> obtenerTemplatesBase() {
		return DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplatesBase();
	}

	@Override
	public TemplateEleccion obtenerTemplate(String tipo, long idEleccion) {
		if (idEleccion == 0)
			return DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplateBase(tipo);
		else
			return DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplate(tipo, idEleccion);
	}

	@Override
	public List obtenerDestinatariosTipoDestinatario(TemplateEleccion t) throws Exception {
		TipoDestinatario tipo = t.getTipoDestinatario();
		if (tipo.equals(TipoDestinatario.VOTANTES)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccion(t.getEleccion().getIdEleccion());
		} else if (tipo.equals(TipoDestinatario.VOTANTES_BR)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccion(t.getEleccion().getIdEleccion(), "BR");
		} else if (tipo.equals(TipoDestinatario.VOTANTES_MX)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccion(t.getEleccion().getIdEleccion(), "MX");
		}

		else if (tipo.equals(TipoDestinatario.VOTANTES_QUE_AUN_NO_VOTARON)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccionQueAunNoVotaron(t.getEleccion().getIdEleccion());
		} else if (tipo.equals(TipoDestinatario.VOTANTES_QUE_AUN_NO_VOTARON_BR)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccionQueAunNoVotaron(t.getEleccion().getIdEleccion(), "BR");
		} else if (tipo.equals(TipoDestinatario.VOTANTES_QUE_AUN_NO_VOTARON_MX)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccionQueAunNoVotaron(t.getEleccion().getIdEleccion(), "MX");
		}

		else if (tipo.equals(TipoDestinatario.VOTANTES_QUE_YA_VOTARON)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccionQueYaVotaron(t.getEleccion().getIdEleccion());
		} else if (tipo.equals(TipoDestinatario.VOTANTES_QUE_YA_VOTARON_BR)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccionQueYaVotaron(t.getEleccion().getIdEleccion(), "BR");
		} else if (tipo.equals(TipoDestinatario.VOTANTES_QUE_YA_VOTARON_MX)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronPaisEleccionQueYaVotaron(t.getEleccion().getIdEleccion(), "MX");
		}

		else if (tipo.equals(TipoDestinatario.VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccionQueNoVotaronEleccionDual(t.getEleccion().getIdEleccion());
		} else if (tipo.equals(TipoDestinatario.VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES_BR)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccionQueNoVotaronEleccionDual(t.getEleccion().getIdEleccion(), "BR");
		} else if (tipo.equals(TipoDestinatario.VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES_MX)) {
			return DaoFactoryElecciones.createUsuarioPadronDao(em).obtenerUsuariosPadronEleccionQueNoVotaronEleccionDual(t.getEleccion().getIdEleccion(), "MX");
		}

		else if (tipo.equals(TipoDestinatario.AUDITORES)) {
			return DaoFactoryElecciones.createAuditorDao(em).obtenerAuditoresEleccion(t.getEleccion().getIdEleccion());
		} else if (tipo.equals(TipoDestinatario.AUDITORES_QUE_AUN_NO_CONFORMARON)) {
			return DaoFactoryElecciones.createAuditorDao(em).obtenerAuditoresEleccionQueNoConformaron(t.getEleccion().getIdEleccion());
		} else if (tipo.equals(TipoDestinatario.AUDITORES_QUE_YA_CONFORMARON)) {
			return DaoFactoryElecciones.createAuditorDao(em).obtenerAuditoresEleccionQueYaConformaron(t.getEleccion().getIdEleccion());
		} else
			throw new Exception("Nunca debería haber llegado aquí");
	}

	@Override
	public boolean agregarPlantillaBase(TemplateEleccion template, String userId, String ip) {
		try {
			template.setTipoTemplate(template.getTipoTemplate().toUpperCase());
			em.persist(template);
			String descripcion = userId.toUpperCase() + " creó un nuevo template base " + template.getTipoTemplate();
			persistirActividad(userId, TipoActividad.AGREGAR_TEMPLATE_BASE, descripcion, ip, null);
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
		List<TemplateEleccion> bases = obtenerTemplatesBase();
		List<Eleccion> eleccionesSinTemplate = obtenerEleccionesLight();
		for (int j = 0; j < eleccionesSinTemplate.size(); j++) {
			Eleccion eleccionJ = eleccionesSinTemplate.get(j);
			for (int i = 0; i < bases.size(); i++) {
				TemplateEleccion templateI = bases.get(i);
				if (obtenerTemplate(templateI.getTipoTemplate(), eleccionJ.getIdEleccion()) == null) {
					em.persist(new TemplateEleccion(eleccionJ, templateI));
					cuenta++;
				}
			}
		}
		return cuenta;
	}

	@Override
	public void encolarEnvioMasivo(List usuarios, TemplateEleccion templateEleccion) {
		EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioMasivo(usuarios, templateEleccion);

	}

	@Override
	public Candidato obtenerCandidatoDEArriba(Candidato candidato) {
		return DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatoDeArriba(candidato.getEleccion().getIdEleccion(), candidato.getOrden());

	}

	@Override
	public Candidato obtenerCandidatoDEAbajo(Candidato candidato) {
		return DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatoDeAbajo(candidato.getEleccion().getIdEleccion(), candidato.getOrden());

	}

	@Override
	public void fijarCandidatoAlPrincipio(long idCandidato) {
		Candidato candidato = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
		Candidato candidatoViejo = DaoFactoryElecciones.createCandidatoDao(em).obtenerPrimerCandidato(candidato.getEleccion().getIdEleccion());
		if (candidatoViejo != null) {
			candidatoViejo.setOrden(candidato.getOrden());
		}
		candidato.setOrden(Constantes.ORDEN_MAXIMO);
		em.persist(candidato);
		em.persist(candidatoViejo);
	}

	@Override
	public void nofijarCandidatoAlPrincipio(long idCandidato) {
		Candidato candidato = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
		int orden = DaoFactoryElecciones.createCandidatoDao(em).obtenerOrdenDelUltimoCandidatoNofijado(candidato.getEleccion().getIdEleccion());
		candidato.setOrden(orden + 1);
		em.persist(candidato);
	}

	@Override
	public void subirCandidato(long idCandidato) {
		Candidato candidato = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
		Candidato candidatoCercano = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatoDeArriba(candidato.getEleccion().getIdEleccion(), candidato.getOrden());
		if (candidatoCercano != null && candidatoCercano.getOrden() != Constantes.ORDEN_MAXIMO) {
			appLogger.info(candidato.getOrden() + " - " + candidatoCercano.getOrden());

			int aux = candidato.getOrden();
			candidato.setOrden(candidatoCercano.getOrden());
			candidatoCercano.setOrden(aux);
			em.persist(candidato);
			em.persist(candidatoCercano);
		}

	}

	@Override
	public void bajarCandidato(long idCandidato) {
		Candidato candidato = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
		Candidato candidatoCercano = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidatoDeAbajo(candidato.getEleccion().getIdEleccion(), candidato.getOrden());
		if (candidatoCercano != null && candidatoCercano.getOrden() != Constantes.ORDEN_MINIMO) {
			int aux = candidato.getOrden();
			appLogger.info(candidato.getOrden() + " - " + candidatoCercano.getOrden());

			candidato.setOrden(candidatoCercano.getOrden());
			candidatoCercano.setOrden(aux);
			em.persist(candidato);
			em.persist(candidatoCercano);
		}
	}

	@Override
	public void fijarCandidatoAlFinal(long idCandidato) {
		Candidato candidato = DaoFactoryElecciones.createCandidatoDao(em).obtenerCandidato(idCandidato);
		Candidato candidatoViejo = DaoFactoryElecciones.createCandidatoDao(em).obtenerUltimoCandidato(candidato.getEleccion().getIdEleccion());
		if (candidatoViejo != null) {
			candidatoViejo.setOrden(candidato.getOrden());
		}
		candidato.setOrden(Constantes.ORDEN_MINIMO);
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
	public List<Voto> obtenerVotos(Long idEleccion) {
		return DaoFactoryElecciones.createVotoDao(em).obtenerVotosEleccion(idEleccion);
	}

	@Override
	public boolean agregarComisionado(String nombre, String mail, String userId, String ip) {
		try {
			if (DaoFactoryElecciones.createComisionadoDao(em).obtenerComisionado(mail) == null) {
				Comisionado a = new Comisionado();
				a.setNombre(nombre);
				a.setMail(mail);
				em.persist(a);
				String descripcion = userId.toUpperCase() + " agregó al comisionado " + nombre;
				persistirActividad(userId, TipoActividad.AGREGAR_COMISIONADO, descripcion, ip, null);
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
		Comisionado a = DaoFactoryElecciones.createComisionadoDao(em).obtenerComisionado(idComisionado);
		em.remove(a);
		String descripcion = userId.toUpperCase() + " eliminó al comisionado: " + nombre + " del sistema";
		persistirActividad(userId, TipoActividad.ELIMINAR_COMISIONADO, descripcion, ip, null);
	}

	@Override
	public void editarComisionado(Comisionado comisionado, String userId, String ip) {
		try {
			em.merge(comisionado);
			persistirActividad(userId, TipoActividad.EDITAR_COMISIONADO, userId + " ha editado el comidionado " + comisionado.getNombre(), ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public Comisionado obtenerComisionado(long idComisionado) {
		return DaoFactoryElecciones.createComisionadoDao(em).obtenerComisionado(idComisionado);

	}

	@Override
	public void reenviarEmailPadron(UsuarioPadron us, Eleccion e, String adminId, String ipClient) {
		TemplateEleccion tComienzo = DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplate("AVISO_ELECCION", e.getIdEleccion());
		TemplateEleccion tEmpezo = DaoFactoryElecciones.createTemplateEleccionesDao(em).obtenerTemplate("COMIENZO_ELECCION", e.getIdEleccion());
		if (tComienzo != null || tEmpezo != null) {
			Date now = new Date();
			if (((now.after(e.getFechaCreacion())) && (now.before(e.getFechaInicio()))))
				EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioIndividual(tComienzo, us, null, e, new ArrayList<>());
			if ((e.isHabilitadoLinkVotacion()) && ((now.after(e.getFechaInicio())) && (now.before(e.getFechaFin()))))
				EJBFactory.getInstance().getEnvioMailsEJB().encolarEnvioIndividual(tEmpezo, us, null, e, new ArrayList<>());
			String descripcion = adminId.toUpperCase() + " reénvio el email al usuario padrón " + us.getNombre().toUpperCase() + " de la elección " + "(" + e.getTituloEspanol() + ")";
			persistirActividad(adminId, TipoActividad.REENVIAR_EMAIL_ELECCION_USUPADRON, descripcion, ipClient, e.getIdEleccion());
		}
	}

	@Override
	public long obtenerIdEleccionUsuAdmin(String adminId) {
		return DaoFactoryElecciones.createUsuarioAdminDao(em).obtenerIdEleccionUsuAdmin(adminId);
	}

	@Override
	public Parametro getParametro(String claveId) {
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
		return EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constantes.REMITENTE_ESTANDAR);
	}

	@Override
	public String obtenerWebsitePorDefecto() {
		return EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constantes.WEBSITE_DEFAULT);
	}

	@Override
	public void actualizarSupraEleccion(SupraEleccion supraEleccion) {
		em.merge(supraEleccion);
	}

	@Override
	public List<SupraEleccion> obtenerSupraElecciones() {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerSupraElecciones();
	}


	@Override
	public boolean isSupraEleccion(long idEleccion) {
		return !DaoFactoryElecciones.createEleccionDao(em).isEleccionSimple(idEleccion);
	}

	@Override
	public SupraEleccion obtenerSupraEleccion(long idEleccion) {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerSupraEleccion(idEleccion);
	}

	@Override
	public void eliminarSupraEleccion(SupraEleccion supraEleccion) {
		em.remove(em.contains(supraEleccion) ? supraEleccion : em.merge(supraEleccion));
	}

	@Override
	public boolean isPadronesIguales(SupraEleccion supraEleccion) {
		return DaoFactoryElecciones.createUsuarioPadronDao(em).padronesIgualesEleccionDual(supraEleccion.getIdEleccionA(), supraEleccion.getIdEleccionB());
	}

	@Override
	public List<String> obtenerEleccionesIdDesc() {
		return DaoFactoryElecciones.createEleccionDao(em).obtenerEleccionesIdDesc();
	}

	@Override
	public Personalizacion getPersonalizacion() {
		return DaoFactoryElecciones.createPersonalizacionDao(em).getPersonalizacion();
	}

	@Override
	public boolean actualizarPersonalizacion(Personalizacion personalizacion) {
		try {
			em.merge(personalizacion);
			return true;
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	public String getDataSiteKey() {
		try {
			return EJBFactory.getInstance().getParametrosEleccionesEJB().getDataSiteKey();
		} catch (Exception e) {
			appLogger.error(e);
			return "";
		}
	}
}