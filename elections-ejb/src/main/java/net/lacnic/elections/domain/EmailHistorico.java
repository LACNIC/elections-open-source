package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class EmailHistorico implements Serializable {

	private static final long serialVersionUID = -6954869970189933966L;

	@Id
	private Long id;

	@Column
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

	@Column
	private String tipoTemplate;

	@Column
	private long idEleccion;

	public EmailHistorico() {
	}

	public EmailHistorico(Email e) {
		this.id = e.getId();
		this.destinatarios = e.getDestinatarios();
		this.desde = e.getDesde();
		this.cc = e.getCc();
		this.bcc = e.getBcc();
		this.asunto = e.getAsunto();
		this.cuerpo = e.getCuerpo();
		this.enviado = e.getEnviado();
		this.fechaCreado = e.getFechaCreado();
		this.tipoTemplate = e.getTipoMail();
		this.idEleccion = e.getEleccion().getIdEleccion();
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

	public Date getFechaCreado() {
		return fechaCreado;
	}

	public void setFechaCreado(Date fechaCreado) {
		this.fechaCreado = fechaCreado;
	}

	public long getIdEleccion() {
		return idEleccion;
	}

	public void setIdEleccion(long idEleccion) {
		this.idEleccion = idEleccion;
	}

	public String getTipoTemplate() {
		return tipoTemplate;
	}

	public void setTipoTemplate(String tipoTemplate) {
		this.tipoTemplate = tipoTemplate;
	}

}
