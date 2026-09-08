package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
 class AppTest 
    extends TestCase
{
    /**
     * @return the suite of tests being tested
     */
     static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    @org.junit.jupiter.api.Test
     void testApp()
    {
        assertTrue( true );
    }
}
