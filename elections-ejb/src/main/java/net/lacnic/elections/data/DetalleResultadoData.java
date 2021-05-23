package net.lacnic.elections.data;

import java.io.Serializable;
/**
 * Clase utilizada para mostrar los resultados de la eleccion: porcentaje, habilitados, participantes, pesos y total de votos
 * @author Antonymous
 *
 */
public class DetalleResultadoData implements Serializable {

	private static final long serialVersionUID = -2581039785970380057L;

	private Long porcentaje;
	private Long habilitados;
	private Long participantes;
	private Integer peso;
	private Long total;

	public DetalleResultadoData(Long habilitados, Long participantes, Integer peso) {
		this.habilitados = habilitados;
		this.participantes = participantes;
		this.peso = peso;
		this.porcentaje = (participantes * 100L) / habilitados;
		this.total = (participantes) * (peso);
	}

	public DetalleResultadoData() {
	}

	public Long getPorcentaje() {
		return porcentaje;
	}

	public String getPorcentajeConSimbolo() {
		return "".concat(porcentaje + " %");
	}

	public void setPorcentaje(Long porcentaje) {
		this.porcentaje = porcentaje;
	}

	public Long getHabilitados() {
		return habilitados;
	}

	public void setHabilitados(Long habilitados) {
		this.habilitados = habilitados;
	}

	public Long getParticipantes() {
		return participantes;
	}

	public void setParticipantes(Long participantes) {
		this.participantes = participantes;
	}

	public Integer getPeso() {
		return peso;
	}

	public void setPeso(Integer peso) {
		this.peso = peso;
	}

	public Long getTotal() {
		return total;
	}

	public String getTotal(int max) {
		
		return "( " + total + " - " + (total * max) + ") ";
	}

	public void setTotal(Long total) {
		this.total = total;
	}

}
