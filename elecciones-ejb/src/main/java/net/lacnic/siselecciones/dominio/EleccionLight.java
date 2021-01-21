package net.lacnic.siselecciones.dominio;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;

import net.lacnic.siselecciones.utils.UtilsLinks;

@Entity
@Table(name = "eleccion")
public class EleccionLight implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@Column(name = "id_eleccion")
	private long idEleccion;

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


	public EleccionLight() { }


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

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
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
		if (displayName.contains("sp"))
			return getDescripcionEspanol();
		else if (displayName.contains("en"))
			return getDescripcionIngles();
		else if (displayName.contains("pt"))
			return getDescripcionPortugues();
		return getDescripcionEspanol();

	}

	public String getTitulo(String displayName) {
		if (displayName.contains("sp"))
			return getTituloEspanol();
		else if (displayName.contains("en"))
			return getTituloIngles();
		else if (displayName.contains("pt"))
			return getTituloPortugues();
		return getTituloEspanol();

	}

	public void copiarDescripcionesIdiomaAlResto(String idiomaACopiar) {

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

	public void copiarUrlsIdiomaAlResto(String idiomaACopiar) {

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
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return simpleDateFormat.format(new DateTime(getFechaInicio()).plusHours(getDiffUTC()).toDate()) + " (UTC)";
	}

	public String getFechaFinString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
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

	public CategoriaEleccion getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaEleccion categoria) {
		this.categoria = categoria;
	}
}