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
public class Email implements Serializable {

	private static final long serialVersionUID = -6954869970189933966L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emaile_seq")
	@SequenceGenerator(name = "emaile_seq", sequenceName = "emaile_seq", allocationSize = 1)
	private Long id;

	@Column(columnDefinition = "TEXT")
	private String destinatarios;

	@Column
	private String desde;

	@Column
	private String cc;

	@Column
	private String bcc;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String asunto;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String cuerpo;

	@Column
	private Boolean enviado = false;

	@Column
	private Date fechaCreado;

	@Column(nullable = false)
	private String tipoTemplate;

	@ManyToOne(optional = true)
	@JoinColumn(name = "id_eleccion")
	private Eleccion eleccion;

	public Email() {
		this.fechaCreado = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDestinatarios() {
		return destinatarios;
	}

	public void setDestinatarios(String destinatarios) {
		this.destinatarios = destinatarios;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}

	public String getCuerpo() {
		return cuerpo;
	}

	public void setCuerpo(String cuerpo) {
		this.cuerpo = cuerpo;
	}

	public Boolean getEnviado() {
		return enviado;
	}

	public void setEnviado(Boolean enviado) {
		this.enviado = enviado;
	}

	public String getDesde() {
		return desde;
	}

	public void setDesde(String desde) {
		this.desde = desde;
	}

	public Eleccion getEleccion() {
		return eleccion;
	}

	public void setEleccion(Eleccion eleccion) {
		this.eleccion = eleccion;
	}

	public String getTipoMail() {
		return tipoTemplate;
	}

	public void setTipoTemplate(String tipo) {
		this.tipoTemplate = tipo;
	}

	public Date getFechaCreado() {
		return fechaCreado;
	}

	public void setFechaCreado(Date fechaCreado) {
		this.fechaCreado = fechaCreado;
	}

}
