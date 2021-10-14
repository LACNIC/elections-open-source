package net.lacnic;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.data.ElectionReport;
import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participation;


public class AditionalDataTest extends TestCase {
	
	public AditionalDataTest(String testName ) {
		super(testName);
	}
	
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AditionalDataTest.class );
    }
    
    @SuppressWarnings("deprecation")
	public void testData()
    {
    	//Participacion Data
    	String orgId, nombre, email, tituloEleccionSP, tituloEleccionEN, tituloEleccionPT, pais, categoria, linkEleccionSP, linkEleccionEN, linkEleccionPT, linkVotar;
    	Date fechaInicioEleccion = new Date(2020, 12, 12);
   	 	Date fechaFinEleccion = new Date(2020, 12, 15);
    	orgId = "Lacnic";
    	nombre = "Juan";
    	email = "mail";
    	tituloEleccionEN = "titEn";
    	tituloEleccionSP = "titSp";
    	tituloEleccionPT = "titPt";
    	pais = "URU";
    	categoria = "Cat";
    	linkEleccionEN = "linkEN";
    	linkEleccionSP = "linkSP";
    	linkEleccionPT = "linkPT";
    	linkVotar = "link";
    	 
    	 
    	
    	Participation participacion = new Participation();
    	
    	assertTrue(participacion != null); 
    	
    	participacion.setName(nombre);
    	participacion.setOrgId(orgId);
    	participacion.setCategory(categoria);
    	participacion.setEmail(email);
    	participacion.setElectionLinkEN(linkEleccionEN);
    	participacion.setElectionLinkPT(linkEleccionPT);
    	participacion.setElectionLinkSP(linkEleccionSP);
    	participacion.setVoteLink(linkVotar);
    	participacion.setCountry(pais);
    	participacion.setElectionTitleEN(tituloEleccionEN);
    	participacion.setElectionTitlePT(tituloEleccionPT);
    	participacion.setElectionTitleSP(tituloEleccionSP);
    	participacion.setElectionEndDate(fechaFinEleccion);
    	participacion.setElectionStartDate(fechaInicioEleccion);
    	participacion.setVoted(true);
    	
    	assertEquals(participacion.getCategory(), categoria);
    	assertEquals(participacion.getEmail(), email);
    	assertEquals(participacion.getElectionLinkEN(), linkEleccionEN);
    	assertEquals(participacion.getElectionLinkPT(), linkEleccionPT);
    	assertEquals(participacion.getElectionLinkSP(), linkEleccionSP);
    	assertEquals(participacion.getVoteLink(), linkVotar);
    	assertEquals(participacion.getName(), nombre);
    	assertEquals(participacion.getOrgId(), orgId);
    	assertEquals(participacion.getCountry(), pais);
    	assertEquals(participacion.getElectionTitleEN(), tituloEleccionEN);
    	assertEquals(participacion.getElectionTitlePT(), tituloEleccionPT);
    	assertEquals(participacion.getElectionTitleSP(),tituloEleccionSP);
    	assertEquals(participacion.getElectionEndDate(), fechaFinEleccion);
    	assertEquals(participacion.getElectionStartDate(), fechaInicioEleccion);
    	assertTrue(participacion.isVoted());
    	
    	
    	String nombreEleccion;
    	long usuariosVotaron, usuariosNoVotaron, usuariosTotales, correosPendientes;
    	nombreEleccion = "test";
    	usuariosVotaron = 10;
    	usuariosNoVotaron = 1;
    	usuariosTotales = 11;
    	correosPendientes = 5;
    	//eleccion reporte
    	ElectionReport eleccion = new ElectionReport(nombreEleccion, usuariosVotaron, usuariosNoVotaron, usuariosTotales, correosPendientes);
    	
    	assertTrue(eleccion != null);
    	
    	assertEquals(eleccion.getPendingMails(), correosPendientes);
    	assertEquals(eleccion.getElectionName(), nombreEleccion);
    	assertEquals(eleccion.getUsersNotVoted(), usuariosNoVotaron);
    	assertEquals(eleccion.getUsersVoted(), usuariosVotaron);
    	assertEquals(eleccion.getUsersTotal(), usuariosTotales);
    	
    	//Health check
    	int intentosDeEnvio;
    	long ipsAccesosFallidos, sumaAccesosFallidos, correosTotales, correosPendientes2, correosEnviados;
    	intentosDeEnvio = 1;
    	ipsAccesosFallidos = 5;
    	sumaAccesosFallidos = 5;
    	correosEnviados = 9;
    	correosPendientes2 = 1;
    	correosTotales = 10;
    	
    	List<ElectionReport> elecciones = new ArrayList<>();
    	elecciones.add(eleccion);
    	
    	HealthCheck healthChk = new HealthCheck(intentosDeEnvio, ipsAccesosFallidos, sumaAccesosFallidos, correosTotales, correosPendientes2, correosEnviados, elecciones);
    	
    	assertTrue(healthChk != null);
    	
    	assertEquals(healthChk.getMailsSent(), correosEnviados);
    	assertEquals(healthChk.getMailsPending(), correosPendientes2);
    	assertEquals(healthChk.getMailsTotal(), correosTotales);
    	assertEquals(healthChk.getSendAttempts(), intentosDeEnvio);
    	assertEquals(healthChk.getFailedAccessIps(), ipsAccesosFallidos);
    	assertEquals(healthChk.getFailedAccessSum(), sumaAccesosFallidos);
    	
    			
    }

}
