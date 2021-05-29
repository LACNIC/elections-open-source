package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.data.DetalleResultadoData;
import net.lacnic.elections.data.ResultadoEleccionesData;

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
    	
    	DetalleResultadoData detalleRes = new DetalleResultadoData(availableVoters,participantVoters,weigth);
    	
    	
    	
    	assertTrue(detalleRes != null);
    	
    	assertEquals(detalleRes.getPorcentaje() , percentage);
    	assertEquals(detalleRes.getPorcentajeConSimbolo(), percentage+" %");
    	assertEquals(detalleRes.getHabilitados() , availableVoters);
    	assertEquals(detalleRes.getParticipantes() , participantVoters);
    	assertEquals(detalleRes.getPeso() , Integer.valueOf(weigth));
    	assertEquals(detalleRes.getTotal(),  total);
    	assertEquals(detalleRes.getTotal(max), strTotal);
    	
    	   	
    	ResultadoEleccionesData resultado = new ResultadoEleccionesData(max);
    	
    	assertTrue(resultado != null);
    	
    	List<DetalleResultadoData> listDetalle = new ArrayList<>();
    	listDetalle.add(detalleRes);
    	
    	resultado.setDetalleResultadoData(listDetalle);
    	resultado.setParticipantesTotal(participantVoters);
    	resultado.setHabilitadosTotal(availableVoters);
    	resultado.setTotalTotal(total);
    	
    	
    	assertEquals(resultado.getMax() , max);
    	assertEquals(resultado.getHabilitadosTotal() , availableVoters);
    	assertEquals(resultado.getParticipantesTotal() , participantVoters);
    	assertEquals(resultado.getTotalTotal() , total);
    	assertEquals(resultado.getPorcentajeTotalConSimbolo(), percentage + " %");
    	assertEquals(resultado.getTotalTotalPosilidades(), strTotalPos);
    	
    	resultado.calcularTotales();
    	
    	assertEquals(resultado.getHabilitadosTotal() , availableVoters);
    	assertEquals(resultado.getParticipantesTotal() , participantVoters);
    	assertEquals(resultado.getTotalTotal() , total);
    	
    	
    	
    	
    	
    
    }
    
    
    

}
