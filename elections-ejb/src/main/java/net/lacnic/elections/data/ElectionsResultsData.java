package net.lacnic.elections.data;

import java.io.Serializable;
import java.util.List;


public class ElectionsResultsData implements Serializable {

	private static final long serialVersionUID = 8383150789880349391L;

	private List<ResultDetailData> resultDetailData;
	private Long totalEnabled;
	private Long totalParticipants;
	private Long totalTotal;
	private int max;


	public ElectionsResultsData() { }

	public ElectionsResultsData(int max) {
		this.max = max;
	}


	public String getTotalPercentageWithSymbol() {
		long totalPercentage = totalParticipants * 100;
		totalPercentage = totalPercentage / totalEnabled;
		return "".concat(totalPercentage + " %");
	}

	public String getTotalTotalPossible() {
		return "( " + totalTotal + " - " + (totalTotal * getMax()) + ") ";
	}
	
	public void calculateTotals() {
		Long totalTotalAux = 0L;
		Long totalEnabledAux = 0L;
		Long totalParticipantsAux = 0L;

		for (ResultDetailData detail : getResultDetailData()) {			
			
			totalEnabledAux = totalEnabledAux + detail.getEnabled();
			totalParticipantsAux = totalParticipantsAux + detail.getParticipants();
			totalTotalAux = totalTotalAux + detail.getTotal();
		}
		setTotalTotal(totalTotalAux);
		setTotalEnabled(totalEnabledAux);
		setTotalParticipants(totalParticipantsAux);
	}


	public List<ResultDetailData> getResultDetailData() {
		return resultDetailData;
	}

	public void setResultDetailData(List<ResultDetailData> resultDetailData) {
		this.resultDetailData = resultDetailData;
	}

	public Long getTotalEnabled() {
		return totalEnabled;
	}

	public void setTotalEnabled(Long totalEnabled) {
		this.totalEnabled = totalEnabled;
	}

	public Long getTotalParticipants() {
		return totalParticipants;
	}

	public void setTotalParticipants(Long totalParticipants) {
		this.totalParticipants = totalParticipants;
	}

	public Long getTotalTotal() {
		return totalTotal;
	}

	public void setTotalTotal(Long totalTotal) {
		this.totalTotal = totalTotal;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

}
