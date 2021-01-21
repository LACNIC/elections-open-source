package net.lacnic;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.lacnic.siselecciones.data.Participacion;
import net.lacnic.siselecciones.data.EleccionReporte;
import net.lacnic.siselecciones.data.HealthCheck;


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
    	 
    	 
    	
    	Participacion participacion = new Participacion();
    	
    	assertTrue(participacion != null); 
    	
    	participacion.setNombre(nombre);
    	participacion.setOrgId(orgId);
    	participacion.setCategoria(categoria);
    	participacion.setEmail(email);
    	participacion.setLinkEleccionEN(linkEleccionEN);
    	participacion.setLinkEleccionPT(linkEleccionPT);
    	participacion.setLinkEleccionSP(linkEleccionSP);
    	participacion.setLinkVotar(linkVotar);
    	participacion.setPais(pais);
    	participacion.setTituloEleccionEN(tituloEleccionEN);
    	participacion.setTituloEleccionPT(tituloEleccionPT);
    	participacion.setTituloEleccionSP(tituloEleccionSP);
    	participacion.setFechaFinEleccion(fechaFinEleccion);
    	participacion.setFechaInicioEleccion(fechaInicioEleccion);
    	participacion.setYaVoto(true);
    	
    	assertEquals(participacion.getCategoria(), categoria);
    	assertEquals(participacion.getEmail(), email);
    	assertEquals(participacion.getLinkEleccionEN(), linkEleccionEN);
    	assertEquals(participacion.getLinkEleccionPT(), linkEleccionPT);
    	assertEquals(participacion.getLinkEleccionSP(), linkEleccionSP);
    	assertEquals(participacion.getLinkVotar(), linkVotar);
    	assertEquals(participacion.getNombre(), nombre);
    	assertEquals(participacion.getOrgId(), orgId);
    	assertEquals(participacion.getPais(), pais);
    	assertEquals(participacion.getTituloEleccionEN(), tituloEleccionEN);
    	assertEquals(participacion.getTituloEleccionPT(), tituloEleccionPT);
    	assertEquals(participacion.getTituloEleccionSP(),tituloEleccionSP);
    	assertEquals(participacion.getFechaFinEleccion(), fechaFinEleccion);
    	assertEquals(participacion.getFechaInicioEleccion(), fechaInicioEleccion);
    	assertTrue(participacion.isYaVoto());
    	
    	
    	String nombreEleccion;
    	long usuariosVotaron, usuariosNoVotaron, usuariosTotales, correosPendientes;
    	nombreEleccion = "test";
    	usuariosVotaron = 10;
    	usuariosNoVotaron = 1;
    	usuariosTotales = 11;
    	correosPendientes = 5;
    	//eleccion reporte
    	EleccionReporte eleccion = new EleccionReporte(nombreEleccion, usuariosVotaron, usuariosNoVotaron, usuariosTotales, correosPendientes);
    	
    	assertTrue(eleccion != null);
    	
    	assertEquals(eleccion.getCorreosPendientes(), correosPendientes);
    	assertEquals(eleccion.getNombreEleccion(), nombreEleccion);
    	assertEquals(eleccion.getUsuariosNoVotaron(), usuariosNoVotaron);
    	assertEquals(eleccion.getUsuariosVotaron(), usuariosVotaron);
    	assertEquals(eleccion.getUsuariosTotales(), usuariosTotales);
    	
    	//Health check
    	int intentosDeEnvio;
    	long ipsAccesosFallidos, sumaAccesosFallidos, correosTotales, correosPendientes2, correosEnviados;
    	intentosDeEnvio = 1;
    	ipsAccesosFallidos = 5;
    	sumaAccesosFallidos = 5;
    	correosEnviados = 9;
    	correosPendientes2 = 1;
    	correosTotales = 10;
    	
    	List<EleccionReporte> elecciones = new ArrayList<>();
    	elecciones.add(eleccion);
    	
    	HealthCheck healthChk = new HealthCheck(intentosDeEnvio, ipsAccesosFallidos, sumaAccesosFallidos, correosTotales, correosPendientes2, correosEnviados, elecciones);
    	
    	assertTrue(healthChk != null);
    	
    	assertEquals(healthChk.getCorreosEnviados(), correosEnviados);
    	assertEquals(healthChk.getCorreosPendientes(), correosPendientes2);
    	assertEquals(healthChk.getCorreosTotales(), correosTotales);
    	assertEquals(healthChk.getIntentosDeEnvio(), intentosDeEnvio);
    	assertEquals(healthChk.getIpsAccesosFallidos(), ipsAccesosFallidos);
    	assertEquals(healthChk.getSumaAccesosFallidos(), sumaAccesosFallidos);
    	
    			
    }

}
