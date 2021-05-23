package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class Voto implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "voto_seq")
	@SequenceGenerator(name = "voto_seq", sequenceName = "voto_seq", allocationSize = 1)
	private long idVoto;

	@Column(nullable = false)
	private String codigo;

	@Column(nullable = true)
	private String ip;

	@Column(nullable = true)
	private Date fechaVoto;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_candidato")
	private Candidato candidato;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_eleccion")
	private Eleccion eleccion;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_usuario_padron")
	private UsuarioPadron usuarioPadron;


	public Voto() { }


	public UsuarioPadron getUsuarioPadron() {
		return usuarioPadron;
	}

	public void setUsuarioPadron(UsuarioPadron usuarioPadron) {
		this.usuarioPadron = usuarioPadron;
	}

	public long getIdVoto() {
		return idVoto;
	}

	public void setIdVoto(long idVoto) {
		this.idVoto = idVoto;
	}

	public Eleccion getEleccion() {
		return eleccion;
	}

	public void setEleccion(Eleccion eleccion) {
		this.eleccion = eleccion;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Candidato getCandidato() {
		return candidato;
	}

	public void setCandidato(Candidato candidato) {
		this.candidato = candidato;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getFechaVoto() {
		return fechaVoto;
	}

	public void setFechaVoto(Date fechaVoto) {
		this.fechaVoto = fechaVoto;
	}

}
