package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;




public class AppDaoTest extends TestCase {
	
	public AppDaoTest(String testName ) {
		super(testName);
	}
	
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppDaoTest.class );
    }
    
    public void testDao()
    {
    	assertTrue(true);
    }
    	
}
