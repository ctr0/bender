package org.ctro.bender;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ctro.bender.beans.BenderBeanSource;

/**
 * Unit test for BenderXml.
 */
public class BenderBeanTest extends TestCase {

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public BenderBeanTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(BenderBeanTest.class);
	}

	/**
	 * Rigourous Test :-)
	 * @throws BenderExeption 
	 */
	public void testApp() throws Exception {
		BenderSession session = new BenderSession();
		BenderBeanSource source = new BenderBeanSource(session, new Node());
		source.accept(new NodeVisitor1());
	}
	
}
