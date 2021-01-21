package net.lacnic.siselecciones.dominio;

import java.io.Serializable;
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

import net.lacnic.siselecciones.utils.Constantes;

@Entity
public class Candidato implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidato_seq")
	@SequenceGenerator(name = "candidato_seq", sequenceName = "candidato_seq", allocationSize = 1)
	@Column(name = "id_candidato")
	private long idCandidato;

	@Column(nullable = true)
	private Long idMigracion;

	@Column(nullable = false, length = 1000)
	private String nombre;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_eleccion")
	private Eleccion eleccion;

	@Column(nullable = false)
	private byte[] contenidoFoto;

	@Column(nullable = false)
	private String nombreFoto;

	@Column(columnDefinition = "TEXT")
	private String bioEspanol;

	@Column(columnDefinition = "TEXT")
	private String bioIngles;

	@Column(columnDefinition = "TEXT")
	private String bioPortugues;

	@Column(nullable = false)
	private String extensionFoto;

	@Column
	private int orden;

	@Column(nullable = true)
	private boolean solosp;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String linkEspanol;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String linkIngles;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String linkPortugues;

	@OneToMany(mappedBy = "candidato", cascade = CascadeType.REMOVE)
	private List<Voto> votos;

	public Candidato() { }

	public long getIdCandidato() {
		return idCandidato;
	}

	public void setIdCandidato(long idCandidato) {
		this.idCandidato = idCandidato;
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

	public byte[] getContenidoFoto() {
		return contenidoFoto;
	}

	public void setContenidoFoto(byte[] contenidoFoto) {
		this.contenidoFoto = contenidoFoto;
	}

	public String getNombreFoto() {
		return nombreFoto;
	}

	public void setNombreFoto(String nombreFoto) {
		this.nombreFoto = nombreFoto;
	}

	public String getExtensionFoto() {
		return extensionFoto;
	}

	public void setExtensionFoto(String extensionFoto) {
		this.extensionFoto = extensionFoto;
	}

	public List<Voto> getVotos() {
		return votos;
	}

	public void setVotos(List<Voto> votos) {
		this.votos = votos;
	}

	public String getBio(String displayName) {
		if (displayName.toLowerCase().contains("en") || displayName.toLowerCase().contains("english"))
			return getBioIngles();
		else if (displayName.toLowerCase().contains("pt") || displayName.toLowerCase().contains("portuguese"))
			return getBioPortugues();
		return getBioEspanol();
	}

	public String getLink(String displayName) {
		if (displayName.toLowerCase().contains("en") || displayName.toLowerCase().contains("english"))
			return getLinkIngles();
		else if (displayName.toLowerCase().contains("pt") || displayName.toLowerCase().contains("portuguese"))
			return getLinkPortugues();
		return getLinkEspanol();
	}

	public String getBioEspanol() {
		return bioEspanol;
	}

	public void setBioEspanol(String bioEspanol) {
		this.bioEspanol = bioEspanol;
	}

	public String getBioIngles() {
		return bioIngles;
	}

	public void setBioIngles(String bioIngles) {
		this.bioIngles = bioIngles;
	}

	public String getBioPortugues() {
		return bioPortugues;
	}

	public void setBioPortugues(String bioPortugues) {
		this.bioPortugues = bioPortugues;
	}

	public void copiarBiosIdiomaAlResto() {

		setBioIngles(getBioEspanol());
		setBioPortugues(getBioEspanol());

		setLinkIngles(getLinkEspanol());
		setLinkPortugues(getLinkEspanol());

	}

	public void clean() {
		this.nombre = null;
		this.contenidoFoto = null;
		this.nombreFoto = null;
		this.bioEspanol = null;
		this.bioIngles = null;
		this.bioPortugues = null;
		this.extensionFoto = null;

	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}

	public boolean isSolosp() {
		return solosp;
	}

	public void setSolosp(boolean solosp) {
		this.solosp = solosp;
	}

	public boolean isFijo() {
		return getOrden() == Constantes.ORDEN_MINIMO || getOrden() == Constantes.ORDEN_MAXIMO;
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

	public long getIdMigracion() {
		return idMigracion;
	}

	public void setIdMigracion(long idMigracion) {
		this.idMigracion = idMigracion;
	}
}