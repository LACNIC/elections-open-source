package net.lacnic.elections.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

@Entity
public class TemplateEleccion implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "templateeleccion_seq")
	@SequenceGenerator(name = "templateeleccion_seq", sequenceName = "templateeleccion_seq", allocationSize = 1, initialValue = 500)
	@Column(name = "id_template_eleccion")
	private long idTemplate;

	@ManyToOne(optional = true)
	@JoinColumn(name = "id_eleccion", nullable = true)
	private Eleccion eleccion;

	@Column(name = "tipo")
	private String tipoTemplate;

	@Column(columnDefinition = "TEXT")
	private String asuntoES;

	@Column(columnDefinition = "TEXT")
	private String asuntoPT;

	@Column(columnDefinition = "TEXT")
	private String asuntoEN;

	@Column(columnDefinition = "TEXT")
	private String cuerpoES;

	@Column(columnDefinition = "TEXT")
	private String cuerpoEN;

	@Column(columnDefinition = "TEXT")
	private String cuerpoPT;

	@Transient
	private TipoDestinatario tipoDestinatario;

	public TemplateEleccion() {

	}

	public TemplateEleccion(Eleccion e, TemplateEleccion t) {
		this.asuntoEN = t.getAsuntoEN();
		this.asuntoES = t.getAsuntoES();
		this.asuntoPT = t.getAsuntoPT();
		this.cuerpoEN = t.getCuerpoEN();
		this.cuerpoES = t.getCuerpoES();
		this.cuerpoPT = t.getCuerpoPT();
		this.tipoTemplate = t.getTipoTemplate();
		this.eleccion = e;
	}

	public long getIdTemplate() {
		return idTemplate;
	}

	public void setIdTemplate(long idTemplate) {
		this.idTemplate = idTemplate;
	}

	public Eleccion getEleccion() {
		return eleccion;
	}

	public void setEleccion(Eleccion eleccion) {
		this.eleccion = eleccion;
	}

	public String getTipoTemplate() {
		return tipoTemplate;
	}

	public void setTipoTemplate(String tipoTemplate) {
		this.tipoTemplate = tipoTemplate.toUpperCase();
	}

	public String getAsuntoES() {
		return asuntoES;
	}

	public void setAsuntoES(String asuntoES) {
		this.asuntoES = asuntoES;
	}

	public String getAsuntoPT() {
		return asuntoPT;
	}

	public void setAsuntoPT(String asuntoPT) {
		this.asuntoPT = asuntoPT;
	}

	public String getAsuntoEN() {
		return asuntoEN;
	}

	public void setAsuntoEN(String asuntoEN) {
		this.asuntoEN = asuntoEN;
	}

	public String getCuerpoES() {
		return cuerpoES;
	}

	public void setCuerpoES(String cuerpoES) {
		this.cuerpoES = cuerpoES;
	}

	public String getCuerpoEN() {
		return cuerpoEN;
	}

	public void setCuerpoEN(String cuerpoEN) {
		this.cuerpoEN = cuerpoEN;
	}

	public String getCuerpoPT() {
		return cuerpoPT;
	}

	public void setCuerpoPT(String cuerpoPT) {
		this.cuerpoPT = cuerpoPT;
	}

	public TipoDestinatario getTipoDestinatario() {
		return tipoDestinatario;
	}

	public void setTipoDestinatario(TipoDestinatario tipoDestinatario) {
		this.tipoDestinatario = tipoDestinatario;
	}

}