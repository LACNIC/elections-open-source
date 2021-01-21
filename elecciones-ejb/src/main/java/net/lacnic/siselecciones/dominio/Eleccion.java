package net.lacnic.siselecciones.dominio;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import net.lacnic.siselecciones.utils.StringUtils;
import net.lacnic.siselecciones.utils.UtilsLinks;

@Entity
public class Eleccion implements Serializable {
	
	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	
	private static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy HH:mm";

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eleccion_seq")
	@SequenceGenerator(name = "eleccion_seq", sequenceName = "eleccion_seq", allocationSize = 1)
	@Column(name = "id_eleccion")
	private long idEleccion;

	@Column(nullable = true)
	private Long idMigracion;

	@Column
	@Enumerated(EnumType.STRING)
	CategoriaEleccion categoria;

	@Column
	private boolean migrada = false;

	@Column(nullable = false)
	private Date fechaInicio;

	@Column(nullable = false)
	private Date fechaFin;

	@Column(nullable = false)
	private Date fechaCreacion;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String tituloEspanol;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String tituloIngles;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String tituloPortugues;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String linkEspanol;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String linkIngles;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String linkPortugues;

	@Column(columnDefinition = "TEXT")
	private String descripcionEspanol;

	@Column(columnDefinition = "TEXT")
	private String descripcionIngles;

	@Column(columnDefinition = "TEXT")
	private String descripcionPortugues;

	@Column(nullable = false)
	private int maxCandidatos;

	@Column(nullable = false)
	private boolean habilitadoLinkVotacion;

	@Column(nullable = false)
	private boolean habilitadoLinkResultado;

	@Column(nullable = false)
	private boolean habilitadoLinkAuditor;

	@Column(nullable = false)
	private boolean solicitarRevision;

	@Column(nullable = true)
	private boolean solosp;

	@Column(nullable = true, length = 1000)
	private String tokenResultado;

	@Column(nullable = true, length = 2000)
	private String remitentePorDefecto;

	@Column(nullable = true)
	private boolean padronSeteado;

	@Column(nullable = true)
	private boolean candidatosSeteado;

	@Column(nullable = true)
	private boolean auditoresSeteado;

	@Column(nullable = true)
	private boolean candidatosAleatorios;

	@Column(nullable = true)
	private int diffUTC;

	@OneToMany(mappedBy = "eleccion", cascade = CascadeType.REMOVE)
	private List<Candidato> candidatos;

	@OneToMany(mappedBy = "eleccion", cascade = CascadeType.REMOVE)
	private List<UsuarioPadron> usuariosPadron;

	@OneToMany(mappedBy = "eleccion", cascade = CascadeType.REMOVE)
	private List<Auditor> auditores;

	@OneToMany(mappedBy = "eleccion", cascade = CascadeType.REMOVE)
	private List<TemplateEleccion> templatesEleccion;

	@OneToMany(mappedBy = "eleccion", cascade = CascadeType.REMOVE)
	private List<Voto> votos;

	@OneToMany(mappedBy = "eleccion", cascade = CascadeType.REMOVE)
	private List<Email> email;

	@Transient
	String auxFechaInicio = "";
	@Transient
	String auxHoraInicio = "";
	@Transient
	String auxFechaFin = "";
	@Transient
	String auxHoraFin = "";

	public Eleccion() {
		setMigrada(false);
		setFechaCreacion(new Date());
		setHabilitadoLinkResultado(false);
		setHabilitadoLinkVotacion(true);
		setHabilitadoLinkAuditor(false);
		setPadronSeteado(false);
		setSolosp(true);
		setCandidatosSeteado(false);
		setAuditoresSeteado(false);
		setCandidatosAleatorios(true);
		setTokenResultado(StringUtils.createSecureToken());
		setDiffUTC(3);
	}

	public Eleccion(long id) {
		setIdEleccion(id);
		setMigrada(false);
		setFechaCreacion(new Date());
		setHabilitadoLinkResultado(false);
		setHabilitadoLinkVotacion(true);
		setHabilitadoLinkAuditor(false);
		setPadronSeteado(false);
		setSolosp(true);
		setCandidatosSeteado(false);
		setAuditoresSeteado(false);
		setCandidatosAleatorios(true);
		setTokenResultado(StringUtils.createSecureToken());
		setTitulo("TODOS");
	}

