package net.lacnic.elections.data;

import java.io.Serializable;


/**
 * Clase utilizada para mostrar los resultados de la eleccion: porcentaje, habilitados, participantes, pesos y total de votos
 * @author Antonymous
 *
 */
public class ResultDetailData implements Serializable {

	private static final long serialVersionUID = -2581039785970380057L;

	private Long percentage;
	private Long enabled;
	private Long participants;
	private Integer weight;
	private Long total;


	public ResultDetailData() { }

	public ResultDetailData(Long enabled, Long participants, Integer weight) {
		this.enabled = enabled;
		this.participants = participants;
		this.weight = weight;
		this.percentage = (participants * 100L) / enabled;
		this.total = (participants) * (weight);
	}


	public String getPercentageWithSymbol() {
		return "".concat(percentage + " %");
	}

	public String getTotal(int max) {
		return "( " + total + " - " + (total * max) + ") ";
	}


	public Long getPercentage() {
		return percentage;
	}

	public void setPercentage(Long percentage) {
		this.percentage = percentage;
	}

	public Long getEnabled() {
		return enabled;
	}

	public void setEnabled(Long enabled) {
		this.enabled = enabled;
	}

	public Long getParticipants() {
		return participants;
	}

	public void setParticipants(Long participants) {
		this.participants = participants;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

}
