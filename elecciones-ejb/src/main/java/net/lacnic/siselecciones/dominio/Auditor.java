package net.lacnic.siselecciones.dominio;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import net.lacnic.siselecciones.utils.StringUtils;
import net.lacnic.siselecciones.utils.UtilsLinks;

@Entity
public class Auditor implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auditor_seq")
	@SequenceGenerator(name = "auditor_seq", sequenceName = "auditor_seq", allocationSize = 1)
	@Column(name = "id_auditor")
	private long idAuditor;

	@Column(nullable = true)
	private Long idMigracion;

	@Column
	private boolean comisionado;

	@Column
	private boolean expresoConformidad;

	@Column
	private boolean habilitaRevision;

	@Column(nullable = true, length = 1000)
	private String tokenResultado;

	@Column(nullable = false)
	private String nombre;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_eleccion")
	private Eleccion eleccion;

	@Column(nullable = false)
	private String mail;

	public Auditor() {
		this.comisionado = false;
		this.expresoConformidad = false;
		this.habilitaRevision = false;
		this.tokenResultado = StringUtils.createSecureToken();
		this.idMigracion = 0L;

	}

	public Auditor(Eleccion eleccion, Comisionado comisionado) {
		this.comisionado = true;
		this.expresoConformidad = false;
		this.tokenResultado = StringUtils.createSecureToken();
		this.habilitaRevision = false;
		this.nombre = comisionado.getNombre();
		this.eleccion = eleccion;
		this.mail = comisionado.getMail();
		this.idMigracion = 0L;
	}

	public long getIdAuditor() {
		return idAuditor;
	}

	public void setIdAuditor(long idAuditor) {
		this.idAuditor = idAuditor;
	}

	public String getTokenResultado() {
		return tokenResultado;
	}

	public void setTokenResultado(String tokenResultado) {
		this.tokenResultado = tokenResultado;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Eleccion getEleccion() {
		return eleccion;
	}

	public void setEleccion(Eleccion eleccion) {
		this.eleccion = eleccion;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public boolean isComisionado() {
		return comisionado;
	}

	public void setComisionado(boolean comisionado) {
		this.comisionado = comisionado;
	}

	public boolean isExpresoConformidad() {
		return expresoConformidad;
	}

	public void setExpresoConformidad(boolean expresoConformidad) {
		this.expresoConformidad = expresoConformidad;
	}

	public void clean() {
		this.nombre = null;
		this.mail = null;
		this.comisionado = false;

	}

	public String getLinkResultado() {
		return UtilsLinks.calcularLinkResultadoAuditor(tokenResultado);
	}

	public boolean isHabilitaRevision() {
		return habilitaRevision;
	}

	public void setHabilitaRevision(boolean habilitaRevision) {
		this.habilitaRevision = habilitaRevision;
	}

	public long getIdMigracion() {
		return idMigracion;
	}

	public void setIdMigracion(long idMigracion) {
		this.idMigracion = idMigracion;
	}
}
