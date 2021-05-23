package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import net.lacnic.elections.utils.UtilsLinks;

@Entity
public class UsuarioPadron implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_padron_seq")
	@SequenceGenerator(name = "usuario_padron_seq", sequenceName = "usuario_padron_seq", allocationSize = 1)
	@Column(name = "id_usuario_padron")
	private long idUsuarioPadron;

	@Column(nullable = true)
	private Long  idMigracion;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_eleccion")
	private Eleccion eleccion;

	@Column(nullable = false)
	private boolean yaVoto;

	@Column(nullable = true, length = 1000)
	private String tokenVotacion;

	@Column(nullable = false)
	private Integer cantVotos;

	@Column(nullable = false, length = 1000)
	private String nombre;

	@Column(nullable = false)
	private String mail;

	@Column(nullable = true)
	private String pais;

	@Column(nullable = false)
	private String idioma;

	@Column(nullable = true)
	private String orgID;

	@Column(nullable = true)
	private Date fechaVoto;

	@OneToMany(mappedBy = "usuarioPadron", cascade = CascadeType.REMOVE)
	private List<Voto> votos;

	@Transient
	private String resumenCodigos;


	public UsuarioPadron() { }


	public long getIdUsuarioPadron() {
		return idUsuarioPadron;
	}

	public void setIdUsuarioPadron(long idUsuarioPadron) {
		this.idUsuarioPadron = idUsuarioPadron;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public String getIdioma() {
		return idioma;
	}

	public void setIdioma(String idioma) {
		this.idioma = idioma;
	}

	public String getOrgID() {
		if (orgID != null)
			this.orgID = orgID.toUpperCase();
		return orgID;
	}

	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}

	public boolean isYaVoto() {
		return yaVoto;
	}

	public void setYaVoto(boolean yaVoto) {
		this.yaVoto = yaVoto;
	}

	public String getTokenVotacion() {
		return tokenVotacion;
	}

	public void setTokenVotacion(String tokenVotacion) {
		this.tokenVotacion = tokenVotacion;
	}

	public Integer getCantVotos() {
		return cantVotos;
	}

	public void setCantVotos(Integer cantVotos) {
		this.cantVotos = cantVotos;
	}

	public Eleccion getEleccion() {
		return eleccion;
	}

	public void setEleccion(Eleccion eleccion) {
		this.eleccion = eleccion;
	}

	public String getInformacionDelVotante() {
		return getNombre().concat((getOrgID() != null && !getOrgID().isEmpty()) ? " - " + getOrgID() : "");
	}

	public String getInformacionDelVotanteCompleta() {
		String email= (getMail() != null && !getMail().isEmpty()) ? " (" + getMail() +") ": "";
		String orgid= (getOrgID() != null && !getOrgID().isEmpty()) ? " - " + getOrgID() : "";
		String pais2= (getPais() != null && !getPais().isEmpty()) ? " - " + getPais() : "";
		return getNombre().concat(email+orgid+pais2);
	}

	public String getResumenCodigos() {
		return resumenCodigos;
	}

	public void setResumenCodigos(String resumenCodigos) {
		this.resumenCodigos = resumenCodigos;
	}

	public void setResumenCodigos(List<Voto> votos) {
		String aux = "";
		for (Voto v : votos) {
			aux = aux.concat(v.getCodigo() + " / " + v.getCandidato().getNombre() + "\n");
		}
		this.resumenCodigos = aux;
	}

	public String getLinkVotar() {
		return UtilsLinks.calcularLinkVotar(tokenVotacion);
	}

	public Date getFechaVoto() {
		return fechaVoto;
	}

	public void setFechaVoto(Date fechaVoto) {
		this.fechaVoto = fechaVoto;
	}

	public long getIdMigracion() {
		return idMigracion;
	}

	public void setIdMigracion(long idMigracion) {
		this.idMigracion = idMigracion;
	}
}