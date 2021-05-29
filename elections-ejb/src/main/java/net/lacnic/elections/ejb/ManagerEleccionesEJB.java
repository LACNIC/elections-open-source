package net.lacnic.elections.ejb;

import java.io.File;
import java.util.List;

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
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.exception.CensusValidationException;

public interface ManagerEleccionesEJB {

	public UserAdmin loginAdmin(String adminId, String password, String ip);

	public UserAdmin obtenerUsuarioAdmin(String idUsuarioAdmin);

	public boolean isValidCaptchaResponse(String reCaptchaResponse);

	public boolean isProd();

	public Election obtenerEleccion(long idEleccion);

	public List<Candidate> obtenerCandidatosEleccionOrdenados(long idEleccion);

	public void darDeBajaEleccion(long idEleccion, String titulo, String userId, String ip) throws Exception;

	public List<Auditor> obtenerAuditoresEleccion(long idEleccion) throws Exception;

	public List<Election> obtenerEleccionesLight();

	public UserVoter obtenerUsuarioPadron(long idUsuarioPadron);

	public Election actualizarEleccion(Election eleccion, String userId, String ip) throws Exception;

	public String obtenerLinkresultado(Election eleccion) throws Exception;

	public List<UserAdmin> obtenerUsuariosAdmin();

	public void habilitarLinkResultado(Long id, Boolean valor, String admin, String ip);

	public void habilitarLinkAuditoria(Long id, Boolean valor, String admin, String ip);

	public List<Comissioner> obtenerComisionados();

	public void eliminarUsuarioPadron(UserVoter actual, String titulo, String userId, String ip);

	public List<Activity> obtenerTodasLasActividades();

	public List<Activity> obtenerTodasLasActividades(long idEleccion);

	public List<UserVoter> obtenerUsuariosPadron(long idEleccion);

	public void actualizarTokenUsuarioPadron(long idUsp, String nombre, String titulo, String userId, String ip);

	public void eliminarUsuarioAdmin(String userAdminId, String userId, String ip);

	public List<UserVoter> obtenerUsuariosPadronEleccion(long idEleccion);

	public void actualizarUsuariosPadron(long idEleccion, byte[] contenido, String admin, String ip) throws CensusValidationException, Exception;

	public List<Election> obtenerEleccionesLightEsteAnio();

	public boolean agregarUsuarioPadron(long idEleccion, UserVoter up, String userId, String ip) throws CensusValidationException;

	public void editarUsuarioPadron(UserVoter usuarioPadron, String userId, String ip) throws CensusValidationException;

	public List<TemplateElection> obtenerTemplatesEleccion(long idEleccion);

	public void modificarTemplateEleccion(TemplateElection t);

	public void persistirActividad(String nomAdmin, ActivityType tipoActividad, String descripcion, String ip, Long idEleccion);

	public void habilitarLinkVotacion(Long id, Boolean valor, String admin, String ip);

	public List<TemplateElection> obtenerTemplatesBase();

	public TemplateElection obtenerTemplate(String tipo, long idEleccion);

	public void editarUsuarioAdmin(UserAdmin usuarioAdmin, String email, Long idEleccionAutorizado, String userId, String ipClient);

	public File exportarPadronElectoral(long idEleccion);

	public void editarPassAdmin(String adminUserId, String password, String userId, String ip);

	public boolean agregarUsuarioAdmin(UserAdmin a, String adminId, String ip);

	public void agregarCandidato(long idEleccion, Candidate candidato, String userId, String ip);

	public void eliminarCandidato(long idCandidato, String userId, String ip);

	public void eliminarAuditor(long idAuditor, String userId, String ip);

	public void agregarAuditor(long idEleccion, Auditor auditor, String tituloEspaniol, String userId, String ip);

	public void persistirAuditoresSeteados(long idEleccion, String tituloEleccion, String userId, String ip);

	public List<Parameter> obtenerListadoParamteros();

	public List<IpAccess> obtenerAccesosIps();

	public boolean agregarParametro(String clave, String valor, String userId, String ip);

	public void editarParametro(Parameter p, String userId, String ip);

	public void borrarParametro(String clave, String userId, String ip);

	public List obtenerDestinatariosTipoDestinatario(TemplateElection t) throws Exception;

	public Integer crearPlantillasEleccionesQueLeFalten();

	public void encolarEnvioMasivo(List usuariosAuditor, TemplateElection templateEleccion);

	public void fijarCandidatoAlPrincipio(long idCandidato);

	public void subirCandidato(long idCandidato);

	public void bajarCandidato(long idCandidato);

	public void fijarCandidatoAlFinal(long idCandidato);

	public void nofijarCandidatoAlPrincipio(long idCandidato);

	public void ordenarCandidatosAleatoriamente(Long id, Boolean valor);

	public Auditor obtenerAuditor(long idAuditor);

	public void editarAuditor(Auditor auditor, String userId, String ip);

	public List<Email> obtenerMailsPorEnviar();

	public List<Email> obtenerEmailsAll();

	public List<Email> obtenerMailsDeEleccion(Long idEleccion);

	public List<Email> obtenerMailsPorEnviarDeEleccion(Long idEleccion);

	public boolean agregarComisionado(String nombre, String mail, String userId, String ip);

	public void eliminarComisionado(long idComisionado, String nombre, String userId, String ip);

	public void editarComisionado(Comissioner comisionado, String userId, String ip);

	public File exportarEjemploPadronElectoral();

	public File exportarEjemploPadronElectoral(String filePath);

	public void crearPlantillasEleccion(Election eleccion);

	public Candidate obtenerCandidato(long idCandidato);

	public void editarCandidato(Candidate candidato, String userId, String ip);

	public Comissioner obtenerComisionado(long idComisionado);

	public void reenviarEmailPadron(UserVoter us, Election e, String adminId, String ipClient);

	public long obtenerIdEleccionUsuAdmin(String adminId);

	public Parameter getParametro(String claveId);

	public void solicitarRevision(Long id, Boolean valor, String admin, String ip);

	public List<Vote> obtenerVotos(Long idEleccion);

	public boolean isRevisionActiva(long idEleccion, String admin, String ip);

	public Candidate obtenerCandidatoDEArriba(Candidate candidato);

	public Candidate obtenerCandidatoDEAbajo(Candidate candidato);

	public boolean existeComisionado(String nombre, String email);

	public boolean existeAuditor(long idEleccion, String nombre, String email);

	public String obtenerRemitentePorDefecto();

	public String obtenerWebsitePorDefecto();

	boolean agregarPlantillaBase(TemplateElection template, String userId, String ip);

	void actualizarSupraEleccion(JointElection supraEleccion);

	List<JointElection> obtenerSupraElecciones();
	
	void eliminarSupraEleccion(JointElection supraEleccion);

	List<String> obtenerEleccionesIdDesc();

	boolean isSupraEleccion(long idEleccion);
	
	public JointElection obtenerSupraEleccion(long idEleccion);
	
	boolean isPadronesIguales(JointElection supraEleccion);

	public Customization getPersonalizacion();

	public boolean actualizarPersonalizacion(Customization personalizacion);



}
