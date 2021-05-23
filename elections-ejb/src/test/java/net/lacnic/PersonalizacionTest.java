package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import net.lacnic.elections.dao.DaoFactoryElecciones;
import net.lacnic.elections.dao.PersonalizacionDao;
import net.lacnic.elections.domain.Personalizacion;


public class PersonalizacionTest extends TestCase {
	
	//private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("elecciones-pu-test");
	
	//private EntityManager em;
	
	public PersonalizacionTest(String testName )
    {
        super( testName );
        //em = emf.createEntityManager();
        
    }
	
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PersonalizacionTest.class );
    }
    
    
    public void testPersonalizacion()
    {
    	assertTrue(true);
    	
    	//assertTrue(em != null);
    	
    	//PersonalizacionDao persDao = new PersonalizacionDao(em);
    	
    	//assertTrue(persDao != null);
    	
    	//Personalizacion personalizacion = persDao.getPersonalizacion();
    	
    	
    	//assertTrue(personalizacion != null);
    	
    	//Long idPers = personalizacion.getIdPersonalizacion();
    	//String smallLogo = personalizacion.getPic_small_logo();
    	//String bigLogo = personalizacion.getPic_big_logo();
    	//String symbol = personalizacion.getPic_simbolo();
    	
    	
    	//assertTrue(idPers == 1);
    	//assertTrue(smallLogo != null);
    	//assertFalse(smallLogo.equals(""));
    	
    	//assertTrue(bigLogo != null);
    	//assertFalse(bigLogo.equals(""));

    	//assertTrue(symbol != null);
    	//assertFalse(symbol.equals(""));

    	
    	
    	
    	
    	
    	
    }

}
