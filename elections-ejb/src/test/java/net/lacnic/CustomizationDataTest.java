package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.lacnic.elections.dao.CustomizationDao;
import net.lacnic.elections.domain.Customization;


public class CustomizationDataTest extends TestCase {
	
	//private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("elections-pu-test");
	
	//private EntityManager em;
	
	public CustomizationDataTest(String testName )
    {
        super( testName );
        //em = emf.createEntityManager();
        
    }
	
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CustomizationDataTest.class );
    }
    
    
    public void testCustomization()
    {
    	assertTrue(true);
    	//assertTrue(em != null);
    	
    	//PersonalizacionDao persDao = new PersonalizacionDao(em);
    	
    	//assertTrue(persDao != null);
    	
    	//Customization customization = persDao.getPersonalizacion();
    	
    	
    	//assertTrue(customization != null);
    	
    	//Long idCust = customization.getIdCstomization();
    	//String smallLogo = customization.getPicSmallLogo();
    	//String bigLogo = customization.getPicBigLogo();
    	//String symbol = customization.getPicSymbol();
    	
    	
    	//assertTrue(idCust == 1);
    	//assertTrue(smallLogo != null);
    	//assertFalse(smallLogo.equals(""));
    	
    	//assertTrue(bigLogo != null);
    	//assertFalse(bigLogo.equals(""));

    	//assertTrue(symbol != null);
    	//assertFalse(symbol.equals(""));

    	
    	
    	
    	
    	
    	
    }

}
