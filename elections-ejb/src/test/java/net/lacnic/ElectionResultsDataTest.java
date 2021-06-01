package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.data.ResultDetailData;
import net.lacnic.elections.data.ElectionsResultsData;

import java.util.ArrayList;
import java.util.List;

public class ElectionResultsDataTest extends TestCase {
	
	public ElectionResultsDataTest(String testName ) {
		super(testName);
	}
	
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ElectionResultsDataTest.class );
    }
    
    public void testResultadoEleccionesData()
    {
    	
    	Long availableVoters, participantVoters, percentage, total;
    	int weigth, max;
    	String strTotal, strTotalPos;
    	availableVoters = (long) 1000;
    	participantVoters = (long) 500;
    	weigth = 5;
    	max = 2;
    	
    	
    	
    	percentage = (participantVoters * 100L) / availableVoters;
    	total = (participantVoters) * (weigth);
    	strTotal ="( " + total + " - " + (total * max) + ") " ;
    	strTotalPos = "( " + total + " - " + (total *max) + ") ";
    	
    	ResultDetailData detalleRes = new ResultDetailData(availableVoters,participantVoters,weigth);
    	
    	
    	
    	assertTrue(detalleRes != null);
    	
    	assertEquals(detalleRes.getPercentage() , percentage);
    	assertEquals(detalleRes.getPercentageWithSymbol(), percentage+" %");
    	assertEquals(detalleRes.getEnabled() , availableVoters);
    	assertEquals(detalleRes.getParticipants() , participantVoters);
    	assertEquals(detalleRes.getWeight() , Integer.valueOf(weigth));
    	assertEquals(detalleRes.getTotal(),  total);
    	assertEquals(detalleRes.getTotal(max), strTotal);
    	
    	   	
    	ElectionsResultsData resultado = new ElectionsResultsData(max);
    	
    	assertTrue(resultado != null);
    	
    	List<ResultDetailData> listDetalle = new ArrayList<>();
    	listDetalle.add(detalleRes);
    	
    	resultado.setResultDetailData(listDetalle);
    	resultado.setTotalParticipants(participantVoters);
    	resultado.setTotalEnabled(availableVoters);
    	resultado.setTotalTotal(total);
    	
    	
    	assertEquals(resultado.getMax() , max);
    	assertEquals(resultado.getTotalEnabled() , availableVoters);
    	assertEquals(resultado.getTotalParticipants() , participantVoters);
    	assertEquals(resultado.getTotalTotal() , total);
    	assertEquals(resultado.getTotalPercentageWithSymbol(), percentage + " %");
    	assertEquals(resultado.getTotalTotalPossible(), strTotalPos);
    	
    	resultado.calculateTotals();
    	
    	assertEquals(resultado.getTotalEnabled() , availableVoters);
    	assertEquals(resultado.getTotalParticipants() , participantVoters);
    	assertEquals(resultado.getTotalTotal() , total);
    	
    	
    	
    	
    	
    
    }
    
    
    

}
