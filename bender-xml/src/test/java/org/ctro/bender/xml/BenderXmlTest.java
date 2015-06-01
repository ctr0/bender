package org.ctro.bender.xml;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ctro.bender.BenderSession;

/**
 * Unit test for BenderXml.
 */
public class BenderXmlTest extends TestCase {

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public BenderXmlTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(BenderXmlTest.class);
	}

	/**
	 * Rigourous Test :-)
	 * @throws BenderExeption 
	 */
	public void testApp() throws Exception {
		BenderSession session = new BenderSession();
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/ctro/bender/xml/test.xml");
		BenderXmlSource source = new BenderXmlSource(session, stream);
		source.accept(new BenderXmlVisitor());
	}
	
	public static class TextXmlVisitor  {
		
		@BenderXml(value="test/tag1")
		public void visitAttr1() {
			
			
		}

		public void visitEnd() {}
		
	}
}
