package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

import net.lacnic.siselecciones.data.ResultadoEleccionesData;
import net.lacnic.siselecciones.data.DetalleResultadoData;

public class ResultadoEleccionesDataTest extends TestCase {
	
	public ResultadoEleccionesDataTest(String testName ) {
		super(testName);
	}
	
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ResultadoEleccionesDataTest.class );
    }
    
    public void testResultadoEleccionesData()
    {
    	
    	Long habilitados, participantes, porcentaje, total;
    	int peso, max;
    	String strTotal, strTotalPos;
    	habilitados = (long) 1000;
    	participantes = (long) 500;
    	peso = 5;
    	max = 2;
    	
    	
    	
    	porcentaje = (participantes * 100L) / habilitados;
    	total = (participantes) * (peso);
    	strTotal ="( " + total + " - " + (total * max) + ") " ;
    	strTotalPos = "( " + total + " - " + (total *max) + ") ";
    	
    	DetalleResultadoData detalleRes = new DetalleResultadoData(habilitados,participantes,peso);
    	
    	
    	
    	assertTrue(detalleRes != null);
    	
    	assertEquals(detalleRes.getPorcentaje() , porcentaje);
    	assertEquals(detalleRes.getPorcentajeConSimbolo(), porcentaje+" %");
    	assertEquals(detalleRes.getHabilitados() , habilitados);
    	assertEquals(detalleRes.getParticipantes() , participantes);
    	assertEquals(detalleRes.getPeso() , Integer.valueOf(peso));
    	assertEquals(detalleRes.getTotal(),  total);
    	assertEquals(detalleRes.getTotal(max), strTotal);
    	
    	   	
    	ResultadoEleccionesData resultado = new ResultadoEleccionesData(max);
    	
    	assertTrue(resultado != null);
    	
    	List<DetalleResultadoData> listDetalle = new ArrayList<>();
    	listDetalle.add(detalleRes);
    	
    	resultado.setDetalleResultadoData(listDetalle);
    	resultado.setParticipantesTotal(participantes);
    	resultado.setHabilitadosTotal(habilitados);
    	resultado.setTotalTotal(total);
    	
    	
    	assertEquals(resultado.getMax() , max);
    	assertEquals(resultado.getHabilitadosTotal() , habilitados);
    	assertEquals(resultado.getParticipantesTotal() , participantes);
    	assertEquals(resultado.getTotalTotal() , total);
    	assertEquals(resultado.getPorcentajeTotalConSimbolo(), porcentaje + " %");
    	assertEquals(resultado.getTotalTotalPosilidades(), strTotalPos);
    	
    	resultado.calcularTotales();
    	
    	assertEquals(resultado.getHabilitadosTotal() , habilitados);
    	assertEquals(resultado.getParticipantesTotal() , participantes);
    	assertEquals(resultado.getTotalTotal() , total);
    	
    	
    	
    	
    	
    
    }
    
    
    

}