	public long getIdEleccion() {
		return idEleccion;
	}

	public void setIdEleccion(long idEleccion) {
		this.idEleccion = idEleccion;
	}

	public Date getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public Date getFechaFin() {
		return fechaFin;
	}

	/**
	 * Modificacion del setter de la hora de Finalizacion para permitir votar
	 * hasta el ultimo minuto del día
	 * 
	 * @param fechaFin
	 *            Dia en el que quiero que termine la eleccion
	 */
	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}

	public int getMaxCandidatos() {
		return maxCandidatos;
	}

	public void setMaxCandidatos(int maxCandidatos) {
		this.maxCandidatos = maxCandidatos;
	}

	public boolean isHabilitadoLinkVotacion() {
		return habilitadoLinkVotacion;
	}

	public void setHabilitadoLinkVotacion(boolean habilitadoLinkVotacion) {
		this.habilitadoLinkVotacion = habilitadoLinkVotacion;
	}

	public List<Candidato> getCandidatos() {
		return candidatos;
	}

	public void setCandidatos(List<Candidato> candidatos) {
		this.candidatos = candidatos;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public List<UsuarioPadron> getUsuariosPadron() {
		return usuariosPadron;
	}

	public void setUsuariosPadron(List<UsuarioPadron> usuariosPadron) {
		this.usuariosPadron = usuariosPadron;
	}

	public List<Voto> getVotos() {
		return votos;
	}

	public void setVotos(List<Voto> votos) {
		this.votos = votos;
	}

	public boolean isHabilitadoLinkResultado() {
		return habilitadoLinkResultado;
	}

	public void setHabilitadoLinkResultado(boolean habilitadoLinkResultado) {
		this.habilitadoLinkResultado = habilitadoLinkResultado;
	}

	public String getTituloEspanol() {
		return tituloEspanol;
	}

	private void setTitulo(String titulo) {
		setTituloEspanol(titulo);
		setTituloPortugues(titulo);
		setTituloIngles(titulo);
	}

	public void setTituloEspanol(String tituloEspanol) {
		this.tituloEspanol = tituloEspanol;
	}

	public String getTituloIngles() {
		return tituloIngles;
	}

	public void setTituloIngles(String tituloIngles) {
		this.tituloIngles = tituloIngles;
	}

	public String getTituloPortugues() {
		return tituloPortugues;
	}

	public void setTituloPortugues(String tituloPortugues) {
		this.tituloPortugues = tituloPortugues;
	}

	public String getDescripcionEspanol() {
		return descripcionEspanol;
	}

	public void setDescripcionEspanol(String descripcionEspanol) {
		this.descripcionEspanol = descripcionEspanol;
	}

	public String getDescripcionIngles() {
		return descripcionIngles;
	}

	public void setDescripcionIngles(String descripcionIngles) {
		this.descripcionIngles = descripcionIngles;
	}

	public String getDescripcionPortugues() {
		return descripcionPortugues;
	}

	public void setDescripcionPortugues(String descripcionPortugues) {
		this.descripcionPortugues = descripcionPortugues;
	}

	public String getTokenResultado() {
		return tokenResultado;
	}

	public void setTokenResultado(String tokenResultado) {
		this.tokenResultado = tokenResultado;
	}

	public List<Auditor> getAuditores() {
		return auditores;
	}

	public void setAuditores(List<Auditor> auditores) {
		this.auditores = auditores;
	}

	public String getLinkEspanol() {
		return linkEspanol;
	}

	public void setLinkEspanol(String linkEspanol) {
		this.linkEspanol = linkEspanol;
	}

	public String getLinkIngles() {
		return linkIngles;
	}

	public void setLinkIngles(String linkIngles) {
		this.linkIngles = linkIngles;
	}

	public String getLinkPortugues() {
		return linkPortugues;
	}

	public void setLinkPortugues(String linkPortugues) {
		this.linkPortugues = linkPortugues;
	}

	public String getDescripcion(String displayName) {
		if (displayName.toLowerCase().contains("en") || displayName.toLowerCase().contains("english"))
			return getDescripcionIngles();
		else if (displayName.toLowerCase().contains("pt") || displayName.toLowerCase().contains("portuguese"))
			return getDescripcionPortugues();
		return getDescripcionEspanol();

	}

	public String getTitulo(String displayName) {
		if (displayName.toLowerCase().contains("en") || displayName.toLowerCase().contains("english"))
			return getTituloIngles();
		else if (displayName.toLowerCase().contains("pt") || displayName.toLowerCase().contains("portuguese"))
			return getTituloPortugues();
		else
			return getTituloEspanol();

	}

	public String getAuxFechaInicio() {
		return auxFechaInicio;
	}

	public void setAuxFechaInicio(String auxFechaInicio) {
		this.auxFechaInicio = auxFechaInicio;
	}

	public String getAuxHoraInicio() {
		return auxHoraInicio;
	}

	public void setAuxHoraInicio(String auxHoraInicio) {
		this.auxHoraInicio = auxHoraInicio;
	}

	public String getAuxHoraFin() {
		return auxHoraFin;
	}

	public void setAuxHoraFin(String auxHoraFin) {
		this.auxHoraFin = auxHoraFin;
	}

	// este metodo siempre se llama antes de crear o editar la eleccion, en
	// ambos casos se actualiza las fechas. la zona horaria no se inicializa mas
	// aca solo se calcula al crear la elección y luego se puede cambiar
	public void initDatesFechaInicioyFin() {
		if (!getAuxFechaFin().isEmpty() && !getAuxFechaInicio().isEmpty() && !getAuxHoraInicio().isEmpty() && !getAuxHoraFin().isEmpty()) {

			String datetimeInicio = getAuxFechaInicio() + " " + getAuxHoraInicio();
			String datetimeFin = getAuxFechaFin() + " " + getAuxHoraFin();
			SimpleDateFormat sdf = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
			
			try {
				Date antesDeRestarInicio = sdf.parse(datetimeInicio);
				Date antesDeRestarFin = sdf.parse(datetimeFin);
				setFechaInicio(new DateTime(antesDeRestarInicio).minusHours(getDiffUTC()).toDate());
				setFechaFin(new DateTime(antesDeRestarFin).minusHours(getDiffUTC()).toDate());
			} catch (ParseException e) {
				appLogger.error(e);
			}
		}

	}

	// este se llama siempre antes de mostrar, por lo que hay que sumarle la
	// diferencia para que lo muestre en UTC.
	public void initStringsFechaInicioyFin() {
		Date fechaInicioSinCambiar = getFechaInicio();
		Date fechaFinSinCambiar = getFechaFin();
		if (fechaFinSinCambiar != null && fechaInicioSinCambiar != null) {
			SimpleDateFormat dia = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat hora = new SimpleDateFormat("HH:mm");

			setAuxFechaInicio(dia.format(new DateTime(getFechaInicio()).plusHours(getDiffUTC()).toDate()));
			setAuxHoraInicio(hora.format(new DateTime(getFechaInicio()).plusHours(getDiffUTC()).toDate()));
			setAuxFechaFin(dia.format(new DateTime(getFechaFin()).plusHours(getDiffUTC()).toDate()));
			setAuxHoraFin(hora.format(new DateTime(getFechaFin()).plusHours(getDiffUTC()).toDate()));
		}
	}

	public String getAuxFechaFin() {
		return auxFechaFin;
	}

	public void setAuxFechaFin(String auxFechaFin) {
		this.auxFechaFin = auxFechaFin;
	}

	public void copiarDescripcionesIdiomaAlResto(String idiomaACopiar)  {

		if (idiomaACopiar.equalsIgnoreCase("EN")) {
			setDescripcionEspanol(getDescripcionIngles());
			setDescripcionPortugues(getDescripcionIngles());

		} else if (idiomaACopiar.equalsIgnoreCase("PT")) {

			setDescripcionIngles(getDescripcionPortugues());
			setDescripcionEspanol(getDescripcionPortugues());

		} else {
			setDescripcionIngles(getDescripcionEspanol());
			setDescripcionPortugues(getDescripcionEspanol());

		}

	}

	public void copiarTitulosIdiomaAlResto(String idiomaACopiar) {

		if (idiomaACopiar.equalsIgnoreCase("EN")) {

			setTituloEspanol(getTituloIngles());
			setTituloPortugues(getTituloIngles());

		} else if (idiomaACopiar.equalsIgnoreCase("PT")) {

			setTituloIngles(getTituloPortugues());
			setTituloEspanol(getTituloPortugues());

		} else {
			setTituloIngles(getTituloEspanol());
			setTituloPortugues(getTituloEspanol());

		}

	}

	public void copiarUrlsIdiomaAlResto(String idiomaACopiar)  {

		if (idiomaACopiar.equalsIgnoreCase("EN")) {

			setLinkEspanol(getLinkIngles());
			setLinkPortugues(getLinkIngles());

		} else if (idiomaACopiar.equalsIgnoreCase("PT")) {

			setLinkIngles(getLinkPortugues());
			setLinkEspanol(getLinkPortugues());

		} else {
			setLinkIngles(getLinkEspanol());
			setLinkPortugues(getLinkEspanol());

		}

	}

	public boolean isSolosp() {
		return solosp;
	}

	public void setSolosp(boolean solosp) {
		this.solosp = solosp;
	}

	public boolean isHabilitadoLinkAuditor() {
		return habilitadoLinkAuditor;
	}

	public void setHabilitadoLinkAuditor(boolean habilitadoLinkAuditor) {
		this.habilitadoLinkAuditor = habilitadoLinkAuditor;
	}

	public boolean isPadronSeteado() {
		return padronSeteado;
	}

	public void setPadronSeteado(boolean padronSeteado) {
		this.padronSeteado = padronSeteado;
	}

	public boolean isCandidatosSeteado() {
		return candidatosSeteado;
	}

	public void setCandidatosSeteado(boolean candidatosSeteado) {
		this.candidatosSeteado = candidatosSeteado;
	}

	public boolean isAuditoresSeteado() {
		return auditoresSeteado;
	}

	public void setAuditoresSeteado(boolean auditoresSeteado) {
		this.auditoresSeteado = auditoresSeteado;
	}

	public List<TemplateEleccion> getTemplatesEleccion() {
		return templatesEleccion;
	}

	public void setTemplatesEleccion(List<TemplateEleccion> templatesEleccion) {
		this.templatesEleccion = templatesEleccion;
	}

	public String getRemitentePorDefecto() {
		return remitentePorDefecto;
	}

	public String getLinkResultado() {
		return UtilsLinks.calcularLinkResultado(tokenResultado);
	}

	public void setRemitentePorDefecto(String remitentePorDefecto) {
		this.remitentePorDefecto = remitentePorDefecto;
	}

	public boolean isCandidatosAleatorios() {
		return candidatosAleatorios;
	}

	public void setCandidatosAleatorios(boolean candidatosAleatorios) {
		this.candidatosAleatorios = candidatosAleatorios;
	}

	public String getFechaInicioString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		return simpleDateFormat.format(new DateTime(getFechaInicio()).plusHours(getDiffUTC()).toDate()) + " (UTC)";
	}

	public String getFechaFinString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		return simpleDateFormat.format(new DateTime(getFechaFin()).plusHours(getDiffUTC()).toDate()) + " (UTC)";
	}

	public int getDiffUTC() {
		return diffUTC;
	}

	public void setDiffUTC(int diffUTC) {
		this.diffUTC = diffUTC;
	}

	public boolean isSolicitarRevision() {
		return solicitarRevision;
	}

	public void setSolicitarRevision(boolean solicitarRevision) {
		this.solicitarRevision = solicitarRevision;
	}

	public boolean isTermino() {
		return new Date().after(getFechaFin());
	}

	public boolean isComenzo() {
		return new Date().after(getFechaInicio());
	}

	public boolean isHabilitadaParaVotar() {
		return (isComenzo() && !isTermino() && isHabilitadoLinkVotacion());
	}

	public boolean isMigrada() {
		return migrada;
	}

	public void setMigrada(boolean migrada) {
		this.migrada = migrada;
	}

	public long getIdMigracion() {
		return idMigracion;
	}

	public void setIdMigracion(long idMigracion) {
		this.idMigracion = idMigracion;
	}

	public CategoriaEleccion getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaEleccion categoria) {
		this.categoria = categoria;
	}
}