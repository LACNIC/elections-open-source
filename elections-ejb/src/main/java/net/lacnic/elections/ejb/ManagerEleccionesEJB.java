package net.lacnic.elections.ejb;

import java.io.File;
import java.util.List;

import net.lacnic.elections.domain.AccesosIps;
import net.lacnic.elections.domain.Actividad;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidato;
import net.lacnic.elections.domain.Comisionado;
import net.lacnic.elections.domain.Eleccion;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.Parametro;
import net.lacnic.elections.domain.Personalizacion;
import net.lacnic.elections.domain.SupraEleccion;
import net.lacnic.elections.domain.TemplateEleccion;
import net.lacnic.elections.domain.TipoActividad;
import net.lacnic.elections.domain.UsuarioAdmin;
import net.lacnic.elections.domain.UsuarioPadron;
import net.lacnic.elections.domain.Voto;
import net.lacnic.elections.exception.CensusValidationException;

public interface ManagerEleccionesEJB {

	public UsuarioAdmin loginAdmin(String adminId, String password, String ip);

	public UsuarioAdmin obtenerUsuarioAdmin(String idUsuarioAdmin);

	public boolean isValidCaptchaResponse(String reCaptchaResponse);

	public boolean isProd();

	public Eleccion obtenerEleccion(long idEleccion);

	public List<Candidato> obtenerCandidatosEleccionOrdenados(long idEleccion);

	public void darDeBajaEleccion(long idEleccion, String titulo, String userId, String ip) throws Exception;

	public List<Auditor> obtenerAuditoresEleccion(long idEleccion) throws Exception;

	public List<Eleccion> obtenerEleccionesLight();

	public UsuarioPadron obtenerUsuarioPadron(long idUsuarioPadron);

	public Eleccion actualizarEleccion(Eleccion eleccion, String userId, String ip) throws Exception;

	public String obtenerLinkresultado(Eleccion eleccion) throws Exception;

	public List<UsuarioAdmin> obtenerUsuariosAdmin();

	public void habilitarLinkResultado(Long id, Boolean valor, String admin, String ip);

	public void habilitarLinkAuditoria(Long id, Boolean valor, String admin, String ip);

	public List<Comisionado> obtenerComisionados();

	public void eliminarUsuarioPadron(UsuarioPadron actual, String titulo, String userId, String ip);

	public List<Actividad> obtenerTodasLasActividades();

	public List<Actividad> obtenerTodasLasActividades(long idEleccion);

	public List<UsuarioPadron> obtenerUsuariosPadron(long idEleccion);

	public void actualizarTokenUsuarioPadron(long idUsp, String nombre, String titulo, String userId, String ip);

	public void eliminarUsuarioAdmin(String userAdminId, String userId, String ip);

	public List<UsuarioPadron> obtenerUsuariosPadronEleccion(long idEleccion);

	public void actualizarUsuariosPadron(long idEleccion, byte[] contenido, String admin, String ip) throws CensusValidationException, Exception;

	public List<Eleccion> obtenerEleccionesLightEsteAnio();

	public boolean agregarUsuarioPadron(long idEleccion, UsuarioPadron up, String userId, String ip) throws CensusValidationException;

	public void editarUsuarioPadron(UsuarioPadron usuarioPadron, String userId, String ip) throws CensusValidationException;

	public List<TemplateEleccion> obtenerTemplatesEleccion(long idEleccion);

	public void modificarTemplateEleccion(TemplateEleccion t);

	public void persistirActividad(String nomAdmin, TipoActividad tipoActividad, String descripcion, String ip, Long idEleccion);

	public void habilitarLinkVotacion(Long id, Boolean valor, String admin, String ip);

	public List<TemplateEleccion> obtenerTemplatesBase();

	public TemplateEleccion obtenerTemplate(String tipo, long idEleccion);

	public void editarUsuarioAdmin(UsuarioAdmin usuarioAdmin, String email, Long idEleccionAutorizado, String userId, String ipClient);

	public File exportarPadronElectoral(long idEleccion);

	public void editarPassAdmin(String adminUserId, String password, String userId, String ip);

	public boolean agregarUsuarioAdmin(UsuarioAdmin a, String adminId, String ip);

	public void agregarCandidato(long idEleccion, Candidato candidato, String userId, String ip);

	public void eliminarCandidato(long idCandidato, String userId, String ip);

	public void eliminarAuditor(long idAuditor, String userId, String ip);

	public void agregarAuditor(long idEleccion, Auditor auditor, String tituloEspaniol, String userId, String ip);

	public void persistirAuditoresSeteados(long idEleccion, String tituloEleccion, String userId, String ip);

	public List<Parametro> obtenerListadoParamteros();

	public List<AccesosIps> obtenerAccesosIps();

	public boolean agregarParametro(String clave, String valor, String userId, String ip);

	public void editarParametro(Parametro p, String userId, String ip);

	public void borrarParametro(String clave, String userId, String ip);

	public List obtenerDestinatariosTipoDestinatario(TemplateEleccion t) throws Exception;

	public Integer crearPlantillasEleccionesQueLeFalten();

	public void encolarEnvioMasivo(List usuariosAuditor, TemplateEleccion templateEleccion);

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

	public void editarComisionado(Comisionado comisionado, String userId, String ip);

	public File exportarEjemploPadronElectoral();

	public File exportarEjemploPadronElectoral(String filePath);

	public void crearPlantillasEleccion(Eleccion eleccion);

	public Candidato obtenerCandidato(long idCandidato);

	public void editarCandidato(Candidato candidato, String userId, String ip);

	public Comisionado obtenerComisionado(long idComisionado);

	public void reenviarEmailPadron(UsuarioPadron us, Eleccion e, String adminId, String ipClient);

	public long obtenerIdEleccionUsuAdmin(String adminId);

	public Parametro getParametro(String claveId);

	public void solicitarRevision(Long id, Boolean valor, String admin, String ip);

	public List<Voto> obtenerVotos(Long idEleccion);

	public boolean isRevisionActiva(long idEleccion, String admin, String ip);

	public Candidato obtenerCandidatoDEArriba(Candidato candidato);

	public Candidato obtenerCandidatoDEAbajo(Candidato candidato);

	public boolean existeComisionado(String nombre, String email);

	public boolean existeAuditor(long idEleccion, String nombre, String email);

	public String obtenerRemitentePorDefecto();

	public String obtenerWebsitePorDefecto();

	boolean agregarPlantillaBase(TemplateEleccion template, String userId, String ip);

	void actualizarSupraEleccion(SupraEleccion supraEleccion);

	List<SupraEleccion> obtenerSupraElecciones();
	
	void eliminarSupraEleccion(SupraEleccion supraEleccion);

	List<String> obtenerEleccionesIdDesc();

	boolean isSupraEleccion(long idEleccion);
	
	public SupraEleccion obtenerSupraEleccion(long idEleccion);
	
	boolean isPadronesIguales(SupraEleccion supraEleccion);

	public Personalizacion getPersonalizacion();

	public boolean actualizarPersonalizacion(Personalizacion personalizacion);



}
