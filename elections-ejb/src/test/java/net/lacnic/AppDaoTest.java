package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;




 class AppDaoTest extends TestCase {
	
	/**
     * @return the suite of tests being tested
     */
     static Test suite()
    {
        return new TestSuite( AppDaoTest.class );
    }
    
    @org.junit.jupiter.api.Test
    
     void testDao()
    {
    	assertTrue(true);
    }
    	
}
