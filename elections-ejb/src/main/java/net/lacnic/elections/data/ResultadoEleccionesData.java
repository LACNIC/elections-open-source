package net.lacnic.elections.data;

import java.io.Serializable;
import java.util.List;

public class ResultadoEleccionesData implements Serializable {

	private static final long serialVersionUID = 8383150789880349391L;

	private List<DetalleResultadoData> detalleResultadoData;

	private Long habilitadosTotal;
	private Long participantesTotal;
	private Long totalTotal;
	private int max;

	public ResultadoEleccionesData() {
	}

	public ResultadoEleccionesData(int max) {
		this.max = max;
	}

	public List<DetalleResultadoData> getDetalleResultadoData() {
		return detalleResultadoData;
	}

	public void setDetalleResultadoData(List<DetalleResultadoData> detalleResultadoData) {
		this.detalleResultadoData = detalleResultadoData;
	}

	public String getPorcentajeTotalConSimbolo() {
		long porcentajeTotal = participantesTotal * 100;
		porcentajeTotal = porcentajeTotal / habilitadosTotal;
		return "".concat(porcentajeTotal + " %");
	}

	public Long getHabilitadosTotal() {
		return habilitadosTotal;
	}

	public void setHabilitadosTotal(Long habilitadosTotal) {
		this.habilitadosTotal = habilitadosTotal;
	}

	public Long getParticipantesTotal() {
		return participantesTotal;
	}

	public void setParticipantesTotal(Long participantesTotal) {
		this.participantesTotal = participantesTotal;
	}

	public Long getTotalTotal() {
		return totalTotal;
	}

	public String getTotalTotalPosilidades() {		
		return "( " + totalTotal + " - " + (totalTotal * getMax()) + ") ";
	}

	public void setTotalTotal(Long totalTotal) {
		this.totalTotal = totalTotal;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void calcularTotales() {
		Long totalTotalAux = 0L;
		Long habilitadosTotalAux = 0L;
		Long participantesTotalAux = 0L;

		for (DetalleResultadoData detalle : getDetalleResultadoData()) {			
			
			habilitadosTotalAux = habilitadosTotalAux + detalle.getHabilitados();
			participantesTotalAux = participantesTotalAux + detalle.getParticipantes();
			totalTotalAux = totalTotalAux + detalle.getTotal();
		}
		setTotalTotal(totalTotalAux);
		setHabilitadosTotal(habilitadosTotalAux);
		setParticipantesTotal(participantesTotalAux);
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}
}
